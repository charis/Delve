/*
 * File          : XYPlotCollectionPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 16 November 2020
 * Last Modified : 19 September 2021
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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
// Import custom classes
import edu.stanford.plot.Point2D;
import edu.stanford.plot.XYPlotPanel;
import edu.stanford.util.MathUtil;
import edu.stanford.util.OSPlatform;

/**
 * Panel that displays a collection of x-y plots. 
 */
public class XYPlotCollectionPanel extends JScrollPane
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -5775118639527761412L;
    
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** Horizontal gap between the x-y plots */
    private static final int    HORIZONTAL_GAP        = 20;
    /** Vertical gap between the x-y plots */
    private static final int    VERTICAL_GAP          = OSPlatform.isWindows()?
                                                        10 : 15;
    /** The ratio width:height for each x-y plot */
    private static final double WIDTH_TO_HEIGHT_RATIO = 1.65;
    
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The list with the x-y plots to add to the panel 
     */
    private final List<XYPlotPanel> xyPlots;
    /**
     * The main panel with the plots. The scroll panel is a wrapper to the main
     * panel.
     */
    private final JPanel            plotPanel;
    /**
     * The panel size
     */
    private final Dimension         size;
    /**
     * The background color
     */
    private final Color             backgroundColor;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a new panel to host x-y plots.<br>
     * 
     * The panel can host up to six x-y plots.
     * 
     * @param size The panel size
     * @param backgroundColor The background color
     */
    protected XYPlotCollectionPanel(Dimension size,
                                    Color     backgroundColor) {
        xyPlots              = new ArrayList<XYPlotPanel>();
        
        this.backgroundColor = backgroundColor;
        this.size            = size;
        setBackground(backgroundColor);
        
        setHorizontalScrollBarPolicy(
                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(
                           ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        plotPanel = new JPanel();
        plotPanel.setBackground(backgroundColor);
        plotPanel.setLayout(new GridLayout(4,              // number of rows
                                           2,              // number of columns
                                           HORIZONTAL_GAP, // horizontal gap
                                           VERTICAL_GAP)); // vertical gap
        setViewportView(plotPanel);
    }
    
    // -------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S   //
    // -------------------------------------------------------------------- //
    /**
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
     * @param seriesColors A map where the key is the title of the series and
     *                     the value is the color to use for that series or
     *                     {@code null} to use the default color
     * @param splineInterpolation {@code true} for spline interpolation or
     *                            {@code false} otherwise    */
    protected void addXYPlot(String                 title,
                             String                 xAxisLabel,
                             String                 yAxisLabel,
                             Map<String, Point2D[]> series,
                             Map<String, Color>     seriesColors,
                             boolean                splineInterpolation) {
        int width  = size.width / 2 - 2 * HORIZONTAL_GAP;
        int height = (int)MathUtil.round(width / WIDTH_TO_HEIGHT_RATIO, 0)
                                  .doubleValue();
        Dimension histogramSize = new Dimension(width, height);
        
        XYPlotPanel xyPlot = new XYPlotPanel(title,
                                             xAxisLabel,
                                             yAxisLabel,
                                             series,
                                             histogramSize,
                                             splineInterpolation);
        
        xyPlot.setBackground(backgroundColor);
        if (seriesColors != null) {
            xyPlot.setColor(seriesColors);
        }
        xyPlots.add(xyPlot);
        
        plotPanel.removeAll(); // Remove all x-y plots
        for (XYPlotPanel currXYPlot : xyPlots) {
            plotPanel.add(currXYPlot);
            plotPanel.add(currXYPlot);
        }
    }
    
    /**
     * Removes all the components from the panel.
     */
    protected void clear() {
        plotPanel.removeAll();
        xyPlots.clear();
        validate();
    }
}