
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
/**
 * 
 * @author Tristan Chen-Cota
 *
 */
public class Gems {
    public static void main(String[] args) throws IOException{
    	//Enter path to Gems.txt here
    	String filePath = "/Users/tristan/Desktop/gemInputs/gems3.txt";
		
    	//File Reading
		File file = new File(filePath); 
		BufferedReader in = new BufferedReader(new FileReader(file)); 
		String str = in.readLine();
		
		//Reading first line of file
		String[] tokens = str.split(",");
		int gNum = Integer.parseInt(tokens[0]);		//Number of gems
		double r = Double.parseDouble(tokens[1]);	//Ratio of vertical velocity to maximum horizontal speed
		
		//Creating gem instances
		Gem[] gems = new Gem[gNum];
		int counter = 0;
	    while ((str = in.readLine()) != null) {
	    	tokens = str.split(",");
	    	double xValue = Double.parseDouble(tokens[0]);
	    	double yValue = Double.parseDouble(tokens[1]);
	    	int gemValue = Integer.parseInt(tokens[2]);
	    	gems[counter] = new Gem(xValue, yValue, gemValue);
	    	counter++;
	    }
	    in.close();
	    Arrays.sort(gems);

	    
	    //Creating paths to gems
	    Graph graph = new Graph(gNum);
	    Graph graphFinal = new Graph(gNum);
	    //Check if there is a viable path between two gems; if there is, then create it
	    for(int i = 0; i < gNum - 1; i++){
	    	for(int j = i+1; j < gNum; j++){
	    		if(check(gems[i].getX(), gems[i].getY(), gems[j].getX(), gems[j].getY(), r)){
	    			graphFinal.addEdge(i, j, gems[j].getValue());
	    		}
	    	}
	    }
	    
	    
	    int maxInteger = 0;		//The path with the greatest value
	    int maxIndex = 0;		//Gem number that has the path with the greatest value
	    ArrayList<Integer> longestPath;
	    //For every gem create a path, make a Directed Acyclic Graph
	    for(int x = 0; x < gNum; x++){
		    for(int i = 0; i < gNum - 1; i++){
		    	for(int j = i+1; j < gNum; j++){	//Starting at i+1 removes redundant paths between gems
		    		if(check(gems[i].getX(), gems[i].getY(), gems[j].getX(), gems[j].getY(), r)){
		    			graph.addEdge(i, j, gems[j].getValue());
		    		}
		    	}
		    }
		    //Create an edge between the first gem and itself to add a value
		    //Find the longest path for a gem
		    //Then check if this path has a greater value
	    	graph.addEdge(x, x, gems[x].getValue());
	    	graph.findLongestPath(x);
	    	if(graph.getMax() > maxInteger){
	    		maxInteger = graph.getMax();
	    		maxIndex = x;
	    	}
	    	//Create a new graph to find the most valuable path
	    	graph = new Graph(gNum);
	    }
	    
	    //Create an edge between the gem with the most valuable path and itself
	    graphFinal.addEdge(maxIndex, maxIndex, gems[maxIndex].getValue());
	    //Use the gem with the most valuable path
	    graphFinal.findLongestPath(maxIndex);
	    longestPath = graphFinal.getPath();
	    Collections.sort(longestPath);
	    
	    
	    //Print results to a file
	    PrintStream o = new PrintStream(new File("race.txt"));
	    System.setOut(o);
	    for(int i = 0; i < longestPath.size(); i++){
	    	System.out.println(Double.toString(gems[longestPath.get(i)].getX()) + "," + Double.toString(gems[longestPath.get(i)].getY()) + "," + Integer.toString(gems[longestPath.get(i)].getValue()));
	
	    }
	    
    }
    
    //Checks if it is possible to move between one gem and another
    public static boolean check(double x1, double y1, double x2, double y2, double r){
		boolean take;
		double height = y2 - y1;
		double ratio = 1/r;
		double checkX = height*ratio;	//How much you can move on the x-axis as you move up the y-axis
		if(x2 > x1){
			if((checkX + x1) >= x2){
				take = true;
			}
			else{
				take = false;
			}	
		}
		else if(x2 < x1){
			if((x1 - checkX) <= x2){
				take = true;
			}
			else{
				take = false;
			}
		}
		else{
			take = true;
		}
		return take;
	}
    
    public static class Graph{
        int V;
        int[][] matrix;
        int[] vertices;
        boolean[] visited;
        int[] distances;
        int[] previous;
        Stack<Integer> stack;
        int[] maxInt;
        int maxIndex;
        ArrayList<Integer> path;
        int max;

        //Initialize all arrays in the graph
        public Graph(int V){
            this.V = V;
            vertices = new int[V];
            visited = new boolean[V];
            previous = new int[V];
            distances = new int[V];
            matrix = new int[V][V];
            stack = new Stack<Integer>();
            maxInt = new int[V];
            //Add vertices for the number of gems
            for(int i = 0; i < V; i++){
                addVertex(i);
                distances[i] = Integer.MIN_VALUE;
                previous[i] = -1;
            }
        }

        public void addVertex(int name){
            vertices[name] = name;
        }
        
        //Create an edge between two points on the graph
        public void addEdge(int source, int destination, int value){
            matrix[source][destination] = value;
        }
        
        //Uses a topological sort to find the path through
        //the gems with the maximum value
        public void findLongestPath(int source){
        	//Start a topological sort
            for(int i = 0; i < V; i++){
                if(!visited[i]){
                    dfs(i);
                }
            }
            distances[source] = 0;
            updateMaxDistanceForAllAdjVertices();
            getDistances(source);
            getPath(source);
        }
        
        //Finds the index of the last gem in the path with the maximum value
        public void getDistances(int source){
            for(int x = 0; x < V; x++){
                int distance = distances[x];
                if(distance == Integer.MIN_VALUE){
                    maxInt[x] = 0;
                }else{
                    maxInt[x] = distance;
                }
            }
            max = 0;
            for(int i = 0; i < maxInt.length; i++){
            	if(maxInt[i] > max){
            		max = maxInt[i];
            		maxIndex = i;	//Index of the last gem in the max path
            	}
            }
        }
        
        //Returns the path with the maximum value
        public ArrayList<Integer> getPath(){
        	return path;
        }
        
        //Returns the maximum value found
        public int getMax(){
        	return max;
        }
        
        //Creates the path with the maximum value
        public void getPath(int source){
        	path = new ArrayList<Integer>();
        	if(maxIndex != source){
                int from = previous[maxIndex]; 	//Gem previous to the final gem in the path
                path.add(source);
                while(from != source){
                    path.add(from);
                    from = previous[from];
                }
                path.add(maxIndex);
        	}
        }
        
        //Calculate distances for all Gems connected directly or indirectly with the source
        public void updateMaxDistanceForAllAdjVertices(){
            while(!stack.isEmpty()){
                int from = stack.pop();
                if(distances[from] != Integer.MIN_VALUE){//
                    for(int adjacent = 0; adjacent < V; adjacent++){
                        if(matrix[from][adjacent] != 0){
                            if(distances[adjacent] < distances[from] + matrix[from][adjacent]){
                                previous[adjacent] = from;
                                distances[adjacent] = distances[from] + matrix[from][adjacent];
                            }
                        }
                    }
                }
            }
        }
        
        //Creates a topological sort
        public void dfs(int source){
            visited[source] = true;
            for(int i = 0; i < V; i++){
            	//Checks for redundancies
                if(matrix[source][i] != 0 && !visited[i]){
                    dfs(i);	//Recur for all adjacent vertices
                }
            }
            stack.push(source);
        }


    }

    public static class Gem implements Comparable<Object>{
    	double x;
    	double y;
    	int value;
    	public Gem(double x, double y, int value){
    		this.x = x;
    		this.y = y;
    		this.value = value;
    	}
    	public double getX(){
    		return x;
    	}
    	public double getY(){
    		return y;
    	}
    	public int getValue(){
    		return value;
    	}
    	public int compareTo(Object other){
    		Gem otherGem = (Gem) other;
    		if(y < otherGem.getY()){
    			return -1;
    		}
    		else if(y > otherGem.getY()){
    			return 1;
    		}
    		else{
    			return 0;
    		}	
    	}
    }
}
