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

import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
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
	 * ブロックリストをデシリアライズ化
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
	 * ソースファイルをブロック
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


	public static ArrayList<SourceFile> updateSourceFileList(String newTarget, String oldTarget, ArrayList<SourceFile> FileList, ArrayList<String> newFileListOrigin){
		ArrayList<SourceFile> fileList = new ArrayList<SourceFile>();

		ArrayList<String> newFileList = (ArrayList<String>) newFileListOrigin.clone();
		int fileId = 0;
		for(SourceFile file : FileList ) {
			String newFilePath = file.getNewPath();
			String oldFilePath = file.getOldPath();
			System.out.println("oldFile = " + newFilePath);
			file.setOldPath(newFilePath);
			String newTargetFilePath = newTarget + file.getOldPath().substring(oldTarget.length());

			int index = newFileList.indexOf(newTargetFilePath);
			if(index > -1) {
				file.setNewPath(newTargetFilePath);
				System.out.println("newFile = " + newTargetFilePath);
				newFileList.remove(newTargetFilePath);
				file.setState(SourceFile.NORMAL);
				for(Block block : file.getNewBlockList()) {
					block.setFileName(newTargetFilePath);
				}
				file.setId(fileId++);
				fileList.add(file);
			}else {
				//新バージョンのファイルリストに含まれないファイルは削除されたもの
				file.setState(SourceFile.DELETED);

				 // ここで削除されたファイルのコードブロックに関わる情報は削除？

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


}
