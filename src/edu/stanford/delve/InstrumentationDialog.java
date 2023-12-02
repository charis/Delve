/*
 * File          : InstrumentationDialog.java
 * Author        : Charis Charitsis
 * Creation Date : 30 November 2020
 * Last Modified : 5 January 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.util.UIUtil;
import edu.stanford.util.filesystem.FileUtil;
// Import constants
import static edu.stanford.constants.Constants.CURR_DIR;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.delve.Constants.IMAGE_DELIMITER;
import static edu.stanford.delve.Constants.IMAGE_DIR;
import static edu.stanford.gui.Constants.HORIZONTAL_SPACER;
import static edu.stanford.gui.Constants.VERTICAL_SPACER;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;

/**
 * Dialog to instrument the code step-by-step.
 */
public class InstrumentationDialog extends JDialog
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -1577116066537700628L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       C   O   N   S   T   A   N   T   S   //
    // --------------------------------------------------------------------- //
    /** The dialog window width in pixels */
    private static final int       WINDOW_WIDTH       = 560;
    /** The dialog window width in pixels */
    private static final int       WINDOW_HEIGHT      = 500;
    /** The size of the control panel */
    private static final Dimension CONTROL_PANEL_SIZE = new Dimension(500, 80);
    /** Font used in most GUI components */
    private static final Font      FONT               = new Font("Courier", 
                                                                 Font.PLAIN,
                                                                 12);
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The guidelines to select which one to display. This depends on the
     * instrumentation step. Index 0 is for the first step, index 1 for the
     * seconds etc.
     */
    private final String[]      guidelines;
    /**
     * The examples that are associated with the guidelines. An empty String
     * means that there is no example the the guideline with the same index.
     */
    private final String[]      examples;
    /**
     * Set with the student submitted programs
     */
    private final Set<JavaFile> submittedFiles;
    /**
     * Text area that displays the text and images
     */
    private final JTextPane     textDisplay;
    /**
     * The panel to select files for code instrumentation
     */
    private JPanel  fileSelectionPanel;
    /**
     * The index of the selected element in the guidelines array
     */
    private int     selectedIndex;
    /**
     * The current directory
     */
    private File    currentDir;
    /**
     * {@code true} if an example is currently displayed in the text area or
     * {@code false} otherwise
     */
    private boolean exampleDisplayed;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates an InstrumentationDialog.
     * 
     * @param guidelines The guidelines to select from. The selected guideline
     *                   appears on screen.
     * @param examples The examples that are associated with the guidelines. An
     *                 empty String means that there is no example the the
     *                 guideline with the same index.
     * @param backgroundColor The background color to use
     * @param submittedFiles Set with the student submitted programs
     * @param instrumentationFiles Placeholder to return the selected files for
     *                             the code instrumentation
     */
    protected InstrumentationDialog(String[]      guidelines,
                                    String[]      examples,
                                    Color         backgroundColor,
                                    Set<JavaFile> submittedFiles,
                                    Set<File>     instrumentationFiles) {
        selectedIndex        = 0;
        this.examples        = examples;
        this.submittedFiles  = submittedFiles;
        this.guidelines      = guidelines;
        processGuidelines();
        
        currentDir           = new File(CURR_DIR);
        exampleDisplayed     = false;
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(backgroundColor);
        
        // Add some space on top (before the text display)
        mainPanel.add(Box.createVerticalStrut(VERTICAL_SPACER));
        
        // Text Display
        JPanel textPanel = new JPanel();
        textPanel.setBackground(backgroundColor);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textDisplay = new JTextPane();
        textDisplay.setFont(FONT);
        JScrollPane scrollPanel = new JScrollPane(textDisplay);
        scrollPanel.setMaximumSize(
                     new Dimension(WINDOW_WIDTH - 2 * HORIZONTAL_SPACER,
                                   WINDOW_HEIGHT - CONTROL_PANEL_SIZE.height));
        textDisplay.setBackground(backgroundColor);//Color.WHITE);
        textDisplay.setEditable(false);
        textDisplay.setText(guidelines[selectedIndex]);
        textPanel.add(Box.createHorizontalStrut(HORIZONTAL_SPACER));
        textPanel.add(scrollPanel);
        textPanel.add(Box.createHorizontalStrut(HORIZONTAL_SPACER));
        mainPanel.add(textPanel);
        
        // Add some space between the text display and the control buttons
        mainPanel.add(Box.createVerticalStrut(VERTICAL_SPACER));
        
        // Control Buttons
        JPanel controlPanel = createControlPanel(backgroundColor,
                                                 instrumentationFiles);
        controlPanel.setBackground(backgroundColor);
        mainPanel.add(controlPanel);
        
        add(mainPanel);
        
        configureDialogWindow();
    }
    
    // ------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       M   E   T   H   O   D   S   //
    // ------------------------------------------------------------- //
    /**
     * Creates the control panel with the 'browse' button and the 'back/next'
     * buttons.
     * 
     * @param backgroundColor The background color to use
     * @param instrumentationFiles Selected files for the code instrumentation
     * 
     * @return the panel with the control buttons
     */
    private JPanel createControlPanel(Color      backgroundColor,
                                      Set<File>  instrumentationFiles) {
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(backgroundColor);
        controlPanel.setMaximumSize(CONTROL_PANEL_SIZE);
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(2, 0, 2, 0);
        
        
        fileSelectionPanel = new JPanel();
        int hgap = 10; // 10 pixels between components horizontally
        fileSelectionPanel.setLayout(new GridLayout(1,    // 1 row
                                                    3,    // 3 columns
                                                    hgap, // 10px between items
                                                    0));  // don't care
        fileSelectionPanel.setBackground(backgroundColor);
        
        ImageIcon addFileIcon = DelveGUIHelper.getIcon("add_file.png");
        JButton addFileButton = new JButton("Add", addFileIcon);
        ImageIcon removeFileIcon = DelveGUIHelper.getIcon("remove_file.png");
        JButton removeFileButton = new JButton("Remove", removeFileIcon);
        
        // Instrumentation file list drop-down box
        JComboBox<String> instrumentationComboBox = new JComboBox<String>();
        instrumentationComboBox.setVisible(false);
        Dimension size = new Dimension(CONTROL_PANEL_SIZE.width/3 - hgap,
                                       CONTROL_PANEL_SIZE.height/2 - hgap);
        instrumentationComboBox.setPreferredSize(size);
        instrumentationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>)e.getSource();
                String selectedFilename = (String)comboBox.getSelectedItem();
                removeFileButton.setVisible(selectedFilename != null);
            }
        });
        fileSelectionPanel.add(instrumentationComboBox);
        
        // Add file button
        addFileButton.setToolTipText("Add source code file");
        addFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File sourceFile = UIUtil.browse(addFileButton,
                                                JFileChooser.FILES_ONLY,
                                                currentDir);
                if (sourceFile != null) { // If there is a selection
                    try { 
                        FileUtil.validateFileToRead(sourceFile);
                    }
                    catch (ErrorException ee) {
                        UIUtil.showError(ee.getMessage());
                        return;
                    }
                    if (!instrumentationFiles.contains(sourceFile)) { 
                        instrumentationFiles.add(sourceFile);
                        instrumentationComboBox.addItem(sourceFile.getName());
                        instrumentationComboBox.setVisible(true);
                        removeFileButton.setVisible(true);
                    }
                    
                    currentDir = sourceFile.getParentFile();
                }
            }
        });
        addFileButton.setVisible(false);
        fileSelectionPanel.add(addFileButton);
        
        // Remove file button
        removeFileButton.setToolTipText("Remove selected source code file");
        removeFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedFile(instrumentationComboBox,
                                   instrumentationFiles);
                int numOfInstrFiles = instrumentationFiles.size();
                instrumentationComboBox.setVisible(numOfInstrFiles > 0);
                removeFileButton.setVisible(numOfInstrFiles > 0);
            }
        });
        removeFileButton.setVisible(false);
        fileSelectionPanel.add(removeFileButton);
        
        gridBagConstraints.gridx = 0;  // col = 0
        gridBagConstraints.gridy = 0;  // row = 0
        controlPanel.add(fileSelectionPanel, gridBagConstraints);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,    // 1 row
                                             3,    // 3 columns
                                             hgap, // 10px between items
                                             0));  // don't care (since 1 row)
        buttonPanel.setBackground(backgroundColor);
        
        int numOfSelections   = guidelines.length;
        JButton nextButton    = new JButton("Next");
        JButton exampleButton = new JButton("Example");
        
        // "Back" button
        ImageIcon backIcon = DelveGUIHelper.getIcon("prev_step.png");
        JButton backButton = new JButton("Back", backIcon);
        backButton.setToolTipText("Go to previous step");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex > 0) {
                    if (!exampleDisplayed) {
                        selectedIndex = selectedIndex - 1;
                    }
                    exampleDisplayed = false;
                }
                
                textDisplay.setText(guidelines[selectedIndex]);
                
                showControlButtons(addFileButton,
                                   removeFileButton,
                                   instrumentationComboBox,
                                   backButton,
                                   exampleButton,
                                   nextButton,
                                   instrumentationFiles.isEmpty());
            }
        });
        backButton.setVisible(selectedIndex > 0);
        buttonPanel.add(backButton);
        
        // "Example" button
        exampleButton.setForeground(Color.BLUE);
        exampleButton.setToolTipText("Show an example");
        exampleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showExample();
                exampleDisplayed = true;
                // Must click on "Back"
                exampleButton.setVisible(false);
                nextButton.setVisible(false);
                addFileButton.setVisible(false);
                removeFileButton.setVisible(false);
                instrumentationComboBox.setVisible(false);
            }
        });
        buttonPanel.add(exampleButton);
        
        // "Next" button
        nextButton.setHorizontalTextPosition(AbstractButton.LEADING);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == numOfSelections - 1) { // We are done
                    // Get the root component for the current component tree
                    JDialog dialog =
                            (JDialog)SwingUtilities.getRoot(nextButton);
                     dialog.dispose(); // Close the dialog
                    
                    return;
                }
                selectedIndex = selectedIndex + 1;
                
                textDisplay.setText(guidelines[selectedIndex]);
                textDisplay.setCaretPosition(0); // Scroll up to the top
                
                showControlButtons(addFileButton,
                                   removeFileButton,
                                   instrumentationComboBox,
                                   backButton,
                                   exampleButton,
                                   nextButton,
                                   instrumentationFiles.isEmpty());
            }
        });
        buttonPanel.add(nextButton);
        
        gridBagConstraints.gridx = 0;  // col = 0
        gridBagConstraints.gridy = 1;  // row = 1
        controlPanel.add(buttonPanel, gridBagConstraints);
        
        showControlButtons(addFileButton,
                           removeFileButton,
                           instrumentationComboBox,
                           backButton,
                           exampleButton,
                           nextButton,
                           instrumentationFiles.isEmpty());
        
        return controlPanel;
    }
    
    /**
     * Shows or hides the control buttons (including the drop-down menu for the
     * selected instrumentation files).
     * 
     * @param addFileButton The button to add instrumentation files
     * @param removeFileButton The button to remove the selected instrumentation
     *                         file
     * @param instrumentationComboBox The drop-down menu with the selected
     *                                instrumentation file names
     * @param backButton The button to go to the previous screen 
     * @param exampleButton The button to show an example
     * @param nextButton The button to go to the previous screen
     * @param noSelectedFiles {@code true} if the user has not selected any
     *                        files to use for code instrumentation or
     *                        {@code false} otherwise
     */
    private void showControlButtons(JButton           addFileButton,
                                    JButton           removeFileButton,
                                    JComboBox<String> instrumentationComboBox,
                                    JButton           backButton,
                                    JButton           exampleButton,
                                    JButton           nextButton,
                                    boolean           noSelectedFiles) {
        // addFileButton
        addFileButton.setVisible(selectedIndex > 0);
        
        // removeFileButton + instrumentationComboBox
        if (selectedIndex == 0 || noSelectedFiles) {
            removeFileButton.setVisible(false);
            instrumentationComboBox.setVisible(false);
        }
        else {
            removeFileButton.setVisible(true);
            instrumentationComboBox.setVisible(true);
        }
        int width   = CONTROL_PANEL_SIZE.width / 3;
        int height = CONTROL_PANEL_SIZE.height;
        instrumentationComboBox.setMaximumSize(new Dimension(width, height));
        
        // nextButton
        int numOfSelections = guidelines.length;
        if (selectedIndex < numOfSelections - 1) {
            ImageIcon nextIcon = DelveGUIHelper.getIcon("next_step.png");
            nextButton.setText("Next");
            nextButton.setIcon(nextIcon);
            nextButton.setToolTipText("Go to next step");
        }
        else {
            ImageIcon checkIcon = DelveGUIHelper.getIcon("check.png");
            nextButton.setText("Done");
            nextButton.setIcon(checkIcon);
            nextButton.setToolTipText("Done with the instrumentation");
        }
        nextButton.setVisible(true);
        
        // exampleButton
        exampleButton.setVisible(selectedIndex > 0 &&
                                 !examples[selectedIndex].isBlank());
        
        // backButton
        backButton.setVisible(selectedIndex > 0);
    }
    
    /**
     * Shows an example for the currently displayed code instrumentation step.
     */
    private void showExample() {
        String[] textSections = examples[selectedIndex].split(IMAGE_DELIMITER);
        textDisplay.setText(null); // Delete existing text
        StyledDocument document = (StyledDocument) textDisplay.getDocument();
        Style imageStyle = document.addStyle("ImageStyle", null);
        ImageIcon image;
        switch (selectedIndex) {
            case 1: // Step 1: Define 'program state'"
                 image = DelveGUIHelper.getIcon("chessboard1.png");
                 StyleConstants.setIcon(imageStyle, image);
                 try {
                     document.insertString(0, textSections[0], null);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[1],
                                           null);
                 }
                 catch (BadLocationException ignored) {
                 }
                 break;
            
            case 2: // Step 2: Replace GUI with non-GUI components
                 image = DelveGUIHelper.getIcon("chessboard2.png");
                 StyleConstants.setIcon(imageStyle, image);
                 try {
                     document.insertString(0, textSections[0], null);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[1],
                                           null);
                     
                     image = DelveGUIHelper.getIcon("chess_notation.png");
                     StyleConstants.setIcon(imageStyle, image);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[2],
                                           null);
                 }
                 catch (BadLocationException ignored) {
                 }
                 break;
            
            case 3:// Step 3: Remove user-interaction code (e.g. keyboard input)
                 image = DelveGUIHelper.getIcon("chess_user_input.png");
                 StyleConstants.setIcon(imageStyle, image);
                 try {
                     document.insertString(0, textSections[0], null);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[1],
                                           null);
                 }
                 catch (BadLocationException ignored) {
                 }
                 break;
            
            case 4: // Step 4: Replace random with deterministic behavior
                 image = DelveGUIHelper.getIcon("backgammon.png");
                 StyleConstants.setIcon(imageStyle, image);
                 try {
                     document.insertString(0, textSections[0], null);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[1],
                                           null);
                 }
                 catch (BadLocationException ignored) {
                 }
                 break;
            
            case 5: // Step 5: Identify common methods/method calls in programs
                 image = DelveGUIHelper.getIcon("capture_state1.png");
                 StyleConstants.setIcon(imageStyle, image);
                 
                 try {
                     document.insertString(0, textSections[0], null);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[1],
                                           null);
                     
                     image = DelveGUIHelper.getIcon("capture_state2.png");
                     StyleConstants.setIcon(imageStyle, image);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[2],
                                           null);
                     
                     image = DelveGUIHelper.getIcon("capture_state3.png");
                     StyleConstants.setIcon(imageStyle, image);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[3],
                                           null);
                     
                     image = DelveGUIHelper.getIcon("capture_state4.png");
                     StyleConstants.setIcon(imageStyle, image);
                     document.insertString(document.getLength(),
                                           " ", // any string that is not empty
                                           imageStyle);
                     document.insertString(document.getLength(),
                                           textSections[4],
                                           null);
                 }
                 catch (BadLocationException ignored) {
                 }
                 break;
                 
            case 7: // Step 7: Provide additional libraries, if required
                 try {
                     document.insertString(0, textSections[0], null);
                 }
                 catch (BadLocationException ignored) {
                 }
                 break;
        }
        textDisplay.setCaretPosition(0); // Scroll up to the top
    }
    
    /**
     * Identifies if there are common methods/method calls and if so it enhances
     * the provided guidelines with this extra piece of information.
     */
    private void processGuidelines() {
        Thread workerThread = new Thread() {
            @Override
            public void run() {
                // Common methods
                StringBuilder commonMethodSignatures = new StringBuilder();
                Set<String> methodSignatures =
                            DelveGUIHelper.getCommonMethods(submittedFiles);
                for (String methodSignature : methodSignatures) {
                    commonMethodSignatures.append(NEW_LINE + "     "
                                                           + methodSignature);
                }
                if (commonMethodSignatures.length() > 0) {
                    guidelines[5] = guidelines[5] + NEW_LINE
                                  + "  - Common methods in student programs: "
                                  + commonMethodSignatures;
                }
                
                // Common method calls
                StringBuilder commonMethodCallSignatures = new StringBuilder();
                Set<String> methodCallSignatures =
                            DelveGUIHelper.getCommonMethodCalls(submittedFiles);
                for (String methodCallSignature : methodCallSignatures) {
                    commonMethodCallSignatures.append(NEW_LINE + "     "
                                                    + methodCallSignature);
                }
                if (commonMethodCallSignatures.length() > 0) {
                    guidelines[5] = guidelines[5] + NEW_LINE
                               + "  - Common method calls in student programs: "
                               + commonMethodCallSignatures;
                }
            }
        };
        
        workerThread.start();
    }
    
    /**
     * Configures the dialog window.<br>
     * 1. Sets the look and feel.<br>
     * 2. Defines the behavior when the close window button is pressed<br>
     * 3. Sets the window size and location<br>
     */
    private void configureDialogWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored) {
            // Do nothing (it's ok even if we can't set the look-and-feel)
        }
        setForeground(Color.BLACK);
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        // Block all windows till we close the dialog
        setModalityType(APPLICATION_MODAL); 
        
        setTitle("Instrumentation Guidelines");
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        pack();
        
        setResizable(false); // Do not allow the user to resize the window
        setLocationRelativeTo(null); // Center the application window
        
        setIconImage(Toolkit.getDefaultToolkit().getImage(IMAGE_DIR
                                                        + File.separator
                                                        + "help.png"));
        setVisible(true);
    }
    
    /**
     * Given a pointer to the drop-down list with the instrumentation filenames,
     * it removes the selected file from that list.
     * 
     * @param comboBox The drop down menu with the instrumentation filenames
     * @param instrumentationFiles Selected files for the code instrumentation
     */
    private void removeSelectedFile(JComboBox<String> comboBox,
                                    Set<File>         instrumentationFiles) {
        String selectedFilename = (String)comboBox.getSelectedItem();
        File selectedFile = null;
        for (File file : instrumentationFiles) {
            if (file.getName().equals(selectedFilename)) {
                selectedFile = file;
            }
        }
        
        if (selectedFile != null) { // A file is selected
            instrumentationFiles.remove(selectedFile);
            comboBox.removeItem(selectedFilename);
        }
    }
}
