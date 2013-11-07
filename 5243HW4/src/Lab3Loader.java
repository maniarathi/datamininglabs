import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Lab3Loader {

	public static int numberOfCentroids = 4;
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		// Open up the file WordCount.csv for reading
		try {
			// Container to hold document number against its coordinate
			HashMap<Integer,ArrayList<Integer>> data = new HashMap<Integer,ArrayList<Integer>>();
			
			// Open file for reading
			BufferedReader br = new BufferedReader(new FileReader("../output/WordCount.csv"));
			
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
			
			// Close file
			br.close();
			
			// Perform K-means on the data collected
			KMeansClustering cluster = new KMeansClustering(data,64,0);
			cluster.performClustering();
			
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
