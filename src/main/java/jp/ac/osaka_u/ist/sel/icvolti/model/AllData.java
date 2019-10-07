package jp.ac.osaka_u.ist.sel.icvolti.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class AllData implements  Serializable {

	static ArrayList<SourceFile> SourceFileList;
	static ArrayList<ClonePair> ClonePairList;


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
	 * <p>
	 * allDataをシリアライズ化
	 * <p>
	 * @param allData
	 */
	public static void serializeAllDataList(AllData allData) {
		 try {
             ObjectOutputStream objOutStream =
             new ObjectOutputStream(
             new FileOutputStream("allData.bin"));
             objOutStream.writeObject(allData);
             objOutStream.close();
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
	public static AllData deserializeAllDataList(String allDataName) {
        try {
            ObjectInputStream objInStream
              = new ObjectInputStream(
                new FileInputStream(allDataName));

            AllData allData = (AllData) objInStream.readObject();

            objInStream.close();

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
	 */
	public static void synchronizeAllData() {

		ArrayList<Block> blockList = new ArrayList<>();
		blockList.addAll(createBlockList());
		for(ClonePair cp : ClonePairList) {
			int idA = cp.cloneA.getId();
			int idB = cp.cloneB.getId();
			System.out.println("test");

			cp.setCloneA(blockList.get(idA));
			cp.setCloneB(blockList.get(idB));

		}


	}

	/**
	 * <p>
	 * allDataのSourceFileListとClonePairListの同期をとる
	 * <p>
	 */
	public static ArrayList<Block> createBlockList() {
		ArrayList<Block> blockList = new ArrayList<>();

		for (SourceFile file : SourceFileList) {
			blockList.addAll(file.getNewBlockList());

		}
		return blockList;
	}




}
