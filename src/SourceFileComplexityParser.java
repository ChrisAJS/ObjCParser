import java.io.File;
import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;


public class SourceFileComplexityParser
{
	private File mSourceFile;
	private File mHeaderFile;
	
	public SourceFileComplexityParser(String aFilename)
	{
		mSourceFile = new File(aFilename);
		mHeaderFile = new File(aFilename.substring(0, aFilename.length()-2)+".h");
	}
	
	public ComplexityResult processFile() throws IOException
	{
		ComplexityResult result = new ComplexityResult(mSourceFile.getPath());
		
		processFile(result, mSourceFile);
		if(mHeaderFile.exists())
		{
			processFile(result, mHeaderFile);
		}
		return result;
	}
	
	private void processFile(ComplexityResult complexityResult, File sourceFile) throws IOException
	{
		CommonTokenStream tokenStream = getTokenStreamForFile(sourceFile.getAbsolutePath());
		List<Token> tokens = tokenStream.getTokens();

		for (Token token : tokens)
		{
			int type = token.getType();
			if (type == ObjCLexer.IF || 
					type == ObjCLexer.CASE ||
					type == ObjCLexer.FOR || 
					type == ObjCLexer.WHILE || 
					type == ObjCLexer.QUESTION || 
					type == ObjCLexer.AND || 
					type == ObjCLexer.ELSE ||
					type == ObjCLexer.OR)
			{
				complexityResult.addBranch();
			}
			else if (type == ObjCLexer.IMPORT)
			{
				// Ensure we don't count the import of the sourcefile's
				// associated header file
				if (!token.getText().contains("\"" + mHeaderFile.getName()+"\""))
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

	private ParserRuleContext generateAST(CommonTokenStream tokenStream)
	{
		return new ObjCParser(tokenStream).lex();
	}

	private CommonTokenStream getTokenStreamForFile(String filename) throws IOException
	{
		ObjCLexer objCLexer = new ObjCLexer(new ANTLRFileStream(filename));
		CommonTokenStream cts = new CommonTokenStream(objCLexer);
		cts.fill();
		return cts;
	}
}
	