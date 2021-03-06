package jp.ac.osaka_u.ist.sel.ccinvec.trace;

import java.util.ArrayList;

import jp.ac.osaka_u.ist.sel.ccinvec.BlockUpdater;
import jp.ac.osaka_u.ist.sel.ccinvec.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccinvec.model.AllData;
import jp.ac.osaka_u.ist.sel.ccinvec.model.Block;
import jp.ac.osaka_u.ist.sel.ccinvec.model.SourceFile;

/**
 * <p>
 * コードブロックの変更履歴分類クラス
 * </p>
 *
 * @author y-yuuki
 */
public class BlockCategorizer {

	/**
	 * <p>
	 * コードブロックの分類
	 * </p>
	 *
	 * @param fileList
	 *            ソースファイルリスト
	 */
	public static ArrayList<Block> categorizeBlock(ArrayList<SourceFile> fileList, AllData allData) {

		ArrayList<Block> updatedBlockList = new ArrayList<Block>();

		for (SourceFile file : fileList) {
			//	System.out.println(" start filelist");
			//ここでmodifiedされたファイルだけやるれば効率化と高速化して，normalなやつはすべてstableつければいい
			if (file.getState() == SourceFile.STABLE) {
				for(Block block : file.getNewBlockList()) {
					block.setCategory(Block.STABLE);
				}
				for(Block block : file.getOldBlockList()) {
					block.setCategory(Block.STABLE);
				}
			}else {
				if (file.getState() == SourceFile.MODIFIED) {
					//ここでmodifiedされたファイルだけやるれば効率化と高速化して，normalなやつはすべてstableつければいい
					if(CloneDetector.modeDebug) {
						System.out.println("Source File Nomal " + file.getNewPath());
					}
					categorizeStableModified(file,updatedBlockList, allData);
				}
				//System.out.println("=============Source File ADD DELETE ");
				// 実行時点で, 新旧両方に存在するブロックは分類されているはず
				categorizeAddedDeleted(file,updatedBlockList);
			}


		}
		return updatedBlockList;
	}

	/**
	 * <p>
	 * Stable/Modifiedブロックの分類
	 * </p>
	 *
	 * @param file
	 *            ブロック分類を行うソースファイル
	 */
	private static void categorizeStableModified(SourceFile file,ArrayList<Block> updatedBlockList, AllData allData) {
		//int i = 0;
		//		System.out.println("categoryzed file  " + file.getNewPath()  );
		for (Block blockA : file.getNewBlockList()) {


			//		System.out.println(i +  " getNewCloneList = " + file.getNewPath());
			//		System.out.println("categorize stable = " + blockA.getId() + " getfile = "  + blockA.getStartLine() + "- " + blockA.getEndLine());
			//		i++;

			int addedLineStart = 0;
			int addedLineEnd = 0;

			//2バージョン間に存在して，追加された行がなければ，ここのfor文は通らない
			//コードブロックの開始行/終了行までの追加行の計算
			for (int line : file.getAddedCodeList()) {
				if (line < blockA.getStartLine()) {
					addedLineStart++;
					addedLineEnd++;
					//コードブロック内に追加された行があれば，コードブロックの終了行をインクメント
				} else if (line <= blockA.getEndLine()) {
					addedLineEnd++;
				} else {
					break;
				}
			}

			for (Block blockB : file.getOldBlockList()) {

				int deletedLineStart = 0;
				int deletedLineEnd = 0;
				// コードブロックの開始行/終了行までの削除行の計算
				for (int line : file.getDeletedCodeList()) {
					//コードブロックがある場所よりまえで削除された行があれば，
					if (line < blockB.getStartLine()) {
						deletedLineStart++;
						deletedLineEnd++;
						//コードブロック内で削除された行があれば，
					} else if (line <= blockB.getEndLine()) {
						deletedLineEnd++;
					} else {
						break;
					}
				}


				// 親子関係が存在するか判定
				// 行の重複が30%以上のとき追跡

				//上記で削除された行と追加された行をカウントした情報をもとに，
				//Block Aの開始行前に新バージョンで追加された行を引く
				int startLineA = blockA.getStartLine() - addedLineStart;
				//Block A内に新バージョンで追加された行を引く
				int endLineA = blockA.getEndLine() - addedLineEnd;
				//Block Bの開始行前に新バージョンで削除された行を引く
				int startLineB = blockB.getStartLine() - deletedLineStart;
				//Block B内に新バージョンで削除された行を引く
				int endLineB = blockB.getEndLine() - deletedLineEnd;
				//コード片の重複度を計算，30パーンセット重複していれば，追跡
				double sim = calcurateLocationSimilarity(blockA, blockB, startLineA, endLineA, startLineB, endLineB);
				if (sim >= 0.1 && blockB.getNewBlock() == null) {
					if (blockA.getOldBlock() != null) {
						//blockA.getLocationSimilarity() デフォルト値は0
						if (sim > blockA.getLocationSimilarity()) {
							blockA.setOldBlock(blockB);
							blockA.setLocationSimilarity(sim);
							if (addedLineStart == addedLineEnd && deletedLineStart == deletedLineEnd && blockA.getWordList().equals(blockB.getWordList())) {
								blockA.setCategory(Block.STABLE);
								blockB.setCategory(Block.STABLE);
								if(blockA.getVector() == null) {
									blockA.setVector(blockB.getVector());
									blockA.setLen(blockB.getLen());
									//blockBと同じIDのブロックをクローンペアリストから探して，探して，blockAに入れ替え？
									BlockUpdater.updateClonePairBlock(blockA,blockB, allData);
									//								System.out.println("stable block List 1");
									//								System.out.println("Block new STABLE = filename " + blockA.getFileName() + "start line =  " + blockA.getStartLine() + "end line = " + blockA.getEndLine());
									//								System.out.println("Block new STABLE = filename " + blockB.getFileName() + "start line =  " + blockB.getStartLine() + "end line = " + blockA.getEndLine());
								}
								//System.out.println("Block STABLE = filename " + blockA.getFileName() + "start line =  " + blockA.getStartLine() + "end line = " + blockA.getEndLine());
							} else {
								if(blockA.getCategory() == Block.NULL) {
									blockA.setCategory(Block.MODIFIED);
								}
								blockB.setCategory(Block.MODIFIED);
								//								updatedBlockList.add(blockA);
//								System.out.println("ADD block List 1");
//								System.out.println("Block MODIFIED = filename " + blockA.getFileName() + "start line =  " + blockA.getStartLine() + "end line = " + blockA.getEndLine());
							}
						}
						//上のifに入るのはどんな状況？基本的にはしたのelseにはいる？
					}else {
						blockA.setOldBlock(blockB);
						blockA.setLocationSimilarity(sim);
						if (addedLineStart == addedLineEnd && deletedLineStart == deletedLineEnd &&  blockA.getWordList().equals(blockB.getWordList())) {
							blockA.setCategory(Block.STABLE);
							blockB.setCategory(Block.STABLE);
							if(blockA.getVector() == null) {
								blockA.setVector(blockB.getVector());
								blockA.setLen(blockB.getLen());
								BlockUpdater.updateClonePairBlock(blockA,blockB, allData);
								//																System.out.println("stable block List 2");
								//																System.out.println("Block new STABLE = filename " + blockA.getFileName() + "start line =  " + blockA.getStartLine() + "end line = " + blockA.getEndLine());
								//																System.out.println("Block new STABLE = filename " + blockB.getFileName() + "start line =  " + blockB.getStartLine() + "end line = " + blockA.getEndLine());

							}
							//	System.out.println("Block STABLE = filename " + blockA.getFileName() + "start line =  " + blockA.getStartLine() + "end line = " + blockA.getEndLine());
						} else {
							if(blockA.getCategory() == Block.NULL) {
								blockA.setCategory(Block.MODIFIED);
							}
							blockB.setCategory(Block.MODIFIED);
//							updatedBlockList.add(blockA);
//							System.out.println("ADD block List 2");
//							System.out.println("Block MODIFIED = filename " + blockA.getFileName() + "start line =  " + blockA.getStartLine() + "end line = " + blockA.getEndLine());
						}
					}
					if (blockB.getNewBlock() != null) {
						if (sim > blockB.getLocationSimilarity()) {
							blockB.setNewBlock(blockA);
							blockB.setLocationSimilarity(sim);
							if (addedLineStart == addedLineEnd && deletedLineStart == deletedLineEnd && blockA.getWordList().equals(blockB.getWordList())) {
								blockA.setCategory(Block.STABLE);
								blockB.setCategory(Block.STABLE);
								if(blockA.getVector() == null) {
									blockA.setVector(blockB.getVector());
									blockA.setLen(blockB.getLen());
									BlockUpdater.updateClonePairBlock(blockA,blockB, allData);
								}
								//System.out.println("Block STABLE = filename " + blockB.getFileName() + "start line =  " + blockB.getStartLine() + "end line = " + blockA.getEndLine());
							} else {
								blockB.setCategory(Block.MODIFIED);
								if(blockA.getCategory() == Block.NULL) {
									blockA.setCategory(Block.MODIFIED);
								}
//								updatedBlockList.add(blockA);
//								System.out.println("ADD block List 3 ");
//								System.out.println("Block MODIFIED = filename " + blockB.getFileName() + "start line =  " + blockB.getStartLine() + "end line = " + blockA.getEndLine());
							}
						}
					}else {
						blockB.setNewBlock(blockA);
						blockB.setLocationSimilarity(sim);
						if (addedLineStart == addedLineEnd && deletedLineStart == deletedLineEnd && blockA.getWordList().equals(blockB.getWordList())) {
							blockA.setCategory(Block.STABLE);
							blockB.setCategory(Block.STABLE);
							if(blockA.getVector() == null) {
								blockA.setVector(blockB.getVector());
								blockA.setLen(blockB.getLen());
								BlockUpdater.updateClonePairBlock(blockA,blockB, allData);
							}
							//	System.out.println("Block STABLE = filename " + blockB.getFileName() + "start line =  " + blockB.getStartLine() + "end line = " + blockA.getEndLine());
						} else {
							blockB.setCategory(Block.MODIFIED);
							if(blockA.getCategory() == Block.NULL) {
								blockA.setCategory(Block.MODIFIED);
							}
							//updatedBlockList.add(blockA);
							//							System.out.println("ADD block List 4 ");
							//							System.out.println("Block MODIFIED = filename " + blockA.getFileName() + "start line =  " + blockA.getStartLine() + "end line = " + blockA.getEndLine());
						}
					}

					/*
					if (!blockB.getBlockSet().getNewBlockSetList().contains(blockA.getBlockSet())) {
						blockB.getBlockSet().getNewBlockSetList().add(blockA.getBlockSet());
					}
					if (!blockA.getBlockSet().getOldBlockSetList().contains(blockB.getBlockSet())) {
						blockA.getBlockSet().getOldBlockSetList().add(blockB.getBlockSet());
					}*/
					//					System.out.println("===========================================");
					//					System.out.println("block A File Name = " + blockA.getFileName());
					//					System.out.println("block A startLine = " + blockA.getStartLine());
					//					System.out.println("block A end  Line = " + blockA.getEndLine());
					//					System.out.println("block B File Name = " + blockB.getFileName());
					//					System.out.println("block B startLine = " + blockB.getStartLine());
					//					System.out.println("block B end  Line = " + blockB.getEndLine());
					//					System.out.println("===========================================");

				}
			}
		}
	}

	private static double calcurateLocationSimilarity(Block blockA, Block blockB, int startLineA, int endLineA, int startLineB,
			int endLineB) {
		double sim = 0.0;
		//ブロックの行数計算
		int lineA = blockA.getEndLine() - blockA.getStartLine();
		int lineB = blockB.getEndLine() - blockB.getStartLine();

		//コードブロックが削除されている？
		if (startLineA >= endLineB) {
			return 0;
		}
		if (startLineB >= endLineA) {
			return 0;
		}
		if (startLineA <= startLineB) {
			if (endLineA >= endLineB) {
				sim = 2.0 * (double) (endLineB - startLineB) / (double) (lineA + lineB);
			} else if (endLineA < endLineB) {
				sim = 2.0 * (double) (endLineA - startLineB) / (double) (lineA + lineB);
			}
		}
		if (startLineA > startLineB) {
			if (endLineA <= endLineB) {
				sim = 2.0 * (double) (endLineA - startLineA) / (double) (lineA + lineB);
			} else if (endLineA < endLineB) {
				sim = 2.0 * (double) (endLineB - startLineA) / (double) (lineA + lineB);
			}
		}
		return sim;
	}

	/**
	 * <p>
	 * Added/Deletedブロックの分類
	 * </p>
	 *
	 * @param file
	 *            ブロック分類を行うソースファイル
	 */
	private static ArrayList<Block> categorizeAddedDeleted(SourceFile file, ArrayList<Block> updatedBlockList) {

		// Addedブロックの分類
		for (Block block : file.getNewBlockList()) {
			if (block.getCategory() == Block.NULL) {
				block.setCategory(Block.ADDED);
				updatedBlockList.add(block);
				//				System.out.println("Block ADDED = filename " + block.getFileName() + "start line =  " + block.getStartLine() + "end line = " + block.getEndLine());
			}
		}

		// Deletedブロックの分類
		for (Block block : file.getOldBlockList()) {
			if (block.getCategory() == Block.NULL) {
				block.setCategory(Block.DELETED);
				updatedBlockList.add(block);
				//				System.out.println("Block DELETED = filename " + block.getFileName() + "start line =  " + block.getStartLine() + "end line = " + block.getEndLine());
			}
		}
		return updatedBlockList;
	}
}
