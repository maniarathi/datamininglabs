/// Roee Ebenstein
/// 2013.09.23
////////////////////////////
// This is a word count vector
// It also implements word reduction (by statistical measures, dictionary, and stop words)
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;

public class WordCountVector 
{
	static double SigmaMultiplier = 0.08; // This is a sigma that matches this data
	static HashSet<String> EnglishDictionary = new HashSet<String>(); // English dictionary 
	static HashMap<String,Integer> Topics = new HashMap<String,Integer>(); // Holds a list of optional topics
	static HashMap<String,Integer> Words = new HashMap<String,Integer>();  // Holds a list of words that being used
	static HashMap<Integer,Integer> Document = new HashMap<Integer,Integer>(); // a match between documentID in the matrices to a lineId 
	static int[][] TopicsTrainingMatrix = null; // The matrix of training - word count vector
	static int[][] TopicsClassificationMatrix = null; // The matrix of classification - each document is related to which topic
	static int TitleWeight = 10; // The additional weight a topic word has.

	// Adds a document to the vectors
	static public void AddDocumentToTopicVector(int DocId, String[] DocumentTopics, String[] Title, String[] Body)
	{
		// If there's nothing to add - exit
		if (DocumentTopics == null || DocumentTopics.length == 0 || ((Title == null || Title.length == 0) && (Body == null || Body.length == 0))) return;
		boolean AllTextEmpty = true;
		for (String s : DocumentTopics)
		{
			if (s != null && s.trim() != "") AllTextEmpty = false;
		}
		if (AllTextEmpty) return;
		AllTextEmpty = true;
		if (Title != null) for (String s : Title)
		{
			if (s != null && s.trim() != "") AllTextEmpty = false;
		}
		if (Body != null) for (String s : Body)
		{
			if (s != null && s.trim() != "") AllTextEmpty = false;
		}
		if (AllTextEmpty) return;

		// Assign the document a number (the line it will be in in the matrices)
		int DocLine = 0;
		synchronized(Document)
		{
			if (Document.containsKey(DocId))
			{
				DocLine = Document.get(DocId);
			}
			else
			{
				DocLine = Document.size();
				Document.put(DocId, DocLine);
			}
		}

		// Add the title words to the matrices (word by word)
		if (Title != null)
		{
			for (String CurrTitle : Title)
			{
				CurrTitle = CurrTitle.replaceAll("[\\W\\d]+", " "); // Removes all non character letters
				String[] TitleWords = CurrTitle.split(" ");

				for (String word : TitleWords)
				{
					AddWordToTopicVector(DocLine, word, TitleWeight);
				}
			}
		}

		// Add the body words to the matrices (word by word)
		if (Body != null)
		{
			for (String CurrBody : Body)
			{
				CurrBody = CurrBody.replaceAll("[\\W\\d]+", " "); // Removes all non character letters
				String[] BodyWords = CurrBody.split(" ");

				for (String word : BodyWords)
				{
					AddWordToTopicVector(DocLine, word, 1);
				}
			}
		}

		if (TopicsTrainingMatrix[DocLine] == null)
		{
			Document.remove(DocId);
		}
		else
		{
			// Add the topics to the appropriate matrix
			AddTopicsClassification(DocLine, DocumentTopics);
		}
	}


	static private StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_44); // A LUCENE analyzer for stopwords
	static private PorterStemmer stemmer = new PorterStemmer(); // A LUCENE stemmer for stemming

	// Adds a word to the vector
	static private void AddWordToTopicVector(int DocLine, String word, int weight)
	{
		// If the word is too short or long - drop it
		if (word.length() <= 2 || word.length() > 15) return;

		// Stem the word
		String OrigWord = word.toLowerCase();
		stemmer.setCurrent(word);
		if (stemmer.stem())
		{
			word =stemmer.getCurrent().toLowerCase();

			// If the word is not a known English word - drop it.
			if (EnglishDictionary!= null && (!EnglishDictionary.contains(word) && !EnglishDictionary.contains(OrigWord))) return;
		}
		else
		{
			// In the case word couldn't stem the word - we don't want it...
			return; 
		}

		// Check the word is not a stop word, if it is - drop it.
		CharArraySet set=analyzer.getStopwordSet();
		if (set.contains(word) || word == "")
			return;

		// The word is good!! yay!!
		// let's add it
		int WordPlace = 0;

		// Get the word column ID, if it doesn't have one yet - create a place for it.
		synchronized(Words)
		{
			if (Words.containsKey(word))
			{
				WordPlace = Words.get(word);
			}
			else
			{
				WordPlace = Words.size();
				Words.put(word, WordPlace);
			}
		}

		//// Make sure the Matrix has a room for it - if not - add a place
		// if the matrix was not initialized yet - do it!
		if (TopicsTrainingMatrix == null)
		{
			TopicsTrainingMatrix = new int[Document.size()][];
			TopicsTrainingMatrix[DocLine] = new int[Words.size()]; 
		}
		// If the matrix was initizlized - but there's no line meant for it - double the matrix size (increasing is common)
		else if (TopicsTrainingMatrix.length <= DocLine)
		{
			int[][] OldTopicsMatrix = TopicsTrainingMatrix;
			TopicsTrainingMatrix = new int[OldTopicsMatrix.length * 2][];
			// copy old data
			for (int i=0;i<OldTopicsMatrix.length;++i)
			{
				TopicsTrainingMatrix[i] = OldTopicsMatrix[i];
			}
		}

		// If the Line was not initialized yet (the room was pre-created, and not used yet...) create it
		if (TopicsTrainingMatrix[DocLine] == null)
		{
			TopicsTrainingMatrix[DocLine] = new int[Words.size()];
		}
		// If it was initialized - but before, and now there's a new word - increase the size of the vector
		else if(TopicsTrainingMatrix[DocLine].length <= WordPlace)
		{
			int[] OldLine = TopicsTrainingMatrix[DocLine];
			TopicsTrainingMatrix[DocLine] = new int[Words.size()];
			for (int i=0;i<OldLine.length;++i)
			{
				TopicsTrainingMatrix[DocLine][i] = OldLine[i];
			}
		}

		// Add the word
		TopicsTrainingMatrix[DocLine][WordPlace] = TopicsTrainingMatrix[DocLine][WordPlace] + weight;
	}

	// Adds the topics to the vector
	static private void AddTopicsClassification(int DocLine, String[] DocumentTopics)
	{
		// If there are topics ...
		if (DocumentTopics == null) return;

		// Get the topic column, if it does not exist yet - add it.
		int TopicPlace = 0;
		synchronized(Topics)
		{
			for (String topic: DocumentTopics)
			{
				if (!Topics.containsKey(topic))
				{
					Topics.put(topic, Topics.size());
				}
			}
		}

		// Add each topic to the vector, one by one
		for (String topic: DocumentTopics)
		{
			TopicPlace = Topics.get(topic);

			// If the matrix was not initialized yet - initialize it
			if (TopicsClassificationMatrix == null)
			{
				TopicsClassificationMatrix = new int[Document.size()][];
				TopicsClassificationMatrix[DocLine] = new int[Topics.size()]; 
			}
			// If the matrix don't have enough lines - double the number of lines
			else if (TopicsClassificationMatrix.length <= DocLine)
			{
				int[][] OldTopicsMatrix = TopicsClassificationMatrix;
				TopicsClassificationMatrix = new int[OldTopicsMatrix.length * 2][];
				// copy old data
				for (int i=0;i<OldTopicsMatrix.length;++i)
				{
					TopicsClassificationMatrix[i] = OldTopicsMatrix[i];
				}
			}

			// If the line was not initialized yet - init it
			if (TopicsClassificationMatrix[DocLine] == null)
			{
				TopicsClassificationMatrix[DocLine] = new int[Topics.size()];
			}
			// If the line don't have enough columns - add more columns
			else if(TopicsClassificationMatrix[DocLine].length <= TopicPlace)
			{
				int[] OldLine = TopicsClassificationMatrix[DocLine];
				TopicsClassificationMatrix[DocLine] = new int[Topics.size()];
				for (int i=0;i<OldLine.length;++i)
				{
					TopicsClassificationMatrix[DocLine][i] = OldLine[i];
				}
			}

			// Add the topic to the vector
			TopicsClassificationMatrix[DocLine][TopicPlace] = TopicsClassificationMatrix[DocLine][TopicPlace] + 1; 
		}
	}


	// Load linux dictionary, if it exists (can be any dictionary)
	static public void LoadDictionary()
	{
		// Init the dictionary
		EnglishDictionary = new HashSet<String>();

		InputStream    fis;
		BufferedReader br;
		String         word;

		try
		{
			// Get the Linux dictionary file
			fis = new FileInputStream("words");
			br = new BufferedReader(new InputStreamReader(fis));

			// for each word, add it as is to the dictionary, and add its stemming - if it wasn't added earlier.
			while ((word = br.readLine().toLowerCase()) != null) 
			{
				if (!EnglishDictionary.contains(word)) EnglishDictionary.add(word);
				stemmer.setCurrent(word);
				if (stemmer.stem())
				{
					word =stemmer.getCurrent().toLowerCase();
					if (!EnglishDictionary.contains(word)) EnglishDictionary.add(word);
				}
			}
			br.close();
		}
		// In the case of an error reading the dictionary - don't initialize it at all.
		catch(Exception ex) {EnglishDictionary = null;}
		br = null;
		fis = null;
	}

	// Build a file for holding the information
	static public void WriteToFile(String FileNameWords, String FileNameTopics)
	{
		// Get ready to save the information - build data structure to help do it faster
		HashMap<Integer,String> ReverseWords = new HashMap<>();
		HashMap<Integer,String> ReverseTopics = new HashMap<>();
		HashMap<Integer,Integer> ReverseDocumentIds = new HashMap<>();

		// Reverse all the data vectors so searching it would be possible.
		for(String word : Words.keySet())
		{
			ReverseWords.put(Words.get(word), word);
		}
		for(String topic : Topics.keySet())
		{
			ReverseTopics.put(Topics.get(topic), topic);
		}
		for(int docId : Document.keySet())
		{
			ReverseDocumentIds.put(Document.get(docId), docId);
		}
		int NumOfWords = Words.size();
		//int NumOfTopics = Topics.size();


		// Build a file using the following format : columns, Matrix (first column is documentId), and afterwards same format for topics
		OutputStream 	fisw;
		//OutputStream 	fist;
		BufferedWriter 	bww;
		//BufferedWriter 	bwt;

		try
		{
			fisw	= new FileOutputStream(FileNameWords);
			//fist	= new FileOutputStream(FileNameTopics);
			bww		= new BufferedWriter(new OutputStreamWriter(fisw));
			//bwt		= new BufferedWriter(new OutputStreamWriter(fist));

			String LineToPutInWordsFile = "";
			//String LineToPutInTopicsFile = "";

			// Write words across the top of the CSV file
			for (int i=0; i < ReverseWords.size();++i)
			{
				if (i==0)
					LineToPutInWordsFile = "DocumentId," + ReverseWords.get(i);
				else
					LineToPutInWordsFile += "," + ReverseWords.get(i);
			}
			// Write topics across the top of the CSV file
			/*for (int i=0; i < ReverseTopics.size();++i)
			{
				if (i==0)
					LineToPutInTopicsFile = "DocumentId," + ReverseWords.get(i);
				LineToPutInTopicsFile += "," + ReverseTopics.get(i);
			}
			LineToPutInWordsFile += ",Topics";*/
			bww.write(LineToPutInWordsFile + "\n\n");
			//bwt.write(LineToPutInTopicsFile + "\n\n");


			// Write the words vector, each line first parameter is the document id - afterwards the word count of the matching column
			for (int i=0; i< Document.size();++i)
			{
				for(int j=0; TopicsTrainingMatrix[i] != null && j < TopicsTrainingMatrix[i].length; ++j)
				{
					if (j==0) LineToPutInWordsFile = String.valueOf(ReverseDocumentIds.get(i)) + "," + String.valueOf(TopicsTrainingMatrix[i][j]);
					else LineToPutInWordsFile += "," + String.valueOf(TopicsTrainingMatrix[i][j]);
				}
				int StartVal = 0;
				if (TopicsTrainingMatrix[i]!= null ) StartVal = TopicsTrainingMatrix[i].length;
				for (int j = StartVal; j < NumOfWords; ++j)
				{
					if (j==0) LineToPutInWordsFile = String.valueOf(ReverseDocumentIds.get(i)) + "," + String.valueOf(0);
					else LineToPutInWordsFile += "," + String.valueOf(0);
				}
				String BasicLine = LineToPutInWordsFile + ",";
				for(int j=0; TopicsClassificationMatrix[i] != null && j < Topics.size() && TopicsClassificationMatrix[i].length >j; ++j)
				{
					if (TopicsClassificationMatrix[i][j] > 0)
					{
						bww.write(BasicLine +  ReverseTopics.get(j) + "\n");
					}
				}

				//bww.write(LineToPutInWordsFile+"\n");
				//bwt.write(LineToPutInTopicsFile+"\n");
			}
			bww.write("\n");
			//bwt.write("\n");
			bww.close();
			//bwt.close();
		}
		catch(Exception ex) 
		{
			System.out.println("Failed to save file. error: " + ex.getMessage() + "\n " + ex.getStackTrace());
		}
		bww = null;
		//bwt = null;
		fisw = null;
		//fist = null;

	}

	static public void DecreaseVectorSize()
	{
		// Get prepared  - prepare the words to be used:
		HashMap<Integer,String> ReverseWords = new HashMap<>();
		HashMap<Integer,Integer> ReverseDocumentIds = new HashMap<>();
		int[][] TopicsClassToSwap = null;

		for(String word : Words.keySet())
		{
			ReverseWords.put(Words.get(word), word);
		}
		for(int docId : Document.keySet())
		{
			ReverseDocumentIds.put(Document.get(docId), docId);
		}

		// Find Mean and variance of words
		double sum = 0;
		double varsum = 0;
		double variance = 0;
		double count = 0;

		for (int i=0; i < Words.size(); ++i)
		{
			varsum = 0;
			for (int j=0;j < Document.size(); ++j)
			{
				if (TopicsTrainingMatrix[j] != null && TopicsTrainingMatrix[j].length>i)
				{
					sum += TopicsTrainingMatrix[j][i];
					varsum += TopicsTrainingMatrix[j][i];
				}
			}
			variance += Math.pow(varsum, 2);
			++count;
		}
		double mean = sum / count;
		variance = variance / count;
		variance = Math.sqrt(variance - Math.pow(mean,2));

		System.out.println("The mean is "+ mean + " and the variance is " + variance);

		// Find words that are between -2sigma to sigma
		HashSet<String> WordsToUse = new HashSet<String>();
		for (int i=0; i < Words.size(); ++i)
		{
			sum = 0;
			for (int j=0;j < Document.size(); ++j)
			{
				if (TopicsTrainingMatrix[j] != null && TopicsTrainingMatrix[j].length>i)
				{
					sum += TopicsTrainingMatrix[j][i];
					varsum += TopicsTrainingMatrix[j][i];
				}
			}

			if (sum >= mean - (SigmaMultiplier*variance) && sum <= mean + (SigmaMultiplier*variance))
			{
				WordsToUse.add(ReverseWords.get(i));
			}
		}
		System.out.println("There are "+ WordsToUse.size() + " words left after cutting by statistical properties.");

		// Fix the matrices to keep only these words.
		 int[][] TopicsTrainingMatrixToSwap = new int[Document.size()][];
		 HashMap<String,Integer> WordsToSwap = new HashMap<String,Integer>();
		 HashMap<Integer,Integer> DocumentToSwap = new HashMap<Integer,Integer>();
		 HashMap<Integer,Integer> DocumentToSwapReverse = new HashMap<Integer,Integer>();
		 int NumOfRowsToGoDown = 0;

		 for (int i=0; i<Document.size();++i)
		 {
			 int NumOfRowsWritten = 0;
			 for (int j=0; j< Words.size() && TopicsTrainingMatrix[i] != null && j<TopicsTrainingMatrix[i].length;++j)
			 {
				 String CurrWord = ReverseWords.get(j);
				 if (WordsToUse.contains(CurrWord))
				 {
					 // Get the word key in the new array
					 int WordKey;
					 if (WordsToSwap.containsKey(CurrWord))
					 {
						 WordKey = WordsToSwap.get(CurrWord);
					 }
					 else
					 {
						 WordKey = WordsToSwap.size();
						 WordsToSwap.put(CurrWord,WordKey);
					 }

					 if (TopicsTrainingMatrixToSwap[i-NumOfRowsToGoDown] == null)
					 {
						 TopicsTrainingMatrixToSwap[i-NumOfRowsToGoDown] = new int[WordsToUse.size()];
					 }

					 TopicsTrainingMatrixToSwap[i-NumOfRowsToGoDown][WordKey] = TopicsTrainingMatrix[i][j];
					 if (TopicsTrainingMatrix[i][j] > 0) ++NumOfRowsWritten;
				 }
			 }
			 if (NumOfRowsWritten == 0)
			 {
				 TopicsTrainingMatrixToSwap[i - NumOfRowsToGoDown] = null;
				 ++NumOfRowsToGoDown;
			 }
			 else
			 {
				 DocumentToSwap.put(ReverseDocumentIds.get(i),i-NumOfRowsToGoDown);
				 DocumentToSwapReverse.put(i-NumOfRowsToGoDown,ReverseDocumentIds.get(i));
			 }
		 }

		 // Build the new classification matrix
		 TopicsClassToSwap = new int[DocumentToSwap.size()][];
		 for (int i=0; i< DocumentToSwap.size();++i)
		 {
			 int TargetRow = Document.get(DocumentToSwapReverse.get(i));
			 TopicsClassToSwap[i] = TopicsClassificationMatrix[TargetRow];
		 }

		 // Swap the dictionaries
		 TopicsTrainingMatrix = TopicsTrainingMatrixToSwap;
		 TopicsClassificationMatrix = TopicsClassToSwap;
		 Words = WordsToSwap;
		 Document = DocumentToSwap;
		 System.out.println("After reducing - there are " + Words.size() + " words represented here.");
	}
}