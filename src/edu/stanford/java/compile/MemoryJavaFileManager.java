/*
 * File          : MemoryJavaCompiler.java
 * Author        : Charis Charitsis
 * Creation Date : 23 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.java.compile;

// Import Java SE classes
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;

/**
 * JavaFileManager that keeps compiled .class bytes in memory.
 */
public class MemoryJavaFileManager
       extends ForwardingJavaFileManager<JavaFileManager>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * Map where the key is the classname and the value is the class bytes.
     */
    private Map<String, byte[]> classbyteLookup;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a new MemoryJavaFileManager.
     * 
     * @param fileManager File manager for tools operating on Java programming
     *                    language source and class files.
     */
    public MemoryJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
        classbyteLookup = new HashMap<>();
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * @return a map where the key is the classname and the value is the class
     *         bytes
     */
    public Map<String, byte[]> getClassbyteLookup() {
        return classbyteLookup;
    }
    
    /**
     * Releases any resources opened by this file manager directly or
     * indirectly.<br>
     * Closing a file manager which has already been closed has no effect.
     */
    @Override
    public void close() throws IOException {
        classbyteLookup = null;
    }
    
    /**
     * Flushes any resources opened for output by this file manager directly or
     * indirectly. Flushing a closed file manager has no effect.
     */
    @Override
    public void flush()
    	   throws IOException {
    }
    
    /**
     * Returns a file object for output representing the specified class of the
     * specified kind in the given package-oriented location.<br>
     * Optionally, this file manager might consider the sibling as a hint for
     * where to place the output. The exact semantics of this hint is
     * unspecified.<br>
     * The JDK compiler, javac, for example, will place class files in the same
     * directories as originating source files unless a class file output
     * directory is provided. To facilitate this behavior, javac might provide
     * the originating source file as sibling when calling this method.
     * 
     * @param location A package-oriented location
     * @param className The name of a class
     * @param kind The kind of file, must be one of SOURCE or CLASS
     * @param sibling A file object to be used as hint for placement; might be
     *               {@code null}
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location   location,
                                               String     className,
                                               Kind       kind,
                                               FileObject sibling)
           throws IOException {
        if (kind == Kind.CLASS) {
            return new ClassOutputBuffer(className);
        }
        else {
            return super.getJavaFileForOutput(location,
                                              className,
                                              kind,
                                              sibling);
        }
    }
    
    /**
     * Give a Java source code and a file name that is associated with this (the
     * source code does not necessarily have to be stored in a file) it returns
     * a JavaFileObject for it.
     * 
     * @param filename The name of the file for the source code which is the
     *                 fully qualified class name with a .java at the end (e.g.,
     *                 'edu.stanford.java.MemoryJavaCompiler.java')
     * @param sourceCode The Java source code
     * 
     * @return JavaFileObject for the given source code with the provided
     *                        filename
     */
    public static JavaFileObject getJavaFileObject(String filename,
                                                   String sourceCode) {
        return new StringInputBuffer(filename, sourceCode);
    }
    
    
    // -------------------------------------------------------- //
    //   N   E   S   T   E   D      C   L   A   S   S   E   S   //
    // -------------------------------------------------------- //
    /**
     * A file object used to represent Java source coming from a string.
     */
    private static class StringInputBuffer extends SimpleJavaFileObject
    {
        // ---------------------------------------------------- //
        //   P  R  I  V  A  T  E    V  A  R  I  A  B  L  E  S   //
        // ---------------------------------------------------- //
        /** The Java source code */
        private final String sourceCode;
        
        // ------------------------------------ //
        //  C  O  N  S  T  R  U  C  T  O  R  S  //
        // ------------------------------------ //
        /**
         * Creates a new StringInputBuffer.
         * 
         * @param filename The name of the file for the source code which is the
         *                 fully qualified class name with a .java at the end
         *                 (e.g., 'edu.stanford.java.MemoryJavaCompiler.java')
         * @param sourceCode The Java source code
         */
        private StringInputBuffer(String filename, String sourceCode) {
            super(toURI(filename), Kind.SOURCE);
            this.sourceCode = sourceCode;
        }
        
        // ------------------------------------------- //
        //   P  U  B  L  I  C    M  E  T  H  O  D  S   //
        // ------------------------------------------- //
        /**
         * Returns the character content of this file object, if available.
         * Any byte that cannot be decoded will be replaced by the default
         * translation character.<br>
         * In addition, a diagnostic may be reported unless ignoreEncodingErrors
         * is {@code true}.
         * 
         * @param ignoreEncodingErrors {@code true} to ignore encoding errors or
         *                             {@code false} otherwise
         */
        @Override
        public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
            return CharBuffer.wrap(sourceCode);
        }
    }
    
    /**
     * A file object that stores Java bytecode into the classBytes map.
     */
    private class ClassOutputBuffer extends SimpleJavaFileObject
    {
        // ---------------------------------------------------- //
        //   P  R  I  V  A  T  E    V  A  R  I  A  B  L  E  S   //
        // ---------------------------------------------------- //
        /** The fully qualified class name */
        private final String fullyQualifiedClassName;
        
        // ------------------------------------ //
        //  C  O  N  S  T  R  U  C  T  O  R  S  //
        // ------------------------------------ //
        /**
         * Creates a new ClassOutputBuffer.
         * 
         * @param fullyQualifiedClassName The fully qualified class name
         */
        private ClassOutputBuffer(String fullyQualifiedClassName) {
            super(toURI(fullyQualifiedClassName), Kind.CLASS);
            this.fullyQualifiedClassName = fullyQualifiedClassName;
        }
        
        /**
         * @return an OutputStream for this file object
         */
        @Override
        public OutputStream openOutputStream() {
            return new FilterOutputStream(new ByteArrayOutputStream()) {
                @Override
                public void close()
                       throws IOException {
                    out.close();
                    ByteArrayOutputStream bos = (ByteArrayOutputStream)out;
                    classbyteLookup.put(fullyQualifiedClassName,
                                        bos.toByteArray());
                }
            };
        }
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Given a name for a source code or a .class file it returns its URI.
     * 
     * @param name A name for a source code or a .class file
     * 
     * @return the URI for the source code or a .class file
     */
    private static URI toURI(String name) {
        File file = new File(name);
        if (file.exists()) {
            return file.toURI();
        }
        else {
            try {
                final StringBuilder newUri = new StringBuilder();
                newUri.append("mfm:///");
                newUri.append(name.replace('.', '/'));
                if(name.endsWith(JAVA_FILE_EXTENSION)) {
                    int start = newUri.length() - JAVA_FILE_EXTENSION.length();
                    int end   = newUri.length();
                    newUri.replace(start, end, JAVA_FILE_EXTENSION);
                }
                return URI.create(newUri.toString());
            }
            catch (Exception ee) {
                return URI.create("mfm:///com/sun/script/java/java_source");
            }
        }
    }
}
