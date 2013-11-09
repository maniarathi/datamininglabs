import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class HierarchicalClustering {
	private HashMap<Integer, ArrayList<Integer>> data;
	private HashMap<Integer, Set<String>> classifications;
	private int numOfTotalClass;

	private int numOfClusters;
	private ArrayList<ArrayList<Float>> distanceMatrix;
	ArrayList<Integer> docNums;
	
	public HierarchicalClustering(HashMap<Integer, ArrayList<Integer>> d, int n, HashMap<Integer, Set<String>> topics, int totalClass) {
		data = d;
		numOfClusters = n;
		classifications = topics;
		numOfTotalClass = totalClass;
	}
	
	public void createDistanceMatrixManhattan() {
		// Create a file to hold the contents of the distance matrix
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("../output/DistanceMatrixManhattan.txt"), "utf-8"));
			
			// Get an ordered array of the document numbers
			docNums = new ArrayList<Integer>();
			Set<Integer> docNumbers = data.keySet();
			for (Integer i : docNumbers) {
				docNums.add(i);
			}
			
			// Populate the matrix file
			System.out.println("Creating the Manhattan distance matrix...");
			for (int i = 0; i < docNums.size(); i++) {
				System.out.println("Computing " + i + " out of " + docNums.size());
				// Compute distances
				ArrayList<Float> rowDist = new ArrayList<Float>();
				for (int j = 0; j < docNums.size(); j++) {
					float dist = measureManhattan(data.get(docNums.get(i)),data.get(docNums.get(j)));
					rowDist.add(dist);
				}
				// Add the row to file
				StringBuffer line = new StringBuffer();
				for (int j = 0; j < rowDist.size(); j++) {
					line.append(rowDist.get(j));
					if (j != rowDist.size() - 1) {
						line.append(",");
					}
				}
				writer.write(line.toString());
				writer.newLine();
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createDistanceMatrixEuclidean() {
		// Create a file to hold the contents of the distance matrix
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("../output/DistanceMatrixEuclidean.txt"), "utf-8"));
			
			// Get an ordered array of the document numbers
			docNums = new ArrayList<Integer>();
			Set<Integer> docNumbers = data.keySet();
			for (Integer i : docNumbers) {
				docNums.add(i);
			}
			
			// Populate the matrix file
			System.out.println("Creating the Euclidean distance matrix...");
			for (int i = 0; i < docNums.size(); i++) {
				System.out.println("Computing " + i + " out of " + docNums.size());
				// Compute distances
				ArrayList<Float> rowDist = new ArrayList<Float>();
				for (int j = 0; j < docNums.size(); j++) {
					float dist = measureEuclidean(data.get(docNums.get(i)),data.get(docNums.get(j)));
					rowDist.add(dist);
				}
				// Add the row to file
				StringBuffer line = new StringBuffer();
				for (int j = 0; j < rowDist.size(); j++) {
					line.append(rowDist.get(j));
					if (j != rowDist.size() - 1) {
						line.append(",");
					}
				}
				writer.write(line.toString());
				writer.newLine();
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void performClustering() {
		System.out.println("Performing clustering...");
		
		// Get papers
		ArrayList<ArrayList<Integer>> papers = new ArrayList<ArrayList<Integer>>();
		for (int i=0; i<data.size(); ++i)
		{
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			tmp.add(docNums.get(i));
			papers.add(tmp);
		}
		
		// Perform hierarchical clustering
		while (distanceMatrix.size() != numOfClusters) {
			//System.out.println("Dimensions of distance matrix: " + distanceMatrix.get(0).size() + " x " + distanceMatrix.size());
			// Find the smallest distance and document indices
			int docOne = 0;
			int docTwo = 0;
			
			if (distanceMatrix.size() %500 == 0)
				System.out.println("Distance matrix size = " + distanceMatrix.size());
			
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
			
			//System.out.println(docOne);
			//System.out.println(docTwo);
			
			ArrayList<Integer> lst1 = papers.get(docOne);
			ArrayList<Integer> lst2 = papers.get(docTwo);
			lst1.addAll(lst2);
			
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
				papers.remove(docOne);
				distanceMatrix.remove(docTwo);
				papers.remove(docTwo);
			} else {
				distanceMatrix.remove(docTwo);
				papers.remove(docTwo);
				distanceMatrix.remove(docOne);
				papers.remove(docOne);
			}
			distanceMatrix.add(mergedDists);
			papers.add(lst1);
			
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
			
			if (papers.size() == 4 || papers.size() == 8 ||
					papers.size() == 16 || papers.size() == 32 ||
						papers.size() ==  64 || papers.size() == 128)
			{
				System.out.println("For class of " + papers.size());
				double totalEntropy = 0;
				double var = 0;
				double sumPower = 0;
				double sum =0;
				for (int i=0; i< papers.size(); ++i)
				{
					HashMap<String,Integer> counters = new HashMap<String, Integer>();
					Integer numOfTotal = 0;
					
					
					
					ArrayList<Integer> in_pap = papers.get(i);
					sumPower += Math.pow((double)in_pap.size(),2);
					sum += in_pap.size();
					for (Integer docId : in_pap)
					{
						Set<String> topics = classifications.get(docId);
						
						for (String topic:topics)
						{
							if (counters.containsKey(topic))
							{
								Integer num = counters.get(topic) +1;
								counters.remove(topic);
								counters.put(topic, num);
							}
							else
							{
								counters.put(topic, 1);
							}
							++numOfTotal;
						}
					}
					
					// Calculate entropy
					double entropy = 0;
					
					for (String key:counters.keySet())
					{
						Integer num = counters.get(key);
						entropy = entropy - (((double)num)/(double)numOfTotal)*Math.log(((double)num)/numOfTotal);
						//System.out.println("There are " + num + " papers in " + key + " cluster.");
					}
					
					totalEntropy = (double)numOfTotal/(double)numOfTotalClass *entropy;
					
				}
				System.out.println("The entropy is " + totalEntropy);
				// variance
				var = (sumPower / (double)papers.size()) - Math.pow((sum/(double)papers.size()), 2);
				System.out.println("The variance is " + Math.sqrt(var));
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
	
	public void readMatrixManhattan() {
		// Clear the distance matrix
		distanceMatrix = new ArrayList<ArrayList<Float>>();
		
		// Open file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader("../output/DistanceMatrixManhattan.txt"));
			// Parse each line
			String line = "";
			while ((line = br.readLine()) != null) {				
				// Separate line by commas
				String[] stringData = line.split(",");
				
				// Convert the string data into floats
				ArrayList<Float> floatData = new ArrayList<Float>();
				Integer docNum = 0;
				for (int i = 0; i < stringData.length; i++) {
					floatData.add(Float.parseFloat(stringData[i]));
				}
				
				// Add to distance matrix array
				distanceMatrix.add(floatData);
			}
			
			// Close file
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Output dimensions of the distance matrix
		System.out.println("Dimensions of distance matrix = " + distanceMatrix.get(0).size() + " x " + distanceMatrix.size());
	}
	
	public void readMatrixEuclidean() {
		// Clear the distance matrix
		distanceMatrix = new ArrayList<ArrayList<Float>>();
		
		// Open file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader("../output/DistanceMatrixEuclidean.txt"));
			// Parse each line
			String line = "";
			while ((line = br.readLine()) != null) {				
				// Separate line by commas
				String[] stringData = line.split(",");
				
				// Convert the string data into floats
				ArrayList<Float> floatData = new ArrayList<Float>();
				Integer docNum = 0;
				for (int i = 0; i < stringData.length; i++) {
					floatData.add(Float.parseFloat(stringData[i]));
				}
				
				// Add to distance matrix array
				distanceMatrix.add(floatData);
			}
			
			// Close file
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Output dimensions of the distance matrix
		System.out.println("Dimensions of distance matrix = " + distanceMatrix.get(0).size() + " x " + distanceMatrix.size());
	}
	
	public float getError() {
		float error = 0;
		
		// TODO: What sort of error measurement? Entropy? Squared sums?
		
		return error;
	}
}
