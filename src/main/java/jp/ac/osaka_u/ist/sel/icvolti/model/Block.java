package jp.ac.osaka_u.ist.sel.icvolti.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.OpenMapRealVector;

public class Block implements Comparable<Block>, Serializable,Cloneable {

	private int id;
	private String fileName;
	private String oldfileName;
	private int startLine;
	private int endLine;
	private int methodStartLine;
	private int methodEndLine;
	private int category = NULL;
	private String name;
	// private String code;
	private double len;
	private double localitySimilarity = 0.0;
	private ArrayList<Word> wordList = new ArrayList<Word>();
	private int nodeNum = 0;
	private OpenMapRealVector vector;
	// private HashMap<Integer, String> stringVector;
	// private boolean checkFlg = false;
	private Block parent;
	//旧バージョンのコードブロック
	private Block oldBlock = null;
	//新バージョンのコードブロック
	private Block newBlock = null;
	// private ArrayList<Block> children;

	/** このクローンを含んでいるファイル */



	/** <p>初期状態</p> */
	public final static int NULL = -1;

	/** <p>Stable Clone</p> */
	public final static int STABLE = 0;

	/** <p>Modified Clone</p> */
	public final static int MODIFIED = 1;

	/** <p>Moved Clone</p> */
	public final static int MOVED = 2;

	/** <p>Added Clone</p> */
	public final static int ADDED = 3;

	/** <p>Deleted Clone</p> */
	public final static int DELETED = 4;

	/** <p>Modified and Deleted Clone</p> */
	public final static int DELETE_MODIFIED = 5;


	public Block() {

	}

	public Block(Block block) {

		this.category = block.getCategory();
		this.fileName = block.getFileName();
		this.oldfileName = block.getOldFileName();

		this.name = block.getName();
		this.startLine = block.getStartLine();
		this.endLine = block.getEndLine();
	//	this.startColumn = block.getStartColumn();
	//	this.endColumn = block.getEndColumn();
	//	this.startToken = block.getStartToken();
	//	this.endToken = block.getEndToken();
		this.newBlock = block.getNewBlock();
		this.newBlock = block.getOldBlock();
	}

	/**
	 * <p>
	 * メソッドIDの取得
	 * </p>
	 *
	 * @return
	 */
	public final int getId() {
		return id;
	}

	/**
	 * <p>
	 * メソッドIDの設定
	 * </p>
	 *
	 * @param id
	 */
	public final void setId(int id) {
		this.id = id;
	}

	/**
	 * <p>
	 * メソッド名の取得
	 * </p>
	 *
	 * @return
	 */
	public final String getName() {
		return name;
	}

	/**
	 * <p>
	 * メソッド名の設定
	 * </p>
	 *
	 * @param name
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>
	 * クラス名の取得
	 * </p>
	 *
	 * @return
	 */
	public final String getFileName() {
		return fileName;
	}

	/**
	 * <p>
	 * クラス名の設定
	 * </p>
	 *
	 * @param className
	 */
	public final void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * <p>
	 * 旧クラス名の取得
	 * </p>
	 *
	 * @return
	 */
	public final String getOldFileName() {
		return oldfileName;
	}

	/**
	 * <p>
	 * 旧クラス名の設定
	 * </p>
	 *
	 * @param className
	 */
	public final void setOldFileName(String oldfileName) {
		this.oldfileName = oldfileName;
	}

	public final double getLen() {
		return len;
	}

	public final void setLen(double len) {
		this.len = len;
	}

	/**
	 * <p>
	 * ワードリストへの追加
	 * </p>
	 */
	public final boolean addWord(String word) {
		for(Word w : wordList) {
			if(w.getName().equals(word)) {
				w.addCount(1);
				return true;
			}
		}
		wordList.add(new Word(word, Word.WORD, 1));
		return true;
	}

	/**
	 * <p>
	 * ワードリストへの追加
	 * </p>
	 */
	public final boolean addWord(String[] words) {
		for (String word : words) {
			this.addWord(word);
		}
		return true;
	}

	/**
	 * <p>
	 * ワードリストの取得
	 * </p>
	 *
	 * @return
	 */
	public final ArrayList<Word> getWordList() {
		return wordList;
	}

	/**
	 * <p>
	 * ワードリストの設定
	 * </p>
	 *
	 * @return
	 */
	public final void setWordList(ArrayList<Word> wordList) {
		this.wordList = wordList;
	}

	public final void clearWordList() {
		wordList.clear();
		wordList = null;
	}

	/**
	 * <p>
	 * ノード数取得
	 * </p>
	 *
	 * @return
	 */
	public final int getNodeNum() {
		return nodeNum;
	}

	/**
	 * <p>
	 * ノード数設定
	 * </p>
	 *
	 * @param nodeNum
	 */
	public final void setNodeNum(int nodeNum) {
		this.nodeNum = nodeNum;
	}

	/**
	 * <p>
	 * ノード数加算
	 * </p>
	 *
	 * @param
	 */
	public final void incNodeNum() {
		nodeNum++;
	}

	/**
	 * <p>
	 * 特徴ベクトルの取得
	 * </p>
	 *
	 * @return
	 */
	public final OpenMapRealVector getVector() {
		return vector;
	}

	/**
	 * <p>
	 * 特徴ベクトルの設定
	 * </p>
	 *
	 * @param vector
	 */
	public final void setVector(OpenMapRealVector vector) {
		this.vector = vector;
	}

	// /**
	// * <p>判定</p>
	// * @return
	// */
	// public final boolean isCheckFlg() {
	// return checkFlg;
	// }
	//
	// /**
	// * <p>判定</p>
	// * @param checkFlg
	// */
	// public final void setCheckFlg(boolean checkFlg) {
	// this.checkFlg = checkFlg;
	// }

	/**
	 * <p>
	 * 開始行の取得
	 * </p>
	 *
	 * @return
	 */
	public final int getStartLine() {
		return startLine;
	}

	/**
	 * <p>
	 * 開始行の設定
	 * </p>
	 *
	 * @param startLine
	 */
	public final void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	/**
	 * <p>
	 * 終了行の取得
	 * </p>
	 *
	 * @return
	 */
	public final int getEndLine() {
		return endLine;
	}

	/**
	 * <p>
	 * 終了行の設定
	 * </p>
	 *
	 * @param endLine
	 */
	public final void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	/**
	 * <p>
	 * ブロックの行数の取得
	 * </p>
	 *
	 * @param endLine
	 */
	public final int getLineSize() {
		return endLine - startLine + 1;
	}

	/**
	 * <p>
	 * 開始行の取得
	 * </p>
	 *
	 * @return
	 */
	public final int getMethodStartLine() {
		return methodStartLine;
	}

	/**
	 * <p>
	 * 開始行の設定
	 * </p>
	 *
	 * @param startLine
	 */
	public final void setMethodStartLine(int startLine) {
		this.methodStartLine = startLine;
	}

	/**
	 * <p>
	 * 終了行の取得
	 * </p>
	 *
	 * @return
	 */
	public final int getMethodEndLine() {
		return methodEndLine;
	}

	/**
	 * <p>
	 * 終了行の設定
	 * </p>
	 *
	 * @param endLine
	 */
	public final void setMethodEndLine(int endLine) {
		this.methodEndLine = endLine;
	}


	/**
	 * <p>旧コードブロックの取得</p>
	 * @return 旧コードブロックオブジェクト
	 */
	public final Block getOldBlock() {
		return oldBlock;
	}

	/**
	 * <p>旧コードブロックの設定</p>
	 * @param oldBlock 旧コードブロックオブジェクト
	 */
	public void setOldBlock(Block oldBlock) {
		this.oldBlock = oldBlock;
	}

	/**
	 * <p>新コードブロックの取得</p>
	 * @return 新コードブロックオブジェクト
	 */
	public Block getNewBlock() {
		return newBlock;
	}

	/**
	 * <p>新コードブロックの設定</p>
	 * @param newBlock 新コードブロックオブジェクト
	 */
	public void setNewBlock(Block newBlock) {
		this.newBlock = newBlock;
	}

	/**
	 * <p>コードブロック分類の取得</p>
	 * @return 分類情報
	 */
	public int getCategory() {
		return category;
	}

	/**
	 *  <p>コードブロック分類の取得</p>
	 * @return 分類情報
	 */
	public String getCategoryString() {
		String str = null;
		switch (category) {
		case ADDED:
			str = "ADDED";
			break;
		case DELETED:
			str = "DELETED";
			break;
		case MODIFIED:
			str = "MODIFIED";
			break;
		case MOVED:
			str = "MOVED";
			break;
		case STABLE:
			str = "STABLE";
			break;
		case DELETE_MODIFIED:
			str = "DELETE_MODIFIED";
		}
		return str;
	}

	/**
	 * <p>クローン分類の設定</p>
	 * @param category 分類情報
	 */
	public void setCategory(int category) {
		this.category = category;
	}

	/**
	 * <p>
	 * 親ブロックの取得
	 * </p>
	 *
	 * @return
	 */
	public final Block getParent() {
		return parent;
	}

	/**
	 * <p>
	 * 親ブロックの設定
	 * </p>
	 *
	 * @param parent
	 */
	public final void setParent(Block parent) {
		this.parent = parent;
	}


	/**
	 * 親子クローン同士位置類似度
	 * @author s-tokui
	 * @return double localitySimilarity
	 */
	public double getLocationSimilarity() {
		return localitySimilarity;
	}

	/**
	 * 親子クローン同士位置類似度の設定
	 * @author s-tokui
	 * @param double
	 *            localitySimilarity
	 */
	public void setLocationSimilarity(double sim) {
		this.localitySimilarity = sim;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}


	/**
	 * <p>
	 * ブロックリストをシリアライズ化
	 * <p>
	 * @param blocklist
	 */
	public static void serializeBlockList(List<Block> blockList) {
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


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			//System.out.println(" object true");
			return true;
		}
		if (obj == null) {
			//System.out.println(" object null");
			return false;
		}
		if (getClass() != obj.getClass()) {
			//System.out.println(" object class difference");
			return false;
		}
		Block other = (Block) obj;
		if (id != other.id) {
			//System.out.println(" object id difference");
			return false;
		}
		return true;
	}

	public boolean equals(Object obj, int i) {
		if (this == obj) {
//			System.out.println(" object true");
			return true;
		}
		if (obj == null) {
//			System.out.println(" object null");
			return false;
		}
		if (getClass() != obj.getClass()) {
//			System.out.println(" object class difference");
			return false;
		}
		Block other = (Block) obj;
		if (id != other.id) {
			//System.out.println(" object id difference");
			return false;
		}
		return true;
	}

	public boolean match(String className, String name) {
		if (this.fileName.equals(className) && this.name.equals(name)) {
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(Block b) {
		return this.id - b.id;
	}

    @Override
    public Block clone(){

        Block block = new Block();
        try {
           block = (Block)super.clone();
        }catch (Exception e){
            e.printStackTrace();
        }
        return block;
    }

}
