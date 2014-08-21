
public class AtomicJSONObject
{
	private String mObjectBuffer = "{";
	
	private String mSeparator = "";
	
	public synchronized void addObject(String object)
	{
		mObjectBuffer += mSeparator + object;
		mSeparator = ",";
	}
	
	public String getObjectBuffer()
	{
		return mObjectBuffer+"}";
	}
}
