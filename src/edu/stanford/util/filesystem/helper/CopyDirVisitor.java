/*
 * File          : CopyDirVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 11 September 2013
 * Last Modified : 19 August 2018
 */
package edu.stanford.util.filesystem.helper;

// Import Java SE classes
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/*
 * M i n i  -  T u t o r i a l:
 * 
 * Path objects represent a sequence of directories that may or may not include
 * a file. There are three ways to construct a Path object:
 * 
 * 1) FileSystems.getDefault().getPath(String first, String� more)
 * 2) Paths.get(String path, String� more), convenience method that calls
 *                                          FileSystems.getDefault().getPath
 * 3) Calling the toPath method on a java.io.File object
 * 
 * Here are some examples of creating Path objects:
 *   Example 1:  Paths.get("/foo");       // Path string would be "/foo"
 *   Example 2:  Paths.get("/foo","bar"); // Path string "/foo/bar"
 *
 * To manipulate Path objects there are the Path.resolve and Path.relativize
 * methods. Here is an example of using Path.resolve:
 *      Path base = Paths.get("/foo"); // This is our base path "/foo"
 *      // filePath is "/foo/bar/file.txt" while base still "/foo"
 *      Path filePath = base.resolve("bar/file.txt");
 * Using the Path.resolve method will append the given String or Path object to
 * the end of the calling Path, unless the given String or Path represents an
 * absolute path, the the given path is returned, for example:
 *      Path path = Paths.get("/foo"); // Path "/foo"
 *      // Resolved Path string is "/usr/local"
 *      Path resolved = path.resolve("/usr/local");
 *
 * The Path.relativize works in the opposite fashion, returning a new relative
 * path that if resolved against the calling Path would result in the same Path
 * string. Here's an example:
 *      Path base     = Paths.get("/usr");    // base Path string "/usr"
 *      Path foo      = base.resolve("foo");  // foo Path string "/usr/foo"
 *      Path bar      = foo.resolve("bar");   // bar Path string "/usr/foo/bar"
 *      Path relative = base.relativize(bar); // relative Path string "foo/bar"
 *
 * Another method on the Path class that is helpful is the Path.getFileName,
 * that returns the name of the farthest element represented by this Path
 * object, with the name being an actual file or just a directory.
 * For example:
 * // Assume filePath constructed elsewhere as "/home/user/info.txt"
 * filePath.getFileName(); // Returns Path with path string "info.txt"
 * // Now assume dirPath constructed elsewhere as "/home/user/Downloads"
 * dirPath.getFileName();  // Returns Path with path string "Downloads"
 * 
 * ------------------------
 *  C O P Y   A   F I L E 
 * ------------------------
 * To copy one file to another you would use the Files.copy method 
 * copy(Path source, Path target, CopyOption... options) very concise and no
 * anonymous inner classes. The options argument are enums that specify how the
 * file should be copied. (There are actually 2 different Enum classes,
 * LinkOption and StandardCopyOption, but both implement the CopyOption
 * interface)
 * Here is the list of available options for Files.copy:
 *  LinkOption.NOFOLLOW_LINKS           // Do not follow symbolic links
 *  StandardCopyOption.COPY_ATTRIBUTES  // Copy attributes to the new file
 *  StandardCopyOption.REPLACE_EXISTING // Replace an existing file if it exists
 *  
 *  Note: - If StandardCopyOption.ATOMIC_MOVE is specified, an
 *          UsupportedOperationException is thrown.
 *        - If no options are specified,  the default is to throw an error if
 *          the target file exists or is a symbolic link.
 *        - If the path object is a directory then an empty directory is created
 *          in the target location.
 * 
 * Here is an example of copying a file to another with Path objects using the
 * Path.resolve and Path.relativize methods:
 *    Path sourcePath ...
 *    Path basePath ...
 *    Path targetPath ...
 *
 *    Files.copy(sourcePath,
 *               targetPath.resolve(basePath.relativize(sourcePath));
 * 
 * ------------------------
 *  M O V E   A   F I L E 
 * ------------------------
 * Moving a file is equally as straight forward as:
 * move(Path source, Path target, CopyOption... options);
 * The available StandardCopyOptions enums available are:
 *  StandardCopyOption.REPLACE_EXISTING // Replace an existing file if it exists
 *  StandardCopyOption.ATOMIC_MOVE      // Move the file as atomic file system
 *                                      // operation
 *
 * Note: - If Files.move is called with StandardCopyOption.COPY_ATTRIBUTES an
 *         UnsupportedOperationException is thrown.
 *       - Files.move can be called on an empty directory or if it does not
 *         require moving a directories contents, re-naming for example, the
 *         call will succeed, otherwise it will throw an IOException
 *       - The default is to throw an Exception if the target file already
 *         exists. 
 *       - If the source is a symbolic link, then the link itself is moved, not
 *         the target of the link.
 *
 * Here is an example of Files.move, again tying in the Path.relativize and
 *  Path.resolve methods:
 *    Path sourcePath ...
 *    Path basePath ...
 *    Path targetPath ...
 *
 *    Files.move(sourcePath,
 *               targetPath.resolve(basePath.relativize(sourcePath));
 *
 * -------------------------------------------------------------
 *  C O P Y I N G   A N D   M O V I N G   D I R E C T O R I E S 
 * -------------------------------------------------------------
 * One of the most useful methods in the Files class is Files.walkFileTree.
 * The walkFileTree method performs a depth first traversal of a file tree.
 * There are two signatures:
 *   1) walkFileTree(Path start, Set options, int maxDepth, FileVisitor visitor)
 *   2) walkFileTree(Path start, FileVisitor visitor)
 *
 * The second method for Files.walkFileTree calls the first method with
 * EnumSet.noneOf(FileVisitOption.class) and Integer.MAX_VALUE. 
 * Today, there is only one file visit option (i.e., FOLLOW_LINKS) to follow the
 * symbolic links. By default (i.e., no visit option) the symbolic links are not
 * followed. 
 * The FileVisitor is an interface that has four methods defined:
 *
 *  - preVisitDirectory(T dir, BasicFileAttributes attrs)
 *       Called for a directory before all entries are traversed
 *  - visitFile(T file, BasicFileAttributes attrs)
 *       Called for a file in the directory
 *  - postVisitDirectory(T dir, IOException exc)
 *       Called only after all files and sub-directories have been traversed
 *  - visitFileFailed(T file, IOException exc)
 *       Called for files that could not be visited
 *
 * All of the methods return one of the four possible FileVisitResult enums :
 *
 *   FileVistitResult.CONTINUE
 *       Continue. When returned from a preVisitDirectory method then the
 *       entries in the directory should also be visited.
 *   FileVistitResult.SKIP_SIBLINGS
 *       Continue without traversing siblings of the directory or file
 *       If returned from the preVisitDirectory method then the entries in the
 *       directory are also skipped and the postVisitDirectory method is not
 *       invoked.
 *   FileVistitResult.SKIP_SUBTREE
 *       Continue without traversing contents of the directory
 *       This result is only meaningful when returned from the preVisitDirectory
 *       method; otherwise this result type is the same as returning CONTINUE
 *   FileVistitResult.TERMINATE
 *       Terminate
 *
 * To make life easier there is a default implementation of the FileVisitor,
 * SimpleFileVisitor (validates arguments are not null and returns
 * FileVisitResult.CONTINUE), that can be subclassed so that we can override
 * just the methods we need to work with. 
 */
/**
 * A visitor of directories to provide to the Files.walkFileTree methods to
 * visit each directory in a directory tree to copy a directory tree.
 */

public class CopyDirVisitor extends SimpleFileVisitor<Path>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The source directory path (i.e., the path to copy from) */
    private final Path                fromPath;
    /** The destination directory path (i.e., the path to copy to) */
    private final Path                toPath;
    /** Defines the standard copy options */
    private final StandardCopyOption  copyOption;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Initializes a new instance of this class
     * 
     * @param fromPath The path of the source directory (i.e., the directory to
     *                 copy from)
     * @param toPath The path of the destination directory (i.e., the directory
     *               to copy to)
     * @param copyOption One of the following copy options:<br>
     *                    1) ATOMIC_MOVE to move the file as an atomic file
     *                       system operation<br>
     *                    2) COPY_ATTRIBUTES to copy the attributes to the
     *                       new file<br>
     *                    3) REPLACE_EXISTING to replace an existing file if
     *                       it exists
     */
    public CopyDirVisitor(Path               fromPath,
                          Path               toPath,
                          StandardCopyOption copyOption) {
        this.fromPath   = fromPath;
        this.toPath     = toPath;
        this.copyOption = copyOption;
    }
    
    /**
     * Initializes a new instance of this class
     * 
     * @param fromPath The path of the source directory (i.e., the directory to
     *                 copy from)
     * @param toPath The path of the destination directory (i.e., the directory
     *               to copy to)
     */
    public CopyDirVisitor(Path               fromPath,
                          Path               toPath) {
        this(fromPath,
             toPath,
             StandardCopyOption.REPLACE_EXISTING);
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Invoked for a directory before entries in the directory are visited.
     *
     * If this method returns CONTINUE, then entries in the directory are
     * visited. If this method returns SKIP_SUBTREE or SKIP_SIBLINGS then
     * entries in the directory (and any descendants) will not be visited.
     * 
     * @param dir A reference to the directory
     * @param attrs The directory's basic attributes
     * 
     * @return the visit result
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    public FileVisitResult preVisitDirectory(Path                dir,
                                             BasicFileAttributes attrs)
           throws IOException {
        // The 'fromPath.relativize(dir)' keeps the relative path of 'dir' in
        // respect to 'fromPath'
        // E.g. if dir is '/a/b/c/' and  fromPath is '/a' then
        //      fromPath.relativize(dir) will be 'b/c'
        // The 'toPath.resolve(aPath)' appends the 'aPath' to the toPath,
        // essentially reconstructing the same path as 'dir' but with 'toPath'
        // instead of 'fromPath' as base directory
        Path targetPath = toPath.resolve(fromPath.relativize(dir));
        
        // Create each directory in the target, 'toPath', as each directory
        // from the source, 'fromPath', is traversed
        if(!Files.exists(targetPath)){
            Files.createDirectories(targetPath);
        }
        
        return FileVisitResult.CONTINUE;
    }
    
    /**
     * Invoked when visiting a file in a directory traversal.
     *
     * @param file A reference to the file
     * @param attrs The file's basic attributes
     * 
     * @return the visit result
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
           throws IOException {
        // The 'fromPath.relativize(file)' keeps the relative path of 'file' in
        // respect to 'fromPath'
        // E.g. if file is '/a/b/c/foo.txt' and  fromPath is '/a' then
        //      fromPath.relativize(file) will be 'b/c/foo.txt'
        // The 'toPath.resolve(aPath)' appends the 'aPath' to the toPath,
        // essentially reconstructing the same path as 'file' but with 'toPath'
        // instead of 'fromPath' as base directory
        Path targetPath = toPath.resolve(fromPath.relativize(file));
        
        // Copy the file from the source directory to the target directory
        Files.copy(file,
                   targetPath,
                   copyOption);
        
        return FileVisitResult.CONTINUE;
    }
}
