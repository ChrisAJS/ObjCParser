import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class Main
{
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			System.err.println("Usage: objparser objcsource.m [objcsource2.m...]");
			System.exit(-1);
		}
		
		ExecutorService threadPool = Executors.newFixedThreadPool(determineThreadCount());
		ConsoleOutputWriter outputWriter = new ConsoleOutputWriter();
		ArrayList<Future<ComplexityResult>> futures = new ArrayList<Future<ComplexityResult>>();

		for (int i = 0; i < args.length; i++)
		{
			futures.add(threadPool.submit(new SourceParseCallable(args[i])));
		}

		for (Future<ComplexityResult> future : futures)
		{
			outputWriter.addComplexityResult(getComplexityResult(future));
		}

		threadPool.shutdown();
		outputWriter.outputComplexityResults();
		System.exit(0);
	}

	private static int determineThreadCount()
	{
		int result = 1;
		String numThreads = System.getProperty("numThreads");

		if (numThreads != null)
		{
			try
			{
				result = Integer.parseInt(numThreads);
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				System.err.println("numThreads must be an integer value. Defaulting to 1 thread");
			}
		}

		System.out.println("NUM THREADS: "+numThreads);
		
		return result;
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
