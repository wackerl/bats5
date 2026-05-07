import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;


/**
 * @author lukas
 *
 */
public class GraphCycle extends JPanel {
	private static final Logger log = LogManager.getLogger("GraphCycle");
	
        JFormattedTextField textField;
        JFormattedTextField slopeField;
        JFormattedTextField interceptField;
        JFormattedTextField rField;
        private JFreeChart chart;
        private XYDataset dataSet;
        private XYDataset dataSet2;
        private XYDataset dataSet3;
        String x;
        String y;
        Double s_range;
    
        private static int h1;
        private static int p;
        private static String ft;
        private static Font fTitel;
        private static Font fText;
        private static Font fAxes;
        private ArrayList<Cycle> cycList;

	
	/**
     * @param cycList 
     */
	public GraphCycle(ArrayList<Cycle> cycList) {
	    
	this.cycList = cycList;

        h1 = Setting.getInt("/bat/general/font/h1");
        p = Setting.getInt("/bat/general/font/p");
        ft = Setting.getString("/bat/general/font/type");
    	fTitel = new Font(ft, Font.PLAIN, h1);
    	fAxes = new Font(ft, Font.PLAIN, p);
    	fText = new Font(ft, Font.PLAIN, p);


        this.setPreferredSize(new Dimension(Setting.getInt("/bat/general/frame/cycle/graph/width"),Setting.getInt("/bat/general/frame/cycle/graph/height")));
        this.setLayout(new BorderLayout());
	dataSet = Func.getXY_cyc(cycList, Setting.getString("/bat/isotope/graph/cycle/serie1"), Setting.getString("/bat/isotope/graph/cycle/serie2"));
	 chart = createChart(dataSet);
	 XYPlot plot = (XYPlot) chart.getPlot();
        dataSet3 = Func.getXY_act(cycList, "active");
        plot.setDataset(2, dataSet3);
        
        if(Setting.isotope.equalsIgnoreCase("C14")) {
		dataSet2 = Func.getXY_cyc2(cycList, Setting.getString("/bat/isotope/graph/cycle/serie3"));
            NumberAxis axis2 = new NumberAxis(Setting.getString("/bat/isotope/graph/cycle/y_axes3"));
            axis2.setRange(Setting.getDouble("/bat/isotope/graph/cycle/y_axes3/min"), Setting.getDouble("/bat/isotope/graph/cycle/y_axes3/max"));
            axis2.setLabelPaint(Color.blue);
            axis2.setTickLabelPaint(Color.blue);
            plot.setRangeAxis(1, axis2);
            plot.setDataset(1, dataSet2);
            plot.mapDatasetToRangeAxis(1, 1);
        }
        
        this.add(new ChartPanel(chart));

	}

    private JFreeChart createChart(XYDataset dataset) {
//	XY
	XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseSeriesVisible(true);
        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesPaint(1, Color.red);
        renderer.setSeriesPaint(2, Color.gray);
        renderer.setSeriesPaint(3, Color.blue);
        renderer.setBaseShapesFilled(true);
        renderer.setBaseShapesVisible(true);
        renderer.setDrawOutlines(true);
        renderer.setSeriesItemLabelFont(1,fText);
        renderer.setBaseItemLabelFont(fText);
        renderer.setLinesVisible(false);
        XYPlot plot = new XYPlot(
        	dataset,
        	new NumberAxis(Setting.getString("/bat/isotope/graph/cycle/x_axes")),     // x axis label
        	new NumberAxis(Setting.getString("/bat/isotope/graph/cycle/y_axes1")+"  /  "+Setting.getString("/bat/isotope/graph/cycle/y_axes2")),     // y axis label
        	renderer
        	);
        XYBarRenderer renderer1 = new XYBarRenderer(0.0);
        renderer1.setBarPainter(new StandardXYBarPainter());
        renderer1.setBaseSeriesVisible(true);
        renderer1.setSeriesPaint(0, Color.LIGHT_GRAY);
        renderer1.setShadowVisible(false);
//        renderer1.setDrawBarOutline(false);
        plot.setRenderer(2,renderer1);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Setting.getColor("/bat/isotope/graph/background"));
        plot.setRangeGridlinePaint(Setting.getColor("/bat/isotope/graph/background"));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        ((NumberAxis)plot.getDomainAxis(1)).setNumberFormatOverride(new DecimalFormat("0.00%"));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        if(Setting.isotope.equalsIgnoreCase("C14")) {
    	    XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
            renderer2.setBaseSeriesVisible(true);
            renderer2.setSeriesPaint(0, Color.blue);
            renderer2.setBaseShapesFilled(false);
            renderer2.setBaseShapesVisible(true);
            renderer2.setDrawOutlines(true);
            renderer2.setLinesVisible(false);
            plot.setRenderer(1, renderer2);
        }


        JFreeChart chart = new JFreeChart(
        	"Cycle gaph",      // chart title
                plot                // data
//                PlotOrientation.VERTICAL, // orientation
//                false,                    // include legend
//                true,                     // tooltips
//                false                     // urls
        );
        chart.getTitle().setFont(fTitel);
        chart.setTitle("Cycle graph");
        
        chart.setBackgroundPaint(Setting.getColor("/bat/isotope/graph/background"));
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(true);
        axis.setLabelFont(fAxes);
        NumberAxis axis2 = (NumberAxis) plot.getDomainAxis();
        axis2.setAutoRangeIncludesZero(true);
        axis2.setLabelFont(fAxes);
	log.debug("GraphTime created");

        return chart;

	
    }
    
	/**
	 * 
	 */
	public void update(){
		dataSet = Func.getXY_cyc(cycList, Setting.getString("/bat/isotope/graph/cycle/serie1"), Setting.getString("/bat/isotope/graph/cycle/serie2"));
		dataSet2 = Func.getXY_cyc2(cycList, Setting.getString("/bat/isotope/graph/cycle/serie3"));
		dataSet3 = Func.getXY_act(cycList, "active");
	        chart = createChart(dataSet);
	        XYPlot plot = (XYPlot) chart.getPlot();
	        plot.setDataset(2, dataSet3);
	        
	        if(Setting.isotope.equalsIgnoreCase("C14")) {
	            NumberAxis axis2 = new NumberAxis(Setting.getString("/bat/isotope/graph/cycle/y_axes3"));
	            axis2.setRange(Setting.getDouble("/bat/isotope/graph/cycle/y_axes3/min"), Setting.getDouble("/bat/isotope/graph/cycle/y_axes3/max"));
	            axis2.setLabelPaint(Color.blue);
	            axis2.setTickLabelPaint(Color.blue);
	            plot.setRangeAxis(1, axis2);
	            plot.setDataset(1, dataSet2);
	            plot.mapDatasetToRangeAxis(1, 1);
	        }
	        this.removeAll();
	        this.add(new ChartPanel(chart));
		log.debug("Updated cycle graph");
	}

	/**
	 * @param cycList 
	 * 
	 */
	public void updateList(ArrayList<Cycle>  cycList){
	    this.cycList=cycList;
		dataSet = Func.getXY_cyc(cycList, Setting.getString("/bat/isotope/graph/cycle/serie1"), Setting.getString("/bat/isotope/graph/cycle/serie2"));
		dataSet2 = Func.getXY_cyc2(cycList, Setting.getString("/bat/isotope/graph/cycle/serie3"));
		dataSet3 = Func.getXY_act(cycList, "active");
	        chart = createChart(dataSet);
	        XYPlot plot = (XYPlot) chart.getPlot();
	        plot.setDataset(2, dataSet3);
	        
	        if(Setting.isotope.equalsIgnoreCase("C14")) {
	            NumberAxis axis2 = new NumberAxis(Setting.getString("/bat/isotope/graph/cycle/y_axes3"));
	            axis2.setRange(Setting.getDouble("/bat/isotope/graph/cycle/y_axes3/min"), Setting.getDouble("/bat/isotope/graph/cycle/y_axes3/max"));
	            axis2.setLabelPaint(Color.blue);
	            axis2.setTickLabelPaint(Color.blue);
	            plot.setRangeAxis(1, axis2);
	            plot.setDataset(1, dataSet2);
	            plot.mapDatasetToRangeAxis(1, 1);
	        }
	        this.removeAll();
	        this.add(new ChartPanel(chart));
		log.debug("Updated cycle graph");
	}




}