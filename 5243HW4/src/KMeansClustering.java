import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class KMeansClustering {
	HashMap<Integer, ArrayList<Integer>> data;
	int numCentroids;
	float convergenceThreshold;
	private HashMap<Integer, Set<String>> classifications;
	private int numOfTotalClass;
	
	public KMeansClustering(HashMap<Integer,ArrayList<Integer>> d, int c, float t, HashMap<Integer,Set<String>> topics, int totalClass) {
		data = d;
		numCentroids = c;
		convergenceThreshold = t;
		classifications = topics;
		numOfTotalClass = totalClass;
	}
	
	public void performClustering(boolean isEuc) {
		// Initialize the centroids to random spots
		HashMap<Integer,ArrayList<Float>> centroidLocations = new HashMap<Integer,ArrayList<Float>>();
		for (int i = 0; i < numCentroids; i++) {
			Integer centroidNum = i;
			// Get a random location
			Random myRandom = new Random();
			int value = myRandom.nextInt()%data.size();
			while (!data.containsKey(value)) {
				value = myRandom.nextInt()%data.size();
			}
			// Convert location to float
			ArrayList<Float> centroidLoc = new ArrayList<Float>();
			System.out.println(value);
			for (int j = 0; j < data.get(value).size(); j++) {
				float v = data.get(value).get(j);
				centroidLoc.add(v);
			}
			centroidLocations.put(centroidNum,centroidLoc);
		}
		
		// Container to hold the mapping of a document to a centroid
		HashMap<Integer,Integer> docToCentroid = new HashMap<Integer,Integer>();
		
		/** Perform k-means **/
		boolean locationsChanged = true;
		int iterations = 0;
		while (locationsChanged) {
			float totalError = 0;
			
			// Assign each of the data points to a centroid based on smallest distance
			Set<Integer> docNumbers = data.keySet();
			for (Integer i : docNumbers) {
				float smallestDistance = Float.MAX_VALUE;
				Integer smallestCentroid = 0;
				for (int j = 0; j < numCentroids; j++) {
					float currentDistance;
					if (isEuc) 
					{
						currentDistance= measureEuclidean(data.get(i),centroidLocations.get(j));
					}
					else
					{ 
						currentDistance= measureManhattan(data.get(i),centroidLocations.get(j));
					}
					if (currentDistance <= smallestDistance) {
						smallestDistance = currentDistance;
						smallestCentroid = j;
					}
				}
				docToCentroid.put(i,smallestCentroid);
				totalError += smallestDistance;
			}
			
			System.out.println("Total error: " + totalError);
			
			// Compute new centroid locations
			boolean atLeastOneChanged = false;
			int sizeOfVector = data.get(1).size();
			for (int i = 0; i < numCentroids; i++) {
				// Average all the data points assigned to the centroid
				int numAssignedPoints = 0;
				Float[] averageLocation = new Float[sizeOfVector];
				// Set to zero
				for (int z = 0; z < averageLocation.length; z++) {
					averageLocation[z] = (float) 0;
				}
				
				// Iterate through document numbers to determine which ones are assigned to the current centroid
				for (Integer j : docNumbers) {
					// Is the data point assigned to the centroid?
					if (docToCentroid.get(j) == i) {
						// Increase number of assigned points
						numAssignedPoints++;
						// Add to location data
						ArrayList<Integer> dataLoc = data.get(j);
						for (int k = 0; k < dataLoc.size(); k++) {
							averageLocation[k] += dataLoc.get(k);
						}
					}
				}
				
				System.out.println("Points associated with centroid " + i + " = " + numAssignedPoints);
				
				// Compute average
				ArrayList<Float> newCentroidLocation = new ArrayList<Float>();
				for (int j = 0; j < averageLocation.length; j++) {
					newCentroidLocation.add((float)averageLocation[j]/(float)numAssignedPoints);
				}
				
				// Check if centroid needs to be updated
				float difference;
				if (isEuc)
				{
					difference = measureEuclideanFloat(newCentroidLocation,centroidLocations.get(i));
				}
				else
				{
					difference = measureManhattanFloat(newCentroidLocation,centroidLocations.get(i));
				}
				System.out.println("Difference for centroid " + i + " = " + difference);
				if (difference > convergenceThreshold) {
					// Update centroid
					centroidLocations.remove(i);
					centroidLocations.put(i,newCentroidLocation);
					atLeastOneChanged = true;
				}
			}
			iterations++;
			System.out.println(iterations);
			if (!atLeastOneChanged) {
				locationsChanged = false;
			}
		}
		System.out.println("Number of iterations: " + iterations);
		
		// Map cluster to docs
		ArrayList<ArrayList<Integer>> papers = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < numCentroids; i++) {
			ArrayList<Integer> doc = new ArrayList<Integer>();
			Set<Integer> allDocs = docToCentroid.keySet();
			for (Integer d : allDocs) {
				if (docToCentroid.get(d) == i) {
					doc.add(d);
				}
			}
			papers.add(doc);
		}
		
		// To file map each centroid to the documents it is associated with
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("../output/KMeansClusters.txt"), "utf-8"));
			for (Integer i = 0; i < papers.size(); i++) {
				writer.write(i.toString());
				for (int j = 0; j < papers.get(i).size(); j++) {
					writer.write(",");
					writer.write(papers.get(i).get(j).toString());
				}
				writer.write("\n");
			}
			writer.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Get standard deviation and entropy
		ResultsComputer results = new ResultsComputer("../output/KMeansClusters.txt","../output/WordList-kmeans.csv");
		System.out.println("Standard deviation = " + results.getStandardDeviation());
		System.out.println("Entropy = " + results.getEntropy());
	}
	
	public void getResults() {
		
	}
	
	private float measureManhattan(ArrayList<Integer> one, ArrayList<Float> two) {
		float distance = 0;
		
		for (int i = 0; i < one.size(); i++) {
			distance += Math.abs((float)one.get(i) - two.get(i));
		}
		
		return distance;
	}
	
	private float measureManhattanFloat(ArrayList<Float> one, ArrayList<Float> two) {
		float distance = 0;
		
		for (int i = 0; i < one.size(); i++) {
			distance += Math.abs(one.get(i) - two.get(i));
		}
		
		return distance;
	}
	
	private float measureEuclidean(ArrayList<Integer> one, ArrayList<Float> two) {
		float distance = 0;
		
		for (int i = 0; i < one.size(); i++) {
			distance += Math.pow(one.get(i) - two.get(i), 2);
		}
		
		return distance;
	}
	
	private float measureEuclideanFloat(ArrayList<Float> one, ArrayList<Float> two) {
		float distance = 0;
		
		for (int i = 0; i < one.size(); i++) {
			distance += Math.pow(one.get(i) - two.get(i), 2);
		}
		
		return distance;
	}
}
