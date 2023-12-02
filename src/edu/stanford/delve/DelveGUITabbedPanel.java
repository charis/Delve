/*
 * File          : DelveGUITabbedPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 16 November 2020
 * Last Modified : 24 September 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentListener;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.gui.ColorTextPanel;
import edu.stanford.gui.ConsolePanel;
import edu.stanford.gui.ProgressPanel;
import edu.stanford.gui.ScrollableTextPanel;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.body.Method;
import edu.stanford.plot.Point2D;
import edu.stanford.studentcode.cyclomatic.MethodMetric;
import edu.stanford.util.MathUtil;
import edu.stanford.util.StatsUtil;
// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.delve.DelveGUI.WINDOW_HEIGHT;
import static edu.stanford.delve.Util.BREAK_TIME;
import static edu.stanford.gui.Constants.GREEN_COLOR;

/**
 * The Delve's main panel with the tabs.
 */
public class DelveGUITabbedPanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -3753695282681760300L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       C   O   N   S   T   A   N   T   S   //
    // --------------------------------------------------------------------- //
    /** The line number column background color */
    private static final Color LINE_COLUMN_COLOR = new Color(200, 210, 240);
    /** The text font for the text panel */
    private static final Font  TEXT_PANEL_FONT   = new Font("Courier New",
                                                            Font.PLAIN,
                                                            12);
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The component that lets the user switch between a the editor, the
     * histograms and the x-y plots  by clicking on the corresponding tab 
     */
    private final JTabbedPane              tabs;
    /**
     * The text area to display the source code, show submission stats etc.
     */
    private final ScrollableTextPanel      textPanel;
    /**
     * The panel with the console output/input
     */
    private final ConsolePanel             consolePanel;
    /**
     * The panel with the histograms
     */
    private final HistogramCollectionPanel histogramPanel;
    /**
     * The panel with the x-y plots
     */
    private final XYPlotCollectionPanel    xyPlotPanel;
    /** Module to visually display the progress of a task towards completion */
    private final ProgressPanel            progressPanel;
    /**
     * The background color
     */
    private final Color                    backgroundColor;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates Delve's main panel with the tabs.
     * 
     * @param size The size of the panel
     * @param backgroundColor The background color
     * @param documentListener The modules that receive notifications of text
     *                         changes or {@code null} to prevent sending
     *                         notifications on text changes
     * @param progressPanel Module to visually display the progress of a task
     *                      towards completion
     */
    protected DelveGUITabbedPanel(Dimension        size,
                                  Color            backgroundColor,
                                  DocumentListener documentListener,
                                  ProgressPanel    progressPanel) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(backgroundColor);
        
        this.backgroundColor = backgroundColor;
        this.progressPanel   = progressPanel;
        tabs = new JTabbedPane();
        tabs.setBackground(backgroundColor);
        // Tab 1: Text Panel
        textPanel = new ScrollableTextPanel(TEXT_PANEL_FONT, documentListener);
        textPanel.setEditable(false);
        textPanel.setLineDisplayBackground(LINE_COLUMN_COLOR);
        tabs.add("Editor", textPanel);
        
        // Tab 2: Histogram Panel
        histogramPanel =  new HistogramCollectionPanel(size, backgroundColor);
        tabs.add("All submissions", histogramPanel);
        
        // Tab 3: XY Plot Panel
        xyPlotPanel =  new XYPlotCollectionPanel(size, backgroundColor);
        tabs.add("Selected submission", xyPlotPanel);
        
        // Tab 4: The console panel
        consolePanel = new ConsolePanel(Color.BLACK,  // text color
                                        Color.WHITE , // background color
                                        false); // don't show std err in console
        tabs.add("Console", consolePanel);
        
        add(tabs);
    }
    
    // ------------------------------------------------------------------ //
    //  P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S  //
    // ------------------------------------------------------------------ //
    /**
     * Highlights the text lines in the specified range 
     * 
     * @param startLine The start line, inclusive (gets highlighted)
     * @param endLine The start line, exclusive (does not get highlighted)
     */
    public void highlightText(int startLine, int endLine) {
        textPanel.highlightText(startLine, endLine);
    }
    
    /**
     * Processes the submission folders and displays the overall results on
     * screen.
     * 
     * @param submissionRootDir The directory that contains the submission
     *                          subfolders
     * @param timeDiffLookup A map where the key is the submission folder and
     *                       the value is a map with the student snapshots as
     *                       keys and the times the student worked on each
     *                       snapshot as values
     * @param submittedFileLookup Lookup map where the key is a submission
     *                            folder and the value is the last parseable
     *                            java file under this folder
     * @param correctnessLookup  Map with the submission programs as keys and
     *                           their functionality correctness (i.e.,
     *                           {@code true} if functionally correct or
     *                           {@code false} otherwise)
     * @param recreateHistograms {@code true} to recreate the histograms about
     *                           all submissions or {@code false} to create them
     *                           only if they have not been created before
     *                 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void
       displayOverallInfo(File                            submissionRootDir,
                          Map<File, Map<String, Integer>> timeDiffLookup,
                          Map<File, JavaFile>             submittedFileLookup,
                          Map<File, Boolean>              correctnessLookup,
                          boolean                         recreateHistograms) {
        if (!recreateHistograms && histogramPanel.getComponentCount() > 0) {
            return;
        }
        histogramPanel.clear(); // Clear old content
        
        // Check if code is instrumented to validate functionality correctness
        boolean correctnessCheck = !correctnessLookup.isEmpty();
        Map<File, Boolean> dirCorrectnessLookup = null;
        if (correctnessCheck) {
            dirCorrectnessLookup = new HashMap<File, Boolean>();
            for (File sourceCodeFile : correctnessLookup.keySet()) {
                boolean isCorrect = correctnessLookup.get(sourceCodeFile);
                dirCorrectnessLookup.put(sourceCodeFile.getParentFile(),
                                         isCorrect);
            }
        }
        
        ColorTextPanel reportArea  = new ColorTextPanel();
        reportArea.setBackground(backgroundColor);
        
        StringBuilder report = new StringBuilder();
        String numOfSubmissionsReport = "Number of submissions: "
                                       + timeDiffLookup.size()
                                       + NEW_LINE + NEW_LINE;
        report.append(numOfSubmissionsReport);
        reportArea.appendText(numOfSubmissionsReport, Color.BLACK);
        
        String seriesOverall = "overall";
        String seriesCorrect = "correct";
       
        // ------------------------
        //     N e t   T i m e
        // ------------------------
        List<Integer> totalTimes = new ArrayList<Integer>();
        List<Integer> totalTimesCorrectPrograms = new ArrayList<Integer>();
        for (File submissionFolder : timeDiffLookup.keySet()) {
            Map<String, Integer> snapshotDiffLookup = 
                                         timeDiffLookup.get(submissionFolder);
            int totalTime = 0;
            for (String snapshotFilename : snapshotDiffLookup.keySet()) {
                int timeDiff = snapshotDiffLookup.get(snapshotFilename);
                if (timeDiff != Util.BREAK_TIME) {
                    totalTime += timeDiff;
                }
            }
            
            totalTimes.add(totalTime);
            if (correctnessCheck &&
                dirCorrectnessLookup.get(submissionFolder) == Boolean.TRUE) {
                totalTimesCorrectPrograms.add(totalTime);
            }
        }
        
        // Report
        int totalTimeAvg = (int)StatsUtil.getMean(totalTimes, 0);
        double totalTimeMedian = StatsUtil.getMedian(totalTimes);
        totalTimeMedian = MathUtil.round(totalTimeMedian, 0);
        StringBuilder timeReport = new StringBuilder();
        timeReport.append("Total net time [mean]  : "
                        + DelveGUIHelper.getTimeFromSeconds(totalTimeAvg)
                        + NEW_LINE);
        timeReport.append("Total net time [median]: "
                       + DelveGUIHelper.getTimeFromSeconds((int)totalTimeMedian)
                       + NEW_LINE);
        if (WINDOW_HEIGHT >= 1000) {
            report.append(NEW_LINE);
        }
        report.append(timeReport);
        reportArea.appendText(timeReport.toString(), Color.RED);
        
        // Histogram
        List<Number> totalTimeValues = new ArrayList<Number>(totalTimes.size());
        for (int i = 0; i < totalTimes.size(); i++) {
            totalTimeValues.add(totalTimes.get(i) / 3600.0);//Convert to hrs
        }
        
        String[] seriesTitles;
        List[]   seriesValues;
        Color[]  colors;
        boolean twoSeries = correctnessCheck &&
                            !totalTimesCorrectPrograms.isEmpty();
        if (twoSeries) {
            seriesTitles = new String[] {seriesCorrect, seriesOverall};
            List<Number> totalTimeValuesCorrectPrograms =
                        new ArrayList<Number>(totalTimesCorrectPrograms.size());
            for (int i = 0; i < totalTimesCorrectPrograms.size(); i++) {
                totalTimeValuesCorrectPrograms.add(
                     totalTimesCorrectPrograms.get(i) / 3600.0);//Convert to hrs
            }
            seriesValues = new List[] {totalTimeValuesCorrectPrograms,
                                       totalTimeValues};
            colors = new Color[] {GREEN_COLOR, Color.RED};
        }
        else {
            seriesTitles = new String[] {seriesOverall};
            seriesValues = new List[] {totalTimeValues};
            colors = new Color[] {Color.RED};
        }
        addHistogram(seriesTitles,
                     seriesValues,
                     "Net Time (hours)", // Horizontal axis label
                     colors);
        
        Set<JavaFile> javaFiles = new TreeSet<JavaFile>();
        Set<JavaFile> correctJavaFiles = new TreeSet<JavaFile>();
        for (File submissionFolder : submittedFileLookup.keySet()) {
            JavaFile lastSnapshot = submittedFileLookup.get(submissionFolder);
            javaFiles.add(lastSnapshot);
            if (correctnessCheck &&
                dirCorrectnessLookup.get(submissionFolder) == Boolean.TRUE) {
                correctJavaFiles.add(lastSnapshot);
            }
        }
        
        twoSeries = correctnessCheck && !correctJavaFiles.isEmpty();
        if (twoSeries) {
            seriesTitles = new String[] {seriesCorrect, seriesOverall};
            colors = new Color[] {GREEN_COLOR, Color.RED};
        }
        else {
            seriesTitles = new String[] {seriesOverall};
            colors = new Color[1];
        }
        
        // --------------------------------------
        //     N u m b e r   o f   L i n e s
        // --------------------------------------
        CodeAttr numOfLinesAttr = new CodeAttr(CodeAttr.Type.NUM_OF_LINES,
                                               javaFiles);
        // Report
        String numOfLinesReport = createReport(numOfLinesAttr);
        report.append(numOfLinesReport);
        reportArea.appendText(numOfLinesReport, Color.BLUE);
        // Histogram
        if (twoSeries) {
            seriesValues = new List[] {
                               new CodeAttr(CodeAttr.Type.NUM_OF_LINES,
                                            correctJavaFiles).getValues(),
                               numOfLinesAttr.getValues()
                           };
        }
        else {
            seriesValues = new List[] {numOfLinesAttr.getValues()};
            colors[0] = Color.BLUE;
        }
        addHistogram(seriesTitles,
                     seriesValues,
                     numOfLinesAttr.getName(), // Horizontal axis label
                     colors);
        
        // ------------------------------------------
        //     N u m b e r   o f   M e t h o d s
        // ------------------------------------------
        CodeAttr numOfMethodsAttr = new CodeAttr(CodeAttr.Type.NUM_OF_METHODS,
                                                 javaFiles);
        // Report
        String numOfMethodsReport = createReport(numOfMethodsAttr);
        report.append(numOfMethodsReport);
        reportArea.appendText(numOfMethodsReport, GREEN_COLOR);
        // Histogram
        if (twoSeries) {
            seriesValues = new List[] {
                               new CodeAttr(CodeAttr.Type.NUM_OF_METHODS,
                                            correctJavaFiles).getValues(),
                               numOfMethodsAttr.getValues()
                           };
        }
        else {
            seriesValues = new List[] {numOfMethodsAttr.getValues()};
            colors[0] = GREEN_COLOR;
        }
        addHistogram(seriesTitles,
                     seriesValues,
                     numOfMethodsAttr.getName(), // Horizontal axis label
                     colors);
        
        // -------------------------------------
        //     N u m b e r   o f   I v a r s
        // -------------------------------------
        CodeAttr numOfIvarsAttr = new CodeAttr(CodeAttr.Type.NUM_OF_IVARS,
                                               javaFiles);
        // Report
        String numOfIvarsReport = createReport(numOfIvarsAttr);
        report.append(numOfIvarsReport);
        reportArea.appendText(numOfIvarsReport, Color.MAGENTA);
        // Histogram
        if (twoSeries) {
            seriesValues = new List[] {
                               new CodeAttr(CodeAttr.Type.NUM_OF_IVARS,
                                            correctJavaFiles).getValues(),
                               numOfIvarsAttr.getValues()
                           };
        }
        else {
            seriesValues = new List[] {numOfIvarsAttr.getValues()};
            colors[0] = Color.MAGENTA;
        }
        addHistogram(seriesTitles,
                     seriesValues,
                     numOfIvarsAttr.getName(), // Horizontal axis label
                     colors);
        
        // -----------------------------------------------------
        //     N u m b e r   o f   C o m m e n t   C h a r s
        // -----------------------------------------------------
        CodeAttr numOfCommentCharsAttr =
                    new CodeAttr(CodeAttr.Type.NUM_OF_COMMENT_CHARS, javaFiles);
        // Report
        String numOfCommentCharsReport = createReport(numOfCommentCharsAttr);
        report.append(numOfCommentCharsReport);
        reportArea.appendText(numOfCommentCharsReport, Color.ORANGE);
        // Histogram
        if (twoSeries) {
            seriesValues = new List[] {
                               new CodeAttr(CodeAttr.Type.NUM_OF_COMMENT_CHARS,
                                            correctJavaFiles).getValues(),
                               numOfCommentCharsAttr.getValues()
                           };
        }
        else {
            seriesValues = new List[] {numOfCommentCharsAttr.getValues()};
            colors[0] = Color.ORANGE;
        }
        addHistogram(seriesTitles,
                     seriesValues,
                     numOfCommentCharsAttr.getName(), // Horizontal axis label
                     colors);
        
        // --------------------
        //     R e p o r t
        // --------------------
        histogramPanel.add(reportArea);
        
        histogramPanel.validate();
    }
    
    /**
     * Given a set with the snapshots for a particular student it plots a bunch
     * of metrics (lines, methods, ivars, comments etc.) over time.<br>
     * If there is no timestamp file, it uses the snapshot order (instead of
     * time) for the x-axis values.
     *
     * @param submissionDir The submission folder with the student snapshots
     * @param javaFiles The snapshots for a particular student
     * @param symbolsResolve {@code true} if there are no issues resolving
     *                       symbols or {@code false} otherwise
     * @param thread The thread that is running the task (i.e., calling this
     *               method) to check if it is interrupted or {@code null} if
     *               we don't want to interrupt the method execution no matter
     * @param cycThreshold The threshold for the cyclomatic complexity for a
     *                     complex method (i.e., method with value equal to or
     *                     greater than the threshold)
     * @param instrThreshold The threshold for the number of instructions for a
     *                       complex method (i.e., method with value equal to or
     *                       greater than the threshold)
     *               what
     * 
     * @return a map where the key is the snapshot file name in the submission
     *         and the value is a list with the complex methods in the file or
     *         an empty map if there are no complex methods or in case of an
     *         error detecting them
     */
    protected SortedMap<File, List<Method>>
                    plotMetricsOverTime(File                submissionDir,
                                        SortedSet<JavaFile> javaFiles,
                                        boolean             symbolsResolve,
                                        Thread              thread,
                                        int                 cycThreshold,
                                        int                 instrThreshold) {
        xyPlotPanel.clear(); // Clear old content
        
        int initProgressValue = progressPanel.getValue();
        double percent = (100 - initProgressValue) / (double)javaFiles.size();
        
        Map<Integer, Integer> timesDiffLookup = null;
        try {
        	timesDiffLookup = Util.getTimeDiffLookupByOrder(submissionDir);
        }
        catch (ErrorException ee) {
        }
        
        boolean useTime = timesDiffLookup != null && !timesDiffLookup.isEmpty();
        
        // Map where each (key, value) entry is (x-value, y-value) to plot.
        // The x-value (i.e., map key) is either the time spent on the
        // assignment at the point the snapshot is taken if the timestamps are
        // known or the snapshot order if the timestamps are unknown.
        // The y-value (i.e., map value) is the volume of comments in this
        // snapshot expressed in characters.
        SortedMap<Integer,Integer> xyLines     = new TreeMap<Integer,Integer>();
        SortedMap<Integer,Integer> xyCodeLines = new TreeMap<Integer,Integer>();
        SortedMap<Integer,Integer> xyMethods   = new TreeMap<Integer,Integer>();
        SortedMap<Integer,Integer> xyComments  = new TreeMap<Integer,Integer>();
        SortedMap<Integer,Integer> xyIvars     = new TreeMap<Integer,Integer>();
        SortedMap<Integer,Integer> xyMethodCalls 
                                               = new TreeMap<Integer,Integer>();
        SortedMap<Integer,Double>  xyCYCAvg    = new TreeMap<Integer,Double>();
        SortedMap<Integer,Integer> xyCYCMax    = new TreeMap<Integer,Integer>();
        SortedMap<File, List<Method>> complexMethodMap =
                                               new TreeMap<File,List<Method>>();
        int totalTimeSoFar = 0;
        int lastProcessedOrder = 1; // The last snapshot order processed so far
        int count = 0;
        for (JavaFile javaFile : javaFiles) {// The files are sorted by filename
            SnapshotMetrics snapshotMetrics = new SnapshotMetrics(javaFile);
            
            int snapshotOrder        = snapshotMetrics.getSnapshotOrder();
            int numOfLines           = snapshotMetrics.getNumOfLines();
            int numOfCodeLines       = snapshotMetrics.getNumOfCodeLines();
            int numOfMethods         = snapshotMetrics.getNumOfMethods();
            int commentVolumeInChars = snapshotMetrics.getCommentVolume();
            int numOfIvars           = snapshotMetrics.getNumOfIvars();
            int numOfMethodCalls     = snapshotMetrics.getNumOfMethodCalls();
            Double  cycAvg           = snapshotMetrics.getCYCAvg();
            Integer cycMax           = snapshotMetrics.getCYCMax();
            
            if (useTime) {
                int temp = snapshotOrder;
                while (temp > lastProcessedOrder) {
                    int timeDiff = timesDiffLookup.get(temp);
                    // Skip over snapshots that are taken after a long break
                    if (timeDiff != BREAK_TIME) {
                        totalTimeSoFar += timeDiff;
                    }
                    temp--;
                }
                lastProcessedOrder = snapshotOrder; // Next for-loop iteration
                
                xyLines.put(totalTimeSoFar, numOfLines);
                xyCodeLines.put(totalTimeSoFar, numOfCodeLines);
                xyMethods.put(totalTimeSoFar, numOfMethods);
                xyComments.put(totalTimeSoFar, commentVolumeInChars);
                xyIvars.put(totalTimeSoFar, numOfIvars);
                if (numOfMethodCalls > 0) { // If 0 => symbol resolution error
                    xyMethodCalls.put(totalTimeSoFar, numOfMethodCalls);
                }
                if (cycAvg != null) { // In this case cycMax is also non-null
                    xyCYCAvg.put(totalTimeSoFar, cycAvg);
                    xyCYCMax.put(totalTimeSoFar, cycMax);
                }
            }
            else { // Use snapshot order for x-values
                xyLines.put(snapshotOrder, numOfLines);
                xyCodeLines.put(snapshotOrder, numOfCodeLines);
                xyMethods.put(snapshotOrder, numOfMethods);
                xyComments.put(snapshotOrder, commentVolumeInChars);
                xyIvars.put(snapshotOrder, numOfIvars);
                if (numOfMethodCalls > 0) { // If 0 => symbol resolution error
                    xyMethodCalls.put(snapshotOrder, numOfMethodCalls);
                }
                if (cycAvg != null) { // In this case cycMax is also non-null
                    xyCYCAvg.put(snapshotOrder, cycAvg);
                    xyCYCMax.put(snapshotOrder, cycMax);
                }
            }
            
            List<Method> complexMethods =
                        getComplexMethods(javaFile,
                                          snapshotMetrics.getMethodCYCMetrics(),
                                          cycThreshold,
                                          instrThreshold);
            complexMethodMap.put(new File(javaFile.getFilePathname()),
                                 complexMethods);
            
            int progress = (int)Math.round(initProgressValue + count * percent);
            progressPanel.setValue(progress);
            count++;
            
            // The thread is interrupted; stop the task and return
            if (thread != null && thread.isInterrupted()) {
                progressPanel.reset(false);
                return complexMethodMap;
            }
        }
        
        String seriesTitle = submissionDir.getName();
        // Plot the number of lines over time
        plotOverTime(xyLines,
                     seriesTitle,
                     "lines = f(time)",
                     "number of lines", // yAxisLabel
                     Color.RED,
                     useTime);
        
        // Plot the number of pure code lines over time
        plotOverTime(xyCodeLines,
                     seriesTitle,
                     "pure code lines = f(time)",
                     "number of pure code lines", // yAxisLabel
                     Color.BLUE,
                     useTime);
        
        // Plot the number of methods over time
        plotOverTime(xyMethods,
                     seriesTitle,
                     "methods = f(time)",
                     "number of methods", // yAxisLabel
                     GREEN_COLOR,
                     useTime);
        
        // Plot the number of comments (expressed in chars) over time
        plotOverTime(xyComments,
                     seriesTitle,
                     "comments = f(time)",
                     "comments (in characters)", // yAxisLabel
                     Color.MAGENTA,
                     useTime);
        
        // Plot the number of instance variables over time
        plotOverTime(xyIvars,
                     seriesTitle,
                     "ivars = f(time)",
                     "number of instance variables", // yAxisLabel
                     Color.ORANGE,
                     useTime);
        
        // Plot the number of method calls over time
        if (symbolsResolve) {
            plotOverTime(xyMethodCalls,
                         seriesTitle,
                         "reusable methods = f(time)",
                         "number of internal method calls", // yAxisLabel
                         Color.GRAY,
                         useTime);
        }
        
        // Plot the average and max cyclomatic complexity over time
        if (!xyCYCAvg.isEmpty() && !xyCYCMax.isEmpty()) {
            plotOverTime(xyCYCAvg,
                         seriesTitle,
                         "avg cyclomatic complexity = f(time)",
                         "avg cyclomatic complexity", // yAxisLabel
                         Color.ORANGE,
                         useTime);
            
            plotOverTime(xyCYCMax,
                         seriesTitle,
                         "max cyclomatic complexity = f(time)",
                         "max cyclomatic complexity", // yAxisLabel
                         Color.ORANGE,
                         useTime);
        }
        
        showCurrSubmissionPlots();
        
        return complexMethodMap;
    }
    
    /**
     * Given a a Java file and a list with the cyclomatic complexity metrics for
     * its methods, it returns a list with the methods that have high complexity
     * or an empty list if there are no high values or if the provided list is
     * {@code null}.
     * 
     * @param javaFile The file 
     * @param methodMetrics List with the method cyclomatic complexity metrics
     * @param cycThreshold The threshold for the cyclomatic complexity for a
     *                     complex method (i.e., method with value equal to or
     *                     greater than the threshold)
     * @param instrThreshold The threshold for the number of instructions for a
     *                       complex method (i.e., method with value equal to or
     *                       greater than the threshold)
     * 
     * @return a list with the methods that have high complexity or an empty
     *         list if there are no high values or if the provided list is
     *         {@code null}
     */
    private List<Method> getComplexMethods(JavaFile           javaFile,
                                           List<MethodMetric> methodMetrics,
                                           int                cycThreshold,
                                           int                instrThreshold){
        List<Method> complexMethods = new ArrayList<Method>();
        
        List<Method> methods = javaFile.getMethods();
        if (methodMetrics != null) {
            for (MethodMetric methodMetric : methodMetrics) {
                if (methodMetric.getCyclomaticComplexity() >= cycThreshold &&
                    methodMetric.getNumOfInstructions() >= instrThreshold) {
                    for (Method method : methods) {
                        if (method.getName().equals(
                                                methodMetric.getMethodName())) {
                            complexMethods.add(method);
                        }
                    }
                }
            }
        }
        
        return complexMethods;
    }
    
    /**
     * Adds a plot that displays how a given source code characteristic changes
     * over time.
     * 
     * @param xyMapping The x-y mapping where x is the time (or the snapshot
     *                  order) and y is the measured characteristic
     * @param seriesTitle The title of the series 
     * @param plotTitle The plot title or {@code null} for no title
     * @param yAxisLabel The label of the measured characteristic
     * @param color The color to use for the plot
     * @param useTime {@code true} if the x in the x-y mapping represents time
     *                or {@code false} if it represents snapshot order
     */
    private void plotOverTime(SortedMap<Integer, ? extends Number> xyMapping,
                              String                               seriesTitle,
                              String                               plotTitle,
                              String                               yAxisLabel,
                              Color                                color,
                              boolean                              useTime) {
        String xAxisLabel =  useTime? "time (in hrs)" : "file order";
        List<Point2D> values = new ArrayList<Point2D>();
        Iterator<Integer> itr = xyMapping.keySet().iterator();
        double lastY = -1; // Pick a value to overwrite lastY in first iteration
        double lastX = -1;
        while (itr.hasNext()) {
            Integer key = itr.next();
            double y = xyMapping.get(key).doubleValue();
            double x;
            if (useTime) { // Convert the seconds to hours
                x = key / 3600.0;
            }
            else {
                x = key; // Snapshot order
            }
            if (y != lastY  || !itr.hasNext()) {
                Point2D lastPoint = new Point2D(lastX, lastY);
                if (lastX != - 1 && !values.contains(lastPoint)) {
                    values.add(lastPoint);
                }
                lastY = y;
                values.add(new Point2D(x, y));
            }
            lastX = x;
        }
        
        addXYPlot(values.toArray(new Point2D[0]),
                  seriesTitle,
                  plotTitle,
                  xAxisLabel,
                  yAxisLabel,
                  color,
                  false); // splineInterpolation
    }
    
    /**
     * Sets the text in the text editor.
     *  
     * @param text The new text to be set
     * @param colorText {@code true} to color the text or {@code false} to use
     *                  black color only
     */
    protected void setText(String  text,
                           boolean colorText) {
        textPanel.setText(text, colorText);
    }
    
    /**
     * @return the text in the text editor
     */
    protected String getText() {
        return textPanel.getText();
    }
    
    /**
     * Removes all content from the all tabs.
     */
    protected void clear() {
        textPanel.setText(null, false);
        histogramPanel.clear();
        consolePanel.clearText();
        xyPlotPanel.clear();
    }
    
    /**
     * Shows the tab with the text editor.
     */
    protected void showTextEditor() {
        tabs.setSelectedComponent(textPanel);
    }
    
    /**
     * Sets the specified boolean flag to indicate whether or not the text area
     * is editable.
     * 
     * @param flag {@code true} to make the text area editable or {@code false}
     *             to make it non-editable
     */
    
    protected void setTextEditorEditable(boolean flag) {
        textPanel.setEditable(flag);
    }
    
    /**
     * Shows the tab with the histograms for all submissions.
     */
    protected void showAllSubmissionsHistograms() {
        tabs.setSelectedComponent(histogramPanel);
    }
    
    /**
     * Shows the tab with the plots for the selected submission.
     */
    protected void showCurrSubmissionPlots() {
        tabs.setSelectedComponent(xyPlotPanel);
    }
    
    /**
     * Shows the tab with the console text.
     */
    protected void showConsole() {
        tabs.setSelectedComponent(consolePanel);
    }
    
    /**
     * Removes the plots for the selected submission.
     */
    protected void clearCurrSubmissionPlots() {
        xyPlotPanel.clear();
    }
    
    /**
     * Clears the console text.
     */
    public void clearConsole() {
        consolePanel.clearText();
    }
    
    /**
     * Sets the console text to the provided error text.<br>
     * The existing console text is cleared.<br>
     * It the provided error text is {@code null} or empty, it has no effect
     * (as if this method was never called).
     * 
     * @param text The error text to display in the console
     */
    protected void setErrorText(String text) {
        consolePanel.setErrorText(text);
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Creates a histogram for the provided values and adds it to the panel with
     * the histograms.
     * 
     * @param seriesTitles Array for the series to add to the histogram where 
     *                     each element accounts for the title of the given
     *                     series
     * @param seriesValues Array for the series to add to the histogram where 
     *                     each element accounts for the values of the given
     *                     series
     * @param horizontalAxisLabel The horizontal axis label (i.e., label for
     *                            the values)
     * @param colors The colors to use for the histogram series
     */
    private void addHistogram(String[]       seriesTitles,
                              List<Number>[] seriesValues,
                              String         horizontalAxisLabel,
                              Color[]        colors) {
        Map<String, Color> seriesColors = new HashMap<String, Color>();
        for (int i = 0; i < seriesTitles.length; i++) {
            seriesColors.put(seriesTitles[i], colors[i]);
        }
        // For the bins take into account the series with the max values
        int maxNumOfValues = 0;
        for (List<Number> currListOfValues : seriesValues) {
            if (currListOfValues.size() > maxNumOfValues) {
                maxNumOfValues = currListOfValues.size();
            }
        }
        int bins = maxNumOfValues > 20 ? 20 : maxNumOfValues / 2;
        histogramPanel.addHistogram(null,                  // no title
                                    horizontalAxisLabel,
                                    "Number of students", // verticalAxisLabel
                                    seriesTitles,
                                    seriesValues, 
                                    seriesColors,
                                    bins,
                                    true);                // showValuesAboveBins
    }
    
    /**
     * Creates a x-y plot for the provided values and adds it to the panel with
     * the x-y plots.
     * 
     * @param values The values used for the x-y plot
     * @param seriesTitle The title for the x-y plot series
     * @param plotTitle The plot title or {@code null} for no title
     * @param xAxisLabel The label for the horizontal axis or {@code null} for
     *                   no label
     * @param yAxisLabel The label for the vertical axis or {@code null} for no
     *                   label
     * @param color The color to use for the x-y plot
     * @param splineInterpolation {@code true} for spline interpolation or
     *                            {@code false} otherwise
     */
    private void addXYPlot(Point2D[] values,
                           String    seriesTitle,
                           String    plotTitle,
                           String    xAxisLabel,
                           String    yAxisLabel,
                           Color     color,
                           boolean   splineInterpolation) {
        Map<String, Point2D[]> series = new HashMap<String, Point2D[]>();
        series.put(seriesTitle, values);
        Map<String, Color> seriesColors = new HashMap<String, Color>();
        seriesColors.put(seriesTitle, color);
        xyPlotPanel.addXYPlot(plotTitle,
                              xAxisLabel,
                              yAxisLabel,
                              series,
                              seriesColors,
                              splineInterpolation);
    }
    
    /**
     * Creates a report for the given code attribute.
     * 
     * @param codeAttr The code attribute to get the report for
     * 
     * @return a report for the given code attribute
     */
    private String createReport(CodeAttr codeAttr) {
        StringBuilder report = new StringBuilder();
        report.append(codeAttr.getName() + " [mean]  : "
                    + codeAttr.getMeanValue() + NEW_LINE);
        report.append(codeAttr.getName() + " [median]: "
                    + codeAttr.getMedianValue() + NEW_LINE);
        if (WINDOW_HEIGHT >= 1000) {
            report.append(NEW_LINE);
        }
        
        return report.toString();
    }
}
