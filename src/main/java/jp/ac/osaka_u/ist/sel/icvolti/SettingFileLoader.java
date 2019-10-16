package jp.ac.osaka_u.ist.sel.icvolti;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * <p>設定ファイル読み込みクラス</p>
 * @author y-yuuki
 */
public class SettingFileLoader {

	/**
	 * <p>設定ファイルの読み込み</p>
	 * @author y-yuuki
	 * @author m-sano
	 * @author h-honda
	 * @param settingFile 設定ファイル名
	 * @param config Projectオブジェクト
	 * @return <ul>
	 *           <li>成功の場合 - true</li>
	 *           <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	public static boolean loadSettingFile(String settingFile[], Config config) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(settingFile[0]));
			int inputFlag =0;
			String line;
			while((line = reader.readLine()) != null) {

				// 先頭 "%" はコメント行として扱えるようにしている模様
				if(line.length() > 0 && line.charAt(0) != '%') {

					// 言語
					if(line.contains("LANGUAGE:")) {
						inputFlag = 0;
							if (removeSpace(line.replace("LANGUAGE:","")).equals("java"))
								config.setLang(0);
							if (removeSpace(line.replace("LANGUAGE:","")).equals("c"))
								config.setLang(1);
							if (removeSpace(line.replace("LANGUAGE:","")).equals("csharp"))
								config.setLang(2);
						//config.setLang(removeSpace(line.replace("LANGUAGE:","")));
					}
					// 出力形式
					if(line.contains("OUTPUT_FORMAT:")) {
						inputFlag = 0;
						config.setOutputFormat(removeSpace(line.replace("OUTPUT_FORMAT:","")));
					}
					// 類似度
					if(line.contains("SIM:")) {
						inputFlag = 0;
						config.setSim(Integer.parseInt(removeSpace(line.replace("SIM:",""))));
					}
					// トークンサイズ
					if(line.contains("SIZE:")) {
						inputFlag = 0;
						config.setSize(Integer.parseInt(removeSpace(line.replace("SIZE:",""))));
					}
					// ブロックトークンサイズ
					if(line.contains("BLOCK_SIZE:")) {
						inputFlag = 0;
						config.setBlockSize(Integer.parseInt(removeSpace(line.replace("BLOCK_SIZE:",""))));
					}
					// 最小行数
					if(line.contains("MIN_LINE:")) {
						inputFlag = 0;
						config.setMinLine(Integer.parseInt(removeSpace(line.replace("MIN_LINE:",""))));
					}
		/*			// 文字コード
					if(line.contains("CHARSET:")) {
						inputFlag = 0;
						config.setCharset(removeSpace(line.replace("CHARSET:","")));
					}
				*/
					// スレッド数
					if(line.contains("THREADS:")) {
						inputFlag = 0;
						config.setThreads(Integer.parseInt(removeSpace(line.replace("THREADS:",""))));
					}

					// 過去のデータの場所
					if(line.contains("DATA_DIR:")) {
						inputFlag = 0;
						config.setDataDir(removeSpace(line.replace("DATA_DIR:","")));
					}
					// 出力場所
					if(line.contains("OUTPUT_DIR:")) {
						inputFlag = 0;
						config.setOutputDir(removeSpace(line.replace("OUTPUT_DIR:","")));
					}

					// 入力ディレクトリ
					if(line.contains("INPUT_DIR:")) {
						inputFlag = 1;

						config.setInputDir(removeSpace(line.replace("INPUT_DIR:","")));
					}


					// Gitから直接clone
					if(line.contains("PRE_DATA:")) {
						if(removeSpace(line.replace("PRE_DATA:","")).equals("true")) {
							config.setPreData(true);
						} else if(removeSpace(line.replace("PRE_DATA:","")).equals("false")) {
							config.setPreData(false);
						}
						inputFlag = 0;
					}

					if(inputFlag == 1) {
						config.setInputDir(removeSpace(line));

					}



				}
			}
		} catch(Exception e) {
			return false;
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Logger.writeError(e);
				}
			}
		}
		// 作業ディレクトリ指定がない場合はデフォルトディレクトリに設定
/*		if (config.getWorkDir() == null)
			config.setWorkDir(Paths.get(Def.NOTIFIER_PATH, Def.DEFAULT_WORK_DIR).toString());

*/
		// 言語とツール名の対応チェック
	/*	if(!Def.isValidLang(config.getLang(), config.getTool())) {
			return false;
		}
*/



		return true;
	}

	/**
	 * <p>スペース除去<p>
	 * @param str 文字列
	 * @return スペース除去後の文字列
	 */
	private static String removeSpace(String str) {
		while(str.startsWith(" ") || str.startsWith("\t")) {
			str = str.substring(1);
		}
		return str;
	}

}
