package jp.ac.osaka_u.ist.sel.ccinvec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.util.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.clique.DegeneracyBronKerboschCliqueFinder;
import org.jgrapht.alg.interfaces.MaximalCliqueEnumerationAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import jp.ac.osaka_u.ist.sel.ccinvec.model.Block;
import jp.ac.osaka_u.ist.sel.ccinvec.model.ClonePair;
import jp.ac.osaka_u.ist.sel.ccinvec.model.CloneSet;
import jp.ac.osaka_u.ist.sel.ccinvec.model.RealVectorUtil;


public class CloneJudgement {
	/**
	 * <p>
	 * クローンペアの検出
	 * </p>
	 *
	 * @return
	 * @throws IOException
	 */
	public ArrayList<ClonePair> getClonePairList(List<Block> blockList, Config config) {
		//		System.out.println("parallel calc distanse");
		long start = System.currentTimeMillis();
		int numHardThread = Runtime.getRuntime().availableProcessors();
		/*	if (config.getThreads() == 0 || config.getThreads() > numHardThread)
			config.setThreads(numHardThread);*/
		if (Config.NUM_THREADS == 0 || Config.NUM_THREADS > numHardThread)
			Config.NUM_THREADS = numHardThread;

		//		ExecutorService executor = Executors.newFixedThreadPool(config.getThreads());
		//	System.out.println("The number of threads : " + config.getThreads());
		ExecutorService executor = Executors.newFixedThreadPool(Config.NUM_THREADS);
		//		System.out.println("The number of threads : " + Config.NUM_THREADS);

		List<Callable<Double>> tasks = new ArrayList<Callable<Double>>();
		List<Pair<Integer, Integer>> pairList = new ArrayList<Pair<Integer, Integer>>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(CloneDetector.LSH_FILE));
			String line = null;
			int i = 0;
			// ExecutorService executor = Executors.newFixedThreadPool(1);

			List<Integer> methodIdList = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Query point")) {
					methodIdList = new ArrayList<Integer>();
					// Map<Integer, Double> methodList = new TreeMap<Integer,
					// Double>();
				} else if (line.matches("\\d+")) {
					if (methodIdList != null) {
						methodIdList.add(Integer.valueOf(line.replaceAll("\t.*", "")));
					} else {
						System.err.println("can't read lsh_result.txt.");
					}
				} else if (line.equals("")) {
					Collections.sort(methodIdList);

					for (Integer methodId : methodIdList) {
						// Double diff =
						// Double.valueOf(line.split("\t")[1].replace("Distance:",""));
						//クラスタ内の自分の番号より上のコード片を対象に類似度を計算
						if (i < methodId) {
							tasks.add(new parallelGetClonePair(blockList.get(i), blockList.get(methodId)));
							pairList.add(new Pair<Integer, Integer>(i, methodId));
						}
					}
					i++;
					methodIdList = null;
				}
			}
			reader.close();

		} catch (FileNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		//System.out.println("task add done");
		ArrayList<ClonePair> clonePairList = new ArrayList<ClonePair>(tasks.size());

		try {
			List<Future<Double>> futures;
			try {
				//類似度が閾値以下はここで省かれている
				futures = executor.invokeAll(tasks);
				tasks = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.err.println(e);
				return null;
			}
			Double sim;
			int i = 0;
			for (Future<Double> future : futures) {
				try {

					if ((sim = future.get()) != null) {
						clonePairList.add(new ClonePair(blockList.get(pairList.get(i).getFirst()),
								blockList.get(pairList.get(i).getSecond()), sim));
					}
					i++;
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println(e);
				}
			}

		} finally {
			if (executor != null)
				executor.shutdown();
		}
		//		System.out.print("parallel calc distanse done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");
		//
		//		System.out.println("filtering start");
		start = System.currentTimeMillis();

		ArrayList<ClonePair> newClonePairList = new ArrayList<ClonePair>(clonePairList.size());

		for (ClonePair pair : clonePairList)
			if (filteringPair(clonePairList, newClonePairList, pair.cloneA, pair.cloneB))
				newClonePairList.add(pair);

		newClonePairList.trimToSize();
		clonePairList = newClonePairList;
		//		System.out.print("filtering done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");

		return clonePairList;
	}

	/**
	 * <p>
	 * インクリメンタルにクローンペアの検出
	 * </p>
	 *
	 * @return
	 * @throws IOException
	 */
	public ArrayList<ClonePair> getClonePairListPartially(ArrayList<Block> blockList, ArrayList<Block> addedModifiedBlockList, ArrayList<ClonePair> originClonePairList, Config config) {
		//		System.out.println("parallel calc distanse");
		long start = System.currentTimeMillis();
		int numHardThread = Runtime.getRuntime().availableProcessors();
		if (Config.NUM_THREADS == 0 || Config.NUM_THREADS > numHardThread)
			//config.setThreads(numHardThread);
			Config.NUM_THREADS = numHardThread;

		ExecutorService executor = Executors.newFixedThreadPool(Config.NUM_THREADS);
		//		System.out.println("The number of threads : " + Config.NUM_THREADS);

		List<Callable<Double>> tasks = new ArrayList<Callable<Double>>();
		List<Pair<Integer, Integer>> pairList = new ArrayList<Pair<Integer, Integer>>();
		ArrayList<Integer> qpList = new ArrayList<Integer>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(CloneDetector.LSH_FILE));
			String line = null;
			int i = 0;
			int qp = 0;

			// ExecutorService executor = Executors.newFixedThreadPool(1);

			List<Integer> methodIdList = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Query point")) {
					qp = addedModifiedBlockList.get(i).getId();
					//QueryPointListにblockIDを追加
					qpList.add(qp);
					methodIdList = new ArrayList<Integer>();
					//					System.out.println(qp + "qp fileName" + blockList.get(qp).getFileName());
					//					System.out.println(qp + "qp  start " + blockList.get(qp).getStartLine() + " end " + blockList.get(qp).getEndLine() );
					// Map<Integer, Double> methodList = new TreeMap<Integer,
					// Double>();
				} else if (line.matches("\\d+")) {
					if (methodIdList != null) {
						methodIdList.add(Integer.valueOf(line.replaceAll("\t.*", "")));
					} else {
						System.err.println("can't read lsh_result.txt.");
					}
				} else if (line.equals("")) {
					Collections.sort(methodIdList);

					//	System.out.println("qp = " + qp);
					for (Integer methodId : methodIdList) {
						// Double diff =
						// Double.valueOf(line.split("\t")[1].replace("Distance:",""));
						//クラスタ内の自分の番号より上のコード片を対象に類似度を計算
						//
						//		System.out.println("mId = " + methodId);

						if (qp <  methodId) {
							//							System.out.println("query point  = " + i);
							//							System.out.println(qp + "qp fileName" + blockList.get(qp).getFileName());
							//							System.out.println(qp + "qp  start " + blockList.get(qp).getStartLine() + " end " + blockList.get(qp).getEndLine() );
							//							System.out.println(methodId + "methodiD fileName" + blockList.get(methodId).getFileName());
							//							System.out.println(methodId + "methodID start " + blockList.get(methodId).getStartLine() + " end " + blockList.get(methodId).getEndLine() );
							tasks.add(new parallelGetClonePair(blockList.get(qp), blockList.get(methodId)));
							//tasks.add(new parallelGetClonePair(addedModifiedBlockList.get(i), blockList.get(methodId)));
							pairList.add(new Pair<Integer, Integer>(qp, methodId));
						}else if(qpList.indexOf(methodId) < 0) {
							//							System.out.println("query point  = " + i);
							//							System.out.println(qp + "qp fileName" + blockList.get(qp).getFileName());
							//							System.out.println(qp + "qp  start " + blockList.get(qp).getStartLine() + " end " + blockList.get(qp).getEndLine() );
							//							System.out.println(methodId + "methodiD fileName" + blockList.get(methodId).getFileName());
							//							System.out.println(methodId + "methodID start " + blockList.get(methodId).getStartLine() + " end " + blockList.get(methodId).getEndLine() );
							tasks.add(new parallelGetClonePair(blockList.get(qp), blockList.get(methodId)));
							//tasks.add(new parallelGetClonePair(addedModifiedBlockList.get(i), blockList.get(methodId)));
							pairList.add(new Pair<Integer, Integer>(qp, methodId));
						}
					}
					i++;
					methodIdList = null;
				}
			}
			reader.close();

		} catch (FileNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		//		System.out.println("task add done");
		ArrayList<ClonePair> clonePairList = new ArrayList<ClonePair>(tasks.size());

		try {
			List<Future<Double>> futures;
			try {
				//類似度が閾値以下はここで省かれている
				futures = executor.invokeAll(tasks);
				tasks = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.err.println(e);
				return null;
			}
			Double sim;
			int i = 0;
			for (Future<Double> future : futures) {
				try {

					if ((sim = future.get()) != null) {
						clonePairList.add(new ClonePair(blockList.get(pairList.get(i).getFirst()),
								blockList.get(pairList.get(i).getSecond()), sim));
						//					System.out.println("add clonepair " + i);
					}
					i++;
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println(e);
				}
			}

		} finally {
			if (executor != null)
				executor.shutdown();
		}
		//		System.out.print("parallel calc distanse done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");
		//
		//		System.out.println("filtering start");
		start = System.currentTimeMillis();

//		ArrayList<ClonePair> newClonePairList = new ArrayList<ClonePair>(clonePairList.size());

		for (ClonePair pair : clonePairList)
//			if (filteringPair(clonePairList, newClonePairList, pair.cloneA, pair.cloneB))
			if (filteringPair(clonePairList, originClonePairList, pair.cloneA, pair.cloneB))
				originClonePairList.add(pair);

		originClonePairList.trimToSize();
//		clonePairList = newClonePairList;
		if(CloneDetector.modeDebug) {
			System.out.print("filtering done : ");
			System.out.println(System.currentTimeMillis() - start + "[ms]");
			System.out.println("cloenpairList size = " + clonePairList.size());
			for(ClonePair cp : clonePairList) {
				System.out.println("clone A File      " + cp.cloneA.getFileName());
				System.out.println("clone A ID        " + cp.cloneA.getId());
				System.out.println("clone A startline " + cp.cloneA.getStartLine());
				System.out.println("clone A File      " + cp.cloneB.getFileName());
				System.out.println("clone B ID        " + cp.cloneB.getId());
				System.out.println("clone B startline " + cp.cloneB.getStartLine());
			}
		}

		return originClonePairList;
	}

	public ArrayList<ClonePair> getClonePairListNoLSH(List<Block> blockList, Config config) {
		//		System.out.println("parallel calc distanse no LSH");
		long start = System.currentTimeMillis();

		int numHardThread = Runtime.getRuntime().availableProcessors();
		if (Config.NUM_THREADS == 0 || Config.NUM_THREADS > numHardThread)
			Config.NUM_THREADS = numHardThread;
		/*	if (config.getThreads() == 0 || config.getThreads() > numHardThread)
			config.setThreads(numHardThread);
		 */
		ExecutorService executor = Executors.newFixedThreadPool(Config.NUM_THREADS);

		List<Callable<Double>> tasks = new ArrayList<Callable<Double>>();
		ArrayList<ClonePair> clonePairList = new ArrayList<ClonePair>();
		ArrayList<Pair<Integer, Integer>> pairList = new ArrayList<Pair<Integer, Integer>>();
		final int blockListSize = blockList.size();
		for (int i = 0; i < blockListSize; i++) {
			for (int j = i + 1; j < blockListSize; j++) {
				tasks.add(new parallelGetClonePair(blockList.get(i), blockList.get(j)));
				pairList.add(new Pair<Integer, Integer>(i, j));
			}
		}
		//		System.out.println("task add done");
		List<Future<Double>> futures;
		try {
			futures = executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			System.err.println(e);
			return null;

		} finally {
			if (executor != null)
				executor.shutdown();
		}
		//		System.out.println("excuter shutdown");
		try {
			Double sim;
			int i = 0;
			for (Future<Double> future : futures) {
				if ((sim = future.get()) != null)

					clonePairList.add(new ClonePair(blockList.get(pairList.get(i).getFirst()),
							blockList.get(pairList.get(i).getSecond()), sim));
				i++;
			}
		} catch (Exception e) {
			System.err.println(e);
		}

		//		System.out.print("parallel calc distanse done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");
		//
		//		System.out.println("filtering start");
		start = System.currentTimeMillis();

		ArrayList<ClonePair> newClonePairList = new ArrayList<ClonePair>();
		for (ClonePair pair : clonePairList)
			if (filteringPair(clonePairList,newClonePairList, pair.cloneA, pair.cloneB)) {
				newClonePairList.add(pair);
				//			System.out.println("add : " + pair);
			}

		clonePairList = newClonePairList;
		//		System.out.print("filtering done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");

		return clonePairList;
	}

	/**
	 * <p>
	 * クローンセットの検出 Bron-Kerbosch派生アルゴリズムを用いた極大クリーク列挙
	 * </p>
	 *
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<CloneSet> getCloneSetList(ArrayList<ClonePair> clonePairList, List<Block> blockList) {
		Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		for (ClonePair clonePair : clonePairList) {
			//	System.out.println("cloneA.getId() " + clonePair.cloneA.getId());
			//	System.out.println("cloneB.getId() " + clonePair.cloneB.getId());
			if(clonePair.cloneA.getId() != clonePair.cloneB.getId()) {
				graph.addVertex(clonePair.cloneA.getId());
				graph.addVertex(clonePair.cloneB.getId());
				graph.addEdge(clonePair.cloneA.getId(), clonePair.cloneB.getId());
			}
			//			System.out.println("cloneA.getId() " + clonePair.cloneA.getId());
			//
			//			if(clonePair.cloneA.getStartLine() == 716) {
			//				System.out.println("716 ID = "+  clonePair.cloneA.getId());
			//				System.out.println("717 ID = "+  clonePair.cloneB.getId());
			//				System.out.println("sarrt lne = "+  clonePair.cloneB.getStartLine());
			//
			//			}
			//
			//			//System.out.println("cloneB.getId() " + clonePair.cloneB.getId());
			//			int aU = blockList.indexOf(clonePair.cloneA);
			//			int bU = blockList.indexOf(clonePair.cloneB);
			//			if((aU != -1) || (bU != -1)) {
			//				System.out.println("noting  ");
			//				System.out.println("============= = ");
			//				System.out.println("clone A = " + clonePair.cloneA.getId());
			//				System.out.println("clone B = " + clonePair.cloneB.getId());
			//				System.out.println("============= = ");
			//
			//			}
		}

		/*	int i =0;
		for(Block block : blockList) {
			System.out.println("block " + i++);
			System.out.println("block ID" + block.getId());
		}*/

		MaximalCliqueEnumerationAlgorithm<Integer, DefaultEdge> cliqueFinder;
		// cliqueFinder = new BronKerboschCliqueFinder<>(graph);
		// cliqueFinder = new PivotBronKerboschCliqueFinder<>(graph);
		cliqueFinder = new DegeneracyBronKerboschCliqueFinder<>(graph);

		ArrayList<CloneSet> cloneSetList = new ArrayList<>();
		for (Set<Integer> clique : cliqueFinder) {
			CloneSet cloneSet = new CloneSet();
			clique = new TreeSet<>(clique); // セットをソート
			for (Integer v : clique) {
				//		System.out.println("v = " + v);
				//				if(blockList.get(v).getStartLine() == 716) {
				//					System.out.println("716 ID = "+  blockList.get(v).getId());
				//
				//				}
				//				if(blockList.get(v).getStartLine() == 717) {
				//					System.out.println("717 ID = "+  blockList.get(v).getId());
				//
				//				}
				//				if(v != blockList.get(v).getId()){
				//					System.out.print(" not match");
				//				}
				cloneSet.cloneList.add(blockList.get(v));
			}
			cloneSetList.add(cloneSet);
		}

		cliqueFinder = null;

		return cloneSetList;
	}

	/**
	 * <p>
	 * クローンコミュニティの検出
	 * </p>
	 *
	 * @return
	 * @throws IOException
	 */
	public static List<List<Block>> getCloneCommunityList(List<ClonePair> clonePairList, List<Block> blockList) {
		Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		for (ClonePair clonePair : clonePairList) {
			graph.addVertex(clonePair.cloneA.getId());
			graph.addVertex(clonePair.cloneB.getId());
			graph.addEdge(clonePair.cloneA.getId(), clonePair.cloneB.getId());
		}

		ConnectivityInspector<Integer, DefaultEdge> inspector = new ConnectivityInspector<>(graph);
		List<List<Block>> cloneCommunityList = new ArrayList<>();
		for (Set<Integer> connectedG : inspector.connectedSets()) {
			List<Block> cloneCommunity = new ArrayList<>();
			connectedG = new TreeSet<>(connectedG); // セットをソート
			for (Integer v : connectedG) {
				cloneCommunity.add(blockList.get(v));
			}
			cloneCommunityList.add(cloneCommunity);
		}

		return cloneCommunityList;
	}

	/**
	 * <p>
	 * 隣接リストの作成
	 * </p>
	 *
	 * @return
	 * @throws IOException
	 */
	private static List<List<Integer>> genAdjancencyList(final List<ClonePair> clonePairList, final int numBlock) {
		List<List<Integer>> adjList = new ArrayList<List<Integer>>(numBlock);
		for (int i = 0; i < numBlock; i++) {
			adjList.add(new ArrayList<>());
		}
		for (ClonePair clonePair : clonePairList) {
			adjList.get(clonePair.cloneA.getId()).add(clonePair.cloneB.getId());
			adjList.get(clonePair.cloneB.getId()).add(clonePair.cloneA.getId());
		}
		return adjList;
	}

	/**
	 * <p>
	 * 幅優先探索
	 * </p>
	 *
	 * @return
	 * @throws IOException
	 */
	private static List<Integer> bfs(final List<List<Integer>> adjList, final int root) {
		boolean[] visited = new boolean[adjList.size()];
		Queue<Integer> queue = new ArrayDeque<>();
		List<Integer> visitedList = new ArrayList<>();

		visited[root] = true;
		queue.offer(root);
		visitedList.add(root);

		while (!queue.isEmpty()) {
			int v = queue.poll();
			for (int u : adjList.get(v)) {
				if (!visited[u]) {
					visited[u] = true;
					queue.offer(u);
					visitedList.add(u);
				}
			}
		}
		Collections.sort(visitedList);
		return visitedList;
	}

	/**
	 * <p>
	 * クローンセットの検出
	 * </p>
	 *
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<CloneSet> getCloneSetListOld(ArrayList<ClonePair> clonePairList) {
		clonePairList.sort(new Comparator<ClonePair>() {
			@Override
			public int compare(ClonePair o1, ClonePair o2) {
				if (o1.cloneA.getId() != o2.cloneA.getId())
					return o1.cloneA.getId() - o2.cloneA.getId();
				else
					return o1.cloneB.getId() - o2.cloneB.getId();
			}
		});

		ArrayList<CloneSet> cloneSetList = new ArrayList<CloneSet>();
		for (ClonePair pair : clonePairList) {
			boolean addFlg = false;
			for (CloneSet set : cloneSetList) {
				boolean contA = false;
				boolean contB = false;
				for (Block clone : set.cloneList) {
					if (clone.getFileName().equals(pair.cloneA.getFileName())
							&& clone.getStartLine() == pair.cloneA.getStartLine()) {
						contA = true;
					}
					if (clone.getFileName().equals(pair.cloneB.getFileName())
							&& clone.getStartLine() == pair.cloneB.getStartLine()) {
						contB = true;
					}
				}
				if (contA && contB) {
					addFlg = true;
					break;
				} else if (contA && !contB) {
					addFlg = true;
					set.cloneList.add(pair.cloneB);
					break;
				} else if (!contA && contB) {
					addFlg = true;
					set.cloneList.add(pair.cloneA);
					break;
				}
			}
			if (!addFlg) {
				CloneSet set = new CloneSet();
				set.cloneList.add(pair.cloneA);
				set.cloneList.add(pair.cloneB);
				cloneSetList.add(set);
			}
		}

		for (CloneSet set : cloneSetList)
			set.calculateMetric();

		return cloneSetList;

	}

	private static double L2(double[] v1, double[] v2) {
		double s = 0;
		int size = v1.length;
		for (int i = 0; i < size; i++) {
			s += Math.pow((v1[i] - v2[i]), 2);
		}
		return s;
	}

	private static boolean filteringPair(ArrayList<ClonePair> clonePairList, ArrayList<ClonePair> newClonePairList, Block cloneA, Block cloneB) {
		//		//同じコード片でないかチェック
		//		if(isSameCode(cloneA, cloneB))
		//			return false;
		//親子関係でないかチェック
		if (isPairWithDescendants(cloneA, cloneB))
			return false;
		if (isPairWithDescendants(cloneB, cloneA))
			return false;
		//重複コードでないかチェック
		if (isDuplicatePair(clonePairList, cloneA, cloneB))
			return false;
		//同じクローンペアがないかチェック
		if (isSameClonePair(newClonePairList, cloneA, cloneB))
			return false;
		return true;
	}

	public static final boolean isPairWithDescendants(final Block parent, final Block child) {
		if (parent.equals(child))
			return true;
		if (!parent.getFileName().equals(child.getFileName()))
			return false;
		if (parent.getStartLine() <= child.getStartLine() && child.getEndLine() <= parent.getEndLine())
			return true;
		return false;
	}

	public static final boolean isSameCode(final Block codeA, final Block codeB) {
		if(codeA.getFileName() == codeB.getFileName() &&
				codeA.getStartLine() == codeB.getStartLine() &&
				codeA.getEndLine() == codeB.getEndLine())
			return true;

		return false;
	}

	public static final boolean isDuplicatePair(final ArrayList<ClonePair> clonePairList, final Block a, final Block b) {
		for (ClonePair pair : clonePairList) {
			final Block pairA = pair.cloneA;
			final Block pairB = pair.cloneB;
			if (isPairWithDescendants(pairA, a) && isPairWithDescendants(pairB, b))
				if (!pairA.equals(a) || !pairB.equals(b))
					return true;
			if (isPairWithDescendants(pairA, b) && isPairWithDescendants(pairB, a))
				if (!pairA.equals(b) || pairB.equals(a))
					return true;
		}
		return false;
	}

	public static final boolean isSameClonePair(final ArrayList<ClonePair> newClonePairList , final Block a, final Block b) {
		for (ClonePair pair : newClonePairList) {
			final Block pairA = pair.cloneA;
			final Block pairB = pair.cloneB;
			String aFile = a.getFileName();
			String bFile = b.getFileName();
			String pairAFile = pairA.getFileName();
			String pairBFile = pairB.getFileName();

			if(aFile == pairAFile) {
				if(bFile == pairBFile) {
					int aStart = a.getStartLine();
					int bStart = b.getStartLine();
					int aEnd = a.getStartLine();
					int bEnd = b.getStartLine();
					int pairAStart = pairA.getStartLine();
					int pairBStart = pairB.getStartLine();
					int pairAEnd = pairA.getStartLine();
					int pairBEnd = pairB.getStartLine();
					if(aStart == pairAStart && bStart == pairBStart &&
							aEnd == pairAEnd && bEnd == pairBEnd) {
						return true;
					}
				}
			}else if(aFile == pairBFile) {
				if(bFile == pairAFile) {
					int aStart = a.getStartLine();
					int bStart = b.getStartLine();
					int aEnd = a.getStartLine();
					int bEnd = b.getStartLine();
					int pairAStart = pairA.getStartLine();
					int pairBStart = pairB.getStartLine();
					int pairAEnd = pairA.getStartLine();
					int pairBEnd = pairB.getStartLine();
					if(aStart == pairBStart && bStart == pairAStart &&
							aEnd == pairBEnd && bEnd == pairAEnd) {
						return true;
					}

				}
			}

		}
		return false;
	}

	public static void removePairOfMethod(List<ClonePair> clonePairList) {
		int i = 0, size = clonePairList.size();
		while (i < size) {
			ClonePair clonePair = clonePairList.get(i);
			if (clonePair.cloneA.getParent() == null && clonePair.cloneB.getParent() == null) {
				clonePairList.remove(i);
				continue;
			}
			i++;
		}
	}


	public void sortClonePair(ArrayList<ClonePair> clonePair_test) {
		clonePair_test.sort(Comparator.comparing(ClonePair::getCloneAId).thenComparing(ClonePair::getCloneBId));


	}


	public void insertClonePairToList(ArrayList<ClonePair> clonePairList_test, ArrayList<ClonePair> addedClonePair) {
		// TODO 自動生成されたメソッド・スタブ
		//for(ClonePair intsertClonePair: addedClonePair) {
		//	int cloneAId = insertClonePair
		//}
		int i =0;
		int cloneAId = addedClonePair.get(i).cloneA.getId();
		int cloneBId = addedClonePair.get(i).cloneB.getId();
		int AId_pre;
		int AId;
		int BId_pre;
		int BId;
		/*ArrayList<ClonePair> clonePairList_sub = new ArrayList<ClonePair>();
		clonePairList_sub = new
		 */
		for(int k =1; k<clonePairList_test.size(); k++) {
			AId_pre= clonePairList_test.get(k-1).cloneA.getId();
			AId= clonePairList_test.get(k).cloneA.getId();
			cloneAId = addedClonePair.get(i).cloneA.getId();
			cloneBId = addedClonePair.get(i).cloneB.getId();
			if(cloneAId < AId_pre) {
				clonePairList_test.add(k, addedClonePair.get(i));
				i++;
			}else if((cloneAId == AId_pre) && (cloneAId == AId)) {
				BId_pre = clonePairList_test.get(k-1).cloneB.getId();
				BId = clonePairList_test.get(k).cloneB.getId();
				if((BId_pre < cloneBId) && (cloneBId < BId)) {
					clonePairList_test.add(k, addedClonePair.get(i));
					i++;
				}else if(BId_pre > cloneBId) {
					clonePairList_test.add(k-1, addedClonePair.get(i));
					i++;
				}else if(BId < cloneBId) {
					clonePairList_test.add(k, addedClonePair.get(i));
					i++;
				}
			}else if((cloneAId == AId_pre) && (cloneAId < AId)) {
				BId_pre = clonePairList_test.get(k-1).cloneB.getId();
				if(BId_pre > cloneBId) {
					clonePairList_test.add(k-1, addedClonePair.get(i));
					i++;
				}else if(BId_pre < cloneBId) {
					clonePairList_test.add(k, addedClonePair.get(i));
					i++;
				}
			}else if((k == clonePairList_test.size()) && (cloneAId > AId)) {
				clonePairList_test.add(k+1, addedClonePair.get(i));
				i++;
			}else if((k == clonePairList_test.size()) && (cloneAId < AId)) {
				clonePairList_test.add(k+1, addedClonePair.get(i));
				i++;

			}

		}
		/*	if(AId <= cloneAId) {
				BId = clonePairList.cloneB.getId();
				if(BId < cloneBId) {
					clonePairList_test.add(j, addedClonePair.get(i));
					i++;
					cloneAId = addedClonePair.get(i).cloneA.getId();
					cloneBId = addedClonePair.get(i).cloneB.getId();
				}
			}
			j++;
			clonePair.cloneA.getId();

		}*/

		/*	for(ClonePair clonePair: clonePairList_test) {
			AId= clonePair.cloneA.getId();
			if(AId <= cloneAId) {
				BId = clonePair.cloneB.getId();
				if(BId < cloneBId) {
					clonePairList_test.add(j, addedClonePair.get(i));
					i++;
					cloneAId = addedClonePair.get(i).cloneA.getId();
					cloneBId = addedClonePair.get(i).cloneB.getId();
				}
			}
			j++;
			clonePair.cloneA.getId();
		}*/

	}

	public void deleteDupulicatePair(ArrayList<ClonePair> clonePairList) {

		Iterator<ClonePair> i = clonePairList.iterator();
		int j =0;
		Block preCloneA = null;
		Block preCloneB = null;
		while(i.hasNext()){
			ClonePair pair = i.next();
			Block cloneA = pair.cloneA;
			Block cloneB = pair.cloneB;

			if(j>0) {
				if(Block.equalsCodeInfo(cloneA, preCloneA) && Block.equalsCodeInfo(cloneB, preCloneB)) {
					i.remove();
				}else {
					preCloneA = cloneA;
					preCloneB = cloneB;
				}
			}else {
				preCloneA = cloneA;
				preCloneB = cloneB;
			}
			j++;
		}

	}

}

class parallelGetClonePair implements Callable<Double> {
	final Block cloneA;
	final Block cloneB;

	public parallelGetClonePair(Block a, Block b) {
		this.cloneA = a;
		this.cloneB = b;
	}

	@Override
	public Double call() throws Exception {
		//クローン間のノード数の差が30以下かつ
		//

		//	System.out.println("clone A getNodeNum = " + cloneA.getNodeNum() + " cloneB.getNodeNum " + cloneB.getNodeNum() );
		//	System.out.println("clone A getlen = " + cloneA.getLen() + " cloneB.getLen " + cloneB.getLen() );

		if (Math.abs(cloneA.getNodeNum() - cloneB.getNodeNum()) < Config.DIFF_TH
				&& Math.abs(cloneA.getLen() - cloneB.getLen()) < Config.DIS_TH) {

			//	System.out.println(" ==== call call callS");
			if (Config.SIM_TH == 1.0)
				if (cloneA.getVector().equals(cloneB.getVector())) {
					//		System.out.println("same vec");
					return 1.0;
				}

			double sim = scalar(cloneA.getVector(), cloneB.getVector());
			if (sim >= Config.SIM_TH) {
				//					System.out.println("similar vec");
				return sim;
			}
		}else {
			//				System.out.println("not or similar vec");
		}
		return null;
	}

	private static double scalar(OpenMapRealVector v1, OpenMapRealVector v2) {
		double s = 0;
		for (int index : RealVectorUtil.getSparseIndexList(v1)) {
			s += v1.getEntry(index) * v2.getEntry(index);
		}
		return s;
	}

}
