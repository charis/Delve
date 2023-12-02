/*
 * File          : JavaFile.java
 * Author        : Charis Charitsis
 * Creation Date : 19 June 2018
 * Last Modified : 21 November 2020
 */
package edu.stanford.javaparser;

// Import Java SE classes
import java.io.File;
import java.util.List;
// Import custom classes
import edu.stanford.exception.ErrorException;

/**
 * <pre>
 * Place holder with the following information after parsing a Java source file:
 * - The classes that are declared within the Java file
 * - The interfaces that are declared within the Java file
 * - The enumerations that are declared in the Java file
 * - The constructors that are declared in the Java file
 * - The methods that are declared in the Java file
 * - The variable declarations (both class and instance) as well as the constant
 *   declarations in the Java file
 * - The comments in the Java file
 * </pre>
 */
public class JavaFile extends JavaSourceCode implements Comparable<JavaFile>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The absolute path name of the Java source code file to parse */
    private final String                 filePathname;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a new JavaFile from the given file path name.<br>
     * It iterates over the contents of the file and extracts the most important
     * information:<br>
     * <pre>
     * - The classes that are declared in the Java file
     * - The interfaces that are declared in the Java file
     * - The enumerations that are declared in the Java file
     * - The constructors that are declared in the Java file
     * - The methods that are declared in the Java file
     * - The variable declarations (both class and instance) as well as the
     *   constant declarations in the Java file
     * - The comments that exist in the Java file.
     * </pre>
     *   
     * @param filePathname The absolute path name of the Java file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file path name is {@code null}<br> 
     *                        2) The file denoted by the provided path name does
     *                           not exist<br>
     *                        3) An error occurred while parsing the file<br>
     */
    public JavaFile(String filePathname)
           throws ErrorException {
        this(filePathname, null, null);
    }
    
    /**
     * Constructs a new JavaFile from the given file path name.<br>
     * It iterates over the contents of the file and extracts the most important
     * information:<br>
     * <pre>
     * - The classes that are declared in the Java file
     * - The interfaces that are declared in the Java file
     * - The enumerations that are declared in the Java file
     * - The constructors that are declared in the Java file
     * - The methods that are declared in the Java file
     * - The variable declarations (both class and instance) as well as the
     *   constant declarations in the Java file
     * - The comments that exist in the Java file.
     * </pre>
     *   
     * @param filePathname The absolute path name of the Java file
     * @param sourceCodeDirs Directories with source code to include in the
     *                       symbol resolution or {@code null} to not include
     *                       such directories.<br>
     *                       Note: The order matters. Directories that appear
     *                             first in the list are added first in the
     *                             classpath. {@code sourceCodeDirs} are added
     *                             before {@code jarFiles}
     * @param jarFiles Jar files to include in the symbol resolution or
     *                 {@code null} if there are no jar files to include.<br>
     *                 Note: The order matters. Files that appear first in
     *                       the list are added first in the classpath.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file path name is {@code null}<br> 
     *                        2) The file denoted by the provided path name does
     *                           not exist<br>
     *                        3) An error occurred while parsing the file<br>
     */
    public JavaFile(String     filePathname,
                    List<File> sourceCodeDirs,
                    List<File> jarFiles)
           throws ErrorException {
        super(new File(filePathname), sourceCodeDirs, jarFiles);
        
        this.filePathname = filePathname;
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * @return the absolute path name of the Java source code file to parse 
     */
    public String getFilePathname() {
        return filePathname;
    }
    
    /**
     * @return the absolute name of the Java source code file to parse 
     */
    public String getFilename() {
        return new File(filePathname).getName();
    }
    
    /**
     * @return the pathname string of the parent directory or {@code null}
     *         if this file exists under the root directory
     */
    public String getParent() {
        return new File(filePathname).getParent();
    }
    
    // ---------------------------------- //
    //    e   q   u   a   l   s   (  )    //
    // ---------------------------------- //
    /**
     * Indicates whether some other object is "equal to" this one.<br>
     * The equals method implements an equivalence relation on non-null object
     * references.<br>
     * For any non-null object (obj) to compare to this reference object (this),
     * this method returns true if and only if:<br>
     * <pre>
     *   1) 'this' and 'obj' refer to the same object (this == obj has the value
     *      true)
     *   OR
     *   2) 'this' and 'obj' refer to different objects, but:
     *      i)   'obj' is not null
     *      ii)  'obj' is an instance of the same class as 'this' 
     *      iii) 'obj' has the same file path name as 'this'
     * </pre>
     * 
     * @param obj The reference object to compare against
     * 
     * @return {@code true} if this object is the same as the obj argument or
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        // 'this' and 'obj' are pointing to the same object => return true
        if (this == obj) {
            return true;
        }
        
        // 'obj' is null and not pointing to the same object as 'this'
        // => return false
        if (obj == null) {
            return false;
        }
        
        // 'obj' is pointing to a different object than 'this'
        // If that object (obj) is not a JavaFile object return false
        if (!(obj instanceof JavaFile)) {
            return false;
        }
        
        JavaFile other = (JavaFile) obj; // Now can cast safely
        
        // Make sure that both JavaFile instances have the same file pathname
        if (!getFilePathname().equals(other.getFilePathname())) {
            return false;
        }
        
        // If we reach here => the two objects are considered equal
        return true;
    }
    
    // ------------------------------------------ //
    //    h   a   s   h   C   o   d   e   (  )    //
    // ------------------------------------------ //
    /**
     * {@inheritDoc}
     * 
     * To calculate the hash code it takes into consideration the file pathname.
     * @see String#hashCode()
     */
    @Override
    public int hashCode() {
        return getFilePathname().hashCode();
    }
    
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this JavaFile method with another JavaFile method object for
     * order.<br>
     * The comparison between the two Java files takes place by the alphabetical
     * order of their pathnames.<br>
     * 
     * @param other The other JavaFile to be compared with this one
     * 
     * @return the value {@code 0} if this JavaFile is equal to the other
     *         JavaFile, -1 if this method is less than the other JavaFile and
     *         +1 if this JavaFile is greater than the other JavaFile
     */
    @Override
    public int compareTo(JavaFile other) {
        String thisFilePathname  = this.getFilePathname();
        String otherFilePathname = other.getFilePathname();
        if (thisFilePathname.compareTo(otherFilePathname) < 0) {
            return -1;
        }
        
        if (thisFilePathname.compareTo(otherFilePathname) > 0) {
            return 1;
        }
        
        // thisFilePathname == otherFilePathname
        return 0;
    }
}
