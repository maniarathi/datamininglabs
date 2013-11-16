import java.io.IOException;
import java.util.ArrayList;
import laur.dm.ar.*;


public class AR {
	
	public ArrayList Mine(String cacheFileName, double minSupport, double minConfidence) throws IOException
	{
		DBCacheReader dbReader = new DBCacheReader(cacheFileName);
		
		AprioriRules am = new AprioriRules();
		ArrayList retVal = am.findAssociations(dbReader, minSupport, minConfidence);
		
		
		return retVal;
	}
}
