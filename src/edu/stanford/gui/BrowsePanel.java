/*
 * File          : BrowsePanel.java
 * Author        : Charis Charitsis
 * Creation Date : 20 November 2019
 * Last Modified : 5 January 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.util.UIUtil;
// Import constants
import static edu.stanford.constants.Constants.LOAD_IMAGES_FROM_CLASSPATH;
import static edu.stanford.gui.Constants.FONT;
import static edu.stanford.gui.Constants.HORIZONTAL_SPACER;
import static edu.stanford.gui.Constants.IMAGE_PACKGAGE;
import static edu.stanford.gui.Constants.IMAGE_REPOSITORY;
import static edu.stanford.gui.Constants.JCOMPONENT_HEIGHT;

/**
 * Panel to browse files and directories.
 */
abstract public class BrowsePanel extends JPanel
{
    // ----------------------------------------------------------------- //
    //   P   U   B   L   I   C       C   O   N   S   T   A   N   T   S   //
    // ----------------------------------------------------------------- //
    /** The label size for the title that accompanies the browse button */
    public static final Dimension TITLE_LABEL_SIZE  = new Dimension(300,
                                                             JCOMPONENT_HEIGHT);
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * Universal version identifier for this Serializable class.<br>
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 1163083364259464316L;
    
    /**
     * The parent component of the dialog or {@code null} to show the browse
     * file dialog in a look-and-feel-dependent position such as the center of
     * the screen
     */
    private final Component parent;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new BrowsePanel to browse files and/or directories.
     * 
     * @param title The label text that accompanies the browse button or
     *              {@code null} for no text label
     * @param tooltipText Tool tip text to display or {@code null} to turn off
     *                    the tool tip
     * @param seclectionMode The type of files to be displayed: <br>
     *                       - JFileChooser.FILES_ONLY <br>
     *                       - JFileChooser.DIRECTORIES_ONLY <br>
     *                       - JFileChooser.FILES_AND_DIRECTORIES <br>
     * @param currentDir The current directory to point to when the browse
     *                   dialog appears
     * @param parent The parent component of the dialog or {@code null} to show
     *               the browse file dialog in a look-and-feel-dependent
     *               position such as the center of the screen
     */
    public BrowsePanel(String    title,
                       String    tooltipText,
                       int       seclectionMode,
                       File      currentDir,
                       Component parent) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        // Create title label
        JLabel titleLabel = null;
        if (title != null) {
            titleLabel = new JLabel(title);
            titleLabel.setPreferredSize(TITLE_LABEL_SIZE);
        }
        
        ImageIcon browseIcon;
        if (LOAD_IMAGES_FROM_CLASSPATH) {
            browseIcon = UIUtil.loadIcon(IMAGE_PACKGAGE + "/browse.png");
        }
        else {
            browseIcon = UIUtil.getIcon(IMAGE_REPOSITORY + File.separator
                                                         + "browse.png");
        }
        
        // Create browse button
        JButton browseButton = new JButton("Browse", browseIcon);
        browseButton.setFont(FONT);
        browseButton.setToolTipText(tooltipText);
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File selection = browse(seclectionMode, currentDir);
                try {
                    performAction(selection);
                }
                catch (ErrorException ee) {
                    String errorMsg = ee.getMessage();
                    UIUtil.showError(errorMsg,
                                     "Error processing submission dir");
                }
            }
        });
        
        // Now add the components
        if (titleLabel != null) {
            add(Box.createHorizontalStrut(HORIZONTAL_SPACER));
            add(titleLabel);
            add(Box.createHorizontalStrut(HORIZONTAL_SPACER));
            add(browseButton);
            add(Box.createHorizontalStrut(HORIZONTAL_SPACER));
            this.setAlignmentX(LEFT_ALIGNMENT);
        }
        else {
            add(browseButton);
            this.setAlignmentX(CENTER_ALIGNMENT);
        }
        
        this.parent = parent;
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * The action that is performed when a file or directory is selected (after
     * browsing)
     * 
     * @param selection The selected file or directory
     * 
     * @throws ErrorException in case of an error performing the action
     */
    abstract public void performAction(File selection)
             throws ErrorException;
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Selects a file or a directory
     * 
     * @param seclectionMode The type of files to be displayed: <br>
     *                       - JFileChooser.FILES_ONLY <br>
     *                       - JFileChooser.DIRECTORIES_ONLY <br>
     *                       - JFileChooser.FILES_AND_DIRECTORIES <br>
     * @param currentDir The current directory to point to when the browse
     *                   dialog appears
     * 
     * @return the file or the directory that the user selected
     */
    private File browse(int seclectionMode, File currentDir) {
        return UIUtil.browse(parent, seclectionMode, currentDir);
    }
}
