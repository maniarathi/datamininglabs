import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Lab3Loader {

	public static int numberOfCentroids = 4;
	
	// args: 
	// 1. kmeans or hir (k-means or hierarchical)
	// 2. euc or man (euclidian or manhattan)
	// 3. For kmeans - K 
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		// Open up the file WordCount.csv for reading
		try {
			// Container to hold document number against its coordinate
			HashMap<Integer,ArrayList<Integer>> data = new HashMap<Integer,ArrayList<Integer>>();
			HashMap<Integer, Set<String>> classifications = new HashMap<Integer, Set<String>>();
			int TotalClass = 0;
			
			// Open file for reading
			BufferedReader br = new BufferedReader(new FileReader("../output/WordCount.csv"));
			BufferedReader brTopic = new BufferedReader(new FileReader("../output/WordList.csv"));
			
			// Parse each line
			String line = "";
			while ((line = br.readLine()) != null) {
				// Separate line by commas
				String[] stringData = line.split(",");
				
				// Convert the string data into floats
				ArrayList<Integer> floatData = new ArrayList<Integer>();
				Integer docNum = 0;
				for (int i = 0; i < stringData.length; i++) {
					// First element is the document number
					if (i == 0) {
						docNum = Integer.parseInt(stringData[i]);
						System.out.println("Reading document number: " + docNum);
					} else {
						floatData.add(Integer.parseInt(stringData[i]));
					}
				}
				
				// Create a key value pair to enter into map
				data.put((Integer) docNum,floatData);
			}
			
			int i =0;
			while ((line = brTopic.readLine()) != null)
			{
				String[] stringData = line.split(",");
				
				System.out.println("Read topic of line " + ++i);
				
				int docId = Integer.parseInt(stringData[0]);
				
				if (classifications.containsKey(docId))
				{
					classifications.get(docId).add(stringData[stringData.length-1]);
				}
				else
				{
					Set<String> tmp = new HashSet<String>();
					tmp.add(stringData[stringData.length-1]);
					classifications.put(docId, tmp);
				}
				++TotalClass;
			}
			
			// Close file
			br.close();
			brTopic.close();
			
			// Perform K-means on the data collected
			if (args[0].equals("kmeans"))
			{
				int k = Integer.parseInt(args[2]);
				KMeansClustering cluster = new KMeansClustering(data,k,0);
				if (args[1].equals("euc"))
				{
					cluster.performClustering(true);
				}
				else
				{
					cluster.performClustering(false);
				}
			}
			
			if (args[0].equals("hir"))
			{
				// Perform Hierarchical clustering on the data collected
				HierarchicalClustering cluster = new HierarchicalClustering(data,4,classifications, TotalClass);
				if (args[1].equals("euc"))
				{
					cluster.createDistanceMatrixEuclidean();
					cluster.readMatrixEuclidean();
				}
				else
				{
					cluster.createDistanceMatrixManhattan();
					cluster.readMatrixManhattan();
				}
				
				cluster.performClustering();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Cannot find file WordCount.csv!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR: Failed to read a line from WordCount.csv!");
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total running time: " + ((float)(endTime - startTime)/1000.0));

	}

}
