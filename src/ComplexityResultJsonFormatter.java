
public final class ComplexityResultJsonFormatter
{
	public String formatComplexityResult(ComplexityResult complexity)
	{
		String result = "\""+complexity.getFilename()+"\":{";

		result += "\"filename\":\""+complexity.getFilename()+"\",";
		result += "\"num_branches\":"+complexity.getBranchCount()+",";
		result += "\"num_dependencies\":"+complexity.getDependencyCount()+",";
		result += "\"num_superclasses\":"+complexity.getSuperClassCount();
		
		result += "}";
		return result;
	}
}
