package jp.ac.osaka_u.ist.sel.icvolti.trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import jp.ac.osaka_u.ist.sel.icvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.icvolti.Config;
import jp.ac.osaka_u.ist.sel.icvolti.Def;
import jp.ac.osaka_u.ist.sel.icvolti.analyze.JavaAnalyzer3;
import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;

/**
 * <p>Diff検出クラス</p>
 * @author h-honda
 */
public class DiffDetector {

	/**
	 * <p>ソースファイル間のdiffの取得</p>
	 * @param fileList 新旧バージョン間でdiffを実施するファイルのリスト
	 * @return <ul>
	 *           <li>成功の場合 - true</li>
	 *           <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	public static boolean  getDiff(ArrayList<SourceFile> fileList) {

		//ディレクトリ全体にdiffをかけて

		for(SourceFile file: fileList) {
			// ファイルが存続 かつ ブロックが消滅していない場合
			//	if(file.getState() == SourceFile.NORMAL && !(file.getNewBlockList().isEmpty() && !file.getOldBlockList().isEmpty())) {
			//	System.out.println("SourceFile.NOMAL = " + file.getState());



			if(file.getState() == SourceFile.NORMAL) {
				//	System.out.println("NOMAL source FIle");
				if(!executeDiff(file)) {
					System.out.println("diff false");
					return false;
				}
			}
		}
		return true;
	}


	public static boolean  getDiff_test(ArrayList<SourceFile> fileList, ArrayList<Block> newBlockList, Config config) {

		//ディレクトリ全体にdiffをかけて

		if(!executeDiff_test(fileList, newBlockList, config)) {
			return false;
		}else {
			return true;
		}
	}



	/**
	 * <p>diffの実行</p>
	 * @param file 新旧バージョン間のdiffを取るファイル
	 * @return <ul>
	 *           <li>成功の場合 - true</li>
	 *           <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private static boolean executeDiff_test(ArrayList<SourceFile> fileList, ArrayList<Block> newBlockList, Config config) {
		try{

			//System.out.println("DIFFFFFFFFF");

			ProcessBuilder pb;
			if (File.separatorChar == '\\') {
				//diff -r oldfolpath newfolpath
				String[] cmdArray = { Paths.get(Def.CCVOLTI_PATH, Def.DIFF_PATH).toString(),"-r" ,config.getOldTarget(), config.getNewTarget() };
				//	System.out.println(Arrays.asList(cmdArray));
				//System.out.println("kita  ====");
				pb = new ProcessBuilder(cmdArray);
			} else {
				String[] cmdArray = { "diff", "-r", config.getOldTarget(), config.getNewTarget() };
				System.out.println(Arrays.asList(cmdArray));
				//System.out.println("kitaeeeeeeee  ====");
				pb = new ProcessBuilder(cmdArray);
			}
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;

			SourceFile subjectFile = null ;
			int diffSearchFlag =0;
			CloneDetector.updatedCode = false;
			while((line = reader.readLine()) != null) {


				//	 	System.out.println("watasiha = ! " + line.substring(0,4).contains("diff"));

				//ここのファイル検索もっと効率化できる

				if(line.contains("diff -r ")) {
					String[] command = line.split(" ");
					if(CloneDetector.modeDebug) {
						   System.out.println("line = " + line);
						}
					//	   System.out.println("command = " + command[3].replace("/", "\\"));
					//特定の拡張子を持ったファイルのみを検出対象にする
					String fileExtension1 = null;
					String fileExtension2 = null;
					if(config.getLang()==0) {
						fileExtension1 = ".java";
					}else if(config.getLang() == 1){
						fileExtension1 = ".c";
						fileExtension2 = ".cpp";
					}else if(config.getLang() == 2){
						fileExtension1 = ".cs";
					}
					File fileName = new File(command[3]);
					if(config.getLang() == 1) {
						if(fileName.isFile() && fileName.getName().endsWith(fileExtension1) && fileName.getName().endsWith(fileExtension2)){
							diffSearchFlag = 1;
							//ここのファイル検索の効率化
							for(SourceFile file: fileList) {
								//		   System.out.println("===========miki = "  + file.getNewPath());
								if(file.getNewPath().contains(command[3].replace("/", "\\"))){
									subjectFile = file;
									JavaAnalyzer3.analyzeAFile(file, newBlockList);
									CloneDetector.updatedCode = true;
									//System.out.println("=========== newBlock List===============");
									//ソースコードのparse
									break;
									//	   System.out.println("===========diff===============");
								}
							}
						}else {
							diffSearchFlag = 0;
						}

					}else {
						if(fileName.isFile() && fileName.getName().endsWith(fileExtension1)){
							diffSearchFlag = 1;
							//
							//ここのファイル検索の効率化
							for(SourceFile file: fileList) {
								//		   System.out.println("===========miki = "  + file.getNewPath());
								if(file.getNewPath().contains(command[3].replace("/", "\\"))){
									subjectFile = file;
									JavaAnalyzer3.analyzeAFile(file, newBlockList);
									CloneDetector.updatedCode = true;
									//System.out.println("=========== newBlock List===============");
									//ソースコードのparse
									break;
									//	   System.out.println("===========diff===============");
								}
							}
						}else {
							diffSearchFlag = 0;
						}
					}
				}else if(Character.isDigit(line.charAt(0)) && diffSearchFlag==1) {


					// 追加コード
					/*line = 29a30 旧29行目から1行追加されて，新バージョンの30行目に追加された行がある
					  line = 30a32 旧30行目から1行追加されて，新バージョンの32行目に追加された行がある
					  line = 194a197,208 旧194行目から12行追加されて，新バージョンんお197行目～208行目に追加された行がある
					  line = 20d19 旧20が削除された
					  line = 22c21 旧22が削除されて新21行が追加された
					  line = 24c23 旧24が削除された新23行目になった
					 * */
					if(line.contains("a")) {
						int startLine, endLine;
						String[] str1 = line.split("a");
						String[] str2 = str1[1].split(",");

						// 1行だけ追加された場合
						if(str2.length == 1) {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[0]);
						} else {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[1]);
						}

						for (int i = startLine; i <= endLine; i++) {
							subjectFile.getAddedCodeList().add(i);
						}

						// 削除コード
					} else if (line.contains("d")) {
						int startLine, endLine;
						String[] str1 = line.split("d");
						String[] str2 = str1[0].split(",");

						// 1行だけ削除された場合
						if (str2.length == 1) {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[0]);
						} else {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[1]);
						}

						for (int i = startLine; i <= endLine; i++) {
							subjectFile.getDeletedCodeList().add(i);
						}

						// 変更コード
						// 変更部分全体が削除, 追加されたとみなす
					} else if(line.contains("c")) {
						int startLine, endLine;
						String[] str1 = line.split("c");
						String[] str2 = str1[0].split(",");
						String[] str3 = str1[1].split(",");

						if(str2.length == 1) {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[0]);
						} else {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[1]);
						}

						for(int i = startLine; i <= endLine; i++) {
							subjectFile.getDeletedCodeList().add(i);
						}

						if(str3.length == 1) {
							startLine = Integer.valueOf(str3[0]);
							endLine = Integer.valueOf(str3[0]);
						} else {
							startLine = Integer.valueOf(str3[0]);
							endLine = Integer.valueOf(str3[1]);
						}

						for(int i = startLine; i <= endLine; i++) {
							subjectFile.getAddedCodeList().add(i);
						}
					}
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * <p>diffの実行</p>
	 * @param file 新旧バージョン間のdiffを取るファイル
	 * @return <ul>
	 *           <li>成功の場合 - true</li>
	 *           <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private static boolean executeDiff(SourceFile file) {
		try{

			//System.out.println("DIFFFFFFFFF");

			ProcessBuilder pb;
			if (File.separatorChar == '\\') {

				String[] cmdArray = { Paths.get(Def.CCVOLTI_PATH, Def.DIFF_PATH).toString(), file.getOldPath(),
						file.getNewPath() };
				//	System.out.println(Arrays.asList(cmdArray));
				//System.out.println("kita  ====");
				pb = new ProcessBuilder(cmdArray);
			} else {
				String[] cmdArray = { "diff", file.getOldPath(), file.getNewPath() };
				System.out.println(Arrays.asList(cmdArray));
				//System.out.println("kitaeeeeeeee  ====");
				pb = new ProcessBuilder(cmdArray);
			}
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;

			while((line = reader.readLine()) != null) {
				//System.out.println("line = " + line);
				if(Character.isDigit(line.charAt(0))) {


					// 追加コード
					/*line = 29a30 旧29行目から1行追加されて，新バージョンの30行目に追加された行がある
					  line = 30a32 旧30行目から1行追加されて，新バージョンの32行目に追加された行がある
					  line = 194a197,208 旧194行目から12行追加されて，新バージョンんお197行目～208行目に追加された行がある
					  line = 20d19 旧20が削除された
					  line = 22c21 旧22が削除されて新21行が追加された
					  line = 24c23 旧24が削除された新23行目になった
					 * */
					if(line.contains("a")) {
						int startLine, endLine;
						String[] str1 = line.split("a");
						String[] str2 = str1[1].split(",");

						// 1行だけ追加された場合
						if(str2.length == 1) {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[0]);
						} else {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[1]);
						}

						for (int i = startLine; i <= endLine; i++) {
							file.getAddedCodeList().add(i);
						}

						// 削除コード
					} else if (line.contains("d")) {
						int startLine, endLine;
						String[] str1 = line.split("d");
						String[] str2 = str1[0].split(",");

						// 1行だけ削除された場合
						if (str2.length == 1) {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[0]);
						} else {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[1]);
						}

						for (int i = startLine; i <= endLine; i++) {
							file.getDeletedCodeList().add(i);
						}

						// 変更コード
						// 変更部分全体が削除, 追加されたとみなす
					} else if(line.contains("c")) {
						int startLine, endLine;
						String[] str1 = line.split("c");
						String[] str2 = str1[0].split(",");
						String[] str3 = str1[1].split(",");

						if(str2.length == 1) {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[0]);
						} else {
							startLine = Integer.valueOf(str2[0]);
							endLine = Integer.valueOf(str2[1]);
						}

						for(int i = startLine; i <= endLine; i++) {
							file.getDeletedCodeList().add(i);
						}

						if(str3.length == 1) {
							startLine = Integer.valueOf(str3[0]);
							endLine = Integer.valueOf(str3[0]);
						} else {
							startLine = Integer.valueOf(str3[0]);
							endLine = Integer.valueOf(str3[1]);
						}

						for(int i = startLine; i <= endLine; i++) {
							file.getAddedCodeList().add(i);
						}
					}
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}