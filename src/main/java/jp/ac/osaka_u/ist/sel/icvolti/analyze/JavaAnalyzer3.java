package jp.ac.osaka_u.ist.sel.icvolti.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;

import jp.ac.osaka_u.ist.sel.icvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.icvolti.Config;
import jp.ac.osaka_u.ist.sel.icvolti.grammar.Java.JavaLexer;
import jp.ac.osaka_u.ist.sel.icvolti.grammar.Java.JavaParser;
import jp.ac.osaka_u.ist.sel.icvolti.grammar.Java.JavaParser.CompilationUnitContext;
import jp.ac.osaka_u.ist.sel.icvolti.model.Block;
import jp.ac.osaka_u.ist.sel.icvolti.model.BlockFactory;
import jp.ac.osaka_u.ist.sel.icvolti.model.SourceFile;


public class JavaAnalyzer3 {

	// private ASTParser parser = ASTParser.newParser(AST.JLS4);
	private ArrayList<String> allWordList;
	private static int blockID;
	public static int countFiles;
	public static int countParseFiles;

	/**
	 * <p>
	 * コンストラクタ
	 * </p>
	 */
	public JavaAnalyzer3() {
		// parser.setBindingsRecovery(true);
		// parser.setStatementsRecovery(true);
		// parser.setResolveBindings(true);
		allWordList = new ArrayList<String>();
		blockID = 0;
		countFiles=0;
		countParseFiles=0;

	}

	/**
	 * <p>
	 * 単語リストの取得
	 * </p>
	 *
	 * @return
	 */
	public final ArrayList<String> getWordList() {
		return allWordList;
	}

	public static final ArrayList<String> searchFiles(String pathname) {
		ArrayList<String> fileList = new ArrayList<String>();
		File file = new File(pathname);
		if(file.isFile() && file.getName().endsWith(".java")) {
			fileList.add(file.getAbsolutePath());
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				fileList.addAll(searchFiles(f.getAbsolutePath()));
			}
		}
		return fileList;
	}

	public static final ArrayList<String> searchFilesName(String pathname) {
		ArrayList<String> fileList = new ArrayList<String>();
		File file = new File(pathname);
		if(file.isFile() && file.getName().endsWith(".java")) {
			fileList.add(file.getAbsolutePath().substring(pathname.length() + 1));
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				fileList.addAll(searchFilesName(f.getAbsolutePath().substring(pathname.length() + 1)));
			}
		}
		return fileList;
	}


	public static final ArrayList<SourceFile> setFilesInfo(String newPathName, String oldPathName ) {
		ArrayList<SourceFile> fileList = new ArrayList<SourceFile>();
		//	File file = new File(pathname);
		ArrayList<String> newFileNameList = searchFiles(newPathName);
		ArrayList<String> oldFileNameList = searchFiles(oldPathName);

		// ソースファイルの取得
		Iterator<String> it = newFileNameList.iterator();
		int fileId = 0;
		//System.out.print("file list start " );
		while (it.hasNext()) {
			String fileName = it.next();
			SourceFile file = new SourceFile();
			//	System.out.println("fileName = " + fileName);
			//	System.out.println("new path fileName = " +fileName);
			//file.setName(fileName);
			//		System.out.println("file liest now  id = " + fileId );
			//file.setNewPath(newPathName + "\\" + fileName);
			//file.setOldPath(oldPathName + "\\" + fileName);
			file.setNewPath(fileName);
			String oldPath = oldPathName + "\\" + fileName.substring(newPathName.length() + 1);
			file.setOldPath(oldPath);
			//file.setOldPath(oldPathName + "\\" + fileName);
			//		System.out.println( "old path file name = " + oldPathName + "\\" +fileName.substring(newPathName.length()+1));
			file.setId(fileId++);

			// 旧ファイルリストに含まれないファイルは新規追加分
			//			int index = oldFileNameList.indexOf(fileName);
			int index = oldFileNameList.indexOf(oldPath);
			if (index > -1) {
				file.setState(SourceFile.NORMAL);
				oldFileNameList.remove(oldPathName + "\\" + fileName.substring(newPathName.length()+1));
			} else {
				file.setState(SourceFile.ADDED);
			}
			fileList.add(file);
		}
		// 残った旧ファイルは変更後に消えたもの
		it = oldFileNameList.iterator();
		while (it.hasNext()) {
			String fileName = it.next();
			//	System.out.println("DELETED FILE = " + fileName);
			SourceFile file = new SourceFile();
			//file.setName(fileName);
			file.setOldPath(fileName);
			file.setId(fileId++);
			file.setState(SourceFile.DELETED);
			fileList.add(file);
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
	 * ディレクトリ探索
	 * </p>
	 *
	 * @param file
	 * @throws IOException
	 */
	public ArrayList<Block> analyze(ArrayList<String> fileList) throws IOException {
		ArrayList<Block> blockList = new ArrayList<>();

		for (String file : fileList) {
			//	System.out.println("analyze file  = " + file);
			countFiles++;
			CharStream stream = CharStreams.fromFileName(file, Charset.forName(Config.charset));
			JavaLexer lexer = new JavaLexer(stream);
			lexer.removeErrorListeners();
			// lexer.addErrorListener(SilentErrorListener.INSTANCE);

			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JavaParser parser = new JavaParser(tokens);

			// parser.addParseListener(new JavaMyListener());
			CompilationUnitContext tree = null;
			parser.removeErrorListeners();
			// parser.addErrorListener(SilentErrorListener.INSTANCE);

			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			try {
				tree = parser.compilationUnit(); // STAGE 1
			} catch (Exception ex) {
				System.out.println("try predictionMode LL");
				lexer = new JavaLexer(stream);
				lexer.removeErrorListeners();
				// lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
				tokens = new CommonTokenStream(lexer); // rewind input stream
				parser = new JavaParser(tokens);
				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				parser.removeErrorListeners();
				// parser.addErrorListener(ConsoleErrorListener.INSTANCE);
				try {
					tree = parser.compilationUnit(); // STAGE 2
					//					System.out.println("success");
				} catch (ParseCancellationException e) {
					System.err.println(file + " parse cancel");
					e.printStackTrace();
					continue;
				} catch (Exception e) {
					System.err.println(e);
					e.printStackTrace();
					continue;
				}catch( StackOverflowError e ) {
					e.printStackTrace();
				}
				// if we parse ok, it's LL not SLL
			} catch( StackOverflowError ex ) {
				ex.printStackTrace();
			}
			blockList.addAll(extractMethod(tree, parser, Block.NULL));
			countParseFiles++;
			tokens.fill();
			CloneDetector.countLine += tokens.LT(tokens.size()).getLine();

		}
		return blockList;
	}



	/**
	 * <p>
	 * ディレクトリ探索
	 * </p>
	 *
	 * @param file
	 * @throws IOException
	 */
//	public ArrayList<Block> incrementalAnalyze(ArrayList<SourceFile> fileList) throws IOException {
	public void incrementalAnalyze(ArrayList<SourceFile> fileList) throws IOException {
//		ArrayList<Block> blockList = new ArrayList<>();
		CloneDetector.addedSourceFile=false;

		for (SourceFile file : fileList) {
			countFiles++;

			if(file.getState()==SourceFile.NORMAL) {
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
				ArrayList<Block> blockListOfFile = new ArrayList<>();
				CharStream newstream = CharStreams.fromFileName(file.getNewPath(), Charset.forName(Config.charset));
				JavaLexer newlexer = new JavaLexer(newstream);
				newlexer.removeErrorListeners();
				// lexer.addErrorListener(SilentErrorListener.INSTANCE);
				CommonTokenStream newtokens = new CommonTokenStream(newlexer);
				JavaParser newparser = new JavaParser(newtokens);
				// parser.addParseListener(new JavaMyListener());
				CompilationUnitContext newtree = null;
				newparser.removeErrorListeners();
				// parser.addErrorListener(SilentErrorListener.INSTANCE);
				newparser.getInterpreter().setPredictionMode(PredictionMode.SLL);

				try {
					newtree = newparser.compilationUnit(); // STAGE 1
				} catch (Exception ex) {
					System.out.println("try predictionMode LL");
					newlexer = new JavaLexer(newstream);
					newlexer.removeErrorListeners();
					// lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
					newtokens = new CommonTokenStream(newlexer); // rewind input stream
					newparser = new JavaParser(newtokens);
					newparser.getInterpreter().setPredictionMode(PredictionMode.LL);
					newparser.removeErrorListeners();
					ex.printStackTrace();
					// parser.addErrorListener(ConsoleErrorListener.INSTANCE);
					try {
						newtree = newparser.compilationUnit(); // STAGE 2
						//						System.out.println("success");
					} catch (ParseCancellationException e) {
						System.err.println(file + " parse cancel");
						e.printStackTrace();
						continue;
					} catch (Exception e) {
						System.err.println(e);
						e.printStackTrace();
						continue;
					}catch( StackOverflowError e ) {
						e.printStackTrace();
						e.printStackTrace();
					}
					// if we parse ok, it's LL not SLL
				} catch( StackOverflowError ex ) {
					ex.printStackTrace();
					ex.printStackTrace();
				}
				CloneDetector.addedSourceFile=true;

				blockListOfFile = extractMethod(newtree,newparser, Block.ADDED);
//				blockList.addAll(blockListOfFile);
				file.getNewBlockList().addAll(blockListOfFile);
				countParseFiles++;
				newtokens.fill();
				CloneDetector.countLine += newtokens.LT(newtokens.size()).getLine();
			}
		}
	}



	/**
	 * <p>
	 * ディレクトリ探索
	 * </p>
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static void analyzeAFile(SourceFile file) throws IOException {

		// 新しくファイルを解析するので，もとにあったblockのデータはnewBlockListから削除
		//この処理ってなぜ？
//		for(Block block : file.getNewBlockList()) {
//			int index = newBlockList.indexOf(block);
//			if(index > -1) {
//				newBlockList.remove(index);
//			}
//		}
		//System.out.println("new Block Size 3  = " + newBlockList.size());

		//新しくnweBlockListを作るので，前作ってたものを削除
		file.getOldBlockList().clear();
		file.setOldBlockList(file.getNewBlockList());
		file.getNewBlockList().clear();

		file.setState(SourceFile.MODIFIED);


		countFiles++;
		CharStream stream = CharStreams.fromFileName(file.getNewPath(), Charset.forName(Config.charset));
		JavaLexer lexer = new JavaLexer(stream);
		lexer.removeErrorListeners();
		// lexer.addErrorListener(SilentErrorListener.INSTANCE);

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokens);

		// parser.addParseListener(new JavaMyListener());
		CompilationUnitContext tree = null;
		parser.removeErrorListeners();
		// parser.addErrorListener(SilentErrorListener.INSTANCE);

		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		try {
			tree = parser.compilationUnit(); // STAGE 1
		} catch (Exception ex) {
			System.out.println("try predictionMode LL");
			lexer = new JavaLexer(stream);
			lexer.removeErrorListeners();
			// lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
			tokens = new CommonTokenStream(lexer); // rewind input stream
			parser = new JavaParser(tokens);
			parser.getInterpreter().setPredictionMode(PredictionMode.LL);
			parser.removeErrorListeners();
			// parser.addErrorListener(ConsoleErrorListener.INSTANCE);
			try {
				tree = parser.compilationUnit(); // STAGE 2
				//					System.out.println("success");
			} catch (ParseCancellationException e) {
				System.err.println(file + " parse cancel");
				e.printStackTrace();

			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();

			} catch( StackOverflowError e ) {
				e.printStackTrace();
			}
			// if we parse ok, it's LL not SLL
		}
		//newBlockList.addAll(extractMethod(tree, parser));
		file.getNewBlockList().addAll(extractMethod(tree, parser, Block.NULL));
//		newBlockList.addAll(file.getNewBlockList());
		countParseFiles++;
		tokens.fill();

		CloneDetector.countLine += tokens.LT(tokens.size()).getLine();

	}





	/**
	 * <p>
	 * ディレクトリ探索
	 * </p>
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public ArrayList<Block> analyze_test_first(ArrayList<SourceFile> fileList) throws IOException {
		ArrayList<Block> blockList = new ArrayList<>();

		for (SourceFile file : fileList) {
			countFiles++;
			CharStream stream = CharStreams.fromFileName(file.getNewPath(), Charset.forName(Config.charset));
			JavaLexer lexer = new JavaLexer(stream);
			lexer.removeErrorListeners();
			// lexer.addErrorListener(SilentErrorListener.INSTANCE);

			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JavaParser parser = new JavaParser(tokens);

			// parser.addParseListener(new JavaMyListener());
			CompilationUnitContext tree = null;
			parser.removeErrorListeners();
			// parser.addErrorListener(SilentErrorListener.INSTANCE);

			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			try {
				tree = parser.compilationUnit(); // STAGE 1
			} catch (Exception ex) {
				System.out.println("try predictionMode LL");
				lexer = new JavaLexer(stream);
				lexer.removeErrorListeners();
				// lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
				tokens = new CommonTokenStream(lexer); // rewind input stream
				parser = new JavaParser(tokens);
				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				parser.removeErrorListeners();

				ex.printStackTrace();
				// parser.addErrorListener(ConsoleErrorListener.INSTANCE);
				try {
					tree = parser.compilationUnit(); // STAGE 2
					//					System.out.println("success");
				} catch (ParseCancellationException e) {
					System.err.println(file + " parse cancel");
					e.printStackTrace();
					continue;
				} catch (Exception e) {
					System.err.println(e);
					e.printStackTrace();
					continue;
				}catch( StackOverflowError e ) {
					System.err.println(e);
					e.printStackTrace();
					continue;
				}
				// if we parse ok, it's LL not SLL
			}
			file.getNewBlockList().addAll(extractMethod(tree, parser, Block.NULL));
			//	blockList.addAll(extractMethod(tree, parser));
			blockList.addAll(file.getNewBlockList());
			countParseFiles++;
			tokens.fill();

			CloneDetector.countLine += tokens.LT(tokens.size()).getLine();


		}
		return blockList;

	}



	/**
	 * <p>
	 * ソースコードテキスト取得
	 * </p>
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private char[] getCode(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String code = "";
		String line;
		while ((line = reader.readLine()) != null)
			code = code + "\n" + line;
		reader.close();
		return code.toCharArray();
	}

	/**
	 * <p>
	 * ASTから各メソッドのASTを構築
	 * </p>
	 *
	 * @param method
	 * @param node
	 * @param parent
	 * @param className
	 */
	private static ArrayList<Block> extractMethod(ParseTree tree, JavaParser parser, int category) {
		ArrayList<Block> blockList = new ArrayList<>();
		for (ParseTree t : XPath.findAll(tree, "//methodDeclaration", parser)) {
			Token start = null;
			CloneDetector.countMethod++;
			for (ParseTree subt : ((JavaParser.MethodDeclarationContext) t).children) {
				if (subt instanceof RuleContext) {
					if (subt instanceof JavaParser.MethodBodyContext) {
						if (subt.getSourceInterval().length() <= Config.METHOD_NODE_TH)
							break;

						Block block = BlockFactory.create(blockID++, start.getText(), parser, subt.getChild(0),
								JavaLexer.IDENTIFIER);
						block.setCategory(category);
						blockList.add(block);
						if (CloneDetector.enableBlockExtract)
							blockList.addAll(extractBlock(subt.getChild(0), parser, block, category));
					}
				} else {
					TerminalNode token = (TerminalNode) subt;
					if (token.getSymbol().getType() == JavaLexer.IDENTIFIER) {
						// System.out.println(token.getText());
						start = token.getSymbol();
					}
				}
			}
		}
		return blockList;
	}




	private static ArrayList<Block> extractBlock(ParseTree tree, JavaParser parser, Block parent, int category) {
		ArrayList<Block> blockList = new ArrayList<>();
		for (ParseTree t : XPath.findAll(tree, "/block/blockStatement/statement", parser)) {
			if (!(t.getChild(0) instanceof RuleContext)) {
				TerminalNode token = (TerminalNode) t.getChild(0);
				List<Integer> args = new ArrayList<Integer>();

				switch (token.getSymbol().getType()) {
				case JavaLexer.IF:
					args.add(2);
					if (t.getChildCount() == 5)
						args.add(4);
					break;

				case JavaLexer.FOR:
					args.add(4);
					break;

				case JavaLexer.WHILE:
					args.add(2);
					break;

				case JavaLexer.DO:
					args.add(1);
					break;

				case JavaLexer.SWITCH:
					args.add(2);
					break;
				}

				if (args.size() == 0)
					continue;

				for (Integer arg : args) {
					CloneDetector.countBlock++;
					if (t.getChild(arg).getSourceInterval().length() <= Config.BLOCK_NODE_TH)
						continue;
					if (t.getChild(arg).getChild(0) instanceof JavaParser.BlockContext) {
						if (t.getChild(arg - 1).getText().equals("else")) {
							token = (TerminalNode) t.getChild(arg - 1);
						}
						//Block block = BlockFactory.create(blockID++,
						Block block = BlockFactory.create(-1,
								parent.getName() + " - " + token.getSymbol().getText(), parser,
								t.getChild(arg).getChild(0), JavaLexer.IDENTIFIER);
						block.setCategory(category);
						block.setParent(parent);
						blockList.add(block);
						blockList.addAll(extractBlock(t.getChild(arg).getChild(0), parser, block, category));
					}
				}
			}
		}
		return blockList;
	}


}
