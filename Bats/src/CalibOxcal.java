import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.swing.JDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * @author lukas
 *
 */
public class CalibOxcal extends JDialog {
	private static final Logger log = LogManager.getLogger("CalibOxcal");
	
	/**
	 * @param sample
	 */
	public CalibOxcal(Sample sample) {
 		String urlString;
		try {
			urlString = Setting.getString("/bat/isotope/calib/oxcal")+"?Command=R_Date(%22"+URLEncoder.encode(sample.desc1.substring(0,5),"UTF-8")+"%22,"+sample.age+","+sample.age_err+")%3B";
			BrowserLaunch.openURL(urlString.trim());
		} catch (UnsupportedEncodingException e) {
			log.error("Could not encode encode");
		}  
	}
}
