// LEAVE THIS FILE IN THE DEFAULT PACKAGE
//  (i.e., DO NOT add 'package cs311.pa1;' or similar)

// DO NOT MODIFY THE EXISTING METHOD SIGNATURES
//  (you may, however, add additional methods and fields)

// DO NOT INCLUDE LIBRARIES OUTSIDE OF THE JAVA STANDARD LIBRARY
//  (i.e., you may include java.util.ArrayList etc. here, but not junit, apache commons, google guava, etc.)

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import javax.swing.text.html.HTMLDocument.Iterator;


public class GraphProcessor
{
	HashMap hash;
	
	int max;
	
	public GraphProcessor(String graphData)
	{
		File file = new File(graphData);
		if(file.exists()) {
			try {
				Scanner scan = new Scanner(file);
				String first = scan.nextLine();
				max = Integer.parseInt(first);
				hash = new HashMap();
				while(scan.hasNextLine()) {
					String line = scan.nextLine();
					Scanner scanLine = new Scanner(line);
					String from = scanLine.next();
					
					Node fromNode;
					if(hash.containsKey(from)) {
						fromNode = (Node) hash.get(from);
					} else {
						fromNode = new Node(from, max);
						hash.put(from, fromNode);
					}
					
					String vertice = scanLine.next();
					Node to;
					if(hash.containsKey(vertice)) {
						to = (Node) hash.get(vertice);
					} else {
						to = new Node(vertice, max);
						hash.put(vertice, to);
					}
					fromNode.adj.add(to);
					to.inAdj.add(fromNode);
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public int outDegree(String v)
	{
		Node n = (Node) hash.get(v);
		return n.adj.size();
	}

	public ArrayList<String> bfsPath(String u, String v)
	{
		resetGraph();
		PriorityQueue<Node> q = new PriorityQueue<Node>();
		Node from = (Node) this.hash.get(u);
		Node to = (Node) this.hash.get(v);
		from.distance = 0;
		q.add(from);
		
		
		while(!q.isEmpty()) {
			Node n = q.poll();
			n.visited = true;
			for(int x=0;x<n.adj.size();x++) {
				n.adj.get(x).distance = Math.min(n.adj.get(x).distance, n.distance + 1);
				if(!n.adj.get(x).visited) {
					q.add(n.adj.get(x));
					
				}
			}
		}
		
		ArrayList<String> path = new ArrayList<String>();
		Node toNode = (Node) this.hash.get(v);
		Node fromNode = (Node) this.hash.get(u);
		path.add(toNode.name);
		while(!toNode.name.equals(fromNode.name)) {
			Node min = toNode.inAdj.get(0);
			for(int x=0;x<toNode.inAdj.size();x++) {
				if(min.distance > toNode.inAdj.get(x).distance) {
					min = toNode.inAdj.get(x);
				}
			}
			toNode = min;
			path.add(toNode.name);
		}
		
		ArrayList<String> reversed = new ArrayList<String>();
		for(int x=path.size()-1;x >= 0; x--) {
			String temp = path.get(x);
			reversed.add(temp);
		}
		return reversed;
	}

	public int diameter()
	{
		Collection col = this.hash.values();
		Object[] nodes =  col.toArray();
		int maxDiameter = 0;
		for(int x=0;x<nodes.length;x++) {
			Node n = (Node) nodes[x];
			Collection col1 = this.hash.values();
			Object[] nodes1 =  (Node[]) col1.toArray();
			boolean first = true;
			int submax = 0;
			for(int y=0;y<nodes1.length;y++) {
				Node n1 = (Node) nodes1[y];
				if(first) {
					this.bfsPath(n.name, n1.name).size();
					first = false;
				}
				if(submax < n1.distance) {
					submax = n1.distance;
				}
			}
			if(maxDiameter < submax) {
				maxDiameter = submax;
			}
		}
		return maxDiameter;
	}

	public int centrality(String v)
	{
		int maxDiameter = 0;
		Collection col = this.hash.values();
		Node[] nodes =  (Node[]) col.toArray();
		for(int x=0;x<nodes.length;x++) {
			Collection col1 = this.hash.values();
			Node[] nodes1 =  (Node[]) col1.toArray();
			for(int y=0;y<nodes1.length;y++) {
				Node n1 = nodes[y];
				Node n = nodes[x];
				this.bfsPath(n.name, n1.name);
			}
		}
		Node temp = (Node) this.hash.get(v);
		return temp.counted;
	}
	
	private void resetGraph() {
		Collection col = this.hash.values();
		Object[] nodes =  col.toArray();
		for(int x=0;x<nodes.length;x++) {
			Node n = (Node) nodes[x];
			n.distance = 2 * max;
			n.visited = false;
			n.counted = 0;
		}
		
	}
	
	class Node implements Comparable {
		
		boolean visited;
		 
		int distance;
		
		String name;
		
		ArrayList<Node> inAdj;
		
		ArrayList<Node> adj;
		
		int counted;
		
		public Node(String name, int sizeOfGraph) {
			this.name = name;
			this.distance = 2 * sizeOfGraph;
			this.visited = false;
			this.counted = 0;
			this.adj = new ArrayList<Node>();
			this.inAdj = new ArrayList<Node>();
		}

		@Override
		public int compareTo(Object o) {
			return this.distance - ((Node) o).distance;
		}
	}

}
