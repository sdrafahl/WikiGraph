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
import java.util.Stack;

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
	
	public void maxOutDegree() {
		Collection col = this.hash.values();
		Object[] nodes =  col.toArray();
		Node temp = (Node) nodes[0];
		int max = this.outDegree(temp.name);
		ArrayList<String> minset = new ArrayList<String>();
		String link = temp.name;
		minset.add(link);
		for(int x=0;x<nodes.length;x++) {
			temp = (Node) nodes[x];
			if(max < this.outDegree(temp.name)) {
				minset.clear();
				minset.add(temp.name);
				max = this.outDegree(temp.name);
			} else if(max == this.outDegree(temp.name)) {
				minset.add(temp.name);
			}
		}
		System.out.println("Degree: " + max);
		for(int y=0;y<minset.size();y++) {
			System.out.println(minset.get(y));
		}
		System.out.println("END");
	}
	
	public void maxCentrality() {
		Collection col = this.hash.values();
		Object[] nodes =  col.toArray();
		Node temp = (Node) nodes[0];
		int max = this.centrality(temp.name);
		ArrayList<String> minset = new ArrayList<String>();
		ArrayList<String> cent = new ArrayList<String>();
		String link = temp.name;
		minset.add(link);
		for(int x=0;x<nodes.length;x++) {
			System.out.println(x);
			temp = (Node) nodes[x];
			int val = this.centrality(temp.name);
			cent.add(temp.name + ": " + val);
			if(max < val) {
				minset.clear();
				max = val;
				minset.add(temp.name);
			} else if(max == val) {
				minset.add(temp.name);
			}
		}
		System.out.println("The Max: " + max);
		for(int x=0;x<minset.size();x++) {
			System.out.println(minset.get(x));
		}
		for(int x=0;x<cent.size();x++) {
			System.out.println(cent.get(x));
		}
	}

	public ArrayList<String> bfsPath(String u, String v)
	{
		resetGraph();
		return this.dykstra(u, v);
	}
	
	private ArrayList<String> dykstra(String u, String v)
	{
		resetDistance();
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
		
		if(to.distance == this.max * 2) {
			return path;
		}
		
		Node toNode = (Node) this.hash.get(v);
		Node fromNode = (Node) this.hash.get(u);
		path.add(toNode.name);
		while(!toNode.name.equals(fromNode.name)) {
			if(toNode.inAdj.size() == 0) {
				path.clear();
				return path;
			}
			Node min = toNode.inAdj.get(0);
			ArrayList<Node> minSet = new ArrayList<Node>();
			for(int x=0;x<toNode.inAdj.size();x++) {
				if(min.distance > toNode.inAdj.get(x).distance) {
					min = toNode.inAdj.get(x);
					minSet.clear();
					minSet.add(min);
				} else if(min.distance == toNode.inAdj.get(x).distance) {
					minSet.add(toNode.inAdj.get(x));
				}
			}
			Node tempOld = toNode;
			toNode = min;
			
			if(minSet.size() > 1) {
				for(int x=0;x<minSet.size();x++) {
					if(!minSet.get(x).name.equals(toNode.name)) {
						this.updateIter(minSet.get(x));
						if(!tempOld.name.equals(v)) {
							tempOld.counted++;
						}
					}
				}
			}
			
			if(!toNode.name.equals(fromNode.name)) {
				toNode.counted++;
			}
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
			Object[] nodes1 = col1.toArray();
			boolean first = true;
			int submax = 0;
			for(int y=0;y<nodes1.length;y++) {
				Node n1 = (Node) nodes1[y];
				if(first) {
					this.resetDistance();
					this.dykstra(n.name, n1.name).size();
					//resetDistance();
					//this.bfsPathHelper(n.name, n1.name, null, null);
					first = false;
				}
				if(submax < n1.distance && n1.visited) {
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
		this.resetGraph();
		int maxDiameter = 0;
		Collection col = this.hash.values();
		Object[] nodes = col.toArray();
		for(int x=0;x<nodes.length;x++) {
			Collection col1 = this.hash.values();
			Object[] nodes1 = col1.toArray();
			for(int y=0;y<nodes1.length;y++) {
				Node n1 = (Node) nodes[y];
				Node n = (Node) nodes[x];
				Node temp = (Node) this.hash.get(v);
				System.out.println(n.name + " " + n1.name);
				this.dykstra(n.name, n1.name);
				System.out.println(temp.counted);
			}
		}
		Node temp = (Node) this.hash.get(v);
		return temp.counted;
	}
	
	private void updateCount(Node n) {
		if(n.distance == 0) {
			return;
		}
		
		int min = n.inAdj.get(0).distance;
		ArrayList<Node> minlist = new ArrayList<Node>();
		
		for(int x=0;x<n.inAdj.size();x++) {
			Node temp = n.inAdj.get(x);
			if(temp.distance == 0) {
				for(int y=0;y<minlist.size();y++) {
					minlist.get(y).counted--;
				}
				n.counted++;
				return;
			}
			if(min > n.distance) {
				min = n.distance;
				for(int y=0;y<minlist.size();y++) {
					minlist.get(y).counted--;
				}
				minlist.clear();
				minlist.add(n);
				n.counted++;
			} else {
				minlist.add(temp);
				if(temp.distance != 0) {
					temp.counted++;
				}
			}
		}
		for(int x=0;x<minlist.size();x++) {
			updateCount(minlist.get(x));
		}
	}
	
	private void updateIter(Node n) {
		Stack<Node> s = new Stack<Node>();
		s.push(n);
		while(n.distance != 0) {
			n = s.pop();
			int min = n.inAdj.get(0).distance;
			ArrayList<Node> minlist = new ArrayList<Node>();
			minlist.add(n.inAdj.get(0));
			for(int x=0;x<n.inAdj.size();x++) {
				Node temp = n.inAdj.get(x);
				if(temp.distance == 0) {
					for(int y=0;y<minlist.size();y++) {
						minlist.get(y).counted--;
					}
					//n.counted++;
					return;
				}
				if(min > temp.distance) {
					min = temp.distance;
					for(int y=0;y<minlist.size();y++) {
						minlist.get(y).counted--;
					}
					minlist.clear();
					minlist.add(temp);
					temp.counted++;
				} else if(temp.distance == min) {
					minlist.add(temp);
					if(temp.distance != 0) {
						temp.counted++;
					}
				}
			}
			for(int x=0;x<minlist.size();x++) {
				s.push(minlist.get(x));
			}
		}
	}
	
	private void resetGraph() {
		Collection col = this.hash.values();
		Object[] nodes =  col.toArray();
		for(int x=0;x<nodes.length;x++) {
			Node n = (Node) nodes[x];
			n.distance = 2 * max;
			n.visited = false;
			n.counted = 0;
			n.from = null;
		}
		
	}
	
	private void resetDistance() {
		Collection col = this.hash.values();
		Object[] nodes =  col.toArray();
		for(int x=0;x<nodes.length;x++) {
			Node n = (Node) nodes[x];
			n.distance = 2 * max;
			n.visited = false;
			n.from = null;
		}
	}
	
	class Node implements Comparable {
		
		boolean visited;
		 
		int distance;
		
		String name;
		
		ArrayList<Node> inAdj;
		
		ArrayList<Node> adj;
		
		Node from;
		
		int counted;
		
		public Node(String name, int sizeOfGraph) {
			this.name = name;
			this.distance = 2 * sizeOfGraph;
			this.visited = false;
			this.counted = 0;
			this.adj = new ArrayList<Node>();
			this.inAdj = new ArrayList<Node>();
			from = null;
		}

		@Override
		public int compareTo(Object o) {
			return this.distance - ((Node) o).distance;
		}
	}

}
