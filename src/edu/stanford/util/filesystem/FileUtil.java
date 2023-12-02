/*
 * File          : FileUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 13 June 2013
 * Last Modified : 22 April 2021
 */
package edu.stanford.util.filesystem;

// Import Java SE classes
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.util.OSPlatform;

// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;

/**
 * Utility class for file-related operations
 */
public class FileUtil 
{
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Given a path name of a regular file to copy and a destination path name
     * it copies that file.
     * 
     * @param sourcePathname The absolute path of the file to copy
     * @param destPathname The absolute path of the destination file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source or destination path name is
     *                           {@code null}<br>
     *                        2) The source path name does not exist in the file
     *                           system<br>
     *                        3) The source path name exists but it maps to a
     *                           directory<br>
     *                        4) The source file cannot be opened for reading 
     *                           (i.e., does has not read access permissions)<br>
     *                        5) The destination path name exists but it maps to
     *                           a directory<br>
     *                        6) The destination file cannot be opened for
     *                           writing (i.e., has not write access
     *                           permissions)<br>
     *                        7) In case of I/O error during the copy
     */
    public static void copyFile(String sourcePathname, String destPathname)
           throws ErrorException {
        validateFileToRead(sourcePathname);
        validateFileToWrite(destPathname);
        
        Path source = Paths.get(sourcePathname);
        Path dest   = Paths.get(destPathname);
        try {
            Files.copy(source, dest, StandardCopyOption.COPY_ATTRIBUTES);
        }
        catch (IOException ioe) {
            throw new ErrorException("Error while copying file '"
                                   + sourcePathname + "' to destination '"
                                   + destPathname + "'. Details:" + NEW_LINE
                                   + ioe.getMessage());
        }
    }
    
    /**
     * It copies the given file to the specified destination.
     * 
     * @param sourceFile The file to copy
     * @param destFile The destination file
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source or destination path name is
     *                           {@code null}<br>
     *                        2) The source path name does not exist in the file
     *                           system<br>
     *                        3) The source path name exists but it maps to a
     *                           directory<br>
     *                        4) The source file cannot be opened for reading 
     *                           (i.e., does has not read access permissions)<br>
     *                        5) The destination path name exists but it maps to
     *                           a directory<br>
     *                        6) The destination file cannot be opened for
     *                           writing (i.e., has not write access
     *                           permissions)<br>
     *                        7) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the source of
     *                           the destination file<br>
     *                        8) In case of I/O error during the copy
     *
     * @deprecated since 1.7 See {@link Files#copy(Path, Path, CopyOption...)}
     */
    public static void copyFile(File sourceFile, File destFile)
           throws ErrorException {
        validateFileToRead(sourceFile);
        validateFileToWrite(destFile);
        
        if(!destFile.exists()) {
            try {
                destFile.createNewFile();
            }
            catch (IOException ioe) {
                throw new ErrorException("Cannot create the destination file '"
                                        + destFile.getPath() + "'");
            }
        }
        
        FileInputStream  inputStream       = null;
        FileChannel      sourceFileChannel = null;
        FileOutputStream outputStream      = null;
        FileChannel      destFileChannel   = null;
        try {
            inputStream       = new FileInputStream(sourceFile);
            sourceFileChannel = inputStream.getChannel();
            outputStream      = new FileOutputStream(destFile);
            destFileChannel   = outputStream.getChannel();
            
            // Note: destination.transferFrom(source, 0, source.size());
            //       can lead to infinite-loop. To avoid infinite loops, we use:
            long count = 0;
            long size  = sourceFileChannel.size();
            while ((count += destFileChannel.transferFrom(sourceFileChannel,
                                                      count,
                                                      size - count)) < size) {
            };
        }
        catch (FileNotFoundException impossible) { // Should not happen
            throw new ErrorException("Internal error. Details: " + NEW_LINE
                                   + impossible.getMessage());
        }
        catch (RuntimeException impossible) { // Should not happen
            throw new ErrorException("Internal error. Details: " + NEW_LINE
                                   + impossible.getMessage());
        }
        catch (IOException ioe) {
            throw new ErrorException("Error while copying file '"
                                   + sourceFile.getPath() + "' to destination '"
                                   + destFile.getPath() + "'. Details:"
                                   + NEW_LINE + ioe.getMessage());
        }
        finally {
            if(sourceFileChannel != null) {
                try {
                   sourceFileChannel.close();
                }
                catch(IOException ignored) {}
            }
            if(destFileChannel != null) {
                try {
                    destFileChannel.close();
                }
                catch(IOException ignored) {}
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ignored) {}
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException ignored) {}
            }
        }
    }
    
    /**
     * Given a path name of a regular file to copy and a destination path name
     * it copies that file.
     * 
     * @param sourcePathname The absolute path of the file to copy
     * @param destPathname The absolute path of the destination file
     * @param options Standard copy options or options as to how symbolic links
     *                are handled
     *                @see java.nio.file.StandardCopyOption
     *                @see java.nio.file.LinkOption
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source or destination path name is
     *                           {@code null}<br>
     *                        2) The source path name does not exist in the file
     *                           system<br>
     *                        3) The source path name exists but it maps to a
     *                           directory<br>
     *                        4) The source file cannot be opened for reading 
     *                           (i.e., has not read access permissions)<br>
     *                        5) The destination path name exists but it maps to
     *                           a directory<br>
     *                        6) The destination file cannot be opened for
     *                           writing (i.e., has not write access
     *                           permissions)<br>
     *                        7) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the source of
     *                           the destination file<br>
     *                        8) In case of I/O error during the copy
     *
     */
    public static void copyFile(String       sourcePathname,
                                String       destPathname,
                                CopyOption...options)
            throws ErrorException {
         validateFileToRead(sourcePathname);
         validateFileToWrite(destPathname);
         
         Path sourceFilePath;
         Path destFilePath;
         try {
             sourceFilePath = Paths.get(sourcePathname);
             destFilePath   = Paths.get(destPathname);
         }
         catch (InvalidPathException impossible) {
             // Just in case...
             System.out.println(impossible.getMessage());
             return;
         }
         
         try {
             Files.copy(sourceFilePath,
                        destFilePath,
                        options);
         }
         catch (IOException ioe) {
             throw new ErrorException("Error while copying file '"
                                    + sourcePathname + "' to destination '"
                                    + destPathname + "'. Details:" + NEW_LINE
                                    + ioe.getMessage());
         }
    }
    
    /**
     * Given a source and a destination path name of a regular file it moves or
     * renames the file from the source path to the destination path.
     * 
     * @param sourcePathname The absolute path of the file to move/rename
     * @param destPathname The absolute path of the destination file after the
     *                     operation
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source or destination path name is
     *                           {@code null}<br>
     *                        2) The source path name does not exist in the file
     *                           system<br>
     *                        3) The source path name exists but it maps to a
     *                           directory<br>
     *                        4) The source file cannot be opened for reading
     *                           (i.e., has not read access permissions)<br>
     *                        5) The destination path name exists but it maps to
     *                           a directory<br>
     *                        6) The destination file cannot be opened for
     *                           writing (i.e., has not write access
     *                           permissions)<br>
     *                        7) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the source of
     *                           the destination file<br>
     *                        8) In case of runtime error during the move/rename
     *                           <br>
     *                        9) In case the move/rename operation is not
     *                           successful (e.g. the destination path already
     *                           exists and the file system denies to rename the
     *                           file or the source or destination file is in
     *                           use etc.)
     */
    public static void moveFile(String sourcePathname, String destPathname)
           throws ErrorException {
        validateFileToRead(sourcePathname);
        validateFileToWrite(destPathname);
        
        if (sourcePathname.equalsIgnoreCase(destPathname)) {
            return; // Source and destination paths are same => nothing to do
        }
        
        Path source = Paths.get(sourcePathname);
        Path dest   = Paths.get(destPathname);
        try {
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException io) {
          throw new ErrorException("Error while moving/renaming file '"
                                 + sourcePathname + "' to destination '"
                                 + destPathname + "'");
        }
    }
    
    /**
     * It moves the given file to the specified destination.
     * 
     * @param sourceFile The file to move/rename
     * @param destFile The destination file after the operation
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source or destination path name is
     *                           {@code null}<br>
     *                        2) The source path name does not exist in the file
     *                           system<br>
     *                        3) The source path name exists but it maps to a
     *                           directory<br>
     *                        4) The source file cannot be opened for reading
     *                           (i.e., has not read access permissions)<br>
     *                        5) The destination path name exists but it maps to
     *                           a directory<br>
     *                        6) The destination file cannot be opened for
     *                           writing (i.e., has not write access
     *                           permissions)<br>
     *                        7) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the source of
     *                           the destination file<br>
     *                        8) In case of runtime error during the move/rename
     *                           <br>
     *                        9) In case the move/rename operation is not
     *                           successful (e.g. the destination path already
     *                           exists and the file system denies to rename the
     *                           file or the source or destination file is in
     *                           use etc.)
     *
     * @deprecated since 1.7 See {@link Files#move(Path, Path, CopyOption...)}
     */
    public static void moveFile(File sourceFile, File destFile)
           throws ErrorException {
        validateFileToRead(sourceFile);
        validateFileToWrite(destFile);
        
        if (sourceFile.getPath().equalsIgnoreCase(destFile.getPath())) {
            return; // Source and destination paths are same => nothing to do
        }
        
        boolean success;
        try {
            success = sourceFile.renameTo(destFile);
            if (!success) {
                throw new ErrorException("Error while moving/renaming file '"
                                       + sourceFile.getPath()
                                       + "' to destination '"
                                       + destFile.getPath() + "'");
            }
        }
        catch (RuntimeException re) {
            throw new ErrorException("Error while moving/renaming file '"
                                   + sourceFile.getPath()
                                   + "' to destination '"
                                   + destFile.getPath()
                                   + "'. Details:" + NEW_LINE
                                   + re.getMessage());
        }
    }
    
    /**
     * Given a source and a destination path name of a regular file it moves or
     * renames the file from the source path to the destination path.
     * It attempts to move the file to the target file, failing if the target
     * file exists except if the source and target are the same file, in which
     * case this method has no effect.
     * 
     * @param sourcePathname The absolute path of the file to move/rename
     * @param destPathname The absolute path of the destination file after the
     *                     operation
     * @param options Standard copy options or options as to how symbolic links
     *                are handled
     *                @see java.nio.file.StandardCopyOption
     *                @see java.nio.file.LinkOption
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source or destination path name is
     *                           {@code null}<br>
     *                        2) The source path name does not exist in the file
     *                           system<br>
     *                        3) The source path name exists but it maps to a
     *                           directory<br>
     *                        4) The source file cannot be opened for reading 
     *                           (i.e., has not read access permissions)<br>
     *                        5) The destination path name exists but it maps to
     *                           a directory<br>
     *                        6) The destination file cannot be opened for
     *                           writing (i.e., has not write access
     *                           permissions)<br>
     *                        7) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the source of
     *                           the destination file<br>
     *                        8) In case of I/O error during the operation
     *
     */
    public static void moveFile(String       sourcePathname,
                                String       destPathname,
                                CopyOption...options)
            throws ErrorException {
         validateFileToRead(sourcePathname);
         validateFileToWrite(destPathname);
         
         Path sourceFilePath;
         Path destFilePath;
         try {
             sourceFilePath = Paths.get(sourcePathname);
             destFilePath   = Paths.get(destPathname);
         }
         catch (InvalidPathException impossible) {
             // Just in case...
             System.out.println(impossible.getMessage());
             return;
         }
         
         try {
             Files.move(sourceFilePath,
                        destFilePath,
                        options);
         }
         catch (IOException ioe) {
             throw new ErrorException("Error while moving/renaming file '"
                                    + sourcePathname + "' to destination '"
                                    + destPathname + "'. Details:" + NEW_LINE
                                    + ioe.getMessage());
         }
    }
    
    /**
     * Sets the owner's permissions to access the file or directory denoted by
     * the given pathname.
     * 
     * @param pathname The absolute path of the file or directory to set its
     *                 permissions
     * @param canRead {@code true} to set the access permission to allow read
     *                operations; {@code false} to disallow read operations
     *                and {@code null} to leave the read access unaffected
     * @param canWrite {@code true} to set the access permission to allow write
     *                 operations; {@code false} to disallow write operations
     *                 and {@code null} to leave the write access unaffected
     * @param canExecute {@code true} to set the access permission to allow
     *                   execute operations; {@code false} to disallow
     *                   execute operations and {@code null} to leave the
     *                   execute access unaffected
     * 
     * @throws ErrorException in case of error setting the permissions
     */
    public static void setOwnerPermission(String  pathname,
                                          Boolean canRead,
                                          Boolean canWrite,
                                          Boolean canExecute)
           throws ErrorException {
        File file = new File(pathname);
        
        boolean success = true;
        try {
            // Read access
            if (canRead != null) {
                success &= file.setReadable(canRead, true);
            }
            
            // Write access
            if (canWrite != null) {
                success &= file.setWritable(canWrite, true);
            }
            
            // Execute access
            if (canExecute != null) {
                success &= file.setExecutable(canExecute, true);
            }
        }
        catch (SecurityException se) {
            success = false;
        }
        
        if (!success) {
            throw new ErrorException("Error setting the owner's access "
                                   + "permissions for '" + pathname + "'");
        }
    }
    
    /**
     * Sets everybody's permissions to access the file or directory denoted by
     * the given pathname.
     * 
     * @param pathname The absolute path of the file or directory to set its
     *                 permissions
     * @param canRead {@code true} to set the access permission to allow read
     *                operations; {@code false} to disallow read operations
     *                and {@code null} to leave the read access unaffected
     * @param canWrite {@code true} to set the access permission to allow write
     *                 operations; {@code false} to disallow write operations
     *                 and {@code null} to leave the write access unaffected
     * @param canExecute {@code true} to set the access permission to allow
     *                   execute operations; {@code false} to disallow
     *                   execute operations and {@code null} to leave the
     *                   execute access unaffected
     * 
     * @throws ErrorException in case of error setting the permissions
     */
    public static void setEverybodyPermission(String  pathname,
                                              Boolean canRead,
                                              Boolean canWrite,
                                              Boolean canExecute)
           throws ErrorException {
        File file = new File(pathname);
        
        boolean success = true;
        try {
            // Read access
            if (canRead != null) {
                success &= file.setReadable(canRead, false);
            }
            
            // Write access
            if (canWrite != null) {
                success &= file.setWritable(canWrite, false);
            }
            
            // Execute access
            if (canExecute != null) {
                success &= file.setExecutable(canExecute, false);
            }
        }
        catch (SecurityException se) {
            success = false;
        }
        
        if (!success) {
            throw new ErrorException("Error setting everybody's access "
                                   + "permissions for '" + pathname + "'");
        }
    }
    
    /**
     * Sets the permissions for the given file, directory or symbolic link on
     * Unix-based file system.
     * 
     * @param pathname The absolute path of the file, directory or symbolic
     *                 link to set its permissions
     * @param octal The octal value that represents the file permissions. Its
     *              value consists of 3 digits where the left digit maps to the
     *              owner permissions, the middle digit maps to the group
     *              permissions and the right digit maps to the permissions of
     *              others. Each digit is caluclated as follows:
     *              - If the file can be accessed for reading it adds 4 to its
     *                total for the given digit 
     *              - If the file can be accessed for writing it adds 2 to its
     *                total for the given digit 
     *              - If the file can be accessed for execution it adds 2 to
     *                its total for the given digit 
     * 
     * @throws ErrorException in case of invalid octal value
     */
    public static void setPermission(String pathname,
                                     String octal)
           throws ErrorException {
        // Validate the octal
        if (octal == null) {
            throw new ErrorException("The octal permissions is null");
        }
        octal = octal.trim();
        if (octal.length() != 3) {
            throw new ErrorException("The octal permissions has " + octal.length()
                                  + " digits instead of 3");
        }
        try {
            Integer.valueOf(octal, 8);
        }
        catch (NumberFormatException notAnOctal) {
            throw new ErrorException("The permissions (" + octal
                                  + ") is not an octal value");
        }
        
        Set<PosixFilePermission> permissions = new HashSet<PosixFilePermission>();
        
        // Set permission for owner
        int value = (int)octal.charAt(0) - Integer.valueOf('0');
        if (value >= 4) {
            permissions.add(PosixFilePermission.OWNER_READ);
            value -= 4;
        }
        if (value >= 2) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
            value -= 2;
        }
        if (value == 1) {
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            value -= 1;
        }
        
        // Set permission for group
        value = (int)octal.charAt(1) - Integer.valueOf('0');
        if (value >= 4) {
            permissions.add(PosixFilePermission.GROUP_READ);
            value -= 4;
        }
        if (value >= 2) {
            permissions.add(PosixFilePermission.GROUP_WRITE);
            value -= 2;
        }
        if (value == 1) {
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            value -= 1;
        }
        
        // Set permission for others
        value = (int)octal.charAt(2) - Integer.valueOf('0');
        if (value >= 4) {
            permissions.add(PosixFilePermission.OTHERS_READ);
            value -= 4;
        }
        if (value >= 2) {
            permissions.add(PosixFilePermission.OTHERS_WRITE);
            value -= 2;
        }
        if (value == 1) {
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            value -= 1;
        }
        
        try {
            Files.setPosixFilePermissions(Paths.get(pathname), permissions);
        }
        catch (IOException ioe) {
            throw new ErrorException("Error setting permissions '" + octal
                                   + "' for '" + pathname + "'");
        }
    }
    
    /**
     * Validates a regular file to write.<br>
     * It makes sure that if the file exists, it is a regular file (or a
     * symbolic link that points to a regular file) and not a directory and it
     * can be opened for writing (to overwrite its contents).
     * 
     * @param file The the regular file to validate
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided path is {@code null}<br>
     *                        2) It exists but it maps to a directory<br>
     *                        3) It cannot be opened for writing (i.e., does not
     *                           have write access permissions)<br>
     *                        4) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the file
     */
    public static void validateFileToWrite(File file)
           throws ErrorException {
        if (file == null) {
            throw new ErrorException("The path for the file to write is null");
        }
        
        try {
            if (!file.exists()) {
                return; // We are done with the checks
            }
            
            if (!file.isFile()) {
                throw new ErrorException("'" + file.getPath()
                                       + "' is not a regular file");
            }
            
            if (!file.canWrite()) {
                throw new ErrorException("File '" + file.getPath()
                                       + "' has not write access permissions");
            }
        }
        catch (SecurityException se) {
            throw new ErrorException("Access to '" + file.getPath()
                                   + "' denied");
        }
    }
    
    /**
     * Validates a regular file to write.<br>
     * It makes sure that if the file exists, it is a regular file (or a
     * symbolic link that points to a regular file) and not a directory and it
     * can be opened for writing (to overwrite its contents).
     * 
     * @param filePathname The absolute path name of the regular file to
     *                     validate
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided path is {@code null}<br>
     *                        2) It exists but it maps to a directory<br>
     *                        3) It cannot be opened for writing (i.e., does not
     *                           have write access permissions)<br>
     *                        4) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the file
     */
    public static void validateFileToWrite(String filePathname)
           throws ErrorException {
        if (filePathname == null) {
            throw new ErrorException("The path for the file to write is null");
        }
        
        validateFileToWrite(new File(filePathname.trim()));
    }
    
    /**
     * Validates a regular file to read.<br>
     * It makes sure that the file exists, it is a regular file (or a symbolic
     * link that points to a regular file) and not a directory and it can be
     * opened for reading.
     * 
     * @param file The regular file to validate
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a directory<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the file
     */
    public static void validateFileToRead(File file)
           throws ErrorException {
        if (file == null) {
            throw new ErrorException("The file to read is null");
        }
        
        
        try {
            if (!file.exists()) {
                throw new ErrorException("'" + file.getPath()
                                       + "' does not exist");
            }
            
            if (!file.isFile()) {
                throw new ErrorException("'" + file.getPath()
                                       + "' is not a regular file");
            }
            
            if (!file.canRead()) {
                throw new ErrorException("File '" + file.getPath()
                                       + "' has not read access permissions");
            }
        }
        catch (SecurityException se) {
            throw new ErrorException("Access to '" + file.getPath()
                                   + "' denied");
        }
    }
    
    /**
     * Validates a regular file to read.<br>
     * It makes sure that the file exists, it is a regular file (or a symbolic
     * link that points to a regular file) and not a directory and it can be
     * opened for reading.
     * 
     * @param filePathname The absolute path name of the regular file to
     *                     validate
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided path is {@code null}<br>
     *                        2) It does not exist in the file system<br>
     *                        3) It exists but it maps to a directory<br>
     *                        4) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        5) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the file
     */
    public static void validateFileToRead(String filePathname)
           throws ErrorException {
        if (filePathname == null) {
            throw new ErrorException("The path for the file to read is null");
        }
        
        File file = new File(filePathname.trim());
        FileUtil.validateFileToRead(file);
    }
    
    /**
     * Validates a regular file to read.<br>
     * It makes sure that the file exists, it is a regular file and not a
     * directory and it can be opened for reading.
     * 
     * @param filename The name of the regular file to validate
     * @param extension The expected extension that the file should have or
     *                  {@code null} to skip this validation (i.e., ignore the
     *                  file extension)
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file name is {@code null}<br>
     *                        2) The file name does not end with the expected
     *                           extension<br>
     *                        3) It does not exist in the file system<br>
     *                        4) It exists but it maps to a directory<br>
     *                        5) It cannot be opened for reading (i.e., does not
     *                           have read access permissions)<br>
     *                        6) In case a security manager exists and its
     *                           SecurityManager.checkRead(java.lang.String)
     *                           method denies read access to the file
     */
    public static void validateFileToRead(String filename,
                                          String extension)
            throws ErrorException {
        if (filename == null) {
            throw new ErrorException("The path for the file to read is null");
        }
        
        filename = filename.trim(); // Trim the path (just in case...)
        
        if ((extension != null) && !filename.endsWith(extension)) {
            throw new ErrorException("'" + filename + "' has not the expected "
                                   + "extension (i.e., '" + extension + "')");
        }
        
        File file = new File(filename);
        FileUtil.validateFileToRead(file);
    }
    
    /**
     * Checks whether a file ends with an extension that exists in a provided
     * list of file extensions.
     * 
     * @param file The file to check if it ends with an extension that exists
     *             in the list of extensions
     * @param fileExtensions The list of extensions to check if the file ends
     *                       with one of them
     *                     
     * @return true if the file ends with an extension that exists in the list
     *         of extensions or false otherwise. The comparison is 
     *         case-insensitive (e.g. if the file ends with ".TXT" and in the
     *         list of extensions that exists extension ".txt" this method will
     *         return true)<br>
     *         Note: if the file is {@code null} or the list of extensions is
     *               {@code null} this method returns {@code false}
     */
    public static boolean extensionMatch(File file, String[] fileExtensions) {
        if ((file == null) || (fileExtensions == null)) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        for (int i = 0; i < fileExtensions.length; i++) {
            if (fileName.endsWith(fileExtensions[i].toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Given a file it returns its attributes.
     * 
     * @param file The file to retrieve its attributes
     * 
     * @return the file attributes
     * 
     * @throws ErrorException in case the file is {@code null} or does not exist
     *                        or in case of an error retrieving the file
     *                        attributes
     */
    public static BasicFileAttributes getAttributes(File file)
           throws ErrorException {
        validateFileToRead(file);
        
        Path filePath = Paths.get(file.getPath());
        try {
            return Files.readAttributes(filePath, BasicFileAttributes.class);
        }
        catch (IOException ioe) {
            throw new ErrorException("Error retrieving the attributes for file'"
                                   + file.getName() + "'. Details:" + NEW_LINE 
                                   + ioe.getMessage());
        }
    }
    
    /**
     * Given a binary name it looks for it in a directory that is part of the
     * provided environment variable and if found it returns its full path name.
     * Otherwise, it returns {@code null}.
     * 
     * @param envVarName The environment variable name
     * @param subpath The subpath to append to the environment variable
     *                locations or {@code null} for nothing to append.<br>
     *                E.g. Looking for java binary can occur via
     *                   1) envVarName = "PATH" , subpath = {@code null} or <br>
     *                   2) envVarName = "JAVA_HOME" , subpath = "bin" 
     * @param binaryName The name of the binary to look for
     * 
     * @return the binary full path name if found or {@code null} otherwise
     */
    public static String findBinaryOnEnvVar(String envVarName,
                                            String subpath,
                                            String binaryName) {
        if (envVarName == null) { // Just in case...
            return null;
        }
        String pathEnvVar = System.getenv(envVarName);
        if (pathEnvVar == null || pathEnvVar.trim().isEmpty()) {
            return null; // The env variable is not set or it is empty
        }
        
        String[] binaryExtensions;
        if (OSPlatform.isWindows()) {
            binaryExtensions = new String[] { "", ".exe", ".bat" };
        }
        else {
            binaryExtensions = new String[] { "" };
        }
        
        String[] dirsToLookUnder = pathEnvVar.split(File.pathSeparator);
        for (String dirPathname : dirsToLookUnder) {
            if (subpath != null) {
               dirPathname = dirPathname + File.separator + subpath;
            }
            
            for (String extension : binaryExtensions) {
                String filename = binaryName + extension;
                
                File file = new File(dirPathname, filename);
                if (file.isFile() && file.canExecute()) {
                    return file.getAbsolutePath();// Found the binary; return it
                }
            }
        }
        
        // Did not find the binary; return null
        return null;
    }
    
    /**
     * Given a fully-qualified class name it returns the absolute path from
     * where the class is loaded.
     * 
     * @param classFQN The fully-qualified class name (e.g. 'java.io.File')
     * 
     * @return the absolute path from where the class is loaded or {@code null}
     *         in case no such path can be found
     */
    public static String getClassLocation(String classFQN) {
        // Step 1: Retrieve the Class object associated with the class
        //         or interface with the qualifiedClassName
        @SuppressWarnings("rawtypes")
        Class qualifiedClassObject = null;
        
        try {
            qualifiedClassObject = Class.forName(classFQN);
        }
        catch (ClassNotFoundException cnfe) { // Class not found
            return null;
        }
        
        // Step 2: Get the protection domain 
        ProtectionDomain protectionDomain = null;
        
        try {
            protectionDomain = qualifiedClassObject.getProtectionDomain();
        }
        catch (SecurityException se) {
            return null;
        }
        
        // Step 3: Get the CodeSource of this protection domain
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            return null;
        }
        
        // Step 4: Return the path associated with this CodeSource
        return codeSource.getLocation().getPath();
    }
}