package jp.ac.osaka_u.ist.sel.icvolti.analyze;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import jp.ac.osaka_u.ist.sel.icvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.icvolti.Config;
import jp.ac.osaka_u.ist.sel.icvolti.grammar.CPP14.CPP14Lexer;
import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;
import jp.ac.osaka_u.ist.sel.icvolti.model.Word;


public class CAnalyzer4 {

	private ArrayList<String> allWordList = new ArrayList<String>();
	static int blockId = 0;
	private static Block currentBlock;
	private static int p;
	private static final String[] controlFlow = { "if", "else", "switch", "case", "default", "for", "while", "do",
			"continue", "break", "return" };
	private static HashSet<String> controlFlowSet;

	public CAnalyzer4() {
		controlFlowSet = new HashSet<String>(Arrays.asList(controlFlow));
	}

	/**
	 * <p>
	 * ディレクトリ探索
	 * </p>
	 *
	 * @param file
	 * @throws IOException
	 */
	public static ArrayList<String> searchFiles(String pathname) {

		//	    if(cs.equals(StandardCharsets.UTF_16) ||
		//	    		cs.equals(StandardCharsets.UTF_8) {
		//
		//	    }
		ArrayList<String> fileList = new ArrayList<String>();
		File file = new File(pathname);
		if (file.isFile() && (file.getName().endsWith(".c") || file.getName().endsWith(".cpp"))) {
			Charset cs = null;
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file.getAbsoluteFile());
			} catch (FileNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			try {
				cs = charsetDetector.getCharsetName(fis);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
//			System.out.println("charset:" + cs);
			if(cs.equals(StandardCharsets.UTF_16) ||
					cs.equals(StandardCharsets.UTF_8) ||
					cs.equals(StandardCharsets.UTF_16BE) ||
					cs.equals(StandardCharsets.UTF_16LE) ||
					cs.equals(StandardCharsets.US_ASCII) ||
					cs.equals(StandardCharsets.ISO_8859_1) ) {
					fileList.add(file.getAbsolutePath());
			}

		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				fileList.addAll(searchFiles(f.getAbsolutePath()));
			}
		}
		return fileList;
	}

	public static final ArrayList<SourceFile> setFilesInfo(String PathName) {
		ArrayList<SourceFile> fileList = new ArrayList<SourceFile>();
		//	File file = new File(pathname);
		ArrayList<String> FileNameList = searchFiles(PathName);
		// ソースファイルの取得
		Iterator<String> it = FileNameList.iterator();
		int fileId = 0;
		//	System.out.print("file list start " );
		while (it.hasNext()) {
			String fileName = it.next();
			SourceFile file = new SourceFile();
			//	System.out.println("fileName = " + fileName);
			//		System.out.println("new path fileName = " +fileName);
			//file.setName(fileName);
			//		System.out.println("file liest now  id = " + fileId );
			//file.setNewPath(newPathName + "\\" + fileName);
			//file.setOldPath(oldPathName + "\\" + fileName);
			file.setNewPath(fileName);
			//file.setOldPath(oldPathName + "\\" + fileName);
			file.setId(fileId++);

			// 旧ファイルリストに含まれないファイルは新規追加分
			//			int index = oldFileNameList.indexOf(fileName);
			fileList.add(file);
		}
		// 残った旧ファイルは変更後に消えたもの
		return fileList;
	}

	/**
	 * <p>
	 * 単語リストの取得
	 * </p>
	 *
	 * @return
	 */
	public ArrayList<String> getWordList() {
		return allWordList;
	}

	public ArrayList<Block> analyzeFirst(ArrayList<SourceFile> fileList) {
		ArrayList<Block> blockList = new ArrayList<>();
		for (SourceFile file : fileList) {
			try {
				file.getNewBlockList().addAll(extractMethod(new File(file.getNewPath()), Block.NULL, file));
				blockList.addAll(file.getNewBlockList());
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(file + " : " + e);
			}
		}
		return blockList;
	}

//	public static void analyzeAFile(SourceFile file,ArrayList<Block> newBlockList ) {
	public static void analyzeAFile(SourceFile file ) {

		// 新しくファイルを解析するので，もとにあったblockのデータはnewBlockListから削除
		//この処理ってなぜ？
//		for(Block block : file.getNewBlockList()) {
//			int index = newBlockList.indexOf(block);
//			if(index > -1) {
//				newBlockList.remove(index);
//			}
//		}

		//System.out.println("new Block Size 3  = " + newBlockList.size());

//		System.out.println("fileName  = " + file.getNewPath());
		//新しくnweBlockListを作るので，前作ってたものを削除
//		file.getOldBlockList().clear();
//		file.setOldBlockList(file.getNewBlockList());
		//まえここでバグ
		file.getNewBlockList().clear();

		file.setState(SourceFile.MODIFIED);

		try {
			file.getNewBlockList().addAll(extractMethod(new File(file.getNewPath()), Block.NULL, file));
//			newBlockList.addAll(file.getNewBlockList());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(file + " : " + e);
		}
	}

	/**
	 * <p>
	 * ディレクトリ探索
	 * </p>
	 *
	 * @param file
	 * @throws IOException
	 */
	public void incrementalAnalyze(ArrayList<SourceFile> fileList) throws IOException {
//		ArrayList<Block> blockList = new ArrayList<>();
		CloneDetector.addedSourceFile=false;

		for (SourceFile file : fileList) {

			if(file.getState()==SourceFile.STABLE) {
				file.getOldBlockList().clear();
				for(Block block : file.getNewBlockList()) {
					block.setCategory(Block.NULL);
					Block oldBlock = new Block();
					oldBlock = block.clone();
					oldBlock.setCategory(Block.NULL);
					//					System.out.println("oldBlock vec " + oldBlock.getVector());
					//					System.out.println("oldBlock len " + oldBlock.getLen());
					file.getOldBlockList().add(oldBlock);
				}
				//file.getOldBlockList().addAll(file.getNewBlockList());
//				blockList.addAll(file.getNewBlockList());
				/*	for(Block block : blockList) {
					System.out.println("aaaa Block l====enn =     " + block.getLen());
				}*/
				//System.out.println(" Normal yade");
			}else {
				//新規追加されたソースファイル
				//				System.out.println("==============new File Analysis");
				try {
					file.getNewBlockList().addAll(extractMethod(new File(file.getNewPath()), Block.ADDED, file));
//					blockList.addAll(file.getNewBlockList());
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println(file + " : " + e);
				}

				CloneDetector.addedSourceFile=true;
			}
		}
//		return blockList;
	}

	/**
	 * <p>
	 * ソースファイルから関数を抽出する
	 * </p>
	 *
	 * @param file
	 * @throws Exception
	 * @throws IOException
	 */
	private static ArrayList<Block> extractMethod(File file, int category, SourceFile srcFile) throws Exception {
		ArrayList<Block> blockList = new ArrayList<>();
		String input = preProcessor(file);
		CharStream stream = CharStreams.fromString(input, file.toString());
		CPP14Lexer lexer = new CPP14Lexer(stream);
		lexer.removeErrorListeners();
		// lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();

		Token token = null;
		Token beforeToken = null;
		String methodName = null;
		int start = 0;
		p = 0;
		int numMethod = 0;
		while (p < tokens.size()) {
			token = tokens.get(p);
			switch (token.getType()) {
			case CPP14Lexer.LeftParen:
				if (beforeToken != null && beforeToken.getType() == CPP14Lexer.Identifier) {
					methodName = beforeToken.getText();
					start = token.getLine();
				} else {
					methodName = null;
				}
				break;
			case CPP14Lexer.LeftBrace:
				if (methodName != null && beforeToken.getType() == CPP14Lexer.RightParen && !methodName.equals("for")
				&& !methodName.equals("if") && !methodName.equals("switch") && !methodName.equals("while")) {
					Block block = new Block();
					currentBlock = block;
					// System.out.printf("%s - %s:
					// %d\r\n",file.toString(),methodName,start);
					block.setId(blockId++);
					block.setName(methodName);
					block.setFileName(file.toString());
					block.setStartLine(start);
					block.setMethodStartLine(start);
					blockList.add(block);
					block.setCategory(category);
					CloneDetector.countMethod++;
					numMethod++;
					int endPtr = p + blockLength(tokens, p);
					block.setEndLine(tokens.get(endPtr).getLine());
					block.setMethodEndLine(block.getEndLine());
					p++;
					blockList.addAll(extractBlock(tokens, block, category));

					methodName = null;
				}
				break;
				// case CPP14Lexer.ErrorCharacter:
				// if(true){
				// System.out.println(token.getChannel());
				// System.out.println(tokens.getText());
				// System.err.println(token.getInputStream().getSourceName()+"line
				// "+token.getLine()+":"+token.getCharPositionInLine()+" lexee error
				// "+token.getText());
				// }

			}
			beforeToken = token;
			p++;
		}
		srcFile.setNumMethod(numMethod);
		CloneDetector.countLine += token.getLine();
		return blockList;
	}




	// プリプロセッサ
	// マクロの除去
	private static String preProcessor(File file) throws Exception {



		List<String> lines = new ArrayList<>();
		try (Stream<String> stream = Files.lines(file.toPath(), Charset.forName(Config.charset))) {
			stream.forEach(line -> {
				line = line.replaceAll("\0", ""); // 制御文字 NULL文字 の削除
				line = line.replaceAll("\f", ""); // 制御文字 書式送り の削除
				line = line.replaceAll("\\$", "_"); // ドル文字 をアンダーバーに置換
				lines.add(line);
			});
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		StringBuilder buf = new StringBuilder();
		for (Iterator<String> lineIter = lines.iterator(); lineIter.hasNext();) {
			String line = lineIter.next();
			int skipCount = 0;
			while (line.endsWith("\\")) {
				line = line.substring(0, line.length() - 1);
				line = line + lineIter.next();
				skipCount++;
			}
			if (line.startsWith("#else") || line.startsWith("#elif")) {
				line = lineIter.next();
				while (!line.startsWith("#endif")) {
					skipCount++;
					line = lineIter.next();
				}
				skipCount++;
				line = "";
			}

			if (line.startsWith("#")) {
				line = "";
			}

			buf.append(line);
			buf.append('\n');
			for (int i = 0; i < skipCount; i++)
				buf.append('\n');
		}

		return buf.toString();
	}

	private static int blockLength(CommonTokenStream tokens, int ptr) {
		Token token;
		int length = 1;
		int depth = 0;
		while (++ptr < tokens.size()) {
			token = tokens.get(ptr);
			switch (token.getType()) {
			case CPP14Lexer.LeftBrace:
				depth++;
				break;
			case CPP14Lexer.RightBrace:
				if (depth == 0)
					return length;
				depth--;
				break;
			}
			length++;
		}
		return --length;
	}

	private void ignoreComment(StreamTokenizer tokenizer) throws IOException {
		int beforeToken = 0;
		int token = 0;
		while ((token = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
			switch (token) {
			case '/':
				if (beforeToken == '*' || beforeToken == '/')
					return;
			case StreamTokenizer.TT_WORD:
				break;
			}
			beforeToken = token;
		}
	}

	/**
	 * <p>
	 * 関数からワードを抽出
	 * </p>
	 *
	 * @param tokenizer
	 * @param method
	 * @throws IOException
	 */
	private static ArrayList<Block> extractBlock(CommonTokenStream tokens, Block block, int category) {
		ArrayList<Block> blockList = new ArrayList<>();
		Token token;
		Token beforeToken = null;
		String blockName = null;

		// int beforeToken = 0;

		while (p < tokens.size()) {
			token = tokens.get(p);
			Block b = block;
			while (b != null) {
				b.incNodeNum();
				b = b.getParent();
			}

			switch (token.getType()) {
			case CPP14Lexer.LeftParen:
				if (beforeToken != null) {
					if (beforeToken.getType() == CPP14Lexer.If || beforeToken.getType() == CPP14Lexer.While
							|| beforeToken.getType() == CPP14Lexer.For || beforeToken.getType() == CPP14Lexer.Switch) {
						blockName = beforeToken.getText();
					}
				} else {
					blockName = null;
				}
				break;

			case CPP14Lexer.LeftBrace:

				Block parent = null;

				if (CloneDetector.enableBlockExtract && beforeToken != null) {
					if ((blockName != null && beforeToken.getType() == CPP14Lexer.RightParen)
							|| beforeToken.getType() == CPP14Lexer.Do) {
						if (beforeToken.getType() == CPP14Lexer.Do)
							blockName = "do-while";

						Block child = new Block();
						parent = block;
						child.setParent(parent);
						child.setId(blockId++);
						child.setName(parent.getName() + " - " + blockName);
						child.setFileName(parent.getFileName());
						child.setStartLine(token.getLine());
						child.setMethodStartLine(parent.getMethodStartLine());
						int endPtr = p + blockLength(tokens, p);
						child.setEndLine(tokens.get(endPtr).getLine());
						child.setMethodEndLine(parent.getMethodEndLine());
						child.setCategory(category);

						blockList.add(child);
						blockName = null;
						CloneDetector.countBlock++;

						block = child;
					}

				}

				p++;
				blockList.addAll(extractBlock(tokens, block, category));

				if (parent != null) {
					block.setEndLine(tokens.get(p).getLine());

					if (block.getParent() != null) {
						block.setMethodEndLine(block.getParent().getMethodEndLine());
					} else {
						block.setMethodStartLine(block.getStartLine());
						block.setMethodEndLine(block.getEndLine());
					}

					block = parent;
				}

				break;
			case CPP14Lexer.RightBrace:
				return blockList;
			case CPP14Lexer.Identifier:
				b = block;
				while (b != null) {
					String[] words = Word.separateIdentifier(token.getText());
					b.addWord(words);
					b = b.getParent();
				}
				break;
			default:
				if (token.getText().matches("[a-zA-Z]+")) {
					b = block;
					while (b != null) {
						b.addWord(token.getText());
						b = b.getParent();
					}

				}
				break;
			}
			beforeToken = token;
			p++;
		}
		return blockList;
	}


}
