/*
 * File          : ConsolePanel.java
 * Author        : Charis Charitsis
 * Creation Date : 21 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 * Makes it possible to attach a Java console to an application.<br>
 * This console shows all errors and other output made by the application.<br>
 * It works by redirecting System.out and System.err to the textArea of the
 * Console.
 */
public class ConsolePanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -549060612276720773L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The area with the console text
     */
    private final JTextArea   consoleTextArea;
    /**
     * A scroll bar wrapper to the console text area
     */
    private final JScrollPane scrollPane;
    /**
     * The output stream
     */
    private final PrintStream outStream;
    /**
     * The error stream
     */
    private final PrintStream errStream;
    /**
     * The text color (unless it is an error which is in red)
     */
    private final Color       textColor;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new ConsolePanel
     * 
     * @param textColor The text color or {@code null} for default color (i.e.,
     *                  white)
     * @param backgroundColor The background color or {@code null} for default
     *                        color (i.e.,  black)
     * @param showErrStream {@code true} to redirect the error stream to the
     *                      console or {@code false} otherwise
     */
    public ConsolePanel(Color   textColor,
                        Color   backgroundColor,
                        boolean showErrStream) {
        // Create and configure the text area for the console
        consoleTextArea = new JTextArea("");
        consoleTextArea.setEditable(false);
        consoleTextArea.setLineWrap(true);
        if (textColor != null) {
            consoleTextArea.setBackground(backgroundColor);
        }
        
        this.textColor = textColor == null ? Color.WHITE : textColor; 
        
        // Add a scroll bar to the console text area
        scrollPane = new JScrollPane(consoleTextArea);
        scrollPane.setBorder(new EmptyBorder(8, 8, 8, 8));
        scrollPane.setVerticalScrollBarPolicy(
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        setLayout(new GridLayout());
        add(scrollPane);
        
        // Output Stream
        outStream = new PrintStream(new OutputStream() {
            @Override
            public void write (int b)
                   throws IOException {
                append(new String(Character.toChars(b)));
            }
        });
        System.setOut(outStream);
        
        // Error Stream
        if (showErrStream) {
            errStream = new PrintStream(new OutputStream() {
                @Override
                public void write (int b)
                       throws IOException {
                    append(new String(Character.toChars(b)));
                }
            });
            System.setErr(errStream);
        }
        else {
            errStream = null;
        }
        
        scrollPane.setVisible(true);
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Clears the console text.
     */
    public void clearText() {
        consoleTextArea.setForeground(textColor);
        consoleTextArea.setText(null);
    }
    
    /**
     * Sets the console text.
     * The previous console text content is erased.<br>
     * It the provided text is {@code null} or empty, it has no effect (as if
     * this method was never called).
     * 
     * @param text The new console text
     * 
     * @see #clearText()
     */
    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        consoleTextArea.setForeground(textColor);
        consoleTextArea.setText(text);
    }
    
    /**
     * Sets the console text to the provided error text.<br>
     * The previous console text content is erased.<br>
     * It the provided error text is {@code null} or empty, it has no effect
     * (as if this method was never called).
     * 
     * @param text The error text to display in the console
     */
    public void setErrorText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        consoleTextArea.setForeground(Color.RED);
        consoleTextArea.setText(text);
    }
    
    /**
     * Sets the specified boolean to indicate whether or not the console text
     * area is editable.
     * 
     * @param flag {@code true} to make the console text area editable or
     *             {@code false} to make it non-editable
     */
    public void setEditable(boolean flag) {
        consoleTextArea.setForeground(textColor);
        consoleTextArea.setEditable(flag);
    }
    
    /**
     * Appends the given text to the end of the console text.<br>
     * If the provided text is {@code null} or empty it has no effect.
     *
     * @param text The text to insert
     */
    public void append(String text) {
        consoleTextArea.setForeground(textColor);
        consoleTextArea.append(text);
    }
    
    /**
     * Prepends the given number before console text.<br>
     *
     * @param number The number to prepend
     */
    public void append(int number) {
        consoleTextArea.setForeground(this.textColor);
        consoleTextArea.append(String.valueOf(number));
    }
    
    /**
     * Appends the given number to the end of the console text.<br>
     *
     * @param number The number to insert
     */
    public void append(double number) {
        consoleTextArea.setForeground(textColor);
        consoleTextArea.append(String.valueOf(number));
    }
    
    /**
     * Appends the given boolean to the end of the console text.<br>
     *
     * @param bool The boolean value to insert
     */
    public void append(boolean bool) {
        consoleTextArea.setForeground(textColor);
        consoleTextArea.append(String.valueOf(bool));
    }
    
    /**
     * @return the console text
     */
    public String getText() {
        return consoleTextArea.getText();
    }
}
