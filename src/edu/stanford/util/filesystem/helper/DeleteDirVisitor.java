/*
 * File          : DeleteDirVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 12 September 2013
 * Last Modified : 19 August 2018
 */
package edu.stanford.util.filesystem.helper;

// Import Java SE classes
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/*
 * M i n i  -  T u t o r i a l: see CopyDirVisitor.java
 */
/**
 * A visitor of directories to provide to the Files.walkFileTree methods to
 * visit each directory in a directory tree to delete a directory tree.
 */
public class DeleteDirVisitor extends SimpleFileVisitor<Path>
{
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Invoked when visiting a file in a directory traversal to remove it from
     * the file system.
     *
     * @param file A reference to the file
     * @param attrs The file's basic attributes
     * @return the visit result
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
           throws IOException {
        Files.delete(file);
        
        return FileVisitResult.CONTINUE;
    }
    
    /**
     * Invoked for a directory after entries in the directory, and all of their
     * descendants, have been visited to remove this directory from the file
     * system.<br>
     * This method is also invoked when iteration of the directory completes
     * prematurely (by a visitFile method returning SKIP_SIBLINGS, or an I/O
     * error when iterating over the directory). In this case the directory is
     * not removed (as it is not empty).
     * 
     * @param dir A reference to the directory
     * @param exc {@code null} if the iteration of the directory completes
     *            without an error; otherwise the I/O exception that caused the
     *            iteration of the directory to complete prematurely
     * 
     * @return the visit result
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
           throws IOException {
        if (exc == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
        
        throw exc;
    }
}
