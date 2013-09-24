import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class XmlReader 
{
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
	
	static public String[] getElementContent(Document doc,String TagNameToGet)
	{
		String[] retVal;
		
		NodeList listToScan = doc.getElementsByTagName(TagNameToGet);
		retVal = getElementContent(listToScan);
		
		return retVal;
	}
	
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
