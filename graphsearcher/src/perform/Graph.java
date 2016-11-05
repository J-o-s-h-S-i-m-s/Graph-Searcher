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
	private static final int TWO_VERTICES = 2;
	
	private static final int VERTEX_FROM = 0;
	
	private static final int VERTEX_TO = 1;
		
	private static final int VERTEX_LOWEST_ID = 0;
		
	private static final int SOURCE = 0;
	
	private static final int DEST = 1;
	
	private static final int ORDER_OF_DISCOVERY = 0;
	
	private static final int SOURCE_DEST_PATH = 1;
	
	private ArrayList<Vertex> vertexList;
	
	private ArrayList<ArrayList<Integer>> adjList;
	
	private boolean[][] adjMatrix;
	
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
	 * directed graph.
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
	
	
	private void readInputGraph(String graphFilePath)
		throws CannotReadGraphFileException, InvalidEdgeException, IOException
	{	
		File graphFile = new File(graphFilePath);
		
		ArrayList<Integer[]> adjs = populateVertexListAndGatherAdjs(graphFile);
		
		populateAdjListAndAdjMatrix(adjs);
	}
	
	private ArrayList<Integer[]> populateVertexListAndGatherAdjs(
		File graphFile)
		throws CannotReadGraphFileException, InvalidEdgeException, IOException
	{
		String edge = "";
		
		ArrayList<Integer[]> adjs = new ArrayList<Integer[]>();
		
		String[] adjVerticesIdsStrings = null;
		Integer[] adjVerticesIds = new Integer[TWO_VERTICES];
		
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
					Integer.parseInt(adjVerticesIdsStrings[VERTEX_FROM]);
				toVertexId = 
					Integer.parseInt(adjVerticesIdsStrings[VERTEX_TO]);
				
				adjVerticesIds[VERTEX_FROM] = fromVertexId;
				adjVerticesIds[VERTEX_TO] = toVertexId;
				
				adjs.add(adjVerticesIds);
				
				// TODO remove
				int debugA = adjs.get(adjs.size() - 1)[0];
				int debugB = adjs.get(adjs.size() - 1)[1];
				
//				// TODO remove
//				System.out.print(adjs.get(adjs.size() - 1)[0] + " ");
//				System.out.println(adjs.get(adjs.size() - 1)[1]);
				
//				// TODO remove
//				System.out.println(adjVerticesIds[0] + " " + adjVerticesIds[1]);
				
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
		
		final int vertexHighestID = vertexList.size() - 1;
		if ((vertexList.get(VERTEX_LOWEST_ID).getID() < VERTEX_LOWEST_ID)
			|| (vertexList.get(vertexHighestID).getID() > vertexHighestID))
		{
			throw new InvalidEdgeException();
		}
		
		// TODO remove
		for (int j = 0; j < adjs.size(); ++j)
		{
			System.out.println(adjs.get(j)[0] + " " + adjs.get(j)[1]);
		}
		
		return adjs;
	}
	
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
			fromVertexId = adj[VERTEX_FROM];
			toVertexId = adj[VERTEX_TO];
			
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
		
		// TODO keep?
//		if (vertexList.isEmpty())
//		{
//			System.err.println(GraphDriver.INVALID_GRAPH_FILE_FORMAT_MESSAGE);
//			System.exit(GraphDriver.INVALID_GRAPH_FILE_FORMAT);
//		}
	}
	
	private int[] readSourceDest()
		throws InvalidSourceDestException
	{
		Scanner userInput = new Scanner(System.in);
		System.out.print("Enter a source vertex and a destination vertex: ");
		String[] maybeSourceDest = userInput.nextLine().split(" ");
		
		if (maybeSourceDest.length != TWO_VERTICES)
		{
			throw new InvalidSourceDestException();
		}
		
		Integer sourceVertexId = null;
		Integer destVertexId = null;
		
		try
		{
			sourceVertexId = Integer.parseInt(maybeSourceDest[SOURCE]);
			destVertexId = Integer.parseInt(maybeSourceDest[DEST]);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidSourceDestException();
		}
		
		final int vertexHighestId = vertexList.size() - 1;
		if (
			(sourceVertexId < VERTEX_LOWEST_ID) 
			|| (sourceVertexId > vertexHighestId)
			|| (destVertexId < VERTEX_LOWEST_ID) 
			|| (destVertexId > vertexHighestId))
		{
			throw new InvalidSourceDestException();
		}
		
		int[] sourceDest = {sourceVertexId, destVertexId};
		
		return sourceDest;
	}
	
	private String[] dfsSearch(int[] sourceDest)
	{
		Integer sourceVertexId = sourceDest[SOURCE];
		Integer destVertexId = sourceDest[DEST];
		
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
			
			String[] sourceDestPathAndOrderOfDiscovery = 
				{orderOfDiscovery, sourceDestPath};
			
			return sourceDestPathAndOrderOfDiscovery;
	}
	
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
				// TODO remove
				System.out.print(adjMatrix[g][a] + " ");
			}
			// TODO remove
			System.out.println("");
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
	
	private boolean cycleSearch()
	{	
		// Clean up in preparation of the cycle search.
		for (Vertex vert : vertexList)
		{
			vert.setColor("white");
		}
				
		boolean cycleExists = false;
		
		LinkedList<Integer> discovered = new LinkedList<Integer>();
		discovered.push(VERTEX_LOWEST_ID);
		
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
	
	private void printGraphStats(
		int[] sourceDest,
		String sourceDestPath, 
		String orderOfDiscovery, 
		String transitiveClosureEdges,
		boolean cycleExists)
	{
		int source = sourceDest[SOURCE];
		int dest = sourceDest[DEST];
		
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