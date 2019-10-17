package jp.ac.osaka_u.ist.sel.icvolti.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class AllData implements  Serializable {

	static ArrayList<SourceFile> SourceFileList;
	static ArrayList<ClonePair> ClonePairList;
	static ArrayList<Block> BlockListOfCalcedVec;
	static Map<String, Integer> wordMap;
	static int wordFreq[];


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
		int i = 0;

		System.out.println(" ===  block list size  " + blockList.size());
		System.out.println(" ===  calced block list size  " + BlockListOfCalcedVec.size());
		for(Block block : blockList) {
			if(block.getVector() == null) {
				System.out.println(i + "vec null" + block.getId());

			}else {
				System.out.println(i +  " vec " +  block.getId());
			}
			i++;
		}
		for(ClonePair cp : ClonePairList) {
			int idA = cp.cloneA.getId();
			int idB = cp.cloneB.getId();
			System.out.println("idA = " + idA);
			System.out.println("idB = " + idB);
		}
		for(ClonePair cp : ClonePairList) {
			int idA = cp.cloneA.getId();
			int idB = cp.cloneB.getId();
		//	System.out.println("test");
			System.out.println("idA = " + idA);
			System.out.println("idB = " + idB);
			System.out.println("blockList.get(idA).getID() = " + blockList.get(idA).getId());
			System.out.println("blockList.get(idB).getID() = " + blockList.get(idB).getId());
			System.out.println("cp.cloneA.getFileName()" + cp.cloneA.getFileName());
			System.out.println("cp.cloneB.getFileName()" + cp.cloneB.getFileName());
			System.out.println("blockList.get(idA).getFileName() = " + blockList.get(idA).getFileName());
			System.out.println("blockList.get(idB).getFileName() = " + blockList.get(idB).getFileName());


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
		int i =0;
		for(Block block : blockList) {
			block.setVector(BlockListOfCalcedVec.get(i).getVector());
			System.out.println("block.setLen(BlockListOfCalcedVec.get(i).getLen()) == " + BlockListOfCalcedVec.get(i).getLen());
			block.setLen(BlockListOfCalcedVec.get(i).getLen());
			block.setId(i);
			i++;
		}
		return blockList;
	}




}
