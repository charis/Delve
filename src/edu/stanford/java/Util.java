/*
 * File          : Util.java
 * Author        : Charis Charitsis
 * Creation Date : 25 June 2020
 * Last Modified : 19 September 2021
 */
package edu.stanford.java;

// Import Java SE classes
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.java.compile.Compiler;
//import edu.stanford.java.execution.StateTrackingProgramExecutor;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.util.OSPlatform;
import edu.stanford.util.filesystem.DirectoryUtil;
import edu.stanford.util.filesystem.FileUtil;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Constants.TEMP_FILE_EXT;
import static edu.stanford.constants.Literals.NEW_LINE;

/**
 * Utility class to filter out files that are non executable till completion
 */
public class Util
{
    // ----------------------------- //
    //   C  O  N  S  T  A  N  T  S   //
    // ----------------------------- //
    /** Input buffer size in bytes */
    private static final int    BUFFER_SIZE   = 1024;
    /** End of file input stream */
    private static final String END_OF_STREAM = null;
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Given a collection with Java files, it returns a set with the ones that
     * are parseable.
     * 
     * @param files The files to process and keep only the parseable ones
     * 
     * @return an alphabetically ordered (by filename) set with the parseable
     *         files or an empty set if there are no parseable files
     */
    public static SortedSet<File> getParseableFiles(Collection<File> files) {
        SortedSet<File> parseableFiles = new TreeSet<File>();
        if (files == null) {
            return parseableFiles;
        }
        for (File file : files) {
            if (file != null) {
                try {
                    new JavaFile(file.getPath());
                    
                    parseableFiles.add(file);
                }
                catch (ErrorException ee) {
                    // The file is non-parseable
                }
            }
        }
        
        return parseableFiles;
    }
    
    /**
     * Given a collection with source code files, it returns a set with the ones
     * that are compilable.
     * 
     * @param files The source code files to process and keep only the 
     *              compilable ones
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return an alphabetically ordered (by file pathname) set with the
     *         compilable files or an empty set if there are no compilable files
     */
    public static SortedSet<File> getCompilableFiles(Collection<File> files,
                                                     String           classpath)
    {
        SortedSet<File> compilableFiles = new TreeSet<File>();
        if (files == null) {
            return compilableFiles;
        }
        
        for (File file : files) {
            if (file != null) {
                if (isCompilable(file.getPath(), classpath)) {
                    compilableFiles.add(file);
                }
            }
        }
        
        return compilableFiles;
    }
    
    /**
     * Given a collection with source code files, it returns a set with the ones
     * that are compilable.<br>
     * The code is instrumented (add provided import statements and replace text
     * with provided replacements) and then compiled using the provided
     * classpath.
     * 
     * @param files The source code files to process and keep only the 
     *              compilable ones
     * @param imports The import statements to add on top of any existing ones
     *                or {@code null} to add no more import statements
     * @param replacements A lookup map that is used for text replacement or
     *                     {@code null} to avoid text replacements. It uses the
     *                     keys as the text to look for and the corresponding
     *                     values as replacement to this text.
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return an alphabetically ordered (by file pathname) set with the
     *         compilable files
     */
    public static SortedSet<File> getCompilableFiles(
                                               Collection<File>    files,
                                               String[]            imports,
                                               Map<String, String> replacements,
                                               String              classpath) {
        SortedSet<File> compilableFiles = new TreeSet<File>();
        for (File file : files) {
            try {
                JavaFile javaFile = new JavaFile(file.getPath());
                
                SourceCodeParser parser = new SourceCodeParser(javaFile);
                String instrumentedCode =
                       parser.getInstrumentedSourceCode(false,// saveStateBefore
                                                        false,// saveStateBAfter
                                                        imports,
                                                        replacements);
                
                Compiler.compileInMemory(instrumentedCode, // sourceCode
                                         classpath);       // classpath
                
                compilableFiles.add(file);
            }
            catch (ErrorException ee) {
                // The file is non-compilable
            }
        }
        
        return compilableFiles;
    }
    
    /**
     * Checks whether a Java source code file is compilable.
     * 
     * @param filePathname The absolute path name of the Java source code file
     *                     to verify whether it is compilable
     * @param classpath The classpath to compile the source code file
     * 
     * @return {@code true} if the source code file is compilable or
     *         {@code false} otherwise
     */
    public static boolean isCompilable(String filePathname,
                                       String classpath) {
        try {
            assertCompilable(filePathname, classpath);
            return true;
        }
        catch (ErrorException ee) {
            return false;
        }
    }
    
    /**
     * Asserts that a Java source code file is compilable.
     * 
     * @param filePathname The absolute path name of the Java source code file
     *                     to verify whether it is compilable
     * @param classpath The classpath to compile the source code file
     * 
     * @throws ErrorException if the provided file path name is {@code null},
     *                        does not exist, is not a file or if a compile
     *                        error occurs
     */
    public static void assertCompilable(String filePathname,
                                        String classpath)
           throws ErrorException {
        if (filePathname == null) {
            throw new ErrorException("Argument 'filePathname' is null");
        }
        File file = new File(filePathname);
        String error = Compiler.isCompilable(file, classpath);
        if (error != null) {
            throw new ErrorException(error);
        }
    }
    
    /**
     * Given a root directory which contains the submission folders, it visits
     * those folders and ensures that all files include the correct import
     * statement(s) (if not, it updates them)<br>
     * 
     * @param rootDir The root directory which contains the submission folders
     * @param importStatements The import statements that the files should
     *                         include
     * 
     * @throws ErrorException in case of an error updating the files to include
     *                        the correct import statement 
     */
    public static void ensureCorrectImportStatement(File     rootDir,
                                                    String[] importStatements)
           throws ErrorException {
        List<File> submissionFolders = DirectoryUtil.listDirs(rootDir);
        for (File submissionFolder : submissionFolders) {
            List<File> files = DirectoryUtil.listFiles(submissionFolder,
                                                       JAVA_FILE_EXTENSION);
            for (File sourceFile : files) {
                addImportStatements(sourceFile, importStatements);
            }
        }
    }
    
    /**
     * Given a root directory which contains the submission folders, it visits
     * those folders (each folder stores the snapshots for a given student
     * submission), finds the snapshots that are parseable and inserts those to
     * a list of snapshots for the given student. Finally, returns a map where
     * the key is the student ID and the value is the list of the parseable
     * snapshots for this student.
     * 
     * @param rootDir The root directory under which all submissions folders
     *                exists
     * 
     * @return a map where the key is the student ID and the value is the list
     *         of the parseable snapshots for this student.
     * 
     * @throws ErrorException in case of an error listing all the subdirectories
     *                        of the given directory or listing the files for
     *                        one or more of those subdirectories
     */
    public static Map<String, List<JavaFile>> getSnapshotMap(File rootDir)
           throws ErrorException {
        Map<String, List<JavaFile>> result =
                                    new TreeMap<String, List<JavaFile>>();
        
        List<File> submissionFolders = DirectoryUtil.listDirs(rootDir);
        for (File submissionFolder : submissionFolders) {
            List<JavaFile> parseableSnapshots = new ArrayList<JavaFile>();
            List<File> snapshots = DirectoryUtil.listFiles(submissionFolder,
                                                           JAVA_FILE_EXTENSION);
            Collections.sort(snapshots);
            for (File snapshot : snapshots) {
                try {
                    JavaFile javaFile = new JavaFile(snapshot.getPath());
                    parseableSnapshots.add(javaFile);
                }
                catch(ErrorException ignored) {
                }
            }
            
            String studentID = submissionFolder.getName();
            result.put(studentID, parseableSnapshots);
        }
        
        return result;
    }
    
    /**
     * Given a Java source code directory it returns the fully qualified
     * package name.
     * 
     * @param sourceCodeDir The Java source code directory
     * @param sourceCodeBaseDir The base directory for the source code files
     * 
     * @return the fully qualified package name for the given Java source code
     *         directory
     */
    public static String getPackageName(File sourceCodeDir,
                                        File sourceCodeBaseDir) {
        String baseDirPathname = sourceCodeBaseDir.getPath();
        String pakageName = sourceCodeDir.getPath().replace(baseDirPathname,
                                                            "");
        if (pakageName.startsWith(File.separator)) {
            pakageName = pakageName.substring(1);
        }
        if (pakageName.endsWith(File.separator)) {
            pakageName = pakageName.substring(0, pakageName.length() - 1);
        }
        String regex = OSPlatform.isUnixBased() ?
                       File.separator : File.separator + File.separator;
        
        return pakageName.replaceAll(regex, "\\.");
    }
    
    /**
     * Given a classpath and a path to append it appends this path to the
     * classpath (at the end of it) and returns the updated classspath.<br>
     * If the path already exists in the classpath or if the path does not exist
     * in the filesystem this method returns the initially provided classpath.
     * 
     * @param classpath The classpath to update
     * @param pathToAppend The path to append to the classpath
     * 
     * @return the updated classpath
     */
    public static String appendClasspath(String classpath,
                                         String pathToAppend) {
        File fileOrDirToAppend = new File(pathToAppend);
        if (!fileOrDirToAppend.exists()) {
            return classpath;
        }
        
        if (classpath == null || classpath.trim().isEmpty()) {
            return pathToAppend;
        }
        
        // The classpath is not null/empty. The path to append exists.
        // Check if the path is already in the classpath
        String[] paths = classpath.trim().split(File.pathSeparator);
        for (String path : paths) {
            File curr = new File(path);
            if (curr.equals(fileOrDirToAppend)) {
                // The path is already in the classpath
                return classpath;
            }
        }
        
        // The path is not in the classpath. Append it.
        return classpath + File.pathSeparator + pathToAppend;
    }
    
    // ------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       M   E   T   H   O   D   S   //
    // ------------------------------------------------------------- //
    /**
     * Given a Java file it adds the specified import statements.<br>
     * If those import statements exist, it replaces them with the given ones.
     * 
     * @param sourceFile The source code Java file
     * @param importStatements The import statements to add
     * 
     * @throws ErrorException in any of the following cases:
     *                        1) The provided source file is {@code null}<br>
     *                        2) The provided source file does not exist in the
     *                           file system<br>
     *                        3) It exists but it maps to a directory<br>
     *                        4) It does not have read and write access
     *                           permissions<br>
     *                        5) Cannot create or write to temporary file which
     *                           is used while processing the source code file
     *                           <br>
     *                        6) We cannot remove the source file to update it
     *                           with the temporary file
     */
    private static void addImportStatements(File     sourceFile,
                                            String[] importStatements)
            throws ErrorException {
        // Make sure that the source code file exists and has read/write access
        FileUtil.validateFileToRead(sourceFile);
        FileUtil.validateFileToWrite(sourceFile);
        
        if (!sourceFile.getName().endsWith(JAVA_FILE_EXTENSION)) {
            return; // Non-Java file
        }
        
        // Create a buffered input stream that holds up to BUFFER_SIZE per read
        BufferedInputStream bufInStream = null;
        try {
            bufInStream = new BufferedInputStream(
                                      new FileInputStream(sourceFile),
                                      BUFFER_SIZE);
        }
        catch (FileNotFoundException fnfe) {
            new RuntimeException("Internal error: " + fnfe.getMessage());
        }
        
        BufferedReader bufferedReader = new BufferedReader(
                                            new InputStreamReader(bufInStream));
        
        File tempFile;
        try {
            tempFile = File.createTempFile(sourceFile.getName(), TEMP_FILE_EXT);
        }
        catch (IOException ioe) {
            // Close the source file after reading
            try {
                bufInStream.close();
            }
            catch (IOException ignored) {}
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {}
            
            throw new ErrorException("Cannot create temporary file '"
                                   + sourceFile.getName() + TEMP_FILE_EXT 
                                   +"' to process file '" + sourceFile.getPath()
                                   + "'. Details:" + NEW_LINE
                                   + ioe.getMessage()); 
        }
        
        // Create a buffered writer
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(tempFile);
        }
        catch (IOException ioe) {
            // Close the source file after reading
            try {
                bufInStream.close();
            }
            catch (IOException ignored) {}
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {}
            
            throw new ErrorException("Cannot write to temporary file '"
                                   + tempFile.getAbsolutePath() + "'. Details:"
                                   + NEW_LINE + ioe.getMessage()); 
        }
        
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        
        String currLine;
        boolean done = false;
        try {
            while ((currLine = bufferedReader.readLine()) != END_OF_STREAM) {
                if (!done) {
                    if (currLine.trim().startsWith("import ")) {
                        currLine = importStatements[0];
                        for (int i = 1; i < importStatements.length; i++) {
                            currLine += NEW_LINE + importStatements[i];
                        }
                        done = true;
                    }
                }
                else {
                    if (currLine.trim().startsWith("import ")) {
                        // We already added all necessary import statemnts
                        // Need this to avoid duplicates
                        continue;
                    }
                }
                
                if (!done) {
                    if (currLine.trim().startsWith("public class") ||
                        currLine.trim().startsWith("class ")) {
                        // Class declaration
                        // Add the import statements just before this line
                        String imports = importStatements[0];
                        for (int i = 1; i < importStatements.length; i++) {
                            imports += NEW_LINE + importStatements[i];
                        }
                        currLine = imports + NEW_LINE + currLine;
                        done = true;
                    }
                }
                
                bufferedWriter.write(currLine + NEW_LINE);
            }
            bufferedWriter.flush();
        }
        catch (IOException ioe) {
            throw new ErrorException("I/O error while reading file '"
                                   + sourceFile.getPath()
                                   + "' and/or writing to temporary file '"
                                   + tempFile.getAbsolutePath()
                                   + "'. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        finally {
            // Close the source file after reading
            try {
                bufInStream.close();
            }
            catch (IOException ignored) {}
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {}
            
            // Close the temporary file after writing
            try {
                fileWriter.close();
            }
            catch (IOException ignored) {}
            try {
                bufferedWriter.close();
            }
            catch (IOException ignored) {}
            
        }
        
        // If we reach here it means that there is no error so far
        if (!sourceFile.delete()) {
            throw new ErrorException("Error deleting '" + sourceFile.getPath()
                                   + "' to update it");
        }
        // We are ready now to rename the temp file to the original source file
        tempFile.renameTo(sourceFile);
    }
}
