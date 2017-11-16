// LEAVE THIS FILE IN THE DEFAULT PACKAGE
//  (i.e., DO NOT add 'package cs311.pa1;' or similar)

// DO NOT MODIFY THE EXISTING METHOD SIGNATURES
//  (you may, however, add additional methods and fields)

// DO NOT INCLUDE LIBRARIES OUTSIDE OF THE JAVA STANDARD LIBRARY
//  (i.e., you may include java.util.ArrayList etc. here, but not junit, apache commons, google guava, etc.)

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WikiCrawler
{
	static int indexCount = 0;
	
	static final String BASE_URL = "https://en.wikipedia.org";
	
	static int requests = 0;
	
	int max;
	
	ArrayList<Node> vertices;
	
	String url;
	
	ArrayList<String> topics;
	
	String fileName;

	public WikiCrawler(String seedUrl, int max, ArrayList<String> topics, String fileName) {
		this.max = max;
		this.url = seedUrl;
		this.topics = topics;
		this.fileName = fileName;
		this.vertices = new ArrayList<Node>();
	}

	public ArrayList<String> extractLinks(String doc) {
		String subDoc = null;
		for(int x=0;x<doc.length();x++) {
			if(doc.charAt(x) == '<') {
				if(doc.charAt(x+1) == 'p' && doc.charAt(x+2) == '>') { 
					subDoc = doc.substring(x+3, doc.length());
					break;
				}
			}
		}
		ArrayList<String> urls = new ArrayList<String>();
		HashSet<String> hash = new HashSet<String>();
		Scanner scan = new Scanner(subDoc);
			String line = subDoc;
			Scanner words = new Scanner(line);
			while(words.hasNext()) {
				String word = words.next();
				String temp = "";
				if(word.contains("href") && word.contains("/wiki/")) {
					int first = 0;
					int last = 0;
					for(int x=0;x<word.length();x++) {
						if(word.charAt(x) == '"' && word.charAt(x-1) == '=') {
							first = x + 1;
						}
						if(word.charAt(x) == '"' && word.charAt(x-1) != '=') {
							last = x;
							break;
						}
					}
					String link = word.substring(first, last);
					if(!link.contains("#") && !link.contains(":") && !hash.contains(link)) {
						if(link.contains(".")) {
							if(link.contains(".com")) {
								urls.add(link);
								hash.add(link);
							}
						} else {
							urls.add(link);
							hash.add(link);
						}
					}
				}
			}
		return urls;
	}

	public void crawl() {
		HashMap h = new HashMap();
		PriorityQueue<Node> q=new PriorityQueue<Node>(); 
		try {
			String doc = getHTML(this.url);
			if(this.hasTopics(doc)) {
				Node root = new Node(doc);
				root.link = this.url;
				q.add(root);
				h.put(doc, root);
				while(!q.isEmpty()) {
					System.out.println("The Size of Que: " +  q.size());
					System.out.println("The number of pages: " + h.size());
					Node temp = q.poll();
					ArrayList<String> links = this.extractLinks(temp.doc);
					if(h.size() < this.max) {
						for(int x=0;x<links.size() && h.size() < this.max;x++) {
							String docTemp = this.getHTML(links.get(x));
							if(!h.containsKey(links.get(x)) && this.hasTopics(docTemp)) {
								Node n = new Node(docTemp);
								n.link = links.get(x);
								temp.adj.add(n);
								h.put(links.get(x), n);
								q.add(n);
							} else {
								Node neighbor = (Node) h.get(docTemp);
								System.out.println("Adding Neighbor: " + neighbor.link);
								if(!temp.link.equals(neighbor.link)) {
									temp.adj.add(neighbor);
								}
							}
						}
					} else {
						for(int x=0;x<links.size();x++) {
							if(h.containsKey(links.get(x)) && !temp.link.equals(links.get(x))) {
								Node neig = (Node) h.get(links.get(x));
								temp.adj.add(neig);
							}
						}
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		int count = 0;
		//while(true) {
			//File temp = new File("../../graph" + count + ".txt");
			File temp = new File("../../WikiISU.txt");
			if(!temp.exists()) {
				try {
					temp.createNewFile();
					PrintWriter writer = new PrintWriter(temp);
					writer.println(h.size());
					Collection col = h.values();
					Object[] nodes =  col.toArray();
					
					for(int x=0;x<nodes.length;x++) {
						Node tem = (Node) nodes[x];
						System.out.println(tem.link);
						for(int y=0;y<tem.adj.size();y++) {
							writer.println(tem.link + " " + tem.adj.get(y).link);
						}
					}
					writer.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//break;
			//} else {
			//	count++;
			//}
		}
		
		
	}
	
	private String getHTML(String url) throws IOException {
		if(requests == 50) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			requests = 0;
		}
		url = this.BASE_URL + url; 
		URL ur = new URL(url);
        URLConnection yc = ur.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            a.append(inputLine);
        in.close();
        requests++;
        return a.toString();
	}
	
	private boolean hasTopics(String doc) {
		for(int x=0;x<this.topics.size();x++) {
			if(!doc.contains(this.topics.get(x))) {
				return false;
			}
		}
		return true;
	}
	
	class Node implements Comparable {
		
		boolean visited;
		
		int index;
		
		String doc;
		
		ArrayList<Node> adj;
		
		String link;
		
		public Node(String doc) {
			this.index = indexCount;
			indexCount++;
			this.visited = false;
			this.doc = doc;
			this.adj = new ArrayList<Node>();
		}

		@Override
		public int compareTo(Object o) {
			Node n = (Node) o;
			return this.index - n.index;
		}
		
	}
}


