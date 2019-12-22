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
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;

/**
 * <p>Diff検出クラス</p>
 * @author h-honda
 */
public class DiffDetector {

	static boolean fileExist = true;

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


	//	public static boolean  getDiff_test(ArrayList<SourceFile> fileList, ArrayList<Block> newBlockList, Config config) {
	public static boolean  getDiff_test(ArrayList<SourceFile> fileList, Config config) {

		//ディレクトリ全体にdiffをかけて

		//		if(!executeDiff_test(fileList, newBlockList, config)) {
		if(!executeDiff_test(fileList, config)) {
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
	//	private static boolean executeDiff_test(ArrayList<SourceFile> fileList, ArrayList<Block> newBlockList, Config config) {
	private static boolean executeDiff_test(ArrayList<SourceFile> fileList, Config config) {
		try{

			//System.out.println("DIFFFFFFFFF");

			ProcessBuilder pb;
			if (File.separatorChar == '\\') {
				//diff -r oldfolpath newfolpath
				/*
				 * 再帰的にスペースは無視して，diff
				 * diff.exe --help でコマンドオプションがわかる
				 * 通常のdiffとオプションが違うので注意
				 * */
				String[] cmdArray = { Paths.get(Def.CCVOLTI_PATH, Def.DIFF_PATH).toString(),"-r" ,"-w","-I=\"\\t\"" ,config.getOldTarget(), config.getNewTarget() };
				//	System.out.println(Arrays.asList(cmdArray));
				//System.out.println("kita  ====");
				pb = new ProcessBuilder(cmdArray);
			} else {
				/*
				 * -E オプションを付けるとタブをスペースとして見てくれるけど，diff.exeにはそのオプションがないので使用しない
				 * */

				String[] cmdArray = { "diff", "-r","-w", "-I=\"\\t\"",config.getOldTarget(), config.getNewTarget() };
				//				System.out.println(Arrays.asList(cmdArray));
				//System.out.println("kitaeeeeeeee  ====");
				pb = new ProcessBuilder(cmdArray);
			}
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;

			SourceFile subjectFile = null ;
			int diffSearchFlag =0;
			CloneDetector.modifiedSourceFile = false;
			while((line = reader.readLine()) != null) {
				//				System.out.println("line = " + line);


				//	 	System.out.println("watasiha = ! " + line.substring(0,4).contains("diff"));

				//ここのファイル検索もっと効率化できる
				boolean analyzeFileDone = false;
				boolean copyRightModified = false;

				if(line.contains("diff -r -w")) {
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
					File fileName = new File(command[5]);
					if(config.getLang() == 1) {
						if((fileName.isFile() && fileName.getName().endsWith(fileExtension1)) ||
								(fileName.isFile() && fileName.getName().endsWith(fileExtension2))){
							diffSearchFlag = 1;
							fileExist = false;
							//ここのファイル検索の効率化
							for(SourceFile file: fileList) {
								//		   System.out.println("===========miki = "  + file.getNewPath());
								if(file.getNewPath().contains(command[5].replace("/", "\\"))){
									subjectFile = file;
									file.setState(SourceFile.MODIFIED);
									//									CAnalyzer4.analyzeAFile(file, newBlockList);
									//									CAnalyzer4.analyzeAFile(file);
									if(CloneDetector.modeDebug) {
										System.out.println("analyze new file c c++");
									}
									CloneDetector.modifiedSourceFile = true;
									//System.out.println("=========== newBlock List===============");
									//ソースコードのparse
									fileExist = true;
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
							fileExist = false;
							//ここのファイル検索の効率化
							for(SourceFile file: fileList) {
								//		   System.out.println("===========miki = "  + file.getNewPath());
								if(file.getNewPath().contains(command[5].replace("/", "\\"))){
									subjectFile = file;
									file.setState(SourceFile.MODIFIED);
									if(config.getLang() == 0) {
										//										JavaAnalyzer3.analyzeAFile(file, newBlockList);
										//										JavaAnalyzer3.analyzeAFile(file);

										if(CloneDetector.modeDebug) {
											System.out.println("analyze new file java ");
										}
									}else if(config.getLang() == 2) {
										//										CSharpAnalyzer.analyzeAFile(file, newBlockList);
										//										CSharpAnalyzer.analyzeAFile(file);
										if(CloneDetector.modeDebug) {
											System.out.println("analyze new file csharp ");
										}
									}
									CloneDetector.modifiedSourceFile = true;
									//System.out.println("=========== newBlock List===============");
									//ソースコードのparse
									fileExist = true;
									break;
									//	   System.out.println("===========diff===============");
								}

							}
						}else {
							diffSearchFlag = 0;
						}
					}
				}else if(diffSearchFlag==1 && fileExist && line.length()>0 ) {
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
					}else {
						if(line.contains("*")) {
							if(line.toLowerCase().contains("copyright") || line.toLowerCase().contains("(c)") ) {
//								System.out.println("flag on copy");
								subjectFile.setCopyRightModified(true);

							}
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