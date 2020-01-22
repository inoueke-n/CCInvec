package jp.ac.osaka_u.ist.sel.icvolti;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.ac.osaka_u.ist.sel.icvolti.model.AllData;
import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
import jp.ac.osaka_u.ist.sel.icvolti.model.ClonePair;
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;

public class BlockUpdater {


	/**
	 * <p>
	 * ブロックリストをシリアライズ化
	 * <p>
	 * @param blocklist
	 */
	public void serializeBlockList(ArrayList<Block> blockList) {
		try {
			ObjectOutputStream objOutStream =
					new ObjectOutputStream(
							new FileOutputStream("blockList.bin"));
			objOutStream.writeObject(blockList);
			objOutStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * <p>
	 * ブロックリストをデシリアライズ化
	 * <p>
	 * @param blocklist
	 */
	public static ArrayList<Block> deserializeBlockList(String blockListName) {
		try {
			ObjectInputStream objInStream
			= new ObjectInputStream(
					new FileInputStream(blockListName));

			ArrayList<Block> blockList = (ArrayList<Block>) objInStream.readObject();

			objInStream.close();

			return blockList;

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
	 * ソースファイルリストをシリアライズ化
	 * <p>
	 * @param sourceFileList
	 */
	public static void serializeSourceFileList(ArrayList<SourceFile> sourceFileList) {
		try {
			ObjectOutputStream objOutStream =
					new ObjectOutputStream(
							new FileOutputStream("sourceFileList.bin"));
			objOutStream.writeObject(sourceFileList);
			objOutStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * <p>
	 * ソースファイルリストをデシリアライズ化
	 * <p>
	 * @param blocklist
	 */
	public static ArrayList<SourceFile> deserializeSourceFileList(String sourceFileListName) {
		try {
			ObjectInputStream objInStream
			= new ObjectInputStream(
					new FileInputStream(sourceFileListName));

			ArrayList<SourceFile> sourceFileList = (ArrayList<SourceFile>) objInStream.readObject();

			objInStream.close();

			return sourceFileList;

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
	 * ソースファイルリストを更新
	 * <p>
	 */
	/*
	public static void changePathNewToOld(String newTarget, String oldTarget, ArrayList<SourceFile> FileList, ArrayList<String> newFileListOrigin){
		ArrayList<String> newFileList = (ArrayList<String>) newFileListOrigin.clone();

		Iterator<String> it = newFileList.iterator();
		int fileid = 0;
		while(it.hasNext()) {
			String fileName = it.next();
			int index = newFileNameList.indexOf(filename)
		}
	}
	 */


	public static ArrayList<SourceFile> updateSourceFileList(String newTarget, String oldTarget, ArrayList<SourceFile> oldFileList, ArrayList<String> newFileListOrigin, ArrayList<Block> updatedBlockList){

		//incrementalAnalyzaと一緒にできるのでは？
		ArrayList<SourceFile> fileList = new ArrayList<SourceFile>();
		CloneDetector.deletedSourceFile=false;

		ArrayList<String> newFileList = (ArrayList<String>) newFileListOrigin.clone();
		int fileId = 0;
		for(SourceFile file : oldFileList ) {
			String newFilePath = file.getNewPath();
			//System.out.println("oldFile = " + newFilePath);
			file.setOldPath(newFilePath);
			String newTargetFilePath = newTarget + file.getOldPath().substring(oldTarget.length());

			int index = newFileList.indexOf(newTargetFilePath);
			if(index > -1) {
				file.setNewPath(newTargetFilePath);
				//System.out.println("newFile = " + newFilePath);
				newFileList.remove(newTargetFilePath);
				file.setState(SourceFile.STABLE);
				for(Block block : file.getNewBlockList()) {
					//					block.setFileterCategory(Block.NO_FILTER);
					if(block.getPreFilterCategory() == Block.PASSFILTER && block.getVector()==null) {
						System.out.println("NNNNUUUUULLLL");
						System.out.println("ID        "+ block.getId() );
						System.out.println("fileName  "+ block.getFileName());
						System.out.println("startLine "+ block.getStartLine());
						System.out.println("endLine   "+ block.getEndLine());
					}
					block.setCategory(Block.NULL);
					block.setOldFileName(block.getFileName());
					block.setFileName(newTargetFilePath);
					block.setNewBlock(null);
					block.setOldBlock(null);
					//とりあえず，ノーマルなファイルに含まれるブロックはSTABLEに分類 ＜－これが原因でAddedにうまく分類されていない？
					//block.setCategory(Block.STABLE);
				}
				file.setCopyRightModified(false);
				file.getAddedCodeList().clear();
				file.getDeletedCodeList().clear();
				file.setId(fileId++);
				fileList.add(file);
			}else {
				//新バージョンのファイルリストに含まれないファイルは削除されたもの
				file.setState(SourceFile.DELETED);
				// ここで削除されたファイルのコードブロックに関わる情報は削除？
				for(Block block : file.getNewBlockList()){
					block.setCategory(Block.DELETED);
				}
				file.setCopyRightModified(false);
				updatedBlockList.addAll(file.getNewBlockList());
				CloneDetector.deletedSourceFile=true;

			}
		}

		Iterator<String> it = newFileList.iterator();
		while(it.hasNext()) {
			String fileName = it.next();
			SourceFile file = new SourceFile();
			file.setNewPath(fileName);
			file.setOldPath(null);
			file.setState(SourceFile.ADDED);
			file.setId(fileId++);
			fileList.add(file);
		}


		ArrayList<SourceFile> sortedFileList = new ArrayList<SourceFile>();

		//並べんでもなんとかなる

		for(String fileName : newFileListOrigin) {
			int i=0;
			for(SourceFile file : fileList){
				if(file.getNewPath().equals(fileName) ) {
					sortedFileList.add(file);
					fileList.remove(i);
					break;
				}
				i++;
			}

		}
		//		System.out.println("sortedFileList size = " + sortedFileList.size());

		/**
		 * 新しいソースファイル順にnewソースファイルを作っていく
		 * もしなかったらnewとして作っていく
		 */

		return sortedFileList;
		//return fileList;

	}


	public static ArrayList<SourceFile> updateSourceFileList2(String newTarget, String oldTarget, ArrayList<SourceFile> oldFileList, ArrayList<String> newFileListOrigin, ArrayList<Block> updatedBlockList){
		ArrayList<SourceFile> fileList = new ArrayList<SourceFile>();

		int fileId = 0;
		String newFilePath;
		String oldFilePath;
		String newTargetFilePath;
		for(String newFile : newFileListOrigin) {
			int i =0;
			for(SourceFile file : oldFileList){
				newFilePath = file.getNewPath();
				oldFilePath = file.getOldPath();
				file.setOldPath(newFilePath);
				newTargetFilePath = newTarget + file.getOldPath().substring(oldTarget.length());
				if(newFile == newTargetFilePath) {
					file.setNewPath(newTargetFilePath);
					file.setState(SourceFile.STABLE);
					for(Block block : file.getNewBlockList()) {
						block.setOldFileName(block.getFileName());
						block.setFileName(newTargetFilePath);
					}
					file.getAddedCodeList().clear();
					file.getDeletedCodeList().clear();
					file.setId(fileId++);
					fileList.add(file);
					oldFileList.remove(i);
				}else {
					//	新規追加ファイル
					SourceFile addedFile = new SourceFile();
					file.setNewPath(newFile);
					file.setOldPath(null);
					file.setState(SourceFile.ADDED);
					file.setId(fileId++);
					fileList.add(addedFile);
				}
			}
		}


		//ここでoldFileListに残っているものは削除されたものと判定される
		for(SourceFile oldFile : oldFileList) {
			oldFile.setState(SourceFile.DELETED);

			// ここで削除されたファイルのコードブロックに関わる情報は削除？
			for(Block block : oldFile.getNewBlockList()){
				block.setCategory(Block.DELETED);
			}
			updatedBlockList.addAll(oldFile.getNewBlockList());

		}

		return fileList;

	}



	/**
	 * <p>
	 * ソースファイルリストをシリアライズ化
	 * <p>
	 * @param clonePairList
	 */
	public static void serializeClonePairList(List<ClonePair> clonePairList) {
		try {
			ObjectOutputStream objOutStream =
					new ObjectOutputStream(
							new FileOutputStream("clonePairList.bin"));
			objOutStream.writeObject(clonePairList);
			objOutStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	/**
	 * <p>
	 * ソースファイルリストをデシリアライズ化
	 * <p>
	 * @param blocklist
	 */
	public static List<ClonePair> deserializeClonePairList(String clonePairListName) {
		try {
			ObjectInputStream objInStream
			= new ObjectInputStream(
					new FileInputStream(clonePairListName));

			List<ClonePair> clonePairList = (List<ClonePair>) objInStream.readObject();

			objInStream.close();

			return clonePairList;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	public static void resetClonePair(List<ClonePair> ClonePairList, List<Block> addedModifiedBlockList, List<Block> deletedBlockList) {

		//ClonePairList.removeIf(ClonePair -> ClonePair.cloneA);

		//iteratorでぶん回す？
		Iterator<ClonePair> i = ClonePairList.iterator();
		while(i.hasNext()) {
			ClonePair cp  = i.next();
			int aU = addedModifiedBlockList.indexOf(cp.cloneA);
			int aD = deletedBlockList.indexOf(cp.cloneA);
			int bU = addedModifiedBlockList.indexOf(cp.cloneB);
			int bD = deletedBlockList.indexOf(cp.cloneB);
			if(aU > -1 || aD > -1 || bU > -1 || bD > -1) {
				System.out.println("DELETE CLONEPAIR ");
				i.remove();
			}

		}

	}*/


	public static void resetClonePair(ArrayList<ClonePair> ClonePairList,  List<Block> updatedBlockList) {

		//ClonePairList.removeIf(ClonePair -> ClonePair.cloneA);

		//iteratorでぶん回す？
		Iterator<ClonePair> i = ClonePairList.iterator();
		int j = 0;
		int deleteCP = 0;
		while(i.hasNext()) {
			ClonePair cp  = i.next();
			/*if(cp.clone) {

			}*/
			//	System.out.println("clone pair j  = " + j++ + "cp.cloneA = " + cp.cloneA.getFileName());
			/*			System.out.println("============= = ");
			System.out.println("clone A = " + cp.cloneA.getId());
			System.out.println("clone B = " + cp.cloneB.getId());
			System.out.println("============= = ");
			 */

			if(cp.cloneA.getCategory() == Block.NULL || cp.cloneB.getCategory() == Block.NULL) {
//												System.out.println("DELETE CLONEPAIR because of Block.NULL ");
//												System.out.println("============= = ");
//												System.out.println("clone A = " + cp.cloneA.getId());
//												System.out.println("catec A = " + cp.cloneA.getCategoryString());
//												System.out.println("clone B = " + cp.cloneB.getId());
//												System.out.println("catec B = " + cp.cloneB.getCategoryString());
//												System.out.println("clone A fileName = " + cp.cloneA.getFileName());
//												System.out.println("clone B fileName = " + cp.cloneB.getFileName());
//												System.out.println("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine());
//												System.out.println("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine());
//												System.out.println("============= = ");

				i.remove();
				deleteCP++;

			}else {
				for(Block block : updatedBlockList) {
					//System.out.println("aa");
					int category  = block.getCategory();
					if (category == Block.MODIFIED ||category == Block.DELETED) {
						//System.out.println("=========  category  =========  " + category);
						//if(block.getOldBlock() != null) {

						//	System.out.println(" old block " + block.getOldBlock().getFileName());
						//	System.out.println(" cpA block " + cp.cloneA.getFileName());
						//	System.out.println(" cpB block " + cp.cloneB.getFileName());

						if(block.getOldBlock() != null) {
							if(Block.equalsCodeInfo(block.getOldBlock(), cp.cloneA)) {
//							if(block.getOldBlock().getFileName() == cp.cloneA.getFileName() &&
//									block.getOldBlock().getStartLine() == cp.cloneA.getStartLine() &&
//									block.getOldBlock().getEndLine() == cp.cloneA.getEndLine()) {

								//							System.out.println("DELETE CLONEPAIR because update A ");
								//							System.out.println("============= = ");
								//							System.out.println("block  = " + block.getOldBlock().getId());
								//							System.out.println("block  = " + block.getOldBlock().getFileName());
								//							System.out.println("block startLine =" + block.getOldBlock().getStartLine() + "endline = " + block.getOldBlock().getEndLine());
								//							System.out.println("clone A = " + cp.cloneA.getId());
								//							System.out.println("catec A = " + cp.cloneA.getCategoryString());
								//							System.out.println("clone B = " + cp.cloneB.getId());
								//							System.out.println("catec B = " + cp.cloneB.getCategoryString());
								//							System.out.println("clone A fileName = " + cp.cloneA.getFileName());
								//							System.out.println("clone B fileName = " + cp.cloneB.getFileName());
								//							System.out.println("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine());
								//							System.out.println("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine());
								//							System.out.println("============= = ");
								i.remove();
								deleteCP++;
								break;
							}else if(Block.equalsCodeInfo(block.getOldBlock(), cp.cloneB)) {

//							}else if(block.getOldBlock().getFileName() == cp.cloneB.getFileName() &&
//									block.getOldBlock().getStartLine() == cp.cloneB.getStartLine() &&
//									block.getOldBlock().getEndLine() == cp.cloneB.getEndLine()) {

								//							System.out.println("DELETE CLONEPAIR because update B ");
								//							System.out.println("============= = ");
								//							System.out.println("block  = " + block.getOldBlock().getId());
								//							System.out.println("block  = " + block.getOldBlock().getFileName());
								//							System.out.println("block startLine =" + block.getOldBlock().getStartLine() + "endline = " + block.getOldBlock().getEndLine());
								//							System.out.println("clone A = " + cp.cloneA.getId());
								//							System.out.println("catec A = " + cp.cloneA.getCategoryString());
								//							System.out.println("clone B = " + cp.cloneB.getId());
								//							System.out.println("catec B = " + cp.cloneB.getCategoryString());
								//							System.out.println("clone A fileName = " + cp.cloneA.getFileName());
								//							System.out.println("clone B fileName = " + cp.cloneB.getFileName());
								//							System.out.println("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine());
								//							System.out.println("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine());
								//							System.out.println("============= = ");
								i.remove();
								deleteCP++;
								break;
							}


						}else {
							if(Block.equalsCodeInfo(block, cp.cloneA)) {


//							if(block.getFileName() == cp.cloneA.getFileName() &&
//									block.getStartLine() == cp.cloneA.getStartLine() &&
//									block.getEndLine() == cp.cloneA.getEndLine()) {

								//							System.out.println("DELETE CLONEPAIR because update C ");
								//							System.out.println("============= = ");
								//							System.out.println("block  = " + block.getId());
								//							System.out.println("block  = " + block.getFileName());
								//							System.out.println("block startLine =" + block.getStartLine() + "endline = " + block.getEndLine());
								//							System.out.println("clone A = " + cp.cloneA.getId());
								//							System.out.println("catec A = " + cp.cloneA.getCategoryString());
								//							System.out.println("clone B = " + cp.cloneB.getId());
								//							System.out.println("catec B = " + cp.cloneB.getCategoryString());
								//							System.out.println("clone A fileName = " + cp.cloneA.getFileName());
								//							System.out.println("clone B fileName = " + cp.cloneB.getFileName());
								//							System.out.println("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine());
								//							System.out.println("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine());
								//							System.out.println("============= = ");
								i.remove();
								deleteCP++;
								break;

							}
							if(Block.equalsCodeInfo(block, cp.cloneB)) {


//							if(block.getFileName() == cp.cloneB.getFileName() &&
//									block.getStartLine() == cp.cloneB.getStartLine() &&
//									block.getEndLine() == cp.cloneB.getEndLine()) {

								//							System.out.println("DELETE CLONEPAIR because update D ");
								//							System.out.println("============= = ");
								//							System.out.println("block  = " + block.getId());
								//							System.out.println("block  = " + block.getFileName());
								//							System.out.println("block startLine =" + block.getStartLine() + "endline = " + block.getEndLine());
								//							System.out.println("clone A = " + cp.cloneA.getId());
								//							System.out.println("catec A = " + cp.cloneA.getCategoryString());
								//							System.out.println("clone B = " + cp.cloneB.getId());
								//							System.out.println("catec B = " + cp.cloneB.getCategoryString());
								//							System.out.println("clone A fileName = " + cp.cloneA.getFileName());
								//							System.out.println("clone B fileName = " + cp.cloneB.getFileName());
								//							System.out.println("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine());
								//							System.out.println("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine());
								//							System.out.println("============= = ");
								i.remove();
								deleteCP++;
								break;
							}
						}
					}
				}
			}

		}


		//		for(Block block : updatedBlockList) {
		//			if(block.getOldBlock() == null) {
		//				System.out.println(" OLD BLOCK NULL ");
		//				System.out.println(" block  fileName" + block.getFileName());
		//				System.out.println(" block start line  " + block.getStartLine());
		//				System.out.println(" block end   line  " + block.getEndLine());
		//				System.out.println(" block cate        " + block.getCategory());
		//			}else {
		//				System.out.println(" OLD BLOCK  ");
		//				System.out.println(" old block " + block.getOldBlock().getFileName());
		//				System.out.println(" old block start line  " + block.getOldBlock().getStartLine());
		//				System.out.println(" old block end   line  " + block.getOldBlock().getEndLine());
		//				System.out.println(" old block cate        " + block.getCategory());
		//			}
		//
		//		}


		//		System.out.println("The number of deleted Clone Pairs = " + deleteCP);

		//	Iterator it = updatedBlockList.iterator();

		/*	while(it.hasNext())
		{
		    Block value = (Block)it.next();
		    System.out.println("updated ID " + value.getId());
		}*/

	}


	public static void deleteOldBlock(List<ClonePair> ClonePairList,  List<Block> updatedBlockList) {

		//ClonePairList.removeIf(ClonePair -> ClonePair.cloneA);

		//iteratorでぶん回す？
		Iterator<ClonePair> i = ClonePairList.iterator();
		int j = 0;
		while(i.hasNext()) {
			//	System.out.println("clone pair j  = " + j++);
			ClonePair cp  = i.next();
			/*			System.out.println("============= = ");
			System.out.println("clone A = " + cp.cloneA.getId());
			System.out.println("clone B = " + cp.cloneB.getId());
			System.out.println("============= = ");
			 */
			int aU = updatedBlockList.indexOf(cp.cloneA);
			int bU = updatedBlockList.indexOf(cp.cloneB);
			if((aU != -1) || (bU != -1)) {
				/*			System.out.println("DELETE CLONEPAIR ");
				System.out.println("============= = ");
				System.out.println("clone A = " + cp.cloneA.getId());
				System.out.println("clone B = " + cp.cloneB.getId());
				System.out.println("============= = ");
				 */		i.remove();
			}

		}

		//	Iterator it = updatedBlockList.iterator();

		/*	while(it.hasNext())
		{
		    Block value = (Block)it.next();
		    System.out.println("updated ID " + value.getId());
		}*/

	}


	public static void updateClonePairBlock(Block blockA, Block blockB, AllData allData) {
		// TODO 自動生成されたメソッド・スタブ
		int bId = blockB.getId();
		ArrayList<ClonePair> clonePair = allData.getClonePairList();
		for(ClonePair cp : clonePair) {
			//			if(cp.cloneA.equals(blockB)) {
			if(Block.equalsCodeInfo(cp.cloneA, blockB)) {


//			if(cp.cloneA.getFileName() == blockB.getFileName() &&
//					cp.cloneA.getStartLine() == blockB.getStartLine() &&
//					cp.cloneA.getEndLine() == blockB.getEndLine()) {
				cp.cloneA = null;
				cp.setCloneA(blockA);
				if(CloneDetector.modeDebug) {
					System.out.println("change clone stable");
				}
			}

			if(Block.equalsCodeInfo(cp.cloneB, blockB)) {


//			if(cp.cloneB.getFileName() == blockB.getFileName() &&
//					cp.cloneB.getStartLine() == blockB.getStartLine() &&
//					cp.cloneB.getEndLine() == blockB.getEndLine()) {
				cp.cloneB = null;
				cp.setCloneB(blockA);
				if(CloneDetector.modeDebug) {
					System.out.println("change clone stable");
				}
			}
			//
			//			if(cp.getCloneAId() == bId ) {
			//				cp.cloneA = null;
			//				cp.setCloneA(blockA);
			//			}
			//			if(cp.getCloneBId() == bId ) {
			//				cp.cloneB = null;
			//				cp.setCloneB(blockA);
			//			}
		}

	}


	public static void deleteClonePair(ArrayList<Block> deleteBlock, AllData allData) {
		// TODO 自動生成されたメソッド・スタブ
		ArrayList<ClonePair> clonePairList = allData.getClonePairList();

		Iterator<ClonePair> i = clonePairList.iterator();
		int j = 0;
		while(i.hasNext()) {
			ClonePair cp  = i.next();

			for(Block block : deleteBlock) {

				if(block.getOldBlock() != null) {
					if(Block.equalsCodeInfo(block,cp.cloneA)) {


//					if(block.getOldBlock().getFileName() == cp.cloneA.getFileName() &&
//							block.getOldBlock().getStartLine() == cp.cloneA.getStartLine() &&
//							block.getOldBlock().getEndLine() == cp.cloneA.getEndLine()) {
						if(CloneDetector.modeDebug) {
							System.out.println("Delete filetered clone");
						}
						i.remove();
						break;
					}
					if(Block.equalsCodeInfo(block,cp.cloneB)) {
//					if(block.getOldBlock().getFileName() == cp.cloneB.getFileName() &&
//							block.getOldBlock().getStartLine() == cp.cloneB.getStartLine() &&
//							block.getOldBlock().getEndLine() == cp.cloneB.getEndLine()) {
						if(CloneDetector.modeDebug) {
							System.out.println("Delete filetered clone");
						}
						i.remove();
						break;
					}



				}


			}
		}
	}


}
