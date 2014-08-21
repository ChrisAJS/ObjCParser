import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main
{

	private static HashMap<String, ComplexityResult>	complexityResults	= new HashMap<String, ComplexityResult>();

	public static void main(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			// Remove the .h or .m from the end of the filename to form the key
			// to the results
			// hashmap
			String resultKey = args[i].substring(0, args[i].length() - 2);

			ComplexityResult complexityResult = createOrReuseResult(resultKey);

			File sourceFile = new File(args[i]);
			String sourceName = sourceFile.getName();
			if (sourceName.endsWith(".m"))
			{
				List<Token> tokens = tokenise(args[i]);

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
						if (!token.getText().contains("\"" + sourceName.substring(0, sourceName.length() - 2) + ".h\""))
						{
							complexityResult.addDependency();
						}
					}
				}
			}
			else if (sourceName.endsWith(".h"))
			{
				try
				{
					ParserRuleContext tree = generateAST(getTokenStreamForFile(args[i]));

					if(foundSuperClass(tree))
					{
						complexityResult.addSuperClass();
					}					
				}
				catch (IOException e)
				{
					System.err.println("Couldn't generate AST for: " + sourceName);
					e.printStackTrace();
				}

			}
			else
			{
				System.err.println("Invalid sourcefile provided: " + sourceName);
			}
		}

		ComplexityResultJsonFormatter formatter = new ComplexityResultJsonFormatter();
		for (Entry<String, ComplexityResult> results : complexityResults.entrySet())
		{
			System.out.println(formatter.formatLexerResult(results.getValue()));
		}
	}

	private static boolean foundSuperClass(ParseTree branch)
	{
		if(branch.getPayload() instanceof ObjCParser.Superclass_nameContext)
		{
			return !branch.getText().equals("NSObject");
		}
		
		boolean result = false;
		
		for (int i = 0; i < branch.getChildCount(); i++)
		{
			if(foundSuperClass(branch.getChild(i)))
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

	private static List<Token> tokenise(String filename)
	{
		List<Token> result = null;
		try
		{
			result = getTokenStreamForFile(filename).getTokens();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return result;
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
