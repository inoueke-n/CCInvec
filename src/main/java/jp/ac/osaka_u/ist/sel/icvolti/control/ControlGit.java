package jp.ac.osaka_u.ist.sel.icvolti.control;

import java.io.IOException;

public class ControlGit {
	public static void checkout(String dirPath, String commitId) {


		try {
			String stashCmd = "git  --git-dir=" + dirPath +  "\\.git  --work-tree=" + dirPath  + " stash ";
			Runtime  stashRuntime = Runtime.getRuntime();
			Process stash_p;
			stash_p = stashRuntime.exec(stashCmd);
			stash_p.waitFor();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}


		try {
			String checkoutIdCmd = "git  --git-dir=" + dirPath +  "\\.git  --work-tree=" + dirPath  + " checkout " + commitId;
			Runtime  checkoutIdRuntime = Runtime.getRuntime();
			Process checkoutId_p;
			checkoutId_p = checkoutIdRuntime.exec(checkoutIdCmd);
			checkoutId_p.waitFor();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}
