package jp.ac.osaka_u.ist.sel.icvolti.trace;

import java.util.ArrayList;
import java.util.Iterator;

import jp.ac.osaka_u.ist.sel.icvolti.Config;
import jp.ac.osaka_u.ist.sel.icvolti.Logger;
import jp.ac.osaka_u.ist.sel.icvolti.model.AllData;
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
	public static ArrayList<Block> analyzeBlock(ArrayList<SourceFile> FileList, ArrayList<Block> newBlockList, Config config, AllData allData) {
		// TODO 自動生成されたメソッド・スタブ
		// ファイルのdiffを取得
		System.out.print("analyze block start");

		ArrayList<Block> updatedBlockList = new ArrayList<Block>();

		long start;
		long end;
		start = System.currentTimeMillis();
		if (!DiffDetector.getDiff_test(FileList, newBlockList, config)) {
			System.out.println("diff miss ======");
			Logger.writeln("Can't get diff of source code.", Logger.ERROR);
			return null;
		}
		end = System.currentTimeMillis();
		System.out.println("diff done  time = " + (end - start) + "[ms]");

		// クローンの分類，コード位置の重複に基づいた親子クローン取得
		updatedBlockList =  new BlockCategorizer().categorizeBlock(FileList,allData);
		Logger.writeln("<Success> Categorized clone.", Logger.INFO);


		System.out.println(" ============ blocksize + " + updatedBlockList.size());

		return updatedBlockList;

	}

	public static ArrayList<Block> devideBlockCategory(ArrayList<Block> updatedBlockList, int flag){
		ArrayList<Block> devidedBlockList = new ArrayList<Block>();
		//flag == 0の場合, 追加，編集されたものに分ける
		//flag == 1の場合, 削除されたものに分ける
		//flag == 2の場合，追加，編集，削除されたものに分ける
		//flag == 3の場合，Stableも含めた，現状プロジェクト内にあるすべてのコードブロック
		if(flag == 0) {
			Iterator<Block> i = updatedBlockList.iterator();
			while(i.hasNext()){
				Block bk = i .next();
				int category = bk.getCategory();
				if(category == 1 || category == 3) {
					devidedBlockList.add(bk);
				}
			}
		}else if(flag == 1){
			Iterator<Block> i = updatedBlockList.iterator();
			while(i.hasNext()){
				Block bk = i .next();
				int category = bk.getCategory();
				if(category == 4) {
					devidedBlockList.add(bk);
				}

			}

		}else if(flag == 2) {
			Iterator<Block> i = updatedBlockList.iterator();
			while(i.hasNext()){
				Block bk = i .next();
				int category = bk.getCategory();
			//	System.out.println("cate = " + category);
				if(category == 1 ) {
			//		System.out.println(" bk..getOldBlock getFile = " + bk.getOldBlock().getFileName() );

				}
				if(category == -1) {
		//			System.out.println(" block class = " + bk.getFileName() );

				}
				if(category == 1 || category == 3 || category ==4) {
					devidedBlockList.add(bk);
				}
			}
		}else if(flag == 3) {
			Iterator<Block> i = updatedBlockList.iterator();
			while(i.hasNext()){
				Block bk = i .next();
				int category = bk.getCategory();
			//	System.out.println("cate = " + category);
				if(category == 1 ) {
			//		System.out.println(" bk..getOldBlock getFile = " + bk.getOldBlock().getFileName() );

				}
				if(category == -1) {
			//		System.out.println(" block class = " + bk.getFileName() );

				}
				if(category == 0 ||category == 1 || category == 3) {
					devidedBlockList.add(bk);
				}
			}
		}

		return devidedBlockList;

	}

	public static ArrayList<Block> getAllBlock(ArrayList<SourceFile> FileList) {
		ArrayList<Block> blockList = new ArrayList<Block>();
		for(SourceFile file : FileList) {
			blockList.addAll(file.getNewBlockList());
		}
		System.out.println("end getAllBlock");

		return blockList;
	}


}