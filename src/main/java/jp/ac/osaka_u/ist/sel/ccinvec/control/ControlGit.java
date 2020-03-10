package jp.ac.osaka_u.ist.sel.ccinvec.control;

import java.io.IOException;

import jp.ac.osaka_u.ist.sel.ccinvec.Config;

public class ControlGit {
	public static void checkout(String dirPath, String commitId, Config config) {


		try {
			String cleanCmd = "git  --git-dir=" + dirPath +  "\\.git  --work-tree=" + dirPath  + " clean -f ";
			Runtime  cleanRuntime = Runtime.getRuntime();
			Process clean_p;
			clean_p = cleanRuntime.exec(cleanCmd);
			clean_p.waitFor();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		try {
			String addCmd = "git  --git-dir=" + dirPath +  "\\.git  --work-tree=" + dirPath  + " add . ";
			Runtime  addRuntime = Runtime.getRuntime();
			Process add_p;
			add_p = addRuntime.exec(addCmd);
			add_p.waitFor();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}



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


		if(config.getGitBranch() != null) {
			try {
				String checkoutIdCmd = "git  --git-dir=" + dirPath +  "\\.git  --work-tree=" + dirPath  + " checkout " + config.getGitBranch();
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
