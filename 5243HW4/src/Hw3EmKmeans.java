import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;


public class Hw3EmKmeans {

	public static void main(String[] in_params)
	{
		LinkedList<Double> lst1d = null;
		
		LinkedList<LinkedList<Double>> lstDevision1D = null;
		
		if (in_params.length < 1)
		{
			System.out.println("This application requires one parameter, which is a file name. Optional parameter - number of Gaussians.");
		}
		else
		{
			int NumOfGaussians = 3;
			if (in_params.length == 2)
			{
				try
				{
					NumOfGaussians = Integer.parseInt(in_params[1]);
				}
				catch (Exception ex) { }
			}
			
			System.out.println("Start reading the file");
			try
			{
				lst1d = ReadFile1d(in_params[0]);
				
				//Q1
				System.out.println("\n\n--Q1\nRunning for " + NumOfGaussians);
				
				// Guess first assignment
				lstDevision1D = GuessFirstSplit(lst1d,NumOfGaussians);
				
				// Find the gaussians mean and std
				EM1d(lstDevision1D,NumOfGaussians);
				
				//Q2
				System.out.println("\n\n--Q2");
				for (int i=4; i <=6; ++i)
				{
					System.out.println("\nFor "+ i +" Gaussians");
					EM1d(GuessFirstSplit(lst1d,i),i);
				}
				
				//Q3
				System.out.println("\n\n--Q3");
				kmeans(lstDevision1D,NumOfGaussians);
				
			}
			catch (Exception ex)
			{
				System.out.println("Exiting due to an exception..." + ex.getMessage());
			}
		}
		
		System.out.println("Exited successfully.");
	}
	
	// EM for 1d
	private static LinkedList<LinkedList<Double>> EM1d(LinkedList<LinkedList<Double>> lstDevision1D, int NumOfGaussians) 
	{
		LinkedList<LinkedList<Double>> retVal = new LinkedList<LinkedList<Double>>();
		
		// Values of mean and std
		double[] means = new double[NumOfGaussians];
		double[] stds = new double[NumOfGaussians];
		
		// Values from last iteration for calculating change
		double[] prev_means = new double[NumOfGaussians];
		double[] prev_stds = new double[NumOfGaussians];
		
		// it's easier to work with arrays for this usage
		Object[] clustersDev1d = lstDevision1D.toArray();
		ArrayList<LinkedList<Double>> cluster = new ArrayList<LinkedList<Double>>();
		
		for (int i = 0; i < NumOfGaussians; i++)
		{
			@SuppressWarnings("unchecked")
			LinkedList<Double> toAdd = (LinkedList<Double>)clustersDev1d[i];
			cluster.add(i,toAdd);
			
			DoublePairStats tmp = EvaluateDataSet1D(toAdd);
			means[i] = tmp.num1Average;
			stds[i] = tmp.num1Std;
			prev_means[i] = Double.MIN_VALUE;
			prev_stds[i] = Double.MIN_VALUE;
		}
		// Try random data here...
		
		
		long iter = 0;
		double error = Double.MAX_VALUE;
		while (error != 0 && iter < 1000000)
		{
			// E stage
			// For each cluster - assign it to the cluster it closest the most
			cluster = Expactation(cluster,means,stds);
			
			// M stage
			// Calculate for each cluster it means and stds
			for (int i = 0; i < NumOfGaussians; i++)
			{	
				DoublePairStats tmp = EvaluateDataSet1D(cluster.get(i));
				means[i] = tmp.num1Average;
				stds[i] = tmp.num1Std;
			}
			
			// Calculate change
			error = 0;
			for (int i = 0; i < NumOfGaussians; i++)
			{
				error += Math.abs(prev_stds[i] - stds[i]) + Math.abs(prev_means[i] - means[i]);
				prev_means[i] = means[i];
				prev_stds[i] = stds[i];
			}
			++iter;
		}
		
		// Print out the results, and prepare return values
		System.out.println("Found results for the 1D EM problem: ");
		for (int i=0; i<cluster.size();i++) 
		{
			System.out.println("Cluster " + i + ": Mean="+means[i] + " , std="+stds[i] + " , number of objects in the cluster=" + cluster.get(i).size());
			retVal.add(cluster.get(i));
		}
		
		return retVal;
	}

	// For each point- assign it to the cluster with the closest value
	private static ArrayList<LinkedList<Double>> Expactation(ArrayList<LinkedList<Double>> cluster, double[] means, double[] stds) 
	{
		ArrayList<LinkedList<Double>> retVal = new ArrayList<LinkedList<Double>>();
		int numOfPoints = 0;
		for (int i=0; i<cluster.size(); ++i)
		{
			retVal.add(i,new LinkedList<Double>());
			numOfPoints += cluster.get(i).size();
		}
		
		for (int c=0; c<cluster.size();c++)
		{
			LinkedList<Double> currCluster = cluster.get(c);
			// ugly syntax for the .net foreach loop
			ListIterator<Double> getListContent = currCluster.listIterator();
			while(getListContent.hasNext())
			{
				Double currVal = getListContent.next();
				
				// which cluster is the nearest (in terms of maximizing likelihood)?
				double maxDist = Double.MIN_VALUE;
				int minPlace = 0;
				for (int i=0; i<means.length;++i)
				{
					double dist4pdf = Math.pow(currVal-means[i],2)/(2*Math.pow(stds[i],2));
					double pdf = (1/(stds[i]*Math.sqrt(2*Math.PI)))*Math.pow(Math.E, -1*dist4pdf);
					if (((double)currCluster.size()/(double)numOfPoints)*pdf > maxDist)
					{
						maxDist = ((double)currCluster.size()/(double)numOfPoints)*pdf;
						minPlace = i;
					}
				}
				// the closest cluster is minPliace...
				(retVal.get(minPlace)).add(currVal);
			}
		}
		
		// return all the values
		return retVal;
	}

	// Calculate mean and std for the input list
	private static DoublePairStats EvaluateDataSet1D(LinkedList<Double> lstPSP) 
	{
		DoublePairStats retVal = new DoublePairStats();
		
		// Calculate means
		double meannum1 = 0;
		double meannum2 = 0;
		int NumOfObjects = 0;
		double maxnum1 = 0;
		double maxnum2 = 0;
		double minnum1 = 0;
		double minnum2 = 0;
		
		// ugly syntax for the .net foreach loop
		ListIterator<Double> getListContent = lstPSP.listIterator();
		while(getListContent.hasNext())
		{
			Double currVal = getListContent.next();
			
			if (NumOfObjects == 0)
			{
				maxnum1 = currVal;
				minnum1 = currVal;
				meannum1 = currVal;
			}
			else
			{
				if (maxnum1 < currVal) maxnum1 = currVal;
				if (minnum1 > currVal) minnum1 = currVal;
				
				meannum1 += currVal;
			}
			
			++NumOfObjects;
		}
		meannum1 = meannum1 / NumOfObjects;
		
		// After having the meant - let's calculate standard deviation
		double stdVarNum1 = 0;
		getListContent = lstPSP.listIterator();
		while(getListContent.hasNext())
		{
			Double currVal = getListContent.next();
			
			stdVarNum1 += Math.pow((currVal - meannum1),2);
		}
		stdVarNum1 = Math.sqrt(stdVarNum1/NumOfObjects);
		
		// Populate return values
		retVal.numOfObjects = NumOfObjects;
		retVal.num1Average = meannum1;
		retVal.num2Average = meannum2;
		retVal.num1Max = maxnum1;
		retVal.num2Max = maxnum2;
		retVal.num1Min = minnum1;
		retVal.num2Min = minnum2;
		retVal.num1Std = stdVarNum1;
		retVal.num2Std = 0;
		
		return retVal;
	}
	
	// Read 1d file
	private static LinkedList<Double> ReadFile1d(String fileName) throws IOException
	{
		LinkedList<Double> retVal = new LinkedList<Double>();
		
		try
		{
			// Open the file
			FileReader fr = new FileReader(fileName);
			BufferedReader bfr = new BufferedReader(fr);
			
			// Read its content
			while (bfr.ready())
			{
				String inputLine = bfr.readLine();
				Double val = Double.parseDouble(inputLine);
				
				// Add content to return list
				retVal.add(val);
			}
			bfr.close();
			fr.close();
			
			System.out.println("Finished reading the 1D file");
		}
		catch(NumberFormatException ex)
		{
			System.out.println("One of the values is not from the right format.");
			throw ex;
		}
		catch(IOException ex)
		{
			System.out.println("the given file could not be found, or could not been read.");
			throw ex;
		}
		
		return retVal;
	}
	
	// Guesses the first split based on an assumption of equal number of samples from each cluster
	private static LinkedList<LinkedList<Double>> GuessFirstSplit(LinkedList<Double> inVal, int numOfGaussians)
	{
		LinkedList<LinkedList<Double>> retVal = new LinkedList<LinkedList<Double>>();
		Object[] sortedArray = inVal.toArray();
		Arrays.sort(sortedArray);
		
		LinkedList<Double> tmp = null;
		for (int i=0; i < sortedArray.length; ++i)
		{
			if (i%(sortedArray.length/numOfGaussians) == 0)
			{
				if (i!= 0) retVal.add(tmp);
				tmp = new LinkedList<Double>();
			}
			tmp.add((Double)sortedArray[i]);
		}
		retVal.add(tmp);
		
		
		return retVal;
	}

	// kmeans
	// Please note my classes actually calculate the standard variation, though - I don't even look at it.
	private static LinkedList<LinkedList<Double>> kmeans(LinkedList<LinkedList<Double>> lstDevision1D, int NumOfGaussians)
	{
		LinkedList<LinkedList<Double>> retVal = new LinkedList<LinkedList<Double>>();
		
		// Values of mean and std
		double[] means = new double[NumOfGaussians];
		
		// Values from last iteration for calculating change
		double[] prev_means = new double[NumOfGaussians];
		
		// it's easier to work with arrays for this usage
		Object[] clustersDev1d = lstDevision1D.toArray();
		ArrayList<LinkedList<Double>> cluster = new ArrayList<LinkedList<Double>>();
		
		for (int i = 0; i < NumOfGaussians; i++)
		{
			@SuppressWarnings("unchecked")
			LinkedList<Double> toAdd = (LinkedList<Double>)clustersDev1d[i];
			cluster.add(i,toAdd);
			
			DoublePairStats tmp = EvaluateDataSet1D(toAdd);
			means[i] = tmp.num1Average;
			prev_means[i] = Double.MIN_VALUE;
		}
		// Try random data here...
		
		
		long iter = 0;
		double error = Double.MAX_VALUE;
		while (error != 0 && iter < 1000000)
		{
			cluster = kmeansReassign(cluster,means);
			
			// Recalculate means
			for (int i = 0; i < NumOfGaussians; i++)
			{	
				DoublePairStats tmp = EvaluateDataSet1D(cluster.get(i));
				means[i] = tmp.num1Average;
			}
			
			// Calculate change
			error = 0;
			for (int i = 0; i < NumOfGaussians; i++)
			{
				error += Math.abs(prev_means[i] - means[i]);
				prev_means[i] = means[i];
			}
			++iter;
		}
		
		// Print out the results, and prepare return values
		System.out.println("Found results for the 1D K-Means problem: ");
		for (int i=0; i<cluster.size();i++) 
		{
			System.out.println("Cluster " + i + ": Mean="+means[i] + " , number of objects in the cluster=" + cluster.get(i).size());
			retVal.add(cluster.get(i));
		}
		
		return retVal;
	}
	
	
	// For each point- assign it to the cluster with the closest value
	private static ArrayList<LinkedList<Double>> kmeansReassign(ArrayList<LinkedList<Double>> cluster, double[] means) 
	{
		ArrayList<LinkedList<Double>> retVal = new ArrayList<LinkedList<Double>>();
		for (int i=0; i<cluster.size(); ++i)
		{
			retVal.add(i,new LinkedList<Double>());
		}
		
		for (int c=0; c<cluster.size();c++)
		{
			LinkedList<Double> currCluster = cluster.get(c);
			// ugly syntax for the .net foreach loop
			ListIterator<Double> getListContent = currCluster.listIterator();
			while(getListContent.hasNext())
			{
				Double currVal = getListContent.next();
				
				// which cluster is the nearest (in terms of maximizing likelihood)?
				double minDist = Double.MAX_VALUE;
				int minPlace = 0;
				for (int i=0; i<means.length;++i)
				{
					double dist = Math.abs(currVal - means[i]);
					if (dist < minDist)
					{
						minDist = dist;
						minPlace = i;
					}
				}
				// the closest cluster is minPliace...
				(retVal.get(minPlace)).add(currVal);
			}
		}
		
		// return all the values
		return retVal;
	}
}
