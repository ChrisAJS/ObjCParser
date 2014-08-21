

/**
 * This class represents a result of complexity calculations on an Objective-C source file
 * @author ChrisAJS
 *
 */
public class ComplexityResult
{
	private int numBranches = 0;
	private int numDependencies = 0;
	private int numSuperClasses = 0;
	private String filename;
	
	/**
	 * @param forFile The filename that this ComplexityResult object stores values for
	 */
	public ComplexityResult(String forFile)
	{
		filename = forFile;
	}
	
	/**
	 * Increments the number of branches found for the source file this {@link ComplexityResult} instance represents
	 */
	public void addBranch()
	{
		numBranches++;
	}
	
	/**
	 * Increments the number of dependencies found for the source file this {@link ComplexityResult} instance represents
	 */
	public void addDependency()
	{
		numDependencies++;
	}
	
	/**
	 * Increments the number of super classes found for the source file this {@link ComplexityResult} instance represents
	 */
	public void addSuperClass()
	{
		numSuperClasses++; 	
	}
	
	/**
	 * @return The number of branches 
	 */
	public int getBranchCount()
	{
		return numBranches;
	}
	
	/**
	 * @return The number of dependencies
	 */
	public int getDependencyCount()
	{
		return numDependencies;
	}
	
	/**
	 * @return The number of super classes
	 */
	public int getSuperClassCount()
	{
		return numSuperClasses;
	}
	
	/**
	 * @return The filename of the file this {@link ComplexityResult} represents
	 */
	public String getFilename()
	{
		return filename;
	}
}
