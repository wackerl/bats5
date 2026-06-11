import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.commons.codec.binary.Base64;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * @author lukas
 *
 */
public class IODb implements DbConnect {
	private static final Logger log = LogManager.getLogger("IODb");
	
	Bats main;
//	Calc data;
	
	String url;
    String user;
    String pw;
    String lm;
    String ly;
    String target_t;
    String sampletype_t;
    String corr_t;
    String calcset_t;
    String calc_sample_t;
    String calc_run_t;
    String calc_cycle_t;
    Boolean cycle_imp;
    Integer cycle_nr;
    String run_t;
    String cycle_t;
    String calcset_null="";
    Integer timeout;
    Integer sampleMin;
    Calendar date, date2;
    TimerTask action;
    Boolean save_all;
    
    /**
     * 
     */
    public Connection conn;
    String sql;

    ArrayList<String> nameJS = new ArrayList<String>();
	ArrayList<String> nameDbS = new ArrayList<String>();
	ArrayList<String> nameJR = new ArrayList<String>();
	ArrayList<String> nameDbR = new ArrayList<String>();
	ArrayList<String> openDbR = new ArrayList<String>();
	ArrayList<String> openDbS = new ArrayList<String>();
	ArrayList<String> openJR = new ArrayList<String>();
	ArrayList<String> openJS = new ArrayList<String>();

//	private String	calcSet;

	/**
	 * @param main 
	 */
	public IODb(Bats main) {
		this.main = main;
//		this.data = main.data;
		Setting.db=this;
		Setting.db_name="sql";
		save_all = Setting.getBoolean("/bat/isotope/db/sql/save_all");
		new LogOutTimer((DbConnect)this);
		log.debug("Create IODB");
	}
	
	/**
	 * 
	 */
	public void getSettings() {
//	    calcset_null=" AND calcset IS NULL";
	    url = Setting.getString("/bat/isotope/db/sql/url");
	    user = Setting.getString("/bat/isotope/db/sql/user");
//	    log.debug(Setting.getString("/bat/isotope/db/sql/pw"));
	    pw = new String(Base64.decodeBase64(Setting.getString("/bat/isotope/db/sql/pw").getBytes()));
//	    log.debug(pw);
	    timeout = Setting.getInt("/bat/isotope/db/sql/timeout");
	    sampleMin = Setting.getInt("/bat/isotope/db/sql/sample_min");
	    lm = Setting.getString("/bat/isotope/db/sql/last_mag");
	    int timespan = Setting.getInt("/bat/isotope/db/sql/timespan");
	    try {
			if (Setting.getElement("/bat/isotope/db/sql/last_year").getAttribute("auto").getBooleanValue()) {
				date2 = Calendar.getInstance();
				date2.add(Calendar.DAY_OF_MONTH,1);
//				log.debug(date2.getTime().getTime());
				date = (Calendar) date2.clone();
				date.add(Calendar.MONTH,-(timespan-2));
//				log.debug(date2.getTime().getTime());
//				log.debug(date.getTimeInMillis());
			} else {	
			    log.debug(Setting.getInt("/bat/isotope/db/sql/last_year"));
//				date.set(2010,1,1);
//				date.set(Setting.getInt("/bat/isotope/db/sql/last_year"),Calendar.JANUARY,1);
//				date.set(Setting.getInt("/bat/isotope/db/sql/last_year"),Calendar.JANUARY,Calendar.DAY_OF_MONTH);
				date = Calendar.getInstance();
				date.add(Calendar.DAY_OF_YEAR,30);
//				log.debug(date.getTime());
				date2 = Calendar.getInstance();
				date2.add(Calendar.DAY_OF_YEAR,30);
				date.set(Setting.getInt("/bat/isotope/db/sql/last_year"),1,1);
			}
		} catch (DataConversionException e1) {
			date2 = Calendar.getInstance();
			date = (Calendar) date2.clone();
			date.add(Calendar.YEAR,-1);
			log.warn("Could not load year from settings -> set to last year");
		}
	    target_t = Setting.getString("/bat/isotope/db/sql/target_t");   // sample table
	    sampletype_t = Setting.getString("/bat/isotope/db/sql/sampletype_t");   // sampletype table
	    corr_t = Setting.getString("/bat/isotope/db/sql/calc_corr_t");   // correction table
	    calcset_t = Setting.getString("/bat/isotope/db/sql/calcset_t");   // calc set table
	    calc_sample_t = Setting.getString("/bat/isotope/db/sql/calc_sample_t");   // calc sample table
	    if(save_all) {
		    calc_run_t = Setting.getString("/bat/isotope/db/sql/calc_run_t");   // calc sample table
		    calc_cycle_t = Setting.getString("/bat/isotope/db/sql/calc_cycle_t");   // calc sample table	    	
	    }
	    run_t = Setting.getString("/bat/isotope/db/sql/run_t");   // run table
	    cycle_t = Setting.getString("/bat/isotope/db/sql/cycle_t");   // run table
		try {
			cycle_imp = Setting.getElement("/bat/isotope/db/sql/cycle").getAttribute("name").getBooleanValue();
			cycle_nr = Setting.getInt("/bat/isotope/db/sql/cycle");
			log.debug("Cycle import set "+cycle_imp+" ("+cycle_nr+")");
		} catch (DataConversionException e) {
			cycle_imp=false;
			log.debug("Cycle setting not found!");
		}
	    
	 	String driver = Setting.getString("/bat/isotope/db/sql/driver");
		try {
			Class.forName(driver).newInstance();
			log.debug("DB driver loaded: "+driver);
		} catch (Exception e) {
			log.error("DB driver could not be loaded! ("+driver+")");
			log.error(e);
		}
		log.debug(date.getTime());
	}
	
	/**
	 * 
	 */
	public void logout() {
		try {
			conn.close();
			conn=null;
			action.cancel();
		} catch (SQLException e) {
			log.debug("DB connection already closed!");
		} catch (NullPointerException e) {
			log.debug("DB connection already closed!");
		}
		pw="";
	}
	
	@SuppressWarnings("unchecked")
	private void getSelectSQL() {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(new File(Setting.batDir+"/"+Setting.isotope+"/db_io/eth_nt.xml"));
			List<Element> list;			
			list = doc.getRootElement().getChild("target_save").getChildren();
			nameJS.clear();
			nameDbS.clear();
			for (int i=0; i<list.size(); i++){
				nameJS.add(list.get(i).getText());
				nameDbS.add(list.get(i).getAttributeValue("field"));
			}
			openJR.clear();
			openDbR.clear();
			if (cycle_imp) {
				list = doc.getRootElement().getChild("cycle_open").getChildren();
				for (int i=0; i<list.size(); i++){
					openJR.add(list.get(i).getText());
					openDbR.add(list.get(i).getAttributeValue("field"));
				}
			} else {
				list = doc.getRootElement().getChild("run_open").getChildren();
				for (int i=0; i<list.size(); i++){
					openJR.add(list.get(i).getText());
					openDbR.add(list.get(i).getAttributeValue("field"));
				}
			}			
			list = doc.getRootElement().getChild("target_open").getChildren();
			openJS.clear();
			openDbS.clear();
			for (int i=0; i<list.size(); i++){
				openJS.add(list.get(i).getText());
				openDbS.add(list.get(i).getAttributeValue("field"));
			}
			doc.removeContent();
			log.debug("read db settings");
		} catch (JDOMException e) {
			log.error("JDOMExeption error reading eth_nt.xml");
		} catch (IOException e) {
			log.error("IOExeption error reading eth_nt.xml");
		}	
	}
	
	/**
	 * 
	 */
	public void saveAs() {
		try {
			main.data.calcSet = (JOptionPane.showInputDialog("Select name to save data: ", main.data.calcSet));
			if (main.data.calcSet.length()>20) {
				try {
					main.data.calcSet=main.data.calcSet.substring(0,20);
				} catch (StringIndexOutOfBoundsException e) {;}
			}
			save();
		} catch (NullPointerException e) { 
			log.debug("Aborded save!"); 
		}		
	}
	
	/**
	 * 
	 */
	public void save() {
	    String calcSet = main.data.calcSet.replaceAll(" ","_");
	    log.debug(calcSet);
	    if (calcSet!=null) {
		if (main.data.runListR.size()>0) {
			main.tba.update("Save data to DB",true);
			Statement stmt = null;
		    	if (conn==null) {
		    		log.debug("Start login");
		    		conn = login();
		    	}
		    	if (conn!=null) {
				    try {	
					log.debug("Connection opend to "+url);
					    stmt = conn.createStatement();
					    stmt.setQueryTimeout(timeout);
					    String[] list = {"0 prelininary evaluation","1 final evaluation (not write protected)","2 final evaluation (write protected)"};
					    String sel = (String) JOptionPane.showInputDialog(main,
						            "Status of evaluation",
						            "Select",
						            JOptionPane.QUESTION_MESSAGE,
						            null, list,
						            null);
					    if (sel != null) {
        					   Integer editable = Integer.valueOf(sel.substring(0,1));
        
        					    // Check if calcSet exists
        					    ResultSet res = stmt.executeQuery("SELECT calcSet, edit FROM "+calcset_t+" WHERE calcSet='"+calcSet+"'");
        					    if( res.next()) {
        					    	if (res.getInt("edit")<2) {
        								int option = JOptionPane.showConfirmDialog(null, "Do your want to overwrite '"+calcSet+"'?", "DB save", JOptionPane.YES_NO_OPTION);
        								if (option == JOptionPane.YES_OPTION) {
        //									stmt.execute("UPDATE "+target_t+" SET calcset=null WHERE calcset='"+calcSet+"'");
        									main.data.calcSet=calcSet;
        									upload(main.data, conn, editable);
        									main.infoPanel.update(main.data);
        									main.tba.update("Overwritten in db!",false);
        								} else {
        						    			log.info("Didn't save to db!");
        									main.tba.update("Didn't save to db",false);
        								}
        					    	} else {
        							String message = String.format( "<html>Your are not allowed to overwrite calc-set: "+calcSet+"</html>");
        							JOptionPane.showMessageDialog( null, message );
        							log.info("Your are not allowed to overwrite calc-set: "+calcSet);
        							main.tba.update("Didn't save to db",false);
        					    	} 
        					    } else {
            					    	log.debug("Start upload");
            						main.data.calcSet=calcSet;
            						upload(main.data, conn, editable);
            						main.infoPanel.update(main.data);
            						main.tba.update("Saved to db!",false);
            						Setting.setLastMag(main.data.calcSet);
            					    }
					    } else {
						main.tba.update("Didn't save!",true);
						    log.debug("Didn't save because no status of evaluation was selected!");
					    }
					} catch (SQLException e) {
					    log.error("Could not execute insert");
					    log.debug("SQLException: " + e.getMessage());
					    logout();
					main.tba.update("Try again!",true);
					} finally {
					    if (stmt != null) {
					        try {
					            stmt.close();
					        } catch (SQLException e) {
								log.error("Could not insert data ");
							    log.info("SQLException: " + e.getMessage());
							    log.info("SQLState: " + e.getSQLState());
							    log.info("VendorError: " + e.getErrorCode());
					        }
					        stmt = null;
					    }
					}
		    	} else {
		    		log.info("Could not login!");
				main.tba.update("Couldn't save!",true);
		    	}
			}
	    }
	}
	
	/**
	 * @param data 
	 * @param conn 
	 * 
	 */
	private void upload(Calc data, Connection conn, Integer editable) {
		main.act.exec("CalibRanges");
		if (data.runListR.size()>0) {
		    Statement stmt = null;
		    try {		
    			if (uploadCorr(data, conn, editable)) {
                		main.tba.update("Save data to DB",true);
                		    String nameQ = "";
	
        			    stmt = conn.createStatement();
        			    stmt.setQueryTimeout(timeout);
        			    
        			    nameQ = "";
        			    for (int j=0;j<nameDbS.size();j++) {
        			    	if (!nameDbS.get(j).equalsIgnoreCase("calcset")) {
        			    		nameQ+=nameDbS.get(j)+"=null,";
        			    	}
        			    }
//        			    JOptionPane.showMessageDialog( null, "1" );
        			    nameQ+="calcset=null";
        			    sql = "UPDATE "+target_t+" SET "+nameQ+" WHERE calcset='"+data.calcSet+"' AND editallowed<2";
        			    stmt.execute(sql);
        			    
//        			    JOptionPane.showMessageDialog( null, sql );
        			    
        			    
        // in case the calcset is changed, but we are not allowed to do a change on the target result, we change the calcset field!!!
        			    sql = "UPDATE "+target_t+" SET calcset='_"+data.calcSet+"' WHERE calcset='"+data.calcSet+"' AND editallowed>1";
//        			    log.debug(sql);
        			    stmt.execute(sql);
        			    
        			    ArrayList<Run> runlist;
        // done
        			    for (int i=0;i<data.sampleList.size();i++) {
					    ResultSet res = stmt.executeQuery("SELECT editallowed, sample_nr FROM "+target_t+
					    " WHERE sample_nr="+data.sampleList.get(i).sample_nr+
	        			    	" AND prep_nr="+data.sampleList.get(i).prep_nr+
	        			    	" AND target_nr="+data.sampleList.get(i).target_nr);
	        			    if (res.next()) {
//	        				log.debug(res.getInt("editallowed")+" - "+res.getInt("sample_nr")+" - "+data.sampleList.get(i).sample_nr);
        					    if (res.getInt("editallowed")<2){
        	        				if (data.sampleList.get(i).active()) {
                					    nameQ = "";
                        				    for (int j=0;j<nameDbS.size();j++) {
                        				    	if (!nameDbS.get(j).equalsIgnoreCase("calcset")) {
                        				    		nameQ+=nameDbS.get(j)+"='"+data.sampleList.get(i).get(nameJS.get(j))+"',";
                        				    	}
                        				    }
                        				    nameQ+="calcset='"+data.calcSet+"',";
                        				    nameQ+="editallowed="+editable+"";
                        				    data.sampleList.get(i).editable=editable;
                        			    	
                        				    nameQ = nameQ.replaceAll("'true'","true").replaceAll("'false'","false")
                        					    .replaceAll("'null'","null").replaceAll("'NaN'","null").replace("'Infinity'","null");
                            			    	
                            			    	    sql="UPDATE "+target_t+" SET "+nameQ+
                            			    		    " WHERE sample_nr="+data.sampleList.get(i).sample_nr+
                            			    		    " AND prep_nr="+data.sampleList.get(i).prep_nr+
                            			    		    " AND target_nr="+data.sampleList.get(i).target_nr;
                            				    stmt.execute(sql);
        	        	    			}else {/*log.debug(res.getInt("editallowed")+" - "+res.getInt("sample_nr")+" - "+data.sampleList.get(i).sample_nr);*/}
                        				    
                        				sql="INSERT INTO "+calc_sample_t+" (sample_nr, prep_nr, target_nr, calcset, type, active, prep_bl, std_bl) VALUES ("
                        				    +data.sampleList.get(i).sample_nr+", "+data.sampleList.get(i).prep_nr+", "
                        				    +data.sampleList.get(i).target_nr+", '"+data.calcSet+"', '"+data.sampleList.get(i).type+"',"+(data.sampleList.get(i).active?1:0)+", "
                        				    +data.sampleList.get(i).prep_bl+", "+(data.stdNom.isStdBl(data.sampleList.get(i).type)?1:0)+")";
                        				stmt.execute(sql);
                        				if(save_all) {
	                        				runlist = data.runLabelList.get(i);
	                        				for (int k=0;k<runlist.size();k++) {
	                            				sql="INSERT INTO "+calc_run_t+" (run, calcset, active) VALUES ('"
	                                				    +runlist.get(k).run+"', '"+data.calcSet+"', "+(runlist.get(k).active?1:0)+")";
	                                			stmt.execute(sql);
	                                			sql = "INSERT INTO "+calc_cycle_t+" (calcset, cycle, run, active) SELECT '"+data.calcSet+"' as calcset, cycle, run, cycltrue is null FROM "+cycle_t+" WHERE run = '"+runlist.get(k).run+"'";
	                                			stmt.execute(sql);
	                                			//log.debug("executed: "+sql);
	                        				}
                        				}
                        				
        					    } else {
        						
        					    }
    					    } else {
    						JOptionPane.showMessageDialog( null, "Target "+data.sampleList.get(i).sample_nr+
                			    		    "."+data.sampleList.get(i).prep_nr+
                			    		    "."+data.sampleList.get(i).target_nr+" could not be updated!<br>(editallowed: "+res.getInt("editallowed")+")");
    						log.warn("Target "+data.sampleList.get(i).sample_nr+
                    			    		    "."+data.sampleList.get(i).prep_nr+
                    			    		    "."+data.sampleList.get(i).target_nr+" could not be updated!");
    					    }
	        			}
					    
        			    }
        			    log.debug("Inserted into "+target_t);
        			    log.debug("Inserted into "+run_t);
        			    main.tba.update("Saved to db!",false);
        	} catch (SQLException e) {
    			log.warn("Could not execute insert");
			    log.info("SQLException: " + e.getMessage());
			    log.info("SQLState: " + e.getSQLState());
			    log.info("Query: " + sql);
			    log.info("VendorError: " + e.getErrorCode());
				String message = String.format( "<html>Could not insert data<br>SQLException: "+e.getMessage()
						+"</html>");
			    JOptionPane.showMessageDialog( null, message );
			} finally {
			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch (SQLException e) {
						log.error("Could not insert data ");
					    log.info("SQLException: " + e.getMessage());
					    log.info("SQLState: " + e.getSQLState());
					    log.info("VendorError: " + e.getErrorCode());
			        }
			        stmt = null;
			    }
    			}
		}
	}
		
	/**
	 * @param data
	 * @param conn
	 * @throws SQLException 
	 */
	private boolean uploadCorr(Calc data, Connection conn, Integer editable) throws SQLException {
	    Statement stmt=null;
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(timeout);
	    log.debug("deleted all samples for "+data.calcSet);
	    
	    sql = "DELETE FROM "+corr_t+" WHERE calcset='"+data.calcSet+"'";
	    stmt.execute(sql);

	    sql = "DELETE FROM "+calc_sample_t+" WHERE calcset='"+data.calcSet+"'";
	    stmt.execute(sql);
	    
	    sql = "DELETE FROM "+calcset_t+" WHERE calcset='"+data.calcSet+"'";
	    stmt.execute(sql);
	    
	    sql = "DELETE FROM "+calc_run_t+" WHERE calcset='"+data.calcSet+"'";
	    stmt.execute(sql);
	    
	    sql = "DELETE FROM "+calc_cycle_t+" WHERE calcset='"+data.calcSet+"'";
	    stmt.execute(sql);

	    log.debug("deleted "+data.calcSet+" in "+corr_t+" and "+calcset_t);
	    
	    String nameQ = "calcset, date_calc, magazine, isobar, "+
						"a_err_abs, a_err_rel, a_off, "+
						"b_err_abs, b_err_rel, b_off, "+
						"charge, first_run, last_run, fract, "+
						"iso_err_abs, iso_err_rel, iso_off, user_calc, comment, "+
						"deadtime, scatter, ra_norm, "+
						"weighting, poisson, cycles, edit";
	    if (Setting.isotope.equalsIgnoreCase("C14")) {
	    	nameQ += ", ba_norm";
	    }
	    int cycles=0;
	    try {
			if (Setting.getElement("/bat/isotope/db/sql/cycle").getAttribute("name").getBooleanValue()) {
				cycles=Setting.getInt("/bat/isotope/db/sql/cycle");
			} 
		} catch (DataConversionException e) {  log.debug("Cycle setting not found!"); }
	    		String dataQ = "'"+data.calcSet+"','"+data.runListR.get(data.runListR.size()-1).get("timestamp")+"','"+data.magazine+"','"+data.isobar+"','"+
	    				data.a_err+"','"+data.a_errR+"','"+data.a_off+"','"+
	    				data.b_err+"','"+data.b_errR+"','"+data.b_off+"','"+
	    				data.charge+"','"+data.firstRun+"','"+data.lastRun+"','"+Setting.getBoolean("/bat/isotope/calc/fract")+"','"+
	    				data.iso_err+"','"+data.iso_errR+"','"+data.iso_off+"','"+System.getProperty("user.name")+"','"+data.comment.getText()+"','"+
	    				Setting.getDouble("/bat/isotope/calc/dead_time")+"','"+Setting.getDouble("/bat/isotope/calc/scatter")+"','"+Setting.getFloat("/bat/isotope/calc/nominal_ra")+
	    				"','"+Setting.getInt("/bat/isotope/calc/mean")+"','"+Setting.getBoolean("/bat/isotope/calc/poisson")+"','"+cycles+"','"+editable+"'";
	    if (Setting.isotope.equalsIgnoreCase("C14")) {
	    	dataQ += ",'"+Setting.getDouble("bat/isotope/calc/nominal_ba")+"'";
	    }
    	dataQ = dataQ.replaceAll("'true'","true").replaceAll("'false'","false")
    	.replaceAll("'null'","null").replaceAll("'NaN'","null").replace("'Infinity'","null");
    	sql = "INSERT INTO "+calcset_t+" ("+nameQ+") VALUES ("+dataQ+")";
	    stmt.executeUpdate(sql);
	    log.debug("Inserted into "+calcset_t);
	    nameQ = "calcset, isobar_fact, isobar_err, "+
	    		"std_ra, std_ra_err, bl_ra, bl_ra_err, "+
	    		"a_slope, a_slope_off, b_slope, b_slope_off, "+
	    		"time_corr, first_run, last_run, corr_index, "+
	    		"bg_const, bg_const_err, bl_const_mass, bl_const, bl_const_err";
	    if (Setting.isotope.equalsIgnoreCase("C14")) {
	    	nameQ += ", std_ba, std_ba_err";
	    }
	    for (int i=0; i<data.corrList.size(); i++) {
	    	Corr corr = data.corrList.get(i);
		    String dataQ2 = "'"+data.calcSet+"','" + corr.isoFact+"','"+corr.isoErr+"','"+
			    corr.std.std_ra+"','"+corr.std.std_ra_err+"','"+corr.blank.ra_bg+"','"+corr.blank.ra_bg_err+"','"+
			    corr.a_slope+"','"+corr.a_slope_off+"','"+corr.b_slope+"','"+corr.b_slope_off+"','"+
			    corr.timeCorr+"','"+corr.firstRun+"','"+corr.lastRun+"','"+i+"','"+
			    corr.constBG+"','"+corr.constBGErr+"','"+corr.constBlWeight+"','"+corr.constBlRatio+"','"+corr.constBlErr+"'";
		    if (Setting.isotope.equalsIgnoreCase("C14")) {
		    	dataQ2 += ",'"+corr.std.std_ba+"','"+corr.std.std_ba_err+"'";
		    }
	    	dataQ2 = dataQ2.replaceAll("'true'","true").replaceAll("'false'","false")
	    	.replaceAll("'null'","null").replaceAll("'NaN'","null").replace("'Infinity'","null");
		    sql="INSERT INTO "+corr_t+" ("+nameQ+") VALUES ("+dataQ2+")";
		    stmt.executeUpdate(sql);
		    log.debug("Inserted into "+corr_t);
	    }
	    return true;
	}
	
	/**
	 * @param conn
	 * @param calcSet 
	 * @param data 
	 * @return corections
	 */
	public ArrayList<Corr> downloadCorr(Connection conn, String calcSet, Calc data) {
		ArrayList<Corr> corrL= new ArrayList<Corr>();
		Statement stmt=null;
		try {
		    stmt = conn.createStatement();
		    stmt.setQueryTimeout(timeout);
		    
		    String nameQ = "calcset, date_calc, magazine, isobar, "+
			"a_err_abs, a_err_rel, a_off, ra_norm, scatter, "+
			"b_err_abs, b_err_rel, b_off, charge, fract, deadtime, "+
			"iso_err_abs, iso_err_rel, iso_off, first_run, last_run, comment, "+
			"cycles, weighting, poisson";
		    if (Setting.isotope.equalsIgnoreCase("C14")) {
		    	nameQ += ", ba_norm";
		    }
			sql="SELECT "+nameQ+" FROM "+calcset_t+" WHERE calcset='"+calcSet+"'";
//	    	log.debug(sql);
			ResultSet resultD = stmt.executeQuery(sql);
			resultD.next();
			data.calcSet = resultD.getString("calcset");
			data.magazine = resultD.getString("magazine");
			data.isobar = resultD.getString("isobar");
			data.firstRun = resultD.getString("first_run");
			data.lastRun = resultD.getString("last_run");
			data.comment.setText(resultD.getString("comment"));
//			log.debug(resultD.getString("comment"));
			Double value = data.a_err = resultD.getDouble("a_err_abs");
			Setting.getElement("/bat/isotope/calc/current/a/error_abs").setText(value.toString());
			value = data.a_errR = resultD.getDouble("a_err_rel");
			Setting.getElement("/bat/isotope/calc/current/a/error_rel").setText(value.toString());
			value = data.a_off = resultD.getDouble("a_off");
			Setting.getElement("/bat/isotope/calc/current/a/offset").setText(value.toString());
			value = data.b_err = resultD.getDouble("b_err_abs");
			Setting.getElement("/bat/isotope/calc/current/b/error_abs").setText(value.toString());
			value = data.b_errR = resultD.getDouble("b_err_rel");
			Setting.getElement("/bat/isotope/calc/current/b/error_rel").setText(value.toString());
			value = data.a_off = resultD.getDouble("b_off");
			Setting.getElement("/bat/isotope/calc/current/b/offset").setText(value.toString());
			value = data.iso_err = resultD.getDouble("iso_err_abs");
			Setting.getElement("/bat/isotope/calc/current/iso/error_abs").setText(value.toString());
			value = data.iso_errR = resultD.getDouble("iso_err_rel");
			Setting.getElement("/bat/isotope/calc/current/iso/error_rel").setText(value.toString());
			value = data.iso_off = resultD.getDouble("iso_off");
			Setting.getElement("/bat/isotope/calc/current/iso/offset").setText(value.toString());
			value = data.deadtime = resultD.getDouble("deadtime");
			Setting.getElement("/bat/isotope/calc/dead_time").setText(value.toString());
			Integer value2 = data.charge = resultD.getInt("charge");
			Setting.getElement("/bat/isotope/calc/current/charge").setText(value2.toString());
			Boolean value3 = resultD.getBoolean("fract");
			Setting.getElement("/bat/isotope/calc/fract").setText(value3.toString());
			value2 = resultD.getInt("weighting");
			Setting.getElement("/bat/isotope/calc/mean").setText(value2.toString());
			value3 = resultD.getBoolean("poisson");
			Setting.getElement("/bat/isotope/calc/poisson").setText(value3.toString());
			value = resultD.getDouble("scatter");
	    	Setting.getElement("bat/isotope/calc/scatter").setText(String.valueOf(value));
			value = resultD.getDouble("ra_norm");
	    	Setting.getElement("bat/isotope/calc/nominal_ra").setText(String.valueOf(value));
			if (resultD.getInt("cycles")>0) {
				value2 = resultD.getInt("cycles");
				Setting.getElement("/bat/isotope/db/sql/cycle").setText(value2.toString());
			} 
		    if (Setting.isotope.equalsIgnoreCase("C14")) {
		    	value = resultD.getDouble("ba_norm");
		    	Setting.getElement("bat/isotope/calc/nominal_ba").setText(value.toString());
		    }
		    log.debug("Calcset loaded.");
		} catch (SQLException e) {
			log.error("Could not get data from "+calcset_t);
		    log.info("SQLException: " + e.getMessage());
		    log.info("SQLState: " + e.getSQLState());
		    log.info("Query: " + sql);
		    log.info("VendorError: " + e.getErrorCode());
		    String message = String.format( "<html>Could not get data from "+calcset_t+"<br>("+e.getMessage()+")</html>");
		    JOptionPane.showMessageDialog( null, message );
		}
		try {			
		    String nameQ = "calcset, isobar_fact, isobar_err, "+
		    		"std_ra, std_ra_err, bl_ra, bl_ra_err, "+
		    		"a_slope, a_slope_off, b_slope, b_slope_off, "+
		    		"time_corr, first_run, last_run, bg_const, bg_const_err, "+
    				"bl_const_mass, bl_const, bl_const_err";
		    if (Setting.isotope.equalsIgnoreCase("C14")) {
		    	nameQ += ", std_ba, std_ba_err";
		    }
		    sql="SELECT "+nameQ+" FROM "+corr_t+" WHERE calcset='"+calcSet+"'";
		    ResultSet resultC = stmt.executeQuery(sql);
		    while (resultC.next()) {
		    	Corr corr = data.newCorrection();
		    	corr.isoFact = resultC.getDouble("isobar_fact");
				Setting.getElement("/bat/isotope/calc/bg/factor").setText(corr.isoFact.toString());
		    	corr.isoErr = resultC.getDouble("isobar_err");
				Setting.getElement("/bat/isotope/calc/bg/error").setText(corr.isoErr.toString());
		    	corr.constBG = resultC.getDouble("bg_const");
		    	corr.constBGErr = resultC.getDouble("bg_const_err");
		    	corr.constBlWeight = resultC.getDouble("bl_const_mass");
		    	corr.constBlRatio = resultC.getDouble("bl_const");
		    	corr.constBlErr = resultC.getDouble("bl_const_err");
		    	corr.std.std_ra = resultC.getDouble("std_ra");
		    	corr.std.std_ra_err = resultC.getDouble("std_ra_err");
			    if (Setting.isotope.equalsIgnoreCase("C14")) {
			    	corr.std.std_ba = resultC.getDouble("std_ba");
			    	corr.std.std_ba_err = resultC.getDouble("std_ba_err");
			    }
		    	corr.blank.ra_bg = resultC.getDouble("bl_ra");
		    	corr.blank.ra_bg_err = resultC.getDouble("bl_ra_err");
		    	corr.a_slope_off = resultC.getDouble("a_slope_off");
		    	corr.a_slope = resultC.getDouble("a_slope");
		    	corr.a_slope_off = resultC.getDouble("a_slope_off");
		    	corr.b_slope = resultC.getDouble("b_slope");
		    	corr.b_slope_off = resultC.getDouble("b_slope_off");
		    	corr.timeCorr = resultC.getDouble("time_corr");
		    	corr.firstRun = resultC.getString("first_run");
		    	corr.lastRun = resultC.getString("last_run");
		    	corrL.add(corr);
		    }
		    log.debug(corrL.size()+" corrections loaded:");
		} catch (SQLException e) {
			log.error("Could not get data from "+corr_t);
		    log.info("SQLException: " + e.getMessage());
		    log.info("SQLState: " + e.getSQLState());
		    log.info("Query: " + sql);
		    log.info("VendorError: " + e.getErrorCode());
		    String message = String.format( "<html>Could not get data from "+corr_t+"<br>("+e.getMessage()+")</html>");
		    JOptionPane.showMessageDialog( null, message );
	    } finally {
		    if (stmt != null) {
		        try {
		            stmt.close();
		        } catch (SQLException e) {
					log.error("Could not execute querry ");
				    log.info("SQLException: " + e.getMessage());
				    log.info("SQLState: " + e.getSQLState());
				    log.info("VendorError: " + e.getErrorCode());
		        }
		        stmt = null;
//		        conn = null;
		    }	
	    }
	    return corrL;
	}
	
	/**
	 * @param magazine 
	 */
	public boolean downloadMag(String magazine) {
		main.tba.update("Get data from DB",true);
		if (magazine!=null) {
		    if (conn==null) {
		    	log.debug("Start login");
		    	conn = login();
		    }
			if (conn!=null) {
			    Statement stmt = null;
				try {
				    if (magazine==null) {
				    	magazine = selectMagazine();
				    }
				    if (magazine!=null) {
					    String selectR = "";
					    String selectS = "";
					    for (int i=0;i<openDbS.size();i++) {
					    	if(!openDbS.get(i).equals("target_id")&&!openDbS.get(i).equals("magazine")) {
					    		selectS+=openDbS.get(i)+",";
					    	}
					    }
					    log.debug("Magazine opened: "+magazine);
					    stmt = conn.createStatement();
					    stmt.setQueryTimeout(timeout);
						sql="SELECT "+selectS+"target_id, magazine FROM "+target_t+
		    				" WHERE magazine='"+magazine+"' AND sample_nr>"+sampleMin+calcset_null;
				    	log.debug(sql);
						ResultSet result = stmt.executeQuery(sql);
	//					String samples="'";
				    	while (result.next()) {
	//			    		samples+=result.getString("target_id")+"','";
							Sample samp = main.data.setSample(result.getString("target_id"));
							for (int i=0; i<openDbS.size(); i++){
						    	if(!openDbS.get(i).equals("target_id")) {
						    		samp.setValue(result.getString(openDbS.get(i)), openJS.get(i));
						    	}
							}
				    	}	    	
				    	log.debug(main.data.sampleList.size()+" samples loaded.");
				    	if (main.data.sampleList.size()>0) {
							
							if (cycle_imp) {
							    for (int i=0;i<openDbR.size();i++) {
							    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("magazine")) {
							    		selectR+=cycle_t+"."+openDbR.get(i)+",";
							    	}
							    }
								sql= "SELECT "+selectR+run_t+".run, "+run_t+".target_id FROM "+cycle_t+", "+run_t+
										" WHERE ("+cycle_t+".run="+run_t+".run) AND "+run_t+".magazine='"+magazine+"' AND cycltrue is null ORDER BY "+cycle_t+".run DESC, "+cycle_t+".cycle ASC";
							} else {
							    for (int i=0;i<openDbR.size();i++) {
							    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("magazine")) {
							    		selectR+=openDbR.get(i)+",";
							    	}
							    }
								sql="SELECT "+selectR+"target_id,magazine FROM "+run_t+" WHERE magazine='"+magazine+"' AND sample_nr>"+sampleMin;
							}
						
					    	log.debug(sql);
						result = stmt.executeQuery(sql);
					    	while (result.next()) {
					    		Sample samp = main.data.getSample(result.getString("target_id"));
					    		if (samp!=null) {
									Run run = new Run( samp );
									for (int i=0; i<openDbR.size(); i++) {
								    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("magazine")) {
								    		run.setValue(result.getString(openDbR.get(i)), openJR.get(i));
								    	}
									}
									main.data.runListL.add(run);
								}
					    	}	
							if (cycle_imp) {
	//							log.debug("Reduce cycles: "+data.runListL+" divide by "+cycle_nr);
								main.data.runListL=Func.reduceCycle(main.data.runListL, cycle_nr);
							}
					    	log.debug(main.data.runListL.size()+" runs loaded.");
							return true;
				    	} else {
				    		return false;
				    	}
				    }
				    else {
				    	log.debug("Nothing loaded!");
						return false;
				    }
				} catch (SQLException e) {
					log.error("Could not execute download");
				    log.info("SQLException: " + e.getMessage());
				    log.info("Query: " + sql);
				    logout();
				    String message = String.format( "<html>Could not execute download<br>Did logout!<br>Try again!</html>");
				    JOptionPane.showMessageDialog( null, message );
					return false;
			    } finally {
				    if (stmt != null) {
				        try {
				            stmt.close();
				        } catch (SQLException e) {
							log.error("Could not execute querry ");
						    log.info("SQLException: " + e.getMessage());
						    log.info("SQLState: " + e.getSQLState());
						    log.info("VendorError: " + e.getErrorCode());
				        }
				        stmt = null;
	//			        conn = null;
				    }	
			    }
			} else {
				log.info("Didn't login!");
				return false;
			}
		} else return false;
 	}

	/**
	 * 
	 */
	public void openCalcSelect() {
		String calcset = this.selectCalcSet();
		openCalc(calcset);
	}

		/**
	 * 
	 */
	public void openCalc(String calcset) {
		main.tba.update("Get data from DB",true);
//		getSettings();
//	    Calc data = Setting.initCalcIso(Setting.isotope);
//		xmlSelect();
	    if (conn==null) {
	    	log.debug("Start login");
	    	conn = login();
	    }
		if (conn!=null) {
		    Statement stmt = null;
			try {
//			    if (calcset==null) {
//			    	calcset = selectCalcSet();
//			    }
			    if (calcset!=null) {
				    String selectR = "";
				    String selectS = "";
				    for (int i=0;i<openDbS.size();i++) {
				    	if(!openDbS.get(i).equals("target_id")&&!openDbS.get(i).equals("calcset")) {
				    		selectS+=target_t+"."+openDbS.get(i)+", ";
				    	}
				    }
				    selectS += target_t+".calcset, "+target_t+".target_id ";
				    
					stmt = conn.createStatement();
				    stmt.setQueryTimeout(timeout);				    
				    sql="SELECT "+calc_sample_t+".sample_nr, "+calc_sample_t+".prep_nr, "+calc_sample_t+".target_nr, "+calc_sample_t+".type, "
					+ calc_sample_t+".calcset, "+calc_sample_t +".active, " +calc_sample_t+".prep_bl, "+calc_sample_t+".std_bl, "+selectS
					+ "FROM ("+target_t+" JOIN "+calc_sample_t+") "
					+ "WHERE "+ "("+target_t+".sample_nr = "+calc_sample_t+".sample_nr) "
				    	+ "AND ("+target_t+".prep_nr = "+calc_sample_t+".prep_nr) "
				    	+ "AND ("+target_t+".target_nr = "+calc_sample_t+".target_nr) "
				    	+ "AND "+calc_sample_t+".calcset='"+calcset+"'";
//				    sql="SELECT  "+calcset_t+".sample_nr, "+calcset_t+".prep_nr, "+calcset_t+".target_nr, "+calcset_t+".type, "+calcset_t+".calcset, "+calcset_t+".active, "
//				    +calcset_t+".prep_bl, "+calcset_t+".std_bl from "+calc_sample_t+" WHERE calcset='"+calcset+"' AND sample_nr>"+sampleMin;
				    ResultSet result = stmt.executeQuery(sql);
				    log.debug(sql);
				    
				    Statement stmt2 = conn.createStatement();
				    stmt2.setQueryTimeout(timeout);		
//				    this.data = main.data;
				    main.data.removeData();

				    sql="SELECT cycles from "+calcset_t+" where calcset='"+calcset+"'";
				    ResultSet cycleResult = stmt2.executeQuery(sql);
				    cycleResult.next();
				    Integer cycle =cycleResult.getInt("cycles");
				     
				    while (result.next()) {
//				    	log.debug(result.getString("type"));
				    	if (!(result.getString("std_bl")==null)) {
				    	    main.data.stdNom.setActive(result.getString("type"),result.getBoolean("std_bl"));
				    	}
				    	log.debug("Import sample: "+result.getString("target_id"));
				    	Sample samp = main.data.setSample(result.getString("target_id"));
				    	samp.setValue(result.getString("type"), "type");
				    	samp.setValue(result.getString("active"), "active");
				    	
				    	for (int i=0; i<openDbS.size(); i++){
					    	if(!openDbS.get(i).equals("target_id")&&!openDbS.get(i).equals("calcset")&&!openDbS.get(i).equals("type")) {
					    		samp.setValue(result.getString(openDbS.get(i)), openJS.get(i));
//					    		log.debug(result.getString(openDbS.get(i)));
					    	}
					}
				    	
//						if (cycle>0) {
//					    		log.debug("cycle import");
//							sql="SELECT "+selectR+" FROM "+cycle_t
//							+" WHERE sample_nr="+result.getString("sample_nr")+" AND prep_nr="+result.getString("prep_nr")+"  AND target_nr="
//							+result.getString("target_nr")+"'  AND cycltrue is null ORDER BY "+cycle_t+".run DESC, "+cycle_t+".cycle ASC";
//						} else {
////					    		log.debug("runimport");
//							sql="SELECT "+selectR+" FROM "+run_t+
//		    					" WHERE sample_nr='"+result.getString("sample_nr")+"' AND prep_nr='"+result.getString("prep_nr")
//		    					+"'  AND target_nr='"+result.getString("target_nr")+"'";
//						}

				    	
//				    	log.debug(sql);
//					ResultSet result2;
//					result2 = stmt2.executeQuery(sql);
//				    	while (result2.next()) {
//							Run run = new Run( main.data.setSample(result2.getString("target_id")) );
//							for (int i=0; i<openDbR.size(); i++){
//						    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("calcset")) {
//						    		run.setValue(result2.getString(openDbR.get(i)), openJR.get(i));
//						    	}
//							}
//							main.data.runListL.add(run);
				    	}		 
				    
				    
				     // select for data from run / cycle table				     
				    for (int i=0;i<openDbR.size();i++) {
				    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("calcset")&&!openDbR.get(i).equals("run")) {
				    		selectR+=openDbR.get(i)+", ";
				    	}
				    }
				    selectR += "target_id, "+run_t+".run as run";
					   
				    if (save_all) {

					    Statement stmt3 = conn.createStatement();
					    stmt3.setQueryTimeout(timeout);	
					    sql="SELECT active from "+calc_run_t+" where calcset='"+calcset+"'";
					    ResultSet calc_run = stmt3.executeQuery(sql);
					    log.debug(sql);
					    
					    if (calc_run.first()) {
					    	// read back status of cycles
						    stmt3 = conn.createStatement();
						    stmt3.setQueryTimeout(timeout);	
						    sql="SELECT "+calc_cycle_t+".run as run, "+calc_cycle_t+".cycle as cycle, active from ("+calc_cycle_t+" join "+cycle_t+") "
						    		+"where calcset='"+calcset+"' and ("+calc_cycle_t+".run = "+cycle_t+".run) and ("
						    		+calc_cycle_t+".cycle = "+cycle_t+".cycle) and active != (cycltrue is null)";
						    log.debug(sql);
						    ResultSet cycle_update = stmt3.executeQuery(sql);
						    while (cycle_update.next()) {
						    	main.db.getConn().setActive(cycle_update.getString("run"), cycle_update.getInt("cycle"), cycle_update.getBoolean("active"));
						    	log.debug("Cycle "+cycle_update.getInt("cycle")+" of run "+cycle_update.getString("run")+" is set "+cycle_update.getBoolean("active"));
						    }
							log.debug(sql);
					    	
					    	// get data when also runs/cycles are saved
							if (cycle>0) {
					    		log.debug("cycle import");
								sql="SELECT "+selectR+" FROM "+cycle_t
								+" WHERE sample_nr="+result.getString("sample_nr")+" AND prep_nr="+result.getString("prep_nr")+"  AND target_nr="
								+result.getString("target_nr")+"'  AND cycltrue is null ORDER BY "+cycle_t+".run DESC, "+cycle_t+".cycle ASC";
							} else {
		//				    		log.debug("runimport");
								sql="SELECT "+selectR+", "+calc_run_t+".active as active"
									+ " FROM ("+run_t+" JOIN "+calc_run_t+" JOIN "+calc_sample_t+") "
									+ "WHERE "+ "("+run_t+".sample_nr = "+calc_sample_t+".sample_nr) "
								    + "AND ("+run_t+".prep_nr = "+calc_sample_t+".prep_nr) "
								    + "AND ("+run_t+".target_nr = "+calc_sample_t+".target_nr) "
								    + "AND ("+run_t+".run = "+calc_run_t+".run) "
								    + "AND "+calc_sample_t+".calcset='"+calcset+"' "
								    + "AND "+calc_run_t+".calcset='"+calcset+"' "
							    	+ "AND "+calc_sample_t+".calcset='"+calcset+"'";
							}
							
							ResultSet result2;
							result2 = stmt2.executeQuery(sql);
							log.debug(sql);
					    	while (result2.next()) {
								Run run = new Run( main.data.setSample(result2.getString("target_id")) );
								for (int i=0; i<openDbR.size(); i++){
							    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("calcset")) {
							    		run.setValue(result2.getString(openDbR.get(i)), openJR.get(i));
							    	}
								}
								run.setValue(result2.getString("active"), "active");
								main.data.runListL.add(run);
					    	}		 
					    } else {
							if (cycle>0) {
					    		log.debug("cycle import");
								sql="SELECT "+selectR+" FROM "+cycle_t
								+" WHERE sample_nr="+result.getString("sample_nr")+" AND prep_nr="+result.getString("prep_nr")+"  AND target_nr="
								+result.getString("target_nr")+"'  AND cycltrue is null ORDER BY "+cycle_t+".run DESC, "+cycle_t+".cycle ASC";
							} else {
		//				    		log.debug("runimport");
								sql="SELECT "+selectR 
									+ " FROM ("+run_t+" JOIN "+calc_sample_t+") "
									+ "WHERE "+ "("+run_t+".sample_nr = "+calc_sample_t+".sample_nr) "
								    + "AND ("+run_t+".prep_nr = "+calc_sample_t+".prep_nr) "
								    + "AND ("+run_t+".target_nr = "+calc_sample_t+".target_nr) "
								    + "AND "+calc_sample_t+".calcset='"+calcset+"'";
							}
							
							ResultSet result2;
							result2 = stmt2.executeQuery(sql);
							log.debug(sql);
							
					    	while (result2.next()) {
								Run run = new Run( main.data.setSample(result2.getString("target_id")) );
								for (int i=0; i<openDbR.size(); i++){
							    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("calcset")) {
							    		run.setValue(result2.getString(openDbR.get(i)), openJR.get(i));
							    	}
								}
								main.data.runListL.add(run);
					    	}		 
	
					    }
				    } else {
						if (cycle>0) {
				    		log.debug("cycle import");
							sql="SELECT "+selectR+" FROM "+cycle_t
							+" WHERE sample_nr="+result.getString("sample_nr")+" AND prep_nr="+result.getString("prep_nr")+"  AND target_nr="
							+result.getString("target_nr")+"'  AND cycltrue is null ORDER BY "+cycle_t+".run DESC, "+cycle_t+".cycle ASC";
						} else {
	//				    		log.debug("runimport");
							sql="SELECT "+selectR 
								+ " FROM ("+run_t+" JOIN "+calc_sample_t+") "
								+ "WHERE "+ "("+run_t+".sample_nr = "+calc_sample_t+".sample_nr) "
							    + "AND ("+run_t+".prep_nr = "+calc_sample_t+".prep_nr) "
							    + "AND ("+run_t+".target_nr = "+calc_sample_t+".target_nr) "
							    + "AND "+calc_sample_t+".calcset='"+calcset+"'";
						}
						
						ResultSet result2;
						result2 = stmt2.executeQuery(sql);
						log.debug(sql);
						
				    	while (result2.next()) {
							Run run = new Run( main.data.setSample(result2.getString("target_id")) );
							for (int i=0; i<openDbR.size(); i++){
						    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("calcset")) {
						    		run.setValue(result2.getString(openDbR.get(i)), openJR.get(i));
						    	}
							}
							main.data.runListL.add(run);
				    	}		 				    	
				    }

				    
					if (cycle>0) {
							//							log.debug("Reduce cycles: "+data.runListL+" divide by "+cycle_nr);
					    main.data.runListL=Func.reduceCycle(main.data.runListL, cycle);
					}
			    	log.debug(main.data.sampleList.size()+" samples loaded.");
			    	log.debug(main.data.runListL.size()+" runs loaded.");
			    	main.data.initData(downloadCorr(conn,calcset,main.data));
			    	main.data.calcSet=calcset;
			    	main.tba.update("Calc-set loaded from DB",false);
			    	Setting.setLastMag(main.data.calcSet);
			    }
			    else {
					main.tba.update("No data from DB",false);
			    	log.debug("Nothing loaded!");
			    }
			} catch (SQLException e) {
			    log.error("Could not execute download");
			    log.info("SQLException: " + e.getMessage());
			    log.info("Query: " + sql);
			    logout();
			    String message = String.format( "<html>Could not execute download<br>Did logout!<br>Try again!<br>"+e.getMessage()+"</html>");
			    JOptionPane.showMessageDialog( null, message );
		    } finally {
			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch (SQLException e) {
						log.error("Could not execute querry ");
					    log.info("SQLException: " + e.getMessage());
					    log.info("SQLState: " + e.getSQLState());
					    log.info("VendorError: " + e.getErrorCode());
			        }
			        stmt = null;
//			        conn = null;
			    }	
		    }
		} else {
			log.info("Didn't login!");
		}
 	}

//	/**
//	 * @param CalcSet 
//	 * @return data
//	 */
//	public Calc openCalcOld(String CalcSet) {
//		main.tba.update("Get data from DB",true);
////		getSettings();
////	    Calc data = Setting.initCalcIso(Setting.isotope);
////		xmlSelect();
//	    String magazine = CalcSet;
//	    if (conn==null) {
//	    	log.debug("Start login");
//	    	conn = login();
//	    }
//		if (conn!=null) {
//		    Statement stmt = null;
//			try {
//			    if (magazine==null) {
//			    	magazine = selectMagazine();
//			    }
//			    if (magazine!=null) {
//				    String selectR = "";
//				    String selectS = "";
//				    for (int i=0;i<openDbR.size();i++) {
//				    	selectR+=openDbR.get(i)+",";
//				    }
//				    selectR += run_t+"."+"calcset";
//				    for (int i=0;i<openDbS.size();i++) {
//				    	selectS+=openDbS.get(i)+",";
//				    }
//				    selectS += "calcset";
//				    log.debug("Magazine opened: "+magazine);
//					ArrayList<Corr> corrL = downloadCorr(conn, magazine, data);			
//					stmt = conn.createStatement();
//				    stmt.setQueryTimeout(timeout);
//					sql="SELECT "+selectR+", "+selectS+" FROM "+run_t+", "+target_t+
//	    				" WHERE ("+run_t+".label="+target_t+".label) " +
//	    				"AND ("+run_t+".calcset="+target_t+".calcset) AND "+run_t+".calcset='"+magazine+"'";
////			    	log.debug(sql);
//					ResultSet result = stmt.executeQuery(sql);
//			    	while (result.next()) {
//						Run run = new Run( data.setSample(result.getString("label")) );
//						for (int i=0; i<openDbR.size(); i++){
//							run.setValue(result.getString(openDbR.get(i)), openJR.get(i));
//						}
//						for (int i=0; i<openDbS.size(); i++){
//							run.setValue(result.getString(openDbS.get(i)), openJS.get(i));
//						}
//						data.runListL.add(run);
//			    	}	    	
//			    	log.debug("Correction size: "+ corrL.size());
//			    	log.debug("Data size: "+data.runListL.size());
//					data.initData(corrL);
//					main.tba.update("Data loaded from DB",false);
//			    }
//			    else {
//					main.tba.update("No data from DB",false);
//			    	log.debug("Nothing loaded!");
//			    }
//			} catch (SQLException e) {
//				log.error("Could not execute download");
//			    log.info("SQLException: " + e.getMessage());
//			    log.info("SQLState: " + e.getSQLState());
//			    log.info("Query: " + sql);
//			    log.info("VendorError: " + e.getErrorCode());
//			    logout();
//			    String message = String.format( "<html>Could not execute download<br>Did logout!<br>Try again!</html>");
//			    JOptionPane.showMessageDialog( null, message );
//		    } finally {
//			    if (stmt != null) {
//			        try {
//			            stmt.close();
//			        } catch (SQLException e) {
//						log.error("Could not execute querry ");
//					    log.info("SQLException: " + e.getMessage());
//					    log.info("SQLState: " + e.getSQLState());
//					    log.info("VendorError: " + e.getErrorCode());
//			        }
//			        stmt = null;
////			        conn = null;
//			    }	
//		    }
//		} else {
//			log.info("Didn't login!");
//		}
//		return data;
// 	}

	/**
	 * @return calc-set
	 */
	public String selectCalcSet() {
		String calSM=null;
		Setting.no_data=true;
		main.data.magazine="";
		
		ArrayList<Mag> magList = new ArrayList<Mag>();
		String query="";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    if (conn==null) {
	    	log.debug("Start login");
	    	conn = login();
	    }
		if (conn!=null) {
			try {
		    		Statement stmt = conn.createStatement();
		    	    stmt.setQueryTimeout(timeout);
		    		query = "SELECT calcset, date_calc, magazine FROM "+calcset_t
		    			+" WHERE date_calc BETWEEN '"+df.format(date.getTime())+"' AND '"+df.format(date2.getTime())
		    			+"' ORDER BY date_calc DESC";
				ResultSet result = stmt.executeQuery (query);
//			    log.debug("Query: "+query);
				String tempMag;
				String tempDat;
				String tempSet;
				while (result.next()){
	    			tempMag=result.getString("magazine");
	    			tempSet=result.getString("calcset");
	    			tempDat=result.getString("date_calc");
		    		Mag mag = new Mag(tempDat, tempMag, tempSet);
		    		magList.add(mag);
				}
			}
			catch (SQLException e){
				String message = String.format( "<html>Could not get calc-set list!<br>(SQLException)</html>");
			    JOptionPane.showMessageDialog( null, message );
			    log.error("Could not get calc-set list! (SQLException)");
			    log.error("Query: "+query);
			    log.error(e);
			    this.logout();
			}
	
			String[] calcSetOnly = new String[magList.size()];
			String[] magInfo = new String[magList.size()];
			int last=0;
			for ( int i=0; i<magList.size(); i++)
			{
				calcSetOnly[i] = magList.get(i).calcSet;
				if (calcSetOnly[i].equals(Setting.getString("/bat/isotope/db/sql/last_mag"))) {
					last=i;
				}
				magInfo[i] = ((magList.get(i).calcSet+"                    ").substring(0,20)
							+ " | "
							+ (magList.get(i).magazine+"                    ").substring(0,20)
							+ " | "
							+ magList.get(i).date);			
			}
			try {
				calSM = JOptionPane.showInputDialog(null,
			            "Select calc-set",
			            "DB connect",
			            JOptionPane.QUESTION_MESSAGE,
			            null, magInfo,
			            magInfo[last]).toString();
			
				calSM = calSM.split(" | ")[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				log.error("No calc-sets!");
			} catch (NullPointerException e) {
				log.error("No calc-set selected!");
			}
		}
   		Setting.getElement("/bat/isotope/db/sql/last_mag").setText(calSM);
   		log.debug("Selected calcset: "+calSM);
		return calSM;
	}


	/**
	 * @return magazine name
	 */
	public String selectMagazine() {
		String magazine=null;
		Setting.no_data=true;
	    if (conn==null) {
	    	log.debug("Start login");
	    	conn = login();
	    }
		if (conn!=null) {
			ArrayList<Magazine> magList = new ArrayList<Magazine>();
			String query="";
			Statement stmt = null;
			try {
	    		stmt = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    	    stmt.setQueryTimeout(Setting.getInt("/bat/isotope/db/sql/timeout"));
	    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    		
	    		query = "SELECT MIN(run) AS run1, MAX(run) AS run2, "+target_t+".magazine, MAX(timedat) AS timedat"
	    		+" from "+target_t+" join "+run_t
	    		+" ON "+run_t+".sample_nr = "+target_t+".sample_nr"
	    		+" AND "+run_t+".`prep_nr` = "+target_t+".prep_nr"
	    		+" AND "+run_t+".`target_nr` = "+target_t+".`target_nr`" 
	    		+ "WHERE "+target_t+".magazine IS NOT NULL"
//	    		+" AND timedat BETWEEN '"+df.format(date.getTime())+"' AND '"+df.format(date2.getTime())+"'"+calcset_null
	    		+" AND "+target_t+".sample_nr>"+sampleMin
	    		+" GROUP BY "+target_t+".magazine ORDER BY timedat DESC";
	    		log.debug("start");
	    		ResultSet result = stmt.executeQuery (query);
	    		log.debug(query);
	    		while (result.next()) {
		    		Magazine mag = new Magazine(result.getString("run1"),result.getString("run2"),result.getString("magazine"),result.getDate("timedat"));
		    		magList.add(mag);
//		    		log.debug(magList.size());
//		    		log.debug(result.getString("run1")+"/"+result.getString("run2")+"/"+result.getString("magazine"));
	    		}
	    		log.debug("end");
	    		if (magList.isEmpty()) {
	    			magazine=null;
	        		log.debug(query);
	    		} else {	    		
					String[] magOnly = new String[magList.size()];
					String[] magInfo = new String[magList.size()];
					int last=0;
					if (magList.size()>0) {
						for ( int i=0; i<magList.size(); i++) {
							magOnly[i] = magList.get(i).magazine;
//							if (magOnly[i].equals(Setting.getString("/bat/isotope/db/sql/last_mag"))) {
//								last=i;
//							}
							df = new SimpleDateFormat("dd.MM.yyyy");
							magInfo[i] = (magList.get(i).magazine+"                    ").substring(0,20)
										+ (magList.get(i).runStart
										+ "-"
										+ magList.get(i).runEnd+"                     ").substring(0,22)
										+ df.format(magList.get(i).timeDat)+ "  ";			
						}
						magazine= (String) JOptionPane.showInputDialog(main,
					            "Select magazine",
					            "DB connect",
					            JOptionPane.QUESTION_MESSAGE,
					            null, magInfo,
					            magInfo[last]);
						try {
							magazine = magazine.split(" ")[0];
						} catch (NullPointerException e) {
							log.info("No Magazine selected!");
						}
					} else {
						String message = String.format( "<html>No Magazine available!<br>(between "+date.getTime()+" and "+date2.getTime()+")</html>");
					    JOptionPane.showMessageDialog( null, message );
					    log.info("No Magazine available!");
					    logout();
					}
	    		}
			}
			catch (SQLException e){
				String message = String.format( "<html>Could not get Magazine List!<br>(SQLException)</html>");
			    JOptionPane.showMessageDialog( null, message );
			    log.error("Could not get Magazine List! (SQLException)");
			    log.error("Query: "+query);
			    log.error(e);
			    logout();
		    } finally {
			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch (SQLException e) {
						log.error("Could not close statement!");
			        }
			        stmt = null;
			    }	
		    }
		}
		return magazine;	
	}

	
	/**
	 * @return magazine name
	 */
	public ArrayList<StdData> getStd() {
	    ArrayList<StdData> stdList=new ArrayList<StdData>();
		Setting.no_data=true;
    	    	if (conn==null) {
    	    	    log.debug("Start login");
    	    	    conn = login();
    	    	}
		if (conn!=null) {
			String query="";
			Statement stmt = null;
			try {
	    		stmt = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    		stmt.setQueryTimeout(Setting.getInt("/bat/isotope/db/sql/timeout"));
	    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    		
	    		query = "SELECT type, indexnr, f14c, f14c_sig, d13c, d13c_sig, d13c_nom, blank, active from "+sampletype_t+" ORDER BY indexnr ASC";
	    		ResultSet result = stmt.executeQuery (query);
	    		log.debug(query);
	    		while (result.next()) {
	    		    StdData std = new StdData();
	    		    std.name = result.getString("type");
	    		    std.F14C = result.getDouble("f14c");
	    		    std.F14C_sig = result.getDouble("f14c_sig");
	    		    std.delta = result.getDouble("d13c");
	    		    std.delta_sig = result.getDouble("d13c_sig");
	    		    std.delta_nom = result.getDouble("d13c_nom");
	    		    std.blank = result.getBoolean("blank");
	    		    std.active = result.getBoolean("active");
	    		    stdList.add(std);
	    		}
			}
			catch (SQLException e){
				String message = String.format( "<html>Could not get standard and blank list from db!<br>(SQLException)</html>");
			    JOptionPane.showMessageDialog( null, message );
			    log.error("Could not get standard/blank List! (SQLException)");
			    log.error("Query: "+query);
			    log.error(e);
			    logout();
		    } finally {
			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch (SQLException e) {
						log.error("Could not close statement!");
			        }
			        stmt = null;
			    }	
		    }
		}
		return stdList;	
	}

	
	/**
	 * @return latest magazine
	 */
	public String latestMag() {
		String magazine=null;
		Setting.no_data=true;
//		conn=null;
	    if (conn==null) {
	    	log.debug("Start login");
	    	conn = login();
	    } 
	    if (conn!=null) {
			String query="";
			Statement stmt = null;
			try {
	    		stmt = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    	    stmt.setQueryTimeout(Setting.getInt("/bat/isotope/db/sql/timeout"));
	    		
				query = "SELECT sample_nr, target_nr, prep_nr, timedat AS time FROM "+run_t+" WHERE sample_nr>"+sampleMin+" ORDER BY time DESC LIMIT 1";
	    		ResultSet result = stmt.executeQuery (query);
	    		result.next();
				query = "SELECT magazine FROM "+run_t+" WHERE sample_nr="+result.getString("sample_nr")+
				" AND prep_nr="+result.getString("prep_nr")+
				" AND target_nr="+result.getString("target_nr")+" LIMIT 1";
	    		result = stmt.executeQuery (query);
	    		log.debug("Obtained results");
	    		log.debug(query);
	    		result.next();
	    		magazine=result.getString("magazine");
			}
			catch (SQLException e){
				String message = String.format( "<html>Could not get Magazine!<br>(SQLException)</html>");
			    JOptionPane.showMessageDialog( null, message );
			    log.info("Could not get Magazine List! (SQLException or empty result set)");
			    log.info("Query: "+query);
			    log.info(e);
			    logout();
		    } finally {
			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch (SQLException e) {
						log.error("Could not close statement!");
			        }
			        stmt = null;
			    }	
		    }
		}
		return magazine;		
	}

	
    /**
 	 * @param run
 	 * @return Run
 	 */
 	public Run updateRun(Run run) {
	    if (conn==null) {
	    	log.debug("Start login");
	    	conn = login();
	    }
		if (conn!=null) {
			Statement stmt = null;
			try {
	    		stmt = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    	    stmt.setQueryTimeout(Setting.getInt("/bat/isotope/db/sql/timeout"));

			    String selectR = "";
			    for (int i=0;i<openDbR.size();i++) {
			    	if(!openDbR.get(i).equals("target_id")) {
			    		selectR+=openDbR.get(i)+",";
			    	}
			    }
			    selectR += "target_id";
		//	    samples=samples.substring(0,samples.length()-2);
				sql="SELECT "+selectR+" FROM "+run_t+
				" WHERE run='"+run.run+"'";
    			   log.debug(sql);
    			   ResultSet result = stmt.executeQuery(sql);
    		    	   result.next();
				for (int i=0; i<openDbR.size(); i++){
        			    	if(!openDbR.get(i).equals("target_id")&&!openDbR.get(i).equals("magazine")) {
        			    		run.setValue(result.getString(openDbR.get(i)), openJR.get(i));
//        			    		log.debug(result.getString(openDbR.get(i))+"--"+ openJR.get(i)+"--"+run.run);
        			    	}
				}
			        main.data.runPreCalc(run);

				log.debug("Run updated.");
			} catch (SQLException e) {
				log.error("Could not execute download");
			    log.info("SQLException: " + e.getMessage());
			    log.info("Query: " + sql);
			    logout();
			    String message = String.format( "<html>Could not execute download<br>Did logout!<br>Try again!</html>");
			    JOptionPane.showMessageDialog( null, message );
		    } finally {
			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch (SQLException e) {
						log.error("Could not execute querry for run.");
					    log.info("SQLException: " + e.getMessage());
					    log.info("SQLState: " + e.getSQLState());
					    log.info("VendorError: " + e.getErrorCode());
			        }
			        stmt = null;
//			        conn = null;
			    }	
		    }
		} else {
			log.info("Didn't login!");
		}
		return run;
	}
    	
	
	private Connection login() {
		this.getSettings();
		this.getSelectSQL();
		Connection con=null;
		JPanel      connectionPanel;
		String[] ConnectOptionNames = { "Login", "Cancel" };
 		// Create the labels and text fields.
		JLabel     userNameLabel = new JLabel("User ID:   ", SwingConstants.RIGHT);
	 	JTextField userNameField = new JTextField("");
	 	userNameField.setText(user);
		JLabel     passwordLabel = new JLabel("Password:   ", SwingConstants.RIGHT);
		JTextField passwordField = new JPasswordField(pw);
//		log.debug(pw);
		passwordField.setPreferredSize(new Dimension(100,10));
		connectionPanel = new JPanel(false);
		connectionPanel.setLayout(new BoxLayout(connectionPanel,BoxLayout.X_AXIS));
		JPanel namePanel = new JPanel(false);
		namePanel.setLayout(new GridLayout(0, 1));
		namePanel.add(userNameLabel);
		namePanel.add(passwordLabel);
		JPanel fieldPanel = new JPanel(false);
		fieldPanel.setLayout(new GridLayout(0, 1));
		fieldPanel.add(userNameField);
		fieldPanel.add(passwordField);
		connectionPanel.add(namePanel);
		connectionPanel.add(fieldPanel);
		if(JOptionPane.showOptionDialog(
				main, connectionPanel, 
				"DB connect",
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.INFORMATION_MESSAGE,
                null, ConnectOptionNames, 
                ConnectOptionNames[0]) 
                != 0) {
			pw = null;
		} else {
	        user = userNameField.getText();
	   		Setting.getElement("/bat/isotope/db/sql/user").setText(user);
	        pw = passwordField.getText();
	    	try {
				Class.forName(Setting.getString("/bat/isotope/db/sql/driver")).newInstance();
				try {
					DriverManager.setLoginTimeout(timeout);
					con = DriverManager.getConnection("jdbc:"+url+"?user="+user+"&password="+pw);
				        TimerTask action = new TimerTask() {
				            public void run() {
				        	logout();
				        	log.debug("LogOut");
				            }
				        };
				        Timer timer = new Timer();
				    timer.schedule(action, (timeout*60000));
				    log.debug("Start logout timer");
				} catch (SQLException e) {
					String message = String.format( "<html>Could not login to "+url+"<br>with user "+user+".</html>");
					JOptionPane.showMessageDialog( null, message );
					log.debug("Could not connect: "+con);
					log.debug("SQLException: " + e.getMessage());
				}
				log.debug("Login to "+url+" with id '"+user+"'");
		} catch (InstantiationException e1) {
			String message = String.format( "<html>Could not DB driver!</html>");
			JOptionPane.showMessageDialog( null, message );
			log.error("DB driver for import could not be loaded!");
			log.error(e1);
		    e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			String message = String.format( "<html>Could not DB driver!</html>");
			JOptionPane.showMessageDialog( null, message );
			log.error("DB driver for import could not be loaded!");
			log.error(e1);
		    e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			String message = String.format( "<html>Could not DB driver!</html>");
			JOptionPane.showMessageDialog( null, message );
			log.error("DB driver for import could not be loaded!");
			log.error(e1);
		    e1.printStackTrace();
		}
        }
		return con;
	}
	
	private class Mag
	{
		/**
		 * 
		 */
		private String calcSet;
		
		/**
		 * 
		 */
		public String magazine;
		
		/**
		 * 
		 */
		public String date;
		
		Mag( String date, String magazine, String calcSet)
		{
			this.date = date;
			this.magazine = magazine;
			this.calcSet = calcSet;
		}
	}

    private class Magazine
    {
    	/**
    	 * 
    	 */
    	private String runStart;
    	
    	/**
    	 * 
    	 */
    	private String runEnd;
    	
    	/**
    	 * 
    	 */
    	private String magazine;
    	
    	/**
    	 * 
    	 */
    	private Date timeDat;
    	
//    	Magazine( String run1, String run2, String magazine, String timedat)
//    	{
//    		runStart=run1;
//    		runEnd=run2;
//    		this.magazine=magazine;
//			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
//			try {
//				timeDat=df.parse(timedat);
//			} catch (ParseException e) {
//				log.debug("Wrong date format: "+timedat);
//			}
//    	}
//    	
    	Magazine( String run1, String run2, String magazine, Date timedat)
    	{
    		runStart=run1;
    		runEnd=run2;
    		this.magazine=magazine;
    		this.timeDat=timedat;
    	}	
    }

	/**
	 * 
	 */
	public boolean downloadRuns() {
		String message = String.format( "<html>Not jet implemented!</html>");
	    JOptionPane.showMessageDialog( null, message );
	    return false;
	}

	
	public void addRuns() {
		String message = String.format( "<html>Not jet implemented!</html>");
	    JOptionPane.showMessageDialog( null, message );
	}

	public DbCycle getConn() {
		DbCycle dbCycle=null;
	    if (conn==null) {
	    	log.debug("Start login");
	    	conn = login();
	    }
		if (conn!=null) {
			try {
				dbCycle = new DbCycle(conn);
			} catch (JDOMException e) {
		   		log.error("Could not open db setup file!");
				String message = String.format( "<html>Could not open connection for update!</html>");
			    JOptionPane.showMessageDialog( null, message );
			} catch (IOException e) {
				String message = String.format( "<html>Could not open connection for update!</html>");
			    JOptionPane.showMessageDialog( null, message );
				log.error("Could not open db setup file!");
		   	} catch (HeadlessException e) {
				String message = String.format( "<html>Headless Exception in MySQL connection.</html>");
			    JOptionPane.showMessageDialog( null, message );
				log.error("Headless Exception in MySQL connection");
				e.printStackTrace();
				conn=null;
			} 
		}
		return dbCycle;
	}

	public Boolean isConn() {
		return (conn!=null);
	}

	public Boolean runTrue(Run run, Boolean active) {
	    if (conn==null) {
	    	log.debug("Start login");
	    	conn = login();
	    }
		if (conn!=null) {
    		try {
	    		String query = "call "+Setting.getString("/bat/isotope/db/"+Setting.db_name+"/run_enable")+
	    		"("+active+",'"+run.run+"')";
				Statement stmt = conn.createStatement ();
				stmt.setQueryTimeout(10);
				stmt.execute(query);
				ResultSet result = stmt.executeQuery("SELECT ratrue FROM "+run_t+" WHERE run='"+run.run+"'");
				result.next();
				run.active = result.getBoolean("ratrue");
				log.debug("Run "+run.run+" set "+result.getBoolean("ratrue"));
				return true;
	    	}
			catch (SQLException e) {
				String message = String.format( "Update failed!");
			    JOptionPane.showMessageDialog( null, message );
				log.debug(e);
				e.printStackTrace();
				return false;
			}			
		} else {
			return false;
		}
	}

}

