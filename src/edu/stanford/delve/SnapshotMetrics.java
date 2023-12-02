/*
 * File          : SnapshotMetrics.java
 * Author        : Charis Charitsis
 * Creation Date : 17 November 2020
 * Last Modified : 3 November 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.body.Comment;
import edu.stanford.javaparser.body.Method;
import edu.stanford.javaparser.body.MethodCall;
import edu.stanford.studentcode.cyclomatic.ClassMetric;
import edu.stanford.studentcode.cyclomatic.CyclomaticAnalyzer;
import edu.stanford.studentcode.cyclomatic.MethodMetric;
import edu.stanford.util.filesystem.FileUtil;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Constants.TEMP_DIR;
import static edu.stanford.constants.Literals.UNDERSCORE;

/**
 * Collection of measures for a given snapshot
 */
public class SnapshotMetrics
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The name of the source code file/snapshot */
    private final String filename;
    /** The snapshot order */
    private final int    snapshotOrder;
    /** The total number of lines */
    private final int    numOfLines;
    /** The total number of pure code lines */
    private final int    numOfCodeLines;
    /** The total number of methods declared in the source code file/snapshot */
    private final int    numOfMethods;
    /** The comment volume, in characters */
    private final int    commentVolume;
    /** The total number of instance variables */
    private final int    numOfIvars;
    /**
     * The total number of method calls where the called method is declared
     * in the Java file (i.e., calling external methods do not count).<br>
     */
    private final int    numOfMethodCalls;
    /**
     * Placeholder with the info about the cyclomatic complexity values for the
     * methods in the file or {@code null} if the file does not compile or has
     * no methods
     */
    private final ClassMetric cycInfo;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a new SnapshotMetrics object that stores measurements about the
     * given source code file.<br>
     * The filename must have the following format:<br>
     *  <pre>{@literal <student ID>_<any string>_<snapshot order>.java}</pre>
     * 
     * @param javaFile The source code file to take measurements
     */
    public SnapshotMetrics(JavaFile javaFile) {
        if (javaFile == null) {
            throw new IllegalArgumentException("'javaFile' is null");
        }
        
        filename = javaFile.getFilename();
        // Filename format: <student ID>_<any string>_<snapshot order>.java
        int start = filename.lastIndexOf(UNDERSCORE);
        int end   = filename.indexOf(JAVA_FILE_EXTENSION);
        String snapshotOrderStr = filename.substring(start + 1, end);
        snapshotOrder   = Integer.valueOf(snapshotOrderStr);
        
        numOfLines         = javaFile.getNumOfLines();
        numOfCodeLines     = javaFile.getNumOfCodeLines();
        numOfMethods       = javaFile.getNumOfMethods();
        commentVolume      = getCommentVolume(javaFile);
        numOfIvars         = javaFile.getInstanceVariables().size();
        numOfMethodCalls   = getMethodCalls(javaFile);
        cycInfo            = getCYCInfo(javaFile);
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * @return the name of the source code file/snapshot
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * @return the snapshot order
     */
    public int getSnapshotOrder() {
        return snapshotOrder;
    }
    
    /**
     * @return the total number of lines
     */
    public int getNumOfLines() {
        return numOfLines;
    }
    
    /**
     * @return the total number of pure code lines
     */
    public int getNumOfCodeLines() {
        return numOfCodeLines;
    }
    
    /**
     * @return the total number of methods declared in the source code
     *         file/snapshot
     */
    public int getNumOfMethods() {
        return numOfMethods;
    }
    
    /**
     * @return the comment volume, in characters
     */
    public int getCommentVolume() {
        return commentVolume;
    }
    
    /**
     * @return the total number of instance variables
     */
    public int getNumOfIvars() {
        return numOfIvars;
    }
    
    /**
     * @return the number of method calls where the called method is declared in
     *         the Java file
     */
    public int getNumOfMethodCalls() {
        return numOfMethodCalls;
    }
    
    /**
     * @return the cyclomatic complexity metrics for the methods in the file
     */
    public List<MethodMetric> getMethodCYCMetrics() {
        if (cycInfo == null) {
            return null;
        }
        
        return cycInfo.getMethodMetrics();
    }
    
    /**
     * @return the average cyclomatic complexity of the methods in the file or
     *         {@code null} if the file does not compile or has no methods
     */
    public Double getCYCAvg() {
        if (cycInfo == null) {
            return null;
        }
        return cycInfo.getAvgCyclomaticComplexity();
    }
    
    /**
     * @return the maximum value among the cyclomatic complexity values for the
     *         methods in the file or {@code null} if the file does not compile
     *         or has no methods
     */
    public Integer getCYCMax() {
        if (cycInfo == null) {
            return null;
        }
        return cycInfo.getMaxCyclomaticComplexity();
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Given a Java file it returns the volume of the comments expressed in
     * characters.
     * 
     * @param javaFile The Java file to process
     * 
     * @return the volume of the comments expressed in characters
     */
    private int getCommentVolume(JavaFile javaFile) {
        // Find the volume of comments in the file (expressed in chars)
        int totalChars = 0;
        for (Comment comment : javaFile.getComments()) {
            String text = comment.getText();
            // If a line contains ';' it is most likely commented out code
            // Exclude this from the comments.
            if (!text.contains(";")) {
                totalChars += text.length();
            }
        }
        
        return totalChars;
    }
    
    /**
     * Returns the number of method calls where the called method is declared
     * in the Java file (i.e., calling external methods do not count).<br>
     * If a method is called in a loop it counts only once.<br>
     * Thus 'foo(); foo(); foo();' accounts for three method calls, but
     * {@literal 'for (int i = 0; i < 3; i++){ foo(); }'} accounts for one
     * method call.
     * 
     * @param javaFile The Java file to process
     * 
     * @return the number of method calls where the called method is declared in
     *         the Java file
     */
    private int getMethodCalls(JavaFile javaFile) {
        Set<String> methodSignatures = new HashSet<String>();
        for (Method method : javaFile.getMethods()) {
            String methodSignature = method.getSignature(false, true);
            methodSignatures.add(methodSignature);
        }
        
        List<MethodCall> methodCalls;
        try {
            methodCalls = javaFile.getMethodCalls();
        }
        catch (Throwable t) {
            return 0;
        }
        
        int internalCalls = 0;
        for (MethodCall methodCall : methodCalls) {
            String calledMethodSignature;
            try {
                calledMethodSignature =
                            methodCall.getCalledMethodSignature(true);
                if (methodSignatures.contains(calledMethodSignature)) {
                    internalCalls++;
                }
            }
            catch (ErrorException ignored) {
            }
        }
        
        return internalCalls;
    }
    
    /**
     * Returns a placeholder with the info about the cyclomatic complexity
     * values for the methods in the provided file. If there are no methods or
     * if the file does not compile, the method returns {@code null}.
     * 
     * @param javaFile The Java file to get the average and maximum cyclomatic
     *                 complexity for
     * 
     * @return a placeholder with the info about the cyclomatic complexity
     *         values for the methods in the provided file or {@code null} if
     *         the file does not compile or has no methods
     */
    private ClassMetric getCYCInfo(JavaFile javaFile) {
        if (javaFile.getNumOfMethods() == 0) { // No methods
            return null;
        }
        
        if (Util.getUnresolvedSymbol(javaFile) != null) { // Unresolved symbol
            return null;
        }
        
        String className = javaFile.getFirstClass() != null?
                           javaFile.getFirstClass().getName(): null;
        if (className == null) {
            return null;
        }
        
        String destinationPathname = TEMP_DIR  + File.separator
                                   + className + JAVA_FILE_EXTENSION;
        File destFile = new File(destinationPathname);
        if (destFile.exists()) {
            destFile.delete();
        }
        try {
            FileUtil.copyFile(javaFile.getFilePathname(), destinationPathname);
        }
        catch (ErrorException ee) {
            System.err.println(ee.getMessage());
            return null;
        }
        File sourceFile = new File(destinationPathname);
        List<File> jarFiles = javaFile.getJarFiles();
        
        CyclomaticAnalyzer analyzer = CyclomaticAnalyzer.getInstance();
        ClassMetric classMetric;
        try {
            classMetric = analyzer.getClassMetric(sourceFile, jarFiles);
        }
        catch (Throwable t) {
            classMetric = null;
        }
        finally {
            sourceFile.delete();
        }
        
        return classMetric;
    }
}
