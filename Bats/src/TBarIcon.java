import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JToolBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jdom2.DataConversionException;

/**
 * @author lukas
 *
 */
public class TBarIcon extends JToolBar implements ActionListener {
	
	private static final Logger log = LogManager.getLogger("TBarIcon");
	
	Action action;
	
	ToolBarButton button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, button11, button12, button13;
	
	/**
	 * @param action 
	 * 
	 */	
	public TBarIcon(Action action) { 
		this.action = action;
		Insets margins = new Insets(2, 2, 2, 2);
		
		ToolBarButton button = new ToolBarButton(Setting.batDir+"/icon/open20.png");
		button.setFocusPainted(true);
		button.setToolTipText("Open data file...");
		button.setName("Open data file...");
		button.setMargin(margins);
		button.addActionListener(this);
		this.add(button);
		
		button1 = new ToolBarButton(Setting.batDir+"/icon/save20.png");
		button1.setFocusPainted(true);
		button1.setToolTipText("Save data file...");
		button1.setName("Save data file...");
		button1.setMargin(margins);
		button1.addActionListener(this);
		button1.setEnabled(!Setting.no_data);
		this.add(button1);
		
		log.debug("Toolbar button1 added.");

		this.addSeparator();
		
		
			button11 = new ToolBarButton(Setting.batDir+"/icon/open_n_db20.png");
			button11.setFocusPainted(true);
			button11.setToolTipText("New magazine from database");
			button11.setName("New db");
			button11.setMargin(margins);
			button11.addActionListener(this);
			this.add(button11);
			
			button12 = new ToolBarButton(Setting.batDir+"/icon/open_l_db20.png");
			button12.setFocusPainted(true);
			button12.setToolTipText("New data from latest magazine in database");
			button12.setName("Latest db");
			button12.setMargin(margins);
			button12.addActionListener(this);
			this.add(button12);
			
			button10 = new ToolBarButton(Setting.batDir+"/icon/open_u_db20.png");
			button10.setFocusPainted(true);
			button10.setToolTipText("Update data from selected magazine");
			button10.setName("Update db");
			button10.setMargin(margins);
			button10.setEnabled(!Setting.no_data);
			button10.addActionListener(this);
			this.add(button10);
			
			button2 = new ToolBarButton(Setting.batDir+"/icon/open_add20.png");
			button2.setFocusPainted(true);
			button2.setToolTipText("Add magazine from database");
			button2.setName("Add db");
			button2.setMargin(margins);
			button2.addActionListener(this);
			button2.setEnabled(!Setting.no_data);
			this.add(button2);
			this.addSeparator();

			button13 = new ToolBarButton(Setting.batDir+"/icon/open_db20.png");
			button13.setFocusPainted(true);
			button13.setToolTipText("Open from DB...");
			button13.setName("Open db");
			button13.setMargin(margins);
			button13.addActionListener(this);
			this.add(button13);
			
        		button3 = new ToolBarButton(Setting.batDir+"/icon/save_db20.png");
        		button3.setFocusPainted(true);
        		button3.setToolTipText("Save to DB...");
        		button3.setName("Save as db");
        		button3.setMargin(margins);
        		button3.addActionListener(this);
        		button3.setEnabled(!Setting.no_data);
        		this.add(button3);

            		boolean active = Setting.getActive("/bat/isotope/db");
			if (active) {		
        			button11.setVisible(active);
        			button2.setVisible(active);
        			button10.setVisible(active);
        			button12.setVisible(active);
        			button3.setVisible(Setting.getActive("/bat/isotope/db/sql"));
        			button13.setVisible(Setting.getActive("/bat/isotope/db/sql"));
			}
			
			log.debug("Toolbar db buttons added.");

	
			this.addSeparator();
//		}

		button4 = new ToolBarButton(Setting.batDir+"/icon/notes20.png");
		button4.setFocusPainted(true);
		button4.setToolTipText("Comment for data-set");
		button4.setName("Comment");
		button4.setMargin(margins);
		button4.addActionListener(this);
		button4.setEnabled(!Setting.no_data);
		this.add(button4);
		
		button5 = new ToolBarButton(Setting.batDir+"/icon/calc20.png");
		button5.setFocusPainted(true);
		button5.setToolTipText("Force recalculate");
		button5.setName("Force recalculate");
		button5.setMargin(margins);
		button5.addActionListener(this);
		button5.setEnabled(!Setting.no_data);
		this.add(button5);
		
		button9 = new ToolBarButton(Setting.batDir+"/icon/calib20.png");
		button9.setFocusPainted(true);
		button9.setToolTipText("Calibrate with SwissCal");
		button9.setName("calibMan");
		button9.setMargin(margins);
		button9.addActionListener(this);
		this.add(button9);

		this.addSeparator();

		button6 = new ToolBarButton(Setting.batDir+"/icon/xhtml20.png");
		button6.setFocusPainted(true);
		button6.setToolTipText("Output to default browser");
		button6.setName("Browser");
		button6.setMargin(margins);
		button6.addActionListener(this);
		button6.setEnabled(!Setting.no_data);
		this.add(button6);

		this.addSeparator();

		button = new ToolBarButton(Setting.batDir+"/icon/pref20.png");
		button.setFocusPainted(true);
		button.setToolTipText("Preferences");
		button.setName("Preferences");
		button.setMargin(margins);
		button.addActionListener(this);
		this.add(button);

		button = new ToolBarButton(Setting.batDir+"/icon/help20.png");
		button.setFocusPainted(true);
		button.setToolTipText("Help");
		button.setName("Help");
		button.setMargin(margins);
		button.addActionListener(this);
		this.add(button);
		
		button8 = new ToolBarButton(Setting.batDir+"/icon/exit20.png");
		button8.setFocusPainted(true);
		button8.setToolTipText("Quit");
		button8.setName("Quit");
		button8.setMargin(margins);
		button8.addActionListener(this);
		this.add(button8);
		
	}
	
	/**
	 * 
	 */
	public void refresh() {
		if (Setting.db!=null) {
			button1.setEnabled(!Setting.no_data);
		} else {
			button1.setEnabled(false);
		}
		button4.setEnabled(!Setting.no_data);
		button5.setEnabled(!Setting.no_data);
		button6.setEnabled(!Setting.no_data);
		
		if (Setting.getActive("/bat/isotope/db")) {		
			button2.setVisible(true);
			button10.setVisible(true);
			button11.setVisible(true);
			button12.setVisible(true);
			button10.setEnabled(!Setting.no_data);
			button2.setEnabled(!Setting.no_data);
			if (Setting.getActive("/bat/isotope/db/sql")) {
			    button3.setVisible(true);
			    button13.setVisible(true);
    			    button3.setEnabled(!Setting.no_data);
			} else {
			    button3.setVisible(false);
			    button13.setVisible(false);
			}
		} else {
			button2.setVisible(false);
			button10.setVisible(false);
			button11.setVisible(false);
			button12.setVisible(false);
			button3.setVisible(false);
			button13.setVisible(false);
		}

	}
	
	public void actionPerformed(ActionEvent e) {
		ToolBarButton source = (ToolBarButton)(e.getSource());
		action.exec(source.getName());
	}
}
