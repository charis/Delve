/*
 * File          : Util.java
 * Author        : Charis Charitsis
 * Creation Date : 8 November 2020
 * Last Modified : 24 September 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
// Import custom classes
import com.github.javaparser.resolution.UnsolvedSymbolException;
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.studentcode.cyclomatic.ClassMetric;
import edu.stanford.studentcode.cyclomatic.CyclomaticAnalyzer;
import edu.stanford.studentcode.cyclomatic.MethodMetric;
import edu.stanford.util.filesystem.DirectoryUtil;
import edu.stanford.util.filesystem.FileUtil;
import edu.stanford.util.io.FileIOUtil;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Constants.TEMP_DIR;
import static edu.stanford.constants.Constants.VERBOSE;
import static edu.stanford.constants.Literals.COLON;
import static edu.stanford.constants.Literals.UNDERSCORE;
import static edu.stanford.delve.Constants.TIMESTAMP_DELIM;
import static edu.stanford.delve.Constants.TIMESTAMP_FILENAME;

/**
 * Utility class for Delve
 */
public class Util
{
    // ----------------------------- //
    //   C  O  N  S  T  A  N  T  S   //
    // ----------------------------- //
    /**
     * Denotes that this is not an actual elapsed time, but the student took a
     * break between the current and the previous snapshot
     */
    public  static final int BREAK_TIME                 = -1;
    /** 
     * The minimum elapsed time in seconds to consider the possibility of a
     * work break between two snapshots. In other words, if the elapsed time is
     * below this threshold, there is no break. If it is above but below the 
     * BREAK_TIME_MAX_THRESHOLD then we need to compare the filesizes to
     * determine if there is a work break.
     */
    private static final int BREAK_TIME_MIN_THRESHOLD   = 600;  // 600secs
    /** 
     * The maximum elapsed time in seconds till a student saves a snapshot or
     * else a work break between is assumed.
     */
    private static final int BREAK_TIME_MAX_THRESHOLD   = 3600; // 3600secs
    /** 
     * The minimum characters per minute that the student should add to the
     * previous snapshot to assume there is no break between the two snapshots
     */
    private static final int CHARS_PER_MINUTE_THRESHOLD = 10; // 3600sec = 1hr
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Given a directory that contains the submission subfolders it processes
     * those submissions and returns a map where the key is the submission
     * folder and the value is the last parseable java file under this folder.
     * 
     * @param submissionRootDir The directory that contains the submission
     *                          subfolders
     * 
     * @return a map where the key is the submission folder and the value is the
     *         last parseable java file under this folder
     * 
     * @throws ErrorException in case a submission folder does not have read
     *                        access
     */
    public static
             Map<File, JavaFile> getSubmittedFileLookup(File submissionRootDir)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(submissionRootDir);
        Map<File, JavaFile> result = new TreeMap<File, JavaFile>();
        List<File> submissionDirs = DirectoryUtil.listDirs(submissionRootDir);
        
        for (File submissionDir : submissionDirs) {
            List<File> files = DirectoryUtil.listFiles(submissionDir,
                                                       JAVA_FILE_EXTENSION);
            Collections.sort(files);
            JavaFile submittedFile = null;
            for (int i = files.size() - 1; i >= 0; i--) {
                String filePathname = files.get(i).getPath();
                try {
                    submittedFile = new JavaFile(filePathname);
                    break;
                }
                catch (ErrorException ignored) {
                }
            }
            
            if (submittedFile != null) {
                result.put(submissionDir, submittedFile);
            }
        }
        
        return result;
    }
    
    /**
     * Given a directory that contains the submission subfolders it processes
     * those submissions and returns a map where the key is the submission
     * folder and the value is a map with the student snapshots and the time the
     * student worked on each snapshot.<br>
     * Note: Each subfolder must contain a file 'timestamp.txt' that has the
     * following format:
     * <pre>
     * {@literal <student ID>_<any string>_<snapshot order>.java#<timestamp>}
     * </pre>
     * where the student name is a string and the timestamp is the difference in
     * seconds between the current time and midnight, January 1, 1970 UTC
     * 
     * @param submissionRootDir The directory that contains the submission
     *                          subfolders
     * 
     * @return a map where the key is the submission folder and the value is a
     *         map with the student snapshots and the time the student worked on
     *         each snapshot
     * 
     * @throws ErrorException in case of an error processing a timestamp file or
     *                        in case a submission folder does not have read
     *                        access
     */
    public static Map<File, Map<String, Integer>>
                                        getTimeDiffs(File submissionRootDir)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(submissionRootDir);
        Map<File, Map<String, Integer>> result =
                              new TreeMap<File, Map<String, Integer>>();
        List<File> submissionDirs = DirectoryUtil.listDirs(submissionRootDir);
        
        for (File submissionDir : submissionDirs) {
            File timestampFile = new File(submissionDir.getPath()
                                        + File.separator + TIMESTAMP_FILENAME);
            if (timestampFile.exists() && timestampFile.canRead()) {
                result.put(submissionDir, getTimeDiffLookup(submissionDir));
            }
        }
        
        return result;
    }
    
    /**
     * Given a folder with the submission snapshots for a given student it
     * returns a set with the parseable snapshots sorted by pathname in
     * alphabetical order.
     *  
     * @param submissionDir The folder with the submission snapshots
     * 
     * @return a set with the parseable snapshots sorted by pathname in
     *         alphabetical order
     * 
     * @throws ErrorException in case of an error listing the java files
     */
    public static 
           SortedSet<JavaFile> getSubmissionJavaFiles(File submissionDir)
            throws ErrorException {
        SortedSet<JavaFile> javaFiles = new TreeSet<JavaFile>();
        List<File> files = DirectoryUtil.listFiles(submissionDir,
                                                   JAVA_FILE_EXTENSION);
        for (File file : files) {
            try {
                javaFiles.add(new JavaFile(file.getPath()));
            }
            catch (ErrorException ignored) {
            }
        }
        
        return javaFiles;
    }
    
    /**
     * Given a Java file it determines if there exists an unresolved symbol and
     * if so it returns its name or a description of the issue.
     * 
     * @param javaFile The files to check for unresolved symbols
     * 
     * @return the name of the unresolved symbol or {@code null} if there are
     *         no unresolved symbols
     */
    public static String getUnresolvedSymbol(JavaFile javaFile) {
        try {
            javaFile.getMethodCalls();
            return null;
        }
        catch (ErrorException ee) {
            Throwable cause = ee.getCause();
            if (cause != null) {
                if (cause instanceof UnsolvedSymbolException) {
                    String errorMsg = cause.getMessage();
                    int start = errorMsg.indexOf(COLON);
                    if (start != -1 && errorMsg.startsWith("Unsolved")) {
                        return errorMsg.substring(start + 1).trim();
                    }
                }
                else {
                    return cause.getMessage();
                }
            }
            else {
                String errorMsg = ee.getMessage();
                if (errorMsg != null) {
                    int start = errorMsg.indexOf(COLON);
                    if (start != -1 && errorMsg.startsWith("Not found")) {
                        return errorMsg.substring(start + 1).trim();
                    }
                }
            }
            return "Unknown error";
        }
        catch (Throwable t) {
            // There can be other issues
            return t.getMessage();
        }
    }
    
    /**
     * Given a directory with student snapshots it returns a map that uses as
     * keys the snapshot order and as value the time difference between a given
     * snapshot and the previous one (the one with the previous order).<br>
     * A map entry with -1 as value means that the corresponding snapshot (key)
     * comes after a work break.<br>
     * The timestamps for the snapshots are found in a file 'timestamps.txt'
     * that has the following format:
     * <pre>
     * {@literal <student ID>_<any string>_<snapshot order>.java#<timestamp>}
     * </pre>
     * where the student name is a string and the timestamp is the difference in
     * seconds between the current time and midnight, January 1, 1970 UTC
     * 
     * @param submissionDir The submission directory with the snapshots for a
     *                      given student
     * 
     * @return a map that uses as key the snapshot order and as value the time
     *         difference between a given snapshot and the previous one (the one
     *         with the previous order)
     * 
     * @throws ErrorException in case of an error processing the timestamp file
     *                        or in case the submission directory does not have
     *                        read access
     */
    public static
           Map<Integer, Integer> getTimeDiffLookupByOrder(File submissionDir)
           throws ErrorException {
        Map<Integer, Integer> result = new TreeMap<Integer, Integer>();
        Map<String, Integer> timestampDiffLookup =
                                          getTimeDiffLookup(submissionDir);
        
        for (String filename : timestampDiffLookup.keySet()) {
            int start = filename.lastIndexOf(UNDERSCORE);
            int end   = filename.indexOf(JAVA_FILE_EXTENSION);
            String snapshotOrderStr = filename.substring(start + 1, end);
            Integer snapshotOrder = Integer.valueOf(snapshotOrderStr);
            Integer timeDiff = timestampDiffLookup.get(filename);
            result.put(snapshotOrder, timeDiff);
        }
        
        return result;
    }
    
    /**
     * Given a directory with student snapshots it returns a map that uses as
     * keys the snapshot filenames and as value the time difference between a
     * given snapshot and the previous one.<br>
     * A map entry with -1 as value means that the corresponding snapshot (key)
     * comes after a work break.<br>
     * The timestamps for the snapshots are found in a file 'timestamps.txt'
     * that has the following format:
     * <pre>
     * {@literal <student ID>_<any string>_<snapshot order>.java#<timestamp>}
     * </pre>
     * where the student name is a string and the timestamp is the difference in
     * seconds between the current time and midnight, January 1, 1970 UTC
     * 
     * @param submissionDir The submission directory with the snapshots for a
     *                      given student
     * 
     * @return a map that uses as key the snapshot filename and as value the
     *         time difference between a given snapshot and the previous one
     * 
     * @throws ErrorException in case of an error processing the timestamp file
     *                        or in case the submission directory does not have
     *                        read access
     */
    public static Map<String, Integer> getTimeDiffLookup(File submissionDir)
           throws ErrorException {
        Map<Integer, Object>[] lookupMaps = getTimestampMaps(submissionDir);
        Map<Integer, Object> timestampLookup = lookupMaps[0];
        Map<Integer, Object> filenameLookup  = lookupMaps[1];
        
        Map<String, Integer> timestampDiffLookup =
                                          new TreeMap<String, Integer>();
        
        // Now find the differences from the previous timestamp
        Long   prevTimestamp = null;
        String prevFilename  = null;
        for (Integer snapshotOrder : timestampLookup.keySet()) {
            String filename = (String)filenameLookup.get(snapshotOrder);
            if (prevTimestamp == null) {
                // Reaching here means that this is the first snapshot
                timestampDiffLookup.put(filename, BREAK_TIME);
                prevTimestamp = (Long)timestampLookup.get(snapshotOrder);
            }
            else {
                Long currTimestamp = (Long)timestampLookup.get(snapshotOrder);
                int elaplseTimeInSec = (int)(currTimestamp - prevTimestamp);
                if (elaplseTimeInSec < 0) {
                    // The time difference cannot be negative unless there is
                    // an error in reporting; treat this as a 'break'
                    prevTimestamp = (Long)timestampLookup.get(snapshotOrder);
                }
                else if (elaplseTimeInSec <= BREAK_TIME_MIN_THRESHOLD) {
                    timestampDiffLookup.put(filename, elaplseTimeInSec);
                }
                else if (elaplseTimeInSec > BREAK_TIME_MAX_THRESHOLD) {
                    timestampDiffLookup.put(filename, BREAK_TIME);
                }
                else {
                    // Compare the size for the two files
                    String dirPathname = submissionDir.getPath();
                    File prevSnapshot = new File(dirPathname + File.separator
                                                             + prevFilename);
                    File snapshot     = new File(dirPathname + File.separator
                                                             + filename);
                    // The size difference in bytes which is the number of
                    // characters that the student added to the previous
                    // snapshot
                    long sizeDiff = snapshot.length() - prevSnapshot.length();
                    double charsPerMin = sizeDiff /( elaplseTimeInSec * 60.0);
                    if (charsPerMin > CHARS_PER_MINUTE_THRESHOLD) {
                        timestampDiffLookup.put(filename, elaplseTimeInSec);
                    }
                    else { // No much change between the two snapshots => break
                        timestampDiffLookup.put(filename, BREAK_TIME);
                    }
                }
                prevTimestamp = currTimestamp;
                prevFilename  = filename;
            }
        }
        
        return timestampDiffLookup;
    }
    
    /**
     * Given a directory with student snapshots it returns a map that uses as
     * key a snapshot filename and as value the timestamp (expressed in seconds)
     * for that file.<br>
     * The timestamps for the snapshots are found in a file 'timestamps.txt'
     * that has the following format:
     * <pre>
     * {@literal <student ID>_<any string>_<snapshot order>.java#<timestamp>}
     * </pre>
     * where the student name is a string and the timestamp is the difference in
     * seconds between the current time and midnight, January 1, 1970 UTC
     * 
     * @param submissionDir The submission directory with the snapshots for a
     *                      given student
     * 
     * @return a map that uses as key a snapshot filename and as value the
     *         timestamp for that file
     * 
     * @throws ErrorException in case of an error processing the timestamp file
     *                        or in case the submission directory does not have
     *                        read access
     */
    public static Map<String, Long> getTimestampLookup(File submissionDir)
           throws ErrorException {
        Map<String, Long> result = new TreeMap<String, Long>();
        
        Map<Integer, Object>[] lookupMaps = getTimestampMaps(submissionDir);
        Map<Integer, Object> timestampLookup = lookupMaps[0];
        Map<Integer, Object> filenameLookup  = lookupMaps[1];
        
        for (Integer snapshotOrder : filenameLookup.keySet()) {
            String filename = (String)filenameLookup.get(snapshotOrder);
            
            Long timestamp = (Long)timestampLookup.get(snapshotOrder);
            result.put(filename, timestamp);
        }
        
        return result;
    }
    
    /**
     * Given a directory that contains the submission subfolders it processes
     * those submissions and returns a map where the key is the submission
     * folder and the value is the list with the metrics for the methods of the
     * last compilable file under this folder.
     * 
     * @param submissionRootDir The directory that contains the submission
     *                          subfolders
     * @param jarFiles The jar files that are required to compile this file or 
     *                 {@code null} if there are no jar files required
     * 
     * @return a map where the key is the submission folder and the value is the
     *         list with the metrics for the methods of the last compilable file
     *         under this folder
     * 
     * @throws ErrorException in case a submission folder does not have read
     *                        access
     */
    public static List<MethodMetric>
                  getMostComplexMethods(File             submissionRootDir,
                                        Collection<File> jarFiles)
           throws ErrorException {
        List<MethodMetric> result = new ArrayList<MethodMetric>();
        
        Map<File, List<MethodMetric>> methodMetricMap =
                             getMethodMetricMap(submissionRootDir, jarFiles);
        
        for (File lastCompilableFile : methodMetricMap.keySet()) {
            List<MethodMetric> methodMetrics = 
                                     methodMetricMap.get(lastCompilableFile);
            
            // The metric for the method with the most instructions
            MethodMetric referenceMetric = null;
            // The max cyclomatic complexity
            int cycMax = 0;
            int maxInstructions = 0;
            for (MethodMetric methodMetric : methodMetrics) {
                if (methodMetric.getNumOfInstructions() > maxInstructions) {
                    maxInstructions = methodMetric.getNumOfInstructions();
                    referenceMetric = methodMetric;
                }
                else if (methodMetric.getNumOfInstructions() == maxInstructions
                    && methodMetric.getCyclomaticComplexity() >  cycMax) {
                    referenceMetric = methodMetric;
                }
                
                if (methodMetric.getCyclomaticComplexity() >  cycMax) {
                    cycMax = methodMetric.getCyclomaticComplexity();
                }
            }
            
            // Check if the reference method has both the max instructions and
            // the max cyclomatic complexity. If not, ignore it
            if  (referenceMetric.getCyclomaticComplexity() == cycMax) {
                boolean highComplexity = referenceMetric.getCyclomaticComplexity() >= 15 && referenceMetric.getNumOfInstructions() >= 250;
                boolean lowComplexity = referenceMetric.getCyclomaticComplexity() <= 7 && referenceMetric.getNumOfInstructions() <= 150;
                if (highComplexity || lowComplexity) {
                    System.err.println(lastCompilableFile.getName() + " --> cyc: " + referenceMetric.getCyclomaticComplexity() + " / instr: " + referenceMetric.getNumOfInstructions());
                }
                result.add(referenceMetric);
            }
        }
        
        return result;
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Given a directory with student snapshots it returns an array with two
     * maps:<br>
     * 1) A map that uses as key the snapshot order and as value the timestamp
     *    (expressed in seconds) for that snapshot<br>
     * 2) A map that uses as key the snapshot order and as value the filename
     *    for that snapshot
     * The timestamps for the snapshots are found in a file 'timestamps.txt'
     * that has the following format:
     * <pre>
     * {@literal <student ID>_<any string>_<snapshot order>.java#<timestamp>}
     * </pre>
     * where the student name is a string and the timestamp is the difference in
     * seconds between the current time and midnight, January 1, 1970 UTC
     * 
     * @param submissionDir The submission directory with the snapshots for a
     *                      given student
     * 
     * @return an array with two maps:<br>
     *         1) A map that uses as key the snapshot order and as value the
     *            timestamp (expressed in seconds) for that snapshot<br>
     *         2) A map that uses as key the snapshot order and as value the
     *            filename for that snapshot
     * 
     * @throws ErrorException in case of an error processing the timestamp file
     *                        or in case the submission directory does not have
     *                        read access
     */
    @SuppressWarnings("unchecked")
    private static Map<Integer, Object>[] getTimestampMaps(File submissionDir)
            throws ErrorException {
        DirectoryUtil.validateDirToRead(submissionDir);
        SortedMap<Integer, Long> timestampLookup = new TreeMap<Integer, Long>();
        Map<Integer, String> filenameLookup = new HashMap<Integer, String>();
        
        File timestampFile = new File(submissionDir.getPath() + File.separator
                                    + TIMESTAMP_FILENAME);
        if (timestampFile.exists() && timestampFile.canRead()) {
            List<String> timestampLines = FileIOUtil.readFile(timestampFile,
                                                              true,
                                                              false,
                                                              true,
                                                              null);
            for (String line : timestampLines) {
                int delimIndex      = line.indexOf(TIMESTAMP_DELIM);
                int orderStartIndex = line.lastIndexOf(UNDERSCORE);
                int orderEndIndex   = line.indexOf(JAVA_FILE_EXTENSION);
                if (delimIndex == -1    || orderStartIndex == -1 ||
                    orderEndIndex == -1 || orderStartIndex >= orderEndIndex) {
                    throw new ErrorException("Invalid timestamp: '" + line
                                           + "' in " + timestampFile.getPath());
                }
                
                // Snapshot order
                String snapshotOrderStr = line.substring(orderStartIndex + 1,
                                                         orderEndIndex);
                Integer snapshotOrder;
                try {
                    snapshotOrder = Integer.valueOf(snapshotOrderStr);
                }
                catch(NumberFormatException nfe) {
                    throw new ErrorException("Invalid snapshot order in line: '"
                                           + line + "' in "
                                           + timestampFile.getPath());
                }
                
                // Timestamp
                long timestampInSec;
                try {
                    timestampInSec =
                               Long.valueOf(line.substring(delimIndex + 1));
                }
                catch (NumberFormatException nfe) {
                    throw new ErrorException("Invalid timestamp (in sec) in "
                                           + "line: '" + line + "' in "
                                           + timestampFile.getPath());
                }
                
                
                timestampLookup.put(snapshotOrder, timestampInSec);
                String filename = line.substring(0, delimIndex);
                filenameLookup.put(snapshotOrder, filename);
            }
        }
        
        return new Map[] { timestampLookup, filenameLookup };
    }
    
    /**
     * Given a directory that contains the submission subfolders it processes
     * those submissions and returns a map where the key is the last compilable
     * file of a given submission and the value is the list with the metrics for
     * the methods in that file.
     * 
     * @param submissionRootDir The directory that contains the submission
     *                          subfolders
     * @param jarFiles The jar files that are required to compile this file or 
     *                 {@code null} if there are no jar files required
     * 
     * @return a map where the key is the last compilable file of a given
     *         submission and the value is the list with the metrics for the
     *         methods in that file
     * 
     * @throws ErrorException in case a submission folder does not have read
     *                        access
     */
    private static Map<File, List<MethodMetric>>
                       getMethodMetricMap(File             submissionRootDir,
                                          Collection<File> jarFiles)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(submissionRootDir);
        
        Map<File, List<MethodMetric>> result =
                                      new TreeMap<File, List<MethodMetric>>();
        
        List<File> submissionDirs = DirectoryUtil.listDirs(submissionRootDir);
        
        for (File submissionDir : submissionDirs) {
            List<File> files = DirectoryUtil.listFiles(submissionDir,
                                                       JAVA_FILE_EXTENSION);
            Collections.sort(files);
            CyclomaticAnalyzer analyzer = CyclomaticAnalyzer.getInstance();
            for (int i = files.size() - 1; i >= 0; i--) {
                File sourceFile = null;
                try {
                    JavaFile javaFile =
                             new JavaFile(files.get(i).getPath(),
                                          null,
                                          new ArrayList<File>(jarFiles));
                    String className = javaFile.getFirstClass() != null?
                            javaFile.getFirstClass().getName(): null;
                    if (className != null) {
                        String destinationPathname = TEMP_DIR  + File.separator
                                                   + className 
                                                   + JAVA_FILE_EXTENSION;
                        File destFile = new File(destinationPathname);
                        if (destFile.exists()) {
                            destFile.delete();
                        }
                        FileUtil.copyFile(javaFile.getFilePathname(),
                                          destinationPathname);
                        sourceFile = new File(destinationPathname);
                    }
                }
                catch (ErrorException ee) {
                    if (VERBOSE) {
                        System.err.println(ee.getMessage());
                    }
                }
                if (sourceFile != null) {
                    try {
                        ClassMetric classMetric =
                                  analyzer.getClassMetric(sourceFile, jarFiles);
                        result.put(files.get(i),
                                   classMetric.getMethodMetrics());
                        break;
                    }
                    catch (Throwable ignored) {
                    }
                }
            }
        }
        
        return result;
    }
}
