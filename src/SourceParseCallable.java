import java.util.concurrent.Callable;

public class SourceParseCallable implements Callable<ComplexityResult>
{
	private String	mFilename;

	public SourceParseCallable(String filename)
	{
		mFilename = filename;
	}

	@Override
	public ComplexityResult call() throws Exception
	{
		SourceFileComplexityParser parser = new SourceFileComplexityParser(mFilename);
		return parser.processFile();
	}
}