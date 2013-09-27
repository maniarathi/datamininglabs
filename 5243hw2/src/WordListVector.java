import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

import org.tartarus.snowball.ext.PorterStemmer;

/// Roee Ebenstein
/// 2013.09.23
////////////////////////////
// This will be the word list vector.
// This vector is depndent on the run of WordCountVector first -it will use it data structures 

public class WordListVector 
{
	static HashMap<Integer, HashSet<String>> WordListVector = new HashMap<Integer, HashSet<String>>(); // Holds for each article its distinct words
	static private PorterStemmer stemmer = new PorterStemmer(); // A LUCENE stemmer for stemming
	
	static public void AddDocumentToTopicVector(int DocId, String[] DocumentTopics, String[] Title, String[] Body)
	{
		// If nothing to do - exit
		//if (DocumentTopics == null || (Title == null && Body == null)) return;
		if (!WordCountVector.Document.containsKey(DocId)) return;
		
		// Build the words the same way as in the WordCountVector
		HashSet<String> RetVal = new HashSet<>();
		// Scan the title
		if (Title != null)
		{
			for (String CurrTitle : Title)
			{
				CurrTitle = CurrTitle.replaceAll("[\\W\\d]+", " "); // Removes all non character letters
				String[] TitleWords = CurrTitle.split(" ");
				
				for (String word : TitleWords)
				{
					stemmer.setCurrent(word);
					if (stemmer.stem())
					{
						word =stemmer.getCurrent().toLowerCase();
						if (WordCountVector.Words.containsKey(word) && !RetVal.contains(word))
						{
							RetVal.add(word);
						}
					}
				}
			}
		}
		
		// Scan the body
		if (Body != null)
		{
			for (String CurrBody : Body)
			{
				CurrBody = CurrBody.replaceAll("[\\W\\d]+", " "); // Removes all non character letters
				String[] BodyWords = CurrBody.split(" ");
				
				for (String word : BodyWords)
				{
					stemmer.setCurrent(word);
					if (stemmer.stem())
					{
						word =stemmer.getCurrent().toLowerCase();
						if (WordCountVector.Words.containsKey(word) && !RetVal.contains(word))
						{
							RetVal.add(word);
						}
					}
				}
			}
		}
		
		// Add the set of words to the dictionary
		// If all the meaningfull words are out - we don't bother writing it
		// assuming it is not important.
		if (RetVal.size() > 0) WordListVector.put(DocId, RetVal);
	}
	// Build a file for holding the information
	static public void WriteToFile(String FileName)
	{
		// Get ready to save the information - build data structure to help do it faster
		HashMap<Integer,String> ReverseTopics = new HashMap<>();
		HashMap<Integer,Integer> ReverseDocumentIds = new HashMap<>();
		
		// Reverse all the data vectors so searching it would be possible.
		for(String topic : WordCountVector.Topics.keySet())
		{
			ReverseTopics.put(WordCountVector.Topics.get(topic), topic);
		}
		for(int docId : WordCountVector.Document.keySet())
		{
			ReverseDocumentIds.put(WordCountVector.Document.get(docId), docId);
		}
		
		// Build a file using the following format : columns, Matrix (first column is documentId), and afterwards same format for topics
		OutputStream fis;
		BufferedWriter bw;

		try
		{
			fis = new FileOutputStream(FileName);
			bw = new BufferedWriter(new OutputStreamWriter(fis));
			
			String LineToPut = "";
			
			// Write words
			for (int id : WordListVector.keySet())
			{
				System.out.println("Writing to file Document " + String.valueOf(id) + " to WordList.csv");
				LineToPut = String.valueOf(id)+ ",";
				boolean first = true;
				for (String word : WordListVector.get(id))
				{
					if (first) {LineToPut += word; first = false;}
					else LineToPut += "," + word;
				}
				LineToPut += ",";
				
				int i = WordCountVector.Document.get(id);
				for(int j=0; WordCountVector.TopicsClassificationMatrix[i] != null && j < WordCountVector.Topics.size() && WordCountVector.TopicsClassificationMatrix[i].length >j; ++j)
				{
					if (WordCountVector.TopicsClassificationMatrix[i][j] > 0)
					{
						bw.write(LineToPut +  ReverseTopics.get(j) + "\n");
					}
				}
				//bw.write(LineToPut);
			}
			bw.write("\n");
			
			/*
			// Write Topics -- This is the same as in the WordCountVector
			for (int i=0; i < ReverseTopics.size();++i)
			{
				if (i==0)
					LineToPut = "DocumentId," + ReverseTopics.get(i);
				else
					LineToPut += "," + ReverseTopics.get(i);
			}
			bw.write(LineToPut + "\n\n");
			
			// Write the words vector, each line first parameter is the document id - afterwards the word count of the matching column
			for (int i=0; i< WordCountVector.Document.size();++i)
			{
				for(int j=0; WordCountVector.TopicsClassificationMatrix[i] != null && j < WordCountVector.TopicsClassificationMatrix[i].length; ++j)
				{
					if (j==0) LineToPut = String.valueOf(ReverseDocumentIds.get(i)) + "," + String.valueOf(WordCountVector.TopicsClassificationMatrix[i][j]);
					else LineToPut += "," + String.valueOf(WordCountVector.TopicsClassificationMatrix[i][j]);
				}
				int StartVal = 0;
				if (WordCountVector.TopicsClassificationMatrix[i] != null ) StartVal = WordCountVector.TopicsClassificationMatrix[i].length;
				for (int j = StartVal; j < WordCountVector.Topics.size(); ++j)
				{
					if (j==0) LineToPut = String.valueOf(ReverseDocumentIds.get(i)) + "," + String.valueOf(0);
					else LineToPut += "," + String.valueOf(0);
				}
				bw.write(LineToPut+"\n");
			}
			bw.write("\n");
			*/
			bw.close();
		}
		catch(Exception ex) 
		{
			System.out.println("Failed to save file. error: " + ex.getMessage() + "\n" + ex.getStackTrace().toString());
		}
		bw = null;
		fis = null;
	}
}
