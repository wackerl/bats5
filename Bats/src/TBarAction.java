import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author lukas
 *
 */
public class TBarAction extends JToolBar {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LogManager.getLogger("TBarAction");
	static int p = Setting.getInt("/bat/general/font/p");
	static String ft = Setting.getString("/bat/general/font/type");
	
	JTextField label;
	Bats main;

	/**
	 * @param main 
	 * 
	 * 
	 */	
	public TBarAction(Bats main) { 
		label = new JTextField();
		label.setFont(new Font(ft,0,p));
		label.setPreferredSize(new Dimension(240,20));
		label.setBorder(BorderFactory.createLineBorder(Color.black));
		label.setText(" - ");
		label.setBackground(new Color(50,255,50));
		label.setOpaque(true);
		this.add(label);
	} 
	
	/**
	 * @param comment 
	 * @param action 
	 * 
	 */
	public void update(String comment, Boolean action) {
		this.removeAll();
		log.debug("info "+comment+" - set "+action);
		if (action==true) {
			label.setText(" "+comment);
			label.setBackground(new Color(255,50,50));
		} else {
			label.setText(" "+comment);
			label.setBackground(new Color(50,255,50));
		}		
		this.add(label);
		this.update(this.getGraphics());
	}
}
