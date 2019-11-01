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
	public void serializeBlockList(List<Block> blockList) {
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
	public static List<Block> deserializeBlockList(String blockListName) {
		try {
			ObjectInputStream objInStream
			= new ObjectInputStream(
					new FileInputStream(blockListName));

			List<Block> blockList = (List<Block>) objInStream.readObject();

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

		ArrayList<String> newFileList = (ArrayList<String>) newFileListOrigin.clone();
		int fileId = 0;
		for(SourceFile file : oldFileList ) {
			String newFilePath = file.getNewPath();
			String oldFilePath = file.getOldPath();
			//System.out.println("oldFile = " + newFilePath);
			file.setOldPath(newFilePath);
			String newTargetFilePath = newTarget + file.getOldPath().substring(oldTarget.length());

			int index = newFileList.indexOf(newTargetFilePath);
			if(index > -1) {
				file.setNewPath(newTargetFilePath);
				//System.out.println("newFile = " + newFilePath);
				newFileList.remove(newTargetFilePath);
				file.setState(SourceFile.NORMAL);
				for(Block block : file.getNewBlockList()) {
					block.setOldFileName(block.getFileName());
					block.setFileName(newTargetFilePath);
					//とりあえず，ノーマルなファイルに含まれるブロックはSTABLEに分類
					block.setCategory(Block.STABLE);
				}
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
				updatedBlockList.addAll(file.getNewBlockList());

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


	public static void resetClonePair(List<ClonePair> ClonePairList,  List<Block> updatedBlockList) {

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
			for(Block block : updatedBlockList) {
				//System.out.println("aa");
				int category  = block.getCategory();
				if (category == Block.MODIFIED ||category == Block.DELETED) {
					//System.out.println("=========  category  =========  " + category);
					//if(block.getOldBlock() != null) {

					//	System.out.println(" old block " + block.getOldBlock().getFileName());
					//	System.out.println(" cpA block " + cp.cloneA.getFileName());
					//	System.out.println(" cpB block " + cp.cloneB.getFileName());
						int aU = updatedBlockList.indexOf(cp.cloneA);
						int bU = updatedBlockList.indexOf(cp.cloneB);
						//if(block.getOldBlock().equals(cp.cloneA,1) || block.getOldBlock().equals(cp.cloneB,1) ) {
						if((aU != -1) || (bU != -1)) {
							System.out.println("DELETE CLONEPAIR ");
							System.out.println("============= = ");
							System.out.println("clone A = " + cp.cloneA.getId());
							System.out.println("clone B = " + cp.cloneB.getId());
							System.out.println("clone A fileName = " + cp.cloneA.getFileName());
							System.out.println("clone B fileName = " + cp.cloneB.getFileName());
							System.out.println("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine());
							System.out.println("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine());
							System.out.println("============= = ");
							i.remove();
							deleteCP++;
							break;
						}
					//}
				}

			}
			/*
			int aU = updatedBlockList.indexOf(cp.cloneA);
			int bU = updatedBlockList.indexOf(cp.cloneB);
			System.out.println(" au = " + aU);
			System.out.println(" bu = " + bU);
			if((aU != -1) || (bU != -1)) {
				System.out.println("DELETE CLONEPAIR ");
				System.out.println("============= = ");
				System.out.println("clone A = " + cp.cloneA.getId());
				System.out.println("clone B = " + cp.cloneB.getId());
				System.out.println("============= = ");
				i.remove();
				break;
			}*/

		}
		System.out.println("The number of deleted Clone Pairs = " + deleteCP);

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
			if(cp.getCloneAId() == bId ) {
				cp.cloneA = null;
				cp.setCloneA(blockA);
			}
			if(cp.getCloneBId() == bId ) {
				cp.cloneB = null;
				cp.setCloneB(blockA);
			}
		}

	}





}
