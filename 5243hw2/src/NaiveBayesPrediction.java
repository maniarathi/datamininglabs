import java.io.File;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;


public class NaiveBayesPrediction {
	static double TrainingSplit = 0.6;
	static double TestSplit = 0.4;
	static CSVLoader loader;
	static Instances AllInstances;
	static Instances TrainingInstances;
	static Instances TestingInstances;
	static NaiveBayes NB;
	static long startTime;
	static long endTime;
	static long duration;
	
	public static void GetPredictions(String FileName, double Split) {
		TrainingSplit = Split;
		TestSplit = 1 - TrainingSplit;
		// Load data
		startTime = System.nanoTime();
		System.out.println("Loading data for Naive Bayes");
		GetDataSource(FileName);
		// Predict
		System.out.println("Performing Naive Bayes prediction...");
		TrainNBModel();
		endTime = System.nanoTime();
		duration = endTime - startTime;
		System.out.println("Time to build Naive Bayes Model: " + String.valueOf(duration));
		// Evaluate
		startTime = System.nanoTime();
		System.out.println("Evaluating Naive Bayes results...");
		TestNBModel();
		endTime = System.nanoTime();
		duration = endTime - startTime;
		System.out.println("Time to test Naive Bayes Model: " + String.valueOf(duration));
	}
	
	private static void GetDataSource(String FileName) {
		
		try {
			DataSource source = new DataSource(FileName);
			AllInstances = source.getDataSet();
			// The last attribute of the data source is the class
			AllInstances.setClassIndex(AllInstances.numAttributes() - 1);
		} catch (Exception e) {
			System.out.println("Failed to extract data from file for training Naive Bayes model: " + e.getMessage());
		}
	}
	
	private static void TrainNBModel() {
		// Split the instances of the data
		int trainingSize = (int) Math.round(AllInstances.numInstances() * TrainingSplit);
		int testingSize = AllInstances.numInstances() - trainingSize;
		TrainingInstances = new Instances(AllInstances, 0, trainingSize);
		TrainingInstances.setClassIndex(TrainingInstances.numAttributes() - 1);
		TestingInstances = new Instances(AllInstances, 0, testingSize);
		TestingInstances.setClassIndex(TestingInstances.numAttributes() - 1);
		
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
	private static void TestNBModel() {
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
