/*
 *
 * Bats.java
 *
 */
//import org.apache.logging.log4j.*;
//import org.apache.log4j.xml.DOMConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.DataConversionException;
import org.jdom2.Document;

//import org.apache.logging.log4j.core.config.ConfigurationSource;
//import org.apache.logging.log4j.core.config.Configurator;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;


/**
 * @author lukas
 *
 */
public class Bats extends JFrame implements WindowListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static {
        System.setProperty("log4j.configurationFile", System.getProperty("user.dir") +  "/settings/log/log_conf.xml");
    }
    private static final Logger log = LogManager.getLogger(Bats.class);
	
    JSplitPane splitPane;
    InfoPanel infoPanel; 
    MenuBar menuBar;
    TBarIcon tBarIcons;
    JToolBar toolBar3;
//    TBarStdBl tbarStdBl;
    TBarAction tba;
    
    DataTable runMeanPanel;
    DataTable runPanel;
    DataTable samplePanel;
    XhtmlPanel2 xPanel;
    String xFile;
    DbConnect db;
    CycleData cycDat;
    Calib calib;
    
    StdBlTable stdPanel;
    StdBlTable std13Panel;
    StdBlTable stdPuPanel;
    StdBlTable blankPanel;
    StdBlGraph stdPlot;
    StdBlGraph std13Plot;
    StdBlGraph blankPlot;
    
    /**
     * 
     */
    Action act;
    
    String runStart = null;
    String runEnd = null;
    
    Calc data;
	static Document setDoc;

	// Ask AWT which menu modifier we should be using.
//	final static int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    /**
     * 
     */
    public Bats() {  
	
	    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    this.addWindowListener(this);
	    this.setLayout(new BorderLayout());		
	    JFrame.setDefaultLookAndFeelDecorated(true);		
	    this.startup();
	}
    
    /**
     * 
     */
    public void startup() {
		new Setting();
		log.debug("Isotope is set to: "+Setting.isotope);  
		this.reInit();
    }
    
    /**
     * 
     */
    public void reInit() {
    	Setting.init(false);
    	Setting.getString("/bat/isotope/path");
    	File file = new File(Setting.getString("/bat/isotope/path"));
    	if(!file.isDirectory()) {
			JFileChooser fc = new JFileChooser();
			log.debug("new file chooser");
	//		fc.setCurrentDirectory(new File(((JButton)e.getSource()).getText()).getParentFile());
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setSelectedFile(new File(Setting.homeDir));
			fc.setDialogTitle("Open directory");
			fc.setFileHidingEnabled(true);
			//Create the file chooser
			int returnVal = fc.showOpenDialog(fc);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			Setting.getElement("/bat/isotope/path").setText(fc.getSelectedFile().toString());
			log.debug("Set work path to "+fc.getSelectedFile().toString());
		}else{
		    log.debug("Open command cancelled by user.");
//			value = tf.getText();
//			Setting.getElement(actCom).setText(value);		
//			log.debug("Focus action performed "+actCom+" / value: "+value);
		}

    	}
    	this.initDb();
    	this.initPanel();
    }
    
    private void initPanel() {
    	String title = Setting.getString("/bat/general/frame/main/name");
	this.setTitle(title+"  version "+Setting.version+" for "+Setting.isotope);
        int height = Setting.getInt("/bat/general/frame/main/height");
        int width = Setting.getInt("/bat/general/frame/main/width");
        this.getContentPane().setPreferredSize(new Dimension(width,height));
        this.setBackground(new Color(0xD7E0ED));
        
        if (Setting.isotope.contains("C14")) {
        	calib= new Calib(Setting.batDir+"/calib/"+Setting.getString("/bat/calib/curve"));
        }
	// Create menubar and toolbar
	act = new Action(this);
	log.debug("Action started");
	tBarIcons = new TBarIcon(act);
	tBarIcons.refresh();
//	tbarStdBl = new TBarStdBl();
	toolBar3 = new TBarOrder(act);
	tba= new TBarAction(this);
	menuBar = new MenuBar(act);
	log.debug("Menu bar created");
	this.getContentPane().removeAll();
	FlowLayout layout = new FlowLayout();
	layout.setAlignment(FlowLayout.LEFT);
	JPanel tbp = new JPanel(layout);
	tbp.add(tBarIcons);		
	tbp.add(tba);	
	tbp.add(toolBar3);		
	log.debug("Toolbar added");
	this.setJMenuBar(menuBar);
	this.getContentPane().add(tbp, BorderLayout.NORTH);		
	// Create the main split content panes
	    
	infoPanel = new InfoPanel(this);
	log.debug("infoPanel created");
		
	splitPane = new JSplitPane();
	this.split();		
	splitPane.setOpaque(true);		
	 this.getContentPane().add(splitPane, BorderLayout.CENTER);		
	//Display the window.
	 this.pack();
	 this.setVisible(true);	
   }
    
    private void initDb() {

    	
    	File dir = new File(Setting.magDir);
    	if (!dir.exists()) {
    		dir.mkdirs();
    		log.debug("Directory "+dir+" created.");
    	}

//	    try {
	    	try {
				if (Setting.getElement("/bat/isotope/db").getAttribute("active").getBooleanValue()) {
					if (Setting.getElement("/bat/isotope/db/sql").getAttribute("active").getBooleanValue()) {
						db = new IODb(this);
					} else if (Setting.getElement("/bat/isotope/db/sql-import").getAttribute("active").getBooleanValue()) {
						db = new DbMySqlConnect(this);
					} else {
						Setting.getElement("/bat/isotope/db").getAttribute("active").setValue(Boolean.toString(false));
						Setting.db=null;log.warn("No DB selected. Could not set-up any DB!");
					}
				}
	    	} catch (DataConversionException e) {
	    		Setting.db=null; log.debug("No DB import, file mode only."); 
			}
		
    	if (Setting.db!=null) {
    	    FormatT format = new FormatT("cycle", this);
    	    cycDat = new CycleData(this, format);	  
    	    log.debug("DB setup: "+Setting.db_name);
    	}	
	data = Setting.initCalcIso(Setting.isotope);		    	

   }
    
    /**
     * 
     */
    public void split() {
    	
        int width = Setting.getInt("/bat/general/frame/main/width");

        JPanel rightPanel = new JPanel();
	    rightPanel.setLayout(new BorderLayout());
	    rightPanel.add(tabbedPane(), BorderLayout.CENTER);
        int divide = Setting.getInt("/bat/general/frame/split/width");
	    splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

		if (Setting.getBoolean("/bat/general/frame/split/right")) {
		    splitPane.setDividerLocation(width-divide-2);
		    splitPane.setRightComponent(infoPanel);
		    splitPane.setDividerSize(2);
		    splitPane.setLeftComponent(rightPanel);
			splitPane.setResizeWeight(1);
	    } else {
		    splitPane.setDividerLocation(divide);
		    splitPane.setLeftComponent(infoPanel);
		    splitPane.setDividerSize(2);
		    splitPane.setRightComponent(rightPanel);
			splitPane.setResizeWeight(0);
	    }   	
    }
    
    /**
     * @return Bats Pane with tabbs
     */
    public JComponent tabbedPane(){
        int fs = Setting.getInt("/bat/general/font/p");
    	String ft = Setting.getString("/bat/general/font/type");
    	final JTabbedPane tabbedPane = new JTabbedPane();
    	tabbedPane.setFont(new Font(ft,1,fs));
    	tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.setAutoscrolls(true);
        int n=0;

        JComponent panel1 = makeRunMeanPanel();
    	String name = Setting.getString("/bat/isotope/tab_pane/tab1");
        tabbedPane.addTab(name, null, panel1, name);
        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_1);
 
        JComponent panel2 = makeRunPanel();
    	name = Setting.getString("/bat/isotope/tab_pane/tab2");
        tabbedPane.addTab(name, null, panel2, name);
        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_2);

        JComponent panel3 = makeBlankPanel();
    	name = Setting.getString("/bat/isotope/tab_pane/tab3");
        tabbedPane.addTab(name, null, panel3, name);
        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_3);
                
        JComponent panel4 = makeStdPanel();
    	name = Setting.getString("/bat/isotope/tab_pane/tab4");
        tabbedPane.addTab(name, null, panel4, name);
        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_4);

        if (Setting.isotope.contains("C14") || Setting.isotope.contains("P")){
	        JComponent panel5 = makeStd13Panel();
	    	name = Setting.getString("/bat/isotope/tab_pane/tab5");
	        tabbedPane.addTab(name, null, panel5, name);
	        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_5);
    	}

        if (Setting.isotope.contains("P")){
	        JComponent panel8 = makeStdPuPanel();
	    	name = Setting.getString("/bat/isotope/tab_pane/tab8");
	        tabbedPane.addTab(name, null, panel8, name);
	        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_8);
    	}

        JComponent panel6 = makeSamplePanel();
    	name = Setting.getString("/bat/isotope/tab_pane/tab6");
        tabbedPane.addTab(name, null, panel6, name);
        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_6);

        JComponent panel7 = makeXhtmlPanel();
    	name = Setting.getString("/bat/isotope/tab_pane/tab7");
        tabbedPane.addTab(name, null, panel7, name);
        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_7);

//        JComponent panel8 = makeHtmlPanel();
//    	name = Setting.getString("/bat/isotope/tab_pane/tab7");
//        tabbedPane.addTab(name, null, panel8, name);
//        tabbedPane.setMnemonicAt(n++, KeyEvent.VK_8);
//
//        tabbedPane.getUI().;
//        tabbedPane.setUI ( new MetalTabbedPaneUI ()
//        {
//            protected MouseListener createMouseListener ()
//            {
//                return new CustomAdapter ( tabbedPane );
//            }
//        } );        
        
        log.debug("Tabbed pane created");
        return tabbedPane;
        
        //Uncomment the following line to use scrolling tabs.
        //tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }
 
    protected JComponent makeXhtmlPanel() {
        FormatT format = new FormatT("output", this);
        xFile = Setting.homeDir+"/"+data.magazine+"/"+data.magazine+".xhtml";
    	xPanel = new XhtmlPanel2(data, xFile, format);
    	return xPanel;
    }
    
    protected JComponent makeRunMeanPanel() {
    	FormatT format = new FormatT("passes_mean", this);    	
    	runMeanPanel = new DataTable(this, data, "runSample", format, true);
		return runMeanPanel;
    }
    
    protected JComponent makeRunPanel()
    {
    	FormatT format = new FormatT("passes", this);
    	runPanel = new DataTable(this, data, "runAll", format, true);	    	
		return runPanel;
    }
    
	protected JComponent makeStdPanel(){
    	FormatT format = new FormatT("std", this);
     	stdPanel = new StdBlTable(this, data, "allStd", format);
		return stdPanel;
    }
    
	protected JComponent makeStd13Panel(){
    	FormatT format = new FormatT("std13", this);
    	std13Panel = new StdBlTable(this, data, "allStd", format);
		return std13Panel;
    }
    
	protected JComponent makeStdPuPanel(){
    	FormatT format = new FormatT("stdPu", this);
    	stdPuPanel = new StdBlTable(this, data, "allStd", format);
		return stdPuPanel;
    }
    
	protected JComponent makeBlankPanel(){
    	FormatT format = new FormatT("blank", this);
    	blankPanel = new StdBlTable(this, data, "allBlank", format);
		return blankPanel;
    }
    
    protected JComponent makeSamplePanel() {
		FormatT format = new FormatT("mean", this);
		samplePanel = new DataTable(this, data, "sample", format, false);	    	
		return samplePanel;
    } 
       
    /**
     * 
     */
 	public void dataUpdate() {
		log.debug("Data was updated!");
		this.dataRecalc();		
    }
       
    /**
     * 
     */
	public void dataRecalc() {
    	if (Setting.getBoolean("/bat/general/calc/autocalc")==true) {
			this.dataRecalc2();
    	}
   }
		
    /**
     * 
     */
	public void dataRecalc2() {
		if (!Setting.no_data) {
			tba.update("recalc", true);
			log.debug("Data will be recalculated!");
//			log.debug(data.runListL.size());
	    	data.calcAll();
	    	infoPanel.update(data);
			log.debug("Data was recalculated!");
			tba.update("table upd.", true);
			runMeanPanel.updateTableDataChanged(data);
			runPanel.updateTableDataChanged(data);
			samplePanel.updateTableDataChanged(data);
			stdPanel.updateTableDataChanged(data);
	        if (Setting.isotope.contains("C14")) {
				std13Panel.updateTableDataChanged(data);
	        }
	        if (Setting.isotope.contains("P")) {
				std13Panel.updateTableDataChanged(data);
				stdPuPanel.updateTableDataChanged(data);
	        }
			blankPanel.updateTableDataChanged(data);
			if (Setting.getBoolean("/bat/general/file/autosave/result")) {
				tba.update("save output", true);
		        xFile = Setting.magDir+"/"+data.calcSet+".xhtml";
				xPanel.update(data, xFile);	
			}
			if (Setting.getBoolean("/bat/general/file/autosave/bats")) {
				tba.update("save bats", true);
				IOFile.saveXML(Setting.homeDir+"/"+Setting.magazine+"/"+data.calcSet+".bats", data);
				log.info("Saved file: "+Setting.homeDir+"/"+Setting.magazine+"/"+data.calcSet+".bats");
			}
			log.debug("Tables were updated!");
			tba.update("ok", false);
		} else {
	    	infoPanel.update(data);
			runMeanPanel.updateTableDataChanged(data);
			runPanel.updateTableDataChanged(data);
			samplePanel.updateTableDataChanged(data);
			stdPanel.updateTableDataChanged(data);
	        if (Setting.isotope.contains("C14")) {
				std13Panel.updateTableDataChanged(data);
	        }
	        if (Setting.isotope.contains("P")) {
				std13Panel.updateTableDataChanged(data);
				stdPuPanel.updateTableDataChanged(data);
	        }
			blankPanel.updateTableDataChanged(data);
			if (Setting.getBoolean("/bat/general/file/autosave/result")) {
				tba.update("save output", true);
		        xFile = Setting.magDir+"/"+data.calcSet+".xhtml";
				xPanel.update(data, xFile);	
			}
			tba.update("no data loaded!", false);
		}
		menuBar.menu_update();
		setJMenuBar(menuBar);
		tBarIcons.refresh();
		tBarIcons.repaint();
		tBarIcons.setOpaque(true);
	}
	
	
    /**
     * This function updates all the tables, with headers!!! (no recalculation)
     */
	public void tableUpdate() {
		runMeanPanel.updateTableStructureChanged();
		runPanel.updateTableStructureChanged();
		samplePanel.updateTableStructureChanged();
		stdPanel.updateTableStructureChanged();
        if (Setting.isotope.contains("C14")) {
			std13Panel.updateTableStructureChanged();
        }
        if (Setting.isotope.contains("P")) {
			std13Panel.updateTableStructureChanged();
			stdPuPanel.updateTableStructureChanged();
        }
		blankPanel.updateTableStructureChanged();
		if (Setting.getBoolean("/bat/general/file/autosave/result")&&!Setting.no_data) {
	        xFile = Setting.magDir+"/"+data.calcSet+".xhtml";
			xPanel.update(data, xFile);	
		}
		log.debug("Tables (with header) were updated!");
    }  
    
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		DOMConfigurator.configure(Setting.batDir+"/log/log_conf.xml");
//	    	System.setProperty("logFilename", System.getProperty("user.dir") + File.separator + "settings/log/bats.log");
	    	
//	    	String log4jConfigFile = System.getProperty("user.dir") +  "/settings/log/log_conf.xml";
//	    	ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jConfigFile));
//	    	Configurator.initialize(null, source);
//	    	
//	    	org.apache.logging.log4j.core.LoggerContext ctx = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
//		ctx.reconfigure();
//
		
		log.debug("java.class.version: "+System.getProperty("java.class.version"));
		log.debug("java.class.path: "+System.getProperty("java.class.path"));
		log.debug("java.version: "+System.getProperty("java.version"));
		log.debug("java.vm.version: "+System.getProperty("java.vm.version"));
		log.debug("java.home: "+System.getProperty("java.home"));
		log.debug("java.vendor: "+System.getProperty("java.vendor"));
		log.debug("java.compiler: "+System.getProperty("java.compiler"));
		log.debug("java.home: "+System.getProperty("java.home"));
		log.debug("java.home: "+System.getProperty("java.home"));
		log.debug("os.arch: "+System.getProperty("os.arch"));
		log.debug("os.name: "+System.getProperty("os.name"));
		log.debug("user.dir: "+System.getProperty("user.dir"));
		log.debug("user.home: "+System.getProperty("user.home"));
		log.debug("user.name: "+System.getProperty("user.name"));
		log.debug("charset: "+Charset.defaultCharset());
		log.debug("max memory: "+Runtime.getRuntime().maxMemory()/1048576+" MB");
		java.util.Locale.setDefault(new Locale( "en" , "CH" ));
		
		if (System.getProperty("os.name").startsWith("Mac OS X")) {
			System.setProperty("apple.awt.graphics.UseQuartz","false");        
//			log.debug("Set look and feel for osx");
//			// Only override the UI's necessary for ColorChooser and
//	        // FileChooser:
//			System.setProperty("apple.laf.useScreenMenuBar", "true");
//			Set includes = new HashSet();
//	        includes.add("ColorChooser");
//	        includes.add("FileChooser");
//	        includes.add("Component");
//	        includes.add("Browser");
//	        includes.add("Tree");
//	        includes.add("SplitPane");
//	        includes.add("Menue");
//	        QuaquaManager.setIncludedUIs(includes);
//	        System.setProperty("Quaqua.tabLayoutPolicy","wrap");
//	
//	        // set the Quaqua Look and Feel in the UIManager
//	        try { 
//	             UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
//	        } catch (Exception e) {
//	        	log.debug("Could not set osx look and feel!");
//	        }
		}

		
		new Bats();
	}

	public void windowActivated(WindowEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	public void windowClosed(WindowEvent e)
	{
//    	Setting.getElement("/bat/general/frame/main/width").setText(Integer.toString(this.getWidth()));
//		Setting.save();
//		log.debug("Applications closed!");
//		System.exit(0);
	}

	public void windowClosing(WindowEvent e)
	{
		int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.YES_OPTION) {
			log.debug(getContentPane().getWidth()+"/"+getContentPane().getHeight());
		   	Setting.getElement("/bat/general/frame/main/width").setText(Integer.toString(this.getContentPane().getWidth()));
		   	Setting.getElement("/bat/general/frame/main/height").setText(Integer.toString(this.getContentPane().getHeight()));
			Setting.save();
			log.debug("Setting are saved!");
			System.exit(0);
			log.debug("exit");
		}
		else {;}
	}

	public void windowDeactivated(WindowEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	public void windowDeiconified(WindowEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	public void windowIconified(WindowEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	public void windowOpened(WindowEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
//	private static class CustomAdapter extends MouseAdapter
//	{
//	    private JTabbedPane tabbedPane;
//
//	    public CustomAdapter ( JTabbedPane tabbedPane )
//	    {
//	        super ();
//	        this.tabbedPane = tabbedPane;
//	    }
//
//	    public void mousePressed ( MouseEvent e )
//	    {
//	        final int index = tabbedPane.getUI ().tabForCoordinate ( tabbedPane, e.getX (), e.getY () );
//	        if ( index != -1 )
//	        {
//	            if ( SwingUtilities.isLeftMouseButton ( e ) ) {
//	                if ( tabbedPane.getSelectedIndex () != index )
//	                {
//	                    tabbedPane.setSelectedIndex ( index );
//	                }
//	                else if ( tabbedPane.isRequestFocusEnabled () )
//	                {
//	                    tabbedPane.requestFocusInWindow ();
//	                }
//	            } else if ( SwingUtilities.isRightMouseButton ( e ) ) {
//	        	
//	        	log.debug(tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()).getName());
//	            }	        
//	        }
//	    }
//	}

}
