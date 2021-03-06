package jp.ac.osaka_u.ist.sel.ccinvec.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class AllData implements  Serializable {

	public  ArrayList<SourceFile> SourceFileList;
	public ArrayList<ClonePair> ClonePairList;
	public  ArrayList<Block> BlockListOfCalcedVec;
	public  Map<String, Integer> wordMap;
	public  Map<String, Integer> wordMapSource;
	public  int wordFreq[];
	public  int dimension;
	public String detectingCommitId = null;
	public String detectingLocalPath = null;
	public  boolean pre_Diff = true;
	public int nextNumWordForRecalc =0;
	public int numWordForCalc =0;
	public  int countMethod =0;


	/**
	 * <p>ソースファイルリストの取得</p>
	 * @return ソースファイルリストオブジェクト
	 */
	public ArrayList<SourceFile> getSourceFileList() {
		return SourceFileList;
	}

	/**
	 * <p>ソースファイルリストの設定</p>
	 * @param SourceFileList
	 */
	public void setSourceFileList(ArrayList<SourceFile> SourceFileList) {
		this.SourceFileList = SourceFileList;
	}

	/**
	 * <p>クローンペアリストの取得</p>
	 * @return クローンペアリストオブジェクト
	 */
	public ArrayList<ClonePair> getClonePairList() {
		return ClonePairList;
	}

	/**
	 * <p>クローンペアリストの設定</p>
	 * @param clonePairList2
	 */
	public void setClonePairList(ArrayList<ClonePair> clonePairList) {
		this.ClonePairList = clonePairList;
	}

	/**
	 * <p>ベクトル計算済みのブロックリストの取得</p>
	 * @return ブロックリストリストオブジェクト
	 */
	public ArrayList<Block> getBlockListOfCalcedVec() {
		return BlockListOfCalcedVec;
	}

	/**
	 * <p>>ベクトル計算済みのブロックリストの設定</p>
	 * @param ブロックリスト
	 */
	public void setBlockListOfCalcedVec(ArrayList<Block> blockList) {
		this.BlockListOfCalcedVec = blockList;
	}

	/**
	 * <p>ワードマップの取得</p>
	 * @return ワードマップ
	 */
	public Map<String, Integer> getWordMap() {
		return wordMap;
	}

	/**
	 * <p>ワードマップの設定</p>
	 * @param ワードマップ
	 */
	public void setWordMap( Map<String, Integer> wordMap) {
		this.wordMap = wordMap;
	}

	/**
	 * <p>ワードマップの取得</p>
	 * @return ワードマップ
	 */
	public Map<String, Integer> getWordMapSource() {
		return wordMapSource;
	}

	/**
	 * <p>ワードマップの設定</p>
	 * @param ワードマップ
	 */
	public void setWordMapSource( Map<String, Integer> wordMapSource) {
		this.wordMapSource = wordMapSource;
	}

	/**
	 * <p>ワードマップの取得</p>
	 * @return ワードマップ
	 */
	public int[] getWordFreq() {
		return wordFreq;
	}

	/**
	 * <p>ワードマップの設定</p>
	 * @param ワードマップ
	 */
	public void setWordFreq(int[] wordFreq) {
		this.wordFreq = wordFreq;
	}


	/**
	 * <p>ワードマップの取得</p>
	 * @return ワードマップ
	 */
	public int getVecDimension() {
		return dimension;
	}

	/**
	 * <p>ワードマップの設定</p>
	 * @param ワードマップ
	 */
	public void setVecDimension(int dimension) {
		this.dimension = dimension;
	}


	/**
	 * <p>ソースコード中の関数の数の取得</p>
	 * @return ソースコード中の関数の数
	 */
	public int getCountMethodForCalc() {
		return countMethod;
	}

	/**
	 * <p>ソースコード中の関数の数の設定</p>
	 * @param ソースコード中の関数の数
	 */
	public void setCountMethodForCalc(int countMethod) {
		this.countMethod = countMethod;
	}

	/**
	 * <p>次にIDFを再計算するソースコード中の単語の数の取得</p>
	 * @return ソースコード中の関数の数
	 */
	public int getNextNumWordForRecalc() {
		return nextNumWordForRecalc;
	}

	/**
	 * <p>次にIDFを再計算するソースコード中の単語の数の設定</p>
	 * @param ソースコード中の関数の数
	 */
	public void setNextNumWordForRecalc(int nextNumWordForRecalc) {
		this.nextNumWordForRecalc = nextNumWordForRecalc;
	}

	/**
	 * <p>現在の計算に使用している単語の数の取得</p>
	 * @return ソースコード中の関数の数
	 */
	public int getNumWordForCalc() {
		return numWordForCalc;
	}

	/**
	 * <p>現在の計算に使用している単語の数の設定</p>
	 * @param ソースコード中の関数の数
	 */
	public void setNumWordForCalc(int numWordForCalc) {
		this.numWordForCalc = numWordForCalc;
	}


	/**
	 * <p>前回diffがあったかどうか</p>
	 * @return pre_diff
	 */
	public boolean getPreDiff() {
		return pre_Diff;
	}

	/**
	 * <p>前回diffがあったかどうか</p>
	 * @param pre_diff
	 */
	public void setPreDiff(boolean pre_Diff) {
		this.pre_Diff = pre_Diff;
	}


	public String getDetectingCommitId() {
		return detectingCommitId;
	}


	public void setDetectingCommitId(String detectingCommitId) {
		this.detectingCommitId = detectingCommitId;
	}


	public String getDetectingLocalPath() {
		return detectingLocalPath;
	}


	public void setDetectingLocalPath(String detectingLocalPath) {
		this.detectingLocalPath = detectingLocalPath;
	}

//	/**
//	 * <p>
//	 * allDataをシリアライズ化
//	 * <p>
//	 * @param allData
//	 */
//	public static void serializeAllDataList(AllData allData, Config config) {
//
//		File folder = new File(config.getDataDir());
//
//		// フォルダの存在を確認する
//		if (!folder.exists()) {
//			if(folder.mkdirs()) {
//			}
//		}
//
//		try {
//			ObjectOutputStream objOutStream =
//					new ObjectOutputStream(
//							new FileOutputStream(config.getDataDir() + "\\allData.bin"));
//			objOutStream.writeObject(allData);
//			objOutStream.close();
//
//			ObjectOutputStream objOutStreamBlockList =
//					new ObjectOutputStream(
//							new FileOutputStream(config.getDataDir() + "\\blockList.bin"));
//			objOutStreamBlockList.writeObject(allData.getBlockListOfCalcedVec());
//			objOutStreamBlockList.close();
//
//			ObjectOutputStream objOutStreamSourceFileList =
//					new ObjectOutputStream(
//							new FileOutputStream(config.getDataDir() + "\\sourceFileList.bin"));
//			objOutStreamSourceFileList.writeObject(allData.getSourceFileList());
//			objOutStreamSourceFileList.close();
//
//			ObjectOutputStream objOutStreamClonePairList =
//					new ObjectOutputStream(
//							new FileOutputStream(config.getDataDir() + "\\clonePairList.bin"));
//			objOutStreamClonePairList.writeObject(allData.getClonePairList());
//			objOutStreamClonePairList.close();
//
//			ObjectOutputStream objOutStreamWordMap =
//					new ObjectOutputStream(
//							new FileOutputStream(config.getDataDir() + "\\wordMap.bin"));
//			objOutStreamWordMap.writeObject(allData.getWordMap());
//			objOutStreamWordMap.close();
//
//			ObjectOutputStream objOutStreamWordFreq =
//					new ObjectOutputStream(
//							new FileOutputStream(config.getDataDir() + "\\wordFreq.bin"));
//			objOutStreamWordFreq.writeObject(allData.getWordFreq());
//			objOutStreamWordFreq.close();
//
//			ObjectOutputStream objOutStreamVecDimension =
//					new ObjectOutputStream(
//							new FileOutputStream(config.getDataDir() + "\\vecDimension.bin"));
//			objOutStreamVecDimension.writeObject(allData.getVecDimension());
//			objOutStreamVecDimension.close();
//
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//
//	/**
//	 * <p>
//	 * allDataをデシリアライズ化
//	 * <p>
//	 * @param allData
//	 */
//	public static AllData deserializeAllDataList(Config config) {
//		try {
//			String DataDir = config.getDataDir();
//			ObjectInputStream objInStream
//			= new ObjectInputStream(
//					new FileInputStream(DataDir + "\\allData.bin"));
//			AllData allData = (AllData) objInStream.readObject();
//			objInStream.close();
//
//			System.out.println("alldata vec 2 " + allData.getVecDimension());
//
//			ObjectInputStream objInStreamBlockList
//			= new ObjectInputStream(
//					new FileInputStream(DataDir + "\\blockList.bin"));
//			allData.setBlockListOfCalcedVec((ArrayList<Block>) objInStreamBlockList.readObject());
//			objInStreamBlockList.close();
//
//			ObjectInputStream objInStreamSourceFileList
//			= new ObjectInputStream(
//					new FileInputStream(DataDir + "\\sourceFileList.bin"));
//			allData.setSourceFileList((ArrayList<SourceFile>) objInStreamSourceFileList.readObject());
//			objInStreamSourceFileList.close();
//
//			ObjectInputStream objInStreamClonePairList
//			= new ObjectInputStream(
//					new FileInputStream(DataDir + "\\clonePairList.bin"));
//			allData.setClonePairList((ArrayList<ClonePair>) objInStreamClonePairList.readObject());
//			objInStreamClonePairList.close();
//
//			ObjectInputStream objInStreamWordMap
//			= new ObjectInputStream(
//					new FileInputStream(DataDir + "\\wordMap.bin"));
//			allData.setWordMap((Map<String, Integer>) objInStreamWordMap.readObject());
//			objInStreamWordMap.close();
//
//			ObjectInputStream objInStreamWordFreq
//			= new ObjectInputStream(
//					new FileInputStream(DataDir + "\\wordFreq.bin"));
//			allData.setWordFreq((int[]) objInStreamWordFreq.readObject());
//			objInStreamWordFreq.close();
//
//			return allData;
//
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	/**
//	 * <p>
//	 * allDataのSourceFileListとClonePairListの同期をとる
//	 * <p>
//	 * @throws IOException
//	 */
//	public void synchronizeAllData(Config config) throws IOException {
//
//		ArrayList<Block> blockList = new ArrayList<>();
//		blockList.addAll(createBlockList(config.getVecMethod()));
//		//blockList.addAll(createBlockList());
//
//		//        FileWriter fw = new FileWriter("test.txt");
//		//
//		if(CloneDetector.modeDebug) {
//			System.out.println(" ===  block list size  " + blockList.size());
//			System.out.println(" ===  calced block list size  " + BlockListOfCalcedVec.size());
//		}
//
//		Iterator<ClonePair> i = ClonePairList.iterator();
//		int j = 0;
//		int deleteCP = 0;
//		while(i.hasNext()) {
//			ClonePair cp  = i.next();
//			int idA = cp.cloneA.getId();
//			int idB = cp.cloneB.getId();
//			int bLSize = blockList.size();
//			if(bLSize >=  idA && bLSize >= idB) {
//				if((Block.equalsCodeInfo(cp.cloneA, blockList.get(idA)) &&
//						(!Block.equalsCodeInfo(cp.cloneB, blockList.get(idB))))) {
//					cp.cloneA = null;
//					cp.cloneB = null;
//
//					cp.setCloneA(blockList.get(idA));
//					cp.setCloneB(blockList.get(idB));
//				}else {
//					boolean cpAset = false;
//					boolean cpBset = false;
//					for(Block block : blockList) {
//						if(Block.equalsCodeInfo(block, cp.cloneA)) {
//							cp.cloneA = null;
//							cp.setCloneA(block);
//							cpAset = true;
//						}
//						if(Block.equalsCodeInfo(block, cp.cloneB)) {
//							cp.cloneB = null;
//							cp.setCloneB(block);
//							cpBset = true;
//						}
//					}
//					if(!cpAset || !cpBset) {
//						//				System.out.println("remove clone pair");
//						i.remove();
//					}
//
//				}
//			}else {
//				i.remove();
//			}
//		}
//
//		//		for(ClonePair cp : ClonePairList) {
//		//			int idA = cp.cloneA.getId();
//		//			int idB = cp.cloneB.getId();
//		//
//		//
//		//			if(cp.cloneA.getStartLine() != blockList.get(idA).getStartLine() || cp.cloneA.getEndLine() !=  blockList.get(idA).getEndLine()) {
//		//				//	if(CloneDetector.modeDebug) {
//		//				System.out.println("no match startline cloneA  startline" + cp.cloneA.getStartLine());
//		//				System.out.println("no match end  line cloneA  end  line" + cp.cloneA.getEndLine());
//		//				System.out.println("blockList                  startline" + blockList.get(idA).getStartLine());
//		//				System.out.println("blockList                  end  line" + blockList.get(idA).getEndLine());
//		//				System.out.println("cloneA  file" + cp.cloneA.getFileName());
//		//				System.out.println("blockLT file" + blockList.get(idA).getFileName());
//		//				System.out.println("cloneA  cate" + cp.cloneA.getCategory());
//		//				System.out.println("blockLT cate" + blockList.get(idA).getCategory());
//		//				System.out.println("no match startline cloneB  startline" + cp.cloneB.getStartLine());
//		//				System.out.println("no match end  line cloneB  end  line" + cp.cloneB.getEndLine());
//		//				System.out.println("cloneB  file" + cp.cloneB.getFileName());
//		//				System.out.println();
//		//				System.out.println();
//		//
//		//				//	}
//		//			}
//		//
//		//			if(cp.cloneB.getStartLine() != blockList.get(idB).getStartLine() || cp.cloneB.getEndLine() !=  blockList.get(idB).getEndLine()) {
//		//				//if(CloneDetector.modeDebug) {
//		//				System.out.println("no match startline cloneB  startline" + cp.cloneB.getStartLine());
//		//				System.out.println("no match end  line cloneA  end  line" + cp.cloneB.getEndLine());
//		//				System.out.println("blockList                  startline" + blockList.get(idB).getStartLine());
//		//				System.out.println("blockList                  end  line" + blockList.get(idB).getEndLine());
//		//				System.out.println("cloneB  file" + cp.cloneB.getFileName());
//		//				System.out.println("blockLT file" + blockList.get(idB).getFileName());
//		//				System.out.println("cloneB  cate" + cp.cloneB.getCategory());
//		//				System.out.println("blockLT cate" + blockList.get(idB).getCategory());
//		//				System.out.println("no match startline cloneA  startline" + cp.cloneA.getStartLine());
//		//				System.out.println("no match end  line cloneA  end  line" + cp.cloneA.getEndLine());
//		//				System.out.println("cloneA  file" + cp.cloneA.getFileName());
//		//				System.out.println();
//		//				System.out.println();
//		//				//}
//		//			}
//		//
//		//			if((cp.cloneA.getStartLine() != blockList.get(idA).getStartLine() || cp.cloneA.getEndLine() !=  blockList.get(idA).getEndLine()) ||
//		//					(cp.cloneB.getStartLine() != blockList.get(idB).getStartLine() || cp.cloneB.getEndLine() !=  blockList.get(idB).getEndLine())) {
//		//				ClonePairList.remove(cp);
//		//				System.out.println("remove clone pair");
//		//			}
//		//
//		//
//		//
//		//			//			fw.write("============= = \r\n");
//		//			//			fw.write("clone A = " + cp.cloneA.getId() + "\r\n");
//		//			//			fw.write("clone B = " + cp.cloneB.getId());
//		//			//			fw.write("clone A fileName = " + cp.cloneA.getFileName() + "\r\n");
//		//			//			fw.write("clone B fileName = " + cp.cloneB.getFileName() + "\r\n");
//		//			//			fw.write("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine() + "\r\n");
//		//			//			fw.write("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine() + "\r\n");
//		//			//			fw.write("============= =  \\r\\n");
//		//
//		//			cp.cloneA = null;
//		//			cp.cloneB = null;
//		//
//		//			cp.setCloneA(blockList.get(idA));
//		//			cp.setCloneB(blockList.get(idB));
//		//
//		//		}
//
//		//		fw.close();
//
//
//	}
//
//	/**
//	 * <p>
//	 * allDataのSourceFileListとClonePairListの同期をとる
//	 * <p>
//	 */
//	public static ArrayList<Block> createBlockList(int vecMethod) {
//		ArrayList<Block> blockList = new ArrayList<>();
//		for (SourceFile file : SourceFileList) {
//			for(Block block :file.getNewBlockList()) {
//				if(pre_Diff) {
//					if(block.getFilterCategory() == Block.PASSFILTER) {
//						block.setFilterCategory(Block.NO_FILTER);
//						//						System.out.println("PASS FILTER");
//						blockList.add(block);
//					}
//					else {
//						//						System.out.println("not added  " + block.getFileName());
//						//						System.out.println("start line " + block.getStartLine());
//						//						System.out.println("end   line " + block.getEndLine());
//					}
//				}else {
//					if(block.getPreFilterCategory() == Block.PASSFILTER) {
//						//						System.out.println("PASS FILTER");
//						blockList.add(block);
//					}
//					else {
//						//						System.out.println("not added  " + block.getFileName());
//						//						System.out.println("start line " + block.getStartLine());
//					}
//
//				}
//
//			}
//			//blockList.addAll(file.getNewBlockList());
//		}
//		int i =0;
//		for(Block block : blockList) {
//			//			if(BlockListOfCalcedVec.get(i).getWordList() == null) {
//			//				System.out.println("vec null " + i );
//			//			}
//			Block calcedVecBlock = BlockListOfCalcedVec.get(i);
//			if(Block.equalsCodeInfo(block, calcedVecBlock)) {
//				block.setWordList(calcedVecBlock.getWordList());
//				block.setVector(calcedVecBlock.getVector());
//				block.setLen(calcedVecBlock.getLen());
//				block.setCategory(Block.NULL);
//			}else {
//				boolean blockExist = false;
//				for(Block calcedBlock : BlockListOfCalcedVec) {
//					if(Block.equalsCodeInfo(block, calcedBlock)) {
//						block.setWordList(calcedBlock.getWordList());
//						block.setVector(calcedBlock.getVector());
//						block.setLen(calcedBlock.getLen());
//						block.setCategory(Block.NULL);
//						blockExist = true;
//						break;
//					}
//				}
//				if(!blockExist) {
//					if(vecMethod == Config.BoW) {
//						block.setCategory(Block.NULL);
//						VectorCalculator.increCalcBoW(block, wordMap, wordMapSource, CloneDetector.countMethod, dimension);
//					}else if(vecMethod == Config.TFIDF) {
//						VectorCalculator.increCalcTfIdf(block, wordMap, wordMapSource,wordFreq ,countMethod, dimension);
//					}
//				}
//			}
//			//	System.out.println("ID " + block.getId());
//			//block.setId(i);
//			i++;
//		}
//		return blockList;
//	}
//
//
//	public void dataClear() {
//		SourceFileList = null;
//		ClonePairList = null;
//		BlockListOfCalcedVec = null;
//		wordMap = null;
//		wordFreq = null;
//	}



}