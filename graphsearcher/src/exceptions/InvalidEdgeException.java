package exceptions;

/**
 * Indicates that the format of at least one edge, represented within the 
 * specified graph file, is invalid.
 * 
 * @author Joshua Sims
 * @version 29 October 2016
 */
public final class InvalidEdgeException extends Exception
{
	/**
	 * Initializes the exception with a helpful error message.
	 */
	public InvalidEdgeException()
	{
		super(
			"The format of at least one edge, represented within the " +
			"specified graph file, is invalid." + 
			"\nThe following is an example of the valid format: " +
			"\n1 3" +
			"\n2 0" +
			"\n4 5" +
			"\nEach vertex ID must be an integer between 0 and " +
			"(the number of vertices - 1)." +
			"\nRepetitions of adjacencies are not permitted.");
	}
}
