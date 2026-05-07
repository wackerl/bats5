import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jfree.util.Log;


@SuppressWarnings("unchecked")
class SortPos implements Comparator<DataSet> {
    
    private static final Logger log = LogManager.getLogger("SortPos");

	public int compare(DataSet aa, DataSet bb ){
		int result = 0;
		Integer a = (Integer)(aa.get("position"));
		Integer b = (Integer)(bb.get("position"));
		if (a==null) {
		    a=0;
		    log.debug("Position for target "+aa.get("label")+" is not defined (set 0).");
		}
		if (b==null) {
		    b=0;
		    log.debug("Position for target "+bb.get("label")+" is not defined (set 0).");
		}
		try {
			if ((result = a.compareTo(b)) == 0) {
				String a1 = (String) ((DataSet)aa).get("label");
				String b1 = (String)(((DataSet)bb).get("label"));
				result = a1.compareTo(b1);
				if ((result = a1.compareTo(b1)) == 0) {
					Integer a2 = (Integer) (aa).get("recno");
					Integer b2 = (Integer) (bb).get("recno");
					result = a2.compareTo(b2);
				}
			}
		}  catch (NullPointerException e) {
			result =0;
		}
		return result;
	}
}
