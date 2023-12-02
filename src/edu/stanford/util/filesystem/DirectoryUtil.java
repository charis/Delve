/*
 * File          : DirectoryUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 20 September 2014
 * Last Modified : 8 November 2021
 */
package edu.stanford.util.filesystem;

// Import Java SE classes
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
// Import custom classes
import edu.stanford.exception.ErrorException;
// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.TAB;
import static edu.stanford.util.filesystem.DirectoryUtil.AccessFilter.*;
/**
 * Utility class for directory-related operations
 */
public class DirectoryUtil 
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** Number of bytes in 1 KB (i.e., 1024 bytes) */
    private  static final int     KB_IN_BYTES        = 1024;
    
    // -------------------------------------- //
    //   E  N  U  M  E  R  A  T  I  O  N  S   //
    // -------------------------------------- //
    /**
     * Enumeration of all possible file access time filters
     */
    public static enum AccessFilter {
        /**
         * Filer by the time that has passed since last modification which has
         * to be less than a given value
         */
        TIME_SINCE_LAST_MODIFICATION_LESS_THAN,
        /**
         * Filer by the time that has passed since last modification which has
         * to be less or equal to a given value
         */
        TIME_SINCE_LAST_MODIFICATION_LESS_EQUALS,
        /**
         * Filer by the time that has passed since last modification which has
         * to be equal to a given value
         */
        TIME_SINCE_LAST_MODIFICATION_EQUALS,
        /**
         * Filer by the time that has passed since last modification which has
         * to be greater or equal to a given value
         */
        TIME_SINCE_LAST_MODIFICATION_GREATER_EQUALS,
        /**
         * Filer by the time that has passed since last modification which has
         * to be greater than a given value
         */
        TIME_SINCE_LAST_MODIFICATION_GREATER_THAN;
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Lists all sub-directories in the given directory. It lists only the
     * child-directories and not any sub-directories under them.
     * 
     * @param dir The directory to list its child-directories
     * 
     * @return a list with all child-directories under the given directory or an
     *         empty list if the directory does not contain any sub-directories
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions) <br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static List<File> listDirs(File dir)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(dir);
        
        File[] dirContents = dir.listFiles();
        List<File> subdirs = new ArrayList<File>(dirContents.length);
        
        for (File dirContent : dirContents) {
            if (dirContent.isDirectory()) {
                subdirs.add(dirContent);
            }
        }
        
        return subdirs;
    }
    
    /**
     * Lists all directories in the given directory any level deep.
     * 
     * @param dir The directory to list all directories under it (any level
     *            deep)
     * 
     * @return a map that uses the directory as key and as value a list with
     *         the child-directories (i.e., just child-directories; not any
     *         level deeper) or an empty list if the directory does not include
     *         any child-directories
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static Map<File, List<File>> listDirsRecursive(File dir)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(dir);
        Map<File, List<File>> directoryMap = new HashMap<File, List<File>>();
        
        DirectoryUtil.listDirsRecursive(dir, directoryMap);
        return directoryMap;
    }
    
    /**
     * Lists all regular files in the given directory including any of its
     * sub-directories any level deep.
     * 
     * @param dir The directory to list its regular files (any level deep)
     * 
     * @return a list with all under the given directory including any of its
     *         sub-directories any level deep or an empty list if there are no
     *         regular files
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static List<File> listFilesAnyLevelDeep(File dir)
           throws ErrorException {
        Map<File, List<File>> dirFilesMap = listFilesRecursive(dir);
        Set<File> dirs = dirFilesMap.keySet();
        
        List<File> allFiles = new LinkedList<File>();
        List<File> currDirFiles;
        for (File currDir : dirs) {
            currDirFiles = dirFilesMap.get(currDir);
            if (!currDirFiles.isEmpty()) {
                allFiles.addAll(currDirFiles);
            }
        }
        
        return allFiles;
    }
    
    /**
     * Lists all regular files in the given directory. It lists only the
     * child-file and not files in any of the sub-directories.
     * 
     * @param dir The directory to list its child-files
     * 
     * @return a list with all child-files under the given directory or an empty
     *         list if the directory does not contain any regular files
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static List<File> listFiles(File dir)
           throws ErrorException {
        return DirectoryUtil.listFiles(dir, null);
    }
    
    /**
     * Lists all regular files in the given directory. It lists only the
     * child-file and not files in any of the sub-directories.
     * 
     * @param dir The directory to list its child-files
     * @param fileExtension The expected extension that a regular file should
     *                      have in order to be in the list of regular files
     *                      that is returned by this method or {@code null} in
     *                      order to skip this check
     * 
     * @return a list with all child-files under the given directory that end
     *         with the specified file extension or an empty list if the
     *         directory does not contain any such files
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static List<File> listFiles(File   dir,
                                       String fileExtension)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(dir);
        
        File[] dirContents = dir.listFiles();
        List<File> files = new ArrayList<File>(dirContents.length);
        
        if (fileExtension != null) {
            fileExtension = fileExtension.trim().toLowerCase();
            
            for (File dirContent : dirContents) {
                if (dirContent.isFile() &&
                    dirContent.getName().toLowerCase().endsWith(fileExtension)){
                    files.add(dirContent);
                }
            }
        }
        else {
            for (File dirContent : dirContents) {
                if (dirContent.isFile()) {
                    files.add(dirContent);
                }
            }
        }
        
        return files;
    }
    
    /**
     * Lists all regular files in the given directory or any of the directories
     * under it any level deep.<br>
     * The returned map uses the directory as key and as value a list with
     * the child-files (i.e., just child-files; not any level deeper) that:<br>
     * <pre>
     *   1) Have read access permissions
     *   2) End with one of the extensions in the specified array of expected
     *      extensions (if {@code null} all extensions are considered)
     *   3) Have file size less or equal to the specified upper limit (if any)
     *   4) Are last modified within the specified time (if any)
     * </pre> 
     * 
     * @param dir The directory to list all files under it (any level deep)
     * @param fileExtensions The list of expected extensions that a regular file
     *                       should have in order to be in the list of regular
     *                       files that is returned by this method or
     *                       {@code null} in order to skip this check while
     *                       determining whether the files should be copied
     * @param maxFilesizeInKB The maximum size in KB that a regular file can
     *                        have in order to be copied. Files with larger size
     *                        are skipped. If {@code null}, a regular file that
     *                        meets the rest of the requirements is copied
     *                        regardless its file size.
     * @param timeSinceLastModification The upper bound for the time since the
     *                                  last modification that a regular file
     *                                  should have in order to be copied or
     *                                  {@code null} in order to skip this check
     *                                  while determining whether the files
     *                                  should be copied
     * 
     * @return a map that uses as key the directory (which can be any level
     *         deep) and as value a list with the child-files (i.e., just
     *         child-files; not any level deeper) that<br>
     *         1) Have read access permissions<br>
     *         2) End with one of the extensions in the specified array of
     *            expected extensions (if {@code null} all extensions are
     *            considered)<br>
     *         3) Have file size less or equal to the specified upper limit
     *            (if any)<br>
     *         4) Are last modified within the specified time (if any) 
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static Map<File, List<File>>
                            listFiles(File     dir,
                                      String[] fileExtensions,
                                      Long     maxFilesizeInKB,
                                      Long     timeSinceLastModification)
            throws ErrorException {
        DirectoryUtil.validateDirToRead(dir);
        
        Map<File, List<File>> dirFilesMap = new HashMap<File, List<File>>();
        
        DirectoryUtil.listFilesRecursive(dir,
                                         dirFilesMap,
                                         fileExtensions,
                                         maxFilesizeInKB,
                                         timeSinceLastModification);
        
        return dirFilesMap;
    }
    
    /**
     * Lists all regular files with the specified extension in the given
     * directory or any of the directories under it any level deep.
     * 
     * @param dir The directory to list all files under it (any level deep)
     * @param fileExt The extension that a file must have to be included in the
     *                returned map values
     * 
     * @return a map that uses the directory as key and as value a list with
     *         the child-files (i.e., just child-files; not any level deeper) or
     *         an empty list if the directory does not include any child-files
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     *                        6) The provided file extension is {@code null} or
     *                           empty string
     */
    public static Map<File, List<File>> listFilesRecursive(File   dir,
                                                           String fileExt)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(dir);
        if (fileExt == null || fileExt.trim().isEmpty()) {
            throw new ErrorException("The file extension is null or empty");
        }
        
        Map<File, List<File>> dirFilesMap = new HashMap<File, List<File>>();
        String[] fileExtensions = new String[] { fileExt };
        DirectoryUtil.listFilesRecursive(dir,
                                         dirFilesMap,
                                         fileExtensions,
                                         null,  // maxFilesizeInKB
                                         null); // timeSinceLastModification
        
        return dirFilesMap;
    }
    
    /**
     * Lists all regular files in the given directory or any of the directories
     * under it any level deep.
     * 
     * @param dir The directory to list all files under it (any level deep)
     * 
     * @return a map that uses the directory as key and as value a list with
     *         the child-files (i.e., just child-files; not any level deeper) or
     *         an empty list if the directory does not include any child-files
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static Map<File, List<File>> listFilesRecursive(File dir)
           throws ErrorException {
        DirectoryUtil.validateDirToRead(dir);
        Map<File, List<File>> dirFilesMap = new HashMap<File, List<File>>();
        
        DirectoryUtil.listFilesRecursive(dir,
                                         dirFilesMap,
                                         null,
                                         null,
                                         null);
        return dirFilesMap;
    }
    
    /**
     * Given a directory path name it creates the directory.<br>
     * Pre-condition: The parent directory already exists and has write access
     * 
     * @param dir The directory to create
     * @param createParentDirs {@code true} to create the parent directories if
     *                         they do not exist or {@code false} to create the
     *                         directory only if the parent directories already
     *                         exist (i.e., do not try to create them)
     * 
     * @throws ErrorException in any of the following cases<br>
     *                        1) The given directory is {@code null}<br>
     *                        2) The given director path name exists but it maps
     *                           to a regular file<br>
     *                        3) The parent directory does not exist or does not
     *                           have write access<br>
     *                        4) An error happens while creating the directory
     */
    public static void createDir(File    dir,
                                 boolean createParentDirs)
           throws ErrorException {
        if (dir == null) {
            throw new ErrorException("The directory to create is null");
        }
        
        try {
            if (dir.exists()) {
                if (dir.isDirectory()) {
                    return; // Noting to do
                }
                else {
                    throw new ErrorException("'" + dir.getPath() 
                                           + "' is not a directory");
                }
            }
        }
        catch (SecurityException se) {
            throw new ErrorException("Access to '" + dir.getPath()
                                   + "' denied");
        }
        
        // Now try to create the directory
        try {
            if (createParentDirs) {
                File parentDir = dir.getParentFile();
                
                // Base case: Root directory
                if (parentDir == null) {
                    return;
                }
                
                // Recursive step
                createDir(parentDir, true);
            }
            
            if (!dir.mkdir()) {
                throw new ErrorException("Failed to create directory '"
                                       + dir.getPath() + "'");
            }
        }
        catch (SecurityException se) {
            throw new ErrorException("'" + dir.getPath()
                                   + "' cannot be created. Details:" + NEW_LINE
                                   + se.getMessage());
        }
    }
    
    /**
     * Renames all files or directories under the specified directory whose name
     * starts with a given prefix using a new name prefix for their names.<br>
     * Note: The rename is non-recursive. It applies only to the sub-directories
     *       or files right under the provided directory (but not under any of
     *       its sub-directories).
     * 
     * @param dir The directory that contains the files or directories to rename
     * @param fromNamePrefix The prefix that the file or directory names should
     *                       have in order to be renamed
     * @param toNamePrefix The new prefix that those file or directory names
     *                     will have once renamed
     * @param isCaseSensitive {@code true} for case-sensitive comparison or
     *                        {@code false} otherwise 
     * @param renameDirs {@code true} to rename directories or {@code false} to
     *                   exclude all directories from the rename operation   
     * @param renameFiles {@code true} to rename files or {@code false} to
     *                    exclude all files from the rename operation
     *    
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The given directory is {@code null}<br>
     *                        2) The given directory path name does not exist
     *                           in the file system<br>
     *                        3) The given director path name exists but it maps
     *                           to a regular file<br>
     *                        4) The given directory cannot be opened for
     *                           writing (i.e., has not write access
     *                           permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the given
     *                           directory<br>
     *                        6) In case of runtime error during the rename<br>
     *                        7) In case the rename operation is not successful
     *                           (e.g. the desired path for the rename already
     *                           exists and the file system denies to rename the
     *                           file or the directory, or it is in use etc.)
     * 
     */
    public static void renameDirContents(File    dir,
                                         String  fromNamePrefix,
                                         String  toNamePrefix,
                                         boolean isCaseSensitive,
                                         boolean renameDirs,
                                         boolean renameFiles)
            throws ErrorException {
        if (!renameDirs && !renameFiles) {
            // There is nothing to rename => NOP
            return;
        }
        
        DirectoryUtil.validateDirToWrite(dir);
        
        File[] dirContents;
        try {
            dirContents = dir.listFiles();
        }
        catch (SecurityException se) {
            throw new ErrorException("Access to '" + dir.getPath()
                                   + "' denied");
        }
        
        String name;
        for (File dirContent : dirContents) {
            name = dirContent.getName();
            boolean match;
            if (isCaseSensitive) {
                match = name.startsWith(fromNamePrefix);
            }
            else {
                match = name.toLowerCase().startsWith(
                                                 fromNamePrefix.toLowerCase());
            }
            
            // The prefix matches the one we want to rename
            // Now, check if there are any restrictions
            try {
                if (match) {
                    // If the content is a directory make sure that renameDirs
                    // is true (i.e., we want to rename directories)
                    if (dirContent.isDirectory()) {
                        if (renameDirs) {
                            // Rename this directory
                            name = toNamePrefix +
                                   name.substring(fromNamePrefix.length());
                            File renamedDir = new File(dir.getPath()
                                                     + File.separator + name);
                            dirContent.renameTo(renamedDir);
                        }
                    }
                    // If the content is a file make sure that renameFiles is
                    // true (i.e., we want to rename regular files)
                    else {
                        if (renameFiles) {
                            // Rename this regular file
                            name = toNamePrefix +
                                   name.substring(fromNamePrefix.length());
                            File renamedFile = new File(dir.getPath()
                                                      + File.separator + name);
                            dirContent.renameTo(renamedFile);
                        }
                    }
                }
            }
            catch (RuntimeException re) {
                throw new ErrorException("Error while renaming file '"
                                       + dirContent.getPath()
                                       + "'. Details:" + NEW_LINE
                                       + re.getMessage());
            }
        }
    }
    
    /**
     * Removes a directory and all its contents.<br>
     * Any softlinks are also removed, but not the softlink targets.
     * 
     * @param dirPathname The absolute path of the directory to remove
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided pathname is {@code null}<br>
     *                        2) A security manager exists and denies write
     *                           access to the directory<br>
     *                        3) The provided pathname denotes a regular file
     *                           rather than a directory<br>
     *                        4) One or more directory contents are not removed
     */
    public static void removeDir(String dirPathname)
            throws ErrorException {
        DirectoryUtil.removeDir(dirPathname, false);
    }
    
    /**
     * Removes a directory and all its contents.<br>
     * Any softlinks are also removed, but not the softlink targets.
     * 
     * @param dir The directory to remove
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) A security manager exists and denies write
     *                           access to the directory<br>
     *                        3) The provided pathname denotes a regular file
     *                           rather than a directory<br>
     *                        4) One or more directory contents are not removed
     */
    public static void removeDir(File dir)
            throws ErrorException {
        DirectoryUtil.removeDir(dir, false);
    }
    
    /**
     * Removes a directory and all its contents.<br>
     * Any softlinks are also removed. The softlink targets can be also removed
     * if the {@code followLinks} is set.
     * 
     * @param dirPathname The absolute path of the directory to remove
     * @param followLinks {@code true} to follow softlinks (removes the linked
     *                    file too) or {@code false} to not follow softlinks
     *                    (removes only the softlink) 
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided pathname is {@code null}<br>
     *                        2) A security manager exists and denies write
     *                           access to the directory<br>
     *                        3) The provided pathname denotes a regular file
     *                           rather than a directory<br>
     *                        4) One or more directory contents are not removed
     */
    public static void removeDir(String  dirPathname,
                                 boolean followLinks)
           throws ErrorException {
        DirectoryUtil.validateDirToWrite(dirPathname);
        if (dirPathname == null) {
            throw new ErrorException("The path name for the directory to "
                                   + "remove is null");
        }
        
        Path dirPath = Paths.get(dirPathname);
        DirectoryUtil.removeDir(dirPath, followLinks);
    }
    
    /**
     * Removes a directory and all its contents.<br>
     * Any softlinks are also removed. The softlink targets can be also removed
     * if the {@code followLinks} is set.
     * 
     * @param dir The directory to remove
     * @param followLinks {@code true} to follow softlinks (removes the linked
     *                    file too) or {@code false} to not follow softlinks
     *                    (removes only the softlink) 
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) A security manager exists and denies write
     *                           access to the directory<br>
     *                        3) The provided pathname denotes a regular file
     *                           rather than a directory<br>
     *                        4) One or more directory contents are not removed
     */
    public static void removeDir(File    dir,
                                 boolean followLinks)
           throws ErrorException {
        if (dir == null) {
            throw new ErrorException("The directory to remove is null");
        }
        
        Path dirPath = Paths.get(dir.getPath());
        DirectoryUtil.removeDir(dirPath, followLinks);
    }
    
    /**
     * Removes a directory and all its contents.<br>
     * Any softlinks are also removed, but not the softlink targets.
     * 
     * @param dirPath The path of the directory to remove
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided pathname is {@code null}<br>
     *                        2) A security manager exists and denies write
     *                           access to the directory<br>
     *                        3) The provided pathname denotes a regular file
     *                           rather than a directory<br>
     *                        4) One or more directory contents are not removed
     */
    public static void removeDir(Path dirPath)
            throws ErrorException {
        DirectoryUtil.removeDir(dirPath, false);
    }
    
    /**
     * Removes a directory and all its contents.<br>
     * Any softlinks are also removed. The softlink targets can be also removed
     * if the {@code followLinks} is set.
     * 
     * @param dirPath The path of the directory to remove
     * @param followLinks {@code true} to follow softlinks (removes the linked
     *                    file too) or {@code false} to not follow softlinks
     *                    (removes only the softlink) 
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided pathname is {@code null}<br>
     *                        2) A security manager exists and denies write
     *                           access to the directory<br>
     *                        3) The provided pathname denotes a regular file
     *                           rather than a directory<br>
     *                        4) One or more directory contents are not removed
     */
    public static void removeDir(Path    dirPath,
                                 boolean followLinks)
           throws ErrorException {
        if (dirPath == null) {
            throw new ErrorException("The directory path to remove is null");
        }
        File dir = dirPath.toFile();
        
        if (!dir.exists()) {
            // The directory does not exist, we are done!
            return;
        }
        
        DirectoryUtil.validateDirToWrite(dirPath.toFile());
        
        try {
            if (followLinks) {
                Files.walk(dirPath, FileVisitOption.FOLLOW_LINKS)
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
            else {
                Files.walk(dirPath)
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
        }
        catch (IOException ioe) {
            throw new ErrorException("Error deleting directory '"
                                   + dirPath.toAbsolutePath() + "'. Details:"
                                   + NEW_LINE
                                   + ioe.getMessage());
        }
    }

    /**
     * Removes a directory and all its contents.
     * 
     * @param dirPathname The absolute path of the directory to remove
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided pathname is {@code null}<br>
     *                        2) A security manager exists and denies write
     *                           access to the directory<br>
     *                        3) The provided pathname denotes a regular file
     *                           rather than a directory<br>
     *                        4) One or more directory contents are not removed
     * 
     * @deprecated since 1.7 See {@link #removeDir(String)}
     */
    public static void removeDir_preJDK7(String dirPathname)
              throws ErrorException {
        DirectoryUtil.validateDirToWrite(dirPathname);
        
        File dir = new File(dirPathname);
        List<File> nonRemovedContents = DirectoryUtil.removeDir_preJDK7(dir);
        if (nonRemovedContents.size() > 0) {
            StringBuilder errorMessage =
                          new StringBuilder("The following directories and/or "
                                          + "files cannot be not removed:");
            for (File nonRemovedContent : nonRemovedContents) {
                errorMessage.append(NEW_LINE + TAB + nonRemovedContent);
            }
            
            throw new ErrorException(errorMessage.toString());
        }
    }
    
    /**
     * Validates a directory to read.<br>
     * It makes sure that the directory exists, it is a directory (or a symbolic
     * link that points to a directory) and not a regular file and it has read
     * access permissions.
     * 
     * @param dirPathname The absolute path name of the directory to validate
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided path is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static void validateDirToRead(String dirPathname)
            throws ErrorException {
        if (dirPathname == null) {
            throw new ErrorException("The directory path name is null");
        }
        
        File dir = new File(dirPathname.trim()); // Trim the path just in case
        validateDirToRead(dir);
    }
    
    /**
     * Validates a directory to read.<br>
     * It makes sure that the directory exists, it is a directory (or a symbolic
     * link that points to a directory) and not a regular file and it has read
     * access permissions.
     * 
     * @param dir The directory to validate
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static void validateDirToRead(File dir)
            throws ErrorException {
        if (dir == null) {
            throw new ErrorException("The directory is null");
        }
        
        try {
            if (!dir.exists()) {
                throw new ErrorException("'" + dir.getPath()
                                       + "' does not exist");
            }
            
            if (!dir.isDirectory()) {
                throw new ErrorException("'" + dir.getPath() 
                                       + "' is not a directory");
            }
            
            if (!dir.canRead()) {
                throw new ErrorException("Directory '" + dir.getPath()
                                       + "' has not read access permissions");
            }
        }
        catch (SecurityException se) {
            throw new ErrorException("Access to '" + dir.getPath()
                                   + "' denied");
        }
    }
    
    /**
     * Validates a directory to write.<br>
     * It makes sure that the directory exists, it is a directory (or a symbolic
     * link that points to a directory) and not a regular file and has write
     * access permissions.
     * 
     * @param dirPathname The absolute path name of the directory to validate
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for writing (i.e., does not
     *                           have write access permissions) <br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static void validateDirToWrite(String dirPathname)
           throws ErrorException {
        if (dirPathname == null) {
            throw new ErrorException("The directory path name is null");
        }
        
        File dir = new File(dirPathname.trim()); // Trim the path just in case
        validateDirToWrite(dir);
    }
    
    /**
     * Validates a directory to write.<br>
     * It makes sure that the directory exists, it is a directory (or a symbolic
     * link that points to a directory) and not a regular file and has write
     * access permissions.
     * 
     * @param dir The directory to validate
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for writing (i.e., does not
     *                           have write access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    public static void validateDirToWrite(File dir)
           throws ErrorException {
        if (dir == null) {
            throw new ErrorException("The directory is null");
        }
        
        try {
            if (!dir.exists()) {
                throw new ErrorException("'" + dir.getPath()
                                       + "' does not exist");
            }
            
            if (!dir.isDirectory()) {
                throw new ErrorException("'" + dir.getPath() 
                                       + "' is not a directory");
            }
            
            if (!dir.canWrite()) {
                throw new ErrorException("Directory '" + dir.getPath()
                                       + "'has not write access permissions");
            }
        }
        catch (SecurityException se) {
            throw new ErrorException("Access to '" + dir.getPath()
                                   + "' denied");
        }
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Lists all directories in the given directory any level deep.
     * 
     * @param dir The directory to list all directories under it any level deep
     * @param directoryMap The map to store the results that uses the directory
     *                     as key and as value a list with the child-directories
     *                     (i.e., just child-directories; not any level deeper)
     *                     or an empty list if the directory does not include
     *                     any child-directories
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        2) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    private static void listDirsRecursive(File                  dir,
                                          Map<File, List<File>> directoryMap)
           throws ErrorException {
        try {
            if (!dir.canRead()) {
                throw new ErrorException("Directory '" + dir.getPath()
                                       + "' has not read access permissions");
            }
        }
        catch (SecurityException se) {
            throw new ErrorException("Access to '" + dir.getPath()
                                   + "' denied");
        }
        
        File[] dirContents = dir.listFiles();
        List<File> childDirs = new ArrayList<File>(dirContents.length);
        
        for (File dirContent : dirContents) {
            if (dirContent.isDirectory()) {
                // Recursive call
                DirectoryUtil.listDirsRecursive(dirContent, directoryMap);
                
                // At this point, we have processed the child-directory
                // completely. Add it to the list with the child-directories.
                childDirs.add(dirContent);
            }
        }
        
        // At this point, we have processed the directory completely. Add it to
        // the map with the results.
        directoryMap.put(dir, childDirs);
    }
    
    /**
     * Removes a directory recursively.
     * 
     * @param dir The directory to remove
     * 
     * @return a list with all directory (including any of its sub-directories)
     *         contents that are not removed
     */
    private static List<File> removeDir_preJDK7(File dir) {
        List<File> nonRemovedContents = new ArrayList<File>();
        File[] dirContents;
        try {
            dirContents = dir.listFiles();
        }
        catch (SecurityException se) {
            // Cannot list directory contents (access denied)
            nonRemovedContents.add(dir);
            return nonRemovedContents;
        }
        
        for (File dirContent : dirContents) {
            try {
                if (dirContent.isDirectory()) {
                    nonRemovedContents.addAll(removeDir_preJDK7(dirContent));
                }
                else {
                    if(!dirContent.delete()) {
                        nonRemovedContents.add(dirContent);
                    }
                }
            }
            catch (SecurityException se) {
                nonRemovedContents.add(dirContent);
            }
        }
        
        // If all directory contents are removed successfully, remove the
        // directory itself
        if (dir.listFiles().length == 0) {
            if(!dir.delete()) {
                nonRemovedContents.add(dir);
            }
        }
        else {
            // Otherwise, it means the one or more contents are not removed and
            // therefore we cannot remove the directory
            nonRemovedContents.add(dir);
        }
        
        return nonRemovedContents;
    }
    
    /**
     * Lists all regular files in the given directory or any of the directories
     * under it any level deep.
     * 
     * @param dir The directory to list all files under it any level deep
     * @param dirFilesMap The map to store the results that uses the directory
     *                    as key and as value a list with the child-files
     *                    (i.e., just child-files; not any level deeper) or an
     *                    empty list in any of the following cases:<br>
     *                    1) The directory does not include any child-files<br>
     *                    2) There exist no files that end with the expected
     *                       extensions<br>
     *                    3) There exist no files with file-size less or equal
     *                       to the specified file-size limit<br>
     *                    4) There exist no files that are last modified within
     *                       the specified time 
     * @param fileExtensions The list of expected extensions that a regular file
     *                       should have in order to be in the list of regular
     *                       files that is returned by this method or
     *                       {@code null} in order to skip this check while
     *                       determining whether the files should be copied
     * @param maxFilesizeInKB The maximum size in KB that a regular file can
     *                        have in order to be copied. Files with larger size
     *                        are skipped. If {@code null}, a regular file that
     *                        meets the rest of the requirements is copied
     *                        regardless its file size.
     * @param timeSinceLastModification The upper bound for the time since the
     *                                  last modification that a regular file
     *                                  should have in order to be copied or
     *                                  {@code null} in order to skip this check
     *                                  while determining whether the files
     *                                  should be copied
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided directory is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a regular file<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the directory
     */
    private static void
            listFilesRecursive(File                  dir,
                               Map<File, List<File>> dirFilesMap,
                               String[]              fileExtensions,
                               Long                  maxFilesizeInKB,
                               Long                  timeSinceLastModification)
            throws ErrorException {
        try {
            if (!dir.canRead()) {
                throw new ErrorException("Directory '" + dir.getPath()
                                       + "' has not read access permissions");
            }
        }
        catch (SecurityException se) {
            throw new ErrorException("Access to '" + dir.getPath()
                                   + "' denied");
        }
        
        final Long MAX_LENGTH = (maxFilesizeInKB == null) ?
                                 null :
                                 maxFilesizeInKB * KB_IN_BYTES;
        
        File[] dirContents = dir.listFiles();
        List<File> childFiles = new LinkedList<File>();
        for (File dirContent : dirContents) {
            if (dirContent.isDirectory()) {
                // Recursive call
                DirectoryUtil.listFilesRecursive(dirContent,
                                                 dirFilesMap,
                                                 fileExtensions,
                                                 maxFilesizeInKB,
                                                 timeSinceLastModification);
            }
            else if (dirContent.isFile()) {
                // Add the regular file to the list with the child-files
                if (timeSinceLastModification == null ||
                    filterBy(dirContent, 
                             false, // don't care (b/c not a directory)
                             timeSinceLastModification,
                             TIME_SINCE_LAST_MODIFICATION_LESS_EQUALS)) {
                    boolean addFile = MAX_LENGTH == null ||
                                      dirContent.length() <= MAX_LENGTH;
                    
                    if (addFile) {
                        if (fileExtensions != null) {
                            if(FileUtil.extensionMatch(dirContent,
                                                       fileExtensions)) {
                                childFiles.add(dirContent);
                            }
                        }
                        else {
                            childFiles.add(dirContent);
                        }
                    }
                }
            }
        }
        
        // The directory is completely processed here => add it to the map
        dirFilesMap.put(dir, childFiles);
    }
    
    /**
     * Compares the time passed since a file was last modified with a given time
     * and return {@code true} if the access filter is satisfied or
     * {@code false} otherwise.<br><br>
     * 
     * An access filter consists of two parts: the first part is the time that
     * has passed since the file was last modified and the second part is the
     * filter when comparing with a second time.<br>
     * In other words if the time that has passed since the file was last
     * modified is X (in msec) and the time to compare with is Y (in msec) then
     * the comparison relationship (less than, less or equal to, equal to,
     * greater or equal to, greater than the time to compare with) determines
     * whether the file is filtered out.<br>
     * For example, if X {@literal >} Y and the access filter is
     * TIME_SINCE_LAST_MODIFICATION_LESS_EQUALS then this method return
     * {@code false}<br>
     * However if the access filter is TIME_SINCE_LAST_MODIFICATION_GREATER_THAN
     * then this method returns {@code true}.
     * 
     * @param file The file to apply the access filter
     * @param checkDirContentsIfDir {@code true} to check the directory contents
     *                              (any level deep) to determine whether the
     *                              directory passes the comparison (in case the
     *                              file is a directory and not a regular file;
     *                              if it is a regular file it has no effect)
     * @param timeToCompareInMillis The time to compare with
     * @param filter The access filter
     * 
     * @return {@code true} if the file passes the comparison or {@code false}
     *         if the file is filtered out
     *         
     * @throws SecurityException in case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the file
     */
    private static boolean filterBy(File         file,
                                    boolean      checkDirContentsIfDir,
                                    long         timeToCompareInMillis,
                                    AccessFilter filter) {
        // In case of a directory we can either look at the directory creation,
        // modification, last access time etc. to determine whether the
        // directory passes the comparison or we can look at all its contents
        // (any level deep) and if one of them passes the comparison then the
        // directory passes the comparison as well.
        // If checkDirContentsIfDir is 'true' that's exactly what happens. We
        // look at all directory contents and if one passes the comparison we
        // return true
        if (checkDirContentsIfDir) {
            try {
                if (file.isDirectory()) {
                    File[] dirContents = file.listFiles();
                    for (int i = 0; i < dirContents.length; i++) {
                        if (DirectoryUtil.filterBy(dirContents[i],
                                                   true,
                                                   timeToCompareInMillis,
                                                   filter)) {
                            return true;
                        }
                    }
                }
            }
            catch (SecurityException ignored) {
                // Just skip this extensive check and use only on the current
                // directory (instead of its contents) to determine the return
                // value
            }
        }
        
        long timePassed = System.currentTimeMillis() - file.lastModified();
        
        switch (filter) {
            case TIME_SINCE_LAST_MODIFICATION_LESS_THAN:
                 return timePassed < timeToCompareInMillis;
            case TIME_SINCE_LAST_MODIFICATION_LESS_EQUALS:
                 return timePassed <= timeToCompareInMillis;
            case TIME_SINCE_LAST_MODIFICATION_EQUALS:
                 return timePassed == timeToCompareInMillis;
            case TIME_SINCE_LAST_MODIFICATION_GREATER_EQUALS:
                 return timePassed >= timeToCompareInMillis;
            case TIME_SINCE_LAST_MODIFICATION_GREATER_THAN:
                 return timePassed > timeToCompareInMillis;
            default:
                 return false; // Ignored 
        }
    }
}