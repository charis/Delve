/*
 * File          : FileTree.java
 * Author        : Charis Charitsis
 * Creation Date : 5 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.BorderLayout;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.util.UIUtil;

/**
 * Displays the directories and files under a root directory in a JTree view.
 */
public abstract class FileTree extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -6946935528379107342L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * Module that displays a set of hierarchical data as an outline.<br>
     * In this case the data is the directories and files (any level deep)
     * under a root directory.
     */
    private final JTree tree;
    /**
     * The root directory in the file tree
     */
    private final File  rootDir;
     
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a FileTree (i.e., directory structure as a tree) for the given
     * directory.
     *  
     * @param rootDir The root directory/root node in the file tree
     */
    public FileTree(File rootDir) {
        this(rootDir, null, null);
    }
    
    /**
     * Creates a FileTree (i.e., directory structure as a tree) for the given
     * directory.
     *  
     * @param rootDir The root directory/root node in the file tree
     * @param includeSet Set with the files and directories that are considered
     *                   (any file or directory not in the set is ignored) or
     *                   {@code null} to not apply this filter (i.e., include
     *                   all files and directories under dir)
     */
    public FileTree(File      rootDir,
                    Set<File> includeSet) {
        this(rootDir, includeSet, null);
    }
    
    /**
     * Creates a FileTree (i.e., directory structure as a tree) for the given
     * directory.
     *  
     * @param rootDir The root directory/root node in the file tree
     * @param fileExtensions The extensions that a file should end with to be
     *                       included in the tree or {@code null} to ignore this
     *                       filter (i.e., any files extensions)
     */
    public FileTree(File     rootDir,
                    String[] fileExtensions) {
        
        this(rootDir, null, fileExtensions);
    }
    
    /**
     * Creates a FileTree (i.e., directory structure as a tree) for the given
     * directory.
     *  
     * @param rootDir The root directory/root node in the file tree
     * @param includeSet Set with the files and directories that are considered
     *                   (any file or directory not in the set is ignored) or
     *                   {@code null} to not apply this filter (i.e., include
     *                   all files and directories under dir)
     * @param fileExtensions The extensions that a file should end with to be
     *                       included in the tree or {@code null} to ignore this
     *                       filter (i.e., any files extensions)
     */
    private FileTree(File      rootDir,
                     Set<File> includeSet,
                     String[]  fileExtensions) {
        if (rootDir == null) {
            throw new IllegalArgumentException("Argument 'rootDir' is null");
        }
        this.rootDir = rootDir;
        setLayout(new BorderLayout());
        
        // Make a tree list with all the nodes, and make it a JTree
        DefaultMutableTreeNode root = addNodesUnderDir(null,    // rootNode
                                                       rootDir, 
                                                       includeSet,
                                                       fileExtensions);
        tree = new JTree(root);
        setCellRenderer(new TreeCellRenderer(rootDir));
        
        // Add a listener
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                Object[] subpaths = e.getPath().getPath();
                StringBuilder pathname = new StringBuilder();
                pathname.append(rootDir.getPath());
                for (int i = 1; i < subpaths.length; i++) {
                    pathname.append(File.separator + subpaths[i]);
                }
                File selection = new File(pathname.toString());
                try {
                    nodeSelected(selection);
                }
                catch (ErrorException ee) {
                    String errorMsg = ee.getMessage();
                    UIUtil.showError(errorMsg,
                                     "Error for '" + pathname + "'");
                }
            }
        });
        
        // Lastly, put the JTree into a JScrollPane.
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(tree);
        add(BorderLayout.CENTER, scrollPane);
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * @return the root directory in the file tree
     */
    public File getRootDir() {
        return rootDir;
    }
    
    /**
     * @return the selected directory or file in the file tree or {@code null}
     *         if no item is selected
     */
    public File getSelectedItem() {
        DefaultMutableTreeNode selectedNode = 
                    (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        
        return getFile(selectedNode, rootDir);
    }
    
    /**
     * Notification that the given file or directory node is selected
     * 
     * @param selection The selected file or directory
     * 
     * @throws ErrorException in case of an error performing the action
     */
    abstract public void nodeSelected(File selection)
             throws ErrorException;
    
    /**
     * Sets the {@code TreeCellRenderer} that will be used to draw each cell.
     *
     * @param cellRenderer The {@code TreeCellRenderer} that is to render each
     *                     cell
     */
    public void setCellRenderer(TreeCellRenderer cellRenderer) {
         tree.setCellRenderer(cellRenderer);
    }
    
    /**
     * Returns the file or directory that maps to the given tree node.
     * 
     * @param node The tree node to get the file or directory for
     * @param rootDir The root directory
     * 
     * @return the file or directory for the given tree node or {@code null}
     *         in case the tree node is {@code null}
     */
    public static File getFile(DefaultMutableTreeNode node,
                               File                   rootDir) {
        if (rootDir == null) {
            throw new IllegalArgumentException("Argument 'rootDir' is null");
        }
        
        if (node == null) {
            return null;
        }
        
        TreeNode[] treeNodes = node.getPath();
        StringBuilder pathname = new StringBuilder();
        pathname.append(rootDir.getPath());
        for (int i = 1; i < treeNodes.length; i++) {
            pathname.append(File.separator + treeNodes[i]);
        }
        File fileOrDir = new File(pathname.toString());
        
        return fileOrDir;
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Recursive method that adds the nodes under the given directory
     * {@code dir} into the current root {@code rootNode}<br>
     * 
     * @param rootNode The root node into which the nodes are added or
     *                 {@code null} for the root node
     * @param dir The directory whose nodes are added into the root node
     * @param includeSet Set with the files and directories that are considered
     *                   (any file or directory not in the set is ignored) or
     *                   {@code null} to not apply this filter (i.e., include
     *                   all files and directories under dir) 
     * @param fileExtensions The extensions that a file should end with to be
     *                       included in the tree or {@code null} to ignore this
     *                       filter (i.e., any files extensions)
     * 
     * @return the node for the provided directory which is added recursively to
     *         the root node
     */
    private DefaultMutableTreeNode
                   addNodesUnderDir(DefaultMutableTreeNode rootNode,
                                    File                   dir,
                                    Set<File>              includeSet,
                                    String[]               fileExtensions) {
        DefaultMutableTreeNode dirNode =
                               new DefaultMutableTreeNode(dir.getName());
        if (rootNode != null) { // root node
            rootNode.add(dirNode);
        }
        
        // Files and directories under dir
        Set<File> files  = new TreeSet<File>();
        Set<File> subdirs = new TreeSet<File>();
        for (File fileOrSubdir : dir.listFiles()) {
            boolean include = includeSet == null ||
                              includeSet.contains(fileOrSubdir);
            boolean isFile = fileOrSubdir.isFile();
            if (isFile && include) {
                if (fileExtensions != null) {
                    include = false;
                    for (String fileExtension : fileExtensions) {
                        if (fileOrSubdir.getName().endsWith(fileExtension)) {
                            include = true;
                            break;
                        }
                    }
                }
            }
            
            if (include) {
                if (isFile) {
                    files.add(fileOrSubdir);
                }
                else {
                    subdirs.add(fileOrSubdir);
                }
            }
        }
        
        // Process first the directories (recursively)
        for (File subdir : subdirs) {
            addNodesUnderDir(dirNode,
                             subdir,
                             includeSet,
                             fileExtensions);
        }
        // Now process all files
        for (File file : files) {
            dirNode.add(new DefaultMutableTreeNode(file.getName()));
        }
        
        return dirNode;
    }
}
