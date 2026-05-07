
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author lukas
 *
 */
class LogOutTimer {
	private static final Logger log = LogManager.getLogger("LogOutTimer");	
	static DbConnect conn;
	
	LogOutTimer(final DbConnect conn) {
	    this.conn = conn;
	}
	
	
	/**
	 * 
	 */
	public static void startTimer() {
	        TimerTask action = new TimerTask() {
	            public void run() {
	        	conn.logout();
	        	log.debug("LogOut");
	            }
	        };
	        Timer timer = new Timer();
	    timer.schedule(action, 900000);
	    log.debug("Start logout timer");
	}	
}
