/// Roee Ebenstein
/// 2013.09.23
////////////////////////////
// This is an xml reader, it will do the processing of XML for the rest of the application,
// so no one needs to know almost anything about the xml (though they get an xml or a node).
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class XmlReader 
{
	// Opens an xml file, loads it, and returns it (very straight forward)
	static public Document ReturnXml(String FilePath)
	{
		Document retVal = null;
		try {
			 
			File fXmlFile = new File(FilePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			retVal = doc;
		 

			} 
			catch (Exception e) 
			{
		    	e.printStackTrace();
		    	
		    }
		return retVal;
	}
	
	// This will return a string out of a node, by a tag name
	static public String[] getElementContent(Document doc,String TagNameToGet)
	{
		String[] retVal;
		
		NodeList listToScan = doc.getElementsByTagName(TagNameToGet);
		retVal = getElementContent(listToScan);
		
		return retVal;
	}
	
	// Returns all the node content (and subnodes)
	static public String[] getElementContent(NodeList nd)
	{
		String[] retVal;

		if (nd.getLength() > 0)
		{
			retVal = new String[nd.getLength()];
		}
		else retVal = null;
		
		for (int i = 0; i < nd.getLength(); i++) {
			 
			Node nNode = nd.item(i);
	 
			retVal[i] = nNode.getTextContent();
		}
		
		return retVal;
	}
	
	// Returns a set of nodes from a docment, by a tagname
	static public Node[] getElements(Document doc, String TagNameToGet)
	{
		Node[] retVal;
		
		NodeList listToScan = doc.getElementsByTagName(TagNameToGet);
		retVal = new Element[listToScan.getLength()];
		
		for (int i = 0; i < listToScan.getLength(); i++) {
			 
			Node nNode = listToScan.item(i);
	 
			retVal[i] = nNode;
		}
		
		return retVal;
	}
}
