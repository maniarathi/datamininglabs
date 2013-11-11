import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class ResultsComputer {
	private String clustersFile;
	private String topicsFile;
	private HashMap<Integer,ArrayList<Integer>> clusterToDocs;
	private HashMap<Integer,ArrayList<String>> docToTopics;
	
	public ResultsComputer(String cf, String tf) {
		clustersFile = cf;
		topicsFile = tf;
		clusterToDocs = new HashMap<Integer,ArrayList<Integer>>();
		docToTopics = new HashMap<Integer,ArrayList<String>>();
		
		/** Populate cluster to document mapping **/
		try {
			BufferedReader br = new BufferedReader(new FileReader(clustersFile));
			// Parse each line
			String line = "";
			while ((line = br.readLine()) != null) {
				// Separate line by commas
				String[] stringData = line.split(",");
				
				// Convert the string data into integers
				ArrayList<Integer> intData = new ArrayList<Integer>();
				Integer clusterNum = 0;
				for (int i = 0; i < stringData.length; i++) {
					// First element is the cluster number
					if (i == 0) {
						clusterNum = Integer.parseInt(stringData[i]);
						System.out.println("Reading cluster number: " + clusterNum);
					} else {
						intData.add(Integer.parseInt(stringData[i]));
					}
				}
				
				// Create a key value pair to enter into map
				clusterToDocs.put(clusterNum,intData);
			}
			// Close file
			br.close();
			System.out.println("Completed reading " + clustersFile);
			
			/** Populate the document to topics mapping **/
			br = new BufferedReader(new FileReader(topicsFile));
			// Parse each line
			line = "";
			while ((line = br.readLine()) != null) {
				// Separate line by commas
				String[] stringData = line.split(",");
				
				ArrayList<String> topics = new ArrayList<String>();
				Integer docNum = 0;
				for (int i = 0; i < stringData.length; i++) {
					// First element is the cluster number
					if (i == 0) {
						docNum = Integer.parseInt(stringData[i]);
						System.out.println("Reading topics for document number: " + docNum);
					} else {
						topics.add(stringData[i]);
					}
				}
				
				// Create a key value pair to enter into map
				docToTopics.put(docNum,topics);
			}
			// Close file
			br.close();
			System.out.println("Completed reading " + topicsFile);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public double getStandardDeviation() {
		double sd = 0;
		
		// Average the number of documents associated with each cluster
		int numberOfDocs = 0;
		Set<Integer> allClusters = clusterToDocs.keySet();
		for (Integer i : allClusters) {
			numberOfDocs += clusterToDocs.get(i).size();
		}
		float mean = (float)numberOfDocs/(float)allClusters.size();
		
		// Get the variance
		double variance = 0;
		for (Integer i : allClusters) {
			variance += Math.pow((clusterToDocs.get(i).size() - mean),2);
		}
		variance /= (double)allClusters.size();
		
		// Now use the variance to get standard deviation
		sd = Math.pow(variance,0.5);
		
		return sd;
	}
	
	public double getEntropy() {
		double entropy = 0;
		
		// Create a container to hold list of topics and number of occurences
		HashMap<String,Float> topicsMap = new HashMap<String,Float>();
		
		// Container to hold cluster entropies
		HashMap<Integer,Float> entropyOfCluster = new HashMap<Integer,Float>();
		
		// Total topics value
		float totalTopics = 0;
		
		// Iterate through the clusters
		Set<Integer> allClusters = clusterToDocs.keySet();
		System.out.println(allClusters.size());
		for (Integer i : allClusters) {
			// Get the documents associated with that cluster
			ArrayList<Integer> docs = clusterToDocs.get(i);
			for (int k = 0; k < docs.size(); k++) {
				int docNumber = docs.get(k);
				// For each document add the topic occurrence to the map
				ArrayList<String> topics = docToTopics.get(docNumber);
				//System.out.println(docToTopics.size());
				if (topics != null)
					for (int j = 0; j < topics.size(); j++) {
						
						if (topicsMap.containsKey(topics.get(j))) {
							// Topic already exists so just add the value
							float value = topicsMap.remove(topics.get(j));
							value += (float) ((1.0)/(topics.size()));
							totalTopics += (float) ((1.0)/(topics.size()));
							topicsMap.put(topics.get(j),value);
						} else {
							// Add the new topic
							float value = (float) ((1.0)/(topics.size()));
							totalTopics += (float) ((1.0)/(topics.size()));
							topicsMap.put(topics.get(j),value);
						}
				}
			}
			
			// Compute probabilities for topics
			Set<String> mappedTopics = topicsMap.keySet();
			HashMap<String,Float> newTopicsMap = new HashMap<String,Float>();
			for (String t : mappedTopics) {
				
				float value = topicsMap.get(t);
				float newValue = value/totalTopics;
				newTopicsMap.put(t,newValue);
			}
			
			// Compute entropy of each cluster
			float clusterEntropy = 0;
			for (String t : mappedTopics) {
				float prob = newTopicsMap.get(t);
				float thisEntropy = (float) (prob * (Math.log(prob)/Math.log(2)));
				clusterEntropy += thisEntropy;
			}
			entropyOfCluster.put(i,clusterEntropy);
		}
		
		// Compute weighted entropy
		for (Integer i : allClusters) {
			double weight = (double)clusterToDocs.get(i).size()/(double)docToTopics.size();
			entropy += weight*(double)entropyOfCluster.get(i);
		}
		
		return entropy * -1;	
	}
}
