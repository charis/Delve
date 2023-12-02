/*
 * File          : JavaSourceCodeObject.java
 * Author        : Charis Charitsis
 * Creation Date : 5 February 2019
 * Last Modified : 1 January 2021
 */
package edu.stanford.java.compile;

// Import Java SE classes
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.util.io.FileIOUtil;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;

/**
 * File abstraction for tools operating on Java programming language source and
 * class files.<br>
 *
 */
public class JavaSourceCodeObject extends SimpleJavaFileObject
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The Java file source code as a single String. 
     */
    private final String sourceCode;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a JavaSourceCodeObject for the specified source code file.
     * 
     * @param sourceFile The source code file
     * @param sourceCodeBaseDir The base directory for the source code files
     * 
     * @throws ErrorException if the file is {@code null}, the file does not
     *                        exist, it is a directory rather than a regular
     *                        file or an I/O error occurs while reading
     */
    protected JavaSourceCodeObject(File sourceFile,
                                   File sourceCodeBaseDir)
              throws ErrorException {
        super(URI.create("string:///"
                       + getRelativePath(sourceFile, sourceCodeBaseDir)),
              Kind.SOURCE);
        sourceCode = FileIOUtil.readFile(sourceFile);
    }
    
    /**
     * Creates a JavaSourceCodeObject for the specified source code.
     * 
     * @param fqcn The fully qualified class name (FQCN) for the Java file that
     *             is associated with the provided source code
     * @param sourceCode The source code
     */
    protected JavaSourceCodeObject(String fqcn,
                                   String sourceCode) {
        super(URI.create("string:///" + fqcn.replaceAll("\\.", "/")
                                      + JAVA_FILE_EXTENSION),
              Kind.SOURCE);
        this.sourceCode = sourceCode;
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Gets the character content(i.e., source code) of this file object, if
     * available.
     * 
     * @param ignoreEncodingErrors Don't care/not used
     * 
     * @return the source code if available or {@code null} otherwise
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
           throws IOException {
        return sourceCode;
    }
    
    // ------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       M   E   T   H   O   D   S   //
    // ------------------------------------------------------------- //
    /**
     * Given a file and a base directory it finds the relative path of the file
     * in respect to the base directory and returns it.
     *  
     * @param file The file
     * @param baseDir The base directory
     * 
     * @return the relative path of the file in respect to the base directory
     */
    private static String getRelativePath(File file,
                                          File baseDir) {
        String relativePath;
        if (file.getPath().startsWith(baseDir.getPath())) {
            int beginIndex = baseDir.getPath().length() + 1;
            relativePath = file.getPath().substring(beginIndex);
        }
        else {
            relativePath = file.getPath();
        }
        
        // Fix relative path on Windows
        relativePath = relativePath.replaceAll("\\\\", "/");
        return relativePath;
    }
}