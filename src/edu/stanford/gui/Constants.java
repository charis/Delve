/*
 * File          : Constants.java
 * Author        : Charis Charitsis
 * Creation Date : 20 November 2019
 * Last Modified : 21 September 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Color;
import java.awt.Font;
import java.io.File;
// Import constants
import static edu.stanford.constants.Constants.RESOURCE_DIR;

/**
 * Contains constants used for file system related operations
 */
public class Constants
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** Image repository location */
    public static final String IMAGE_REPOSITORY  = RESOURCE_DIR + File.separator
                                                                + "img";
    /**
     * The name of the package with the images (that is part of the source code
     * packages)
     */
    public static final String IMAGE_PACKGAGE    = "img";
    /** Horizontal space in pixels between the GUI control components */
    public static final int    HORIZONTAL_SPACER = 20;
    /** Vertical space in pixels between the GUI control components */
    public static final int    VERTICAL_SPACER   = 20;
    /** The height for the GUI components */
    public static final int    JCOMPONENT_HEIGHT = 30;
    
    /** Font used in most GUI components */
    public static final Font   FONT              = new Font("Calibri", 
                                                            Font.PLAIN,
                                                            12);
    /** Green color */
    public static final Color  GREEN_COLOR       = new Color(8, 132, 34);
    /** Magenta color */
    public static final Color  MAGENTA_COLOR     = new Color(150, 0, 85);
}
