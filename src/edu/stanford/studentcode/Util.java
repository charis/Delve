/*
 * File          : Util.java
 * Author        : Charis Charitsis
 * Creation Date : 8 June 2014
 * Last Modified : 5 January 2021
 */
package edu.stanford.studentcode;

// Import Java SE classes
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.util.filesystem.DirectoryUtil;
import edu.stanford.util.io.FileIOUtil;
import edu.stanford.util.TimeUtil;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Constants.VERBOSE;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.POUND;
import static edu.stanford.constants.Literals.UNDERSCORE;

/**
 * Utility class for package stanford.studentcode
 */
public class Util
{
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /** 
     * Checks whether a Java source code file is parseable.
     * 
     * @param filePathname The absolute path name of the Java source code file
     *                     to verify whether it is parseable 
     * 
     * @return {@code true} if the source code file is parseable or
     *         {@code false} otherwise
     */
    public static boolean isParseable(String filePathname) {
        try {
            // Make sure that the sourceFilePathname has valid syntax
            new JavaFile(filePathname);
            return true;
        }
        catch (ErrorException error) {
            return false;
        }
    }
    
    /**
     * Given a root directory which contains the submission folders, it visits
     * those folders, finds the last snapshot that is parseable and adds it to
     * the returned list of path names.
     * 
     * @param rootDir The root directory under which all submissions folders
     *                exist
     * 
     * @return a list with the path names of the last parseable code snapshots
     *         for each student which is empty if there are no code snapshots
     *         returned
     * 
     * @throws ErrorException in case of an error listing the subdirectories of
     *                        the given root directory or listing the files in
     *                        one or more of those subdirectories
     */
    public static List<String> getSubmissionCodePathnames(File rootDir)
           throws ErrorException {
        List<String> lastSnapshotPathnames = new LinkedList<String>();
        
        List<File> submissionFolders = DirectoryUtil.listDirs(rootDir);
        for (File submissionFolder : submissionFolders) {
            List<File> snapshots = DirectoryUtil.listFiles(submissionFolder,
                                                           JAVA_FILE_EXTENSION);
            int numOfSnapshots = snapshots.size();
            JavaFile javaFile = null;
            if (numOfSnapshots > 0) {
                Collections.sort(snapshots);
                
                for (int i = numOfSnapshots - 1; i >= 0; i--) {
                    try {
                        javaFile = new JavaFile(snapshots.get(i).getPath());
                        break;
                    }
                    catch(ErrorException ignored) {
                    }
                }
            }
            
            if (javaFile != null) {
                lastSnapshotPathnames.add(javaFile.getFilePathname());
            }
        }
        
        return lastSnapshotPathnames;
    }
    
    /**
     * Given a root directory which contains the submission folders, it visits
     * those folders, finds the snapshots that are parseable and returns a map
     * where the key is a submission folder and the value is the set of
     * parseable files under this submission folder.<br>
     * The keys (submission folders) and also the values (java files) in the
     * returned map are sorted by pathname in alphabetical order.
     *  
     * @param rootDir The root directory which contains the submission folders
     * 
     * @return a map where the key is a submission folder and the value is the
     *         set of parseable files under this submission folder
     * 
     * @throws ErrorException in case of an error reading the file with the
     *                        student IDs to skip or in case of an error
     *                        processing a student submission
     */
    public static
           SortedMap<File, SortedSet<JavaFile>> getJavaFileLookup(File rootDir)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(rootDir);
        
        SortedMap<File, SortedSet<JavaFile>> result =
                                      new TreeMap<File, SortedSet<JavaFile>>();
        File[] filesOrDirs = rootDir.listFiles();
        for (File submissionDir : filesOrDirs) {
            if (submissionDir.isDirectory()) {
                SortedSet<JavaFile> javaFiles = new TreeSet<JavaFile>();
                for (File file : DirectoryUtil.listFiles(submissionDir,
                                                         JAVA_FILE_EXTENSION)) {
                    try {
                        javaFiles.add(new JavaFile(file.getPath()));
                    }
                    catch (ErrorException ignored) {
                    }
                };
                result.put(submissionDir, javaFiles);
            }
        }
        
        return result;
    }
    
    /**
     * Given a root directory which contains the submission folders, it visits
     * those folders, finds the last code snapshot that is parseable and returns
     * a set with the Java files to analyze. The files are sorted by pathname in
     * alphabetical order.
     * 
     * @param rootDir The root directory which contains the submission folders
     * @param skipIDFile A file with the IDs for the students to skip over
     *                   (i.e., not process their submissions) or {@code null}
     *                   to process all submission folders.<br>
     *                   This file will be updated with the student IDs that
     *                   are skipped down the road. Thus, unless {@code null} is
     *                   passed, the file will be created (if not initially
     *                   present) or updated later.
     *
     * @return a set with the Java files (last snapshot of every student
     *         submission) sorted by pathname in alphabetical order
     * 
     * @throws ErrorException in case of an error reading the file with the
     *                        student IDs to skip or in case of an error
     *                        processing a student submission
     */
    public static SortedSet<JavaFile> getJavaFiles(File rootDir,
                                                   File skipIDFile)
           throws ErrorException {
        SortedSet<String> skipIDs = new TreeSet<String>();
        if (skipIDFile != null && skipIDFile.exists()) {
            List<String> skipList = FileIOUtil.readFile(skipIDFile,
                                                        true, //skipEmptyLines
                                                        false,//skipCommentLines
                                                        true, //trimLines
                                                        null);//regexToMatch
            skipIDs.addAll(skipList);
        }
        
        SortedSet<JavaFile> javaFiles = new TreeSet<JavaFile>();
        boolean updateSkipIDFile = false;
        List<String> submissionCodePathnames =
                                   Util.getSubmissionCodePathnames(rootDir);
        for (String submissionCodePathname : submissionCodePathnames) {
            File sourceCodeFile = new File(submissionCodePathname);
            String studentID = sourceCodeFile.getParentFile().getName();
            
            if (!skipIDs.contains(studentID)) {
                try {
                    javaFiles.add(new JavaFile(submissionCodePathname));
                }
                catch (ErrorException ex) {
                    skipIDs.add(studentID);
                    try {
                        FileIOUtil.writeFile(skipIDFile,
                                             skipIDs,
                                             false, // append
                                             true); // addNewLine
                    }
                    catch (ErrorException ee) {
                        System.out.println("Error updating the file with the "
                                         + "skip IDs. Details:" + NEW_LINE
                                         + ee.getMessage());
                    }
                    if (VERBOSE) {
                        System.out.println("Skip ID: " + studentID);
                    }
                    updateSkipIDFile = true;
                }
            }
        }
        
        if (skipIDFile != null && updateSkipIDFile) {
            FileIOUtil.writeFile(skipIDFile,
                                 skipIDs,
                                 false, // append
                                 true); // addNewLine
        }
        
        return javaFiles;
    }
    
    /**
     * Given a file with timestamps it returns a sorted map (in ascending order)
     * with the snapshot order as key and the timestamp (Date instance) as
     * value.<br>
     * Each line in the file has the following format:<br>
     * <pre>
     * {@literal <student name>_<any string>_<snapshot order>.java#<timestamp>}
     * </pre>
     * where the student name is a string and the timestamp is the difference in
     * seconds between the current time and midnight, January 1, 1970 UTC
     *  
     * @param timestampFile The file with the timestamps
     * 
     * @return a sorted map (in ascending order) with the snapshot order as key
     *         and the timestamp (Date instance) as value
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The file with the timestamps does not exist, it
     *                           is a directory rather than a regular file or an
     *                           I/O error occurs while reading<br>
     *                        2) One or more lines do not have the expected
     *                           format:<br>
     *                           {@literal 
     *                            <student>_*_<snapshot order>.java#<timestamp>}
     *                        3) One ore more timestamp textual representations
     *                           do not have the expected format
     */
    public static SortedMap<Integer,Date> getTimestampLookup(File timestampFile)
           throws ErrorException {
        List<String> lines = FileIOUtil.readFile(timestampFile,
                                                 true,  // skipEmptyLines
                                                 false, // skipCommentLines
                                                 true,  // trimLines
                                                 null); // regexToMatch
        
        SortedMap<Integer, Date> timestampLookup = new TreeMap<Integer, Date>();
        
        for (String line : lines) {
            int poundIndex      = line.indexOf(POUND);
            int orderStartIndex = line.lastIndexOf(UNDERSCORE);
            int orderEndIndex   = line.indexOf(JAVA_FILE_EXTENSION);
            if (poundIndex == -1    || orderStartIndex == -1 ||
                orderEndIndex == -1 || orderStartIndex >= orderEndIndex) {
                throw new ErrorException("Invalid timestamp: '" + line + "'");
            }
            
            // Snapshot order
            String snapshotOrderStr = line.substring(orderStartIndex + 1,
                                                     orderEndIndex);
            Integer snapshotOrder;
            try {
                snapshotOrder = Integer.valueOf(snapshotOrderStr);
            }
            catch(NumberFormatException nfe) {
                throw new ErrorException("Invalid snapshot order in line: "
                                       + line);
            }
            
            // Timestamp
            Date timestamp;
            try {
                long timestampInSec =
                                Long.valueOf(line.substring(poundIndex + 1));
                // formattedDate: 'yyyy-MM-dd HH:mm:ss'
                String formattedDate = TimeUtil.getTime(timestampInSec * 1000);
                timestamp = TimeUtil.getDate(formattedDate);
            }
            catch (NumberFormatException | ErrorException e) {
                throw new ErrorException("Invalid timestamp (in sec) in line: "
                                        + line);
            }
            
            timestampLookup.put(snapshotOrder, timestamp);
        }
        
        return timestampLookup;
    }
    
    /**
     * Given a file with timestamps it returns a sorted map (in ascending order)
     * with the snapshot order as key and the time elapsed from the previous
     * snapshot in seconds as value. For the first snapshot (has no previous
     * snapshot) the map value is -1.<br>
     * Each line in the file has the following format:<br>
     * <pre>
     * {@literal <student name>_<any string>_<snapshot order>.java#<timestamp>}
     * </pre>
     * where the student name is a string and the timestamp is the difference in
     * seconds between the current time and midnight, January 1, 1970 UTC
     *  
     * @param timestampFile The file with the timestamps
     * 
     * @return  sorted map (in ascending order) with the snapshot order as key
     *          and the time elapsed from the previous snapshot in seconds as
     *          value. For the first snapshot (has no previous snapshot) the map
     *          value is 0.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The file with the timestamps does not exist, it
     *                           is a directory rather than a regular file or an
     *                           I/O error occurs while reading<br>
     *                        2) One or more lines do not have the expected
     *                           format:<br>
     *                           {@literal 
     *                            <student>_*_<snapshot order>.java#<timestamp>}
     *                        3) One ore more timestamp textual representations
     *                           do not have the expected format
     */
    public static SortedMap<Integer, Integer>
                                     getTimestampDiffLookup(File timestampFile)
           throws ErrorException {
        List<String> lines = FileIOUtil.readFile(timestampFile,
                                                 true,  // skipEmptyLines
                                                 false, // skipCommentLines
                                                 true,  // trimLines
                                                 null); // regexToMatch
        
        SortedMap<Integer, Long> timestampLookup = new TreeMap<Integer, Long>();
        
        for (String line : lines) {
            int poundIndex      = line.indexOf(POUND);
            int orderStartIndex = line.lastIndexOf(UNDERSCORE);
            int orderEndIndex   = line.indexOf(JAVA_FILE_EXTENSION);
            if (poundIndex == -1    || orderStartIndex == -1 ||
                orderEndIndex == -1 || orderStartIndex >= orderEndIndex) {
                throw new ErrorException("Invalid timestamp: '" + line + "'");
            }
            
            // Snapshot order
            String snapshotOrderStr = line.substring(orderStartIndex + 1,
                                                     orderEndIndex);
            Integer snapshotOrder;
            try {
                snapshotOrder = Integer.valueOf(snapshotOrderStr);
            }
            catch(NumberFormatException nfe) {
                throw new ErrorException("Invalid snapshot order in line: "
                                       + line);
            }
            
            // Timestamp
            long timestampInSec;
            try {
                timestampInSec = Long.valueOf(line.substring(poundIndex + 1));
            }
            catch (NumberFormatException nfe) {
                throw new ErrorException("Invalid timestamp (in sec) in line: "
                                        + line);
            }
            
            timestampLookup.put(snapshotOrder, timestampInSec);
        }
        
        SortedMap<Integer, Integer> timestampDiffLookup =
                                             new TreeMap<Integer, Integer>();
        // Now find the differences from the previous timestamp
        Long prevTimestamp = null;
        for (Integer snapshotOrder : timestampLookup.keySet()) {
            if (prevTimestamp == null) {
                // Reaching here means that this is the first snapshot
                timestampDiffLookup.put(snapshotOrder, -1);
                prevTimestamp = timestampLookup.get(snapshotOrder);
            }
            else {
                Long currTimestamp = timestampLookup.get(snapshotOrder);
                int elaplseTimeInSec = (int)(currTimestamp - prevTimestamp);
                timestampDiffLookup.put(snapshotOrder, elaplseTimeInSec);
                prevTimestamp = currTimestamp;
            }
        }
        
        return timestampDiffLookup;
    }
}
