/*
 * File          : CleanDirVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 12 September 2013
 * Last Modified : 19 August 2018
 */
package edu.stanford.util.filesystem.helper;

//Import Java SE classes
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
* visit each directory in a directory tree to delete only the regular files
* from a directory tree.
*/
public class CleanDirVisitor extends SimpleFileVisitor<Path>
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
     * 
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
}
