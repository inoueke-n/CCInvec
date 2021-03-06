package jp.ac.osaka_u.ist.sel.ccinvec;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import jp.ac.osaka_u.ist.sel.ccinvec.analyze.CAnalyzer4;
import jp.ac.osaka_u.ist.sel.ccinvec.analyze.CSharpAnalyzer;
import jp.ac.osaka_u.ist.sel.ccinvec.analyze.JavaAnalyzer3;
import jp.ac.osaka_u.ist.sel.ccinvec.control.ControlAllData;
import jp.ac.osaka_u.ist.sel.ccinvec.control.ControlGit;
import jp.ac.osaka_u.ist.sel.ccinvec.model.AllData;
import jp.ac.osaka_u.ist.sel.ccinvec.model.Block;
import jp.ac.osaka_u.ist.sel.ccinvec.model.ClonePair;
import jp.ac.osaka_u.ist.sel.ccinvec.model.CloneSet;
import jp.ac.osaka_u.ist.sel.ccinvec.model.SourceFile;
import jp.ac.osaka_u.ist.sel.ccinvec.trace.TraceManager;

public class CloneDetector {
	public static final String PARAM_FILE = "dataset.txt.params";
	public static final String DATASET_FILE = "dataset";
	public static final String PARTIAL_QUERY_POINT = "partialQueryPoint";
	public static final String LSH_FILE = "lsh_result.txt";
	public static final String LSH_LOG = "lsh_log.txt";
	public static final String BLOCKLIST_CSV = "blocklist.csv";
	public static final boolean enableBlockExtract = true;
	public static final boolean removeMethodPair = false;
	public static final boolean lda = false;
	public static final boolean absoluteTracking = true;

	public static final boolean modeDebug = false;
	public static final boolean modeStdout = true;
	//研究用のタイマー測定
	public static final boolean modeTimeMeasure = false;
	//研究用ソースファイルにdiffがあるコミットのみを出力したい場合はtrue
	public static final boolean modeEvalForOnlyDiffVer = false;

	public static boolean finalLoop =false;
	public static boolean modifiedSourceFile =false;
	public static boolean addedSourceFile =false;
	public static boolean deletedSourceFile =false;
	public static boolean idfRecalc =false;

	public static boolean allRecalc =true;


	public static String javaClassPath;
	//	public static String vecMethod;

	private static ArrayList<Block> blockList;
	private static ArrayList<Block> allBlockList;
	public static ArrayList<Block> testBlockList;
	//public static ArrayList<ClonePair> clonePairList;
	//	private static HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
	public static int countMethod, countBlock, countLine;
	private static final String version = "20.03.13";
	//public static int dimention_test;
	public static long start = System.currentTimeMillis();
	public static long subStart = start;
	public static long currentTime;
	public static long javaTime = start;
	public static long cTime = start;
	public static  long csharpTime = start;
	public static long resetTime = start;
	public static long vecTime = start;
	public static long lshTime = start;
	public static long mkcpTime = start;
	public static long cpTime = start;
	public static long csTime = start;
	public static long otTime = start;
	public static long serializeTime = start;
	public static long diffStart = 0;
	public static long diffEnd = 0;
	public static long diffTime = 0;

	/**
	 * <p>
	 * メイン
	 * <p>
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// ログファイル初期化
		try {
			Logger.init();
		} catch (IOException e) {
			Logger.printlnConsole("Can't generate log file.", Logger.ERROR);
			System.exit(1);
		}
		Def.CCINVEC_PATH = new File(".").getAbsoluteFile().getParent();
		//		System.out.println("CCVolti = " + Def.CCINVEC_PATH);

		if(modeStdout) {
			System.out.println("CCInvec " + version);
			System.out.println("----START----");
		}
		Config config = new Config();
		if(args.length == 1) {
			if(SettingFileLoader.loadSettingFile(args, config)) {
				makeFol(config);
				int maxNum = getMaxFileName(config);
				AllData allData = new AllData();
				if(config.getTargetGit()) {
					if(config.getPreData()) {
						//以前の検出の続きの場合
						int num = maxNum+1;
						config.setNewTarget(config.getNewDir());
						config.setOldTarget(config.getOldDir());
						allData =  ControlAllData.deserializeAllDataList(config);
//						System.out.println("alldata vec " + allData.getVecDimension());
//						System.out.println("alldata commit " + allData.getDetectingCommitId());
//						ControlAllData.synchronizeAllData(config, allData);
						for(int i =0; i < config.getInputCommitId().size(); i++) {

							String oldCommitId = null;
							String newCommitId = null;
							if(i == 0) {
								if(i == (config.getInputCommitId().size() -1)) {
									finalLoop = true;
								}
								oldCommitId = allData.getDetectingCommitId();
							}else {
								if(i == (config.getInputCommitId().size() -1)) {
									finalLoop = true;
								}
								oldCommitId = config.getInputCommitId().get(i-1);
							}
							newCommitId = config.getInputCommitId().get(i);
							ControlGit.checkout(config.getOldTarget(), oldCommitId, config);
							ControlGit.checkout(config.getNewTarget(), newCommitId, config);
							config.setResultFile(config.getOutputDir() + "\\" + config.getResultFileName() + num + "_" + newCommitId);
							allData.setDetectingCommitId(newCommitId);
							start = System.currentTimeMillis();

							allData = incrementalRun(config, i, allData);
							num++;
						}

					}else {
						int num = maxNum+1;
						config.setNewTarget(config.getNewDir());
						config.setOldTarget(config.getOldDir());
						for(int i =0; i < config.getInputCommitId().size(); i++) {
							if(i == 0) {
								if(i == (config.getInputCommitId().size() -1)) {
									finalLoop = true;
								}
								String newCommitId = config.getInputCommitId().get(i);
								ControlGit.checkout(config.getNewTarget(), newCommitId, config);
								config.setResultFile(config.getOutputDir() + "\\" + config.getResultFileName() + num + "_" + newCommitId);
								allData.setDetectingCommitId(newCommitId);
								start = System.currentTimeMillis();
								allData = firstRun(config);
								num++;
							}else {
								if(i == (config.getInputCommitId().size() -1)) {
									finalLoop = true;
								}
								String oldCommitId = config.getInputCommitId().get(i-1);
								String newCommitId = config.getInputCommitId().get(i);
								ControlGit.checkout(config.getOldTarget(), oldCommitId, config);
								ControlGit.checkout(config.getNewTarget(), newCommitId, config);
								config.setResultFile(config.getOutputDir() + "\\" + config.getResultFileName() + num + "_" + newCommitId);
								allData.setDetectingCommitId(newCommitId);
								start = System.currentTimeMillis();
//								ControlAllData.synchronizeAllData(config, allData);
								allData = incrementalRun(config, i, allData);
								num++;
							}
						}
					}
				}else if(config.getPreData()) {
					//前のデータがある場合
					int num = maxNum+1;
					allData =  ControlAllData.deserializeAllDataList(config);
//					ControlAllData.synchronizeAllData(config, allData);
					for(int i =0; i < config.getInputDir().size(); i++) {
						if(i==0) {
							if(i == (config.getInputDir().size() -1)) {
								finalLoop = true;
							}
							String folName = new File(config.getInputDir().get(0)).getName();
							config.setOldTarget(allData.getDetectingLocalPath());
							config.setNewTarget(config.getInputDir().get(i));
							config.setResultFile(config.getOutputDir() + "\\" + config.getResultFileName() + num + "_" + folName);
							allData = incrementalRun(config,i,allData);
							num++;
						}else {
							if(i == (config.getInputDir().size() -1)) {
								finalLoop = true;
							}
							String folName = new File(config.getInputDir().get(i)).getName();
							config.setOldTarget(config.getInputDir().get(i-1));
							config.setNewTarget(config.getInputDir().get(i));
							config.setResultFile(config.getOutputDir() + "\\" + config.getResultFileName() + num + "_" + folName);
//							ControlAllData.synchronizeAllData(config, allData);
							allData = incrementalRun(config,i, allData);
							num++;
						}
					}
				}else {
					//前のデータがない場合
					int num = maxNum+1;
					for(int i =0; i < config.getInputDir().size(); i++) {
						if(i == 0) {
							if(i == (config.getInputDir().size() -1)) {
								finalLoop = true;
							}
							String folName = new File(config.getInputDir().get(0)).getName();
							config.setNewTarget(config.getInputDir().get(0));
							config.setResultFile(config.getOutputDir() + "\\" + config.getResultFileName() + num + "_" + folName);
							allData.setDetectingLocalPath(config.getNewTarget());
							allData = firstRun(config);
							num++;
						}else {
							if(i == (config.getInputDir().size() -1)) {
								finalLoop = true;
							}
							String folName = new File(config.getInputDir().get(i)).getName();
							config.setOldTarget(config.getInputDir().get(i-1));
							config.setNewTarget(config.getInputDir().get(i));
							config.setResultFile(config.getOutputDir() + "\\" + config.getResultFileName() + num + "_" + folName);
//							ControlAllData.synchronizeAllData(config, allData);
							allData.setDetectingLocalPath(config.getNewTarget());
							allData = incrementalRun(config, i, allData);
							num++;
						}
					}

				}
			}
		}
		if(modeStdout) {
			System.out.println("Finished : ");
		}
	}

	private static void makeFol(Config config) {
		if (!(new File(config.getOutputDir())).exists()) {
			Logger.writeln("Create directory 'file'.", Logger.INFO);
			(new File(config.getOutputDir())).mkdirs();
		}


		if (!(new File(config.getDataDir())).exists()) {
			Logger.writeln("Create directory 'file'.", Logger.INFO);
			(new File(config.getDataDir())).mkdirs();
		}

	}

	/**
	 * <p>
	 * 最初の実行
	 * <p>
	 * @param args
	 * @throws Exception
	 */

	private static AllData firstRun(Config config) throws Exception {
		if(modeStdout) {
			System.out.println("=============================");
			System.out.println("-----First run algorithm-----");
		}
		// setJavaClassPath();
		getApplicationPath();
		//commandOption(args);



		//		System.out.println("Extract word in source code ...");
		countMethod = 0;
		countBlock = 0;
		countLine = 0;
		//ArrayList<SourceFile> fileList = new ArrayList<SourceFile>();
		//SourceFile file = new SourceFile();
		//ArrayList<String> fileListName = null;
		ArrayList<String> fileList = null;
		ArrayList<SourceFile> FileList = new ArrayList<SourceFile>();
		ArrayList<ClonePair> clonePairList = new ArrayList<ClonePair>();
		AllData allData = new AllData();
		//fileListの取得をちゃんとする
		switch (config.getLang()) {
		case 0: // "java"

			//			JavaAnalyzer3 javaanalyzer = new JavaAnalyzer3();
			//			fileList = JavaAnalyzer3.searchFiles(Config.target);
			//			blockList = javaanalyzer.analyze(fileList);
			long javaStart = System.currentTimeMillis();
			JavaAnalyzer3 javaanalyzer = new JavaAnalyzer3();
			//fileList = JavaAnalyzer3.searchFiles(Config.target);
			//blockList = javaanalyzer.analyze(fileList);
			FileList = JavaAnalyzer3.setFilesInfo(config.getNewTarget());
			blockList = javaanalyzer.analyzeFirst(FileList);
			long javaEnd = System.currentTimeMillis();
			javaTime = javaEnd-javaStart;
			//			System.out.println(
			//					"Parse file / All file = " + javaanalyzer.countParseFiles + " / " + javaanalyzer.countFiles);
			break;
		case 1: // "c"
			long cStart = System.currentTimeMillis();
			CAnalyzer4 canalyzer = new CAnalyzer4();
			FileList = CAnalyzer4.setFilesInfo(config.getNewTarget());
			blockList = canalyzer.analyzeFirst(FileList);
			long cEnd = System.currentTimeMillis();
			cTime = cEnd - cStart;
			break;
		case 2: // "c#"
			long csharpStart = System.currentTimeMillis();
			CSharpAnalyzer csharpAnalyzer = new CSharpAnalyzer();
			FileList = CSharpAnalyzer.setFilesInfo(config.getNewTarget());
			blockList = csharpAnalyzer.analyzeFirst(FileList);
			long csharpEnd = System.currentTimeMillis();
			csharpTime = csharpEnd - csharpStart;
			//			System.out.println(
			//					"Parse file / All file = " + csharpAnalyzer.countParseFiles + " / " + csharpAnalyzer.countFiles);
			break;
		}
		//		System.out.println("The number of methods : " + countMethod);
		//		System.out.println("The number of blocks (Excluding methods) : " + countBlock);
		//		System.out.println("The line : " + countLine);
		//		System.out.println("Extract word in source code done : " + (System.currentTimeMillis() - start) + "[ms]");
		//		System.out.println();

		if(modeStdout) {
			System.out.println("Extract word in source code done");
		}
		// 特徴ベクトル計算
		subStart = System.currentTimeMillis();

		VectorCalculator calculator = new VectorCalculator();
		blockList = calculator.filterMethod(blockList, config,allData);
		//		System.out.println("The threshold of size for method : " + config.getSize());
		//		System.out.println("The threshold of size for block : " + config.getBlockSize());
		//		System.out.println("The threshold of line of block : " + config.getMinLine());
		//		System.out.println("Filtered blocks / All blocks : " + blockList.size() + " / " + (countMethod + countBlock));
		//		System.out.println();
		//
		//		System.out.println("Calculate vector of each method ...");
		calculator.calculateVector(blockList, allData, config);

		/*		for(Block block : blockList) {
				System.out.println("l====enn =     " + block.getLen());
			}
		 */

		// System.out.println("wordmap.size = " + wordMap.size());
		currentTime = System.currentTimeMillis();
		if(modeStdout) {
			System.out.println("Generate clone pair done");
		}
		//		System.out.println(
		//				"Calculate vector done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
		vecTime = currentTime - subStart;

		if (Config.LSH_PRG != LSHController.NO_LSH) {
			// LSHクラスタリング
			//			System.out.println("Cluster vector of each method ...");
			subStart = System.currentTimeMillis();

			// LSHController.computeParam(wordMap.size());
			//			System.out.println("LSH start");
			LSHController lshCtlr = new LSHController();
			//dimention_test = calculator.getDimension();
			//			System.out.println("dimention = " + dimention_test);
			long lshStart  = System.currentTimeMillis();
			lshCtlr.execute(blockList, allData.getVecDimension(), Config.LSH_PRG, config);
			lshCtlr = null;
			long lshEnd  = System.currentTimeMillis();
			lshTime = lshEnd - lshStart;
			//			System.out.println("LSH done : " + (System.currentTimeMillis() - subStart) + "[ms]");
			long mkcpStart  = System.currentTimeMillis();
			CloneJudgement cloneJudge = new CloneJudgement();
			clonePairList = cloneJudge.getClonePairList(blockList, config);
			cloneJudge = null;
			long mkcpEnd  = System.currentTimeMillis();
			mkcpTime = mkcpEnd - mkcpStart;
		} else {
			CloneJudgement cloneJudge = new CloneJudgement();
			clonePairList = cloneJudge.getClonePairListNoLSH(blockList, config);
		}

		//		System.out.println("The number of clone pair : " + clonePairList.size());
		if (removeMethodPair)
			CloneJudgement.removePairOfMethod(clonePairList);

		currentTime = System.currentTimeMillis();
		cpTime = currentTime - subStart;
		if(modeStdout) {
			System.out.println("Clustering done");
		}
		//		System.out.println("Cluster done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		//		ArrayList<CloneSet> cloneSetList = null;
		ArrayList<CloneSet> cloneSetList = new ArrayList<CloneSet>();
		if (config.getResultNotifier() != null || config.getResultCloneSet() != null) {
			//			System.out.println("generate clone set start...");
			subStart = System.currentTimeMillis();
			//			for(Block block : blockList){
			//				System.out.println("clone A file " + block.getFileName() + "clonea startline " + block.getStartLine() + " end line " + block.getEndLine());
			//
			//			}
			cloneSetList = CloneJudgement.getCloneSetList(clonePairList, blockList);
			//			System.out.println("The number of clone set : " + cloneSetList.size());
			currentTime = System.currentTimeMillis();
			//			System.out.println(
			//					"generate clone set done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
			csTime = currentTime - subStart;
			if(modeStdout) {
				System.out.println("Generate clone set done");
			}
		}


		// ファイル出力
		//		System.out.println("Output start ...");
		subStart = System.currentTimeMillis();
		// ArrayList<CloneSet> cloneSetList =
		// LSHController.getCloneSetList(clonePairList);
		// Outputter.outputCloneSetCSVforCPP(cloneSetList);
		// Outputter.outputCSVforCPP(clonePairList);
		if (config.getResultCSV() != null) {
			//			System.out.println("=======output csv" + config.getResultCSV());
			Outputter.outputCSV(clonePairList, config);
		}
		if (config.getResultTXT() != null) {
			//			System.out.println("=======output txt" + config.getResultTXT());
			Outputter.outputTXT(clonePairList, config);
		}
		if (config.getResultHTML() != null) {
			//			System.out.println("=======output html" + config.getResultHTML());
			Outputter.outputHTML(clonePairList, config);
		}
		if (config.getResultNotifier() != null) {
			fileList = JavaAnalyzer3.searchFiles(config.getNewTarget());
			//			System.out.println("=======output notifier" + config.getResultNotifier());
			Outputter.outputNotifier(cloneSetList, fileList, config);
		}
		if (config.getResultCloneSet() != null) {
			//			System.out.println("=======output cloneset " + config.getResultCloneSet());
			Outputter.outputNotifier(cloneSetList, fileList, config);
			Outputter.outputCloneSetTXTforCPP(cloneSetList, config);
		}
		if(modeStdout) {
			System.out.println("Output result done");
			System.out.println("=============================");
		}
		// Outputter.outputForBigCloneEval(clonePairList);
		// Outputter.outputTXTforCPP(clonePairList);
		// Outputter.outputCSVforJava(clonePairList);
		// Outputter.outputTXTforJava(clonePairList);
		//Outputter.outputBlockList(blockList);
		// Outputter.outputStatisticsSample(clonePairList, 0.05, 1.96, 0.9);
		currentTime = System.currentTimeMillis();
		otTime = currentTime - subStart;
		//		System.out.println("Output done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
		// 評価
		// Comparator.compareMeCC(cloneSetList);
		// Evaluator2.evaluate();
		// Debug.debug();
		// Debug.outputResult02(cloneSetList);
		// Debug.outputResult(cloneSetList);
		//SourceFileListをシリアライズ化
		//BlockUpdater.serializeSourceFileList(FileList);
		//ClonePairListをシリアライズ化
		//BlockUpdater.serializeClonePairList(clonePairList);

		allData.setSourceFileList(FileList);
		allData.setClonePairList(clonePairList);
		allData.setBlockListOfCalcedVec(blockList);

		//		System.out.println("blockList size " + blockList.size());

		fileList = null;
		FileList = null;
		clonePairList = null;
		cloneSetList = null;
		blockList = null;
		//		int i =0;
		//		for(Block block : blockList) {
		//			if(block.getWordList() ==null) {
		//				System.out.println("aaa wordList null " + i);
		//			}
		//			i++;
		//		}
		//		System.out.println(lshTime + "," +mkcpTime);

		currentTime = System.currentTimeMillis();
		diffTime = diffEnd -diffStart;
		if(modeTimeMeasure) {
			System.out.println(currentTime - start + "[ms]");
		}

		if(config.getLang() == 0) {
			System.out.print(javaTime + ",");
		}else if(config.getLang() ==1) {
			System.out.print(cTime + ",");
		}else if(config.getLang() == 2){
			System.out.print(csharpTime + ",");
		}
		System.out.print(diffTime + "," +
				resetTime + "," +
				vecTime + "," +
				cpTime + "," +
				csTime + "," +
				otTime + "," +
				serializeTime + ",");

		if(config.getTargetGit()) {
			System.out.println(currentTime - start + "," + allData.getDetectingCommitId());
		}else {
			System.out.println(currentTime - start);
		}


		if(finalLoop) {
			ControlAllData.serializeAllDataList(allData,config);
//			allData.dataClear();
//			allData = null;
		}

		return allData;

	}

	/**
	 * <p>
	 * 2回目以降の実行
	 * <p>
	 * @param args, 旧バージョンのソースファイルオブジェクト
	 * @throws Exception
	 */

	private static AllData incrementalRun(Config config, int loopNum, AllData allData) throws Exception {

		if(modeStdout) {
			System.out.println("=============================");
			System.out.println("-----Incremental algorithm-----");
		}
		//		System.out.println("CCInvec " + version);
		//		System.out.println("Start Incremental Clone Detection fase");
		//		System.out.println("----BoW ver----");
		// setJavaClassPath();
		getApplicationPath();
		//commandOption(args);

		long start = System.currentTimeMillis();
		long subStart = start;
		long currentTime;
		long javaTime = start;
		long addTime = start;
		long analyzeTime = start;
		long cTime = start;
		long csharpTime = start;
		long resetTime = start;
		long vecTime = start;
		long cpTime = start;
		long csTime = start;
		long otTime = start;
		long serializeTime = start;


		//		System.out.println("Extract word in source code ...");
		countMethod = 0;
		countBlock = 0;
		countLine = 0;

		ArrayList<String> oldFileList = null;
		ArrayList<String> newFileList = null;
		ArrayList<Block> oldBlockList = new ArrayList<Block>();
		//updateBlockList 編集や削除されたブロックのリスト
		ArrayList<Block> updatedBlockList = new ArrayList<Block>();
		//		ArrayList<Block> needResetBlockList = new ArrayList<Block>();
		ArrayList<Block> addedModifiedBlockList = new ArrayList<Block>();
		//	ArrayList<Block> deletedBlockList = new ArrayList<Block>();
		//ArrayList<SourceFile> newFileList
		//fileListの取得をちゃんとする
		long synchroStart = System.currentTimeMillis();
		//AllData allData = new AllData();
		//	allData =  AllData.deserializeAllDataList(config);
		ArrayList<ClonePair> ClonePairList_test = new ArrayList<ClonePair>();
		//allData.synchronizeAllData();
		long synchroEnd = System.currentTimeMillis();
		long synchroTime = synchroEnd - synchroStart;

		ClonePairList_test = allData.getClonePairList();
		ArrayList<SourceFile> FileList = new ArrayList<SourceFile>();
		ArrayList<SourceFile> oldFileList_test = new ArrayList<SourceFile>();

		switch (config.getLang()) {
		case 0: // "java"
			//	JavaAnalyzer3 oldJavaanalyzer = new JavaAnalyzer3();

			long javaStart = System.currentTimeMillis();
			JavaAnalyzer3 newJavaanalyzer = new JavaAnalyzer3();
			//oldFileList = JavaAnalyzer3.searchFiles(Config.target2);
			/*ゆくゆくはなくしたいやつ*/
			long analyzeStart = System.currentTimeMillis();
			newFileList = JavaAnalyzer3.searchFiles(config.getNewTarget());
			oldFileList_test =  allData.getSourceFileList();
			//ここでdeletedに分類されたクローンをupdatedBlockListに格納
			FileList = BlockUpdater.updateSourceFileList(config.getNewTarget(), config.getOldTarget(), oldFileList_test, newFileList,updatedBlockList);
			//			newBlockList = newJavaanalyzer.incrementalAnalyze(FileList);
			newJavaanalyzer.incrementalAnalyze(FileList);
			long analyzeEnd = System.currentTimeMillis();
			analyzeTime = analyzeEnd - analyzeStart;
			//			System.out.println("new Block Size 1  = " + newBlockList.size());
			//新旧コードブロック間の対応をとる
			long addStart = System.currentTimeMillis();

			//			newBlockList.addAll(TraceManager.analyzeBlock(FileList, newBlockList, config, allData));
			//			TraceManager.analyzeBlock(FileList, newBlockList, config, allData);
			TraceManager.analyzeBlock(FileList, config, allData);
			//コードブロックのIDを再度割り振りなおす
			allBlockList = TraceManager.getAllBlock(FileList);
			//needResetBlockList.addAll(TraceManager.devideBlockCategory(newBlockList, 4));
			//modifiedされたもの
			updatedBlockList.addAll(TraceManager.devideBlockCategory(allBlockList, 4));
			long addEnd = System.currentTimeMillis();
			addTime = addEnd - addStart;
			//			addedModifiedBlockList = TraceManager.devideBlockCategory(allBlockList, 0);
			long javaEnd = System.currentTimeMillis();
			javaTime = javaEnd - javaStart;

			//ほしいのはdeletedなコード片，それ以外はblockListに入っている．

			break;
		case 1: // "c" "cpp"
			long cStart = System.currentTimeMillis();
			CAnalyzer4 Canalyzer = new CAnalyzer4();

			newFileList = CAnalyzer4.searchFiles(config.getNewTarget());
			oldFileList_test =  allData.getSourceFileList();
			FileList = BlockUpdater.updateSourceFileList(config.getNewTarget(), config.getOldTarget(), oldFileList_test, newFileList,updatedBlockList);
			Canalyzer.incrementalAnalyze(FileList);
			//			newBlockList.addAll(TraceManager.analyzeBlock(FileList, newBlockList, config, allData));
			TraceManager.analyzeBlock(FileList, config, allData);
			//コードブロックのIDを再度割り振りなおす
			allBlockList = TraceManager.getAllBlock(FileList);
			//とりあえず，modifiedとdeletedにするaddedはいれない，おなじクローンペアはfilteringで除去する
			updatedBlockList.addAll(TraceManager.devideBlockCategory(allBlockList, 4));
			long cEnd = System.currentTimeMillis();
			cTime = cEnd - cStart;
			break;
		case 2: // "c#"
			//			CSharpAnalyzer csharpAnalyzer = new CSharpAnalyzer();
			//			fileList = CSharpAnalyzer.searchFiles(Config.target);
			//			blockList = csharpAnalyzer.analyze(fileList);
			//			System.out.println(
			//					"Parse file / All file = " + csharpAnalyzer.countParseFiles + " / " + csharpAnalyzer.countFiles);


			long csharpStart = System.currentTimeMillis();
			CSharpAnalyzer csharpAnalyzer = new CSharpAnalyzer();
			newFileList = CSharpAnalyzer.searchFiles(config.getNewTarget());
			oldFileList_test =  allData.getSourceFileList();
			FileList = BlockUpdater.updateSourceFileList(config.getNewTarget(), config.getOldTarget(), oldFileList_test, newFileList,updatedBlockList);
			csharpAnalyzer.incrementalAnalyze(FileList);
			//新旧コードブロック間の対応をとる
			//			newBlockList.addAll(TraceManager.analyzeBlock(FileList, newBlockList, config, allData));
			TraceManager.analyzeBlock(FileList, config, allData);
			//コードブロックのIDを再度割り振りなおす
			allBlockList = TraceManager.getAllBlock(FileList);

			updatedBlockList.addAll(TraceManager.devideBlockCategory(allBlockList, 4));
			long csharpEnd = System.currentTimeMillis();
			csharpTime = csharpEnd - csharpStart;
			break;
		}
		if(modeStdout) {
			System.out.println("Extract word in source code done");
		}


		//		System.out.println("The number of methods : " + countMethod);
		//		System.out.println("The number of blocks (Excluding methods) : " + countBlock);
		//		System.out.println("The line : " + countLine);
		//		System.out.println("Extract word in source code done : " + (System.currentTimeMillis() - start) + "[ms]");
		//		System.out.println();

		if(modifiedSourceFile || addedSourceFile || deletedSourceFile) {
			allData.setPreDiff(true);

			long resetStart = System.currentTimeMillis();
			//編集，削除されたクローンペアの削除
			//needReserBlockListがnullならやらなくていい設定に
			BlockUpdater.resetClonePair(ClonePairList_test, updatedBlockList);
			long resetEnd = System.currentTimeMillis();
			resetTime = resetEnd - resetStart;
			//BlockUpdater.resetClonePair(ClonePairList_test, newBlockList);

			// 特徴ベクトル計算
			subStart = System.currentTimeMillis();

			VectorCalculator calculator = new VectorCalculator();
			//		blockList = calculator.filterMethod(blockList);
			//newBlockList = calculator.filterMethod(newBlockList);
			long vecStart = System.currentTimeMillis();
			allBlockList = calculator.increFilterMethod(allBlockList, config, allData);

			//			int s =0;
			//			for(Block block : allBlockList) {
			//				if(block.getVector() == null) {
			//					System.out.println("vec null " + s++);
			//					System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
			//					System.out.println("ID        "+ block.getId() );
			//					System.out.println("fileName  "+ block.getFileName());
			//					System.out.println("startLine "+ block.getStartLine());
			//					System.out.println("endLine   "+ block.getEndLine());
			//					System.out.println("category   "+ block.getCategoryString());
			//					System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
			//
			//				}
			//			}


			addedModifiedBlockList = TraceManager.devideBlockCategory(allBlockList, 0);
			//		System.out.println("The threshold of size for method : " + config.getSize());
			//		System.out.println("The threshold of size for block : " + config.getBlockSize());
			//		System.out.println("The threshold of line of block : " + config.getMinLine());
			//		System.out.println("Filtered blocks / All blocks : " + allBlockList.size() + " / " + (countMethod + countBlock));
			//		System.out.println();
			//
			//		System.out.println("Calculate vector of each method ...");
			if(modeStdout) {
				System.out.println("Caluculate vector done");
			}

			//			System.out.println("allblockList size " + allBlockList.size());

			if(addedModifiedBlockList.size() > 0 && allBlockList.size() > 0) {
				int numMethod=0;
				if(config.getVecMethod()==Config.BoW) {
					allBlockList = calculator.increCalculateVectorForBoW(allBlockList, addedModifiedBlockList, allData,config.getVecMethod());
				}else if(config.getVecMethod()==Config.TFIDF) {
					allBlockList = calculator.increCalculateVectorForTfIdf(allBlockList, addedModifiedBlockList, allData,config.getVecMethod(), FileList, config);
				}



				//				allBlockList = calculator.calculateVector_test(allBlockList, addedModifiedBlockList, allData);
				// System.out.println("wordmap.size = " + wordMap.size());
				long vecEnd = System.currentTimeMillis();
				vecTime = vecEnd - vecStart;
				currentTime = System.currentTimeMillis();
				//		System.out.println(
				//				"Calculate vector done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

				long cpStart = System.currentTimeMillis();
				if (Config.LSH_PRG != LSHController.NO_LSH) {
					// LSHクラスタリング
					//			System.out.println("Cluster vector of each method ...");
					subStart = System.currentTimeMillis();

					// LSHController.computeParam(wordMap.size());
					//			System.out.println("LSH start");
					LSHController lshCtlr = new LSHController();
					lshCtlr.executePartially(allBlockList,addedModifiedBlockList, allData.getVecDimension(), Config.LSH_PRG,config);
					//			System.out.println("dimention = " + dimention_test);
					//			System.out.println("calculator.getDimension() = " + calculator.getDimension());
					//	lshCtlr.executePartially(allBlockList,addedModifiedBlockList, dimention_test, Config.LSH_PRG);
					lshCtlr = null;
					//			System.out.println("LSH done : " + (System.currentTimeMillis() - subStart) + "[ms]");
					CloneJudgement cloneJudge = new CloneJudgement();
					//	ArrayList<ClonePair> addedClonePair = new ArrayList<ClonePair>();
					//addedClonePair = cloneJudge.getClonePairListPartially(allBlockList, addedModifiedBlockList, config);
					//cloneJudge,insertClonePairToList(ClonePairList_test, addedClonePair);
					//					int i =0;
					//					for(Block block : addedModifiedBlockList) {
					//						if(block.getFileName().contains("JsonSerializerInternalReader.cs")) {
					//							System.out.println("++++++++++++++++++++++++++++++++");
					//							System.out.println("ID        "+ block.getId() );
					//							System.out.println("fileName  "+ block.getFileName());
					//							System.out.println("startLine "+ block.getStartLine());
					//							System.out.println("endLine   "+ block.getEndLine());
					//							System.out.println("++++++++++++++++++++++++++++++++");
					//						}
					//						if(block.getVector() == null) {
					//							System.out.println("vec null " + i);
					//							System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
					//							System.out.println("ID        "+ block.getId() );
					//							System.out.println("fileName  "+ block.getFileName());
					//							System.out.println("startLine "+ block.getStartLine());
					//							System.out.println("endLine   "+ block.getEndLine());
					//							System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
					//
					//						}
					//						i++;
					//					}
					//					int j =0;
					//					for(Block block : allBlockList) {
					//						if(block.getFileName().contains("JsonSerializerInternalReader.cs")) {
					//							System.out.println("====================================");
					//							System.out.println("ID        "+ block.getId() );
					//							System.out.println("fileName  "+ block.getFileName());
					//							System.out.println("startLine "+ block.getStartLine());
					//							System.out.println("endLine   "+ block.getEndLine());
					//							System.out.println("====================================");
					//						}
					//						if(block.getVector() == null) {
					//							System.out.println("vec null " + j);
					//							System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
					//							System.out.println("ID        "+ block.getId() );
					//							System.out.println("fileName  "+ block.getFileName());
					//							System.out.println("startLine "+ block.getStartLine());
					//							System.out.println("endLine   "+ block.getEndLine());
					//							System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
					//						}
					//						j++;
					//					}
					ClonePairList_test = cloneJudge.getClonePairListPartially(allBlockList, addedModifiedBlockList, ClonePairList_test,config);
//					ClonePairList_test.addAll(cloneJudge.getClonePairListPartially(allBlockList, addedModifiedBlockList, ClonePairList_test,config));
					cloneJudge.sortClonePair(ClonePairList_test);
					cloneJudge.deleteDupulicatePair(ClonePairList_test);
					cloneJudge = null;
				} else {
					CloneJudgement cloneJudge = new CloneJudgement();
					//clonePairList = cloneJudge.getClonePairListNoLSH(newBlockList);
				}
				long cpEnd = System.currentTimeMillis();
				cpTime = cpEnd - cpStart;
				//		System.out.println("The number of clone pair : " + ClonePairList_test.size());
				long csStart= System.currentTimeMillis();
				if (removeMethodPair)
					CloneJudgement.removePairOfMethod(ClonePairList_test);
				if(modeStdout) {
					System.out.println("Generate clone pair done");
				}
				currentTime = System.currentTimeMillis();
				//		System.out.println("Cluster done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
			}
		}else {
			allData.setPreDiff(false);
			allBlockList = TraceManager.devideBlockCategory(allBlockList, 5);

		}
		//ArrayList<CloneSet> cloneSetList = null;
		//		for (ClonePair clonePair : ClonePairList_test) {
		//			System.out.println("cloneA.getId() " + clonePair.cloneA.getId());
		//			if(clonePair.cloneA.getStartLine() == 716) {
		//				System.out.println("716 ID = "+  clonePair.cloneA.getId());
		//				System.out.println("717 ID = "+  clonePair.cloneB.getId());
		//				System.out.println("sarrt lne = "+  clonePair.cloneB.getStartLine());
		//
		//			}
		//
		//			//System.out.println("cloneB.getId() " + clonePair.cloneB.getId());
		//			/*int aU = blockList.indexOf(clonePair.cloneA);
		//			int bU = blockList.indexOf(clonePair.cloneB);
		//			if((aU != -1) || (bU != -1)) {
		//				System.out.println("noting  ");
		//				System.out.println("============= = ");
		//				System.out.println("clone A = " + clonePair.cloneA.getId());
		//				System.out.println("clone B = " + clonePair.cloneB.getId());
		//				System.out.println("============= = ");
		//
		//			}*/
		//		}
		long csStart= System.currentTimeMillis();
		ArrayList<CloneSet> cloneSetList_test = new ArrayList<CloneSet>();
		if (config.getResultNotifier() != null || config.getResultCloneSet() != null) {
			//			System.out.println("generate clone set start...");
			subStart = System.currentTimeMillis();

			/*for(Block block : allBlockList){
				System.out.println("clone A file " + block.getFileName() + "clonea startline " + block.getStartLine() + " end line " + block.getEndLine());

			}*/
			cloneSetList_test = CloneJudgement.getCloneSetList(ClonePairList_test, allBlockList);
			//			System.out.println("The number of clone set : " + cloneSetList_test.size());
			currentTime = System.currentTimeMillis();
			//			System.out.println(
			//					"generate clone set done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
			if(modeStdout) {
				System.out.println("Generate clone set done");
			}

		}
		long csEnd= System.currentTimeMillis();
		csTime = csEnd - csStart;


		// ファイル出力
		//		System.out.println("Output start ...");
		subStart = System.currentTimeMillis();
		// ArrayList<CloneSet> cloneSetList =
		// LSHController.getCloneSetList(clonePairList);
		// Outputter.outputCloneSetCSVforCPP(cloneSetList);
		// Outputter.outputCSVforCPP(clonePairList);

		long otStart= System.currentTimeMillis();
		if(modeEvalForOnlyDiffVer) {
			if(modifiedSourceFile || addedSourceFile || deletedSourceFile) {
				if (config.getResultCSV() != null)
					Outputter.outputCSV(ClonePairList_test, config);
				if (config.getResultTXT() != null)
					Outputter.outputTXT(ClonePairList_test, config);
				if (config.getResultHTML() != null)
					Outputter.outputHTML(ClonePairList_test, config);
				if (config.getResultNotifier() != null)
					Outputter.outputNotifier(cloneSetList_test, newFileList, config);
				if (config.getResultCloneSet() != null)
					Outputter.outputCloneSetTXTforCPP(cloneSetList_test, config);
			}
		}else {
			if (config.getResultCSV() != null)
				Outputter.outputCSV(ClonePairList_test, config);
			if (config.getResultTXT() != null)
				Outputter.outputTXT(ClonePairList_test, config);
			if (config.getResultHTML() != null)
				Outputter.outputHTML(ClonePairList_test, config);
			if (config.getResultNotifier() != null)
				Outputter.outputNotifier(cloneSetList_test, newFileList, config);
			if (config.getResultCloneSet() != null)
				Outputter.outputCloneSetTXTforCPP(cloneSetList_test, config);
		}
		if(modeStdout) {
			System.out.println("Output result done");
			System.out.println("=============================");
		}
		long otEnd= System.currentTimeMillis();
		otTime = otEnd - otStart;
		// Outputter.outputForBigCloneEval(clonePairList);
		// Outputter.outputTXTforCPP(clonePairList);
		// Outputter.outputCSVforJava(clonePairList);
		// Outputter.outputTXTforJava(clonePairList);
		//Outputter.outputBlockList(blockList);
		// Outputter.outputStatisticsSample(clonePairList, 0.05, 1.96, 0.9);
		//		currentTime = System.currentTimeMillis();
		//		System.out.println("Output done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
		// 評価
		// Comparator.compareMeCC(cloneSetList);
		// Evaluator2.evaluate();
		// Debug.debug();
		// Debug.outputResult02(cloneSetList);
		// Debug.outputResult(cloneSetList);

		//		FileWriter sw = new FileWriter("test2.txt");
		//		for(ClonePair cp : ClonePairList_test) {
		//			sw.write("============= = \r\n");
		//			sw.write("clone A = " + cp.cloneA.getId() + "\r\n");
		//			sw.write("clone B = " + cp.cloneB.getId() +  "\r\n");
		//			sw.write("clone A fileName = " + cp.cloneA.getFileName() + "\r\n");
		//			sw.write("clone B fileName = " + cp.cloneB.getFileName() + "\r\n");
		//			sw.write("clone A startLine =" + cp.cloneA.getStartLine() + "endline = " + cp.cloneA.getEndLine() + "\r\n");
		//			sw.write("clone B startLine =" + cp.cloneB.getStartLine() + "endline = " + cp.cloneB.getEndLine() + "\r\n");
		//			sw.write("============= =  \\r\\n");
		//
		//		}
		//		sw.close();

		long serializeStart = System.currentTimeMillis();
		allData.setSourceFileList(FileList);
		allData.setClonePairList(ClonePairList_test);
		allData.setBlockListOfCalcedVec(allBlockList);
		//allData.synchronizeAllData2();

		long serializeEnd = System.currentTimeMillis();
		serializeTime = serializeEnd - serializeStart;

		oldFileList = null;
		newFileList = null;
		oldBlockList = null;
		updatedBlockList = null;
		addedModifiedBlockList = null;
		//				allBlockList = null;
		//				ClonePairList_test = null;
		cloneSetList_test = null;
		System.gc();

		currentTime = System.currentTimeMillis();
		//		if(modeTimeMeasure) {
		//			//			System.out.println("Synchro Data time = "+ synchroTime + "[ms]");
		//			//			System.out.println("java         time = "+ javaTime + "[ms]");
		//			//			System.out.println("reset        time = "+ resetTime + "[ms]");
		//			//			System.out.println("vec          time = "+ vecTime + "[ms]");
		//			//			System.out.println("cp           time = "+ cpTime + "[ms]");
		//			//			System.out.println("cs           time = "+ csTime + "[ms]");
		//			//			System.out.println("ot           time = "+ otTime + "[ms]");
		//			//			System.out.println("serializ     time = "+ serializeTime + "[ms]");
		//			//			System.out.println("All          time = " + (currentTime - start) + "[ms]");
		//			if(config.getLang() == 0) {
		//				System.out.print(javaTime + ",");
		//			}else if(config.getLang() ==1) {
		//				System.out.print(cTime + ",");
		//			}else if(config.getLang() == 2){
		//				System.out.print(csharpTime + ",");
		//			}
		//			System.out.print(    resetTime + "," +
		//					vecTime + "," +
		//					cpTime + "," +
		//					csTime + "," +
		//					otTime + "," +
		//					serializeTime + ",");
		//			if(config.getTargetGit()) {
		//				System.out.println(currentTime - start + "," + allData.getDetectingCommitId());
		//			}else {
		//				System.out.println(currentTime - start);
		//			}
		//		}else
		//		System.out.println(analyzeTime +  "," + addTime);
		diffTime = diffEnd -diffStart;
		if(modeEvalForOnlyDiffVer) {
			if(modifiedSourceFile || addedSourceFile || deletedSourceFile) {
				if(config.getLang() == 0) {
					System.out.print(javaTime + ",");
				}else if(config.getLang() ==1) {
					System.out.print(cTime + ",");
				}else if(config.getLang() == 2){
					System.out.print(csharpTime + ",");
				}
				System.out.print(diffTime + "," +
						resetTime + "," +
						vecTime + "," +
						cpTime + "," +
						csTime + "," +
						otTime + "," +
						serializeTime + ",");
				if(config.getTargetGit()) {
					System.out.print(currentTime - start + "," + allData.getDetectingCommitId());
				}else {
					System.out.print(currentTime - start);
				}
				if(idfRecalc) {
					System.out.print(",Recalc");
				}
				System.out.println();
			}
		}else if(config.getTargetGit()) {
			System.out.println(currentTime - start + "," + allData.getDetectingCommitId());
		}else {
			System.out.println(currentTime - start);
		}


		if(finalLoop) {
			ControlAllData.serializeAllDataList(allData,config);
			ControlAllData.dataClear(allData);
			allData = null;
		}



		return allData;

	}


	private static void getApplicationPath() throws URISyntaxException {
		ProtectionDomain pd = CloneDetector.class.getProtectionDomain();
		CodeSource cs = pd.getCodeSource();
		URL location = cs.getLocation();
		URI uri = location.toURI();
		Path path = Paths.get(uri);
		javaClassPath = path.getParent().toString();
	}

	private static void setJavaClassPath() {
		Path path = null;
		try {
			path = Paths.get(CloneDetector.class.getClassLoader().getResource("").toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		if (path.endsWith("bin"))
			path = path.getParent();
		javaClassPath = path.toString();
	}


	/**
	 * ファイル名に使われている最大の数字を取得
	 * @param config
	 * @return maxNum
	 */
	private static int getMaxFileName(Config config) {
		File dir = new File(config.getOutputDir());
		//listFilesメソッドを使用して一覧を取得する
		File[] list = dir.listFiles();
		int maxNum=0;
		for(int i=0; i<list.length; i++) {
			if(list[i].isFile()) {
				if(list[i].getName().contains(config.getResultFileName())) {
					String fileNumStr;
					if(config.getOutputFormat().equals("notifier") || config.getOutputFormat().equals("cloneset")) {
						fileNumStr = list[i].getName().substring(config.getResultFileName().length());
					}else if(list[i].getName().contains(".")){
						if(config.getTargetGit()) {
							fileNumStr = list[i].getName().substring(config.getResultFileName().length(), list[i].getName().lastIndexOf("_"));
						}else {
							fileNumStr = list[i].getName().substring(config.getResultFileName().length(), list[i].getName().lastIndexOf("_"));
						}
					}else {
						fileNumStr = "0";
					}
					boolean isNumber =true;
					for(int j =0; j<fileNumStr.length(); j++) {
						if(!Character.isDigit(fileNumStr.charAt(j))) {
							isNumber = false;
						}
					}
					if(isNumber) {
						int fileNum = Integer.parseInt(fileNumStr);
						//						System.out.println("aa = " + fileNum);
						if(maxNum < fileNum) {
							maxNum = fileNum;
						}
					}
				}
			}
		}
		return maxNum;
	}

	/*	private static void commandOption(String[] args) {
		Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help").desc("display help").build());
		options.addOption(Option.builder("d").longOpt("dir").desc("select directory for clone detection").hasArg()
				.argName("dirname").build());
		options.addOption(Option.builder("d2").longOpt("dir2").desc("select directory for clone detection").hasArg()
				.argName("dirname2").build());
		options.addOption(Option.builder("l").longOpt("lang")
				.desc("select language from following ( default: java )\r\n  * java\r\n  * c\\r\\n  * csharp").hasArg()
				.argName("lang")
				.build());
		// options.addOption(Option.builder("p").longOpt("param").desc("select
		// parameter file for LSH
		// execution").hasArg().argName("*.param").build());
		options.addOption(Option.builder("oc").longOpt("outputcsv")
				.desc("select csv file name for output").hasArg().argName("*.csv").build());
		options.addOption(Option.builder("ot").longOpt("outputtxt")
				.desc("select text file name for output").hasArg().argName("*.txt").build());
		options.addOption(Option.builder("oh").longOpt("outputhtml").desc("select html file name for output").hasArg()
				.argName("*.html").build());
		options.addOption(Option.builder("on").longOpt("outputnotifier").desc("select notifier file name for output")
				.hasArg().argName("filename").build());
		options.addOption(Option.builder("ocs").longOpt("outputcloneset")
				.desc("select text file name for output clone set").hasArg().argName("filename").build());

		options.addOption(Option.builder().longOpt("sim")
				.desc("set threshold of similarity for clone detection ( 0.0<=sim<=1.0 ) ( default: 0.9 )").hasArg()
				.argName("value").build());
		options.addOption(
				Option.builder().longOpt("size").desc("set threshold of size for method  ( 0<=size ) ( default: 50 )")
				.hasArg().argName("value").build());
		options.addOption(
				Option.builder().longOpt("sizeb")
				.desc("set threshold of size for block  ( 0<=size ) ( default: sizeb = size )")
				.hasArg().argName("value").build());
		options.addOption(Option.builder("mil").longOpt("min-lines")
				.desc("set threshold of line of block ( 0<=mil ) ( default: 0 )").hasArg().argName("value").build());
		options.addOption(Option.builder("cs").longOpt("charset")
				.desc("set the name of character encoding ( default: UTF-8 )").hasArg().argName("charset").build());
		options.addOption(Option.builder("t").longOpt("threads")
				.desc("set the number of threads. 0 indicates max threads. ( default: 1 )").hasArg().argName("value")
				.build());



		CommandLine cl = null;
		try {
			CommandLineParser parser = new DefaultParser();
			cl = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error: can't read options.");
			System.exit(1);
		}
		if (cl.hasOption("help") || args.length == 0) { HelpFormatter f = new HelpFormatter(); f.printHelp("-d [dirname] -l [lang] <*options>", options); System.exit(0); } if (cl.hasOption("dir")) Config.target = cl.getOptionValue("dir"); if (Config.target == null) { System.err.println("Usage Error: please select target directory for clone detection."); System.exit(1); } if (cl.hasOption("dir2"))
			Config.target2 = cl.getOptionValue("dir2");
		System.out.println("args = " + cl.getOptionValue("dir2"));
		if (Config.target2 == null) {
			System.err.println("Usage Error: please select target directory for clone detection.");
			System.exit(1);
		}
		if (cl.hasOption("lang")) {
			if (cl.getOptionValue("lang").toLowerCase().equals("java"))
				Config.lang = 0;
			if (cl.getOptionValue("lang").toLowerCase().equals("c"))
				Config.lang = 1;
			if (cl.getOptionValue("lang").toLowerCase().equals("csharp"))
				Config.lang = 2;

		}

		// if(cl.hasOption("param")) { Config.paramFile =
		 // cl.getOptionValue("param"); Config.paramFlg = false; }

		if (cl.hasOption("outputcsv"))
			Config.resultCSV = cl.getOptionValue("outputcsv");
		if (cl.hasOption("outputtxt"))
			Config.resultTXT = cl.getOptionValue("outputtxt");
		if (cl.hasOption("outputhtml"))
			Config.resultHTML = cl.getOptionValue("outputhtml");
		if (cl.hasOption("outputnotifier"))
			Config.resultNotifier = cl.getOptionValue("outputnotifier");
		if (cl.hasOption("outputcloneset"))
			Config.resultCloneSet = cl.getOptionValue("outputcloneset");
		if (Config.resultCSV == null && Config.resultTXT == null && Config.resultHTML == null
				&& Config.resultNotifier == null && Config.resultCloneSet == null) {
			System.err.println("Usage Error: please set output file name. (-oc, -ot, -oh, -on, -ocs)");
			System.exit(1);
		}

		if (cl.hasOption("sim"))
			try {
				Config.SIM_TH = Double.parseDouble(cl.getOptionValue("sim"));
				if (0.0D > Config.SIM_TH || 1.0D < Config.SIM_TH)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set similarity threshold.");
				System.exit(1);
			}
		if (cl.hasOption("size"))
			try {
				Config.METHOD_NODE_TH = Integer.parseInt(cl.getOptionValue("size"));
				if (Config.METHOD_NODE_TH < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set method size threshold.");
				System.exit(1);
			}
		if (cl.hasOption("sizeb")) {
			try {
				Config.BLOCK_NODE_TH = Integer.parseInt(cl.getOptionValue("sizeb"));
				if (Config.BLOCK_NODE_TH < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set block size threshold.");
				System.exit(1);
			}
		} else {
			Config.BLOCK_NODE_TH = Config.METHOD_NODE_TH;
		}
		if (cl.hasOption("min-lines"))
			try {
				Config.LINE_TH = Integer.parseInt(cl.getOptionValue("min-lines"));
				if (Config.LINE_TH < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set min line threshold.");
				System.exit(1);
			}
		if (cl.hasOption("charset"))
			Config.charset = cl.getOptionValue("charset");
		if (cl.hasOption("threads")) {
			try {
				Config.NUM_THREADS = Integer.parseInt(cl.getOptionValue("threads"));
				if (Config.NUM_THREADS < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set num threads to negative number.");
				System.exit(1);
			}
		} else
			Config.NUM_THREADS = 1;
	}
	 */


}