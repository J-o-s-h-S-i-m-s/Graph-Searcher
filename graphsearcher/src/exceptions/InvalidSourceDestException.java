package exceptions;

/**
 * Indicates that an invalid source vertex or an invalid destination vertex has
 * been specified.
 * 
 * @author Joshua Sims
 * @version 29 October 2016
 */
public final class InvalidSourceDestException extends Exception
{
	/**
	 * Initializes the exception with a helpful error message.
	 */
	public InvalidSourceDestException()
	{
		super(
			"You specified an invalid source vertex or an invalid " +
			"destination vertex." +
			"\nBoth vertices must be integers between 0 and (the number of " +
			"vertices - 1)." +
			"\nEnsure that the format is correct before entering the source " +
			"and destination vertices." +
			"\nThe valid format: <source_vertex> <destination_vertex>");
	}
}
