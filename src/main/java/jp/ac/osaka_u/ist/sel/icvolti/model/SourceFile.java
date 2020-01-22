package jp.ac.osaka_u.ist.sel.icvolti.model;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * ソースファイルデータクラス
 * @author h-honda
 */
public class SourceFile implements Serializable {

	/** ファイルID */
	private int id = NULL;

	/** メソッドの数 */
	private int numMethodinFile = 0;

	/** ファイル名 */
	private String name = null;

	/** 最新プロジェクトにおけるパス */
	private String newPath = null;

	/** 旧プロジェクトにおけるパス */
	private String oldPath = null;

	private boolean copyRightModified = false;

	/** ファイルの状態 */
	private int state = NULL;

	/** 新バージョンにおける, このファイルに属するブロック */
	private ArrayList<Block> newBlockList = new ArrayList<Block>();

	/** 旧バージョンにおける, このファイルに属するブロック */
	private ArrayList<Block> oldBlockList = new ArrayList<Block>();

	/** 新バージョンで追加されたコード行 */
	private ArrayList<Integer> addedCodeList = new ArrayList<Integer>();

	/** 新バージョンで削除されたコード行 */
	private ArrayList<Integer> deletedCodeList = new ArrayList<Integer>();

	/**
	 * 旧バージョンのメソッドリスト
	 * @author m-sano
	 */
	private ArrayList<Method> oldMethodList = new ArrayList<Method>();

	/**
	 * 新バージョンのメソッドリスト
	 * @author m-sano
	 */
	private ArrayList<Method> newMethodList = new ArrayList<Method>();

	/**
	 * <p>ソースファイルIDの取得</p>
	 * @return ソースファイルID
	 */
	public int getId() {
		return id;
	}

	/**
	 * <p>ソースファイルIDの設定</p>
	 * @param name ソースファイルID
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * <p>ソースファイル名の取得</p>
	 * @return ソースファイル名
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>ソースファイル名の設定</p>
	 * @param name ソースファイル名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>新バージョンソースファイルパスの取得</p>
	 * @return　ファイルパス
	 */
	public String getNewPath() {
		return newPath;
	}

	/**
	 * <p>新バージョンソースファイルパスの設定</p>
	 * @param newPath ファイルパス
	 */
	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}

	/**
	 * <p>旧バージョンソースファイルパスの取得</p>
	 * @return　ファイルパス
	 */
	public String getOldPath() {
		return oldPath;
	}

	/**
	 * <p>旧バージョンソースファイルパスの設定</p>
	 * @param oldPath ファイルパス
	 */
	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}

	/**
	 * <p>新バージョンソースファイルに含まれるブロックリストの取得</p>
	 * @return ブロックリスト
	 */
	public ArrayList<Block> getNewBlockList() {
		return newBlockList;
	}


	/**
	 * <p>新バージョンソースファイルに含まれるブロックリストの設定</p>
	 * @param newBlockList ブロックリスト
	 */
	public void setNewBlockList(ArrayList<Block> newBlockList) {
		this.newBlockList = newBlockList;
	}

	/**
	 * <p>旧バージョンソースファイルに含まれるブロックリストの取得</p>
	 * @return　ブロックリスト
	 */
	public ArrayList<Block> getOldBlockList() {
		return oldBlockList;
	}

	/**
	 * <p>旧バージョンソースファイルに含まれるブロックリストの取得</p>
	 * @return　ブロックリスト
	 */
	public void initBlockList() {
		newBlockList = new ArrayList<Block>();
		oldBlockList = new ArrayList<Block>();
	}

	/**
	 * <p>旧バージョンソースファイルに含まれるブロックリストの設定</p>
	 * @param oldBlockList ブロックリスト
	 */
	public void setOldBlockList(ArrayList<Block> oldBlockList) {
		this.oldBlockList = oldBlockList;
	}

	/**
	 * <p>追加コードリストの取得</p>
	 * @return コードリスト
	 */
	public ArrayList<Integer> getAddedCodeList() {
		return addedCodeList;
	}

	/**
	 * <p>削除コードリストの取得</p>
	 * @return コードリスト
	 */
	public ArrayList<Integer> getDeletedCodeList() {
		return deletedCodeList;
	}

	/** <p>初期状態</p> */
	public final static int NULL = -1;

	/** <p>存続ファイル</p> */
	public final static int STABLE = 0;

	/** <p>追加ファイル</p> */
	public final static int ADDED = 1;

	/** <p>削除ファイル</p> */
	public final static int DELETED = 2;

	/** <p>変更ファイル</p> */
	public final static int MODIFIED = 3;

	/**
	 * <p>ソースファイル状態の取得</p>
	 * @return 状態
	 */
	public int getState() {
		return state;
	}

	/**
	 * <p>ソースファイル状態の設定</p>
	 * @param state 状態
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * <p>ソースファイルに含まれる関数の数</p>
	 * @return 関数の数
	 */
	public int getNumMethod() {
		return numMethodinFile;
	}

	/**
	 * <p>ソースファイルに含まれる関数の数の設定</p>
	 * @param 関数の数
	 */
	public void setNumMethod(int numMethodinFile) {
		this.numMethodinFile = numMethodinFile;
	}

	/**
	 * <p>ブロックリストを行番号順で整列</p>
	 */
	public void sortBlockListbyLine() {
		sortBlockListbyLine(newBlockList);
		sortBlockListbyLine(oldBlockList);
	}

	/**
	 * <p>ブロックリストを行番号順で整列</p>
	 * @param blockList ブロックリスト
	 */
	private void sortBlockListbyLine(ArrayList<Block> blockList) {
		for(int i = 0; i < blockList.size(); i++) {
			for (int j = i; j > 0 && blockList.get(j).getStartLine() < blockList.get(j - 1).getStartLine(); j--) {
				Block tmp = blockList.get(j);
				blockList.set(j,blockList.get(j-1));
				blockList.set(j-1,tmp);
			}
		}
	}

	/**
	 * <p>ブロックリストをトークン順で整列</p>
	 */
/*	public void sortBlockListbyToken() {
		sortBlockListbyToken(newBlockList);
		sortBlockListbyToken(oldBlockList);
	}
*/
	/**
	 * <p>ブロックリストをトークン順で整列</p>
	 * @param blockList ブロックリスト
	 */
/*	private void sortBlockListbyToken(ArrayList<Block> blockList) {
		for(int i = 0; i < blockList.size(); i++) {
			for(int j = i; j > 0 && blockList.get(j).getStartToken() < blockList.get(j-1).getStartToken(); j--) {
				Block tmp = blockList.get(j);
				blockList.set(j,blockList.get(j-1));
				blockList.set(j-1,tmp);
			}
		}
	}
*/
	/**
	 * <p>ブロックリストをメソッド名順で整列</p>
	 * @author m-sano
	 */
	public void sortBlockListbyMethod() {
		sortBlockListbyMethod(newBlockList);
		sortBlockListbyMethod(oldBlockList);
	}

	/**
	 * <p>ブロックリストをメソッド名順で整列</p>
	 * @author m-sano
	 * @param blockList ブロックリスト
	 */
	private void sortBlockListbyMethod(ArrayList<Block> blockList) {
		for(int i = 0; i < blockList.size(); i++) {
			for(int j = i; j > 0 && blockList.get(j).getName().compareTo(blockList.get(j-1).getName()) < 0; j--) {
				Block tmp = blockList.get(j);
				blockList.set(j, blockList.get(j-1));
				blockList.set(j-1, tmp);
			}
		}
	}

	/**
	 * 旧バージョンのメソッドリストを取得する
	 * @return 旧バージョンのメソッドリスト
	 */
	public ArrayList<Method> getOldMethodList() {
		return oldMethodList;
	}

	/**
	 * 旧バージョンのメソッドリストを設定する
	 * @param oldMethodList 旧バージョンのメソッドリスト
	 */
	public void setOldMethodList(ArrayList<Method> oldMethodList) {
		this.oldMethodList = oldMethodList;
	}

	/**
	 * 新バージョンのメソッドリストを取得する
	 * @return 新バージョンのメソッドリスト
	 */
	public ArrayList<Method> getNewMethodList() {
		return newMethodList;
	}

	/**
	 * 新バージョンのメソッドリストを設定する
	 * @param newMethodList 新バージョンのメソッドリスト
	 */
	public void setNewMethodList(ArrayList<Method> newMethodList) {
		this.newMethodList = newMethodList;
	}

	public boolean isCopyRightModified() {
		return copyRightModified;
	}

	public void setCopyRightModified(boolean copyRightModified) {
		this.copyRightModified = copyRightModified;
	}

	/**
	 * このファイル中のメソッドを取得する.
	 * Class.Subclass.Method(Type param,Type param)
	 * @param modifiedName 修飾子 + メソッド名(Cはファイル名)
	 * @param methodName メソッド名
	 * @param params 引数部. ()不要 (Cは未使用)
	 * @param isNew trueなら新バージョンのメソッド, falseなら旧バージョンのメソッドを探索する
	 * @param forJava java用ならtrue
	 * @return フルネームに対応するMethod
	 * @author m-sano
	 */
/*	public Method getMethod(String modifiedName, String methodName, String params, boolean isNew, boolean forJava) {
		Iterator<Method> it;

		Logger.writeln("<SourceFile.getMethod> " + name, Logger.DEBUG);

		if(isNew) {
			it = newMethodList.iterator();
		} else {
			it = oldMethodList.iterator();
		}

		Logger.writeln("<SourceFile.getMethod> search - " + modifiedName + params, Logger.DEBUG);

		while(it.hasNext()) {
			Method md = it.next();
			Logger.writeln("<SourceFile.getMethod> equal check - " + md.getModifiedName() + md.getParams(), Logger.DEBUG);
			if(md.equalsMethod(modifiedName, methodName, params, forJava)) {
				Logger.writeln("<SourceFile.getMethod> return", Logger.DEBUG);
				return md;
			}
			Logger.writeln("<SourceFile.getMethod> next loop", Logger.DEBUG);
		}
		Logger.writeln("<SourceFile.getMethod> return null", Logger.DEBUG);
		return null;
	}
*/


	/**
	 * メソッドリストをクリアする.
	 * ヒープ領域不足の一時しのぎ.
	 */
	/*
	@Deprecated
	public void clearMethodList() {
		newMethodList = new ArrayList<Method>();
		oldMethodList = new ArrayList<Method>();
	}
	*/
}

