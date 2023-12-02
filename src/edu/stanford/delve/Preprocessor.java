/*
 * File          : Preprocessor.java
 * Author        : Charis Charitsis
 * Creation Date : 25 October 2020
 * Last Modified : 14 January 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.util.filesystem.DirectoryUtil;
import edu.stanford.util.filesystem.FileUtil;
import edu.stanford.util.io.FileIOUtil;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.UNDERSCORE;
import static edu.stanford.delve.Constants.EXTRA_FILES_DIR;
import static edu.stanford.delve.Constants.REMOVE_IRRELEVANT_FILES;
import static edu.stanford.delve.Constants.STUDENT_EXTRA_DIRS;
import static edu.stanford.delve.Constants.TIMESTAMP_DELIM;
import static edu.stanford.delve.Constants.TIMESTAMP_FILENAME;

/**
 * Organizes the student submissions.<br>
 * Visits the files under the submission directory and creates subfolders for
 * every student where it stores the snapshots for the given student.
 */
public class Preprocessor
{
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Processes the submission directory with the student snapshots.<br>
     * Every Java file found in the submission directory or in  any of its
     * subdirectories must have the following file name pattern:<br>
     *       {@literal <student ID>_<classname>_<snapshot number>.java}<br>
     * where {@code student ID} is the student ID (or any unique identifier),
     * the {@code classname} is typically the name of the program and the
     * {@code snapshot number} is the number of the snapshot (e.g., 1, 2, 3
     * etc.)<br>
     * Files that do not have this name pattern are ignored.<br>
     * Then this method creates a subdirectory for every student ID under the
     * submission directory and moves there the snapshots whose filename starts
     * with that student ID.<br>
     * It then renames the snapshots under every of those subfolder so that they
     * all have the same length. In other words if there are 12 snapshots then
     * their filenames will be renamed to
     * {@literal <student ID>_<classname>_<snapshot number>.java} where the
     * snapshot number = 01, 02, ..., 12.<br>
     * Any file that is not processed (i.e., file whose name does no follow the
     * naming patter, or non .java file except 'timestamps.txt'/'report.txt') is
     * moved to a subdirectory names 'skip' under the submission directory.<br>
     * Note: The 'timestamps.txt' is a file with the timestamps for the
     *       snapshots that has the following format:<br>
     *       {@literal <filename>#<timestamp in seconds>}<br>
     *       There can by multiple 'timestamps.txt' files in different
     *       directories, but there should each there should be no duplicate
     *       entries (each filename must have a unique timestamp)<br>
     * Note: At the end of the operation any files that are irrelevant are
     *       removed. Once this method executes, the submission directory
     *       contains no files, just subdirectories with the student IDs that
     *       include the program snapshots for this Istudent D and the
     *       'timestamps.txt'  but no other files or directories.
     * 
     * @param submissionDir The submission directory
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is not a directory but a
     *                           regular file or does not have read and write
     *                           access<br>
     *                        2) Error reading the file(s) with the snapshot
     *                           timestamps or one or more of them are malformed
     *                           (e.g., the delimiter '#' is missing)<br>
     *                        3) The snapshot filenames are not following the
     *                           expected notation: {@literal
     *                           <student ID>_<classname>_<snapshot number>.java
     *                           }<br>
     *                        4) Error moving or renaming a snapshot<br>
     *                        5) Error writing out the timestamps to a file in
     *                           a given subdirectory<br> 
     *                        6) Error removing any irrelevant file or directory
     */
    public static void sortSnapshots(File submissionDir)
           throws ErrorException {
        // Validate the submission directory
        DirectoryUtil.validateDirToWrite(submissionDir);
        
        // S t e p  1:
        //   Read all contents under the submission directory and find which are
        //   the student snapshots and the related timestamps
        
        // Map with an entry for every line found in file(s) 'timestamp.txt'
        // The filename (part of timestamp line up to the delimiter '#') is
        // used as the key in the map and the entire line is used as the value
        Map<String, String> timestampMap = new TreeMap<String, String>();
        
        // Find all files under the submission directory
        List<File> files = DirectoryUtil.listFilesAnyLevelDeep(submissionDir);
        
        // Create a map where the <student ID> (extracted from the snapshot
        // filenames) is the key and the value is the set of files for this
        // <student ID>
        Map<String, Set<File>> filesLookup = new TreeMap<String, Set<File>>();
        for (File file : files) {
            String filename = file.getName();
            if (filename.endsWith(JAVA_FILE_EXTENSION)) {
                String[] tokens = filename.split(UNDERSCORE);
                if (tokens.length == 3) {
                    String studentID = tokens[0];
                    Set<File> filesForID = filesLookup.get(studentID);
                    if (filesForID == null) {
                        filesForID = new TreeSet<File>();
                        filesLookup.put(studentID, filesForID);
                    }
                    filesForID.add(file);
                }
            }
            else if (filename.equals(TIMESTAMP_FILENAME)) {
                List<String> timestampLines = FileIOUtil.readFile(file,
                                                                  true,
                                                                  false,
                                                                  true,
                                                                  null);
                for (String line : timestampLines) {
                    int endIndex = line.indexOf(TIMESTAMP_DELIM);
                    if (endIndex == -1) {
                        throw new ErrorException("File '" + file.getPath()
                                               + "' is malformed. Line '"
                                               + line + "' has no delimiter '"
                                               + TIMESTAMP_DELIM + "'");
                    }
                    timestampMap.put(line.substring(0, endIndex), line);
                }
            }
        }
        
        // S t e p  2:
        //   Create a folder for every student and move the student snapshots to
        //   this folder
        createStudentSubmissionFolders(submissionDir,
                                       filesLookup,
                                       timestampMap);
        
        // S t e p  3: Cleanup
        if (REMOVE_IRRELEVANT_FILES) {
            cleanup(submissionDir, filesLookup);
        }
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Creates (if not present) a folder under the submission directory for
     * every student who submitted code and moves/puts there the code snapshots
     * for that student. It saves also a file 'timestamp.txt' with the
     * timestamps for those snapshots.
     * 
     * @param submissionDir The submission directory
     * @param filesLookup A map that uses the student ID as key and as value the
     *                    snapshot files for the given student
     * @param timestampMap Map with an entry for every line found in file(s)
     *                     'timestamp.txt'. The filename (part of timestamp line
     *                     up to the delimiter '#') is used as the key in the
     *                     map and the entire line is used as value.
     * 
     * @throws ErrorException in any of the following cases:
     *                        1) The snapshot filenames are not following the
     *                           expected notation: {@literal
     *                           <student ID>_<classname>_<snapshot number>.java
     *                           }<br>
     *                        2) Error moving or renaming a snapshot<br>
     *                        3) Error writing out the timestamps to a file in
     *                           a given subdirectory 
     * 
     */
    private static void
            createStudentSubmissionFolders(File                   submissionDir,
                                           Map<String, Set<File>> filesLookup,
                                           Map<String, String>    timestampMap)
            throws ErrorException {
        // Process the 'filesLookup' whose keys are the subdirectories of the
        // submission directory to create and the values are the files to move
        // to these subdirectories. The files must be renamed to have all the
        // same string length (for any given student ID/subdirectory)
        String submissionDirPath = submissionDir.getPath();
        for (String studentID : filesLookup.keySet()) {
            String filenameBase = getFilenameBase(filesLookup.get(studentID));
            File destDir = new File(submissionDirPath + File.separator
                                                      + studentID);
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            
            // Files that belong to <student ID> to move them to its own
            // subdirectory 
            Set<File> filesToMove = filesLookup.get(studentID);
            // Content for 'timestamps.txt' to create under this subdirectory
            // The files may be renamed which affects also 'timestamps.txt' 
            Set<String> timestampOutputLines = new TreeSet<String>();
            int numOfDigits = String.valueOf(filesToMove.size()).length();
            for (File sourceFile : filesToMove) {
                String filename = sourceFile.getName();
                int underscoreIndex = filename.lastIndexOf(UNDERSCORE);
                int endIndex = underscoreIndex + UNDERSCORE.length();
                String currFilenameBase = filename.substring(0, endIndex);
                String snapshotNumberStr = 
                      filename.substring(underscoreIndex + UNDERSCORE.length(),
                                         filename.indexOf(JAVA_FILE_EXTENSION));
                try {
                    Integer.valueOf(snapshotNumberStr);
                }
                catch (NumberFormatException notAnInteger) {
                    throw new ErrorException("Invalid file name for '"
                                           + sourceFile.getPath()
                                           + "'. The snapshot order: "
                                           + snapshotNumberStr
                                           + " is not an integer");
                }
                
                // A homework assignment may consist of multiple files that the
                // student submits. We are interested only in the snapshots of
                // the main file (i.e., file with the most snapshots). The rest
                // of the files are moved to a subfolder 'extra_files'.
                boolean isMainSnapshot = currFilenameBase.equals(filenameBase);
                
                int zerosToPrepend = numOfDigits - snapshotNumberStr.length();
                StringBuilder destFilename = new StringBuilder();
                if (zerosToPrepend != 0 && isMainSnapshot) {
                    destFilename.append(currFilenameBase);
                    if (zerosToPrepend > 0) {
                        for (int i = 0 ; i < zerosToPrepend; i++) {
                            destFilename.append("0");
                        }
                    }
                    else { // zerosToPrepend < 0
                        snapshotNumberStr =
                                snapshotNumberStr.substring(-zerosToPrepend);
                    }
                    destFilename.append(snapshotNumberStr);
                    destFilename.append(JAVA_FILE_EXTENSION);
                }
                else {
                    destFilename.append(filename);
                }
                
                String sourcePathname = sourceFile.getPath();
                String destDirPathname;
                if (isMainSnapshot) {
                    destDirPathname = destDir.getPath();
                }
                else {
                    destDirPathname = destDir.getPath() + File.separator
                                                        + EXTRA_FILES_DIR;
                    File dir = new File(destDirPathname);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                }
                String destPathname = destDirPathname + File.separator
                                                      + destFilename;
                
                // Add the timestamp for that file and if it is renamed,
                // make the necessary change there too
                String timestampLine = timestampMap.get(filename);
                if (timestampLine != null && isMainSnapshot) {
                   timestampOutputLines.add(
                                timestampLine.replace(filename,
                                                      destFilename.toString()));
                }
                
                if (!sourcePathname.equals(destPathname)) {
                    try {
                        FileUtil.moveFile(sourcePathname, destPathname);
                    }
                    catch (RuntimeException re) {
                        throw new ErrorException("Error moving '"
                                               + sourcePathname + "' to '"
                                               + destPathname   + "'. Details:"
                                               + NEW_LINE + re.getMessage());
                    }
                }
            }
            
            // If there exist extra-files copy the last snapshot for each to a
            // temporary location
            File extraFilesDir = new File(destDir.getPath() + File.separator
                                                            + EXTRA_FILES_DIR);
            if (extraFilesDir.exists()) {
                File tempDestDir = new File(STUDENT_EXTRA_DIRS + File.separator
                                                               + studentID);
                copyLastSnapshots(extraFilesDir, tempDestDir);
            }
            
            // Write out 'timestamps.txt' in the subdirectory for the given <ID>
            File timestampFile = new File(destDir.getPath() + File.separator
                                        + TIMESTAMP_FILENAME);
            FileIOUtil.writeFile(timestampFile,
                                 timestampOutputLines, // contents
                                 false,                // append
                                 true);                // addNewLine
        }
    }
    
    /**
     * Given a set with student snapshots it returns the filename base for
     * the file with the most snapshots. The filename base is just the filename
     * without the snapshot order number and the file extension (in other words
     * it is the filename up to the last underscore '_' (inclusive)).<br>
     * Note that a student submission may include multiple files each of which
     * has its own snapshots.
     * 
     * @param snapshots The set with the student snapshot files
     * 
     * @return the filename base for the file with the most snapshots
     */
    private static String getFilenameBase(Set<File> snapshots) {
        // Iterate once to find the file with the most snapshots. To do so, we
        // look at the filenames of all snapshots and create a histogram based
        // on the filename base (part of the filename that is common for all
        // snapshots of the same file)
        Map<String, Integer> histogram = new HashMap<String, Integer>();
        for (File snapshot : snapshots) {
            int endIndex = snapshot.getName().lastIndexOf(UNDERSCORE);
            String filenameBase = snapshot.getName().substring(0, endIndex + 1);
            Integer occurrences = histogram.get(filenameBase);
            if (occurrences == null) {
                histogram.put(filenameBase, 1);
            }
            else {
                histogram.put(filenameBase, occurrences + 1);
            }
        }
        
        // File name w/o the snapshot order number and the file extension
        String filenameBase = null;
        int maxOccurrences = 0;
        for (String currFilenameBase : histogram.keySet()) {
            if (histogram.get(currFilenameBase) > maxOccurrences) {
                maxOccurrences = histogram.get(currFilenameBase);
                filenameBase = currFilenameBase;
            }
        }
        
        return filenameBase;
    }
    
    /**
     * Copies the last parseable snapshot for every different module (class or
     * interface) that is found in the provided source directory
     * 
     * @param sourceDir The source directory where the snapshots exists
     * @param destDir The destination directory to copy the last parseable
     *                snapshot for every different module (class or interface)
     *                that is found in the provided source directory
     */
    private static void copyLastSnapshots(File sourceDir, File destDir) {
        // Find the last snapshot for each file
        List<File> files;
        try {
            files = DirectoryUtil.listFiles(sourceDir, JAVA_FILE_EXTENSION);
        }
        catch (ErrorException ee) { // Error retrieving the files
            return;
        }
        
        Collections.sort(files);
        
        // Map that uses as key the file name part before the snapshot order and
        // as value the last parseable snapshot among the files with that prefix
        Map<String, File> lastSnapshotLookup =
                                      new HashMap<String, File>();
        for (int i = files.size() - 1; i >= 0; i--) {
            File currFile = files.get(i);
            String filename = files.get(i).getName();
            int endIndex = filename.lastIndexOf(UNDERSCORE);
            if (endIndex != -1) {
                String prefix = filename.substring(0, endIndex);
                
                if (!lastSnapshotLookup.containsKey(prefix)) {
                    try {
                        new JavaFile(currFile.getPath());
                        // The file is parseable, add it to the lookup map
                        lastSnapshotLookup.put(prefix, currFile);
                    }
                    catch (ErrorException ignored) {
                    }
                }
            }
        }
        
        if (!lastSnapshotLookup.isEmpty()) { // Create the destination directory
            destDir.mkdirs();
        }
        
        for (File snapshot : lastSnapshotLookup.values()) {
            // Get rid of the studentID and the snapshot order in the filename
            String filename = snapshot.getName();
            int startIndex = filename.indexOf(UNDERSCORE);
            int endIndex = filename.lastIndexOf(UNDERSCORE);
            filename = filename.substring(startIndex + 1, endIndex)
                                                        + JAVA_FILE_EXTENSION;
            String destPathname = destDir.getPath() + File.separator + filename;
            try {
                FileUtil.copyFile(snapshot.getPath(), destPathname);
            }
            catch (ErrorException ee) {
                // We can't do anything if there is an error during the copy
                // Move to the next file to copy
            }
        }
    }
    
    /**
     * Removes all files under the submission directory as well as every
     * subdirectory that is unrelated/does not contain code snapshots.
     * 
     * @param submissionDir The submission directory
     * @param filesLookup A map that uses the student ID as key and as value the
     *                    snapshot files for the given student
     * 
     * @throws ErrorException in case of error cleaning up a directory
     */
    private static void cleanup(File                   submissionDir,
                                Map<String, Set<File>> filesLookup)
            throws ErrorException {
        // Cleanup: Remove everything except the subdirectories with the 
        // snapshots (Jave files) in the submission and the 'timestamp.txt'
        for (File fileOrDir : submissionDir.listFiles()) {
            if (fileOrDir.isDirectory()) {
                String dirname = fileOrDir.getName();
                if (!filesLookup.containsKey(dirname)) {
                    DirectoryUtil.removeDir(fileOrDir);
                }
            }
            else {
                fileOrDir.delete();
            }
        }
        for (String id : filesLookup.keySet()) {
            File subdir = new File(submissionDir + File.separator + id);
            Set<File> expectedFiles = filesLookup.get(id);
            // There is likely also a 'timestamps.txt' file
            expectedFiles.add(new File(subdir.getPath() + File.separator
                                                        + TIMESTAMP_FILENAME));
            // Any other file or directory in the given subdir must be deleted
            for (File fileOrDir : subdir.listFiles()) {
                if (fileOrDir.isDirectory()) {
                    DirectoryUtil.removeDir(fileOrDir);
                }
                else {
                    String filename = fileOrDir.getName();
                    if (!filename.equals(TIMESTAMP_FILENAME) &&
                        !(filename.startsWith(id) &&
                          filename.endsWith(JAVA_FILE_EXTENSION))) {
                       fileOrDir.delete();
                    }
                }
            }
        }
    }
}
