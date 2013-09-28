import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KNNPrediction {
	static double TrainingSplit = 0.01;
	
	static Map<Integer, ArrayList<Integer>> TrainingData;
	static Map<Integer, ArrayList<String>> TrainingClasses;
	static Map<Integer, ArrayList<Integer>> TestingData;
	static Map<Integer, ArrayList<String>> TestingClasses;
	static Map<Integer, Set<String>> Predictions;
	static long startTime;
	static long endTime;
	static long duration;
	
	static public void GetPredictions(String FileName, int NumDocs, String OutputFile, double split) {
		TrainingSplit = split;
		startTime = System.nanoTime();
		// READ FILE CONTENTS TO STRUCTURE
		TrainingData = new HashMap<Integer, ArrayList<Integer>>();
		TrainingClasses = new HashMap<Integer, ArrayList<String>>();
		TestingData = new HashMap<Integer, ArrayList<Integer>>();
		TestingClasses = new HashMap<Integer, ArrayList<String>>();
		Predictions = new HashMap<Integer, Set<String>>();
		// Calculate the number of documents that should go into the training set.
		int numTraining = (int) Math.round(NumDocs * TrainingSplit);
		try {
			System.out.println("Loading data...");
			BufferedReader br = new BufferedReader(new FileReader(FileName));
			String line;
			Integer currentDocNum = null;
			int numDocsParsed = 0;
			int lineNum = 0;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0 || lineNum == 0) {
					lineNum++;
					continue;
				}
				// Separate line by commas
				String[] data = line.split(",");
				// The first entity is the document number
				Integer docNum = Integer.parseInt(data[0]);
				//System.out.println("Parsing doc " + String.valueOf(docNum));
				
				// If the docNum is less than the size of the training set, then put the data into training sets
				if (numDocsParsed < numTraining || (numDocsParsed == numTraining && docNum.equals(currentDocNum))) {
					if (docNum.equals(currentDocNum)) {
						// Only add the additional topic to the class list
						// Fetch the current list of topics for the document number
						ArrayList<String> currentClasses = TrainingClasses.remove(docNum);
						// Add the new topic
						currentClasses.add(data[data.length-1]);
						// Write it back
						TrainingClasses.put(docNum, currentClasses);
					} else {
						numDocsParsed++;
						// Add the data to the TrainingData map
						ArrayList<Integer> attributes = new ArrayList<Integer>();
						for (int i = 1; i < data.length - 1; i++) {
							attributes.add(Integer.parseInt(data[i]));
						}
						TrainingData.put(docNum, attributes);
						// Add topic to TrainingClasses
						ArrayList<String> classes = new ArrayList<String>();
						classes.add(data[data.length-1]);
						TrainingClasses.put(docNum, classes);
						currentDocNum = docNum;
					}
				} else if (numDocsParsed < NumDocs || (numDocsParsed == NumDocs && docNum.equals(currentDocNum))){
					
					if (docNum.equals(currentDocNum)) {
						// Only add the additional topic to the class list
						// Fetch the current list of topics for the document number
						ArrayList<String> currentClasses = TestingClasses.remove(docNum);
						// Add the new topic
						currentClasses.add(data[data.length-1]);
						// Write it back
						TestingClasses.put(docNum, currentClasses);
					} else {
						numDocsParsed++;
						// Add the data to the TestingData map
						ArrayList<Integer> attributes = new ArrayList<Integer>();
						for (int i = 1; i < data.length - 1; i++) {
							attributes.add(Integer.parseInt(data[i]));
						}
						TestingData.put(docNum, attributes);
						// Add topic to TestingClasses
						ArrayList<String> classes = new ArrayList<String>();
						classes.add(data[data.length-1]);
						TestingClasses.put(docNum, classes);
						currentDocNum = docNum;
					}
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Was unable to read " + FileName + " for KNN model: " + e.getMessage());
		}
		endTime = System.nanoTime();
		duration = endTime - startTime;
		System.out.println("Time to build KNN model " + String.valueOf(duration));
		
		// PREDICTION STEP
		startTime = System.nanoTime();
		Set<Integer> testingDocIds = TestingData.keySet();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(OutputFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			
			for (Integer i : testingDocIds) {
				System.out.println("Predicting documents " + String.valueOf(i));
				Integer closestOne, closestTwo, closestThree;
				closestOne = closestTwo = closestThree = -1;
				double valueOne, valueTwo, valueThree;
				valueOne = valueTwo = valueThree = Integer.MAX_VALUE;
				ArrayList<Integer> targetData = TestingData.get(i);
				
				// Iterate through the testing set to determine which three entities are the closest
				Set<Integer> trainingDocIds = TrainingData.keySet();
				for (Integer j : trainingDocIds) {
					// Calculate the Euclidean distance between the vectors
					ArrayList<Integer> compareData = TrainingData.get(j);
					double distance = CalculateEuclidean(targetData, compareData);
					// Check if distance is bigger than any of the three stored
					// If so, store it and kick out the smallest one if applicable.
					if (distance < valueOne) {
						valueThree = valueTwo;
						closestThree = closestTwo;
						valueTwo = valueOne;
						closestTwo = closestOne;
						valueOne = distance;
						closestOne = j;
					} else if (distance < valueTwo) {
						valueThree = valueTwo;
						closestThree = closestTwo;
						valueTwo = distance;
						closestTwo = j;
					} else if (distance < valueThree) {
						valueThree = distance;
						closestThree = j;
					}
				}
				
				// Output the topics
				Set<String> predictedTopics = new HashSet<String>();
				StringBuilder LineToWrite = new StringBuilder(); 
				LineToWrite.append(String.valueOf(i));
				if (closestOne != -1) {
					// Write the closest topics
					ArrayList<String> closestOneTopics = TrainingClasses.get(closestOne);
					for (String t : closestOneTopics) {
						predictedTopics.add(t);
					}
				}
				if (closestTwo != -1) {
					// Write the second closest topics
					ArrayList<String> closestTwoTopics = TrainingClasses.get(closestTwo);
					for (String t : closestTwoTopics) {
						predictedTopics.add(t);
					}
				}
				if (closestThree != -1) {
					// Write the third closest topics
					ArrayList<String> closestThreeTopics = TrainingClasses.get(closestThree);
					for (String t : closestThreeTopics) {
						predictedTopics.add(t);
					}
				}
				for (String topic : predictedTopics) {
					LineToWrite.append(",");
					LineToWrite.append(topic);
				}

				Predictions.put(i, predictedTopics);
				LineToWrite.append("\n");
				bw.write(LineToWrite.toString());
				bw.write("\n");
			}
			bw.close();
		} catch (Exception e) {
			System.out.println("Unable to predict topics for KNN model: " + e.getMessage());
		}
		endTime = System.nanoTime();
		duration = endTime - startTime;
		System.out.println("Time to predict entities: " + String.valueOf(duration));
		
		// EVALUATION STEP
		int correctlyClassified = 0;
		int totalClassified = 0;
		int AtLeastOne = 0;
		for (Integer i : Predictions.keySet()) {
			// Get the set of actual topics
			Set<String> actualTopics = new HashSet<String>();
			for (String t : TestingClasses.get(i)) {
				actualTopics.add(t);
			}
			if (TestSimilarity(actualTopics, Predictions.get(i))) {
				correctlyClassified++;
			}
			if (TestSimilarity2(actualTopics, Predictions.get(i))) {
				AtLeastOne++;
			}
			totalClassified++;
		}
		System.out.println("Correctly classified " + String.valueOf(correctlyClassified) + " documents out of " + String.valueOf(totalClassified));
		System.out.println("At least one Correctly classified " + String.valueOf(AtLeastOne) + " documents out of " + String.valueOf(totalClassified));
		double errorRate = (double)(totalClassified - correctlyClassified)/(double)totalClassified;
		errorRate *= 100;
		double errorRate2 = (double)(totalClassified - AtLeastOne)/(double)totalClassified;
		errorRate2 *= 100;
		System.out.println("Error rate = " + String.valueOf(errorRate) + ", For at least one = " + String.valueOf(errorRate2));
	}
	
	static private double CalculateEuclidean(ArrayList<Integer> one, ArrayList<Integer> two) {
		double sum = 0;
		for (int i = 0; i < one.size(); i++) {
			sum += Math.pow(Math.abs(one.get(i) - two.get(i)), 2);
		}
		return sum;
	}
	
	static private boolean TestSimilarity(Set<String> actual, Set<String> predict) {
		boolean val = false;
		if (actual.retainAll(predict)) {
			val = true;
		}
		return val;
	}
	static private boolean TestSimilarity2(Set<String> actual, Set<String> predict) {
		boolean val = false;
		for (String s : actual)
			if (predict.contains(s)) {
				val = true;
				return val;
			}
		return val;
	}
}
