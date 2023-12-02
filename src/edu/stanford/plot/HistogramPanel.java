/*
 * File          : HistogramPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 11 November 2020
 * Last Modified : 27 December 2020
 */
package edu.stanford.plot;

// Import Java SE classes
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
// Import JFree classes 
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYDataset;

/**
 * Panel with a histogram plot
 */
public class HistogramPanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -9174576202526715866L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The histogram
     */
    private final JFreeChart histogram;
    /**
     * The titles for the series on the histogram
     */
    private final String[]   seriesTitles;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a HistogramPanel.<br>
     * Creates a histogram with the given series and adds it to a panel.
     * 
     * @param title The chart title or {@code null} for no title
     * @param horizontalAxisLabel The label for the horizontal axis or
     *                            {@code null} for no label
     * @param verticalAxisLabel The label for the vertical axis or {@code null}
     *                          for no label
     * @param seriesTitles Array for the series to add to the histogram where 
     *                     each element accounts for the title of the given
     *                     series
     * @param seriesValues Array for the series to add to the histogram where 
     *                     each element accounts for the values of the given
     *                     series
     * @param bins The number of bins (must be at least 1).
     * @param showValuesAboveBins {@code true} to show the value above each bin
     *                            (i.e., how many items fall into the bin) or
     *                            {@code false} to hide the text with the value
     * @param size The size for this histogram panel or {@code null} for the
     *             default size
     */
    public HistogramPanel(String         title,
                          String         horizontalAxisLabel,
                          String         verticalAxisLabel,
                          String[]       seriesTitles,
                          List<Number>[] seriesValues,
                          int            bins,
                          boolean        showValuesAboveBins,
                          Dimension      size) {
        HistogramDataset dataset = setSeries(seriesTitles,
                                             seriesValues,
                                             bins);
        histogram = ChartFactory.createHistogram(title,
                                                 horizontalAxisLabel,
                                                 verticalAxisLabel,
                                                 dataset);
        // If there is only one series, remove the legend
        if (seriesTitles.length == 1) {
            histogram.removeLegend();
        }
        // Show values on the bars
        XYPlot plot = histogram.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        if (showValuesAboveBins) {
            XYItemLabelGenerator itemLabelGenerator =
                                          new StandardXYItemLabelGenerator();
            renderer.setDefaultItemLabelGenerator(itemLabelGenerator);
            renderer.setDefaultItemLabelsVisible(true);
        }
        this.seriesTitles = seriesTitles;
        
        ChartPanel chartPanel = new ChartPanel(histogram, false);
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
        histogram.setTitle(title);
    }
    
    /**
     * @return the chart title or {@code null} if the chart has no title
     */
    public String getTitle() {
        return histogram.getTitle().getText();
    }
    
    /**
     * Sets the label for the horizontal axis.
     * 
     * @param horizontalAxisLabel The label for the horizontal axis or
     *                            {@code null} for no label
     */
    public void setHorizontalAxisLabel(String horizontalAxisLabel) {
        histogram.getXYPlot().getRangeAxis().setLabel(horizontalAxisLabel);
    }
    
    /**
     * @return the label for the horizontal axis or {@code null} if the chart
     *         has no horizontal axis label
     */
    public String getHorizontalAxisLabel() {
        return histogram.getXYPlot().getRangeAxis().getLabel();
    }
    
    /**
     * Sets the label for the vertical axis.
     * 
     * @param verticalAxisLabel The label for the vertical axis or {@code null}
     *                          for no label
     */
    public void setVerticalAxisLabel(String verticalAxisLabel) {
        histogram.getXYPlot().getDomainAxis().setLabel(verticalAxisLabel);
    }
    
    /**
     * @return the label for the vertical axis or {@code null} if the chart
     *         has no vertical axis label
     */
    public String getVerticalAxisLabel() {
        return histogram.getXYPlot().getDomainAxis().getLabel();
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
        XYPlot plot = histogram.getXYPlot();
        XYDataset dataset = plot.getDataset();
        XYItemRenderer renderer = plot.getRendererForDataset(dataset);
        
        int seriesCount = seriesTitles.length;
        for (String seriesTitle : seriesTitles) {
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
     * Adds the given series added to the histogram and returns the generated
     * dataset.<br>
     * The series is provided as a map where the key is the title of the series
     * and the value is the numeric values for that series.
     * 
     * @param seriesTitles Array for the series to add to the histogram where 
     *                     each element accounts for the title of the given
     *                     series
     * @param seriesValues Array for the series to add to the histogram where 
     *                     each element accounts for the values of the given
     *                     series
     * @param bins The number of bins (must be at least 1)
     * 
     * @return the histogram dataset that is generated from the provided series 
     */
    private HistogramDataset setSeries(String[]       seriesTitles,
                                       List<Number>[] seriesValues,
                                       int            bins) {
        HistogramDataset dataset = new HistogramDataset();
        if (seriesTitles == null || seriesTitles.length == 0) {
            throw new RuntimeException("Argument 'seriesTitles' is either null "
                                     + "or a zero-size array");
        }
        if (seriesValues == null || seriesValues.length == 0) {
            throw new RuntimeException("Argument 'seriesValues' is either null "
                                     + "or a zero-size array");
        }
        if (seriesTitles.length != seriesValues.length) {
            throw new RuntimeException("'seriesTitles' and 'seriesValues' must "
                                     + "have the same number of elements");
        }
        
        double[][] values = new double[seriesTitles.length][];
        double overallMin = Double.MAX_VALUE;
        double overallMax = -Double.MAX_VALUE;
        for (int i = 0; i < seriesTitles.length; i++) {
            values[i] = new double[seriesValues[i].size()];
            int index = 0;
            for (Number value : seriesValues[i]) {
                double val = value.doubleValue();
                values[i][index] = val;
                index++;
                
                if (val < overallMin) {
                   overallMin = val;
                }
                else if (val > overallMax) {
                    overallMax = val;
                }
            }
        }
        for (int i = 0; i < seriesTitles.length; i++) {
            dataset.addSeries(seriesTitles[i],
                              values[i],
                              bins,
                              overallMin,
                              overallMax);
        }
        
        return dataset;
    }
}
