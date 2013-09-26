import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.neighboursearch.LinearNNSearch;

public class KNNPrediction {
	static double TrainingSplit = 0.6;
	static double TestSplit = 0.4;
	static DataSource DS;
	static Instances AllInstances;
	static Instances TrainingInstances;
	static Instances TestingInstances;
	static LinearNNSearch KNN;
	
	static public void GetDataSource(String FileName) {
		// Make sure FileName is a valid file name (i.e. ARFF, CSV, XRFF, etc.)
		try {
			DS = new DataSource(FileName);
			AllInstances = DS.getDataSet();
		} catch (Exception e) {
			System.out.println("Failed to extract data from file for training KNN model: " + e.getMessage());
		}
	}
	
	static public void TrainKNNModel() {
		int trainingSize = (int) Math.round(AllInstances.numInstances() * TrainingSplit);
		int testingSize = AllInstances.numInstances() - trainingSize;
		TrainingInstances = new Instances(AllInstances, 0, trainingSize);
		TestingInstances = new Instances(AllInstances, 0, testingSize);
		KNN = new LinearNNSearch(TrainingInstances);
	}
	
	static public void TestKNNModel() {
		for (int i = 0; i < TestingInstances.numInstances(); i++) {
			Instance target = TestingInstances.instance(i);
			try {
				Instances nearestInstances = KNN.kNearestNeighbours(target, 3);
				// Print out the nearest instances
				System.out.println("\nFound nearest 3 instances");
				System.out.println(nearestInstances);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
