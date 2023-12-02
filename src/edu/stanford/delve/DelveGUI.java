/*
 * File          : DelveGUI.java
 * Author        : Charis Charitsis
 * Creation Date : 3 November 2020
 * Last Modified : 24 September 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.gui.BrowsePanel;
import edu.stanford.gui.OnOffSwitchPanel;
import edu.stanford.gui.ProgressPanel;
import edu.stanford.java.compile.Compiler;
import edu.stanford.java.execution.InstrumentationConfig;
import edu.stanford.java.execution.ProgramExecutor;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.body.Method;
import edu.stanford.studentcode.cyclomatic.MethodMetric;
import edu.stanford.util.OSPlatform;
import edu.stanford.util.StatsUtil;
import edu.stanford.util.UIUtil;
import edu.stanford.util.filesystem.DirectoryUtil;
import edu.stanford.util.io.FileIOUtil;
// Import constants
import static edu.stanford.constants.Constants.JAR_FILE_EXTENSION;
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Constants.LOAD_IMAGES_FROM_CLASSPATH;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.delve.Constants.COMPILE_DEST_DIR;
import static edu.stanford.delve.Constants.DEFAULT_CYC_THRESHOLD;
import static edu.stanford.delve.Constants.DEFAULT_INSTUCTION_THRESHOLD;
import static edu.stanford.delve.Constants.DELVE_IMAGE_DIR;
import static edu.stanford.delve.Constants.EXTRA_FILES_DIR;
import static edu.stanford.delve.Constants.IMAGE_DIR;
import static edu.stanford.delve.Constants.INSTRUMENTATION_EXAMPLES;
import static edu.stanford.delve.Constants.INSTRUMENTATION_GUIDELINES;
import static edu.stanford.delve.Constants.TIMEOUT_LOOKUP;
import static edu.stanford.delve.Constants.TIMEOUT_OPTIONS;
import static edu.stanford.gui.Constants.IMAGE_PACKGAGE;
import static edu.stanford.gui.Constants.JCOMPONENT_HEIGHT;

/**
 * User interface for Delve 
 */
public class DelveGUI extends JFrame
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -5707156809453721497L;
    
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** The screen size */
    protected static final Dimension SCREEN_SIZE   = Toolkit.getDefaultToolkit()
                                                            .getScreenSize();
    /** The GUI width in pixels */
    protected static final int       WINDOW_WIDTH  = SCREEN_SIZE.width >= 1300?
                                                     1300 : SCREEN_SIZE.width;
    /** The GUI width in pixels */
    protected static final int       WINDOW_HEIGHT = SCREEN_SIZE.height >= 1035?
                                                     1000 :
                                                     SCREEN_SIZE.height - 35;
    /**
     * {@code true} if Windows OS or {@code false} otherwise
     */
    private static final boolean     IS_WINDOWS        = OSPlatform.isWindows();
    /** Gap between GUI elements */
    private static final int         GAP               = 20;
    /**
     * The width in pixels for the column that shows the tree with the student
     * submission folders
     */
    private static final int         DIR_TREE_WIDTH    = 270;
    /**
     * The width in pixels for the main panel
     */
    private static final int       MAIN_PANEL_WIDTH    = WINDOW_WIDTH 
                                                         - DIR_TREE_WIDTH - GAP;
    /**
     * The size of the progress bar
     */
    private static final Dimension PROGRESS_BAR_SIZE   = new Dimension(150, 21);
    /** The size for the buttons */
    private static final Dimension BUTTON_SIZE         = new Dimension(80,
                                                             JCOMPONENT_HEIGHT);
    /** The size for the instrumentation button */
    private static final Dimension INSTRUMENTATION_BUTTON_SIZE
                                                       = new Dimension(100,
                                                             JCOMPONENT_HEIGHT);
    /** The size for the buttons */
    private static final Dimension DROPDOWN_MENU_SIZE  = new Dimension(100, 20);
    /**
     * The height in pixels for the control panel
     */
    private static final int       CONTROL_ITEM_HEIGHT = 65;
    /**
     * The size of the panel to start/stop the analysis for a given submission
     */
    private static final Dimension DIR_ANALYSIS_PANEL_SIZE = 
                                   new Dimension(2 * BUTTON_SIZE.width,
                                                 CONTROL_ITEM_HEIGHT);
    /**
     * The size of the classpath control panel
     */
    private static final Dimension CLASSPATH_PANEL_SIZE    = IS_WINDOWS?
                                   new Dimension(3 * BUTTON_SIZE.width + GAP,
                                                 CONTROL_ITEM_HEIGHT) :
                                   new Dimension(3 * BUTTON_SIZE.width,
                                                 CONTROL_ITEM_HEIGHT);
    /**
     * The size of the program control panel
     */
   private static final Dimension PROGRAM_PANEL_SIZE =
                                 new Dimension(3 * BUTTON_SIZE.width 
                                             + INSTRUMENTATION_BUTTON_SIZE.width
                                             + DROPDOWN_MENU_SIZE.width,
                                               CONTROL_ITEM_HEIGHT);
    
    /** The background color */
    private static final Color     BACKGROUND_COLOR  = new Color(225, 230, 245);
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The main panel with the tabs */
    private DelveGUITabbedPanel             mainPanel;
    /** The panel with the student submissions as a file tree */
    private JPanel                          fileTreePanel;
    /** The file tree with the student submissions */
    private DelveFileTree                   fileTree;
    /** The control to show/hide the panel with the plots */
    private OnOffSwitchPanel                analyzeDirSwitch;
    /** The control to start/stop running the program in the text editor */
    private OnOffSwitchPanel                runProgramSwitch;
    /** The button to clear the classpath */
    private JButton                         clearClasspathButton;
    /** The button to fix the classpath (if there are missing classes) */
    private JButton                         fixClasspathButton;
    /** The button to instrument the program */
    private JButton                         instrumentationButton;
    /**
     * Set with source code files that are required to compile the code or an
     * empty set if there are no such dependencies
     */
    private Set<File>                       requiredSourceFiles;
    /**
     * Jar-files to include in the classpath or an empty set if there are no
     * jar-file dependencies
     */
    private List<File>                      classpathJarFiles;
    /**
     * The most frequent unresolved symbol or {@code null} if there are no
     * unresolved symbols
     */
    private String                          unresolvedSymbol;
    /**
     * The timeout for the program execution, expressed in seconds, or
     * {@code null} for no timeout
     */
    private Double                          timeoutInSec;
    /** Module to visually display the progress of a task towards completion */
    private ProgressPanel                   progressPanel;
    /**
     * The current submission folder (even if we have selected a snapshot in
     * that folder)
     */
    private File                            currSubmissionFolder;
    /**
     * Lookup map where the key is a submission folder and the value is the
     * submission report for that folder
     */
    private Map<File, String>               reportLookup;
    /**
     * Lookup map where the key is a submission folder and the value is the last
     * parseable java file under this folder
     */
    private Map<File, JavaFile>             submittedFileLookup;
    /**
     * Map with the submission programs as keys and their functionality
     * correctness (i.e., {@code true} if functionally correct or {@code false}
     * otherwise)
     */
    private Map<File, Boolean>              correctnessLookup;
    /**
     * Map where the key is the snapshot file name in the submission and the
     * value is a list with the complex methods in the file or an empty map if
     * there are no complex methods
     */
    private Map<File, List<Method>>         complexMethodMap;
    /**
     * The threshold for the cyclomatic complexity for a complex method (i.e.,
     * method with value equal to or greater than the threshold)
     */
    private int                             cycThreshold;
    /**
     * The threshold for the number of instructions for a complex method (i.e.,
     * method with value equal to or greater than the threshold)
     */
    private int                             instrThreshold;
    /** Worker thread that calculates the complexity thresholds */
    private Thread                          complexityThresholdThread;
    
    /**
     * A map where the key is the submission folder and the value is a map with
     * the student snapshots as keys and the times the student worked on each
     * snapshot as values
     */
    private Map<File, Map<String, Integer>> timeDiffLookup;
    /** Worker thread that runs the program in the text editor */
    private Thread                          programRunner;
    /** Worker thread that analyzes a submission folder */
    private Thread                          workerThread;
    /** Worker thread that compiles the files in a submission folder */
    private Thread                          compilerThread;
    /**
     * Worker thread that executes the instrumented student programs to verify
     * their functionality
     */
    private Thread                          functionalityAnalyzer;
    /**
     * {@code true} if the file tree selection is a source code file or
     * {@code false} otherwise
     */
    private boolean                         selectionIsSourceCode;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a GUI for the method name evaluation.
     */
    public DelveGUI() {
        super("Delve");
        
        if (LOAD_IMAGES_FROM_CLASSPATH) {
            URL url = ClassLoader.getSystemClassLoader()
                                 .getResource(IMAGE_PACKGAGE  + "/"
                                            + DELVE_IMAGE_DIR + "/delve.png");
            if (url != null) {
                BufferedImage image = null;
                try {
                    image = ImageIO.read(url);
                    setIconImage(image);
                }
                catch (IOException e) {
                }
            }
        }
        else {
            setIconImage(Toolkit.getDefaultToolkit().getImage(IMAGE_DIR
                                                            + File.separator
                                                            + "delve.png"));
        }
        
        // Set the default values for the complexity thresholds
        cycThreshold = DEFAULT_CYC_THRESHOLD;
        instrThreshold = DEFAULT_INSTUCTION_THRESHOLD;
        
        // Step 1: Create visual components of the GUI
        createGUI();
        
        // Step 2: Make the frame visible only once done with everything
        setVisible(true);
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Creates the GUI. <br>
     * The GUI has all the visual components, but no ability to interact with
     * the user.
     */
    private void createGUI() {
        configureWindow();
        
        JComponent container = (JComponent)getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        
        // Add GUI components
        
        // File Tree Panel: shows the student submissions as a file tree
        JPanel fileTreePanel = createFileTreePanel();
        container.add(fileTreePanel);
        
        // Main Panel: shows the control toolbar on top and then either an
        //             editor to view/modify the source code or a number of
        //             plots with aggregated info
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        //  1) Control Panel
        JPanel controlPanel = createControlPanel();
        controlPanel.setBackground(BACKGROUND_COLOR);
        Dimension controlPanelSize = new Dimension(MAIN_PANEL_WIDTH, 0);
        controlPanel.setMinimumSize(controlPanelSize);
        centerPanel.add(controlPanel, BorderLayout.PAGE_START);
        
        // 2) Main Panel
        Dimension mainPanelSize = new Dimension(MAIN_PANEL_WIDTH,
                                                WINDOW_HEIGHT);
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                sourceCodeChanged();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                sourceCodeChanged();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) { // No text change
            }
        };
        mainPanel = new DelveGUITabbedPanel(mainPanelSize,
                                            BACKGROUND_COLOR,
                                            documentListener,
                                            progressPanel);
        centerPanel.add(mainPanel, BorderLayout.CENTER);
        centerPanel.setPreferredSize(mainPanelSize);
        centerPanel.setMaximumSize(mainPanelSize);
        
        container.add(centerPanel, BorderLayout.CENTER);
        container.setBackground(BACKGROUND_COLOR);
        
        // Initially, the plot panel does not appear on screen
        reset();
        
        // Make the frame non-resizable
        setResizable(true);
    }
    
    // ----------------------------------------------------------------- //
    //   S  U  B  M  I  S  S  I  O  N     T  R  E  E     P  A  N  E  L   //
    // ----------------------------------------------------------------- //
    /**
     * Creates the panel that displays the student submission folders in a tree
     * structure.<br>
     * 
     * @return the panel with the student submission folders in a tree
     *         structure
     */
    private JPanel createFileTreePanel() {
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        
        fileTreePanel = new JPanel();
        fileTreePanel.setLayout(new BoxLayout(fileTreePanel, BoxLayout.Y_AXIS));
        fileTreePanel.setAlignmentX(LEFT_ALIGNMENT);
        fileTreePanel.setMaximumSize(new Dimension(DIR_TREE_WIDTH,
                                                   WINDOW_HEIGHT));
        fileTreePanel.setBackground(BACKGROUND_COLOR);
        
        // 1. Select the root directory with student submission folders
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.X_AXIS));
        final JLabel loadingLabel = new JLabel(" ");
        correctnessLookup = new HashMap<File, Boolean>();
        complexMethodMap  = new HashMap<File, List<Method>>();
        reportLookup = new HashMap<File, String>();
        @SuppressWarnings("serial")
        JPanel rootDirBrowsePanel =
               new BrowsePanel(null,
                               "Select the root directory with the submission "
                             + "subfolders",
                               JFileChooser.DIRECTORIES_ONLY,
                               null,
                               null) {
                   public void performAction(File rootDir)
                          throws ErrorException {
                       if (rootDir != null   &&
                           rootDir.canRead() && rootDir.isDirectory()) {
                           loadingLabel.setText("  Loading ...");
                           loadingLabel.validate();
                           reset();
                           
                           // Use a thread (do not block the Swing thread) to: 
                           // 1) Process the directory to bring the submissions
                           //    into the expected format (if different)
                           // 2) Load the file tree for the processed structure
                           Thread worker = new Thread() {
                               @Override
                               public void run() {
                                   try {
                                       Preprocessor.sortSnapshots(rootDir);
                                       timeDiffLookup = 
                                           Util.getTimeDiffs(rootDir);
                                       submittedFileLookup =
                                           Util.getSubmittedFileLookup(rootDir);
                                   }
                                   catch (ErrorException ee) {
                                       loadingLabel.setText(" ");
                                       UIUtil.showError(ee.getMessage(),
                                                        "Error processing "
                                                      + rootDir.getName());
                                       return;
                                   }
                                   
                                   unresolvedSymbol = getUnresolvedSymbol();
                                   if (unresolvedSymbol == null) {
                                       calculateComplexityThresholds();
                                   }
                                   fileTree =
                                       new DelveFileTree(rootDir,
                                                         new String[] {
                                                             JAVA_FILE_EXTENSION
                                                         },
                                                         correctnessLookup,
                                                         complexMethodMap) {
                                       @Override
                                       public void nodeSelected(File selection)
                                              throws ErrorException {
                                           if (selection.isFile()) {
                                               selectionIsSourceCode = true;
                                               List<Method> complexMethods =
                                                complexMethodMap.get(selection);
                                               displaySourceCode(selection,
                                                                complexMethods);
                                           }
                                           else if (selection.isDirectory()) {
                                               selectionIsSourceCode = false;
                                               File dir = selection;
                                               if (dir.equals(rootDir)) {
                                                   displayOverallInfo(rootDir,
                                                                      true);
                                               }
                                               else {
                                                   displaySubmissionInfo(dir,
                                                                         null);
                                               }
                                           }
                                       }
                                   };
                                   
                                   SwingUtilities.invokeLater(new Runnable() {
                                       @Override
                                       public void run() {
                                           fixClasspathButton.setVisible(
                                                      unresolvedSymbol != null);
                                           fileTreePanel.add(fileTree);
                                           displayOverallInfo(rootDir, false);
                                           resultPanel.validate();
                                           loadingLabel.setText("");
                                       }
                                   });
                               }
                           };
                           
                           worker.start();
                       }
                   }
               };
        
        loadingPanel.setMaximumSize(new Dimension(DIR_TREE_WIDTH,
                                                  2 * JCOMPONENT_HEIGHT));
        
        // Now add the components
        rootDirBrowsePanel.setBackground(BACKGROUND_COLOR);
        loadingPanel.add(rootDirBrowsePanel);
        loadingPanel.add(loadingLabel);
        loadingPanel.setBorder(BorderFactory.createTitledBorder(
                                                    "Load submission folders"));
        loadingPanel.setAlignmentX(LEFT_ALIGNMENT);
        loadingPanel.setBackground(BACKGROUND_COLOR);
        resultPanel.add(loadingPanel);
        resultPanel.add(fileTreePanel);
        resultPanel.setBackground(BACKGROUND_COLOR);
        
        return resultPanel;
    }
    
    /**
     * Creates the panel with the controls 
     * 
     * @return the panel that displays the source code, shows stat info about
     *         the submission etc.
     */
    private JPanel createControlPanel() {
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.X_AXIS));
        
        // ----------------------------------------------------------- //
        //   Switch to start / stop processing the selected directory  //
        // ----------------------------------------------------------- //
        ActionListener onActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Start analysis
                analyzeSingleSubmission();
                analyzeDirSwitch.enableOffButton();
                analyzeDirSwitch.lockButtons();
            }
        };
        ActionListener offActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Stop analysis
                if (workerThread != null && workerThread.isAlive()) {
                    workerThread.interrupt();
                }
                analyzeDirSwitch.unlockButtons();
                analyzeDirSwitch.enableOnButton();
            }
        };
        
        analyzeDirSwitch =
               new OnOffSwitchPanel("Submission Analysis",
                                    "Start",
                                    DelveGUIHelper.getIcon("start.png"),
                                    "Process the selected submission",
                                    onActionListener,
                                    "Stop",
                                    DelveGUIHelper.getIcon("stop.png"),
                                    "Stop processing the selected submission",
                                    offActionListener,
                                    BUTTON_SIZE);
        analyzeDirSwitch.setBackground(BACKGROUND_COLOR);
        analyzeDirSwitch.setMaximumSize(DIR_ANALYSIS_PANEL_SIZE);
        resultPanel.add(analyzeDirSwitch);
        
        // ---------------------------------- //
        //  Buttons to set/fix the classpath  //
        // ---------------------------------- //
        requiredSourceFiles = new TreeSet<File>();
        classpathJarFiles = new ArrayList<File>();
        JPanel classpathPanel = new JPanel();
        classpathPanel.setLayout(new BoxLayout(classpathPanel,
                                               BoxLayout.X_AXIS));
        classpathPanel.setBorder(BorderFactory.createTitledBorder("Classpath"));
        ImageIcon listIcon = DelveGUIHelper.getIcon("list.png");
        JButton showClasspathButton = new JButton("Show", listIcon);
        showClasspathButton.setToolTipText("Show a list of files in the order "
                                         + "they are added to the classpath");
        showClasspathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder classpath = new StringBuilder();
                int i = 1;
                for (File file : requiredSourceFiles) {
                    classpath.append(i + ". " + file.getPath() + NEW_LINE);
                    i++;
                }
                for (File file : classpathJarFiles) {
                    classpath.append(i + ". " + file.getPath() + NEW_LINE);
                    i++;
                }
                if (classpath.length() == 0) {
                    classpath.append("The classpath is empty");
                }
                JOptionPane.showMessageDialog(showClasspathButton,
                                              classpath,
                                              "Classpath files", 
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        });
        classpathPanel.add(showClasspathButton);
        
        ImageIcon clearIcon = DelveGUIHelper.getIcon("clear.png");
        clearClasspathButton = new JButton("Clear", clearIcon);
        clearClasspathButton.setToolTipText("Clear the classpath");
        clearClasspathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (requiredSourceFiles.isEmpty() && 
                    classpathJarFiles.isEmpty()) {
                    return; // The classpath is already empty
                }
                requiredSourceFiles.clear();
                classpathJarFiles.clear();
                Thread worker = new Thread() {
                    @Override
                    public void run() {
                        progressPanel.setVisible(true);
                        unresolvedSymbol = getUnresolvedSymbol();
                        if (unresolvedSymbol == null) {
                            calculateComplexityThresholds();
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                clearClasspathButton.setEnabled(false);
                                runProgramSwitch.disableButtons();
                                fixClasspathButton.setVisible(
                                                    unresolvedSymbol != null);
                            }
                        });
                    }
                };
                worker.start();
            }
        });
        clearClasspathButton.setEnabled(false);
        classpathPanel.add(clearClasspathButton);
        
        ImageIcon repairIcon = DelveGUIHelper.getIcon("repair.png");
        fixClasspathButton = new JButton("Fix errors", repairIcon);
        fixClasspathButton.setForeground(Color.RED);
        fixClasspathButton.setToolTipText("Add required files to classpath");
        fixClasspathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Browse for classpath
                fixClasspath();
            }
        });
        fixClasspathButton.setVisible(false); 
        classpathPanel.add(fixClasspathButton);
        classpathPanel.setBackground(BACKGROUND_COLOR);
        classpathPanel.setVisible(true);
        classpathPanel.setMaximumSize(CLASSPATH_PANEL_SIZE);
        resultPanel.add(classpathPanel);
        
        // ------------------------------------------------------------ //
        //   Switch to start / stop running the program in the editor   //
        // ------------------------------------------------------------ //
        JPanel programPanel = new JPanel();
        programPanel.setLayout(new BoxLayout(programPanel, BoxLayout.X_AXIS));
        programPanel.setBackground(BACKGROUND_COLOR);
        programPanel.setBorder(BorderFactory.createTitledBorder("Program"));
        
        onActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Run the program
                runProgramSwitch.enableOffButton();
                runProgramSwitch.lockButtons();
                runProgram();
            }
        };
        offActionListener = new ActionListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void actionPerformed(ActionEvent e) { // Stop the program
                if (programRunner != null && programRunner.isAlive()) {
                    programRunner.interrupt();
                    programRunner.stop();
                }
                runProgramSwitch.unlockButtons();
                runProgramSwitch.enableOnButton();
            }
        };
        
        runProgramSwitch =
                 new OnOffSwitchPanel(null,
                                      "Run",
                                      DelveGUIHelper.getIcon("run.png"),
                                      "Execute the program shown in the editor",
                                      onActionListener,
                                      "Abort",
                                      DelveGUIHelper.getIcon("terminate.png"),
                                      "Abort the program execution",
                                      offActionListener,
                                      BUTTON_SIZE);
        runProgramSwitch.setBackground(BACKGROUND_COLOR);
        runProgramSwitch.disableButtons();
        programPanel.add(runProgramSwitch);
        
        // Add button for code instrumentation
        instrumentationButton = new JButton();
        instrumentationButton.setPreferredSize(INSTRUMENTATION_BUTTON_SIZE);
        instrumentationButton.setMinimumSize(INSTRUMENTATION_BUTTON_SIZE);
        resetInstrumentationButton();
        instrumentationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Code instrumentation
                if (functionalityAnalyzer != null &&
                    functionalityAnalyzer.isAlive() &&
                    !functionalityAnalyzer.isInterrupted()) {
                    
                    functionalityAnalyzer.interrupt();
                    resetInstrumentationButton();
                    progressPanel.setVisible(false);
                }
                else {
                    instrumentCode();
                }
            }
        });
        instrumentationButton.setEnabled(false);
        programPanel.add(instrumentationButton);
        
        // Add drop-down box to select the timeout
        JLabel timeoutLabel = new JLabel("   Timeout: ");
        JComboBox<String> timeoutComboBox =
                                      new JComboBox<String>(TIMEOUT_OPTIONS);
        Dimension size = new Dimension(DROPDOWN_MENU_SIZE);
        timeoutComboBox.setMaximumSize(size);
        timeoutComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>)e.getSource();
                String selection = (String)comboBox.getSelectedItem();
                timeoutInSec = TIMEOUT_LOOKUP.get(selection);
            }
        });
        timeoutInSec = TIMEOUT_LOOKUP.get(TIMEOUT_OPTIONS[0]); // Initially
        timeoutComboBox.setToolTipText("The program has to complete before this"
                                     + " time or else it gets terminated");
        programPanel.add(timeoutLabel);
        programPanel.add(timeoutComboBox);
        programPanel.setMaximumSize(PROGRAM_PANEL_SIZE);
        resultPanel.add(programPanel);
        
        // -------------- //
        //  Progress Bar  //
        // -------------- //
        progressPanel = new ProgressPanel(PROGRESS_BAR_SIZE);
        progressPanel.setBackground(BACKGROUND_COLOR);
        resultPanel.add(progressPanel);
        
        return resultPanel;
    }
    
    /**
     * Loads the text from the give source code file and displays it on screen.
     *  
     * @param sourceCodeFile The file with the source code
     * @param complexMethods List with the complex methods in the source code
     *                       or {@code null} or empty for none
     */
    private void displaySourceCode(File         sourceCodeFile,
                                   List<Method> complexMethods) {
        if (!sourceCodeFile.getParentFile().equals(currSubmissionFolder)) {
            mainPanel.clearCurrSubmissionPlots();
            currSubmissionFolder = null;
        }
        
        try {
            List<String> lines = FileIOUtil.readFile(sourceCodeFile.getPath(),
                                                     false, // skipEmptyLines
                                                     false, // skipCommentLines
                                                     false, // trimLines
                                                     null); // regexToMatch
            
            StringBuilder fileContentsStrBuilder = new StringBuilder();
            for (String currLine: lines) {
                fileContentsStrBuilder.append(currLine);
                fileContentsStrBuilder.append(NEW_LINE);
            }
            
            String text = fileContentsStrBuilder.toString();
            mainPanel.setText(text, true);
            if (complexMethods != null && !complexMethods.isEmpty()) {
                for (Method complexMethod : complexMethods) {
                    int startLine = complexMethod.getBeginLine() - 1;
                    int endLine = complexMethod.getEndLine();
                    mainPanel.highlightText(startLine, endLine);
                }
            }
            mainPanel.setTextEditorEditable(true);
            // Disable the buttons to analyze a submission folder (since we
            // selected a source file and not a submission folder)
            analyzeDirSwitch.disableButtons();
            // Allow the program/snapshot code to run if the classpath is ok
            String studentID = sourceCodeFile.getParentFile().getName();
            boolean isCompilable = unresolvedSymbol == null &&
                                   !studentID.equals(EXTRA_FILES_DIR);
            if (isCompilable) {
                Set<File> updatedRequiredSourceFiles =
                  DelveGUIHelper.updateRequiredSourceFiles(studentID,
                                                           requiredSourceFiles);
                String compileError =
                              Compiler.isCompilable(sourceCodeFile,
                                                    updatedRequiredSourceFiles,
                                                    classpathJarFiles);
                if (compileError != null) {
                    mainPanel.setErrorText(compileError);
                    isCompilable = false;
                }
                else {
                    mainPanel.clearConsole();
                }
            }
            if (isCompilable) {
                runProgramSwitch.enableOnButton();
            }
            else {
                runProgramSwitch.disableButtons();
            }
            mainPanel.showTextEditor(); 
        }
        catch (ErrorException ee) {
            UIUtil.showError(ee.getMessage(),
                             "Error loading code from file '" 
                            + sourceCodeFile.getName() + "'");
        }
    }
    
    /**
     * Processes the submission folders and displays the overall results on
     * screen.
     * 
     * @param submissionRootDir The directory that contains the submission
     *                          subfolders
     * @param recreateHistograms {@code true} to recreate the histograms about
     *                           all submissions or {@code false} to create them
     *                           only if they have not been created before
     */
    private void displayOverallInfo(File    submissionRootDir,
                                    boolean recreateHistograms) {
        mainPanel.clearCurrSubmissionPlots();
        currSubmissionFolder = null;
        
        mainPanel.displayOverallInfo(submissionRootDir,
                                     timeDiffLookup,
                                     submittedFileLookup,
                                     correctnessLookup,
                                     recreateHistograms);
        
        if (recreateHistograms) {  // We selected the root directory
            // Disable the buttons to analyze a submission folder (since we
            // selected the root directory and not a particular submission)
            analyzeDirSwitch.disableButtons();
            // There is no selected program/snapshot code to run
            runProgramSwitch.disableButtons();
            // Disable the text editor
            mainPanel.setTextEditorEditable(false);
            // Show the panel with the histograms for all submissions
            mainPanel.showAllSubmissionsHistograms();
        }
    }
    
    /**
     * Processes the given submission folder and displays the results on screen.
     *  
     * @param submissionDir The submission folder to process
     * @param compilableFiles A set with the submission files that are
     *                        compilable or {@code null} to skip this piece of
     *                        information (e.g., when the classpath is not set)
     */
    private void displaySubmissionInfo(File        submissionDir,
                                       Set<String> compilableFiles) {
        if (!submissionDir.equals(currSubmissionFolder)) {
            mainPanel.clearCurrSubmissionPlots();
            currSubmissionFolder = null;
        }
        
        String report;
        if (reportLookup.containsKey(submissionDir)) {
            report = reportLookup.get(submissionDir);
        }
        else {
            // Get the last snapshot
            JavaFile lastSnapshot = submittedFileLookup.get(submissionDir);
            if (lastSnapshot == null) {
                mainPanel.setText("There is no snapsot that compiles", false);
                return;
            }
            
            Map<String, Integer> snapshotTimeDiffs = 
                                         timeDiffLookup.get(submissionDir);
            report = DelveGUIHelper.getSubmissionTimeReport(submissionDir,
                                                            lastSnapshot,
                                                            snapshotTimeDiffs,
                                                            compilableFiles);
        }
        
        // Show the report in the text editor
        mainPanel.setText(report, false);
        mainPanel.setTextEditorEditable(false);
        // Enable the buttons to analyze the submission folder
        analyzeDirSwitch.enableOnButton();
        // There is no selected program/snapshot code to run
        runProgramSwitch.disableButtons();
        // Show the panel with the text editor to display the submission info
        mainPanel.showTextEditor(); 
    }
    
    /**
     * Guides the user step-by-step to fix the classpath so that there are no
     * unresolved symbols.
     */
    private void fixClasspath() {
        String prompt = "Please provide the jar-files and/or the directories "
                      + NEW_LINE + "with source files to resolve the following:"
                      + NEW_LINE + unresolvedSymbol; 
        boolean proceed = UIUtil.showConfirmMessage(prompt, "Missing classes");
        if (!proceed) {
            return;
        }
        
        File currentDir = null;
        while (true) {
            File file = UIUtil.browse(this,
                                      JFileChooser.FILES_ONLY,
                                      currentDir);
            if (file == null) {
                break;
            }
            
            currentDir = file.getParentFile();
            String filename = file.getName().toLowerCase();
            if (filename.endsWith(JAVA_FILE_EXTENSION)) {
                requiredSourceFiles.add(file);
            }
            else if (filename.endsWith(JAR_FILE_EXTENSION)) {
                if (!classpathJarFiles.contains(file)) {
                    classpathJarFiles.add(file);
                }
            }
            else {
                UIUtil.showError(file.getName() + " is not a jar-file or "
                               + "directory." + NEW_LINE + "Try again.",
                                 "Invalid selection");
                continue;
            }
            
            int answer = JOptionPane.showConfirmDialog(
                                   null,
                                   "Are you done with the classpath selection?",
                                   "Classpath Selection",
                                   JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                break;
            }
        }
        
        Thread worker = new Thread() {
            @Override
            public void run() {
                unresolvedSymbol = getUnresolvedSymbol();
                if (unresolvedSymbol == null) {
                    calculateComplexityThresholds();
                }
                fixClasspathButton.setVisible(unresolvedSymbol != null);
                clearClasspathButton.setEnabled(classpathJarFiles.size() > 0 ||
                                                requiredSourceFiles.size() > 0);
                instrumentationButton.setEnabled(unresolvedSymbol == null);
            }
        };
        
        progressPanel.reset(true); // Show the progress bar with a value of 0%
        worker.start();
    }
    
    /**
     * Runs to code that appears in the text editor.
     */
    private void runProgram() {
        programRunner = new Thread() {
            @Override
            public void run() {
                String sourceCode = mainPanel.getText();
                
                String classpath = null;
                if (!classpathJarFiles.isEmpty()) {
                    StringBuilder classpathBuilder = new StringBuilder();
                    Iterator<File> itr = classpathJarFiles.iterator();
                    while (itr.hasNext()) {
                        classpathBuilder.append(itr.next().getPath());
                        if (itr.hasNext()) {
                            classpathBuilder.append(File.pathSeparator);
                        }
                    }
                    classpath = classpathBuilder.toString();
                }
                
                File submissionDir = fileTree.getSelectedItem().getParentFile();
                String studentID = submissionDir.getName();
                Set<File> updatedRequiredSourceFiles =
                  DelveGUIHelper.updateRequiredSourceFiles(studentID,
                                                            requiredSourceFiles);
                
                if (classpath == null && updatedRequiredSourceFiles.isEmpty()) {
                    try {
                        ProgramExecutor.executeProgram(sourceCode,
                                                       null, // imports
                                                       null, // replacements
                                                       null, // required files
                                                       null);// classpath
                    }
                    catch (ErrorException ee) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!programRunner.isInterrupted()) {
                                   mainPanel.setErrorText(ee.getMessage());
                                   mainPanel.showConsole();
                                }
                            }
                        });
                    }
                }
                else {
                    try {
                        ProgramExecutor.compileAndExecuteProgramFromDisk(
                                                     sourceCode,
                                                     false, // saveStateBefore
                                                     false, // saveStateAfter
                                                     null,  // imports
                                                     null,  // replacements
                                                     updatedRequiredSourceFiles,
                                                     classpath,
                                                     COMPILE_DEST_DIR,
                                                     null);
                    }
                    catch (ErrorException ee) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!programRunner.isInterrupted()) {
                                    mainPanel.setErrorText(ee.getMessage());
                                    mainPanel.showConsole();
                                 }
                            }
                        });
                    }
                }
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        runProgramSwitch.unlockButtons();
                        runProgramSwitch.enableOnButton();
                    }
                });
            }
        };
        
        programRunner.start();
    }
    
    /**
     * Calculates the minimum number of instructions and the minimum cyclomatic
     * complexity that a method should have to be considered complex
     */
    @SuppressWarnings("deprecation")
    private void calculateComplexityThresholds() {
        if(complexityThresholdThread != null &&
           complexityThresholdThread.isAlive()) {
            complexityThresholdThread.interrupt();
            complexityThresholdThread.stop();
        }
            
        complexityThresholdThread = new Thread() {
            @Override
            public void run() {
                try {
                    List<MethodMetric>  methodMetrics =
                               Util.getMostComplexMethods(fileTree.getRootDir(),
                                                          classpathJarFiles);
                    List<Integer> instrList = new ArrayList<Integer>();
                    List<Integer> cycList = new ArrayList<Integer>();
                    for (MethodMetric methodMetric : methodMetrics) {
                        instrList.add(methodMetric.getNumOfInstructions());
                        cycList.add(methodMetric.getCyclomaticComplexity());
                    }
                    
                    double cycMedian = StatsUtil.getMedian(cycList);
                    double instrMedian = StatsUtil.getMedian(instrList);
                    double cycMean = StatsUtil.getMean(cycList, 0);
                    double instrMean = StatsUtil.getMean(instrList, 0);
                    
                    // The thresholds for the cyc and number of instructions for
                    // a complex method are calculated as follows: First we find
                    // the median and mean values for the most complex methods. 
                    // The threshold is then the minimum of the {median, mean}
                    cycThreshold = (int)(Math.min(cycMedian, cycMean));
                    instrThreshold = (int)(Math.min(instrMedian, instrMean));
                    System.err.println("-------------------------------------");
                    System.err.println("cycThreshold = " + cycThreshold);
                    System.err.println("instrThreshold = " + instrThreshold);
                }
                catch (ErrorException ignored) {
                }
            }
        };
        
        complexityThresholdThread.start();
    }
    
    /**
     * Instruments the code to verify the functionality of the submitted
     * snapshots.
     */
    private void instrumentCode() {
        Set<JavaFile> submittedPrograms = new HashSet<JavaFile>();
        submittedPrograms.addAll(submittedFileLookup.values());
        
        // List to store the files that the user provides to perform the code
        // instrumentation
        Set<File> instrumentationFiles = new TreeSet<File>();
        new InstrumentationDialog(INSTRUMENTATION_GUIDELINES,
                                  INSTRUMENTATION_EXAMPLES,
                                  BACKGROUND_COLOR,
                                  submittedPrograms,
                                  instrumentationFiles);
        
        File referenceProgram;
        InstrumentationConfig instrumentationConfig;
        try {
            referenceProgram =
                     DelveGUIHelper.getReferenceProgram(instrumentationFiles,
                                                        submittedPrograms);
            String error = Compiler.isCompilable(referenceProgram,
                                                 requiredSourceFiles,
                                                 classpathJarFiles);
            if (error != null) {
                UIUtil.showError("File '" +  referenceProgram.getName()
                               + "' does not compile. Details: " + NEW_LINE
                               + error,
                                 "Compile Error");
                return;
            }
            
            instrumentationFiles.remove(referenceProgram);
            
            instrumentationConfig = 
                      DelveGUIHelper.instrumentProgram(instrumentationFiles,
                                                       requiredSourceFiles,
                                                       classpathJarFiles,
                                                       referenceProgram);
        }
        catch (ErrorException ee) {
            UIUtil.showError(ee.getMessage(), "Instrumentation Error");
            return;
        }
        
        StringBuilder error = new StringBuilder();
        File referenceOutputFile = executeProgram(referenceProgram,
                                                  instrumentationConfig,
                                                  true, // showError
                                                  error);
        if (error.length() > 0) {
             UIUtil.showError(error.toString(), "Execution Error");
        }
        if (referenceOutputFile == null) {
            // Error producing reference output. There is no point moving on.
            return;
        }
        
        // Make sure that the reference program has executed and produced
        String referenceOutput;
        try {
            referenceOutput = FileIOUtil.readFile(referenceOutputFile);
        }
        catch (ErrorException ee) {
            UIUtil.showError("Error extracting reference result from file '"
                           + referenceOutputFile.getPath() + "'. Details:"
                           + NEW_LINE + ee.getMessage(),
                             "Reference result error");
            // Error reading reference output. There is no point moving on.
            return;
        }
        
        progressPanel.reset(true);
        
        // Map with the functionality correctness results that uses as key the
        // submitted program and as value {@code true} if the program that the
        // student submitted is correct or {@code false} otherwise 
        Map<File, Boolean> correctnessLookupTemp = new HashMap<File, Boolean>();
        functionalityAnalyzer = new Thread() {
            @Override
            public void run() {
                ImageIcon cancelIcon = DelveGUIHelper.getIcon("cancel.png");
                instrumentationButton.setIcon(cancelIcon);
                instrumentationButton.setText(null);
                instrumentationButton.setToolTipText("Cancel the "
                                                   + "instrumentation process");
                
                double percent = 100.0 / submittedPrograms.size();
                int count = 0;
                StringBuilder aggregatedErrors = new StringBuilder();
                for (JavaFile submittedProgram : submittedPrograms) {
                    File srcFile = new File(submittedProgram.getFilePathname());
                    // Check if the student folder has 'extra_files' to include
                    // If so, those (the last parseable snapshot) exist in a
                    // subfolder with the student name in the temp directory
                    String studentID = srcFile.getParentFile().getName();
                    InstrumentationConfig currConfig;
                    try {
                        currConfig =
                            DelveGUIHelper.updateConfig(studentID,
                                                        instrumentationConfig);
                    }
                    catch (ErrorException ee) { // Should not happen
                        // The execution will probably fail, but give it a try
                        currConfig = instrumentationConfig;
                    }
                    
                    // Instrument and then execute the program
                    File outputFile = executeProgram(srcFile,
                                                     currConfig,
                                                     false, // showError
                                                     aggregatedErrors);
                    String outputResult = null;
                    if (outputFile != null) {
                        try {
                            outputResult = FileIOUtil.readFile(outputFile);
                        }
                        catch (ErrorException ignored) {
                        }
                    }
                    
                    boolean correct = outputResult != null &&
                                      outputResult.equals(referenceOutput);
                    correctnessLookupTemp.put(srcFile, correct);
                    count++;
                    
                    // Show progress
                    int progressValue = (int)Math.round(count * percent);
                    progressPanel.setValue(progressValue);
                    if (isInterrupted()) {
                        return;
                    }
                }
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (aggregatedErrors.length() > 0) {
                            UIUtil.showError(aggregatedErrors.toString(),
                                             "Execution Error");
                        }
                        fileTree.repaint();
                        mainPanel.clear();
                        resetInstrumentationButton();
                        progressPanel.setVisible(false);
                    }
                });
                
                // Update the map with the instrumentation results
                correctnessLookup.clear(); // Clear old values
                correctnessLookup.putAll(correctnessLookupTemp);//Add new values
                // Show the main submission histograms with the results (i.e.,
                // how many submissions are correct in every bin)
                displayOverallInfo(fileTree.getRootDir(),
                                   true); // recreateHistograms
            }
        };
        functionalityAnalyzer.start();
    }
    
    /**
     * Given a source code file, it instruments it according to the specified
     * configuration and executes it.<br>
     * The result of the execution is an output file with the saved state(s).
     * 
     * @param sourceCodeFile The source code file to instrument and execute
     * @param instrumentationConfig The instrumentation configuration
     * @param showError {@code true} to show a user message in case of an error
     *                  or {@code false} otherwise
     * @param aggregatedErrors Buffer to store the errors to show them at once
     *                         after all files have been analyzed
     * 
     * @return the produced file with the saved states or {@code null} in case
     *         of an error executing the instrumented program
     */
    private File executeProgram(File                  sourceCodeFile,
                                InstrumentationConfig instrumentationConfig,
                                boolean               showError,
                                StringBuilder         aggregatedErrors) {
        String code = null;
        try {
            code = DelveGUIHelper.instrumentMainMethod(sourceCodeFile,
                                                       timeoutInSec);
        }
        catch (ErrorException ee) {
            aggregatedErrors.append(ee.getMessage() + NEW_LINE);
        }
        
        File outputFile = null;
        if (code != null) {
            try {
                boolean success =
                    ProgramExecutor.compileAndExecuteProgramFromDisk(
                                                          code,
                                                          instrumentationConfig,
                                                          COMPILE_DEST_DIR,
                                                          timeoutInSec);
                if (success) {
                    outputFile =
                    DelveGUIHelper.getInstrumentationOutputFile(sourceCodeFile);
                }
            }
            catch (ErrorException ee) {
                String errorMsg = "Error executing code in file '"
                                + sourceCodeFile.getName() + "'.";
                if (ee.getMessage() != null) {
                    errorMsg += " Details:" + NEW_LINE + ee.getMessage();
                }
                else if (ee.getCause() != null &&
                         ee.getCause().getMessage() != null) {
                    errorMsg += " Details:" + NEW_LINE 
                                + ee.getCause().getMessage();
                }
                else {
                    errorMsg = null; // Unknown error: do not confuse the user
                }
                
                if (errorMsg != null && showError) {
                    aggregatedErrors.append(errorMsg + NEW_LINE);
                }
            }
        }
        
        return outputFile;
    }
    
    /**
     * Called when the source code in the text panel changed.<br>
     * If the new code is compilable it enables the button to run the program.
     * Otherwise it disables it.
     */
    private void sourceCodeChanged() {
        if (selectionIsSourceCode) {
            String sourceCode = mainPanel.getText();
            String compileError = Compiler.isCompilable(sourceCode,
                                                        requiredSourceFiles,
                                                        classpathJarFiles);
            boolean isCompilable;
            if (compileError != null) {
                mainPanel.setErrorText(compileError);
                isCompilable = false;
            }
            else {
                mainPanel.clearConsole();
                isCompilable = true;
            }
            
            if (isCompilable) {
                runProgramSwitch.enableOnButton();
            }
            else {
                runProgramSwitch.disableButtons();
            }
        }
    }
    
    /**
     * Determines if there are any unresolved symbols by analyzing the last
     * snapshot for each student.<br>
     * If there is one or more unresolved student in more than 25% of the
     * students it does not process (for efficiency) the rest of the snapshots
     * (at this point we know that source code or a library is missing) and
     * returns the most common symbol resolution error so far.
     * 
     * @return the most common symbol resolution error or {@code null} if the
     *         majority of the files do not include unresolved symbols
     */
    private String getUnresolvedSymbol() {
        Collection<JavaFile> javaFiles = submittedFileLookup.values();
        for (File submissionDir : submittedFileLookup.keySet()) {
            JavaFile javaFile =  submittedFileLookup.get(submissionDir);
            
            String studentID = submissionDir.getName();
            Set<File> sourceFiles =
                  DelveGUIHelper.updateRequiredSourceFiles(studentID,
                                                           requiredSourceFiles);
            
            try {
                javaFile.setSymbolSolver(getParentDirs(sourceFiles),
                                         classpathJarFiles);
            }
            catch (ErrorException ee) {
                UIUtil.showError(ee.getMessage(),
                                 "Symbol solver config error");
            }
        }
        
        String result = null;
        
        Map<String, Integer> histogram = new HashMap<String, Integer>();
        double percent = 100.0 / javaFiles.size();
        int filesWithClasspathError = 0;
        int errorThrescold = (int)Math.round(javaFiles.size() * 0.25);
        
        int count = 0;
        boolean unrsolvedSymbolError = false;
        for (JavaFile javaFile : javaFiles) {
            String unresolvedSymbol = Util.getUnresolvedSymbol(javaFile);
            if (unresolvedSymbol != null) {
                Integer appearances = histogram.get(unresolvedSymbol);
                if (appearances == null) {
                    appearances = 0;
                }
                histogram.put(unresolvedSymbol, appearances + 1);
                
                filesWithClasspathError++;
                if (filesWithClasspathError > errorThrescold) {
                    // 25% of the files has a symbol resolution error => stop
                    unrsolvedSymbolError = true;
                    break;
                }
            }
            count++;
            
            int progressValue = (int)Math.round(count * percent);
            progressPanel.setValue(progressValue);
        }
        
        if (unrsolvedSymbolError) {
            int maxAppearances = 0;
            for (String unresolvedSymbol : histogram.keySet()) {
                Integer appearances = histogram.get(unresolvedSymbol);
                if (appearances > maxAppearances) {
                    result = unresolvedSymbol;
                    maxAppearances = appearances;
                }
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressPanel.setVisible(false);
            }
        });
        
        return result;
    }
    
    /**
     * Reset to initial state after loading the application.<br>
     * Clears all text, plots etc.<br>
     */
    private void reset() {
        fileTreePanel.removeAll();
        mainPanel.clear();
        analyzeDirSwitch.disableButtons();
        progressPanel.setVisible(false);
        selectionIsSourceCode = false;
        correctnessLookup.clear();
        validate();
    }
    
    /**
     * Configures the GUI window.<br>
     * 1. Sets the look and feel.<br>
     * 2. Defines the behavior when the close window button is pressed<br>
     * 3. Sets the window size and location<br>
     */
    private void configureWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored) {
            // Do nothing (it's ok even if we can't set the look-and-feel)
        }
        setForeground(Color.BLACK);
        setBackground(Color.LIGHT_GRAY);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        pack();
        
        setResizable(true); // Do not allow the user to resize the window
        setLocationRelativeTo(null); // Center the application window
    }
    
    // -------------   C  O  D  E     A  N  A  L  Y  S  I  S   ------------- //
    /**
     * Analyzes the code snapshots for a single student submission.
     */
    private void analyzeSingleSubmission() {
        File submissionDir = fileTree.getSelectedItem();
        
        // Kick off the thread that tries to compile the code
        final Set<String> compilableFiles;
        if (unresolvedSymbol == null) { // The classpath is configured
            // The next line starts the compilerThread
            compilableFiles = getCompilableFiles(submissionDir);
        }
        else {
            compilableFiles = null;
        }
        
        List<File> sourceCodeFiles; // Code snapshot files
        Set<File> extraSourceFiles = new HashSet<File>(); // Extra files
        try {
            sourceCodeFiles = DirectoryUtil.listFiles(submissionDir,
                                                      JAVA_FILE_EXTENSION);
            File extraDir = new File(submissionDir.getPath() + File.separator
                                                             + EXTRA_FILES_DIR);
            if (extraDir.exists()) {
                List<File> files = DirectoryUtil.listFiles(extraDir,
                                                           JAVA_FILE_EXTENSION);
                extraSourceFiles.addAll(files);
            }
        }
        catch (ErrorException ee) {
            UIUtil.showError(ee.getMessage(),
                             "Error analyzing submission '" 
                            + submissionDir.getName() + "'");
            return;
        }
        
        final double percent;
        if (unresolvedSymbol == null) { // This takes twice as long
            percent = 100.0 / (2 * sourceCodeFiles.size());
        }
        else {
            percent = 100.0 / sourceCodeFiles.size();
        }
        progressPanel.reset(true); // Show the progress bar with a value of 0%
        
        workerThread = new Thread() {
            @Override
            public void run() {
                SortedSet<JavaFile> javaFiles = new TreeSet<JavaFile>();
                List<File> requiredSourceCodeDirs =
                                             getParentDirs(extraSourceFiles);
                int count = 0;
                for (File sourceCodeFile : sourceCodeFiles) {
                    try {
                        javaFiles.add(new JavaFile(sourceCodeFile.getPath(),
                                                   requiredSourceCodeDirs,
                                                   classpathJarFiles));
                    }
                    catch (ErrorException ee) { // The file is non-parseable
                    }
                    count++;
                    
                    int progressValue = (int)Math.round(count * percent);
                    progressPanel.setValue(progressValue);
                    
                    if (isInterrupted()) {
                        progressPanel.reset(false); // Reset + hide progress bar
                        compilerThread.interrupt();
                        return;
                    }
                }
                complexMethodMap =
                      mainPanel.plotMetricsOverTime(submissionDir,
                                                   javaFiles,
                                                   unresolvedSymbol == null,
                                                   this,
                                                   cycThreshold,
                                                   instrThreshold);
                if (!complexMethodMap.isEmpty()) {
                    fileTree.updateComplexityMap(complexMethodMap);
                }
                
                if (compilerThread != null) {
                    try { // By this time the compiler should have finished
                        compilerThread.join();
                    }
                    catch (InterruptedException ie) {
                    }
                }
                
                Map<String, Integer> snapshotTimeDiffs = 
                                             timeDiffLookup.get(submissionDir);
                JavaFile lastSnapshot = submittedFileLookup.get(submissionDir);
                
                String report =
                       DelveGUIHelper.getSubmissionTimeReport(submissionDir,
                                                              lastSnapshot,
                                                              snapshotTimeDiffs,
                                                              compilableFiles);
                
                reportLookup.put(submissionDir, report);
                currSubmissionFolder = submissionDir;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mainPanel.setText(report, false);
                        analyzeDirSwitch.unlockButtons();
                        analyzeDirSwitch.enableOnButton();
                        progressPanel.setVisible(false);
                    }
                });
            }
        };
        
        workerThread.start();
    }
    
    /**
     * Given a directory with source code files, it returns a set with the
     * files that are compilable.<br>
     * Note: This is a non-blocking method that runs in the background by the
     *       {@code compilerThread} thread. The caller should call
     *       'compilerThread.join()' before processing the returned set
     * 
     * @param submissionDir The submission directory with source code files
     * 
     * @return a set with the files that are compilable or {@code null} of the
     *         classpath has not been configured yet
     */
    private Set<String> getCompilableFiles(File submissionDir) {
        if (unresolvedSymbol != null) {
            // There exist unresolved symbols; fix the classpath first!
            return null;
        }
        
        List<File> sourceCodeFiles; // Code snapshot files
        try {
            sourceCodeFiles = DirectoryUtil.listFiles(submissionDir,
                                                      JAVA_FILE_EXTENSION);
        }
        catch (ErrorException ee) { // Should not happen but just in case
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    UIUtil.showError(ee.getMessage(),
                                     "Error analyzing submission '" 
                                   + submissionDir.getName() + "'");
                }
            });
            return null;
        }
        
        Set<String> compilableFiles = new TreeSet<String>();
        compilerThread = new Thread() {
            @Override
            public void run() {
                String studentID = submissionDir.getName();
                
                Set<File> updatedRequiredSourceFiles =
                  DelveGUIHelper.updateRequiredSourceFiles(studentID,
                                                           requiredSourceFiles);
                
                for (File file : sourceCodeFiles) {
                    String error =
                           Compiler.isCompilable(file,
                                                 updatedRequiredSourceFiles,
                                                 classpathJarFiles);
                    if (error == null) {
                        compilableFiles.add(file.getName());
                    }
                    
                    if (isInterrupted()) {
                        compilableFiles.clear(); // Clear intermediate results
                        return;
                    }
                }
            }
        };
        compilerThread.start();
        
        return compilableFiles;
    }
    
    /**
     * Given a set of files it returns a set with the parent directories for
     * those files.
     * 
     * @param files The set of files to get parent directories for
     * 
     * @return a list with the parent directories for the provided files
     */
    private List<File> getParentDirs(Collection<File> files) {
        Set<File> parentDirs = new HashSet<File>();
        for (File file : files) {
            parentDirs.add(file.getParentFile());
        }
        
        return new ArrayList<File>(parentDirs);
    }
    
    /**
     * Resets the instrumentation button to its default appearance.
     */
    private void resetInstrumentationButton() {
        instrumentationButton.setText("Instrument");
        ImageIcon instrumentationIcon =
                                 DelveGUIHelper.getIcon("instrumentation.png");
        instrumentationButton.setIcon(instrumentationIcon);
        instrumentationButton.setToolTipText("Instrument the code to validate "
                                           + "its correctness");
    }
    
    /**
     * Main application method.
     * 
     * @param args Ignored
     */
    public static void main(String[] args) {
        new DelveGUI();
    }
}
