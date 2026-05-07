import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jdom2.DataConversionException;

/**
 * @author lukas
 *
 */
public class CycleData implements WindowListener {

    private static final Logger log = LogManager.getLogger("CycleData");

	ArrayList<DataSet> data;
	Bats main;
	FormatT format;
	Run run;
	
	/**
	 * @param main
	 * @param format 
	 */
	public CycleData(Bats main, FormatT format) {
		this.main = main;
		this.format = format;
		run=null;
	}
	
	/**
	 * @param run
	 */
	public void getCycleTable(Run run) {
		this.run=run;
        	log.debug("Edit run: "+run.run);
		ArrayList<Cycle> cycList=null;
		DbCycle cycleConn= main.db.getConn();
		log.debug(cycleConn);
        	if (cycleConn!=null) {
        		cycList = cycleConn.getCycleList(run.run);
			if (cycList!=null) {
			    CycleTable table = new CycleTable(cycList, run, format, main);
			    table.setModal(true);
			    table.addWindowListener(this);
			    //Display the window.
			    table.pack();
			    table.setVisible(true);
			}
    	} else {
    	    
    		log.debug("Could not connect to DB.");
    	}
	}
	
	/**
	 * @param run 
	 * @param cTable 
	 */
	public void getNextCycleTable(Run run, CycleTable cTable) {
//	    cTable.dispatchEvent(new WindowEvent(cTable, WindowEvent.WINDOW_CLOSED));
//		cTable.setVisible(false);
//		cTable.dispose();
		
		this.run=run;
        	log.debug("Edit run: "+run.run);
		ArrayList<Cycle> cycList=null;
		DbCycle cycleConn= main.db.getConn();
		log.debug(cycleConn);
        	if (cycleConn!=null) {
        		cycList = cycleConn.getCycleList(run.run);
			if (cycList!=null) {
			    cTable.updateTable(cycList, run);
			    cTable.setModal(true);
			    cTable.addWindowListener(this);
			    //Display the window.
			    cTable.pack();
			    cTable.setVisible(true);
			}
    	} else {
    	    
    		log.debug("Could not connect to DB.");
    	}
	}
	
	
	public void windowActivated(WindowEvent e) {;}

	public void windowClosing(WindowEvent e) {
		try {
			if (Setting.getElement("/bat/isotope/db").getAttribute("active").getBooleanValue()) {
				main.db.updateRun(run);
				main.dataRecalc();

			} else {
				main.db.updateRun(run);
				main.dataRecalc();
			}
		} catch (DataConversionException e1) {
			main.db.updateRun(run);
			main.dataRecalc();
		}
	}

	public void windowClosed(WindowEvent e) {;}

	public void windowDeactivated(WindowEvent e) {;}

	public void windowDeiconified(WindowEvent e) {;}

	public void windowIconified(WindowEvent e) {;}

	public void windowOpened(WindowEvent e) {;}


}
