import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class HierarchicalClustering {
	HashMap<Integer, ArrayList<Integer>> data;
	int numOfClusters;
	
	public HierarchicalClustering(HashMap<Integer, ArrayList<Integer>> d, int n) {
		data = d;
		numOfClusters = n;
	}
	
	public void performClustering() {
		// Create a distance matrix between each of the data points
		ArrayList<ArrayList<Float>> distanceMatrix = new ArrayList<ArrayList<Float>>();
		
		// Get an ordered array of the document numbers
		ArrayList<Integer> docNums = new ArrayList<Integer>();
		Set<Integer> docNumbers = data.keySet();
		for (Integer i : docNumbers) {
			docNums.add(i);
		}
		
		// Populate the matrix
		System.out.println("Creating the distance matrix...");
		for (int i = 0; i < docNums.size(); i++) {
			System.out.println("Computing " + i + " out of " + docNums.size());
			// Compute distances
			ArrayList<Float> rowDist = new ArrayList<Float>();
			for (int j = 0; j < docNums.size(); j++) {
				float dist = measureEuclidean(data.get(docNums.get(i)),data.get(docNums.get(j)));
				rowDist.add(dist);
			}
			// Add the row
			distanceMatrix.add(rowDist);
		}
		
		System.out.println("Finished creating distance matrix; now performing clustering...");
		
		// Perform hierarchical clustering
		while (distanceMatrix.size() != numOfClusters) {
			System.out.println("Dimensions of distance matrix: " + distanceMatrix.get(0).size() + " x " + distanceMatrix.size());
			// Find the smallest distance and document indices
			int docOne = 0;
			int docTwo = 0;
			
			float smallestDistance = Float.MAX_VALUE;
			for (int i = 0; i < distanceMatrix.size()-1; i++) {
				for (int j = i+1; j < distanceMatrix.get(i).size(); j++) {
					float currentDistance = distanceMatrix.get(i).get(j);
					if (currentDistance <= smallestDistance) {
						smallestDistance = currentDistance;
						docOne = i;
						docTwo = j;
					}
				}
			}
			
			System.out.println(docOne);
			System.out.println(docTwo);
			
			if (docOne == docTwo) {
				System.out.println("Problem");
			}
			
			// Merge the two smallest distances
			// By row
			ArrayList<Float> mergedDists = new ArrayList<Float>();
			for (int i = 0; i < distanceMatrix.get(docOne).size(); i++) {
				Float smaller = distanceMatrix.get(docOne).get(i);
				if (distanceMatrix.get(docTwo).get(i) < smaller) {
					smaller = distanceMatrix.get(docTwo).get(i);
				}
				mergedDists.add(smaller);
			}
			// Remove the higher doc number first
			if (docOne > docTwo) {
				distanceMatrix.remove(docOne);
				distanceMatrix.remove(docTwo);
			} else {
				distanceMatrix.remove(docTwo);
				distanceMatrix.remove(docOne);
			}
			distanceMatrix.add(mergedDists);
			
			// Now by column
			for (int i = 0; i < distanceMatrix.size(); i++) {
				// Remove each row
				ArrayList<Float> row = distanceMatrix.remove(0);
				ArrayList<Float> replacementRow = new ArrayList<Float>();
				Float smaller = row.get(docOne);
				if (row.get(docTwo) < smaller) {
					smaller = row.get(docTwo);
				}
				for (int j = 0; j < row.size(); j++) {
					if (j == docOne || j == docTwo) {
						// Skip it
					} else {
						replacementRow.add(row.get(j));
					}
				}
				// Add the merged value
				replacementRow.add(smaller);
				// Add the replacement row to the matrix
				distanceMatrix.add(replacementRow);
			}
			
		}
	}
	
	private float measureManhattan(ArrayList<Integer> one, ArrayList<Integer> two) {
		float distance = 0;
		
		for (int i = 0; i < one.size(); i++) {
			distance += Math.abs(one.get(i) - two.get(i));
		}
		
		return distance;
	}
	
	private float measureEuclidean(ArrayList<Integer> one, ArrayList<Integer> two) {
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
	
	public float getError() {
		float error = 0;
		
		// TODO: What sort of error measurement? Entropy? Squared sums?
		
		return error;
	}
}
