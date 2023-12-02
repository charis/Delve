/*
 * File          : UIUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 1 December 2019
 * Last Modified : 5 January 2021
 */
package edu.stanford.util;

// Import Java SE classes
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Utility class for UI related operations.
 */
public class UIUtil
{
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Retrieves an icon from a PNG image
     * 
     * @param iconPathname The PNG image pathname
     * 
     * @return the corresponding icon or null if the image is not found
     */
    public static ImageIcon getIcon(String iconPathname) {
        File iconFile = new File(iconPathname);
        
        try{
            iconPathname = iconFile.getCanonicalPath();
        }
        catch (IOException ioe) {
            return null;
        }
        
        // Make sure that the icon exists => otherwise return null
        if (!iconFile.exists()) { 
            return null;
        }
        
        return new ImageIcon(iconPathname);
    }
    
    /**
     * Retrieves an icon from resource name that is loaded by the system loader.
     * The resource name should be expressed as relative path in respect to the
     * loaded location.<br>
     * For example, if we load a jar file with a sub-directory 'img' and we want
     * to load the icon for 'browse.png', we should pass {@code img/browse.png}
     * as argument for the {@code resourceName}. 
     * 
     * @param resourceName The resource name expressed as relative path in the
     *                     loaded location
     * 
     * @return the corresponding icon or null if the image is not found
     */
    public static ImageIcon loadIcon(String resourceName) {
        URL url = ClassLoader.getSystemClassLoader().getResource(resourceName);
        if (url == null) {
            return null;
        }
        
        return new ImageIcon(url);
    }
    
    /**
     * Selects a file or a directory.
     * 
     * @param parent The parent component of the dialog or {@code null} to show
     *               the browse file dialog in a look-and-feel-dependent
     *               position such as the center of the screen
     * @param seclectionMode The type of files to be displayed: <br>
     *                       - JFileChooser.FILES_ONLY <br>
     *                       - JFileChooser.DIRECTORIES_ONLY <br>
     *                       - JFileChooser.FILES_AND_DIRECTORIES <br>
     * @param currentDir The current directory to point to when the browse
     *                   dialog appears
     * 
     * @return the file or the directory that the user selected
     */
    public static File browse(Component parent,
                              int       seclectionMode,
                              File      currentDir) {
        JFileChooser chooser = new JFileChooser();
        
        // Sets the string that goes in the JFileChooser window's title bar
        // so that it tells the user what to do (i.e., browse...)
        chooser.setDialogTitle("Browse..");
        
        // Use as default path the project directory
        File defaultDir = null;
        try {
            defaultDir = new File(new File(".").getCanonicalPath());
        }
        catch (IOException ignored) {
            // Do nothing
        }
        
        if (currentDir == null) {
            currentDir = defaultDir;
        }
        chooser.setCurrentDirectory(currentDir);
        
        // Sets the JFileChooser to allow the user to select both directories
        // and files (which is set by default, but let's make it explicit)
        chooser.setFileSelectionMode(seclectionMode);
        
        // Pops up a "Browse..." file chooser dialog over the JFrame
        int openStatus = chooser.showOpenDialog(parent);
        
        // When the user clicks on the "Browse" button get the file
        if (openStatus == JFileChooser.APPROVE_OPTION)  {
            return chooser.getSelectedFile();
        }
        else {
            return null;
        }
    }
    
    /**
     * Shows an information message window over the GUI.
     * 
     * @param message The message to display
     * @param windowTitle The title of the window with the information message
     */
    public static void showInformation(String message,
                                       String windowTitle) {
        JOptionPane.showMessageDialog(null,
                                      message,
                                      windowTitle,
                                      JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Shows an error message window over the GUI.
     * 
     * @param errorMessage the error message that the user sees
     */
    public static void showError(String errorMessage) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      errorMessage,
                                      "Error",
                                      JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Brings up a window with an error message.
     * 
     * @param errorMessage The error message to display
     * @param windowTitle The title of the window with the error
     */
    public static void showError(String errorMessage,
                                 String windowTitle) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      errorMessage,
                                      windowTitle,
                                      JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Brings up a window with a warning message.
     * 
     * @param warningMessage The warning message to display
     * @param windowTitle The title of the window with the warning
     */
    public static void showWarning(String warningMessage,
                                   String windowTitle) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      warningMessage,
                                      windowTitle,
                                      JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Brings up a window with a message asking the user to confirm.
     * 
     * @param message The message to display
     * @param windowTitle The title of the window with the confirm dialog
     * 
     * @return {@code true} if the user selects YES or {@code false} if the user
     *         selects NO 
     */
    public static boolean showConfirmMessage(String message,
                                             String windowTitle){
        Toolkit.getDefaultToolkit().beep();
        int retVal = JOptionPane.showConfirmDialog(null,
                                                   message,
                                                   windowTitle,
                                                   JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.WARNING_MESSAGE);
        return retVal == 0; // true if the user selects 'yes'; false otherwise
    }
        
    /**
     * Brings up a window with a message asking the user to confirm. The user
     * can also cancel the operation.
     * 
     * @param message The message to display
     * @param windowTitle The title of the window with the confirm dialog
     * 
     * @return {@code 0} if the user selects YES, {@code 1} if the user selects
     *         NO or {@code 2} if the user selects CANCEL. 
     */
    public static int showConfirmMessageWithCancelOption(String message,
                                                         String windowTitle) {
        Toolkit.getDefaultToolkit().beep();
        return JOptionPane.showConfirmDialog(null,
                                             message,
                                             windowTitle,
                                             JOptionPane.YES_NO_CANCEL_OPTION);
    }
    
    /**
     * Adds undo/redo capacity to the given a text component.
     * 
     * @param component The text component to add undo/redo capacity to it
     * 
     * @return the module that manages the text edits, providing a way to undo
     *         or redo the appropriate edits
     */
    public static UndoManager addUndoRedoFunctionality(JTextComponent component)
    {
        UndoManager undoManager = new UndoManager();
        Document document = component.getDocument();
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        
        // Listen for undo and redo events
        document.addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });
        
        // ---------------------------------------- //
        // ---      U      N      D      O      --- //
        // ---------------------------------------- //
        // Create an undo action and add it to the text component
        component.getActionMap().put("Undo", new AbstractAction("Undo") {
            /** Universal version identifier for this Serializable class */
            private static final long serialVersionUID = 9095693355192621171L;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                }
                catch (CannotUndoException ignored) {
                    // Nothing to do
                }
            }
        });
        
        // Bind the undo action to ctl-Z (or command-Z on mac)
        KeyStroke undoKeyStroke =
             KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                                    defaultToolkit.getMenuShortcutKeyMaskEx());
        component.getInputMap().put(undoKeyStroke, "Undo");
        
        // ---------------------------------------- //
        // ---      R      E      D      O      --- //
        // ---------------------------------------- //
        // Create a redo action and add it to the text component
        // Create a redo action and add it to the text component
        component.getActionMap().put("Redo", new AbstractAction("Redo") {
            /** Universal version identifier for this Serializable class */
            private static final long serialVersionUID = -4896844407595143233L;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                }
                catch (CannotRedoException ignored) {
                }
            }
        });
        
        // Bind the redo action to ctl-Y (or command-Y on mac)
        KeyStroke redoKeyStroke =
              KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                                     defaultToolkit.getMenuShortcutKeyMaskEx());
        component.getInputMap().put(redoKeyStroke, "Redo");
        
        return undoManager;
    }
}
