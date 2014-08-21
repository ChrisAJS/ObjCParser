import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main
{
	private static class ProcessRunnable implements Runnable
	{
		private String	mFilename;

		public ProcessRunnable(String filename)
		{
			mFilename = filename;
		}

		@Override
		public void run()
		{
			String resultKey = mFilename;
			ComplexityResult complexityResult = createOrReuseResult(resultKey);

			File sourceFile = new File(mFilename);
			File headerFile = new File(mFilename.substring(0, mFilename.length() - 2) + ".h");

			try
			{
				processFile(complexityResult, sourceFile);
				if(headerFile.exists())
				{
					processFile(complexityResult, headerFile);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			processedFilesLatch.countDown();
		}
	}
	
	private static AtomicJSONObject resultJson;
	private static CountDownLatch processedFilesLatch;
	private static HashMap<String, ComplexityResult>	complexityResults	= new HashMap<String, ComplexityResult>();
	
	private static Executor threadPool = Executors.newFixedThreadPool(4);

	public static void main(String[] args)
	{
		resultJson = new AtomicJSONObject();
		processedFilesLatch = new CountDownLatch(args.length);
		
		for (int i = 0; i < args.length; i++)
		{
			threadPool.execute(new ProcessRunnable(args[i]));
		}

		try
		{
			processedFilesLatch.await();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		String result = "";
		ComplexityResultJsonFormatter formatter = new ComplexityResultJsonFormatter();
		if(args.length == 1)
		{
			for (Entry<String, ComplexityResult> results : complexityResults.entrySet())
			{
				result += formatter.formatLexerResult(results.getValue());
			}
		}
		else
		{
			for (Entry<String, ComplexityResult> results : complexityResults.entrySet())
			{
				resultJson.addObject(formatter.formatLexerResult(results.getValue()));
			}
			result = resultJson.getObjectBuffer();
		}

		System.out.println(result);
		System.exit(0);
	}

	private static void processFile(ComplexityResult complexityResult, File sourceFile) throws IOException
	{
		CommonTokenStream tokenStream = getTokenStreamForFile(sourceFile.getAbsolutePath());
		List<Token> tokens = tokenStream.getTokens();

		for (Token token : tokens)
		{
			int type = token.getType();
			if (type == ObjCLexer.IF || type == ObjCLexer.CASE || type == ObjCLexer.FOR || type == ObjCLexer.WHILE || type == ObjCLexer.QUESTION || type == ObjCLexer.AND || type == ObjCLexer.OR)
			{
				complexityResult.addBranch();
			}
			else if (type == ObjCLexer.IMPORT)
			{
				// Ensure we don't count the import of the sourcefile's
				// associated header file
				if (!token.getText().contains("\"" + sourceFile.getName().substring(0, sourceFile.getName().length() - 2) + ".h\""))
				{
					complexityResult.addDependency();
				}
			}
		}

		ParserRuleContext tree = generateAST(tokenStream);

		if (foundSuperClass(tree))
		{
			complexityResult.addSuperClass();
		}

	}

	private static boolean foundSuperClass(ParseTree branch)
	{
		if (branch.getPayload() instanceof ObjCParser.Superclass_nameContext)
		{
			return !branch.getText().equals("NSObject");
		}

		boolean result = false;

		for (int i = 0; i < branch.getChildCount(); i++)
		{
			if (foundSuperClass(branch.getChild(i)))
			{
				result = true;
				break;
			}
		}
		return result;
	}

	private static ComplexityResult createOrReuseResult(String aResultKey)
	{
		ComplexityResult result = complexityResults.get(aResultKey);
		if (result == null)
		{
			result = new ComplexityResult(aResultKey);
			complexityResults.put(aResultKey, result);
		}
		return result;
	}

	private static ParserRuleContext generateAST(TokenStream tokenStream)
	{
		ObjCParser parser = new ObjCParser(tokenStream);
		return parser.lex();
	}

	private static CommonTokenStream getTokenStreamForFile(String filename) throws IOException
	{
		ObjCLexer objCLexer = new ObjCLexer(null);
		objCLexer.setInputStream(new ANTLRFileStream(filename));
		CommonTokenStream cts = new CommonTokenStream(objCLexer);
		cts.fill();
		return cts;
	}

}
