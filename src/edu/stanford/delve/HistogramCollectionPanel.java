/*
 * File          : HistogramCollectionPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 11 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
// Import custom classes
import edu.stanford.plot.HistogramPanel;
import edu.stanford.util.MathUtil;
import edu.stanford.util.OSPlatform;

/**
 * Panel that displays a collection of histograms. 
 */
public class HistogramCollectionPanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -1963177374338064334L;
    
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** Horizontal gap between the histograms */
    private static final int    HORIZONTAL_GAP        = 20;
    /** Vertical gap between the histograms */
    private static final int    VERTICAL_GAP          = OSPlatform.isWindows()?
                                                        12 : 18;
    /** The ratio width:height for each histogram */
    private static final double WIDTH_TO_HEIGHT_RATIO = OSPlatform.isWindows()?
                                                        1.6 : 1.7;
    
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The list with the histograms to add to the panel 
     */
    private final List<HistogramPanel> histograms;
    /**
     * The panel size
     */
    private final Dimension            size;
    /**
     * The background color
     */
    private final Color                backgroundColor;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a new panel to host histograms.<br>
     * 
     * The panel can host up to six histograms.
     * 
     * @param size The panel size
     * @param backgroundColor The background color
     */
    protected HistogramCollectionPanel(Dimension size,
                                       Color     backgroundColor) {
        histograms           = new ArrayList<HistogramPanel>();
        this.backgroundColor = backgroundColor;
        this.size            = size;
        setBackground(backgroundColor);
        setMaximumSize(size);
        
        setLayout(new GridLayout(3,              // number of rows
                                 2,              // number of columns
                                 HORIZONTAL_GAP, // horizontal gap
                                 VERTICAL_GAP)); // vertical gap
    }
    
    // -------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S   //
    // -------------------------------------------------------------------- //
    /**
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
     * @param seriesColors A map where the key is the title of the series and
     *                     the value is the color to use for that series or
     *                     {@code null} to use the default color
     * @param bins The number of bins (must be at least 1)
     * @param showValuesAboveBins {@code true} to show the value above each bin
     *                            (i.e., how many items fall into the bin) or
     *                            {@code false} to hide the text with the value 
     */
    protected void addHistogram(String                    title,
                                String                    horizontalAxisLabel,
                                String                    verticalAxisLabel,
                                String[]                  seriesTitles,
                                List<Number>[]            seriesValues,
                                Map<String, Color>        seriesColors,
                                int                       bins,
                                boolean                   showValuesAboveBins) {
        int height = (int)((size.getHeight() / 3) - (4 * VERTICAL_GAP));
        int width  = (int)MathUtil.round(height * WIDTH_TO_HEIGHT_RATIO, 0)
                                  .doubleValue();
        Dimension histogramSize = new Dimension(width, height);
        HistogramPanel histogram = new HistogramPanel(title,
                                                      horizontalAxisLabel,
                                                      verticalAxisLabel,
                                                      seriesTitles,
                                                      seriesValues,
                                                      bins,
                                                      showValuesAboveBins,
                                                      histogramSize);
        
        histogram.setBackground(backgroundColor);
        if (seriesColors != null) {
            histogram.setColor(seriesColors);
        }
        histograms.add(histogram);
        
        removeAll(); // Remove all histograms
        //add(Box.createVerticalStrut(VERTICAL_SPACER / 2));
        for (HistogramPanel histogramPanel : histograms) {
            add(histogramPanel);
            add(histogramPanel);
        }
    }
    
    /**
     * Removes all histograms from the panel.
     */
    protected void removeHistograms() {
        for (HistogramPanel histogram : histograms) {
            remove(histogram);
        }
    }
    
    /**
     * Removes all the components from the panel.
     */
    protected void clear() {
       removeAll();
       histograms.clear();
       validate();
    }
}