package jp.ac.osaka_u.ist.sel.icvolti;

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
import java.util.HashMap;

import jp.ac.osaka_u.ist.sel.icvolti.analyze.CAnalyzer4;
import jp.ac.osaka_u.ist.sel.icvolti.analyze.CSharpAnalyzer;
import jp.ac.osaka_u.ist.sel.icvolti.analyze.JavaAnalyzer3;
import jp.ac.osaka_u.ist.sel.icvolti.model.AllData;
import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
import jp.ac.osaka_u.ist.sel.icvolti.model.ClonePair;
import jp.ac.osaka_u.ist.sel.icvolti.model.CloneSet;
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;
import jp.ac.osaka_u.ist.sel.icvolti.trace.TraceManager;

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

	public static String javaClassPath;

	private static ArrayList<Block> blockList;
	public static ArrayList<Block> testBlockList;
	//public static ArrayList<ClonePair> clonePairList;
	private static HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
	public static int countMethod, countBlock, countLine;
	private static final String version = "19.01.24";
	public static int dimention_test;

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
		Def.CCVOLTI_PATH = new File(".").getAbsoluteFile().getParent();
		System.out.println("CCVolti = " + Def.CCVOLTI_PATH);


		Config config = new Config();
		if(args.length == 1) {
			if(SettingFileLoader.loadSettingFile(args, config)) {
				if(config.getPreData()) {
					//前のデータがある場合
					for(int i =0; i < config.getInputDir().size(); i++) {

					}
				}else {
					for(int i =0; i < config.getInputDir().size(); i++) {
						System.out.println("str = " + config.getInputDir().get(i));

					}
					//前のデータがない場合
					for(int i =0; i < config.getInputDir().size(); i++) {
						int num = i+1;
						if(i == 0) {
							config.setNewTarget(config.getInputDir().get(0));
							config.setResultFile(config.getOutputDir() + "\\result" + num);
							firstRun(config);
						}else {
							System.out.println("config size = " + config.getInputDir().size());
							System.out.println("config i = " + config.getInputDir().get(i));
							config.setOldTarget(config.getInputDir().get(i-1));
							config.setNewTarget(config.getInputDir().get(i));
							config.setResultFile(config.getOutputDir() + "\\result" + num);
							incrementalRun(config);
						}
					}

				}


			}
		}

	}
	/**
	 * <p>
	 * 最初の実行
	 * <p>
	 * @param args
	 * @throws Exception
	 */

	private static void firstRun(Config config) throws Exception {
		System.out.println("ICVolti " + version);
		System.out.println("AAAAAAAAAAAAAAAAAA");
		System.out.println("----BoW ver----");
		// setJavaClassPath();
		getApplicationPath();
		//commandOption(args);

		long start = System.currentTimeMillis();
		long subStart = start;
		long currentTime;

		System.out.println("Extract word in source code ...");
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
			JavaAnalyzer3 javaanalyzer = new JavaAnalyzer3();
			//fileList = JavaAnalyzer3.searchFiles(Config.target);
			//		fileList = JavaAnalyzer3.setFilesInfo(Config.target);
			//blockList = javaanalyzer.analyze(fileList);
			FileList = JavaAnalyzer3.setFilesInfo(config.getNewTarget());
			blockList = javaanalyzer.analyze_test_first(FileList);
			System.out.println(
					"Parse file / All file = " + javaanalyzer.countParseFiles + " / " + javaanalyzer.countFiles);
			break;
		case 1: // "c"
			CAnalyzer4 canalyzer = new CAnalyzer4();
			fileList = canalyzer.searchFiles(config.getNewTarget());
			FileList = JavaAnalyzer3.setFilesInfo(config.getNewTarget());
			blockList = canalyzer.analyze(fileList);
			break;
		case 2: // "c#"
			CSharpAnalyzer csharpAnalyzer = new CSharpAnalyzer();
			fileList = CSharpAnalyzer.searchFiles(config.getNewTarget());
			FileList= JavaAnalyzer3.setFilesInfo(config.getNewTarget());
			blockList = csharpAnalyzer.analyze(fileList);
			System.out.println(
					"Parse file / All file = " + csharpAnalyzer.countParseFiles + " / " + csharpAnalyzer.countFiles);
			break;
		}
		System.out.println("The number of methods : " + countMethod);
		System.out.println("The number of blocks (Excluding methods) : " + countBlock);
		System.out.println("The line : " + countLine);
		System.out.println("Extract word in source code done : " + (System.currentTimeMillis() - start) + "[ms]");
		System.out.println();

		// 特徴ベクトル計算
		subStart = System.currentTimeMillis();

		VectorCalculator calculator = new VectorCalculator();
		blockList = calculator.filterMethod(blockList, config);
		System.out.println("The threshold of size for method : " + config.getSize());
		System.out.println("The threshold of size for block : " + config.getBlockSize());
		System.out.println("The threshold of line of block : " + config.getMinLine());
		System.out.println("Filtered blocks / All blocks : " + blockList.size() + " / " + (countMethod + countBlock));
		System.out.println();

		System.out.println("Calculate vector of each method ...");
		calculator.calculateVector(blockList, allData);

		/*		for(Block block : blockList) {
				System.out.println("l====enn =     " + block.getLen());
			}
		 */

		// System.out.println("wordmap.size = " + wordMap.size());
		currentTime = System.currentTimeMillis();
		System.out.println(
				"Calculate vector done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		if (Config.LSH_PRG != LSHController.NO_LSH) {
			// LSHクラスタリング
			System.out.println("Cluster vector of each method ...");
			subStart = System.currentTimeMillis();

			// LSHController.computeParam(wordMap.size());
			System.out.println("LSH start");
			LSHController lshCtlr = new LSHController();
			dimention_test = calculator.getDimention();
			System.out.println("dimention = " + dimention_test);
			lshCtlr.execute(blockList, calculator.getDimention(), Config.LSH_PRG, config);
			lshCtlr = null;
			System.out.println("LSH done : " + (System.currentTimeMillis() - subStart) + "[ms]");
			CloneJudgement cloneJudge = new CloneJudgement();
			clonePairList = cloneJudge.getClonePairList(blockList, config);
			cloneJudge = null;
		} else {
			CloneJudgement cloneJudge = new CloneJudgement();
			clonePairList = cloneJudge.getClonePairListNoLSH(blockList, config);
		}

		System.out.println("The number of clone pair : " + clonePairList.size());
		if (removeMethodPair)
			CloneJudgement.removePairOfMethod(clonePairList);

		currentTime = System.currentTimeMillis();
		System.out.println("Cluster done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		ArrayList<CloneSet> cloneSetList = null;
		if (config.getResultNotifier() != null || config.getResultCloneSet() != null) {
			System.out.println("generate clone set start...");
			subStart = System.currentTimeMillis();
			cloneSetList = CloneJudgement.getCloneSetList(clonePairList, blockList);
			System.out.println("The number of clone set : " + cloneSetList.size());
			currentTime = System.currentTimeMillis();
			System.out.println(
					"generate clone set done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		}

		// ファイル出力
		System.out.println("Output start ...");
		subStart = System.currentTimeMillis();
		// ArrayList<CloneSet> cloneSetList =
		// LSHController.getCloneSetList(clonePairList);
		// Outputter.outputCloneSetCSVforCPP(cloneSetList);
		// Outputter.outputCSVforCPP(clonePairList);
		if (config.getResultCSV() != null) {
			System.out.println("=======output csv" + config.getResultCSV());
			Outputter.outputCSV(clonePairList, config);
		}
		if (config.getResultTXT() != null) {
			System.out.println("=======output txt" + config.getResultTXT());
			Outputter.outputTXT(clonePairList, config);
		}
		if (config.getResultHTML() != null) {
			System.out.println("=======output html" + config.getResultHTML());
			Outputter.outputHTML(clonePairList, config);
		}
		if (config.getResultNotifier() != null) {
			System.out.println("=======output notifier" + config.getResultNotifier());
			Outputter.outputNotifier(cloneSetList, fileList, config);
		}
		if (config.getResultCloneSet() != null) {
			System.out.println("=======output cloneset " + config.getResultCloneSet());
			Outputter.outputNotifier(cloneSetList, fileList, config);
			Outputter.outputCloneSetTXTforCPP(cloneSetList, config);
		}
		// Outputter.outputForBigCloneEval(clonePairList);
		// Outputter.outputTXTforCPP(clonePairList);
		// Outputter.outputCSVforJava(clonePairList);
		// Outputter.outputTXTforJava(clonePairList);
		//Outputter.outputBlockList(blockList);
		// Outputter.outputStatisticsSample(clonePairList, 0.05, 1.96, 0.9);
		currentTime = System.currentTimeMillis();
		System.out.println("Output done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
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

		for(Block block : blockList) {
			System.out.println("Id  " + block.getId() );
			System.out.println("Id  " + block.getId() );

		}
		for(ClonePair cp : clonePairList) {
			int idA = cp.cloneA.getId();
			int idB = cp.cloneB.getId();
			System.out.println("aidA = " + idA);
			System.out.println("aidB = " + idB);
		}
		allData.setSourceFileList(FileList);
		allData.setClonePairList(clonePairList);
		allData.setBlockListOfCalcedVec(blockList);
		AllData.serializeAllDataList(allData);
		allData = null;
		fileList = null;
		FileList = null;
		clonePairList = null;
		/*for(SourceFile file : FileList) {
			for(Block block : file.getNewBlockList()) {
				System.out.println("l====enn =     " + block.getLen());
			}

		}*/
		System.out.print("Finished : ");
		currentTime = System.currentTimeMillis();
		System.out.println(currentTime - start + "[ms]");

	}

	/**
	 * <p>
	 * 2回目以降の実行
	 * <p>
	 * @param args, 旧バージョンのソースファイルオブジェクト
	 * @throws Exception
	 */

	private static void incrementalRun(Config config) throws Exception {
		System.out.println("ICVolti " + version);
		System.out.println("Start Incremental Clone Detection fase");
		System.out.println("----BoW ver----");
		// setJavaClassPath();
		getApplicationPath();
		//commandOption(args);

		long start = System.currentTimeMillis();
		long subStart = start;
		long currentTime;


		System.out.println("Extract word in source code ...");
		countMethod = 0;
		countBlock = 0;
		countLine = 0;

		ArrayList<String> oldFileList = null;
		ArrayList<String> newFileList = null;
		ArrayList<Block> newBlockList = new ArrayList<Block>();
		ArrayList<Block> oldBlockList = new ArrayList<Block>();
		ArrayList<Block> updatedBlockList = new ArrayList<Block>();
		ArrayList<Block> addedModifiedBlockList = new ArrayList<Block>();
		ArrayList<Block> deletedBlockList = new ArrayList<Block>();
		ArrayList<Block> allBlockList = new ArrayList<Block>();
		//ArrayList<SourceFile> newFileList
		//fileListの取得をちゃんとする
		AllData allData = new AllData();
		allData =  AllData.deserializeAllDataList("allData.bin");
		ArrayList<ClonePair> ClonePairList_test = new ArrayList<ClonePair>();
		AllData.synchronizeAllData();
		ClonePairList_test = allData.getClonePairList();
		ArrayList<SourceFile> FileList = new ArrayList<SourceFile>();

		switch (config.getLang()) {
		case 0: // "java"
			//	JavaAnalyzer3 oldJavaanalyzer = new JavaAnalyzer3();
			JavaAnalyzer3 newJavaanalyzer = new JavaAnalyzer3();
			//oldFileList = JavaAnalyzer3.searchFiles(Config.target2);
			/*ゆくゆくはなくしたいやつ*/
			newFileList = JavaAnalyzer3.searchFiles(config.getNewTarget());
			ArrayList<SourceFile> oldFileList_test = new ArrayList<SourceFile>();
			oldFileList_test =  allData.getSourceFileList();
			FileList = BlockUpdater.updateSourceFileList(config.getNewTarget(), config.getOldTarget(), oldFileList_test, newFileList,updatedBlockList);
			//			ArrayList<SourceFile> oldFileList_test = BlockUpdater.deserializeSourceFileList("SourceFileList.bin");
			//		ArrayList<SourceFile> FileList = BlockUpdater.updateSourceFileList(Config.target2, Config.target, oldFileList_test, newFileList);

			//ArrayList<SourceFile> FileList = JavaAnalyzer3.setFilesInfo(Config.target, Config.target2);
			//		System.out.println(Arrays.asList(FileList));


			//oldBlockList = oldJavaanalyzer.analyze(oldFileList);
			//newBlockList = newJavaanalyzer.analyze(newFileList);
			//List<Block> oldBlockList = new ArrayList<Block>();
			//oldBlockList.deserializeBlockList("blcoklist.bin");
			/*	for(SourceFile file : FileList) {
			for(Block block : file.getNewBlockList()) {
				System.out.println("FLIE l====enn =     " + block.getLen());
			}

			}*/

			System.out.println("newFileList size = " + newFileList.size());
			System.out.println("FileList size = " + FileList.size());
			//	newBlockListCorrect = newJavaanalyzer.analyze_test(FileList);
			newBlockList = newJavaanalyzer.incrementalAnalyze(FileList);
			//oldBlockList = BlockUpdater.deserializeBlockList("blocklist.bin");
			//System.out.println("old Block Size  = " + oldBlockList.size());
			//System.out.println("new Block Correct Size  = " + newBlockListCorrect.size());

			System.out.println("new Block Size 1  = " + newBlockList.size());


			//新旧コードブロック間の対応をとる
			newBlockList.addAll(TraceManager.analyzeBlock(FileList, newBlockList, config));
			/*		for(Block block : newBlockList) {
				System.out.println("new Block l====enn =     " + block.getLen());
			}*/
			//コードブロックのIDを再度割り振りなおす
			allBlockList = TraceManager.getAllBlock(FileList);


			//newBlockListの中から，DELETEDやADDEDやMODIFIEDに分類されたものがupdatedBLockListになるようにする

			System.out.println("new Block Size 2  = " + newBlockList.size());

			updatedBlockList.addAll(TraceManager.devideBlockCategory(newBlockList, 2));
			//allBlockList = TraceManager.devideBlockCategory(newBlockList, 3);
			addedModifiedBlockList = TraceManager.devideBlockCategory(allBlockList, 0);
			deletedBlockList = TraceManager.devideBlockCategory(updatedBlockList, 1);



			if(addedModifiedBlockList != null) {
				System.out.println("analyze block done ====");
			}else {

				System.out.println("====analyze block cant ====");

			}
			/*	for (Block block : addedModifiedBlockList) {
				System.out.println(block.getCategory());
			}*/


			System.out.println("addedModified block size  = " + addedModifiedBlockList.size());
			System.out.println("updated block size  = " + updatedBlockList.size());


			System.out.println("deleted block size  = " + deletedBlockList.size());


			System.out.println(
					"Parse new file / All file = " + newJavaanalyzer.countParseFiles + " / " + newJavaanalyzer.countFiles);
			break;
		case 1: // "c"
			CAnalyzer4 oldCanalyzer = new CAnalyzer4();
			CAnalyzer4 newCanalyzer = new CAnalyzer4();
			//		oldFileList = oldCanalyzer.searchFiles(Config.target2);
			//		newFileList = newCanalyzer.searchFiles(Config.target);
			oldBlockList = oldCanalyzer.analyze(oldFileList);
			newBlockList = oldCanalyzer.analyze(newFileList);

			break;
		case 2: // "c#"
			/*			CSharpAnalyzer csharpAnalyzer = new CSharpAnalyzer();
			fileList = CSharpAnalyzer.searchFiles(Config.target);
			blockList = csharpAnalyzer.analyze(fileList);
			System.out.println(
					"Parse file / All file = " + csharpAnalyzer.countParseFiles + " / " + csharpAnalyzer.countFiles);
			break;
			 */	}

		System.out.println("The number of methods : " + countMethod);
		System.out.println("The number of blocks (Excluding methods) : " + countBlock);
		System.out.println("The line : " + countLine);
		System.out.println("Extract word in source code done : " + (System.currentTimeMillis() - start) + "[ms]");
		System.out.println();


		//編集，削除されたクローンペアの削除
		BlockUpdater.resetClonePair(ClonePairList_test, updatedBlockList);
		//BlockUpdater.resetClonePair(ClonePairList_test, newBlockList);

		// 特徴ベクトル計算
		subStart = System.currentTimeMillis();

		VectorCalculator calculator = new VectorCalculator();
		//		blockList = calculator.filterMethod(blockList);
		//newBlockList = calculator.filterMethod(newBlockList);
		allBlockList = calculator.filterMethod(allBlockList, config);
		System.out.println("The threshold of size for method : " + config.getSize());
		System.out.println("The threshold of size for block : " + config.getBlockSize());
		System.out.println("The threshold of line of block : " + config.getMinLine());
		System.out.println("Filtered blocks / All blocks : " + allBlockList.size() + " / " + (countMethod + countBlock));
		System.out.println();

		System.out.println("Calculate vector of each method ...");

		calculator.calculateVector_test(allBlockList, addedModifiedBlockList, allData);
		// System.out.println("wordmap.size = " + wordMap.size());
		currentTime = System.currentTimeMillis();
		System.out.println(
				"Calculate vector done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		if (Config.LSH_PRG != LSHController.NO_LSH) {
			// LSHクラスタリング
			System.out.println("Cluster vector of each method ...");
			subStart = System.currentTimeMillis();

			// LSHController.computeParam(wordMap.size());
			System.out.println("LSH start");
			LSHController lshCtlr = new LSHController();
			lshCtlr.executePartially(allBlockList,addedModifiedBlockList, calculator.getDimention(), Config.LSH_PRG,config);
			System.out.println("dimention = " + dimention_test);
			System.out.println("calculator.getDimention() = " + calculator.getDimention());
			//	lshCtlr.executePartially(allBlockList,addedModifiedBlockList, dimention_test, Config.LSH_PRG);
			lshCtlr = null;
			System.out.println("LSH done : " + (System.currentTimeMillis() - subStart) + "[ms]");
			CloneJudgement cloneJudge = new CloneJudgement();
			for(ClonePair cp : ClonePairList_test) {
				int idA = cp.cloneA.getId();
				int idB = cp.cloneB.getId();
				System.out.println("testaidA = " + idA);
				System.out.println(" testaidB = " + idB);
				System.out.println("testA = " + cp.cloneA.getStartLine() + "  " +cp.cloneA.getEndLine());
				System.out.println("tesB = " + cp.cloneB.getStartLine() + "  " +cp.cloneB.getEndLine());
			}
			ClonePairList_test.addAll(cloneJudge.getClonePairListPartially(allBlockList, addedModifiedBlockList, config));
			for(ClonePair cp : ClonePairList_test) {
				int idA = cp.cloneA.getId();
				int idB = cp.cloneB.getId();
				System.out.println("ato testaidA = " + idA);
				System.out.println("ato testaidB = " + idB);
				System.out.println("atotestA = " + cp.cloneA.getStartLine() + "  " +cp.cloneA.getEndLine());
				System.out.println("ato tesB = " + cp.cloneB.getStartLine() + "  " +cp.cloneB.getEndLine());
			}
			cloneJudge = null;
		} else {
			CloneJudgement cloneJudge = new CloneJudgement();
			//clonePairList = cloneJudge.getClonePairListNoLSH(newBlockList);
		}

		System.out.println("The number of clone pair : " + ClonePairList_test.size());
		if (removeMethodPair)
			CloneJudgement.removePairOfMethod(ClonePairList_test);

		currentTime = System.currentTimeMillis();
		System.out.println("Cluster done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		ArrayList<CloneSet> cloneSetList = null;
		if (config.getResultNotifier() != null || config.getResultCloneSet() != null) {
			System.out.println("generate clone set start...");
			subStart = System.currentTimeMillis();

			cloneSetList = CloneJudgement.getCloneSetList(ClonePairList_test, allBlockList);
			System.out.println("The number of clone set : " + cloneSetList.size());
			currentTime = System.currentTimeMillis();
			System.out.println(
					"generate clone set done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		}

		// ファイル出力
		System.out.println("Output start ...");
		subStart = System.currentTimeMillis();
		// ArrayList<CloneSet> cloneSetList =
		// LSHController.getCloneSetList(clonePairList);
		// Outputter.outputCloneSetCSVforCPP(cloneSetList);
		// Outputter.outputCSVforCPP(clonePairList);


		if (config.getResultCSV() != null)
			Outputter.outputCSV(ClonePairList_test, config);
		if (config.getResultTXT() != null)
			Outputter.outputTXT(ClonePairList_test, config);
		if (config.getResultHTML() != null)
			Outputter.outputHTML(ClonePairList_test, config);
		if (config.getResultNotifier() != null)
			Outputter.outputNotifier(cloneSetList, newFileList, config);
		if (config.getResultCloneSet() != null)
			Outputter.outputCloneSetTXTforCPP(cloneSetList, config);


		// Outputter.outputForBigCloneEval(clonePairList);
		// Outputter.outputTXTforCPP(clonePairList);
		// Outputter.outputCSVforJava(clonePairList);
		// Outputter.outputTXTforJava(clonePairList);
		//Outputter.outputBlockList(blockList);
		// Outputter.outputStatisticsSample(clonePairList, 0.05, 1.96, 0.9);
		currentTime = System.currentTimeMillis();
		System.out.println("Output done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
		// 評価
		// Comparator.compareMeCC(cloneSetList);
		// Evaluator2.evaluate();
		// Debug.debug();
		// Debug.outputResult02(cloneSetList);
		// Debug.outputResult(cloneSetList);
		for(Block block : allBlockList) {
			System.out.println("Id  " + block.getId() );
		}

		for(Block block : updatedBlockList) {
			System.out.println("uId  " + block.getId() );
		}

		for(Block block : addedModifiedBlockList) {
				System.out.println("admoId  " + block.getId() );

		}
		for(ClonePair cp : ClonePairList_test) {
			int idA = cp.cloneA.getId();
			int idB = cp.cloneB.getId();
			System.out.println("aidA = " + idA);
			System.out.println("aidB = " + idB);
			System.out.println("A = " + cp.cloneA.getStartLine() + "  " +cp.cloneA.getEndLine());
			System.out.println("B = " + cp.cloneB.getStartLine() + "  " +cp.cloneB.getEndLine());
		}
		allData.setSourceFileList(FileList);
		allData.setClonePairList(ClonePairList_test);
		allData.setBlockListOfCalcedVec(allBlockList);
		AllData.serializeAllDataList(allData);
		allData = null;

		System.out.print("Finished : ");
		currentTime = System.currentTimeMillis();
		System.out.println(currentTime - start + "[ms]");

		oldFileList = null;
		newFileList = null;
		newBlockList = null;
		oldBlockList = null;
		updatedBlockList = null;
		addedModifiedBlockList = null;
		deletedBlockList = null;
		allBlockList = null;

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