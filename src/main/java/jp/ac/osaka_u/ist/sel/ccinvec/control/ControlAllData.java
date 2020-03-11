package jp.ac.osaka_u.ist.sel.ccinvec.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import jp.ac.osaka_u.ist.sel.ccinvec.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccinvec.Config;
import jp.ac.osaka_u.ist.sel.ccinvec.VectorCalculator;
import jp.ac.osaka_u.ist.sel.ccinvec.model.AllData;
import jp.ac.osaka_u.ist.sel.ccinvec.model.Block;
import jp.ac.osaka_u.ist.sel.ccinvec.model.ClonePair;
import jp.ac.osaka_u.ist.sel.ccinvec.model.SourceFile;

public class ControlAllData {

	/**
	 * <p>
	 * allDataをシリアライズ化
	 * <p>
	 * @param allData
	 */
	public static void serializeAllDataList(AllData allData, Config config) {

		File folder = new File(config.getDataDir());

		// フォルダの存在を確認する
		if (!folder.exists()) {
			if(folder.mkdirs()) {
			}
		}

		try {
			ObjectOutputStream objOutStream =
					new ObjectOutputStream(
							new FileOutputStream(config.getDataDir() + "\\allData.bin"));
			objOutStream.writeObject(allData);
			objOutStream.close();
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

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * <p>
	 * allDataをデシリアライズ化
	 * <p>
	 * @param allData
	 */
	public static AllData deserializeAllDataList(Config config) {
		try {
			String DataDir = config.getDataDir();
			ObjectInputStream objInStream
			= new ObjectInputStream(
					new FileInputStream(DataDir + "\\allData.bin"));
			AllData allData = (AllData) objInStream.readObject();
			objInStream.close();

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

			return allData;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * <p>
	 * allDataのSourceFileListとClonePairListの同期をとる
	 * <p>
	 * @throws IOException
	 */
	public static void synchronizeAllData(Config config, AllData allData) throws IOException {

		ArrayList<Block> blockList = new ArrayList<>();
		blockList.addAll(createBlockList(config.getVecMethod(), allData));
		//blockList.addAll(createBlockList());

		//        FileWriter fw = new FileWriter("test.txt");
		//
		if(CloneDetector.modeDebug) {
			System.out.println(" ===  block list size  " + blockList.size());
			System.out.println(" ===  calced block list size  " + allData.BlockListOfCalcedVec.size());
		}

		Iterator<ClonePair> i = allData.ClonePairList.iterator();
		int j = 0;
		int deleteCP = 0;
		while(i.hasNext()) {
			ClonePair cp  = i.next();
			int idA = cp.cloneA.getId();
			int idB = cp.cloneB.getId();
			int bLSize = blockList.size();
			if(bLSize >=  idA && bLSize >= idB) {
				if((Block.equalsCodeInfo(cp.cloneA, blockList.get(idA)) &&
						(!Block.equalsCodeInfo(cp.cloneB, blockList.get(idB))))) {
					cp.cloneA = null;
					cp.cloneB = null;

					cp.setCloneA(blockList.get(idA));
					cp.setCloneB(blockList.get(idB));
				}else {
					boolean cpAset = false;
					boolean cpBset = false;
					for(Block block : blockList) {
						if(Block.equalsCodeInfo(block, cp.cloneA)) {
							cp.cloneA = null;
							cp.setCloneA(block);
							cpAset = true;
						}
						if(Block.equalsCodeInfo(block, cp.cloneB)) {
							cp.cloneB = null;
							cp.setCloneB(block);
							cpBset = true;
						}
					}
					if(!cpAset || !cpBset) {
						//				System.out.println("remove clone pair");
						i.remove();
					}

				}
			}else {
				i.remove();
			}
		}

		//		for(ClonePair cp : ClonePairList) {
		//			int idA = cp.cloneA.getId();
		//			int idB = cp.cloneB.getId();
		//
		//
		//			if(cp.cloneA.getStartLine() != blockList.get(idA).getStartLine() || cp.cloneA.getEndLine() !=  blockList.get(idA).getEndLine()) {
		//				//	if(CloneDetector.modeDebug) {
		//				System.out.println("no match startline cloneA  startline" + cp.cloneA.getStartLine());
		//				System.out.println("no match end  line cloneA  end  line" + cp.cloneA.getEndLine());
		//				System.out.println("blockList                  startline" + blockList.get(idA).getStartLine());
		//				System.out.println("blockList                  end  line" + blockList.get(idA).getEndLine());
		//				System.out.println("cloneA  file" + cp.cloneA.getFileName());
		//				System.out.println("blockLT file" + blockList.get(idA).getFileName());
		//				System.out.println("cloneA  cate" + cp.cloneA.getCategory());
		//				System.out.println("blockLT cate" + blockList.get(idA).getCategory());
		//				System.out.println("no match startline cloneB  startline" + cp.cloneB.getStartLine());
		//				System.out.println("no match end  line cloneB  end  line" + cp.cloneB.getEndLine());
		//				System.out.println("cloneB  file" + cp.cloneB.getFileName());
		//				System.out.println();
		//				System.out.println();
		//
		//				//	}
		//			}
		//
		//			if(cp.cloneB.getStartLine() != blockList.get(idB).getStartLine() || cp.cloneB.getEndLine() !=  blockList.get(idB).getEndLine()) {
		//				//if(CloneDetector.modeDebug) {
		//				System.out.println("no match startline cloneB  startline" + cp.cloneB.getStartLine());
		//				System.out.println("no match end  line cloneA  end  line" + cp.cloneB.getEndLine());
		//				System.out.println("blockList                  startline" + blockList.get(idB).getStartLine());
		//				System.out.println("blockList                  end  line" + blockList.get(idB).getEndLine());
		//				System.out.println("cloneB  file" + cp.cloneB.getFileName());
		//				System.out.println("blockLT file" + blockList.get(idB).getFileName());
		//				System.out.println("cloneB  cate" + cp.cloneB.getCategory());
		//				System.out.println("blockLT cate" + blockList.get(idB).getCategory());
		//				System.out.println("no match startline cloneA  startline" + cp.cloneA.getStartLine());
		//				System.out.println("no match end  line cloneA  end  line" + cp.cloneA.getEndLine());
		//				System.out.println("cloneA  file" + cp.cloneA.getFileName());
		//				System.out.println();
		//				System.out.println();
		//				//}
		//			}
		//
		//			if((cp.cloneA.getStartLine() != blockList.get(idA).getStartLine() || cp.cloneA.getEndLine() !=  blockList.get(idA).getEndLine()) ||
		//					(cp.cloneB.getStartLine() != blockList.get(idB).getStartLine() || cp.cloneB.getEndLine() !=  blockList.get(idB).getEndLine())) {
		//				ClonePairList.remove(cp);
		//				System.out.println("remove clone pair");
		//			}
		//
		//
		//
		//			//			fw.write("============= = \r\n");
		//			//			fw.write("clone A = " + cp.cloneA.getId() + "\r\n");
		//			//			fw.write("clone B = " + cp.cloneB.getId());
		//			//			fw.write("clone A fileName = " + cp.cloneA.getFileName() + "\r\n");
		//			//			fw.write("clone B fileName = " + cp.cloneB.getFileName() + "\r\n");
		//			//			fw.write("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine() + "\r\n");
		//			//			fw.write("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine() + "\r\n");
		//			//			fw.write("============= =  \\r\\n");
		//
		//			cp.cloneA = null;
		//			cp.cloneB = null;
		//
		//			cp.setCloneA(blockList.get(idA));
		//			cp.setCloneB(blockList.get(idB));
		//
		//		}

		//		fw.close();


	}

	/**
	 * <p>
	 * allDataのSourceFileListとClonePairListの同期をとる
	 * <p>
	 */
	public static ArrayList<Block> createBlockList(int vecMethod, AllData allData) {
		ArrayList<Block> blockList = new ArrayList<>();
		for (SourceFile file : allData.SourceFileList) {
			for(Block block :file.getNewBlockList()) {
				if(allData.pre_Diff) {
					if(block.getFilterCategory() == Block.PASSFILTER) {
						block.setFilterCategory(Block.NO_FILTER);
						//						System.out.println("PASS FILTER");
						blockList.add(block);
					}
					else {
						//						System.out.println("not added  " + block.getFileName());
						//						System.out.println("start line " + block.getStartLine());
						//						System.out.println("end   line " + block.getEndLine());
					}
				}else {
					if(block.getPreFilterCategory() == Block.PASSFILTER) {
						//						System.out.println("PASS FILTER");
						blockList.add(block);
					}
					else {
						//						System.out.println("not added  " + block.getFileName());
						//						System.out.println("start line " + block.getStartLine());
					}

				}

			}
			//blockList.addAll(file.getNewBlockList());
		}
		int i =0;
		for(Block block : blockList) {
			//			if(BlockListOfCalcedVec.get(i).getWordList() == null) {
			//				System.out.println("vec null " + i );
			//			}
			Block calcedVecBlock = allData.BlockListOfCalcedVec.get(i);
			if(Block.equalsCodeInfo(block, calcedVecBlock)) {
				block.setWordList(calcedVecBlock.getWordList());
				block.setVector(calcedVecBlock.getVector());
				block.setLen(calcedVecBlock.getLen());
				block.setCategory(Block.NULL);
			}else {
				boolean blockExist = false;
				for(Block calcedBlock : allData.BlockListOfCalcedVec) {
					if(Block.equalsCodeInfo(block, calcedBlock)) {
						block.setWordList(calcedBlock.getWordList());
						block.setVector(calcedBlock.getVector());
						block.setLen(calcedBlock.getLen());
						block.setCategory(Block.NULL);
						blockExist = true;
						break;
					}
				}
				if(!blockExist) {
					if(vecMethod == Config.BoW) {
						block.setCategory(Block.NULL);
						VectorCalculator.increCalcBoW(block, allData.wordMap, allData.wordMapSource, CloneDetector.countMethod, allData.dimension);
					}else if(vecMethod == Config.TFIDF) {
						VectorCalculator.increCalcTfIdf(block, allData.wordMap, allData.wordMapSource,allData.wordFreq ,allData.countMethod, allData.dimension);
					}
				}
			}
			//	System.out.println("ID " + block.getId());
			//block.setId(i);
			i++;
		}
		return blockList;
	}


	public static void dataClear(AllData allData) {
		allData.SourceFileList = null;
		allData.ClonePairList = null;
		allData.BlockListOfCalcedVec = null;
		allData.wordMap = null;
		allData.wordFreq = null;
	}


}
