package jp.ac.osaka_u.ist.sel.icvolti.trace;

import java.util.ArrayList;

import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;

/**
 * <p>
 * コードクローンの変更履歴分類クラス
 * </p>
 *
 * @author y-yuuki
 */
public class BlockCategorizer {

	/**
	 * <p>
	 * コードクローンの分類
	 * </p>
	 *
	 * @param fileList
	 *            ソースファイルリスト
	 */
	public void categorizeBlock(ArrayList<SourceFile> fileList) {

		for (SourceFile file : fileList) {
			if (file.getState() == SourceFile.NORMAL) {
				categorizeStableModified(file);
			}

			// 実行時点で, 新旧両方に存在するクローンは分類されているはず
			categorizeAddedDeleted(file);

		}
	}

	/**
	 * <p>
	 * Stable/Modifiedクローンの分類
	 * </p>
	 *
	 * @param file
	 *            クローン分類を行うソースファイル
	 */
	private void categorizeStableModified(SourceFile file) {

		for (Block blockA : file.getNewBlockList()) {

			int addedLineStart = 0;
			int addedLineEnd = 0;

			//2バージョン間に存在して，追加された行がなければ，ここのfor文は通らない
			//コードクローンの開始行/終了行までの追加行の計算
			for (int line : file.getAddedCodeList()) {
				if (line < blockA.getStartLine()) {
					addedLineStart++;
					addedLineEnd++;
				//コードクローン内に追加された行があれば，コードクローンの終了行をインクメント
				} else if (line <= blockA.getEndLine()) {
					addedLineEnd++;
				} else {
					break;
				}
			}

			for (Block blockB : file.getOldBlockList()) {

				int deletedLineStart = 0;
				int deletedLineEnd = 0;
				// コードクローンの開始行/終了行までの削除行の計算
				for (int line : file.getDeletedCodeList()) {
					//コードクローンがある場所よりまえで削除された行があれば，
					if (line < blockB.getStartLine()) {
						deletedLineStart++;
						deletedLineEnd++;
					//コードクローン内で削除された行があれば，
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
				if (sim >= 0.3) {
					if (blockA.getOldBlock() != null) {
						//blockA.getLocationSimilarity() デフォルト値は0
						if (sim > blockA.getLocationSimilarity()) {
							blockA.setOldBlock(blockB);
							blockA.setLocationSimilarity(sim);
							if (addedLineStart == addedLineEnd && deletedLineStart == deletedLineEnd) {
								blockA.setCategory(Block.STABLE);
							} else {
								blockA.setCategory(Block.MODIFIED);
							}
						}
						//上のifに入るのはどんな状況？基本的にはしたのelseにはいる？
					}else {
						blockA.setOldBlock(blockB);
						blockA.setLocationSimilarity(sim);
						if (addedLineStart == addedLineEnd && deletedLineStart == deletedLineEnd) {
							blockA.setCategory(Block.STABLE);
						} else {
							blockA.setCategory(Block.MODIFIED);
						}
					}
					if (blockB.getNewBlock() != null) {
						if (sim > blockB.getLocationSimilarity()) {
							blockB.setNewBlock(blockA);
							blockB.setLocationSimilarity(sim);
							if (addedLineStart == addedLineEnd && deletedLineStart == deletedLineEnd) {
								blockB.setCategory(Block.STABLE);
							} else {
								blockB.setCategory(Block.MODIFIED);
							}
						}
					}else {
						blockB.setNewBlock(blockA);
						blockB.setLocationSimilarity(sim);
						if (addedLineStart == addedLineEnd && deletedLineStart == deletedLineEnd) {
							blockB.setCategory(Block.STABLE);
						} else {
							blockB.setCategory(Block.MODIFIED);
						}
					}

/*
					if (!blockB.getBlockSet().getNewBlockSetList().contains(blockA.getBlockSet())) {
						blockB.getBlockSet().getNewBlockSetList().add(blockA.getBlockSet());
					}
					if (!blockA.getBlockSet().getOldBlockSetList().contains(blockB.getBlockSet())) {
						blockA.getBlockSet().getOldBlockSetList().add(blockB.getBlockSet());
					}*/
				}
			}
		}
	}

	private double calcurateLocationSimilarity(Block blockA, Block blockB, int startLineA, int endLineA, int startLineB,
			int endLineB) {
		double sim = 0.0;
		//クローンの行数計算
		int lineA = blockA.getEndLine() - blockA.getStartLine();
		int lineB = blockB.getEndLine() - blockB.getStartLine();

		//コードクローンが削除されている？
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
	 * Added/Deletedクローンの分類
	 * </p>
	 *
	 * @param file
	 *            クローン分類を行うソースファイル
	 */
	private void categorizeAddedDeleted(SourceFile file) {

		// Addedクローンの分類
		for (Block block : file.getNewBlockList()) {
			if (block.getCategory() == Block.NULL) {
				block.setCategory(Block.ADDED);
			}
		}

		// Deletedクローンの分類
		for (Block block : file.getOldBlockList()) {
			if (block.getCategory() == Block.NULL) {
				block.setCategory(Block.DELETED);
			}
		}
	}
}
