import java.util.ArrayList;


public final class ConsoleOutputWriter
{
	private ArrayList<ComplexityResult> complexityResults = new ArrayList<ComplexityResult>();
	
	public void addComplexityResult(ComplexityResult result)
	{
		complexityResults.add(result);
	}
	
	public void outputComplexityResults()
	{
		ComplexityResultJsonFormatter formatter = new ComplexityResultJsonFormatter();
		String result = "";
		if (complexityResults.size() == 1)
		{
			result = formatter.formatComplexityResult(complexityResults.get(0));
		}
		else
		{
			BatchComplexityResultFormatter batchFormatter = new BatchComplexityResultFormatter();
			for(ComplexityResult complexityResult : complexityResults)
			{
				batchFormatter.addResult(formatter.formatComplexityResult(complexityResult));
			}
			result = batchFormatter.getFormattedResults();
		}
		
		System.out.println(result);
	}
}
