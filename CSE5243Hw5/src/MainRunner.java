import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


public class MainRunner {

	public static void main(String[] args) {
		//createKTransactionFiles("../output/KMeansClusters.txt");
		String fileName = "../output/WordList.csv";
		//String fileName = "../output/WordTest.csv";
		int NumOfTopics = 100;
		double Support = 0.1;
		double Confidence = 50;
		
		if (args.length > 0) {
			fileName = args[0];
			if (args.length > 1) {
				NumOfTopics = Integer.parseInt(args[1]);
				if (args.length > 2) {
					Support = Double.parseDouble(args[2]);
					if (args.length > 3) {
						Confidence = Double.parseDouble(args[3]);
					}
				}
			}	
		}
		//AR dm = new AR();
		//try
		//{
			//dm.Mine(fileName, 1, 1);
			
		
			// Prepare files
			FixFile(fileName,fileName+".fix");
			
			NumOfTopics = TopicMap.size();
			
			String[] params = new String[4];
			params[0] = "-F"+fileName+".fix"; // this is the training file name
			params[1] = "-N"+NumOfTopics; // We have 123 topics in our topic vector
			params[2] = "-S"+ Support;
			params[3] = "-C" + Confidence;
			AprioriTFP_CBA atc = new AprioriTFP_CBA(params);
			
			
			double time1 = (double) System.currentTimeMillis();
			
			atc.inputDataSet();
			atc.idInputDataOrdering();  // ClassificationAprioriT
			atc.recastInputData();      // AssocRuleMining
		
			// Create training data set (method in ClassificationAprioriT class)
			// assuming a 50:50 split
			atc.createTrainingAndTestDataSets();
		
			// Mine data, produce T-tree and generate CRs
			atc.startClassification();
			
			
			// Output 
			atc.outputDuration(time1,
					(double) System.currentTimeMillis());

			// Standard output
			atc.outputNumFreqSets();
			atc.outputNumUpdates();
			atc.outputStorage();
			atc.outputNumRules();
			double accuracy = atc.getAccuracy();
			System.out.println("Accuracy = " + twoDecPlaces(accuracy));
			double aucValue = atc.getAUCvalue();
			System.out.println("AUC value = " + fourDecPlaces(aucValue));
			
			// Additional output
			//newClassification.outputTtree();
			atc.outputRules();
			
		//}
		/*catch (Exception ex)
		{
			System.out.println("Could not read the DB...\n" + ex.getMessage());
		}*/
	}
	
	static HashMap<String,Integer> DataMap = new HashMap<String,Integer>();
	static HashMap<String,Integer> TopicMap = new HashMap<String,Integer>();
	static Integer MaxId = 0;
	static Integer TopicsOffset = 0;
	
	
	static void FixFile(String inputFile, String outputFile) 
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			
			while (br.ready())
			{
				String[] data = br.readLine().split(",");
				for (int i=1; i<data.length-1; ++i)
				{
					AddToMapIfNotThere(DataMap, data[i]);
				}
			}
			br.close();
			
			TopicsOffset = DataMap.size();
			
			br = new BufferedReader(new FileReader(inputFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			while (br.ready())
			{
				String[] data = br.readLine().split(",");
				Integer[] IntData = new Integer[data.length -2];
				String outputSz = "";
				for (int i=1; i<data.length-1; ++i)
				{
					IntData[i-1] = AddToMapIfNotThere(DataMap, data[i]);
				}
				Arrays.sort(IntData);
				for (int i=0; i< IntData.length;++i)
				{
					outputSz += IntData[i] + "\t";
				}
				
				outputSz += (AddToMapIfNotThere(TopicMap, data[data.length-1])+ TopicsOffset) + "\n";
				
				bw.write(outputSz);
			}
			br.close();
			bw.close();
		}
		catch(Exception ex)
		{
			System.out.println("ERROR: Was not able to read input file " + inputFile);
		}
	}
	
	static int AddToMapIfNotThere(HashMap<String,Integer> map, String value)
	{
		int RetVal = MaxId + 1;
		
		if (map.containsKey(value))
		{
			RetVal = map.get(value);
		}
		else
		{
			MaxId = RetVal;
			map.put(value, RetVal);
		}
		
		
		return RetVal;
	}

	/** Converts given real number to real number rounded up to two decimal 
    places. 
    @param number the given number.
    @return the number to two decimal places. */ 
    
    protected static double twoDecPlaces(double number) {
    	int numInt = (int) ((number+0.005)*100.0);
		number = ((double) numInt)/100.0;
		return(number);
    }

    /** Converts given real number to real number rounded up to four decimal
    places.
    @param number the given number.
    @return the number to four decimal places. */

    protected static double fourDecPlaces(double number) {
    	int numInt = (int) ((number+0.00005)*10000.0);
		number = ((double) numInt)/10000.0;
		return(number);
    }	

    private static void createKTransactionFiles(String clusterFile) {    	
    	HashMap<String, ArrayList<String>> docToClasses = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> docToAttributes = new HashMap<String, ArrayList<String>>();
    	
    	try {
        	// First open the list of all topic vectors and write them to a hash map for easy searching.
    		BufferedReader br = new BufferedReader(new FileReader("../output/WordList.csv"));
    		System.out.println("Opened file ../output/WordList.csv for reading.");
    		// Read each line
    		String line = "";
    		Integer docNum = -1;
    		ArrayList<String> classes = new ArrayList<String>();
    		while (br.ready()) {
    			line = br.readLine();
    			// Separate line by commas
    			String[] data = line.split(",");
    			// First number is the document number
    			Integer num = Integer.parseInt(data[0]);
    			if (!docToAttributes.containsKey(num)) {
    				// Insert the previous document's classes
    				if (docNum != -1) {
    					docToClasses.put(docNum.toString(), classes);
    				}
    				docNum = num;
    				// Start a fresh arraylist of classes
    				classes = new ArrayList<String>();
    				// Insert the attributes vector
    				ArrayList<String> attributes = new ArrayList<String>();
    				for (int i = 1; i < data.length - 1; i++) {
    					attributes.add(data[i]);
    				}
    				docToAttributes.put(data[0], attributes);
    				// Add the class to the class arraylist
    				classes.add(data[data.length - 1]);
    				
    			} else {
    				// Add the class to the class arraylist
    				classes.add(data[data.length - 1]);
    			}
    		}
    		docToClasses.put(docNum.toString(), classes);
        	// Open the file for reading clusters
    		br.close();
    		
    		BufferedWriter bw = new BufferedWriter(new FileWriter("../output/Test.txt"));
    		Set<String> keyset = docToClasses.keySet();
    		for (String s : keyset) {
    			bw.write(s);
    			bw.write("\t");
    			ArrayList<String> values = docToClasses.get(s);
    			for (int v = 0; v < values.size(); v++) {
    				bw.write(values.get(v));
    				bw.write("\t");
    			}
    			bw.newLine();
    		}
    		bw.close();
    		
    		
			br = new BufferedReader(new FileReader(clusterFile));
			
			System.out.println("Opened ../output/KMeansCluster for reading.");
			
			// Read each line
			String clusterLine = "";
			Integer numClusters = 0;
			while (br.ready()) {
				String outputFileName = "../output/KMeansCluster";
				clusterLine = br.readLine();
				// Create a new file
				outputFileName += numClusters.toString();
				outputFileName += ".txt";
				bw = new BufferedWriter(new FileWriter(outputFileName));
				System.out.println("Creating file " + outputFileName);
				
				String[] clusterData = clusterLine.split(",");
				// First number is the cluster number, toss it.  Iterate through the other doc numbers.
				for (int i = 1; i < clusterData.length; i++) {
					// Get the docNum
					String currentDocNum = clusterData[i];
					String lineToPut = currentDocNum;
					// Get the attributes of the document number
					System.out.println(currentDocNum);
					Set<String> allDocNums = docToAttributes.keySet();
					Set<String> allClassDocNums = docToClasses.keySet();
					if (allDocNums.contains(currentDocNum) && allClassDocNums.contains(currentDocNum)) {
						System.out.println("Equals " + currentDocNum);
						ArrayList<String> attr = docToAttributes.get(currentDocNum);
						ArrayList<String> clas = docToClasses.get(currentDocNum);
						System.out.println("Size " + attr.size() + " " + clas.size());
						
						for (int a = 0; a < attr.size(); a++) {
							lineToPut += ",";
							lineToPut += attr.get(a);
						}
						
						for (int c = 0; c < clas.size(); c++) {
							String lineToWriteToFile = lineToPut;
							lineToWriteToFile += ",";
							lineToWriteToFile += clas.get(c);
							bw.write(lineToWriteToFile);
							System.out.println(lineToWriteToFile);
							bw.newLine();
						}
						
					}
				}
				// Close writing file
				bw.close();
				numClusters++;
			}
			// Close reading file
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Could not find file for reading " + clusterFile);
		} catch (IOException e) {
			System.out.println("ERROR: Could not read file " + clusterFile);
		}
    }
}
