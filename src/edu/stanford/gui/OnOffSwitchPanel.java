/*
 * File          : OnOffSwitchPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 14 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
//Import Java SE classes
import javax.swing.JPanel;
// Import constants
import static edu.stanford.gui.Constants.GREEN_COLOR;

/**
 * Panel with a switch that consists of two buttons (on/off).
 */
public class OnOffSwitchPanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 668260981817581984L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The button to press to set the state to 'on'
     */
    private final JButton  onButton;
    /**
     * The button to press to set the state to 'off'
     */
    private final JButton  offButton;
    /** 
     * {@code true} if the buttons are locked or {@code false} otherwise.<br>
     * Locking the buttons is useful when there is a running thread that we want
     * to complete (or terminate).
     */
    private       boolean  locked;

    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new OnOffSwitchPanel to choose between two states (ON/OFF).<br>
     * By default the 'ON' button is enabled and the 'OFF' button is disabled.
     * 
     * @param title It not {@code null} titled border with the specified title
     *              is added to the panel
     * @param onTooltipText The text to display in a tool tip when the cursor
     *                      lingers over the 'ON' button or {@code null} for no
     *                      such text
     * @param onActionListener The action listener that determines what needs to
     *                         be done when the 'on' button is pressed
     * @param offTooltipText The text to display in a tool tip when the cursor
     *                       lingers over the 'OFF' button or {@code null} for
     *                       no such text
     * @param offActionListener The action listener that determines what needs
     *                          to be done when the 'off' button is pressed
     * @param buttonSize The size for each of the two switch buttons
     */
    public OnOffSwitchPanel(String         title,
                            String         onTooltipText,
                            ActionListener onActionListener,
                            String         offTooltipText,
                            ActionListener offActionListener,
                            Dimension      buttonSize) {
        this(title,
             "ON",
             null,
             onTooltipText,
             onActionListener,
             "OFF",
             null,
             offTooltipText,
             offActionListener,
             buttonSize);
    }
    
    /**
     * Creates a new OnOffSwitchPanel to choose between two states (ON/OFF).<br>
     * By default the 'ON' button is enabled and the 'OFF' button is disabled.
     * 
     * @param title It not {@code null} titled border with the specified title
     *              is added to the panel
     * @param onLabel The label for the 'ON button or {@code null} for no label
     * @param onIcon The Icon image to display on the 'ON' button or
     *               {@code null} for no icon image
     * @param onTooltipText The text to display in a tool tip when the cursor
     *                      lingers over the 'ON' button or {@code null} for no
     *                      such text
     * @param onActionListener The action listener that determines what needs to
     *                         be done when the 'ON' button is pressed
     * @param offLabel The label for the 'OFF button or {@code null} for no
     *                 label
     * @param offIcon The Icon image to display on the 'OFF' button or
     *                {@code null} for no icon image
     * @param offTooltipText The text to display in a tool tip when the cursor
     *                       lingers over the 'OFF' button or {@code null} for
     *                       no such text
     * @param offActionListener The action listener that determines what needs
     *                          to be done when the 'OFF' button is pressed
     * @param buttonSize The size for each of the two switch buttons
     */
    public OnOffSwitchPanel(String         title,
                            String         onLabel,
                            ImageIcon      onIcon,
                            String         onTooltipText,
                            ActionListener onActionListener,
                            String         offLabel,
                            ImageIcon      offIcon,
                            String         offTooltipText,
                            ActionListener offActionListener,
                            Dimension      buttonSize) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        if (title != null) {
            setBorder(BorderFactory.createTitledBorder(title));
        }
        
        // "ON" Button
        onButton = new JButton(onLabel, onIcon);
        onButton.setForeground(GREEN_COLOR);
        onButton.setPreferredSize(buttonSize);
        if (onTooltipText != null && !onTooltipText.trim().isEmpty()) {
            onButton.setToolTipText(onTooltipText);
        }
        onButton.addActionListener(onActionListener);
        onButton.setEnabled(true); // By default: enabled
        
        // "OFF" Button
        offButton = new JButton(offLabel, offIcon);
        offButton.setPreferredSize(buttonSize);
        offButton.setForeground(Color.RED);
        if (offTooltipText != null && !offTooltipText.trim().isEmpty()) {
            offButton.setToolTipText(offTooltipText);
        }
        offButton.addActionListener(offActionListener);
        offButton.setEnabled(false); // By default: disabled
        
        add(onButton);
        add(offButton);
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Enables the 'ON' button and disables the 'OFF' button.
     */
    public void enableOnButton() {
        if (!locked) {
            onButton.setEnabled(true);
            offButton.setEnabled(false);
        }
    }
    
    /**
     * Enables the 'OFF' button and disables the 'ON' button.
     */
    public void enableOffButton() {
        if (!locked) {
            onButton.setEnabled(false);
            offButton.setEnabled(true);
        }
    }
    
    /**
     * Locks the buttons so that their state does not change (enable/disable)
     * until they get unlocked.
     * 
     * @see #unlockButtons()
     */
    public void lockButtons() {
        locked = true;
    }
    
    /**
     * Unlocks the buttons so that their state can change again.
     *  
     * @see #lockButtons()
     */
    public void unlockButtons() {
        locked = false;
    }
    
    /**
     * Disables both the 'ON' and the 'OFF' buttons
     */
    public void disableButtons() {
        if (!locked) {
            onButton.setEnabled(false);
            offButton.setEnabled(false);
        }
    }
    
    /**
     * Returns {@code true} if the buttons are locked or {@code false}
     * otherwise.<br>
     * Locking the buttons is useful when there is a running thread that we want
     * to complete (or terminate).
     * 
     * @return {@code true} if the buttons are locked or {@code false} otherwise
     */
    public boolean isLocked() {
        return locked;
    }
}
