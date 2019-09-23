package jp.ac.osaka_u.ist.sel.icvolti;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import jp.ac.osaka_u.ist.sel.icvolti.model.Block;

public class BlockUpdater {


		/**
	 * <p>
	 * ブロックリストをシリアライズ化
	 * <p>
	 * @param blocklist
	 */
	public void serializeBlockList(List<Block> blockList) {
		 try {
             ObjectOutputStream objOutStream =
             new ObjectOutputStream(
             new FileOutputStream("blockList.bin"));
             objOutStream.writeObject(blockList);
             objOutStream.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
	}


	/**
	 * <p>
	 * ブロックリストをデシリアライズ化
	 * <p>
	 * @param blocklist
	 */
	public static List<Block> deserializeBlockList(String blockListName) {
        try {
            ObjectInputStream objInStream
              = new ObjectInputStream(
                new FileInputStream(blockListName));

            List<Block> blockList = (List<Block>) objInStream.readObject();

            objInStream.close();

            return blockList;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
		return null;
	}

}
