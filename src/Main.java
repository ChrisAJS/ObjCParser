import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main
{

	public static void main(String[] args)
	{
		ExecutorService threadPool = Executors.newFixedThreadPool(8);
		ArrayList<Future<ComplexityResult>> futures = new ArrayList<Future<ComplexityResult>>();
		for (int i = 0; i < args.length; i++)
		{
			futures.add(threadPool.submit(new SourceParseCallable(args[i])));
		}

		String result = "";

		ComplexityResultJsonFormatter formatter = new ComplexityResultJsonFormatter();
		if (futures.size() == 1)
		{
			result += formatter.formatComplexityResult(getComplexityResult(futures.get(0)));
		}
		else
		{
			BatchComplexityResultFormatter resultFormatter = new BatchComplexityResultFormatter();
			for (Future<ComplexityResult> future : futures)
			{
				resultFormatter.addResult(formatter.formatComplexityResult(getComplexityResult(future)));
			}
			result = resultFormatter.getFormattedResults();
		}
		threadPool.shutdown();
		System.out.println(result);
	}

	private static ComplexityResult getComplexityResult(Future<ComplexityResult> future)
	{
		ComplexityResult result = null;
		try
		{
			result = future.get();
		}
		catch (InterruptedException e)
		{
			System.err.println("Interrupted whilst awaiting result");
		}
		catch (ExecutionException e)
		{
			System.err.println("Error occured parsing source file");
		}
		return result;
	}
}
