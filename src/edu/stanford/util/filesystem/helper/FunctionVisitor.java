/*
 * File          : FunctionVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 12 September 2013
 * Last Modified : 1 July  2018
 */
package edu.stanford.util.filesystem.helper;

// Import Java SE classes
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;

/*
 * M i n i  -  T u t o r i a l: see CopyDirVisitor.java
 */
/**
 * A visitor of directories to provide to the Files.walkFileTree methods to
 * visit each directory in a directory tree to delete a directory tree.
 */
public class FunctionVisitor extends SimpleFileVisitor<Path>
{
}
