package jp.ac.osaka_u.ist.sel.icvolti;

import java.util.Comparator;

import jp.ac.osaka_u.ist.sel.icvolti.model.Block;

/**
 * <p>比較クラス</p>
 * @author h-honda
 */

public class MyComp implements Comparator<Block> {
    public int compare(Block cA, Block cB) {
        if(cA.getId() < cB.getId()) {
            return -1;
        } else if(cA.getId() > cB.getId()) {
            return 1;
        } else {


            return cA.getName().compareTo(cB.getName());
        }
    }
}