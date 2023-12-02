/*
 * File          : FileIOUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 13 June 2013
 * Last Modified : 12 January 2021
 */
package edu.stanford.util.io;

// Import Java SE classes
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.ImageIcon;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.util.filesystem.FileUtil;
// Import constants
import static edu.stanford.constants.Constants.TEMP_FILE_EXT;
import static edu.stanford.constants.Literals.COMMA;
import static edu.stanford.constants.Literals.COMMA_CHAR;
import static edu.stanford.constants.Literals.NEW_LINE;

/**
 * Utility class for file I/O related operations
 */
public class FileIOUtil 
{
    // ----------------------------------------------------- //
    //   P  R  I  V  A  T  E     C  O  N  S  T  A  N  T  S   //
    // ----------------------------------------------------- //
    /** End of file input stream */
    private static final String       END_OF_STREAM      = null;
    /** Input buffer size in bytes */
    private static final int          BUFFER_SIZE        = 1024;
    /**
     * Comment identifier (i.e., comment lines start with this string) while
     * reading an input file
     */
    public  static final String       COMMENT_IDENTIFIER = "#";
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The lines to write to the output file */
    private static final List<String> outputFileLines    =
                                                     new LinkedList<String>();
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Converts an input stream to String.
     * 
     * @param inputStream The input stream
     * @param charsetName The encoding type used to convert bytes from the
     *                    stream into characters or {@code null} to use the
     *                    underlying platform's default charset
     * 
     * @return the String that is extracted from the input stream
     */
    public static String inputStreamToString(InputStream inputStream,
                                             String      charsetName) {
        Scanner scanner;
        if (charsetName == null) {
            scanner = new Scanner(inputStream);
        }
        else {
            scanner = new Scanner(inputStream, charsetName);
        }
        
        // Use the beginning of the input stream as delimiting pattern
        scanner.useDelimiter("\\A");
        String string = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        
        return string;
    }
    
    /**
     * Reads a file and returns the contents of the file as a single String.<br>
     * The file lines are trimmed.
     *  
     * @param file The file to read
     * 
     * @return a single String with all the contents of the file
     * 
     * @throws ErrorException if the file is {@code null}, the file does not
     *                        exist, it is a directory rather than a regular
     *                        file or an I/O error occurs while reading
     */
    public static String readFile(File file)
           throws ErrorException {
        return readFile(file, true);
    }
    
    /**
     * Reads a file and returns the contents of the file as a single String.<br>
     * The file lines are optionally trimmed.
     *  
     * @param file The file to read
     * @param trimLines {@code true} to remove leading and trailing white spaces
     *                  from each line in the file or {@code false} otherwise
     * 
     * @return a single String with all the contents of the file
     * 
     * @throws ErrorException if the file is {@code null}, the file does not
     *                        exist, it is a directory rather than a regular
     *                        file or an I/O error occurs while reading
     */
    public static String readFile(File file, boolean trimLines)
           throws ErrorException {
        if (file == null) {
            throw new ErrorException("The file to read is null");
        }
        
        return readFile(file.getPath(), trimLines);
    }
    
    /**
     * Reads a file and returns the contents of the file as a single String.<br>
     * The file lines are trimmed.
     *  
     * @param filePathname The absolute path name of the file to read
     * 
     * @return a single String with all the contents of the file
     * 
     * @throws ErrorException if the file path is {@code null}, the file does
     *                        not exist, it is a directory rather than a regular
     *                        file or an I/O error occurs while reading
     */
    public static String readFile(String filePathname)
           throws ErrorException {
        return readFile(filePathname, true);
    }
    
    /**
     * Reads a file and returns the contents of the file as a single String.<br>
     * The file lines are optionally trimmed.
     *  
     * @param filePathname The absolute path name of the file to read
     * @param trimLines {@code true} to remove leading and trailing white spaces
     *                  from each line in the file or {@code false} otherwise
     * 
     * @return a single String with all the contents of the file
     * 
     * @throws ErrorException if the file is {@code null}, the file does not
     *                        exist, it is a directory rather than a regular
     *                        file or an I/O error occurs while reading
     */
    public static String readFile(String filePathname, boolean trimLines)
           throws ErrorException {
        List<String> fileLines = readFile(filePathname,
                                          false,
                                          false,
                                          trimLines,
                                          null);
        StringBuilder fileContentsStrBuilder = new StringBuilder();
        
        for (String currLine: fileLines) {
            fileContentsStrBuilder.append(currLine);
            fileContentsStrBuilder.append(NEW_LINE);
        }
        
        return fileContentsStrBuilder.toString();
    }
    
    /**
     * Reads a file and returns an array list with the contents of the file.
     *  
     * @param file The file to read
     * @param skipEmptyLines {@code true} to skip empty lines or {@code false}
     *                        otherwise
     * @param skipCommentLines {@code true} to skip comment lines or
     *                         {@code false} otherwise
     * @param trimLines {@code true} to remove leading and trailing white spaces
     *                  from each line in the file or {@code false} otherwise
     * @param regexToMatch The regular expression to compare with each line as
     *                     the file is being read. The lines that do not match
     *                     the regular expression are skipped.<br>
     *                     If this argument is {@code null}, then it has no
     *                     effect (i.e., no regex matching takes place).
     * 
     * @return a list with all the contents of the file where each element in
     *         the list corresponds to a line in the file
     * 
     * @throws ErrorException if the file is {@code null}, the file does not
     *                        exist, it is a directory rather than a regular
     *                        file or an I/O error occurs while reading
     */
    public static List<String> readFile(File    file,
                                        boolean skipEmptyLines,
                                        boolean skipCommentLines,
                                        boolean trimLines,
                                        String  regexToMatch)
           throws ErrorException  {
        if (file == null) {
            throw new ErrorException("The file to read is null");
        }
        
        return readFile(file.getPath(),
                        skipEmptyLines,
                        skipCommentLines,
                        trimLines,
                        regexToMatch);
    }
    
    /**
     * Reads a file and returns an array list with the contents of the file.
     *  
     * @param filePathname The absolute path name of the file to read
     * @param skipEmptyLines {@code true} to skip empty lines or {@code false}
     *                        otherwise
     * @param skipCommentLines {@code true} to skip comment lines or
     *                         {@code false} otherwise
     * @param trimLines {@code true} to remove leading and trailing white spaces
     *                  from each line in the file or {@code false} otherwise
     * @param regexToMatch The regular expression to compare with each line as
     *                     the file is being read. The lines that do not match
     *                     the regular expression are skipped.<br>
     *                     If this argument is {@code null}, then it has no
     *                     effect (i.e., no regex matching takes place).
     * 
     * @return a list with all the contents of the file where each element in
     *         the list corresponds to a line in the file
     * 
     * @throws ErrorException if the file path is {@code null}, the file does
     *                        not exist, it is a directory rather than a regular
     *                        file or an I/O error occurs while reading
     */
    public static List<String> readFile(String  filePathname,
                                        boolean skipEmptyLines,
                                        boolean skipCommentLines,
                                        boolean trimLines,
                                        String  regexToMatch)
           throws ErrorException  {
        if (filePathname == null) {
            throw new ErrorException("The path name for the file to read is "
                                   + "null");
        }
        
        // Create a buffered input stream that holds up to BUFFER_SIZE per read
        BufferedInputStream bufInStream;
        try {
            bufInStream = new BufferedInputStream(
                                      new FileInputStream(filePathname),
                                      BUFFER_SIZE);
        }
        catch (FileNotFoundException fnfe) {
            throw new ErrorException("File '" + filePathname + "' does not "
                                   + "exist, is a directory rather than a " 
                                   + "regular file, or cannot be opened for "
                                   + "reading. Details:" + NEW_LINE
                                   + fnfe.getMessage());
        }
        
        BufferedReader bufferedReader = new BufferedReader(
                                            new InputStreamReader(bufInStream));
        
        String  currLine;
        boolean skipCurrLine;
        Pattern matchingPattern = null;
        if (regexToMatch != null) {
            try {
                matchingPattern = Pattern.compile(regexToMatch);
            }
            catch (PatternSyntaxException pse) {
                System.out.println("Invalid syntax for pattern '" + regexToMatch
                                 + "'. Details:" + NEW_LINE 
                                 + pse.getMessage());
            }
        }
        List<String> processedLines = new ArrayList<String>();
        try {
            while ((currLine = bufferedReader.readLine()) != END_OF_STREAM) {
                if (trimLines) {
                    currLine = currLine.trim(); // Trim the line
                }
                
                if (matchingPattern != null) {
                    skipCurrLine = !isPatternMatch(matchingPattern, currLine);
                    skipCurrLine = skipCurrLine ||
                                  (skipEmptyLines && currLine.isBlank()) ||
                                  (skipCommentLines &&
                                   currLine.startsWith(COMMENT_IDENTIFIER));
                }
                else {
                    skipCurrLine = (skipEmptyLines && currLine.isBlank()) ||
                                   (skipCommentLines &&
                                    currLine.startsWith(COMMENT_IDENTIFIER));
                }
                
                // If the current line should not be skipped, add it to the
                // array list of lines
                if (!skipCurrLine) {
                    processedLines.add(currLine);
                }
            }
        }
        catch (IOException ioe) {
            throw new ErrorException("I/O error while reading file '"
                                   + filePathname + "'. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        finally {
            // Close the file after reading
            try {
                bufInStream.close();
            }
            catch (IOException ignored) {}
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {}
        }
        
        return processedLines;
    }
    
    /**
     * Reads a file from the specified start line to the specified end line and
     * returns an array list with the contents of the file within that range.
     *  
     * @param file The file to read
     * @param skipEmptyLines {@code true} to skip empty lines or {@code false}
     *                       otherwise
     * @param skipCommentLines {@code true} to skip comment lines or
     *                         {@code false} otherwise
     * @param trimLines {@code true} to remove leading and trailing white spaces
     *                  from each line in the file or {@code false} otherwise
     * @param beginLineIndex The index of the first line to read (min value is
     *                       0)
     * @param endLineIndex The index of the last line to read (max value is
     *                     'number of file lines - 1')
     * @param regexToMatch The regular expression to compare with each line as
     *                     the file is being read. The lines that do not match
     *                     the regular expression are skipped.<br>
     *                     If this argument is {@code null}, then it has no
     *                     effect (i.e., no regex matching takes place).
     * 
     * @return a list with all the contents of the file where each element in
     *         the list corresponds to a line in the file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The file is {@code null}<br>
     *                        2) The file does not exist, it is a directory
     *                           rather than a regular file or an I/O error
     *                           occurs while reading<br>
     *                        3) Either the start or end line index is negative
     *                           <br>
     *                        4) The start line index is greater than the end
     *                           line index<br>
     *                        5) The index of the first line is greater than or 
     *                           equal to the number of file lines (i.e.,
     *                           exceeds the number of lines in the file)<br>
     *                        6) The index of the last line is greater than or
     *                           equal to the number of file lines (i.e.,
     *                           exceeds the number of lines in the file)
     */
    public static List<String> readFile(File    file,
                                        boolean skipEmptyLines,
                                        boolean skipCommentLines,
                                        boolean trimLines,
                                        int     beginLineIndex,
                                        int     endLineIndex,
                                        String  regexToMatch)
           throws ErrorException  {
        if (file == null) {
            throw new ErrorException("The path name for the file to read is "
                                   + "null");
        }
        
        return readFile(file.getPath(),
                        skipEmptyLines,
                        skipCommentLines,
                        trimLines,
                        beginLineIndex,
                        endLineIndex,
                        regexToMatch);
    }
    
    /**
     * Reads a file from the specified start line to the specified end line and
     * returns an array list with the contents of the file within that range.
     *  
     * @param filePathname The absolute path name of the file to read
     * @param skipEmptyLines {@code true} to skip empty lines or {@code false}
     *                       otherwise
     * @param skipCommentLines {@code true} to skip comment lines or
     *                         {@code false} otherwise
     * @param trimLines {@code true} to remove leading and trailing white spaces
     *                  from each line in the file or {@code false} otherwise
     * @param beginLineIndex The index of the first line to read (min value is
     *                       0)
     * @param endLineIndex The index of the last line to read (max value is
     *                     'number of file lines - 1')
     * @param regexToMatch The regular expression to compare with each line as
     *                     the file is being read. The lines that do not match
     *                     the regular expression are skipped.<br>
     *                     If this argument is {@code null}, then it has no
     *                     effect (i.e., no regex matching takes place).
     * 
     * @return a list with all the contents of the file where each element in
     *         the list corresponds to a line in the file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The file path name is {@code null}<br>
     *                        2) The file does not exist, it is a directory
     *                           rather than a regular file or an I/O error
     *                           occurs while reading<br>
     *                        3) Either the start or end line index is negative
     *                           <br>
     *                        4) The start line index is greater than the end
     *                           line index<br>
     *                        5) The index of the first line is greater than or 
     *                           equal to the number of file lines (i.e.,
     *                           exceeds the number of lines in the file)<br>
     *                        6) The index of the last line is greater than or
     *                           equal to the number of file lines (i.e.,
     *                           exceeds the number of lines in the file)
     */
    public static List<String> readFile(String  filePathname,
                                        boolean skipEmptyLines,
                                        boolean skipCommentLines,
                                        boolean trimLines,
                                        int     beginLineIndex,
                                        int     endLineIndex,
                                        String  regexToMatch)
           throws ErrorException  {
        if (filePathname == null) {
            throw new ErrorException("The path name for the file to read is "
                                   + "null");
        }
        
        // Make sure that the beginLineIndex is not negative
        if (beginLineIndex < 0) {
            throw new ErrorException("Negative begin line index: "
                                   + beginLineIndex);
        }
        // Make sure that the endLineIndex is not negative
        if (endLineIndex < 0) {
            throw new ErrorException("Negative end line index: "
                                   + endLineIndex);
        }
        // Make sure that the beginLineIndex is less than or equal to the
        // endLineIndex
        if (beginLineIndex > endLineIndex) {
            throw new ErrorException("The begin line index (" + beginLineIndex
                                   + ") exceeds the end line index ("
                                   + endLineIndex + ")");
        }
        
        // Create a buffered input stream that holds up to BUFFER_SIZE per read
        BufferedInputStream bufInStream;
        try {
            bufInStream = new BufferedInputStream(
                                      new FileInputStream(filePathname),
                                      BUFFER_SIZE);
        }
        catch (FileNotFoundException fnfe) {
            throw new ErrorException("File '" + filePathname + "' does not "
                                   + "exist, is a directory rather than a " 
                                   + "regular file, or cannot be opened for "
                                   + "reading. Details:" + NEW_LINE
                                   + fnfe.getMessage());
        }
        
        BufferedReader bufferedReader = new BufferedReader(
                                            new InputStreamReader(bufInStream));
        
        String currLine;
        boolean skipCurrLine;
        Pattern matchingPattern = null;
        if (regexToMatch!= null) {
            try {
                matchingPattern = Pattern.compile(regexToMatch);
            }
            catch (PatternSyntaxException pse) {
                System.out.println("Invalid syntax for pattern '" + regexToMatch
                                 + "'. Details:" + NEW_LINE + pse.getMessage());
            }
        }
        
        int numOfLinesReadSoFar     = 0;
        int numOfLinesLeft          = endLineIndex - beginLineIndex + 1;
        List<String> processedLines = new ArrayList<String>(numOfLinesLeft);
        try {
            while ((currLine = bufferedReader.readLine()) != END_OF_STREAM) {
                if (numOfLinesReadSoFar >= beginLineIndex &&
                    numOfLinesReadSoFar <= endLineIndex) {
                    if (trimLines) {
                        currLine = currLine.trim();     // Trim the line
                    }
                    
                    if (matchingPattern != null) {
                        skipCurrLine = isPatternMatch(matchingPattern,
                                                      currLine);
                        skipCurrLine = skipCurrLine ||
                                      (skipEmptyLines && currLine.isBlank()) ||
                                      (skipCommentLines &&
                                       currLine.startsWith(COMMENT_IDENTIFIER));
                    }
                    else {
                        skipCurrLine = (skipEmptyLines && currLine.isBlank()) ||
                                       (skipCommentLines &&
                                       currLine.startsWith(COMMENT_IDENTIFIER));
                    }
                    
                    // If the current line should not be skipped, add it to the
                    // array list of lines
                    if (!skipCurrLine) {
                        processedLines.add(currLine);
                    }
                    
                    numOfLinesLeft--;
                    
                    if (numOfLinesLeft == 0) {
                        break;
                    }
                }
                
                numOfLinesReadSoFar++;
            }
        }
        catch (IOException ioe) {
            throw new ErrorException("I/O error while reading file '"
                                   + filePathname + "'. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        finally {
            // Close the file after reading
            try {
                bufInStream.close();
            }
            catch (IOException ignored) {}
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {}
        }
        
        // Make sure that the endLineIndex has been reached
        if (numOfLinesLeft > 0) {
            throw new ErrorException("The file has fewer lines than "
                                   + (endLineIndex - beginLineIndex + 1));
        }
        
        return processedLines;
    }
    
    /**
     * Reads a file and returns an array list with the contents of the CSV file.
     * Each element in the returned list corresponds to a line in the CSV file
     * and is represented as a list of Strings where each String maps to a
     * comma-separated column in the given line.
     *  
     * @param file The CSV file to read
     * 
     * @return a list with all the contents of the file where each element in
     *         the list corresponds to a line in the CSV file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The file is {@code null}<br>
     *                        2) The file does not exist, it is a directory
     *                           rather than a regular file or an I/O error
     *                           occurs while reading<br>
     *                        3) Not all lines have the same number of
     *                           comma-separated columns 
     */
    public static List<List<String>> readCSVFile(File file)
           throws ErrorException  {
        if (file == null) {
            throw new ErrorException("The CSV file to read is null");
        }
        
        return readCSVFile(file.getPath());
    }
    
    /**
     * Reads a file and returns an array list with the contents of the CSV file.
     * Each element in the returned list corresponds to a line in the CSV file
     * and is represented as a list of Strings where each String maps to a
     * comma-separated column in the given line.
     *  
     * @param filePathname The absolute path name of the CSV file to read
     * 
     * @return a list with all the contents of the file where each element in
     *         the list corresponds to a line in the CSV file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The file path name is {@code null}<br>
     *                        2) The file does not exist, it is a directory
     *                           rather than a regular file or an I/O error
     *                           occurs while reading<br>
     *                        3) Not all lines have the same number of
     *                           comma-separated columns 
     */
    public static List<List<String>> readCSVFile(String filePathname)
           throws ErrorException  {
        if (filePathname == null) {
            throw new ErrorException("The path name for the CSV file to read "
                                   + "is null");
        }
        
        // Create a buffered input stream that holds up to BUFFER_SIZE per read
        BufferedInputStream bufInStream;
        try {
            bufInStream = new BufferedInputStream(
                                      new FileInputStream(filePathname),
                                      BUFFER_SIZE);
        }
        catch (FileNotFoundException fnfe) {
            throw new ErrorException("File '" + filePathname + "' does not "
                                   + "exist, is a directory rather than a " 
                                   + "regular file, or cannot be opened for "
                                   + "reading. Details:" + NEW_LINE
                                   + fnfe.getMessage());
        }
        
        BufferedReader bufferedReader = new BufferedReader(
                                            new InputStreamReader(bufInStream));
        
        List<List<String>> csvFileLines = new LinkedList<List<String>>();
        
        int          numOfColumns = -1;
        String       currLine;
        List<String> currCSVLine;
        try {
            while ((currLine = bufferedReader.readLine()) != END_OF_STREAM) {
                currLine = currLine.trim(); // Trim the line
                
                // Skip empty lines
                if (currLine.equals("")) {
                    continue;
                }
                
                currCSVLine =
                    FileIOUtil.processCSVLine(currLine);
                if (numOfColumns < 0) { // Use first row to count the columns 
                    numOfColumns = currCSVLine.size();
                }
                else {
                    if (currCSVLine.size() != numOfColumns) {
                        throw new ErrorException("Line '" + currLine + "' has "
                                               + currCSVLine.size()
                                               + " columns instead of "
                                               + numOfColumns);
                    }
                }
                
                csvFileLines.add(currCSVLine);
            }
        }
        catch (IOException ioe) {
            throw new ErrorException("I/O error while reading file '"
                                   + filePathname + "'. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        finally {
            // Close the file after reading
            try {
                bufInStream.close();
            }
            catch (IOException ignored) {}
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {}
        }
        
        return csvFileLines;
    }
    
    /**
     * Returns a map extracted from a file where every line in the files has
     * the following format:<br>
     * <pre>
     *        {@literal <key><delimiter><value>}
     * </pre>
     *         
     * @param file The file with the mapping
     * @param delimiter The delimiter that each line in the file uses to
     *                  separate the key from the value
     * @param reverse {@code true} to reverse the map so that the key in the
     *                file (i.e., token before the delimiter) becomes value in
     *                the returned map and vice versa or {@code false} to return
     *                the map using the same key-value pairs as in the provided
     *                file
     * @param sortByFirstToken {@code true} to sort the map by the first token
     *                         (before the delimiter) in the provided file 
     * @param sortBySecondToken {@code true} to sort the map by the second token 
     *                          (after the delimiter) in the provided file 
     * @return the map that is extracted from the file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The file is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It is a directory rather than a regular file<br>
     *                        4) An I/O error occurs while reading the file<br>
     *                        5) A key (or a value if the {@code reverse} flag
     *                           is set) exists more than once in the file<br>
     *                        6) Both the {@code sortByFirstToken} and
     *                           {@code sortBySecondToken} are {@code true}
     */
    public static Map<String, String> readMapFromFile(File    file,
                                                      String  delimiter,
                                                      boolean reverse,
                                                      boolean sortByFirstToken,
                                                      boolean sortBySecondToken)
           throws ErrorException {
        if (file == null) {
            throw new ErrorException("The file to read the map from is null");
        }
        
        return readMapFromFile(file.getPath(),
                               delimiter,
                               reverse,
                               sortByFirstToken,
                               sortBySecondToken);
    }
    
    /**
     * Returns a map extracted from a file where every line in the files has
     * the following format:<br>
     * <pre>
     *        {@literal <key><delimiter><value>}
     * </pre>
     *         
     * @param filePathname The absolute path name of the file with the mapping
     * @param delimiter The delimiter that each line in the file uses to
     *                  separate the key from the value
     * @param reverse {@code true} to reverse the map so that the key in the
     *                file (i.e., token before the delimiter) becomes value in
     *                the returned map and vice versa or {@code false} to return
     *                the map using the same key-value pairs as in the provided
     *                file
     * @param sortByFirstToken {@code true} to sort the map by the first token
     *                         (before the delimiter) in the provided file 
     * @param sortBySecondToken {@code true} to sort the map by the second token 
     *                          (after the delimiter) in the provided file 
     * @return the map that is extracted from the file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The file path name is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It is a directory rather than a regular file<br>
     *                        4) An I/O error occurs while reading the file<br>
     *                        5) A key (or a value if the {@code reverse} flag
     *                           is set) exists more than once in the file<br>
     *                        6) Both the {@code sortByFirstToken} and
     *                           {@code sortBySecondToken} are {@code true}
     */
    public static Map<String, String> readMapFromFile(String  filePathname,
                                                      String  delimiter,
                                                      boolean reverse,
                                                      boolean sortByFirstToken,
                                                      boolean sortBySecondToken)
           throws ErrorException {
        if (filePathname == null) {
            throw new ErrorException("The path name for the file to read the "
                                   + "map from is null");
        }
        
        if (sortByFirstToken && sortBySecondToken) {
            throw new ErrorException("Cannot sort the map by the first and the "
                                   + "second token at the same time");
        }
        Map<String, String> map;
        if (sortByFirstToken || sortBySecondToken) {
            map = new TreeMap<String, String>();
        }
        else {
            map = new HashMap<String, String>();
        }
        
        List<String> fileLines = FileIOUtil.readFile(filePathname,
                                                     true,
                                                     false,
                                                     true,
                                                     null);
        for (String line : fileLines) {
            int colonIndex = line.indexOf(delimiter);
            if (colonIndex == -1) {
                continue; // Just in case
            }
            
            String key   = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1).trim();
            
            if (reverse) {
                if (map.containsKey(value)) {
                    throw new ErrorException("Key '" + value
                                           + "' already exists in the map");
                }
                map.put(value, key);
            }
            else {
                if (map.containsKey(key)) {
                    throw new ErrorException("Key '" + key
                                           + "' already exists in the map");
                }
                map.put(key, value);
            }
        }
        
        return map;
    }
    
    /**
     * Writes the text contents (of a list) to a file
     *  
     * @param file The file to write
     * @param contents A collection with all the contents to write to the file
     *                 where each element in the list corresponds to a new line
     *                 in the file
     * @param append {@code true} to append the file (i.e., write the new
     *               contents below any existing ones) or {@code false} to erase
     *               all existing contents (if any)
     * 
     * @return {@code true} if the contents are written successfully to the file
     *         or {@code false} otherwise
     * 
     * @throws ErrorException if the file is {@code null}, the file is a
     *                        directory rather than a regular file or an I/O
     *                        error occurs while writing to the file
     */
    public static boolean writeFile(File               file,
                                    Collection<String> contents,
                                    boolean            append)
           throws ErrorException {
        if (file == null) {
            throw new ErrorException("The file to write is null");
        }
        
        return writeFile(file.getPath(),
                         contents,
                         append);
    }
    
    /**
     * Writes the text contents (of a list) to a file.
     *  
     * @param filePathname The absolute pathname of the file to write
     * @param contents A collection with all the contents to write to the file
     *                 where each element in the list corresponds to a new line
     *                 in the file
     * @param append {@code true} to append the file (i.e., write the new
     *               contents below any existing ones) or {@code false} to erase
     *               all existing contents (if any)
     * 
     * @return {@code true} if the contents are written successfully to the file
     *         or {@code false} otherwise
     * 
     * @throws ErrorException if the file path is {@code null}, the file is a
     *                        directory rather than a regular file or an I/O
     *                        error occurs while writing to the file
     */
    public static boolean writeFile(String             filePathname,
                                    Collection<String> contents,
                                    boolean            append)
           throws ErrorException {
        if (filePathname == null) {
            throw new ErrorException("The path name for the file to write is "
                                   + "null");
        }
        
        // Create a buffered writer
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filePathname, append);
        }
        catch (IOException ioe) {
            throw new ErrorException("File '" + filePathname + "' either does "
                                   + "not exist and cannot be created, or "
                                   + "exists but is a directory rather than a "
                                   + "regular file, or cannot be opened for "
                                   + "any other reason. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        
        try {
            for (String currString : contents) {
                bufferedWriter.write(currString);
            }
        }
        catch (IOException ioe) {
            throw new ErrorException("I/O error while writing to file '"
                                   + filePathname + "'. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        finally {
            // Close the file after writing
            try {
                bufferedWriter.close();
            }
            catch (IOException ignored) {}
            try {
                fileWriter.close();
            }
            catch (IOException ignored) {}
        }
        
        return true; // Return true to indicate that everything went right
    }
    
    /**
     * Writes the text contents (represented as a single string) to a file.
     *  
     * @param filePathname The absolute pathname of the file to write
     * @param contents The contents to write to the file represented as a single
     *                 string
     * @param append {@code true} to append the file (i.e., write the new
     *               contents below any existing ones) or {@code false} to erase
     *               all existing contents (if any)
     * 
     * @return {@code true} if the contents are written successfully to the file
     *         or {@code false} otherwise
     * 
     * @throws ErrorException if the file path is {@code null}, the file is a
     *                        directory rather than a regular file or an I/O
     *                        error occurs while writing to the file
     */
    public static boolean writeFile(String  filePathname,
                                    String  contents,
                                    boolean append)
           throws ErrorException {
        if (filePathname == null) {
            throw new ErrorException("The path name for the file to write is "
                                   + "null");
        }
        
        // Create a buffered writer
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filePathname, append);
        }
        catch (IOException ioe) {
            throw new ErrorException("File '" + filePathname + "' either does "
                                   + "not exist and cannot be created, or "
                                   + "exists but is a directory rather than a "
                                   + "regular file, or cannot be opened for "
                                   + "any other reason. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        
        try {
            bufferedWriter.write(contents);
        }
        catch (IOException ioe) {
            throw new ErrorException("I/O error while writing to file '"
                                   + filePathname + "'. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        finally {
            // Close the file after writing
            try {
                bufferedWriter.close();
            }
            catch (IOException ignored) {}
            try {
                fileWriter.close();
            }
            catch (IOException ignored) {}
        }
        
        return true; // Return true to indicate that everything went right
    }
    
    /**
     * Writes the text contents (of a collection) to a file.
     *  
     * @param file The file to write
     * @param contents Collection with the contents to write to the file where
     *                 each element in the list corresponds to a new line in the
     *                 file
     * @param append {@code true} to append the file (i.e., write the new
     *               contents below any existing ones) or {@code false} to erase
     *               all existing  contents (if any)
     * @param addNewLine {@code true} to add a new line after every element in
     *                   the collection or {@code false} otherwise
     * 
     * @return {@code true} if the contents are written successfully to the file
     *         or {@code false} otherwise
     * 
     * @throws ErrorException if the file is {@code null}, the file is a
     *                        directory rather than a regular file or an I/O
     *                        error occurs while writing to the file
     */
    public static boolean writeFile(File               file,
                                    Collection<String> contents,
                                    boolean            append,
                                    boolean            addNewLine)
           throws ErrorException {
        if (file == null) {
            throw new ErrorException("The file to write is null");
        }
        
        return writeFile(file.getPath(),
                         contents,
                         append,
                         addNewLine);
    }
    
    /**
     * Writes the text contents (of a collection) to a file.
     *  
     * @param filePathname The absolute pathname of the file to write
     * @param contents Collection with the contents to write to the file where
     *                 each element in the list corresponds to a new line in the
     *                 file
     * @param append {@code true} to append the file (i.e., write the new
     *               contents below any existing ones) or {@code false} to erase
     *               all existing  contents (if any)
     * @param addNewLine {@code true} to add a new line after every element in
     *                   the collection or {@code false} otherwise
     * 
     * @return {@code true} if the contents are written successfully to the file
     *         or {@code false} otherwise
     * 
     * @throws ErrorException if the file path is {@code null}, the file is a
     *                        directory rather than a regular file or an I/O
     *                        error occurs while writing to the file
     */
    public static boolean writeFile(String             filePathname,
                                    Collection<String> contents,
                                    boolean            append,
                                    boolean            addNewLine)
           throws ErrorException {
        if (filePathname == null) {
            throw new ErrorException("The path name for the file to write is "
                                   + "null");
        }
        
        // Create a buffered writer
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filePathname, append);
        }
        catch (IOException ioe) {
            throw new ErrorException("File '" + filePathname + "' either does "
                                   + "not exist and cannot be created, or "
                                   + "exists but is a directory rather than a "
                                   + "regular file, or cannot be opened for "
                                   + "any other reason. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        
        try {
            for (String currString : contents) {
                if (addNewLine) {
                    currString += NEW_LINE;
                }
                bufferedWriter.write(currString);
            }
        }
        catch (IOException ioe) {
            throw new ErrorException("I/O error while writing to file \""
                                   + filePathname + "\". Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
        finally {
            // Close the file after writing
            try {
                bufferedWriter.close();
            }
            catch (IOException ignored) {}
            try {
                fileWriter.close();
            }
            catch (IOException ignored) {}
        }
        
        return true; // Return true to indicate that everything went right
    }
    
    /**
     * Appends a line to the output file. It does not append it immediately.
     * Instead it adds the line to the buffer (i.e., list of lines to write to
     * the output) and write them to the the output file once flushOutput is
     * called.
     * 
     * @param line The line to append to the output
     */
    public static void appendOutput(String line) {
        outputFileLines.add(line + NEW_LINE);
    }
    
    /**
     * Prints the output file lines on the screen
     */
    public static void printOutput() {
        for (String outputLine : outputFileLines) {
            System.out.print(outputLine);
        }
    }
    
    /**
     * Flushes the content that is already stored in the outputFileLines to the
     * specified file.
     * 
     * @param filePathname The absolute path name of the file to flush the
     *                     output to
     * 
     * @throws ErrorException if the file path is {@code null}, the file is a
     *                        directory rather than a regular file or an I/O
     *                        error occurs while writing to the file
     */
    public static void flushOutput(String filePathname)
           throws ErrorException {
        FileIOUtil.writeFile(filePathname,
                             outputFileLines,
                             false);
        // Clear the buffer with the output lines
        outputFileLines.clear();
    }
    
    /**
     * Given a list of rows and a header (optional) it exports/saves them to the
     * provided CSV file. Each row is represented as a list of strings where a
     * string accounts for a CSV column in that row. The string can contain
     * also a comma if the intention is to have a comma as part of that column
     * in the exported CSV file. In other words, the caller does not have to
     * convert the strings to CSV format.
     * 
     * @param csvHeader The header columns in the exported CSV file or
     *                  {@code null}/empty to skip the CSV header. If there is a
     *                  header, then every other row is expected to have the
     *                  same number of columns.
     * @param csvRows The CSV rows (excluding the header) in the order to be
     *                written. Each row is represented as a list of strings
     *                where each string accounts for a column in that row.
     *                That string can also contain a comma (the underlying code
     *                detects and takes care of this).
     * @param csvOutputFilePathname The absolute path name of the CSV file to
     *                              create
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The CSV rows is {@code null} or empty (i.e.,
     *                           there is no content to export)<br>
     *                        2) There exists a CSV row which is either
     *                           {@code null} or empty<br>
     *                        3) A column (in any row) is {@code null} <br>
     *                        4) Not all CSV rows contain the same number of
     *                           columns<br>
     *                        5) The output file path is {@code null}, it
     *                           denotes a directory rather than a regular file
     *                           or an I/O error occurs while writing to the
     *                           file<br>
     *                        6) An I/O error occurs while writing to the CSV
     *                           file
     */
    public static void exportToCSV(List<String>       csvHeader,
                                   List<List<String>> csvRows,
                                   String             csvOutputFilePathname)
            throws ErrorException {
        if (csvRows == null || csvRows.isEmpty()) {
            throw new ErrorException("No CSV content to export");
        }
        
        // Export the contents of the anonymizedGrades to a .csv file
        List<String> csvOutputLines = new ArrayList<String>(csvRows.size() + 1);
        
        int numOfColumns;
        if (csvHeader != null && !csvHeader.isEmpty()) {
            numOfColumns = csvHeader.size();
            csvOutputLines.add(FileIOUtil.toCSVLine(csvHeader));
        }
        else {
            // There is no header. Use the first CSV row to find out how many
            // columns there are
            List<String> firstRow = csvRows.get(0);
            if (firstRow == null || firstRow.isEmpty()) {
               throw new ErrorException("The first CSV row is empty");
            }
            numOfColumns = firstRow.size();
        }
        
        
        String csvOutputLine;
        for (List<String> csvRow : csvRows) {
            // Validate that the row is not null and not empty; then convert it
            // to CSV format
            csvOutputLine = FileIOUtil.toCSVLine(csvRow);
            // Make sure that the number of columns is the expected
            if (csvRow.size() != numOfColumns) {
                throw new ErrorException("Row " + csvRow + " has "
                                       + csvRow.size() + " columns instead of "
                                       + numOfColumns);
            }
            // Add it to the list of the output lines to be exported
            csvOutputLines.add(csvOutputLine);
        }
        
        FileIOUtil.writeFile(csvOutputFilePathname,
                             csvOutputLines,
                             false,
                             true);
    }
    
    /**
     * Given a path name of a file, it reads is and removes the provided string
     * patterns if found in the text.<br>
     * It opens the file for reading and at the same time it creates a temporary
     * file to store the file content. As it reads the file line-by-line if a
     * line contains any of the strings in the provide list it either skips over
     * this line or replaces the matching string with an empty string.<br>
     * Once the entire file has been processed the result is written to the
     * temporary files that had been created initially. At this point the
     * temporary file is renamed to the given output file path name. This can
     * be the same as the file to process in which case the original file is
     * replaced.<br>
     * If no string is found in the file, this method returns {@code false} to
     * indicate that no new file is created (i.e., the temporary which is
     * identical to the original file is removed).
     * 
     * @param inputFilePathname The absolute path name of the file to process
     * @param outputFilePathname The absolute path name of the output file where
     *                           the result is written. This can be the same as
     *                           the input file in which case it replaces the
     *                           original file
     * @param stringsToExclude  A list with the strings to exclude in case they
     *                          appear in the file
     * @param skipLine {@code true} to completely skip the line where a string
     *                 to exclude is found in the file or {@code false} to
     *                 just remove this string in that line (i.e., replace it
     *                 with an empty string)
     * 
     * @return {@code true} if any of the strings in the provided list is found
     *         in the file and therefore a new file is written as output or
     *         {@code false} otherwise
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The list with the string to exclude is either
     *                           {@code null} or empty or one of its strings is
     *                           {@code null}<br>
     *                        2) The provided file name is {@code null}, does
     *                           not exist in the file system, exists but it
     *                           maps to a directory or does not have read
     *                           access permissions<br>
     *                        3) The provided output file name is {@code null},
     *                           exists and maps to a directory or does not have
     *                           write access permissions<br>
     *                        4) Cannot create or write to temporary file which
     *                           is used while processing the original file<br>
     *                        5) An error occurs while writing the temporary
     *                           file
     */
    public static boolean removeTextFromFile(String       inputFilePathname,
                                             String       outputFilePathname,
                                             List<String> stringsToExclude,
                                             boolean      skipLine) 
            throws ErrorException {
        // Validate the list of strings to exclude
        if ((stringsToExclude == null) || stringsToExclude.isEmpty()) {
            throw new ErrorException("The list of strings to exclude is null "
                                   + "or empty");
        }
        for (String stringToExclude : stringsToExclude) {
            if (stringToExclude == null) {
                throw new ErrorException("A string in the list of strings to "
                                       + "exclude is null");
            }
        }
        
        // Validate the input file
        FileUtil.validateFileToRead(inputFilePathname);
        
        // Make sure that the output file is not null and not a directory
        if (outputFilePathname == null) {
            throw new ErrorException("The output file path name is null");
        }
        if (new File(outputFilePathname).isDirectory()) {
            throw new ErrorException("The output file exists and is a "
                                   + "directory");
        }
        
        // Create a buffered input stream that holds up to BUFFER_SIZE per read
        BufferedInputStream bufInStream;
        try {
            bufInStream = new BufferedInputStream(
                                      new FileInputStream(inputFilePathname),
                                      BUFFER_SIZE);
        }
        catch (FileNotFoundException impossible) {
            throw new ErrorException("Internal error");
        }
        
        BufferedReader bufferedReader = new BufferedReader(
                                            new InputStreamReader(bufInStream));
        
        File sourceFile = new File(inputFilePathname);
        File tempFile;
        try {
            tempFile = File.createTempFile(sourceFile.getName(),
                                           TEMP_FILE_EXT);
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
                                   +"' to process file '" + inputFilePathname
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
        boolean differentThanOriginalFile = false;
        
        String currLine;
        try {
            while ((currLine = bufferedReader.readLine()) != END_OF_STREAM) {
                boolean match = false;
                // Check if this line contains any of the strings to exclude
                for (String stringToExclude : stringsToExclude) {
                    if (currLine.contains(stringToExclude)) {
                        if (!skipLine) {
                            // Replace the string with an empty string so that
                            // this modified line is written to the output
                            int beginIndex = currLine.indexOf(stringToExclude);
                            while (beginIndex != -1) {
                                 currLine = currLine.substring(0, beginIndex) +
                                            currLine.substring(beginIndex +
                                                     stringToExclude.length());
                                 beginIndex = currLine.indexOf(stringToExclude);
                            }
                        }
                        differentThanOriginalFile = true;
                        match = true;
                    }
                }
                
                if (!match || !skipLine) {
                    bufferedWriter.write(currLine + NEW_LINE);
                }
            }
            bufferedWriter.flush();
        }
        catch (IOException ioe) {
            throw new ErrorException("I/O error while reading file '"
                                   + inputFilePathname
                                   + "' and/or writing to temporary file '"
                                   + tempFile.getAbsolutePath() + "'. Details:"
                                   + NEW_LINE + ioe.getMessage());
            
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
        
        // None of the strings to exclude was found in the file
        if (!differentThanOriginalFile) {
            return false;
        }
        
        // If we reach here it means that there is no error so far and that
        // the processed file is different than the original file
        File destFile = new File(outputFilePathname);
        if (destFile.exists()) {
            if (!destFile.delete()) {
                throw new ErrorException("Failed to delete existing '"
                                       + outputFilePathname
                                       + "' before saving the anonymized "
                                       + "source code there");
            }
        }
        
        // We are ready now to rename the temp file to the destination path name
        tempFile.renameTo(destFile);
        return true;
    }
    
    /**
     * Retrieves an imange icon an from a PNG image.
     * 
     * @param iconPathname The PNG image pathname
     * 
     * @return the corresponding icon or {@code null} if the image is not found
     */
    public static ImageIcon getIcon(String iconPathname) {
        File iconFile = new File(iconPathname);
        
        try{
            iconPathname = iconFile.getCanonicalPath();
        }
        catch (IOException ioe) {
            return null;
        }
        
        // Make sure that the icon exists => otherwise return null
        if (!iconFile.exists()) { 
            return null;
        }
        
        return new ImageIcon(iconPathname);
    }
    
    /**
     * Convert from a {@code URL} to a {@code File}.
     * <p>
     * From version 1.1 this method will decode the URL.
     * Syntax such as {@literal file:///my%20docs/file.txt} will be correctly
     * decoded to {@literal /my docs/file.txt}.<br>
     * This method uses UTF-8 to decode percent-encoded octets to characters.
     * Additionally, malformed percent-encoded octets are handled leniently by
     * passing them through literally.
     * </p>
     *
     * @param url The file URL to convert, {@code null} returns {@code null}
     * 
     * @return the equivalent <code>File</code> object, or {@code null} if the
     *         URL's protocol is not <code>file</code>
     */
    public static File toFile(URL url) {
        if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
            return null;
        }
        String filename = url.getFile().replace('/', File.separatorChar);
        filename = decodeUrl(filename);
        return new File(filename);
    }
    
    /**
     * Processes a CSV file line and returns a list with the comma separated
     * columns.<br>
     * Note: A comma within quotes does not count as separator between two
     *       columns, but it is part of the column name.
     * 
     * @param line The CSV file line to process
     * 
     * @return a list with the columns names that are extracted from the line
     */
    public static List<String> processCSVLine(String line) {
        List<String> columnNames = new ArrayList<String>();
        
        int beginIndex = 0;
        boolean withinQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char currChar = line.charAt(i);
            
            if (currChar == '\"'){
                // Next column in the header starts with quotes (this means that
                // it contains comma in it)
                if (!withinQuotes) {
                    withinQuotes = true; // Now are are opening the quotes
                }
                // Column in the header that started with quotes (because it
                // it contained comma in it) ends now
                else {
                    columnNames.add(line.substring(beginIndex, i));
                    withinQuotes = false; // Now are are closing the quotes
                }
                beginIndex = i + 1;
            }
            else if (currChar == COMMA_CHAR) {
                // The columns ends here unless we are within quotes in which
                // case the comma is part of the column name
                if (!withinQuotes) { // End of header column
                    // Check if this is a comma after double quotes
                    if (i == 0 || line.charAt(i - 1) != '\"') {
                        columnNames.add(line.substring(beginIndex, i));
                    }
                    beginIndex = i + 1;
                }
            }
        }
        
        // Last column
        if (beginIndex <= line.length()) {
            columnNames.add(line.substring(beginIndex, line.length()));
        }
        
        return columnNames;
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Given a string it checks whether it matches the specified regular
     * expression pattern.
     * 
     * @param pattern The regular expression pattern
     * @param stringToCheck The string to check against for match
     * 
     * @return {@code true} if the provided string matches the regular
     *         expression pattern or {@code false} otherwise
     */
    private static boolean isPatternMatch(Pattern pattern,
                                          String  stringToCheck) {
        return pattern.matcher(stringToCheck).matches();
    }
    
    /**
     * Decodes the specified URL as per RFC 3986, i.e. transforms
     * percent-encoded octets to characters by decoding with the UTF-8 character
     * set.<br>
     * This method is primarily intended for usage with {@link java.net.URL}
     * which unfortunately does not enforce proper URLs. As such, this method
     * will leniently accept invalid characters or malformed percent-encoded
     * octets and simply pass them literally through to the result string.
     * Except for rare edge cases, this will make unencoded URLs pass through
     * unaltered.
     *
     * @param url The URL to decode, may be {@code null}.
     *
     * @return The decoded URL or {@code null} if the input was {@code null}
     */
    private static String decodeUrl(String url) {
        String decoded = url;
        if (url != null && url.indexOf('%') >= 0) {
            int n = url.length();
            StringBuilder buffer = new StringBuilder();
            final ByteBuffer bytes = ByteBuffer.allocate(n);
            for (int i = 0; i < n; ) {
                if (url.charAt(i) == '%') {
                    try {
                        do {
                            byte octet = (byte)
                              Integer.parseInt(url.substring(i + 1, i + 3), 16);
                            bytes.put(octet);
                            i += 3;
                        } while (i < n && url.charAt(i) == '%');
                        continue;
                    }
                    catch (final RuntimeException e) {
                        // Malformed percent-encoded octet, fall through and
                        // append characters literally
                    }
                    finally {
                        if (bytes.position() > 0) {
                            bytes.flip();
                            buffer.append(StandardCharsets.UTF_8
                                                          .decode(bytes)
                                                          .toString());
                            bytes.clear();
                        }
                    }
                }
                buffer.append(url.charAt(i++));
            }
            decoded = buffer.toString();
        }
        
        return decoded;
    }
    
    /**
     * Given a list of columns which may or may not contain comma it formats
     * them (i.e., detects if there exists a comma in any of them and if so it
     * surrounds it with double quotes) and returns the appropriately formatted
     * CSV string for the that line.
     * 
     * @param lineColumns The line columns to concatenate to a single CSV line
     * 
     * @return the formatted CSV line out of the provided line columns
     * 
     * @throws ErrorException in case the list of columns is either {@code null}
     *                        or empty or in case any of the strings in that
     *                        list is {@code null} 
     */
    private static String toCSVLine(List<String> lineColumns)
            throws ErrorException {
        if (lineColumns == null || lineColumns.isEmpty()) {
           throw new ErrorException("Null or empty list of line columns");
        }
        
        StringBuilder csvLine = new StringBuilder();
        String columnValue;
        Iterator<String> lineColItr = lineColumns.iterator();
        while (lineColItr.hasNext()) {
            columnValue = lineColItr.next();
            if (columnValue.contains(COMMA)) {
                // The line contains comma => surround it with quotes before
                // saving it to the output CSV file
                columnValue = "\"" + columnValue + "\"";
            }
            csvLine.append(columnValue);
            // Add a comma between the column values (i.e., unless this is
            // the last column)
            if (lineColItr.hasNext()) {
                csvLine.append(COMMA);
            }
        }
        
        return csvLine.toString();
    }
}