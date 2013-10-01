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
		// Evaluation 1: Check if at least one of the actual topics matches the predicted topics.
		// Evaluation 2: Check if all the actual topics matches the predicted topics.
		// Evaluation 3: Check what percentage of the topics were misclassified (i.e. the predicted topic is not in the actual topic)
		int atLeastOneClassified = 0;
		int perfectClassification = 0;
		int misClassified = 0;
		int totalTopicsClassified = 0;
		int totalClassified = 0;
		for (Integer i : Predictions.keySet()) {
			// Get the set of actual topics
			Set<String> actualTopics = new HashSet<String>();
			for (String t : TestingClasses.get(i)) {
				actualTopics.add(t);
			}
			Set<String> predictedTopics = Predictions.get(i);
			totalTopicsClassified += predictedTopics.size();
			boolean changed = predictedTopics.retainAll(actualTopics);
			if (predictedTopics.size() > 0) {
				atLeastOneClassified++;
			}
			if (changed) {
				// Some topics were removed because they did not exist in the actual topics list
				misClassified += (Predictions.get(i).size() - predictedTopics.size());
			}
			// Check how many were perfectly classified
			predictedTopics = Predictions.get(i); // reset because retainAll may have changed the set.
			if (predictedTopics.removeAll(actualTopics)) {
				if (predictedTopics.size() == 0) {
					perfectClassification++;
				}
			}
			totalClassified++;
		}
		
		System.out.println("Percentage of documents that had at least one topic correctly classified: " + String.valueOf((double)atLeastOneClassified/(double)totalClassified));
		System.out.println("Percentage of documents that were perfectly classified: " + String.valueOf((double)perfectClassification/(double)totalClassified));
		System.out.println("Percentage of erronous topics classified: " + String.valueOf((double)misClassified/(double)totalTopicsClassified));
	}
	
	static private double CalculateEuclidean(ArrayList<Integer> one, ArrayList<Integer> two) {
		double sum = 0;
		for (int i = 0; i < one.size(); i++) {
			sum += Math.pow(Math.abs(one.get(i) - two.get(i)), 2);
		}
		return sum;
	}
}
