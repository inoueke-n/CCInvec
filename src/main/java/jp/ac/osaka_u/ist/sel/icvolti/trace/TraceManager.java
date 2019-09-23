package jp.ac.osaka_u.ist.sel.icvolti.trace;

import java.util.ArrayList;
import java.util.List;

import jp.ac.osaka_u.ist.sel.icvolti.Logger;
import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;

public class TraceManager {



	/**
	 * <p>コードブロック変更情報を分類する</p>
	 * @param newBlockList
	 * @param oldBlockList
	 * @return
	 * @return <ul>
	 *           <li>成功の場合 - true</li>
	 *           <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	public static List<Block> analyzeBlock(ArrayList<SourceFile> FileList) {
		// TODO 自動生成されたメソッド・スタブ
		// ファイルのdiffを取得
		System.out.print("analyze block start");

		List<Block> updatedBlockList = new ArrayList<Block>();

		if (!DiffDetector.getDiff_test(FileList)) {
		System.out.println("diff miss ======");
			Logger.writeln("Can't get diff of source code.", Logger.ERROR);
			return null;
		}
		System.out.println("diff done ======");

		// クローンの分類，コード位置の重複に基づいた親子クローン取得
		updatedBlockList =  new BlockCategorizer().categorizeBlock(FileList);
		Logger.writeln("<Success> Categorized clone.", Logger.INFO);

		return updatedBlockList;

	}

}
