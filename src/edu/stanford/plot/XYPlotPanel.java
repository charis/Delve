/*
 * File          : XYPlotPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 16 November 2020
 * Last Modified : 16 November 2020
 */
package edu.stanford.plot;

// Import Java SE classes
import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;
import javax.swing.JPanel;
// Import JFree classes 
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Panel with a x-y plot
 */
public class XYPlotPanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -150846876098931123L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The x-y plot
     */
    private final JFreeChart             xyPlot;
    /**
     * The series to add to the x-y plot as a map where the key is the title of
     * the series and the value is the numbers for that series
     */
    private final Map<String, Point2D[]> series;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a XYPlotPanel.<br>
     * Creates a x-y plot with the given series and adds it to a panel.
     * 
     * @param title The chart title or {@code null} for no title
     * @param xAxisLabel The label for the horizontal axis or {@code null} for
     *                   no label
     * @param yAxisLabel The label for the vertical axis or {@code null} for no
     *                   label
     * @param series The series to add to the x-y plot as a map where the key is
     *               the title of the series and the value is the numbers for
     *               that series
     * @param size The size for this x-y plot panel or {@code null} for the
     *             default size
     * @param splineInterpolation {@code true} for spline interpolation or
     *                            {@code false} otherwise
     */
    public XYPlotPanel(String                 title,
                       String                 xAxisLabel,
                       String                 yAxisLabel,
                       Map<String, Point2D[]> series,
                       Dimension              size,
                       boolean                splineInterpolation) {
       
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        yAxis.setAutoRangeIncludesZero(false);
        
        XYItemRenderer renderer = splineInterpolation?
                                  new XYSplineRenderer():
                                  new XYLineAndShapeRenderer();
        XYDataset dataset = setSeries(series);
        XYPlot plot = new XYPlot(dataset,
                                 xAxis,
                                 yAxis,
                                 renderer);
        // Add insets
        plot.setAxisOffset(new RectangleInsets(4.0, 4.0, 4.0, 4.0));
        
        boolean createLegend = series.size() > 1;
        xyPlot = new JFreeChart(title,
                                JFreeChart.DEFAULT_TITLE_FONT,
                                plot,
                                createLegend);
        this.series = series;
        
        // Show values on the bars
        ChartPanel chartPanel = new ChartPanel(xyPlot, false);
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
        if (size != null) {
           chartPanel.setPreferredSize(size);
        }
        
        add(chartPanel);
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Sets the chart title.
     * 
     * @param title The chart title or {@code null} if the chart no title
     */
    public void setTitle(String title) {
        xyPlot.setTitle(title);
    }
    
    /**
     * @return the chart title or {@code null} if the chart has no title
     */
    public String getTitle() {
        return xyPlot.getTitle().getText();
    }
    
    /**
     * Sets the label for the horizontal axis.
     * 
     * @param horizontalAxisLabel The label for the horizontal axis or
     *                            {@code null} for no label
     */
    public void setHorizontalAxisLabel(String horizontalAxisLabel) {
        xyPlot.getXYPlot().getRangeAxis().setLabel(horizontalAxisLabel);
    }
    
    /**
     * @return the label for the horizontal axis or {@code null} if the chart
     *         has no horizontal axis label
     */
    public String getHorizontalAxisLabel() {
        return xyPlot.getXYPlot().getRangeAxis().getLabel();
    }
    
    /**
     * Sets the label for the vertical axis.
     * 
     * @param verticalAxisLabel The label for the vertical axis or {@code null}
     *                          for no label
     */
    public void setVerticalAxisLabel(String verticalAxisLabel) {
        xyPlot.getXYPlot().getDomainAxis().setLabel(verticalAxisLabel);
    }
    
    /**
     * @return the label for the vertical axis or {@code null} if the chart
     *         has no vertical axis label
     */
    public String getVerticalAxisLabel() {
        return xyPlot.getXYPlot().getDomainAxis().getLabel();
    }
    
    /**
     * Sets the colors to use for the provided series.<br>
     * This is done via a map where the key is the title of the series and the
     * value is the color to use for that series. If a series is missing from
     * the map, its color won't change (will be the default that was chosen).
     * 
     * @param seriesColors A map where the key is the title of the series and
     *                     the value is the color to use for that series
     */
    public void setColor(Map<String, Color> seriesColors) {
        XYPlot plot = xyPlot.getXYPlot();
        XYDataset dataset = plot.getDataset();
        XYItemRenderer renderer = plot.getRendererForDataset(dataset);
        
        int seriesCount = series.size();
        for (String seriesTitle : series.keySet()) {
            // Find the color to use for this series (if specified)
            Color seriesColor = seriesColors.get(seriesTitle);
            if (seriesColor != null) { // Found
                // Find the series index
                int index = 0;
                while (index < seriesCount) {
                    String currTitle = (String)dataset.getSeriesKey(index);
                    if (currTitle.equals(seriesTitle)) {
                        break;
                    }
                    index++;
                }
                
                // Set the color now
                renderer.setSeriesPaint(index, seriesColor);
            }
        }
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Adds the given series added to the x-y plot and returns the generated
     * dataset.<br>
     * The series is provided as a map where the key is the title of the series
     * and the value is the numeric values for that series.
     * 
     * @param series A map where the key is the title of the series and the
     *               value is the x-y pairs (i.e., 2D points) for that series
     * 
     * @return the x-y dataset that is generated from the provided series 
     */
    private XYDataset setSeries(Map<String, Point2D[]> series) {
        XYSeriesCollection result = new XYSeriesCollection();
        
        if (series == null || series.isEmpty()) {
            throw new RuntimeException("Argument 'series' is either null or an "
                                     + "empty map");
        }
        
        if (series != null) {
            for (String seriesTitle : series.keySet()) {
                XYSeries xySeries = new XYSeries(seriesTitle);
                Point2D[] values = series.get(seriesTitle);
                for (Point2D value : values) {
                    xySeries.add(value.getX(), value.getY());
                }
                result.addSeries(xySeries);
            }
        }
        
        return (XYDataset)result;
    }
}
