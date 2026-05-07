import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * @author lukas
 *
 */
public class StdNom {
	private static final Logger log = LogManager.getLogger("StdNom");
		
	File file;
	
	/**
	 * 
	 */
	public ArrayList<StdData> stdList = new ArrayList<StdData>();
	StdNomTable stdPanel;
	
	StdNom(){
	    file = new File(Setting.batDir+"/"+Setting.isotope+"/standards.xml");
	    Class cl;                        // this is not elegant, but avoids null pointer exception
	    log.debug("stdNom");
	    try {
		    log.debug("DB");
	    	cl = Setting.db.getClass();
	    } catch (NullPointerException e) {
		    log.debug("file");
		cl = IOFile.class;
	    }
	    try {
		if (Setting.getElement("/bat/isotope/db/sql").getAttribute("active").getBooleanValue() && Setting.getElement("/bat/isotope/db").getAttribute("active").getBooleanValue()) {
//	    if (cl ==IODb.class) {
        		openDB(Setting.db);
        		} else {
        		if (file.exists()){
        			try{
        				open(file);
        				log.debug("Standards loaded from: "+file);
        			} catch (IOException e) {
        				String message = String.format( "<html>File could not be opend: <br>"+file+"<br>(IO exception)</html>");
        				JOptionPane.showMessageDialog( null, message );
        				log.error("File could not be opend: "+file+" (IO exception)");
        				addStd();				
        			}
        		}
        		else {
        			String message = String.format( "<html>File could not be opend: <br>"+file+"</html>");
        			JOptionPane.showMessageDialog( null, message );
        			log.error("File could not be opend: "+file+" (IO exception)");
        			addStd();
        		}		
		}
	    } catch (HeadlessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (DataConversionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    stdPanel = new StdNomTable(this);
	}
	
	/**
	 * 
	 */
	public void showStd() {
	    if (stdPanel== null) {
		this.openDB(Setting.db);
		log.debug("stdPanel is null");
	    }
		//Create and set up the window.
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("Standards");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		frame.addWindowListener((WindowListener) this);
		JMenuBar menuBar = new StdNomTableMenu(this);
		frame.setJMenuBar(menuBar);
		StdNomTableTBar toolBar = new StdNomTableTBar(this);
		frame.getContentPane().add(toolBar, BorderLayout.NORTH); 		
		frame.getContentPane().add(stdPanel,BorderLayout.SOUTH);
		//Display the window.
		frame.pack();
		frame.setVisible(true);
		log.debug("finish");
	}
	
	/**
	 * @param stdName
	 * @return if it is an active standard
	 */
	public Boolean isStd(String stdName) {
		Boolean a = false;
		if (!(stdName == null)) {
			for (int i=0;i<stdList.size();i++) {
				if (stdList.get(i).active && !stdList.get(i).blank && stdList.get(i).name.equalsIgnoreCase(stdName)) {
					a = true;
//					log.debug("Is std: "+stdName);
				}
			}
		}
		return a;
	}
	
	/**
	 * @param stdName
	 * @return if it is an active standard/blank
	 */
	public Boolean isStdBl(String stdName) {
		Boolean a = false;
		if (!(stdName == null)) {
			for (int i=0;i<stdList.size();i++) {
				if (stdList.get(i).active && stdList.get(i).name.equalsIgnoreCase(stdName)) {
					a = true;
					log.debug("Is std or blank: "+stdName);
				}
			}
		}
		return a;
	}
	
	/**
	 * @param stdName
	 * @return if it is an active blank
	 */
	public Boolean isBl(String stdName) {
		Boolean a = false;
		if (!(stdName == null)) {
			for (int i=0;i<stdList.size();i++) {
				if ((stdList.get(i).active && stdList.get(i).blank) && stdList.get(i).name.equalsIgnoreCase(stdName)) {
					a = true;
//					log.debug("Is blank: "+stdName);
				}
			}
		}
		return a;
	}
	
	/**
	 * @param stdName
	 * @param active 
	 * @return sets standard/blank active/inactive
	 */
	public Boolean setActive(String stdName, Boolean active) {
		Boolean a = false;
//		log.debug(stdName+"---"+active);
		if (!(stdName == null) && !(active == null)) {
			for (int i=0;i<stdList.size();i++) {
				if (stdList.get(i).name.equalsIgnoreCase(stdName)) {
					stdList.get(i).active = active;
//					log.debug("Set "+stdList.get(i).name + " (std/bl: "+active+")");
				}
			}
		}
		return a;
	}
	
	/**
	 * @param stdName
	 * @return if it is an active standard
	 * @throws NullPointerException 
	 */
	public StdData getStd(String stdName) {
		StdData std = null;
		if (!(stdName == null)) {
			for (int i=0;i<stdList.size();i++) {
				if (stdList.get(i).name.equalsIgnoreCase(stdName)) {
					std = stdList.get(i);
				}
			}
		}
		return std;
	}
	
	/**
	 * 
	 */
	public void addStd() {
		StdData std = new StdData();
		std.name = "new standard";
		std.ra = 1.0;
		std.ra_sig = 0.1;
		std.ba = 1.0;
		std.ba_sig = 0.1;
		std.delta = -19.4;
		std.delta_sig = 0.0;
		std.delta_nom = -19.0;
		std.F14C = 1.0;
		std.F14C = 0.0;
		std.ra = 1.21;
		std.ra_sig = 0.0;
		std.active = true;
		std.blank = false;
		stdList.add(std);
		log.debug("add Standard -> size:"+stdList.size());		
	}

	/**
	 * @param file
	 * Save settings to file
	 */
	public void save(File file) {
		Element root = new Element("standard");
		root.setAttribute("date",new Date().toString());
	    for (int i=0; i<stdList.size();i++) {
	    	Element col = new Element("std");
	    	col.setAttribute("name", stdList.get(i).getXML("name"));
	    	root.addContent(col);
	    	Element element = new Element("ra");
	    	element.addContent(stdList.get(i).getXML("ra"));
	    	col.addContent(element);
	    	element = new Element("ra_sig");
	    	element.addContent(stdList.get(i).getXML("ra_sig"));
	    	col.addContent(element);
	    	element = new Element("ba");
	    	element.addContent(stdList.get(i).getXML("ba"));
	    	col.addContent(element);
	    	element = new Element("ba_nom");
	    	element.addContent(stdList.get(i).getXML("ba_nom"));
	    	col.addContent(element);
	    	element = new Element("ba_sig");
	    	element.addContent(stdList.get(i).getXML("ba_sig"));
	    	col.addContent(element);
	    	element = new Element("delta");
	    	element.addContent(stdList.get(i).getXML("delta"));
	    	col.addContent(element);
	    	element = new Element("delta_nom");
	    	element.addContent(stdList.get(i).getXML("delta_nom"));
	    	col.addContent(element);
	    	element = new Element("delta_sig");
	    	element.addContent(stdList.get(i).getXML("delta_sig"));
	    	col.addContent(element);
	    	element = new Element("F14C");
	    	element.addContent(stdList.get(i).getXML("F14C"));
	    	col.addContent(element);
	    	element = new Element("F14C_sig");
	    	element.addContent(stdList.get(i).getXML("F14C_sig"));
	    	col.addContent(element);
	    	element = new Element("active");
	    	element.addContent(stdList.get(i).getXML("active"));
	    	col.addContent(element);
	    	element = new Element("blank");
	    	element.addContent(stdList.get(i).getXML("blank"));
	    	col.addContent(element);
	    }
	    Document doc = new Document();
	    doc.setRootElement(root);
	    Format format= Format.getPrettyFormat();
	    format.setEncoding("ISO-8859-1");
		XMLOutputter out = new XMLOutputter(format);
		try {
			out.output(doc,new FileWriter(file));
			log.debug("File saved: "+file);
		} catch (IOException e) {
			String message = String.format( "<html>File could not be written: <br>"+file+"</html>");
			JOptionPane.showMessageDialog( null, message );
			log.error("XML-output could not be written to file: "+file);
		}
	}

	/**
	 * @param fileName 
	 * Opens file with XML settings
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public void open(File fileName) throws IOException, FileNotFoundException { 
	    Document doc;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(fileName);
			log.debug("File ("+fileName+") for standards opened!");
			Element root = doc.getRootElement();
			List<Element> list = root.getChildren();
			stdList = new ArrayList<StdData>();
			StdData std;
			for (int i=0; i<list.size(); i++) {
				try {
					if (list.get(i).getAttribute("name").getValue().equals("")) {
						log.debug("Standard ignored because name=\"\"");
					} else {
						std = new StdData();
				    		try{std.active=Boolean.valueOf(list.get(i).getChild("active").getText());}
							catch(NumberFormatException e){std.active=true;log.debug("active not loaded from std list!");}
				    			catch(NullPointerException e){std.active=true;log.debug("active not loaded from std list!");}
				    		try{std.blank=Boolean.valueOf(list.get(i).getChild("blank").getText());}
				    			catch(NumberFormatException e){std.blank=false;log.debug("blank not loaded from std list!");}
				    			catch(NullPointerException e){std.blank=false;log.debug("blank not loaded from std list!");}
        			    		try{std.name=list.get(i).getAttribute("name").getValue();}
        						catch(NumberFormatException e){std.ra=null;log.debug("ra not loaded from std list!");}
       			    		    if (Setting.isotope.equals("C14")) {
                			    		try{std.delta=Double.valueOf(list.get(i).getChild("delta").getText());}
                						catch(NumberFormatException e){std.delta=null; if(!std.blank) {std.active=false;}log.debug("delta not loaded from std list!");} 
                			    		try{std.delta_nom=Double.valueOf(list.get(i).getChild("delta_nom").getText());}
                						catch(NumberFormatException e){std.delta_nom=null; if(!std.blank) {std.active=false;}log.debug("deltanom not loaded from std list!");} 
                			    		try{std.delta_sig=Double.valueOf(list.get(i).getChild("delta_sig").getText());}
                						catch(NumberFormatException e){std.delta_sig=null; if(!std.blank) {std.active=false;}log.debug("delta_sig not loaded from std list!");} 
                			    		try{std.F14C=Double.valueOf(list.get(i).getChild("F14C").getText());}
                	    					catch(NullPointerException  e){std.F14C=null; if(!std.blank) {std.active=false;}log.debug("F14C not loaded from std list!");} 
                		    				catch(NumberFormatException e){std.F14C=null; if(!std.blank) {std.active=false;}log.debug("F14C not loaded from std list!");} 
                			    		try{std.F14C_sig=Double.valueOf(list.get(i).getChild("F14C_sig").getText());}
                	    					catch(NullPointerException  e){std.F14C_sig=null; if(!std.blank) {std.active=false;}log.debug("F14C_sig not loaded from std list!");} 
                	    					catch(NumberFormatException e){std.F14C_sig=null; if(!std.blank) {std.active=false;}log.debug("F14C_sig not loaded from std list!");}  			    		
        			    		} else {
                    			    		try{std.ra=Double.valueOf(list.get(i).getChild("ra").getText());}
                    			    			catch(NumberFormatException e){std.ra=null; if(!std.blank) {std.active=false;}log.debug("ra not loaded from std list!");} 
                    			    			catch(NullPointerException e){std.ra=null; if(!std.blank) {std.active=false;}} 
                			    		try{std.ra_sig=Double.valueOf(list.get(i).getChild("ra_sig").getText());}
                						catch(NumberFormatException e){std.ra_sig=null; if(!std.blank) {std.active=false;}log.debug("ra_sig not loaded from std list!");} 
                			    		try{std.ba=Double.valueOf(list.get(i).getChild("ba").getText());}
                			    			catch(NumberFormatException e){std.ba=null; } 
                						catch(NullPointerException e){std.ba=null; } 
                			    		try{std.ba_nom=Double.valueOf(list.get(i).getChild("ba_nom").getText());}
                			    			catch(NumberFormatException e){std.ba_nom=null; } 
                			    			catch(NullPointerException e){std.ba_nom=null; } 
                			    		try{std.ba_sig=Double.valueOf(list.get(i).getChild("ba_sig").getText());}
                			    			catch(NumberFormatException e){std.ba_sig=null; } 
                			    			catch(NullPointerException e){std.ba_sig=null; } 
        			    		}
        					stdList.add(std);
					}
				} catch (NumberFormatException e) {
					log.error("File improperly formed: edit in "+file+"");
//				} catch (NullPointerException e) {
//					log.error("NulPointerException in XML read-in (col "+(i)+").");
				}
			}
		}
		catch(JDOMException e) {
			log.error("Standard file ("+fileName+") exists, but could not be read!");
		}
	}
	
	/**
	 * @param db 
	 * Opens file with XML settings
	 */
	public void openDB(DbConnect db) { 
        	stdList = db.getStd();
	}
	
	/**
	 * @param text 
	 * 
	 */
	public void action(String text) {
        if (text == "Save...") {
    	    JFileChooser fc = new JFileChooser();
    	    fc.setSelectedFile(this.file);
    	    fc.setFileFilter( new FileFilter() {
    	    	      @Override public boolean accept( File f ) {
    	    	        return f.isDirectory() ||
    	    	          f.getName().toLowerCase().endsWith( ".xml" );
    	    	      }
    	    	      @Override public String getDescription() {
    	    	        return "AMS standard file";
    	    	      }
    	    	    } );	    		
    		int returnVal = fc.showSaveDialog(null);	    		
    		if (returnVal == JFileChooser.APPROVE_OPTION) {
    			if (fc.getSelectedFile().isFile()) {
    				int overwrite = JOptionPane.showConfirmDialog(null, "<html>Do your want to overwrite this file?<br>"+fc.getSelectedFile()+"</html>");
    				if(overwrite==0) {
	            		this.file = fc.getSelectedFile();
	            		this.save(this.file);
    				} else {
    					log.debug("Save canceled by user: "+fc.getSelectedFile());
   				}
				} else {
            		this.file = fc.getSelectedFile();
            		this.save(this.file);
    			}
    		} else {
				String message = String.format( "Did not colSaveBat!");
				JOptionPane.showMessageDialog( null, message );
				log.debug("Selection was not approved: "+fc.getSelectedFile());
	    	}
        }        
        else if (text == "Save") {
			this.save(this.file);
        }        
        else if (text == "Add new standard") {
			this.addStd();
			this.stdPanel.updateTable();
			log.debug("add Standard");
        } 
        else if (text == "Open...") {
    		//Create a file chooser
    	    JFileChooser fc = new JFileChooser();
    	    fc.setSelectedFile(this.file);
     	    fc.setFileFilter( new FileFilter() {
    	    	      @Override public boolean accept( File f ) {
    	    	        return f.isDirectory() ||
    	    	          f.getName().toLowerCase().endsWith( ".xml" );
    	    	      }
    	    	      @Override public String getDescription() {
    	    	        return "AMS standard file";
    	    	      }
    	    	    } );
    		int returnVal = fc.showOpenDialog(fc);
    		if (returnVal == JFileChooser.APPROVE_OPTION) {
        		this.file = fc.getSelectedFile();
        		try {
        			this.open(this.file);
        		}
        		catch (IOException e1) {
					String message = String.format( "File could not be opened ("+this.file+")");
					JOptionPane.showMessageDialog( null, message );
				}
    		} 
    		else 
    		{
    		    log.debug("Open command cancelled by user.");
    		}
       }  
   }
}

/**
 * @author lukas
 *
 */
class StdNomTableMenu extends JMenuBar implements ActionListener {
	/**
	 * 
	 */
	// Ask AWT which menu modifier we should be using.
	final static int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	StdNom stdNom;
	Bats main;
	
	/**
	 * @param stdNom
	 */
	public StdNomTableMenu(StdNom stdNom) {
		this.stdNom = stdNom;
		
	    JMenu menu;
	    JMenuItem menuItem;
	
	    //Build the first menu.
	    menu = new JMenu("File");
	    menu.setMnemonic(KeyEvent.VK_F);
	    menu.getAccessibleContext().setAccessibleDescription("File");
	    this.add(menu);

	    menuItem = new JMenuItem("Open...");
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,  MENU_MASK));
	    menuItem.getAccessibleContext().setAccessibleDescription("Opens...");
	    menuItem.addActionListener(this);
	    menu.add(menuItem);		
	
	    menuItem = new JMenuItem("Save...");
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,  MENU_MASK));
	    menuItem.getAccessibleContext().setAccessibleDescription("Save stdNom...");
	    menuItem.addActionListener(this);
	    menu.add(menuItem);

	    menuItem = new JMenuItem("Save");
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,  MENU_MASK));
	    menuItem.getAccessibleContext().setAccessibleDescription("Save stdNom");
	    menuItem.addActionListener(this);
	    menu.add(menuItem);

	    menuItem = new JMenuItem("Add new standard");
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,  MENU_MASK));
	    menuItem.getAccessibleContext().setAccessibleDescription("Add new standard");
	    menuItem.addActionListener(this);
	    menu.add(menuItem);		
	}

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        stdNom.action(source.getText());
    } 
}

/**
 * @author lukas
 *
 */
class StdNomTableTBar extends JToolBar implements ActionListener
{
	StdNom stdNom;
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * @param stdNom 
	 * 
	 */
	public StdNomTableTBar(StdNom stdNom) { 
		this.stdNom = stdNom;
		Insets margins = new Insets(2, 2, 2, 2);

		ToolBarButton button = new ToolBarButton(Setting.batDir+"/icon/open20.png");
		button.setToolTipText("Open...");
		button.setMargin(margins);
		button.addActionListener(this);
		add(button);
		addSeparator();
		
		ToolBarButton button2 = new ToolBarButton(Setting.batDir+"/icon/save20.png");
		button2.setToolTipText("Save");
		button2.setMargin(margins);
		button2.addActionListener(this);
		add(button2);
		addSeparator();
		
		ToolBarButton button3 = new ToolBarButton(Setting.batDir+"/icon/save_as20.png");
		button3.setToolTipText("Save...");
		button3.setMargin(margins);
		button3.addActionListener(this);
		add(button3);
		addSeparator();
		
		ToolBarButton button4 = new ToolBarButton(Setting.batDir+"/icon/list-add.png");
		button4.setToolTipText("Add new standard");
		button4.setMargin(margins);
		button4.addActionListener(this);
		add(button4);
		addSeparator();
	}
	
	/**
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
        ToolBarButton source = (ToolBarButton)(e.getSource());
        stdNom.action(source.getToolTipText());
	}
}
       
/**
 * @author lukas
 *
 */
class StdData {
	private static final Logger log = LogManager.getLogger("StdData");
	
	/**
	 * Name of standard
	 */
	public String name = new String();
	/**
	 * delta 13C
	 */
	public Double delta; 		
	/**
	 * delta 13C sigma
	 */
	public Double delta_sig;
	/**
	 * delta 13C nominal
	 */
	public Double delta_nom;
	/**
	 * 
	 */
	public Double ba; 		
	/**
	 * 
	 */
	public Double ba_sig;
	/**
	 * 
	 */
	public Double ba_nom;
	/**
	 * 
	 */
	public Double F14C;
	/**
	 * 
	 */
	public Double F14C_sig;
	/**
	 * 
	 */
	public Double ra;		
	/**
	 * 
	 */
	public Double ra_sig;		
	/**
	 * 
	 */
	public Boolean active;
	/**
	 * 
	 */
	public Boolean blank;
	
	StdData(){
		name = "";
		active = true;
		blank = false;
	}
	
	/**
	 * @param field 
	 * @return value of Object
	 */
	public Object get(String field){
		Object returnVal = null;
		if (field.equals("name")) { returnVal = name; }
		else if (field.equals("ba")) { returnVal = ba; }
		else if (field.equals("ba_sig")) { returnVal = ba_sig; }
		else if (field.equals("ba_nom")) { returnVal = ba_nom; }
		else if (field.equals("delta")) { returnVal = delta; }
		else if (field.equals("delta_sig")) { returnVal = delta_sig; }
		else if (field.equals("delta_nom")) { returnVal = delta_nom; }
		else if (field.equals("pmC")) { returnVal = F14C*100; }
		else if (field.equals("pmC_sig")) { returnVal = F14C_sig*100; }
		else if (field.equals("F14C")) { returnVal = F14C; }
		else if (field.equals("F14C_sig")) { returnVal = F14C_sig; }
		else if (field.equals("ra")) { returnVal = ra; }
		else if (field.equals("ra_sig")) { returnVal = ra_sig; }
		else if (field.equals("active")) { returnVal = active; }
		else if (field.equals("blank")) { returnVal = blank; }
		return returnVal;
	}
	
	/**
	 * @param field
	 * @return value
	 */
	public Object getValue(String field){
		Object returnVal;
		returnVal = this.get(field);
		if (returnVal==""){returnVal="-";} 
		else if (returnVal==null){returnVal="null";}
		return returnVal;
	}
	
	/**
	 * @param field
	 * @return value
	 */
	public String getXML(String field){
		Object returnVal;
		returnVal = this.get(field);
		if (returnVal==null) {
			returnVal="";
		}
		if (returnVal.getClass()==String.class) {
			return (String)returnVal;			
		} else if (returnVal.getClass()==Double.class) {
			return Double.toString((Double)returnVal);			
		} else if (returnVal.getClass()==Boolean.class) {
			return Boolean.toString((Boolean)returnVal);			
		} else { 
			return "";
		}		
	}
	
	/**
	 * @param value
	 * @param field
	 */
	public void setValues( String[] value,  String[] field ){
		int i;
		for (i=0;i<value.length;i++){
			this.setValue(value[i],field[i]);
		}
	}
	
	/**
	 * @param value
	 * @param field 
	 */
	public void setValue( String value, String field ) {
		try {
			if (field.equals("name")) {name = String.valueOf(value);}
			else if (field.equals("ba")) {ba = Double.valueOf(value);}
			else if (field.equals("ba_sig")) {ba_sig = Double.valueOf(value);}
			else if (field.equals("ba_nom")) {ba_nom = Double.valueOf(value);}
			else if (field.equals("delta")) {delta = Double.valueOf(value);}
			else if (field.equals("delta_sig")) {delta_sig = Double.valueOf(value);}
			else if (field.equals("delta_nom")) {delta_nom = Double.valueOf(value);}
			else if (field.equals("pmC")) {F14C = Double.valueOf(value)/100;}
			else if (field.equals("pmC_sig")) {F14C_sig = Double.valueOf(value)/100;}
			else if (field.equals("F14C")) {F14C = Double.valueOf(value);}
			else if (field.equals("F14C_sig")) {F14C_sig = Double.valueOf(value);}
			else if (field.equals("ra")) {ra = Double.valueOf(value);}
			else if (field.equals("ra_sig")) {ra_sig = Double.valueOf(value);}
			else if (field.equals("active")) {active = Boolean.valueOf(value);}
			else if (field.equals("blank")) {blank = Boolean.valueOf(value);}
			else {
				String message = String.format("Standard readin error: Wrong field -> " + field + " - " + value);
				log.error(message);
			}
		}
		catch (NumberFormatException e) {log.error("NumberFormatException in SetValue! (field "+field+")");}
	}

	/**
	 * @param value
	 * @param field
	 */
	public void set( Object value, String field ) {
		try {
			if (field.equals("name")) {name = (String)value;}
			else if (field.equals("ba")) {ba = (Double)value;}
			else if (field.equals("ba_sig")) {ba_sig = (Double)value;}
			else if (field.equals("ba_nom")) {ba_nom = (Double)value;}
			else if (field.equals("delta")) {delta = (Double)value;}
			else if (field.equals("delta_sig")) {delta_sig = (Double)value;}
			else if (field.equals("delta_nom")) {delta_nom = (Double)value;}
			else if (field.equals("pmC")) {F14C = (Double)value;}
			else if (field.equals("pmC_sig")) {F14C_sig = (Double)value/100;}
			else if (field.equals("F14C")) {F14C = (Double)value;}
			else if (field.equals("F14C_sig")) {F14C_sig = (Double)value;}
			else if (field.equals("ra")) {ra = (Double)value;}
			else if (field.equals("ra_sig")) {ra_sig = (Double)value;}
			else if (field.equals("active")) {active = (Boolean)value;}
			else if (field.equals("blank")) {blank = (Boolean)value;}
			else {
				String message = String.format("Standard readin error: Wrong field -> " + field + " - " + value);
	//			JOptionPane.showMessageDialog( null, message );
				log.error(message);
			}
		}
		catch (NumberFormatException e) {log.error("NumberFormatException in Set! (field "+field+")");}
	}
/**
	 * @param value
	 * @param field
	 */
	public void setValues( ArrayList<String> value, String[] field){
		int i;
		for (i=0;i<value.size();i++){
			this.setValue(value.get(i),field[i]);
		}
	}
}
