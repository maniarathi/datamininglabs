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
		String XmlsPath;
		
		if (params.length == 0)
			XmlsPath = "../5243hw2Py/hw1/xmls/";
		else
			XmlsPath = params[0];
		
		ReadXmlAndBuildVector(XmlsPath);
	}
	
	static public void ReadXmlAndBuildVector(String XmlsPath)
	{
		   
		  File dir = new File(XmlsPath);
		  File [] files = dir.listFiles(new FilenameFilter() {
		      @Override
		      public boolean accept(File dir, String name) {
		          return name.endsWith(".xml");
		      }
		  });

		  // Getting the articles...
		  if (files != null)
		  {
			  for (File xmlfile : files) 
			  {
			      Document doc = XmlReader.ReturnXml(xmlfile.getPath());
			      Node[] articles = XmlReader.getElements(doc, "REUTERS");
			      
			      if (articles != null)
			      {
				      for (Node article : articles)
				      {
				    	  if (article.getNodeType() == Node.ELEMENT_NODE) 
				    	  {  
				  			Element eArticle = (Element) article;
				  			
				  			// For each article - extract the topics, locations, and text
				  			int id = Integer.parseInt(eArticle.getAttribute("NEWID"));
				  			
							NodeList nTopics = ((Element)eArticle.getElementsByTagName("TOPICS").item(0)).getElementsByTagName("D");
							NodeList nLocations = ((Element)eArticle.getElementsByTagName("PLACES").item(0)).getElementsByTagName("D");
							
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
							
				  			System.out.println("Processed Document " + id);
				    	  }
				      }
			      }
			  }
		  }
	}
}
