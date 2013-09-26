import java.io.File;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;


public class NaiveBayesPrediction {
	static double TrainingSplit = 0.6;
	static double TestSplit = 0.4;
	static CSVLoader loader;
	static Instances AllInstances;
	static Instances TrainingInstances;
	static Instances TestingInstances;
	static NaiveBayes NB;
	
	public static void GetDataSource(String FileName) {
		loader = new CSVLoader();
		try {
			loader.setSource(new File(FileName));
			AllInstances = loader.getStructure();
			// The last attribute of the data source is the class
			AllInstances.setClassIndex(AllInstances.numInstances() - 1);
		} catch (Exception e) {
			System.out.println("Failed to extract data from file for training Naive Bayes model: " + e.getMessage());
		}
		
	}
	
	public static void TrainNBModel() {
		// Split the instances of the data
		int trainingSize = (int) Math.round(AllInstances.numInstances() * TrainingSplit);
		int testingSize = AllInstances.numInstances() - trainingSize;
		TrainingInstances = new Instances(AllInstances, 0, trainingSize);
		TestingInstances = new Instances(AllInstances, 0, testingSize);
		
		// Build the classifier
		NB = new NaiveBayes();
		try {
			NB.buildClassifier(AllInstances);
			for (int i = 0; i < TrainingInstances.numInstances(); i++) {
				Instance target = TrainingInstances.instance(i);
				NB.updateClassifier(target);
			}
		} catch (Exception e) {
			System.out.println("Failed to bulid Naive Bayes model: " + e.getMessage());
		}	
	}
	
	// Test the Naive Bayes classifier
	public static void TestNBModel() {
		try {
			Evaluation NBTest = new Evaluation(TrainingInstances);
			NBTest.evaluateModel(NB, TestingInstances);
			// Print out results
			System.out.println("Number of correctly classified instances: " + String.valueOf(NBTest.correct()));
			System.out.println("Error rate: " + String.valueOf(NBTest.errorRate()));
		} catch (Exception e) {
			System.out.println("Failed to test Naive Bayes model: " + e.getMessage());
		}
		
	}
}
