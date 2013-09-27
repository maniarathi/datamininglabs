/// Roee Ebenstein
/// 2013.09.23
////////////////////////////
// This is the main file - creates the vectors, and writes the files.
// Over here there's also an xml readers that extract the interesting sections of the xmls.

import java.io.File;
import java.io.FilenameFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AppLoader 
{
		
	static public void main(String[] params)
	{
		// Load the xmls and the dictionary
		String XmlsPath;
		WordCountVector.LoadDictionary();
		
		if (params.length == 0)
			XmlsPath = "../5243hw2Py/hw1/xmls/";
		else
			XmlsPath = params[0];
		
		// Build the vector
		/*ReadXmlAndBuildVector(XmlsPath);
		WordCountVector.DecreaseVectorSize();
		System.out.println("Finished building WordCountVector.");
		ReadXmlAndBuildWordListVector(XmlsPath);
		System.out.println("Finished building WordListVector.");
		
		// Write the vector to disk
		System.out.println("Finished building vector, starting to write WordCountVector file and TopicsVector file.");
		WordCountVector.WriteToFile("../output/WordCount.csv", "../output/TopicsVector.csv");
		System.out.println("Finished building vector, starting to write WordListVector file.");
		WordListVector.WriteToFile("../output/WordList.csv");
		System.out.println("Finished writing files.");*/
		
		// Start KNN Classification
		double split = 0.6;
		KNNPrediction.GetPredictions("../output/WordCount.csv",9341,"../output/predictKNN6040.txt",split);
		split = 0.8;
		KNNPrediction.GetPredictions("../output/WordCount.csv",9341,"../output/predictKNN8020.txt",split);
		//KNNPrediction.GetDataSource("output/WordCount.csv");
		//KNNPrediction.TrainKNNModel();
		//KNNPrediction.TestKNNModel();
		
		// Start Naive Bayes Classification
	}
	
	// Read all the xmls in a directory - and build the vectors
	static public void ReadXmlAndBuildVector(String XmlsPath)
	{
		   // Get all XML files
		  File dir = new File(XmlsPath);
		  File [] files = dir.listFiles(new FilenameFilter() {
		      @Override
		      public boolean accept(File dir, String name) {
		          return name.endsWith(".xml");
		      }
		  });

		  // For each file, parse the xml
		  if (files != null)
		  {
			  for (File xmlfile : files) 
			  {
				  // Get each article (within the large XML
			      Document doc = XmlReader.ReturnXml(xmlfile.getPath());
			      Node[] articles = XmlReader.getElements(doc, "REUTERS");
			      
			      if (articles != null)
			      {
			    	  // Get the properties of each XML - and add it to the vector
				      for (Node article : articles)
				      {
				    	  if (article.getNodeType() == Node.ELEMENT_NODE) 
				    	  {  
				  			Element eArticle = (Element) article;
				  			
				  			// For each article - extract the topics, locations, and text
				  			int id = Integer.parseInt(eArticle.getAttribute("NEWID"));
				  			
							NodeList nTopics = null;
							if (eArticle.getElementsByTagName("TOPICS").getLength()> 0)
								nTopics = ((Element)eArticle.getElementsByTagName("TOPICS").item(0)).getElementsByTagName("D");
							NodeList nLocations = null;
							if (eArticle.getElementsByTagName("PLACES").getLength()> 0)
								nLocations = ((Element)eArticle.getElementsByTagName("PLACES").item(0)).getElementsByTagName("D");
							
							// Get Text and subjects
							Node nText = eArticle.getElementsByTagName("TEXT").item(0);
							NodeList nTitle = ((Element)nText).getElementsByTagName("TITLE");
							NodeList nDateLine = ((Element)nText).getElementsByTagName("DATELINE");
							NodeList nBody = ((Element)nText).getElementsByTagName("BODY");
							
							// Extract Texts
							String[] Topics = XmlReader.getElementContent(nTopics);
							String[] Locations = XmlReader.getElementContent(nLocations);
							String[] Title = XmlReader.getElementContent(nTitle);
							String[] DateLine = XmlReader.getElementContent(nDateLine);
							String[] Body = XmlReader.getElementContent(nBody);
							
							WordCountVector.AddDocumentToTopicVector(id,Topics, Title, Body);
							
				  			System.out.println("Processed Document " + id);
				    	  }
				      }
			      }
			  }
		  }
		  
		  // Outputs the results
		  System.out.println("finished creating WordCount vector.\nThere are " + WordCountVector.Words.size() + " Words, and " + 
				  			WordCountVector.Topics.size() + " Topics.\n"+
				  			"There are " + WordCountVector.Document.size() + " Documents.");
	}
	
	static public void ReadXmlAndBuildWordListVector(String XmlsPath)
	{
		   // Get all XML files
		  File dir = new File(XmlsPath);
		  File [] files = dir.listFiles(new FilenameFilter() {
		      @Override
		      public boolean accept(File dir, String name) {
		          return name.endsWith(".xml");
		      }
		  });

		  // For each file, parse the xml
		  if (files != null)
		  {
			  for (File xmlfile : files) 
			  {
				  // Get each article (within the large XML
			      Document doc = XmlReader.ReturnXml(xmlfile.getPath());
			      Node[] articles = XmlReader.getElements(doc, "REUTERS");
			      
			      if (articles != null)
			      {
			    	  // Get the properties of each XML - and add it to the vector
				      for (Node article : articles)
				      {
				    	  if (article.getNodeType() == Node.ELEMENT_NODE) 
				    	  {  
				  			Element eArticle = (Element) article;
				  			
				  			// For each article - extract the topics, locations, and text
				  			int id = Integer.parseInt(eArticle.getAttribute("NEWID"));
				  			
							NodeList nTopics = null;
							if (eArticle.getElementsByTagName("TOPICS").getLength()> 0)
								nTopics = ((Element)eArticle.getElementsByTagName("TOPICS").item(0)).getElementsByTagName("D");
							NodeList nLocations = null;
							if (eArticle.getElementsByTagName("PLACES").getLength()> 0)
								nLocations = ((Element)eArticle.getElementsByTagName("PLACES").item(0)).getElementsByTagName("D");
							
							// Get Text and subjects
							Node nText = eArticle.getElementsByTagName("TEXT").item(0);
							NodeList nTitle = ((Element)nText).getElementsByTagName("TITLE");
							NodeList nDateLine = ((Element)nText).getElementsByTagName("DATELINE");
							NodeList nBody = ((Element)nText).getElementsByTagName("BODY");
							
							// Extract Texts
							String[] Topics = XmlReader.getElementContent(nTopics);
							String[] Locations = XmlReader.getElementContent(nLocations);
							String[] Title = XmlReader.getElementContent(nTitle);
							String[] DateLine = XmlReader.getElementContent(nDateLine);
							String[] Body = XmlReader.getElementContent(nBody);
							
							WordListVector.AddDocumentToTopicVector(id,Topics, Title, Body);
							
				  			System.out.println("Processed Document " + id);
				    	  }
				      }
			      }
			  }
		  }
		  
		  // Outputs the results
		  System.out.println("finished creating WordList vector.\nThere are " + WordCountVector.Words.size() + " Words, and " + 
				  			WordCountVector.Topics.size() + " Topics.\n"+
				  			"There are " + WordCountVector.Document.size() + " Documents.");
	}
}
