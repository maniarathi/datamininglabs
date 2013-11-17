import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;


public class MainRunner {

	public static void main(String[] args) {
		String fileName = "../output/WordList.csv";
		//String fileName = "../output/WordTest.csv";
		int NumOfTopics = 100;
		double Support = 0.1;
		double Confidence = 50;
		
		if (args.length > 0)
		{
			fileName = args[0];
			NumOfTopics = Integer.parseInt(args[1]);
			Support = Double.parseDouble(args[2]);
			Confidence = Double.parseDouble(args[3]);
			
		}
		//AR dm = new AR();
		//try
		//{
			//dm.Mine(fileName, 1, 1);
			
			// Prepare file
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
		}/* FOUR DECIMAL PLACES */

    /** Converts given real number to real number rounded up to four decimal
    places.
    @param number the given number.
    @return the number to gour decimal places. */

    protected static double fourDecPlaces(double number) {
    	int numInt = (int) ((number+0.00005)*10000.0);
		number = ((double) numInt)/10000.0;
		return(number);
		}	

}
