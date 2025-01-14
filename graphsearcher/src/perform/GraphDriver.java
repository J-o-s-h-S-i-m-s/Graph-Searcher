package perform;

import java.io.IOException;

import exceptions.CannotReadGraphFileException;
import exceptions.InvalidEdgeException;
import exceptions.InvalidNumOfCmdLineArgsException;
import exceptions.InvalidSourceDestException;

/**
 * Reads a specified text file which represents a directed graph, performs a
 * depth-first search from the specified source vertex to the specified 
 * destination vertex, prints the order of discovery of the vertices and the
 * first discovered path between the source vertex and destination vertex to 
 * the console if the destination was found, determines the transitive closure 
 * of the graph, prints the transitive closure edges to the console, determines 
 * if the graph is cyclic, and prints to the console whether or not the graph 
 * is cyclic.
 * 
 * <p>Wrapper class for Graph which is responsible for performing the purpose
 * of this program.
 * 
 * @author Joshua Sims
 * @version 29 October 2016
 */
public final class GraphDriver 
{
	// Usage message
	/**
	 * Indicates the command line parameters for this program.
	 */
	public static final String USAGE_MESSAGE = 
		"Usage: java perform/GraphDriver <graph_file_path>";
	
	// Possible numbers of command line arguments
	/**
	 * Indicates that one command line argument was specified.
	 */
	private static final int ONE_ARG = 1;
	
	// Indices of command line arguments
	/**
	 * The index of the command line argument which specifies the path of the 
	 * text file which represents the directed graph.
	 */
	private static final int INDEX_OF_GRAPH_FILE_PATH_ARG = 0;
	
	// Exit codes
	/**
	 * Exit code indicating that an invalid number of command line arguments
	 * was specified.
	 */
	private static final int INVALID_NUM_OF_CMD_LINE_ARGS = 1;
	
	/**
	 * Exit code indicating that the specified text file, which represents the 
	 * directed graph, cannot be read.
	 */
	static final int CANNOT_READ_GRAPH_FILE = 2;
	
	/**
	 * Exit code indicating that the specified text file does not represent
	 * the directed graph in the correct format.
	 */
	static final int INVALID_EDGE = 3;
	
	/**
	 * Exit code indicating that either the source vertex or the destination 
	 * vertex is not an integer between 0 and (the number of vertices - 1) or
	 * that the source and destination vertices were specified in an incorrect
	 * format.
	 */
	static final int INVALID_SOURCE_DEST = 4;
	
	/**
	 * Exit code indicating an error related to an IO event.
	 */
	static final int IO_EXCEPTION = 6;
	
	// Methods
	/**
	 * Reads a specified text file which represents a directed graph, performs 
	 * a depth-first search from the specified source vertex to the specified 
	 * destination vertex, prints the order of discovery of the vertices and 
	 * the first discovered path between the source vertex and destination 
	 * vertex to the console if the destination was found, determines the 
	 * transitive closure of the graph, prints the transitive closure edges to 
	 * the console, determines if the graph is cyclic, and prints to the 
	 * console whether or not the graph is cyclic.
	 * 
	 * <p>Wrapper method for startGraph which is responsible for performing 
	 * the purpose of this program.
	 * 
	 * @param args - <graph_file_path>
	 */
	public static void main(String[] args)
	{
		try
		{
			examineCmdLineArgs(args);
		}
		catch (InvalidNumOfCmdLineArgsException e)
		{
			System.err.println(e.getMessage());
			System.exit(INVALID_NUM_OF_CMD_LINE_ARGS);
		}
		
		Graph graph = new Graph();
		
		String graphFilePath = args[INDEX_OF_GRAPH_FILE_PATH_ARG];
		
		try
		{
			graph.startGraph(graphFilePath);
		}
		catch (CannotReadGraphFileException e)
		{
			System.err.println(e.getMessage());
			System.exit(CANNOT_READ_GRAPH_FILE);
		}
		catch (InvalidEdgeException e)
		{
			System.err.println(e.getMessage());
			System.exit(INVALID_EDGE);
		}
		catch (InvalidSourceDestException e)
		{
			System.err.println(e.getMessage());
			System.exit(INVALID_SOURCE_DEST);
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(IO_EXCEPTION);
		}
	}
	
	/**
	 * Examines the specified command line arguments for validity.
	 * 
	 * @param args - <graph_file_path>
	 * 
	 * @throws InvalidNumOfCmdLineArgsException - if more than 1 command line
	 *     argument was passed.
	 */
	private static void examineCmdLineArgs(String[] args)
		throws InvalidNumOfCmdLineArgsException
	{
		if (args.length != ONE_ARG)
		{
			throw new InvalidNumOfCmdLineArgsException();
		}
	}
}
