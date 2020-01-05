package jp.ac.osaka_u.ist.sel.icvolti;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.OpenMapRealVector;

import jp.ac.osaka_u.ist.sel.icvolti.model.AllData;
import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
import jp.ac.osaka_u.ist.sel.icvolti.model.RealVectorUtil;
import jp.ac.osaka_u.ist.sel.icvolti.model.Word;

public class VectorCalculator implements Serializable {
	private static final int APPEARANCE_TH = 1;
	private static final int SPARSE = 0;
	private static final int DENSE = 1;
	private static int dimension;

	public int getDimension() {
		return dimension;
	}

	/**
	 * <p>
	 * メソッドリストのフィルタリング
	 * </p>
	 *
	 * @return
	 */
	public ArrayList<Block> filterMethod(ArrayList<Block> blockList, Config config, AllData allData) {
		ArrayList<Block> newBlockList = new ArrayList<Block>();
		int i = 0;
		for (Block block : blockList) {
			if (filter(block, config)) {
				/*
				 * && !('A' <= block.getName().charAt(0) && block.getName().charAt(0) <= 'Z')
				 */
				// !method.getName().contains("test") &&
				// !method.getName().contains("Test")&&
				// !method.getClassName().contains("test") &&
				// !method.getClassName().contains("Test")){
				block.setId(i++);
				block.setFilterCategory(Block.PASSFILTER);
				block.setPreFilterCategory(Block.PASSFILTER);
				newBlockList.add(block);
				//				System.out.println("block fine  Name " + block.getFileName());
				//				System.out.println("block start line " + block.getStartLine());
				//				System.out.println("block end   line " + block.getEndLine());
				/*
				 * for(Word word: block.getWordList()){
				 * if(!CloneDetector.wordMap.containsKey(word.getName()))
				 * CloneDetector.wordMap.put(word.getName(),i++); }
				 */
			}else {
				block.setFilterCategory(Block.NO_PASSFILTER);
				block.setPreFilterCategory(Block.NO_PASSFILTER);
				if(CloneDetector.modeDebug) {
					//					System.out.println("removed because of filtering ");
					//					System.out.println("block fine  Name " + block.getFileName());
					//					System.out.println("block start line " + block.getStartLine());
					//					System.out.println("block end   line " + block.getEndLine());
				}
			}
		}


		//		newBlockList.trimToSize();
		return newBlockList;
	}

	public ArrayList<Block> increFilterMethod(ArrayList<Block> blockList, Config config, AllData allData) {
		ArrayList<Block> newBlockList = new ArrayList<Block>();
		ArrayList<Block> deleteBlock = new ArrayList<Block>();
		int i = 0;
		for (Block block : blockList) {
			//ブロックリストフィルタリング
			if (filter(block, config)) {
				/*
				 * && !('A' <= block.getName().charAt(0) && block.getName().charAt(0) <= 'Z')
				 */
				// !method.getName().contains("test") &&
				// !method.getName().contains("Test")&&
				// !method.getClassName().contains("test") &&
				// !method.getClassName().contains("Test")){
				block.setId(i++);
				block.setFilterCategory(Block.PASSFILTER);
				block.setPreFilterCategory(Block.PASSFILTER);

				//				if(block.getVector() == null) {
				//					System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				//					System.out.println("vector null = " + block.getId());
				//					System.out.println("file Name  = " + block.getFileName());
				//					System.out.println("start line = " + block.getStartLine());
				//					System.out.println("end   line = " + block.getEndLine());
				//					System.out.println("category  = " + block.getCategoryString());
				//					System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				//				}
				newBlockList.add(block);

				/*
				 * for(Word word: block.getWordList()){
				 * if(!CloneDetector.wordMap.containsKey(word.getName()))
				 * CloneDetector.wordMap.put(word.getName(),i++); }
				 */
			}else if(CloneDetector.absoluteTracking && block.getCategory() == Block.MODIFIED && block.getPreFilterCategory() == Block.PASSFILTER) {
				block.setId(i++);
				block.setFilterCategory(Block.PASSFILTER);
				block.setPreFilterCategory(Block.PASSFILTER);
				newBlockList.add(block);
				//				System.out.println("DELETE");
			}else{
				//追加されたブロックでなければ，clonepairリストから削除
				if(block.getCategory() != Block.ADDED) {
					deleteBlock.add(block);
					//					System.out.println("DELETE2  2");
				}
				block.setFilterCategory(Block.NO_PASSFILTER);
				block.setPreFilterCategory(Block.NO_PASSFILTER);

				//				System.out.println("DELETE2  3");
			}
		}

		//		BlockUpdater.deleteClonePair(deleteBlock, allData);
		deleteBlock = null;

		//		newBlockList.trimToSize();
		return newBlockList;
	}


	private static boolean filter(Block block, Config config) {
		if (block.getParent() == null) {
			//親がいない（メソッド）かつトークン数が閾値以下であれば省く　config.get~にするとバグ
			if (block.getNodeNum() <Config.METHOD_NODE_TH)
				return false;
		} else {
			//親がいる（ブロック）トークン数が閾値以下あれば省く　config.get~にするとバグ
			if (block.getNodeNum() < Config.BLOCK_NODE_TH)
				return false;
		}
		//行数が閾値以下であれば省く config.get~にするとバグ
		if (block.getLineSize() < Config.LINE_TH)
			return false;

		return true;
	}

	/**
	 * <p>
	 * 重みの計算
	 * </p>
	 *
	 * @throws FileNotFoundException
	 */
	public void calculateVector(ArrayList<Block> blockList, AllData allData) throws FileNotFoundException {
		// ワードマップの生成
		HashMap<String, Integer> wordFreqMap = new HashMap<String, Integer>();
		ArrayList<String> dictionary = new ArrayList<String>();

		/**
		 * wordFreqMap どんなワードが何回出たかのデータを保持
		 * dictionary  1回以上出てきた単語を保持
		 *
		 */

		// ワードの出現頻度の計測と、ワード辞書の生成
		int elementCount = 0;
		for (Block block : blockList) {
			if (block.getParent() == null) {
				for (Word word : block.getWordList()) {
					if (wordFreqMap.containsKey(word.getName())) {
						//一度出てきたことがあるワードはwordFreqの出現回数をインクリメント
						int value = wordFreqMap.get(word.getName());
						wordFreqMap.put(word.getName(), ++value);
					} else {
						//新しくワードが出てきたらそれをwordFreqMapとdictionaryに登録
						wordFreqMap.put(word.getName(), 1);
						dictionary.add(word.getName());
					}
					elementCount++;
				}
			} else {
				elementCount += block.getWordList().size();
			}
		}

		//		System.out.println("blocklist size : " + blockList.size());
		//		System.out.println("word count : " + wordFreqMap.size());
		//		System.out.println("element count : " + elementCount);
		//		System.out.println("Density : " + String.format("%f",
		//				(double) elementCount / ((double) wordFreqMap.size() * (double) blockList.size())));

		// ワードの出現回数でフィルタリング（デフォルト 1以下は除去）
		Map<String, Integer> wordMap = new HashMap<>();
		int wordFreq[] = new int[wordFreqMap.size()];
		Iterator<String> iter = dictionary.iterator();
		for (int i = 0; iter.hasNext();) {
			String wordName = iter.next();
			if (wordFreqMap.get(wordName) > APPEARANCE_TH) {
				//一回以上でてきたことがあるワードはwordMapとwordFreqに登録
				wordMap.put(wordName, i);
				wordFreq[i] = wordFreqMap.get(wordName);
				i++;
			} else {
				//1回しか出現しなかったワードはdictionaryから削除
				iter.remove();
			}
		}

		//次元数はAllDataに保存しておく必要がある
		dimension = wordMap.size() + 15000;
		//dimension = wordMap.size();
		allData.setVecDimension(dimension);
		System.out.println("Dimension = " + allData.getVecDimension());
		//		System.out.println("filtered word count : " + wordMap.size());

		long start = System.currentTimeMillis();
		{
			final int size = blockList.size();
			for (int i = 0; i < size; i++) {
				blockList.set(i, calcBoW(blockList.get(i), wordMap, wordFreq, CloneDetector.countMethod, allData));
			}
		}

		//	Iterator<Block> i = blockList.iterator();
		//		int iD =0;
		//		while(i.hasNext()) {
		//			Block block  = i.next();
		//			if(block.getVector() == null) {
		//				System.out.println("remove block because of no vector");
		//				i.remove();
		//			}else {
		//				block.setId(iD);
		//				iD++;
		//			}
		//		}
		//		System.out.print("calc vector done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");
		//
		//		System.out.println("file out start");
		//		start = System.currentTimeMillis();

		if (Config.LSH_PRG == LSHController.E2LSH) {
			outputDenseDataset(blockList);
		} else {
			//			outputSparseDataset(CloneDetector.blockList);
			//			outputSparseDataset(blockList);
			outputSparseDatasetBinary(blockList);
		}
		outputDictionary(dictionary);

		allData.setWordMap(wordMap);
		//allData.setWordFreq(wordFreq);
		//		System.out.print("file out done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");

	}


	/**
	 * <p>
	 * 重みの計算
	 * </p>
	 *
	 * @throws FileNotFoundException
	 */
	public ArrayList<Block> calculateVector_test(ArrayList<Block> blockList, ArrayList<Block> addedModifiedBlockList, AllData allData) throws FileNotFoundException {
		// ワードマップの生成
		HashMap<String, Integer> wordFreqMap = new HashMap<String, Integer>();
		ArrayList<String> dictionary = new ArrayList<String>();

		Map<String, Integer> wordMap = new HashMap<>();
		wordMap = allData.getWordMap();
		int wordFreq[] = allData.getWordFreq();
		//System.out.println(" === wordFReq = " + wordFreq.length);


		// ワードの出現頻度の計測と、ワード辞書の生成
		int elementCount = 0;
		//System.out.println("test mae");
		/*		for (Block block : addedModifiedBlockList) {
			if (block.getParent() == null) {
				for (Word word : block.getWordList()) {
					System.out.println("test");
					if (wordFreqMap.containsKey(word.getName())) {
						int value = wordFreqMap.get(word.getName());
						wordFreqMap.put(word.getName(), ++value);
					} else {
						wordFreqMap.put(word.getName(), 1);
						System.out.println("add dictionaly");
						//ここTF-IDFを使わないなら消すことが出来る
						dictionary.add(word.getName());
					}
					elementCount++;
				}
			} else {
				elementCount += block.getWordList().size();
			}
		}*/
		/*
		 *
		 *
		 * wordMap
		 * wordFreq
		 * */


		//		System.out.println("addedModifiedBlockList size : " + addedModifiedBlockList.size());
		//		System.out.println("word count : " + wordFreqMap.size());
		//		System.out.println("element count : " + elementCount);
		//		System.out.println("Density : " + String.format("%f",
		//				(double) elementCount / ((double) wordFreqMap.size() * (double) blockList.size())));

		/*
		// ワードの出現回数でフィルタリング（デフォルト 1以下は除去）
		Map<String, Integer> wordMap = new HashMap<>();
	int wordFreq[] = new int[wordFreqMap.size()];
		Iterator<String> iter = dictionary.iterator();
		for (int i = 0; iter.hasNext();) {
			String wordName = iter.next();
			if (wordFreqMap.get(wordName) > APPEARANCE_TH) {
				wordMap.put(wordName, i);
				wordFreq[i] = wordFreqMap.get(wordName);
				i++;
			} else {
				//dictionaryから削除
				System.out.println("delete dictionaly");

				iter.remove();
			}
		}
		 */
		dimension = wordMap.size();
		//		System.out.println("filtered word count : " + wordMap.size());


		long start = System.currentTimeMillis();
		{
			final int size = addedModifiedBlockList.size();
			for (int i = 0; i < size; i++) {
				/*		System.out.println("addedModifiedBlockList count : " + i);
				System.out.println("addedModifiedBlockList category : " + addedModifiedBlockList.get(i).getCategory());
				System.out.println("addedModifiedBlockList id : " + addedModifiedBlockList.get(i).getId());
				System.out.println("addedModifiedBlockList start = " + addedModifiedBlockList.get(i).getStartLine() +  " end line  " + addedModifiedBlockList.get(i).getEndLine());
				System.out.println("addedModifiedBlockList filename = " + addedModifiedBlockList.get(i).getFileName());
				System.out.println("addedModifiedBlockList blocksize = " + addedModifiedBlockList.get(i).getWordList().size());
				 */	//System.out.println("addedModifiedBlockList old Blockfilename = " + addedModifiedBlockList.get(i).getOldBlock().getFileName());
				addedModifiedBlockList.set(i, calcBoW(addedModifiedBlockList.get(i), wordMap, wordFreq, CloneDetector.countMethod, allData));
			}
		}

		//
		//		Iterator<Block> i = blockList.iterator();
		//		int iD =0;
		//		while(i.hasNext()) {
		//			Block block  = i.next();
		//			if(block.getVector() == null) {
		//				System.out.println("remove block because of no vector");
		//				i.remove();
		//			}else {
		//				block.setId(iD);
		//				iD++;
		//			}
		//		}
		//
		//		Iterator<Block> j = addedModifiedBlockList.iterator();
		//		while(i.hasNext()) {
		//			Block block  = j.next();
		//			if(block.getVector() == null) {
		//				System.out.println("remove block because of no vector");
		//				j.remove();
		//			}
		//		}

		//		System.out.print("calc vector done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");
		//
		//		System.out.println("file out start");
		start = System.currentTimeMillis();

		if (Config.LSH_PRG == LSHController.E2LSH) {
			outputDenseDataset(blockList);
		} else {
			//			outputSparseDataset(CloneDetector.blockList);
			//			outputSparseDataset(blockList);
			outputSparseDatasetBinary(blockList);
			outputSparsePartialDatasetBinary(addedModifiedBlockList);

		}
		//s	outputDictionary(dictionary);

		allData.setWordMap(wordMap);
		//allData.setWordFreq(wordFreq);
		//		System.out.print("file out done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");
		return blockList;
	}

	public ArrayList<Block> increCalculateVector(ArrayList<Block> blockList, ArrayList<Block> addedModifiedBlockList, AllData allData) throws FileNotFoundException {
		// ワードマップの生成
		HashMap<String, Integer> wordFreqMap = new HashMap<String, Integer>();
		HashMap<String, Integer> wordFreqMapSource = new HashMap<String, Integer>();
		ArrayList<String> dictionary = new ArrayList<String>();
		ArrayList<String> targetAddWord = new ArrayList<String>();
		ArrayList<String> addedWord = new ArrayList<String>();
		ArrayList<Block> reCalcBlockList = new ArrayList<Block>();


		Map<String, Integer> wordMap = new HashMap<>();
		Map<String, Integer> wordMapSource = new HashMap<>();
		wordMap = allData.getWordMap();
		//int wordFreq[] = allData.getWordFreq();
		//System.out.println(" === wordFReq = " + wordFreq.length);

		int wordMapSize = wordMap.size();
		int allDataVecDimension = allData.getVecDimension();
		int numOfExtraWords = allDataVecDimension- wordMapSize;
		if(numOfExtraWords > 0) {


			// ワードの出現頻度の計測と、ワード辞書の生成
			// ワードの出現回数でフィルタリング（デフォルト 1以下は除去）
			int elementCount = 0;
			//System.out.println("test mae");
			ArrayList<Integer> setNum = new ArrayList<Integer>(blockList.size());
			int n = 0;
			for (Block block : blockList) {
				if (block.getParent() == null) {
					for (Word word : block.getWordList()) {
						//そのバージョンのソースコードに出てくるワードをカウント
						if (wordFreqMapSource.containsKey(word.getName())) {
							int valueAll = wordFreqMapSource.get(word.getName());
							wordFreqMapSource.put(word.getName(), ++valueAll);
						} else {
							wordFreqMapSource.put(word.getName(), 1);
						}
						//新規単語かつ，APPEARANCE_TH以上出現する単語を空いているベクトルの次元数に割り当てる
						if(!wordMap.containsKey(word.getName())) {
							if (wordFreqMap.containsKey(word.getName())) {
								int value = wordFreqMap.get(word.getName());
								wordFreqMap.put(word.getName(), ++value);
								if(value > APPEARANCE_TH) {
									targetAddWord.add(word.getName());
									if(block.getCategory() == Block.STABLE) {
										reCalcBlockList.add(block);
										setNum.add(n);
									}
								}
							} else {
								wordFreqMap.put(word.getName(), 1);
								//	System.out.println("add dictionaly");
								//ここTF-IDFを使わないなら消すことが出来る
								dictionary.add(word.getName());
							}
						}
						elementCount++;
					}
				} else {
					elementCount += block.getWordList().size();
				}
				n++;
			}


			// ワードの出現回数でフィルタリング（デフォルト 1以下は除去）

			int wordFreqSource[] = new int[wordFreqMapSource.size()];
			Iterator<String> iter = dictionary.iterator();
			for (int i = 0; iter.hasNext();) {
				String wordName = iter.next();
				if (wordFreqMapSource.get(wordName) > APPEARANCE_TH) {
					wordMapSource.put(wordName, i);
					wordFreqSource[i] = wordFreqMapSource.get(wordName);
					i++;
				} else {
					iter.remove();
				}
			}

			int reCalcNum = 0;
			if(targetAddWord.size() > 0) {
				int breakPoint = targetAddWord.size();
				int j = wordMap.size();
				for(int i=0; i < numOfExtraWords; i++) {
					wordMap.put(targetAddWord.get(i), j++);
					addedWord.add(targetAddWord.get(i));
					if(breakPoint-1 == i) {
						reCalcNum = i;
						break;
					}
				}
			}

			//		System.out.println("addedModifiedBlockList size : " + addedModifiedBlockList.size());
			//		System.out.println("word count : " + wordFreqMap.size());
			//		System.out.println("element count : " + elementCount);
			//		System.out.println("Density : " + String.format("%f",
			//				(double) elementCount / ((double) wordFreqMap.size() * (double) blockList.size())));

			// ワードの出現回数でフィルタリング（デフォルト 1以下は除去）
			if(reCalcBlockList.size() > 0) {
				int k =0;
				for(Block block : reCalcBlockList) {
					if(reCalcNum < k) break;
					if(block.getCategory() == Block.STABLE) {
						//						System.out.println("stable");
						//						blockList.set(setNum.get(k),increCalcBoW(block, wordMap, CloneDetector.countMethod, allData));
						increCalcBoW(block, wordMap, wordMapSource, CloneDetector.countMethod, allData);
						//						System.out.println("change vec of stable code");
					}
					k++;
				}
			}
		}


		//dimension = allData.getVecDimension();
		//		System.out.println("filtered word count : " + wordMap.size());

		//		System.out.println("Dimension = " + allData.getVecDimension());

		final int size = addedModifiedBlockList.size();
		for (int i = 0; i < size; i++) {
			//				System.out.println("addedModifiedBlockList count : " + i);
			//				System.out.println("addedModifiedBlockList category : " + addedModifiedBlockList.get(i).getCategoryString());
			//				System.out.println("addedModifiedBlockList id : " + addedModifiedBlockList.get(i).getId());
			//				System.out.println("addedModifiedBlockList start = " + addedModifiedBlockList.get(i).getStartLine() +  " end line  " + addedModifiedBlockList.get(i).getEndLine());
			//				System.out.println("addedModifiedBlockList filename = " + addedModifiedBlockList.get(i).getFileName());
			//				System.out.println("addedModifiedBlockList blocksize = " + addedModifiedBlockList.get(i).getWordList().size());

			//System.out.println("addedModifiedBlockList old Blockfilename = " + addedModifiedBlockList.get(i).getOldBlock().getFileName());
			addedModifiedBlockList.set(i, increCalcBoW(addedModifiedBlockList.get(i), wordMap, wordMapSource, CloneDetector.countMethod, allData));
		}



		if (Config.LSH_PRG == LSHController.E2LSH) {
			outputDenseDataset(blockList);
		} else {
			//			outputSparseDataset(CloneDetector.blockList);
			//			outputSparseDataset(blockList);
			outputSparseDatasetBinary(blockList);
			outputSparsePartialDatasetBinary(addedModifiedBlockList);

		}

		allData.setWordMap(wordMap);
		//allData.setWordFreq(wordFreq);
		//		System.out.print("file out done : ");
		//		System.out.println(System.currentTimeMillis() - start + "[ms]");
		return blockList;
	}


	//private static void outputSparseDataset(ArrayList<Block> blockList) {
	private static void outputSparseDataset(ArrayList<Block> blockList) {
		try {
			Block.serializeBlockList(blockList);
			List<Block> blockList2 =Block.deserializeBlockList("blockList.bin");

			PrintWriter writer = new PrintWriter(
					new BufferedWriter(new FileWriter(CloneDetector.DATASET_FILE + ".txt")));
			for (Block block : blockList2) {
				StringBuilder buf = new StringBuilder();
				OpenMapRealVector vector = block.getVector();
				for (int index : RealVectorUtil.getSparseIndexList(vector)) {
					buf.append(Integer.toString(index));
					buf.append(':');
					buf.append(dtos(vector.getEntry(index), 6));
					buf.append(' ');
				}
				writer.println(buf.toString());
			}
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
		}

	}

	private static void outputSparseDatasetBinary(ArrayList<Block> blockList) {
		byte[] writeBuffer = new byte[8];

		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(CloneDetector.DATASET_FILE))) {
			int i = 0;
			//		Block.serializeBlockList(blockList);
			//		List<Block> blockList2 =Block.deserializeBlockList("blockList.bin");


			for (Block block : blockList) {

				if(block.getVector() == null) {
					System.out.println("vector null = " + i);
					System.out.println("file Name  = " + block.getFileName());
					System.out.println("start line = " + block.getStartLine());
					System.out.println("end   line = " + block.getEndLine());
					System.out.println("category  = " + block.getCategoryString());
					i++;

				}else {

					i++;

					OpenMapRealVector vector = block.getVector();
					//	System.out.println(i + " = " + vector);

					int size = (int) Math.round(vector.getDimension() * vector.getSparsity());
					out.write((size >>> 0) & 0xFF);
					out.write((size >>> 8) & 0xFF);
					out.write((size >>> 16) & 0xFF);
					out.write((size >>> 24) & 0xFF);

					for (int index : RealVectorUtil.getSparseIndexList(vector)) {
						//System.out.println("index" + " = " + index);
						out.write((index >>> 0) & 0xFF);
						out.write((index >>> 8) & 0xFF);
						out.write((index >>> 16) & 0xFF);
						out.write((index >>> 24) & 0xFF);

						long entry = Double.doubleToLongBits(vector.getEntry(index));
						//System.out.println("entry" + " = " + entry);
						writeBuffer[0] = (byte) (entry >>> 0);
						writeBuffer[1] = (byte) (entry >>> 8);
						writeBuffer[2] = (byte) (entry >>> 16);
						writeBuffer[3] = (byte) (entry >>> 24);
						writeBuffer[4] = (byte) (entry >>> 32);
						writeBuffer[5] = (byte) (entry >>> 40);
						writeBuffer[6] = (byte) (entry >>> 48);
						writeBuffer[7] = (byte) (entry >>> 56);
						out.write(writeBuffer, 0, 8);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	private static void outputSparsePartialDatasetBinary(ArrayList<Block> updatedBlockList) {
		byte[] writeBuffer = new byte[8];

		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(CloneDetector.PARTIAL_QUERY_POINT))) {

			for (Block block : updatedBlockList) {
				OpenMapRealVector vector = block.getVector();
				int size = (int) Math.round(vector.getDimension() * vector.getSparsity());
				out.write((size >>> 0) & 0xFF);
				out.write((size >>> 8) & 0xFF);
				out.write((size >>> 16) & 0xFF);
				out.write((size >>> 24) & 0xFF);

				for (int index : RealVectorUtil.getSparseIndexList(vector)) {
					//System.out.println("index" + " = " + index);
					out.write((index >>> 0) & 0xFF);
					out.write((index >>> 8) & 0xFF);
					out.write((index >>> 16) & 0xFF);
					out.write((index >>> 24) & 0xFF);

					long entry = Double.doubleToLongBits(vector.getEntry(index));
					//System.out.println("entry" + " = " + entry);
					writeBuffer[0] = (byte) (entry >>> 0);
					writeBuffer[1] = (byte) (entry >>> 8);
					writeBuffer[2] = (byte) (entry >>> 16);
					writeBuffer[3] = (byte) (entry >>> 24);
					writeBuffer[4] = (byte) (entry >>> 32);
					writeBuffer[5] = (byte) (entry >>> 40);
					writeBuffer[6] = (byte) (entry >>> 48);
					writeBuffer[7] = (byte) (entry >>> 56);
					out.write(writeBuffer, 0, 8);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}



	private static void outputDenseDataset(List<Block> blockList) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(CloneDetector.DATASET_FILE)));
			for (Block block : blockList) {
				StringBuilder buf = new StringBuilder();
				OpenMapRealVector vector = block.getVector();
				for (double value : RealVectorUtil.getDenseValueList(vector)) {
					buf.append(dtos(value, 6));
					buf.append(' ');
				}
				writer.println(buf.toString());
			}
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private static void outputDictionary(ArrayList<String> dictionary) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("dictionary.txt")));
			for (String string : dictionary) {
				writer.println(string);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String dtos(double x, int n) {
		StringBuilder sb = new StringBuilder();
		if (x < 0) {
			sb.append('-');
			x = -x;
		}
		x += Math.pow(10, -n) / 2;
		// if(x < 0){ x = 0; }
		sb.append((long) x);
		sb.append(".");
		x -= (long) x;
		for (int i = 0; i < n; i++) {
			x *= 10;
			sb.append((int) x);
			x -= (int) x;
		}
		return sb.toString();
	}

	private static Block calcTfIdf(Block block, Map<String, Integer> wordMap, int[] wordFreq, int numMethod) {
		int wordCount = 0;
		int statementCount = 0;
		for (Word word : block.getWordList()) {
			if (wordMap.containsKey(word.getName())) {
				switch (word.getType()) {
				case Word.WORD:
					wordCount += word.getCount();
					break;
				case Word.STATEMENT:
					statementCount += word.getCount();
					break;
				}
			}
		}

		// 重みの計算
		List<Integer> indexList = new ArrayList<Integer>();
		List<Double> valueList = new ArrayList<Double>();
		double len = 0;
		for (Word word : block.getWordList()) {
			if (wordMap.containsKey(word.getName())) {
				int id = wordMap.get(word.getName());
				double tf = 0;
				switch (word.getType()) {
				case Word.WORD:
					tf = (double) word.getCount() / (double) wordCount;
					break;
				case Word.STATEMENT:
					tf = (double) word.getCount() / (double) statementCount;
					break;
				}
				//double idf = 1.0 + Math.log((double) numMethod / (double) wordFreq[id]);
				//		double tfidf = tf * idf;
				//BoW
				double tfidf = tf;
				indexList.add(id);
				valueList.add(tfidf);
				len += tfidf * tfidf;
			}
		}
		len = Math.sqrt(len);
		OpenMapRealVector vector = new OpenMapRealVector(wordMap.size());
		final int size = indexList.size();
		for (int i = 0; i < size; i++) {
			//		System.out.println("index " +  indexList.get(i) +  "; valueList.get(i) / len) = " + valueList.get(i) / len);
			vector.setEntry(indexList.get(i), valueList.get(i) / len);
		}

		block.setVector(vector);
		block.setLen(len);
		//block.clearWordList();
		// block.setStringVector(stringVector);
		return block;
	}

	private static Block calcBoW(Block block, Map<String, Integer> wordMap, int[] wordFreq, int numMethod, AllData allData) {
		int wordCount = 0;
		int statementCount = 0;

		//		System.out.println("wordListsize : " + block.getWordList().size());
		for (Word word : block.getWordList()) {
			if (wordMap.containsKey(word.getName())) {
				switch (word.getType()) {
				case Word.WORD:
					wordCount += word.getCount();
					break;
				case Word.STATEMENT:
					statementCount += word.getCount();
					break;
				}
			}
		}

		// 重みの計算
		List<Integer> indexList = new ArrayList<Integer>();
		List<Double> valueList = new ArrayList<Double>();
		double len = 0;
		for (Word word : block.getWordList()) {
			if (wordMap.containsKey(word.getName())) {
				int id = wordMap.get(word.getName());
				double tf = 0;
				switch (word.getType()) {
				case Word.WORD:
					tf = (double) word.getCount() / (double) wordCount;
					break;
				case Word.STATEMENT:
					tf = (double) word.getCount() / (double) statementCount;
					break;
				}
				//double idf = 1.0 + Math.log((double) numMethod / (double) wordFreq[id]);
				//		double tfidf = tf * idf;
				//BoW
				double bow = tf;
				indexList.add(id);
				valueList.add(bow);
				len += bow * bow;
			}
		}
		len = Math.sqrt(len);
		//	System.out.println("len = " + len);
		OpenMapRealVector vector = new OpenMapRealVector(allData.getVecDimension());
		final int size = indexList.size();
		for (int i = 0; i < size; i++) {
			//			System.out.println("index " +  indexList.get(i) +  "; valueList.get(i) / len) = " + valueList.get(i) / len);
			vector.setEntry(indexList.get(i), valueList.get(i) / len);
		}
		block.setVector(vector);
		block.setLen(len);
		//block.clearWordList();
		// block.setStringVector(stringVector);
		return block;
	}


	private static Block increCalcBoW(Block block, Map<String, Integer> wordMap, Map<String, Integer> wordMapSource, int numMethod, AllData allData) {
		int wordCount = 0;
		int statementCount = 0;

		//		System.out.println("wordListsize : " + block.getWordList().size());
		for (Word word : block.getWordList()) {
			if (wordMap.containsKey(word.getName())) {
				switch (word.getType()) {
				case Word.WORD:
					wordCount += word.getCount();
					break;
				case Word.STATEMENT:
					statementCount += word.getCount();
					break;
				}
			}
		}

		// 重みの計算
		List<Integer> indexList = new ArrayList<Integer>();
		List<Double> valueList = new ArrayList<Double>();
		double len = 0;
		for (Word word : block.getWordList()) {
			//wordMapにもあるし，今回のバージョンのソースコードにも単語がある
			if (wordMap.containsKey(word.getName()) && wordMapSource.containsKey(word.getName())) {
				int id = wordMap.get(word.getName());
				double tf = 0;
				switch (word.getType()) {
				case Word.WORD:
					tf = (double) word.getCount() / (double) wordCount;
					break;
				case Word.STATEMENT:
					tf = (double) word.getCount() / (double) statementCount;
					break;
				}
				//double idf = 1.0 + Math.log((double) numMethod / (double) wordFreq[id]);
				//		double tfidf = tf * idf;
				//BoW
				double bow = tf;
				indexList.add(id);
				valueList.add(bow);
				len += bow * bow;
			}
		}
		len = Math.sqrt(len);
		//	System.out.println("len = " + len);
		OpenMapRealVector vector = new OpenMapRealVector(allData.getVecDimension());
		final int size = indexList.size();
		for (int i = 0; i < size; i++) {
			//	System.out.println("index " +  indexList.get(i) +  "; valueList.get(i) / len) = " + valueList.get(i) / len);
			vector.setEntry(indexList.get(i), valueList.get(i) / len);
		}
		block.setVector(vector);
		block.setLen(len);
		//block.clearWordList();
		// block.setStringVector(stringVector);
		return block;
	}
}
