/*
 * File          : DelveFileTree.java
 * Author        : Charis Charitsis
 * Creation Date : 25 December 2020
 * Last Modified : 19 September 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
// Import custom classes
import edu.stanford.delve.Constants.HighCYCMethodCount;
import edu.stanford.gui.FileTree;
import edu.stanford.gui.TreeCellRenderer;
import edu.stanford.javaparser.body.Method;

// Import constants
import static edu.stanford.gui.Constants.GREEN_COLOR;

/**
 * Display a file system under a given root directory in a JTree view.
 */
public abstract class DelveFileTree extends FileTree
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 5936066638048892473L;
    
    // ----------------------------------------------------------------- //
    // P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S //
    // ----------------------------------------------------------------- //
    /**
     * Map where the key is the snapshot file and the value is the object that
     * specifies what happened to the number of methods with high complexity
     * compared to the previous snapshot
     */
    private Map<File, HighCYCMethodCount> complexityCountMap;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a tree directory structure for the given directory.
     *  
     * @param rootDir The root directory/root node in the file tree
     * @param fileExtensions The extensions that a file should end with to be
     *                       included in the tree or {@code null} to ignore this
     *                       filter (i.e., any files extensions)
     * @param correctnessLookup Map with the submission programs as keys and
     *                          their correctness (i.e., {@code true} if
     *                          functionally correct or {@code false} otherwise)
     * @param complexMethodMap a map where the key is the snapshot file name in
     *                         the submission and the value is a list with the
     *                         complex methods in the file or an empty map if
     *                         there are no complex methods
     */
    public DelveFileTree(File                    rootDir,
                         String[]                fileExtensions,
                         Map<File, Boolean>      correctnessLookup,
                         Map<File, List<Method>> complexMethodMap) {
        super(rootDir, fileExtensions);
        complexityCountMap = new TreeMap<File, HighCYCMethodCount>();
        updateComplexityMap(complexMethodMap);
        setCellRenderer(new DelveCellRenderer(rootDir,
                                              correctnessLookup));
    }
    
    /**
     * Sets the map with the complex methods in the submission snapshots
     * 
     * @param complexMethodMap a map where the key is the snapshot file name in
     *                         the submission and the value is a list with the
     *                         complex methods in the file or an empty map if
     *                         there are no complex methods
     */
    public void updateComplexityMap(Map<File, List<Method>> complexMethodMap) {
        complexityCountMap.clear();
        complexityCountMap.putAll(
                       DelveGUIHelper.getComplexityCountMap(complexMethodMap));
    }
    
    // -------------------------------------------- //
    //   I   N   N   E   R      C   L   A   S   S   //
    // -------------------------------------------- //
    /**
     * Determines how a tree node is displayed.
     */
    private class DelveCellRenderer extends TreeCellRenderer
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
        /**
         * Image icon for non-empty directories in the tree with correct
         * submissions.
         */
        private final ImageIcon CORRECT_SUBMISSION_DIR_ICON   =
                                DelveGUIHelper.getIcon("folder-correct.png");
        /**
         * Image icon for non-empty directories in the tree with incorrect
         * submissions.
         */
        private final ImageIcon INCORRECT_SUBMISSION_DIR_ICON =
                                DelveGUIHelper.getIcon("folder-error.png");
        
        /**
         * Image icon for files in the tree with functionally correct source
         * code.
         */
        private final ImageIcon CORRECT_SOURCE_FILE_ICON      =
                                DelveGUIHelper.getIcon("file-correct.png");
        /**
         * Image icon for files in the tree with functionally incorrect source
         * code.
         */
        private final ImageIcon INCORRECT_SOURCE_FILE_ICON    =
                                DelveGUIHelper.getIcon("file-error.png");
        
        // ----------------------------------------------------------------- //
        // P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S //
        // ----------------------------------------------------------------- //
        /**
         * Map with the submission programs as keys and their correctness (i.e.,
         * {@code true} if functionally correct or {@code false} otherwise)
         */
        private final Map<File, Boolean>            correctnessLookup;
        
        // ------------------------------------------------- //
        //   C   O   N   S   T   R   U   C   T   O   R   S   //
        // ------------------------------------------------- //
        /**
         * Creates a new TreeCellRenderer which determines how a tree node is
         * displayed.
         * 
         * @param rootDir The root directory/root node in the file tree
         * @param correctnessLookup Map with the submission programs as keys and
         *                          their correctness (i.e., {@code true} if
         *                          functionally correct or {@code false}
         *                          otherwise)
         * @param highCYCLookup Map with the submission programs as keys and
         *                      their cyclomatic complexity classification
         *                      (i.e., telling if the number of high CYC methods
         *                      increased, decreased or remained the same
         *                      compared to the previous snapshot) as values.
         */
        private DelveCellRenderer(File               rootDir,
                                  Map<File, Boolean> correctnessLookup) {
            super(rootDir);
            this.correctnessLookup = correctnessLookup;
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
            
            Map<File, Boolean> dirCorrectnessLookup =
                                                   new HashMap<File, Boolean>();
            for (File sourceCodeFile : correctnessLookup.keySet()) {
                boolean isCorrect = correctnessLookup.get(sourceCodeFile);
                dirCorrectnessLookup.put(sourceCodeFile.getParentFile(),
                                         isCorrect);
            }
            
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                File fileOrDir = getFile(node, new File(getRootDirPathname()));
                if (fileOrDir.isDirectory()) {
                    if (!leaf && dirCorrectnessLookup.containsKey(fileOrDir)) {
                        boolean isCorrect = dirCorrectnessLookup.get(fileOrDir);
                        if (isCorrect) {
                            setIcon(CORRECT_SUBMISSION_DIR_ICON);
                        }
                        else {
                            setIcon(INCORRECT_SUBMISSION_DIR_ICON);
                        }
                    }
                }
                else if (fileOrDir.isFile()) {
                    if (correctnessLookup.containsKey(fileOrDir)) {
                        boolean isCorrect = correctnessLookup.get(fileOrDir);
                        if (isCorrect) {
                            setIcon(CORRECT_SOURCE_FILE_ICON);
                        }
                        else {
                            setIcon(INCORRECT_SOURCE_FILE_ICON);
                        }
                    }
                    else {
                        HighCYCMethodCount category =
                                           complexityCountMap.get(fileOrDir);
                        if (category != null) {
                            switch (category) {
                                case INCREASED:
                                     setForeground(Color.RED);
                                     break;
                                     
                                case DECREASED:
                                     setForeground(GREEN_COLOR);
                                     break;
                                     
                                case NO_CHANGE:
                                     break;
                            }
                        }
                    }
                }
            }
            
            return this;
        }
    }
}
