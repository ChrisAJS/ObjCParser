
public class BatchComplexityResultFormatter
{
	private String mObjectBuffer = "{";
	
	private String mSeparator = "";
	
	public synchronized void addResult(String object)
	{
		mObjectBuffer += mSeparator + object;
		mSeparator = ",";
	}
	
	public String getFormattedResults()
	{
		return mObjectBuffer+"}";
	}
}
