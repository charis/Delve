/*
 * File          : ProgressPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 18 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Panel that use a progress bar to display the progress of a task towards
 * completion.
 */
public class ProgressPanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 5142066969304920260L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * Visual display of a task progress towards completion
     */
    private final JProgressBar  progressBar;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a panel with a progress bar.
     * 
     * @param size The size of the panel
     */
    public ProgressPanel(Dimension size) {
        progressBar = new JProgressBar(0, 100); // Show percentage
        progressBar.setAlignmentY(TOP_ALIGNMENT);
        //progressBar.setMaximumSize(size);
        setMaximumSize(size);
        progressBar.setStringPainted(true); // Text to show the percentage done
        
        add(progressBar);
    }
    
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Sets the progress bar's current value.<br>
     * The value has to be between 0 (inclusive) and 100 (inclusive). A value
     * less than 0 will be translated as 0 and a value greater than 100 will be
     * translated as 100.
     * 
     * @param value The new value (between 0 and 100)
     */
    public void setValue(int value) {
        // This method is often called by a thread.
        // Use SwingUtilities.invokeLater() for safety
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(value);
            }
        });
    }
    
    /**
     * @return the progress bar's current value (i.e., value between 0
     *         (inclusive) and 100 (inclusive)).
     */
    public int getValue() {
        return progressBar.getValue();
    }
    
    /**
     * Shows or hides the progress bar.
     * 
     * @param visible {@code true} to show the progress bar or {@code false} to
     *                hide it
     */
    public void setVisible(boolean visible) {
        // This method is often called by a thread.
        // Use SwingUtilities.invokeLater() for safety
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setVisible(visible);
            }
        });
    }
    
    /**
     * Resets the progress bar (i.e., sets its value to 0%)
     * 
     * @param visible {@code true} to show the progress bar or {@code false} to
     *                hide it
     */
    public void reset(boolean visible) {
        // This method is often called by a thread.
        // Use SwingUtilities.invokeLater() for safety
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(0);
                progressBar.setVisible(visible);
            }
        });
    }
}
