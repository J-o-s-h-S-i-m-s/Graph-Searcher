package perform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

import exceptions.CannotReadGraphFileException;
import exceptions.InvalidEdgeException;
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
 * @author Joshua Sims
 * @version 29 October 2016
 */
final class Graph 
{
	// Vertex constants
	/**
	 * The quantity of two vertices.
	 */
	private static final int TWO_VERTICES = 2;
	
	/**
	 * The index, in a two element long, adjacency array, of the vertex from
	 * which the adjacent vertex is reachable.
	 */
	private static final int FROM_VERTEX = 0;
	
	/**
	 * The index, in a two element long, adjacency array, of the vertex that
	 * is reachable by the other vertex.
	 */
	private static final int TO_VERTEX = 1;
	
	/**
	 * The lowest ID that a vertex may have.
	 */
	private static final int LOWEST_ID = 0;
		
	/**
	 * The index, in a two element long array, of the source vertex.
	 */
	private static final int SOURCE_VERTEX = 0;
	
	/**
	 * The index, in a two element long array, of the destination vertex.
	 */
	private static final int DEST_VERTEX = 1;
	
	// Output constants
	/**
	 * The index, in a two element long array, of the string which describes
	 * the order of discovery of vertices in the depth-first search.
	 */
	private static final int ORDER_OF_DISCOVERY = 0;
	
	/**
	 * The index, in a two element long array, of the string which describes
	 * the first discovered path form the source vertex to the destination
	 * vertex in the depth-first search.
	 */
	private static final int SOURCE_DEST_PATH = 1;
	
	// Data structures for graph
	/**
	 * The list of vertices which exist in the graph.
	 */
	private ArrayList<Vertex> vertexList;
	
	/**
	 * The list of vertices which exist in the graph with respect to their
	 * adjacent vertices.
	 */
	private ArrayList<ArrayList<Integer>> adjList;
	
	/**
	 * A two-dimensional array in which each element represents the existence
	 * of an adjacency from one vertex to another.
	 */
	private boolean[][] adjMatrix;
	
	// Constructors
	/** 
	 * Initializes the data structures, which represent the directed graph, 
	 * to null.
	 */
	Graph()
	{
		vertexList = null;
		adjList = null;
		adjMatrix = null;
	}
	
	// Methods
	/**
	 * Reads a specified text file which represents a directed graph, performs 
	 * a depth-first search from the specified source vertex to the specified 
	 * destination vertex, prints the order of discovery of the vertices and 
	 * the first discovered path to the destination vertex to the console if 
	 * the destination was found, determines the transitive closure of the 
	 * graph, prints the transitive closure edges to the console, determines if 
	 * the graph is cyclic, and prints to the console whether or not the graph 
	 * is cyclic.
	 * 
	 * @param graphFilePath - the path of the text file which represents the 
	 * directed graph
	 * 
	 * @throws CannotReadGraphFileException - if the specified text file which 
	 *     represents the directed graph could not be read
	 * @throws InvalidEdgeException - if the format of at least one edge, 
	 *     represented within the specified graph file, is invalid
	 * @throws InvalidSourceDestException - if either the specified source
	 *     vertex or the specified destination vertex is invalid
	 * @throws IOException - if an error related to an IO event disrupted this
	 *     program's execution
	 */
	void startGraph(String graphFilePath)
		throws 
		CannotReadGraphFileException, 
		InvalidEdgeException, 
		InvalidSourceDestException, 
		IOException
	{
		readInputGraph(graphFilePath);
		
		int[] sourceDest = readSourceDest();
		
		String[] sourceDestPathAndOrderOfDiscovery = 
			dfsSearch(sourceDest);
		String orderOfDiscovery = 
						sourceDestPathAndOrderOfDiscovery[ORDER_OF_DISCOVERY];
		String sourceDestPath = 
			sourceDestPathAndOrderOfDiscovery[SOURCE_DEST_PATH];
		
		String transitiveClosureEdges = transitiveClosure();
		
		boolean cycleExists = cycleSearch();
		
		printGraphStats(
			sourceDest,
			sourceDestPath, 
			orderOfDiscovery, 
			transitiveClosureEdges,
			cycleExists);
	}
	
	/**
	 * Reads the specified graph file and populates the vertex list, adjacency
	 * list, and adjacency matrix accordingly. 
	 * 
	 * @param graphFilePath - the path of the text file which represents the 
	 * directed graph
	 * 
	 * @throws CannotReadGraphFileException - if the specified text file which 
	 *     represents the directed graph could not be read
	 * @throws InvalidEdgeException - if the format of at least one edge, 
	 *     represented within the specified graph file, is invalid
	 * @throws IOException - if an error related to an IO event disrupted this
	 *     program's execution
	 */
	private void readInputGraph(String graphFilePath)
		throws CannotReadGraphFileException, InvalidEdgeException, IOException
	{	
		File graphFile = new File(graphFilePath);
		
		ArrayList<Integer[]> adjs = populateVertexListAndGatherAdjs(graphFile);
		
		populateAdjListAndAdjMatrix(adjs);
	}
	
	/**
	 * Populates the vertex list and gathers the adjacencies which will be used
	 * to populate the adjacency list and adjacency matrix.
	 * 
	 * @param graphFilePath - the text file which represents the directed 
	 *     graph
	 * 
	 * @return adjs - the adjacencies existent in the graph
	 * 
	 * @throws CannotReadGraphFileException - if the specified text file which 
	 *     represents the directed graph could not be read
	 * @throws InvalidEdgeException - if the format of at least one edge, 
	 *     represented within the specified graph file, is invalid
	 * @throws IOException - if an error related to an IO event disrupted this
	 *     program's execution
	 */
	private ArrayList<Integer[]> populateVertexListAndGatherAdjs(
		File graphFile)
		throws CannotReadGraphFileException, InvalidEdgeException, IOException
	{
		String edge = "";
		
		ArrayList<Integer[]> adjs = new ArrayList<Integer[]>();
		
		String[] adjVerticesIdsStrings = null;
		
		Integer fromVertexId = -1;
		Integer toVertexId = -1;
		
		Vertex fromVertex = null;
		Vertex toVertex = null;
		
		vertexList = new ArrayList<Vertex>();
		
		try (
			BufferedReader graphFileReader = new BufferedReader(
			new FileReader(graphFile));)
		{	
			while ((edge = graphFileReader.readLine()) != null)
			{
				adjVerticesIdsStrings = edge.split(" ");
				
				if (adjVerticesIdsStrings.length != TWO_VERTICES)
				{
					throw new InvalidEdgeException();
				}
				
				fromVertexId = 
					Integer.parseInt(adjVerticesIdsStrings[FROM_VERTEX]);
				toVertexId = 
					Integer.parseInt(adjVerticesIdsStrings[TO_VERTEX]);
				
				Integer[] adjVerticesIds = {fromVertexId, toVertexId};
				
				adjs.add(adjVerticesIds);
				
				fromVertex = new Vertex(fromVertexId, "white");
				toVertex = new Vertex(toVertexId, "white");

				if (vertexList.contains(fromVertex) == false)
				{
					vertexList.add(fromVertex);						
				}
				if (vertexList.contains(toVertex) == false)
				{
					vertexList.add(toVertex);						
				}
			}
		}
		catch (FileNotFoundException e)
		{
			throw new CannotReadGraphFileException();
		}
		catch (NumberFormatException e)
		{
			throw new InvalidEdgeException();
		}
			
		Collections.sort(vertexList);
		
		final int highestId = vertexList.size() - 1;
		if ((vertexList.get(LOWEST_ID).getID() < LOWEST_ID)
			|| (vertexList.get(highestId).getID() > highestId))
		{
			throw new InvalidEdgeException();
		}
		
		return adjs;
	}
	
	/**
	 * Populates the adjacency list and adjacency matrix according to the 
	 * adjacencies existent in the graph.
	 * 
	 * @param adjs - the adjacencies existent in the graph
	 */
	private void populateAdjListAndAdjMatrix(ArrayList<Integer[]> adjs)
	{
		int numOfVertices = vertexList.size();
		
		adjList = new ArrayList<ArrayList<Integer>>(numOfVertices);		
		for (
			int verticesToAdd = numOfVertices; 
			verticesToAdd > 0;
			--verticesToAdd)
		{
			adjList.add(new ArrayList<Integer>());
		}
		
		adjMatrix = new boolean[numOfVertices][numOfVertices];
		
		Integer fromVertexId = null;
		Integer toVertexId = null;
		ArrayList<Integer> fromVertexAdjs = null;
		for (Integer[] adj : adjs)
		{
			fromVertexId = adj[FROM_VERTEX];
			toVertexId = adj[TO_VERTEX];
			
			fromVertexAdjs = adjList.get(fromVertexId);
			if (fromVertexAdjs.contains(toVertexId) == false)
			{
				fromVertexAdjs.add(toVertexId);
			}
			
			adjMatrix[fromVertexId][toVertexId] = true;
		}
		
		for (ArrayList<Integer> fromVertexAdjsOfAdjList : adjList)
		{
			Collections.sort(fromVertexAdjsOfAdjList);
		}
	}
	
	/**
	 * Reads the specified source vertex and destination vertex from the user.
	 * 
	 * @return sourceDest - the source vertex and destination vertex relevant
	 *     to the depth-first search of the graph
	 *     
	 * @throws InvalidSourceDestException - if either the specified source
	 *     vertex or the specified destination vertex is invalid
	 */
	private int[] readSourceDest()
		throws InvalidSourceDestException
	{
		Scanner userInput = new Scanner(System.in);
		System.out.print("Enter a source vertex and a destination vertex: ");
		String[] maybeSourceDest = userInput.nextLine().split(" ");
		userInput.close();
		
		if (maybeSourceDest.length != TWO_VERTICES)
		{
			throw new InvalidSourceDestException();
		}
		
		Integer sourceVertexId = null;
		Integer destVertexId = null;
		
		try
		{
			sourceVertexId = Integer.parseInt(maybeSourceDest[SOURCE_VERTEX]);
			destVertexId = Integer.parseInt(maybeSourceDest[DEST_VERTEX]);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidSourceDestException();
		}
		
		final int highestId = vertexList.size() - 1;
		if (
			(sourceVertexId < LOWEST_ID) 
			|| (sourceVertexId > highestId)
			|| (destVertexId < LOWEST_ID) 
			|| (destVertexId > highestId))
		{
			throw new InvalidSourceDestException();
		}
		
		int[] sourceDest = {sourceVertexId, destVertexId};
		return sourceDest;
	}
	
	/**
	 * Performs a depth-first search on the graph, beginning from the specified
	 * source vertex. If the destination vertex is found, gathers two strings 
	 * representing the order of discovery of the vertices traversed and the
	 * first discovered path from the source vertex to the detination vertex. 
	 * 
	 * @param sourceDest - the source vertex and destination vertex relevant
	 *     to the depth-first search of the graph
	 *     
	 * @return orderOfDiscoveryAndSourceDestPath - An array of two strings 
	 *     representing the order of discovery of the vertices traversed and 
	 *     the first discovered path from the source vertex to the detination 
	 *     vertex
	 */
	private String[] dfsSearch(int[] sourceDest)
	{
		Integer sourceVertexId = sourceDest[SOURCE_VERTEX];
		Integer destVertexId = sourceDest[DEST_VERTEX];
		
		String sourceDestPath = "";
		String orderOfDiscovery = sourceVertexId + ", ";
		
		LinkedList<Integer> discovered = new LinkedList<Integer>();
		Integer discoveredVertexId = sourceVertexId;
		vertexList.get(sourceVertexId).setColor("black");
		discovered.push(discoveredVertexId);
		
		Integer examinedVertexId = null;
		
		Vertex adjVertex = null;
		
		boolean popExaminedVertex = true;
		
		while ((examinedVertexId = discovered.peek()) != null)
		{
			popExaminedVertex = true;
			
			examineAdjs:
			for (Integer adjVertexId : adjList.get(examinedVertexId))
			{
				adjVertex = vertexList.get(adjVertexId);
				
				examineColorOfAdj:
				switch (adjVertex.getColor())
				{
					case "white":
						orderOfDiscovery += adjVertexId + ", ";
						
						if (adjVertexId == destVertexId)
						{
							String[] sourceDestPathAndOrderOfDiscovery =
								assembleOrderOfDiscoveryAndSourceDestPath(
								orderOfDiscovery, 
								discovered, 
								destVertexId);
							return sourceDestPathAndOrderOfDiscovery;
						}
						
						adjVertex.setColor("black");
						discovered.push(adjVertexId);
						
						popExaminedVertex = false;
						break examineAdjs;
						
					case "black":
						break examineColorOfAdj;
				}
			}
			
			if (popExaminedVertex == true)
			{
				discovered.pop();
			}
		}
		
		String[] orderOfDiscoveryAndSourceDestPath = {"", "Not Found"};
		return  orderOfDiscoveryAndSourceDestPath;
	}
	
	/**
	 * Assembles and returns the two strings representing the order of 
	 * discovery of the vertices traversed and the first discovered path from 
	 * the source vertex to the detination vertex.
	 * 
	 * @param orderOfDiscovery - the order of discovery of the vertices 
	 *     traversed during the source to destination traversal
	 * @param discovered - the discovered vertices in order of most recently
	 *     discovered to least recently discovered
	 * @param destVertexId - the ID of the destination vertex
	 * 
	 * @return orderOfDiscoveryAndSourceDestPath - An array of two strings 
	 *     representing the order of discovery of the vertices traversed and 
	 *     the first discovered path from the source vertex to the detination 
	 *     vertex
	 */
	private String[] assembleOrderOfDiscoveryAndSourceDestPath(
		String orderOfDiscovery, LinkedList<Integer> discovered, 
		int destVertexId)
	{
		LinkedList<Integer> reversedDiscovered = 
			new LinkedList<Integer>();
			while (discovered.peek() != null)
			{
				reversedDiscovered.addFirst(discovered.pop());
			}
				
			String sourceDestPath = 
				reversedDiscovered.toString();
			sourceDestPath = 
				sourceDestPath.substring(
				1, sourceDestPath.length() - 1);
			sourceDestPath = 
				sourceDestPath.replace(",", " ->");
			sourceDestPath += 
				" -> " + destVertexId;
			
			orderOfDiscovery = 
				orderOfDiscovery.substring(
				0, orderOfDiscovery.length() - 2);
			
			String[] orderOfDiscoveryAndSourceDestPath = 
				{orderOfDiscovery, sourceDestPath};
			
			return orderOfDiscoveryAndSourceDestPath;
	}
	
	/**
	 * Determines the transitive closure of the graph and returns the string 
	 * describing the transitive closure edges.
	 * 
	 * @return transitiveClosureEdges - the transitive closure edges of the 
	 *     graph (does not include the original edges of the graph)
	 */
	private String transitiveClosure()
	{
		int numOfVertices = vertexList.size();

		boolean[][] transitiveClosureMatrix = 
			new boolean[numOfVertices][numOfVertices];
		
		for (int g = 0; g < numOfVertices; ++g)
		{
			for (int a = 0; a < numOfVertices; ++a)
			{
				transitiveClosureMatrix[g][a] = adjMatrix[g][a];
			}
		}
		
		for (int vertex = 0; vertex < numOfVertices; ++vertex)
		{
			for (int vertexFrom = 0; vertexFrom < numOfVertices; ++vertexFrom)
			{
				for (int vertexTo = 0; vertexTo < numOfVertices; ++vertexTo)
				{	
					if ((vertexFrom != vertex) && (vertexTo != vertex))
					{
						transitiveClosureMatrix[vertexFrom][vertexTo] = 
							(transitiveClosureMatrix[vertexFrom][vertexTo] 
							|| 
							(transitiveClosureMatrix[vertexFrom][vertex] 
							&& transitiveClosureMatrix[vertex][vertexTo]));						
					}
				}
			}
		}
		
		String transitiveClosureEdges = 
			assembleTransitiveClosureEdges(transitiveClosureMatrix);
		return transitiveClosureEdges;
	}
	
	/**
	 * Assembles and returns the string describing the transitive closure 
	 * edges.
	 * 
	 * @param transitiveClosureMatrix - a two-dimensional array in which each 
	 *     element represents the existence of transitive closure from one 
	 *     vertex to another.
	 *     
	 * @return transitiveClosureEdges - the transitive closure edges of the 
	 *     graph (does not include the original edges of the graph
	 */
	private String assembleTransitiveClosureEdges(
		boolean[][] transitiveClosureMatrix)
	{
		int numOfVertices = vertexList.size();
		
		String transitiveClosureEdges = "";
		String indentation = "                ";
		for (int n = 0; n < numOfVertices; ++n)
		{
			for (int m = 0; m < numOfVertices; ++m)
			{				
				if ((transitiveClosureMatrix[n][m] == true)
					&& (adjMatrix[n][m] == false))
				{
					if (transitiveClosureEdges != "")
					{
						transitiveClosureEdges += indentation;
					}
					transitiveClosureEdges += 	
						n + " " + m + "\n";
				}
			}
		}
		transitiveClosureEdges = 
			transitiveClosureEdges.substring(
			0, transitiveClosureEdges.length() - 1);
		
		return transitiveClosureEdges;
	}
	
	/**
	 * Determines if the graph is cyclic and returns the determination.
	 * 
	 * @return cycleExists - true if a cycle exists in the graph; false 
	 *     otherwise
	 */
	private boolean cycleSearch()
	{	
		// Clean up in preparation of the cycle search.
		for (Vertex vert : vertexList)
		{
			vert.setColor("white");
		}
				
		boolean cycleExists = false;
		
		LinkedList<Integer> discovered = new LinkedList<Integer>();
		discovered.push(LOWEST_ID);
		
		Integer examinedVertexId = null;
		Vertex adjVertex = null;
		boolean popExaminedVertex = true;
		while (discovered.isEmpty() == false)
		{
			examinedVertexId = discovered.peek();
			popExaminedVertex = true;
						
			examineAdjs:
			for (Integer adjVertexId : adjList.get(examinedVertexId))
			{
				adjVertex = vertexList.get(adjVertexId);
								
				examineColorOfAdj:
				switch (adjVertex.getColor())
				{
					case "white":
						adjVertex.setColor("grey");
						discovered.push(adjVertexId);
						
						popExaminedVertex = false;
						break examineAdjs;
					case "grey":
						cycleExists = true;
						return cycleExists;
					case "black":
						break examineColorOfAdj;
				}
			}
						
			if (popExaminedVertex == true)
			{
				vertexList.get(examinedVertexId).setColor("black");
				discovered.pop();
			}
		}
		
		cycleExists = false;
		return cycleExists;
	}
	
	/**
	 * Reads a specified text file which represents a directed graph, performs 
	 * a depth-first search from the specified source vertex to the specified 
	 * destination vertex, prints the order of discovery of the vertices and 
	 * the first discovered path between the source vertex and destination vertex to 
	 * the console if the destination was found, determines the transitive closure 
	 * of the graph, prints the transitive closure edges to the console, determines 
	 * if the graph is cyclic, and prints to the console whether or not the graph 
	 * is cyclic.
	 * 
	 * @param sourceDest - the source vertex and destination vertex relevant
	 *     to the depth-first search of the graph
	 * @param sourceDestPath - the first discovered path from the source vertex
	 *     to the detination vertex
	 * @param orderOfDiscovery - the order of discovery of the vertices 
	 *     traversed during the source to destination traversal
	 * @parm transitiveClosureEdges - the transitive closure edges of the graph 
	 *     (does not include the original edges of the graph)
	 * @param cycleExists - true if a cycle exists in the graph; false 
	 *     otherwise
	 */
	private void printGraphStats(
		int[] sourceDest,
		String sourceDestPath, 
		String orderOfDiscovery, 
		String transitiveClosureEdges,
		boolean cycleExists)
	{
		int source = sourceDest[SOURCE_VERTEX];
		int dest = sourceDest[DEST_VERTEX];
		
		String cycleExistsString = null;
		if (cycleExists == true)
		{
			cycleExistsString = "Cycle Exists";
		}
		else
		{
			cycleExistsString = "Cycle Does Not Exist";
		}
		
		if (sourceDestPath.equals("Not Found") == false)
		{
			System.out.println(
				"[DFS Discovered Vertices: " + 
				source + ", " + dest + "] " + orderOfDiscovery);
		}
		System.out.println(
			"[DFS Path: " + 
			source + ", " + dest + "] " + sourceDestPath);
		System.out.println(
			"[TC: New Edges] " + 
			transitiveClosureEdges);
		System.out.println(
			"[Cycle]: " + cycleExistsString);
	}
}
