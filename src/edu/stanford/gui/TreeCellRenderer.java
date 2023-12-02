/*
 * File          : FileTree.java
 * Author        : Charis Charitsis
 * Creation Date : 5 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
// Import custom classes
import edu.stanford.util.UIUtil;
// Import constants
import static edu.stanford.constants.Constants.LOAD_IMAGES_FROM_CLASSPATH;
import static edu.stanford.gui.Constants.IMAGE_PACKGAGE;
import static edu.stanford.gui.Constants.IMAGE_REPOSITORY;

/**
 * Determines how a tree node is displayed.
 */
public class TreeCellRenderer extends DefaultTreeCellRenderer
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 1531699149333909L;
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** Image icon for empty directories in the tree */
    private static final ImageIcon EMPTY_DIR_ICON = getIcon("empty_folder.png");
    /** Image icon for non-empty directories in the tree */
    private static final ImageIcon DIR_ICON       = getIcon("folder.png");
    /** Image icon for files in the tree */
    private static final ImageIcon FILE_ICON      = getIcon("file.png");
    
    // ----------------------------------------------------------------- //
    // P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S //
    // ----------------------------------------------------------------- //
    /**
     * The pathname for the root directory in the file tree
     */
    private final String rootDirPathname;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new TreeCellRenderer which determines how a tree node is
     * displayed.
     * 
     * @param rootDir The root directory/root node in the file tree
     */
    public TreeCellRenderer(File rootDir) {
        String pathname;
        try {
            pathname = rootDir.getCanonicalPath();
        }
        catch (IOException ioe) {
            pathname = rootDir.getPath();
        }
        
        rootDirPathname = pathname;
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    @Override
    public Component getTreeCellRendererComponent(JTree   tree,
                                                  Object  value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int     row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree,
                                           value,
                                           sel,
                                           expanded,
                                           leaf,
                                           row,
                                           hasFocus);
        
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            File fileOrDir = FileTree.getFile(node, new File(rootDirPathname));
            if (fileOrDir.isDirectory()) {
                if (leaf) {
                    setIcon(EMPTY_DIR_ICON);
                }
                else {
                    setIcon(DIR_ICON);
                }
            }
            else if (fileOrDir.isFile()) {
                setIcon(FILE_ICON);
            }
        }
        
        return this;
    }
    
    /**
     * @return the pathname for the root directory in the file tree
     */
    public String getRootDirPathname() {
        return rootDirPathname;
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Given a filename it returns the corresponding image icon or {@code null}
     * if the image icon is not found.
     * 
     * @param filename The image icon filename
     * 
     * @return the corresponding image icon or {@code null} if the image icon is
     *         not found
     */
    private static ImageIcon getIcon(String filename) {
        if (LOAD_IMAGES_FROM_CLASSPATH) {
            return UIUtil.loadIcon(IMAGE_PACKGAGE + "/" + filename);
        }
        else {
            return UIUtil.getIcon(IMAGE_REPOSITORY + File.separator + filename);
        }
    }
}
