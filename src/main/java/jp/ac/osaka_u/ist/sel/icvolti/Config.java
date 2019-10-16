package jp.ac.osaka_u.ist.sel.icvolti;

import java.util.ArrayList;

import jp.ac.osaka_u.ist.sel.icvolti.model.CloneSet;
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;

public class Config {

	//	private String target = null;
	//	private String target2 = null;
	private String oldTarget = null;
	private String newTarget = null;
	private String DATASET_FILE = "dataset.txt";
	private String LSH_FILE = "lsh_result.txt";
	private String resultTXT = null;
	private String resultCSV = null;
	private String resultHTML = null;
	private String resultNotifier = null;
	private String resultCloneSet = null;
	private int JAVA = 0;
	private int CPP = 1;
	private int lang = JAVA;
	public static String charset = "UTF-8";
	private int NUM_THREADS;
	// private  boolean paramFlg = true;

	//LSHパラメータ
	public static int LSH_PRG = LSHController.FALCONN64;
	//    private  int LSH_PRG = LSHController.FALCONN32;
	//    private  int LSH_PRG = LSHController.E2LSH;
	//    private  int LSH_PRG = LSHController.NO_LSH;
	public static double LSH_R =1.0;
	public static double LSH_PROB = 0.9;
	public static int LSH_L = 20; // HASH_TABLE_NUM

	//検出パラメータ
	public static int METHOD_NODE_TH =50;
	public static int BLOCK_NODE_TH = 50;
	public static int LINE_TH = 0;

	public static double DIS_TH = 0.2;//0.2
	public static int DIFF_TH = 30;//30
	public static double SIM_TH = 0.9;

	//評価用
	public static double E_DIFF=45.0;

	/** int用NULL */
	public static int NULL = -1;

	/** output format */
	private String outputFormat = null;


	/** 過去のデータのディレクトリパス */
	private String dataDir = null;

	/** 出力先のディレクトリパス */
	private String outputDir = null;

	/** トークンの閾値. 指定トークン数以下のクローンを除外. */
	private int tokenTh;




	/** 過去のデータを使うならtrue */
	private boolean preData = false;


	/** プロジェクトが持つファイルのリスト */
	private ArrayList<SourceFile> fileList = new ArrayList<SourceFile>();

	/** プロジェクトが持つクローンセットのリスト */
	private ArrayList<CloneSet> cloneSetList = new ArrayList<CloneSet>();


	/** 入力対象のディレクトリを保存 **/
	private ArrayList<String> inputDir = new ArrayList<String>();





	/*
	 * <p>出力形式の取得</p>
	 * @return oldTarget
	 */
	public String getOldTarget() {
		return oldTarget;
	}

	/**
	 * <p>oldTargetの設定</p>
	 * @param name oldTarget
	 */
	public void setOldTarget(String oldTarget) {
		this.oldTarget = oldTarget;
	}

	/*
	 * <p>出力形式の取得</p>
	 * @return newTarget
	 */
	public String getNewTarget() {
		return newTarget;
	}

	/**
	 * <p>newTargetの設定</p>
	 * @param name newTarget
	 */
	public void setNewTarget(String newTarget) {
		this.newTarget = newTarget;
	}


	/*
	 * <p>出力形式の取得</p>
	 * @return outputFormat
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * <p>outputFormatの設定</p>
	 * @param name outputFormat
	 */
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}


	/*
	 * <p>過去のデータの場所の取得</p>
	 * @return dataDir
	 */
	public String getDataDir() {
		return dataDir;
	}

	/**
	 * <p>dataDirの設定</p>
	 * @param name dataDir
	 */
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	/*
	 * <p>過去のデータの場所の取得</p>
	 * @return outputDir
	 */
	public String getOutputDir() {
		return outputDir;
	}

	/**
	 * <p>outputDirの設定</p>
	 * @param name outputDir
	 */
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}


	/*
	 * <p>検出対象の場所の取得</p>
	 * @return outputDir
	 */
	public ArrayList<String> getInputDir() {
		return inputDir;
	}

	/**
	 * <p>inputDirの設定</p>
	 * @param name inputDir
	 */
	public void setInputDir(String inputDirStr) {
		inputDir.add(inputDirStr);
	}

	/**
	 * <p>inputDirのクリア</p>
	 * @param name inputDir
	 */
	public void clearInputDir() {
		inputDir.clear();
	}



	/**
	 * <p>スレッド数の取得</p>
	 * @return NUM_THREADS
	 */
	public int getThreads() {
		return NUM_THREADS;
	}

	/**
	 * <p>最小行の設定</p>
	 * @param NUM_THREADS
	 */
	public void setThreads(int NUM_THREADS) {
		this.NUM_THREADS = NUM_THREADS;
	}

	/**
	 * <p>最小行の取得</p>
	 * @return LINE_TH
	 */
	public int getMinLine() {
		return LINE_TH;
	}

	/**
	 * <p>最小行の設定</p>
	 * @param LINE_TH
	 */
	public void setMinLine(int LINE_TH) {
		this.LINE_TH = LINE_TH;
	}

	/**
	 * <p>ブロックサイズの取得</p>
	 * @return BLOCK_NODE_TH
	 */
	public int getBlockSize() {
		return BLOCK_NODE_TH;
	}

	/**
	 * <p>ブロックサイズの設定</p>
	 * @param BLOCK_NODE_TH
	 */
	public void setBlockSize(int BLOCK_NODE_TH) {
		this.BLOCK_NODE_TH = BLOCK_NODE_TH;
	}

	/**
	 * <p>サイズの取得</p>
	 * @return METHOD_NODE_TH
	 */
	public int getSize() {
		return METHOD_NODE_TH;
	}

	/**
	 * <p>サイズの設定</p>
	 * @param METHOD_NODE_TH
	 */
	public void setSize(int METHOD_NODE_TH) {
		this.METHOD_NODE_TH = METHOD_NODE_TH;
	}

	/**
	 * <p>類似度の取得</p>
	 * @return SIM_TH
	 */
	public double getSim() {
		return SIM_TH;
	}

	/**
	 * <p>類似度の設定</p>
	 * @param SIM_TH
	 */
	public void setSim(double SIM_TH) {
		this.SIM_TH = SIM_TH;
	}



	/**
	 * <p>言語の取得</p>
	 * @return 言語
	 */
	public int getLang() {
		return lang;
	}

	/**
	 * <p>言語の設定</p>
	 * @param lang 言語
	 */
	public void setLang(int lang) {
		this.lang = lang;
	}



	/*
	 * <p>前回のデータをつかうかどうか</p>
	 * @return <ul>
	 *           <li>使う場合 - true</li>
	 *           <li>使わない場合 - false</li>
	 *         </ul>
	 */
	public boolean getPreData() {
		return preData;
	}

	/**
	 * <p>preDataの設定</p>
	 * @param <ul>
	 *           <li>使う場合 - true</li>
	 *           <li>使わない場合 - false</li>
	 *         </ul>
	 */
	public void setPreData(boolean preData) {
		this.preData = preData;
	}





	/**
	 * <p>トークン閾値の取得</p>
	 * @return
	 */
	public int getTokenTh() {
		return tokenTh;
	}

	/**
	 * <p>トークン閾値の設定</p>
	 * @param tokenTh
	 */
	public void setTokenTh(int tokenTh) {
		this.tokenTh = tokenTh;
	}

	/**
	 * <p>該当ファイル名の SourceFile オブジェクト取得</p>
	 * @param fileList ファイルリスト
	 * @param name ファイル名
	 * @return SourceFile オブジェクト
	 */
	public static SourceFile getFileObj(ArrayList<SourceFile> fileList, String name) {
		for(SourceFile file: fileList) {
			if(file.getName().equals(name)) {
				return file;
			}
		}
		return null;
	}







	/**
	 * <p>該当ファイルIDの SourceFile オブジェクト取得</p>
	 * @param fileList ファイルリスト
	 * @param id ファイルID
	 * @return SourceFile オブジェクト
	 */
	public static SourceFile getFileObj(ArrayList<SourceFile> fileList, int id){
		for(SourceFile file: fileList){
			if(file.getId()==id)
				return file;
		}
		return null;
	}



	public void init_project() {

		fileList.clear();
		cloneSetList.clear();
	}


	public void setResultFile(String filepath) {
		switch(outputFormat) {
		case "txt":
			resultTXT = filepath + ".txt";
			System.out.println("========= txt");
			break;
		case "csv":
			resultCSV = filepath + ".csv";
			System.out.println("========= csv");
			break;
		case "notifier":
			resultNotifier = filepath;
			System.out.println("========= noti");
			break;
		case "html":
			resultHTML = filepath + ".html";
			System.out.println("========= html");
			break;
		case "cloneset":
			resultCloneSet = filepath;
			break;
		}

	}

	public String getResultTXT() {
		return resultCSV;
	}

	public String getResultCSV() {
		return resultCSV;
	}

	public String getResultHTML() {
		return resultHTML;
	}
	public String getResultNotifier() {
		return resultNotifier;
	}
	public String getResultCloneSet() {
		return resultCloneSet;
	}

	/*	private String resultTXT = null;
	private String resultCSV = null;
	private String resultHTML = null;
	private String resultNotifier = null;
	private String resultCloneSet = null;/
*/

}
