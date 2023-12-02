/*
 * File          : Constants.java
 * Author        : Charis Charitsis
 * Creation Date : 16 June 2013
 * Last Modified : 21 September 2021
 */
package edu.stanford.constants;

// Import Java SE classes
import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * Contains common constants that are shared across all packages
 */
public class Constants
{
    // -------------------------------------------------- //
    //   P  U  B  L  I  C     C  O  N  S  T  A  N  T  S   //
    // -------------------------------------------------- //
    /** Current working directory */
    public static final String   CURR_DIR           = System.getProperty(
                                                                "user.dir");
    /** Temporary directory */
    public static final String   TEMP_DIR           = System.getProperty(
                                                              "java.io.tmpdir");
    /** The absolute path name of the directory with the resources */
    public static final String   RESOURCE_DIR       = CURR_DIR + File.separator
                                                               + "resources";
    /**
     * {@code true} to use load the resources images from the classpath or
     * {@code false} otherwise
     */
    public static final boolean  LOAD_IMAGES_FROM_CLASSPATH = true;
    
    /** Extension for temporary files */
    public static final String   TEMP_FILE_EXT      = ".tmp";
    /** {@code true} to print verbose information or {@code false} otherwise */
    public static final boolean  VERBOSE            = false ||
                                                   System.getProperty("VERBOSE")
                                                                        != null;
    /** The extension of the Java library files */
    public static final String   JAR_FILE_EXTENSION  = ".jar";
    /** The extension of the student source code files to process */
    public static final String   JAVA_FILE_EXTENSION = ".java";
    /** The extension of the compiled student source code files */
    public static final String   CLASS_FILE_EXTENSION = ".class";
    /**
     * Array with the Java language reserved keywords
     */
    public static final String[] JAVA_KEYWORDS_ARR    = new String[] {
                                     "abstract",   "assert",       "boolean",
                                     "break",      "byte",         "case",
                                     "catch",      "char",         "class",
                                     "const",      "continue",     "default",
                                     "double",     "do",           "else",
                                     "enum",       "extends",      "false",
                                     "final",      "finally",      "float",
                                     "for",        "goto",         "if",
                                     "implements", "import",       "instanceof",
                                     "int",        "interface",    "long",
                                     "native",     "new",          "null",
                                     "package",    "private",      "protected",
                                     "public",     "return",       "short",
                                     "static",     "strictfp",     "super",
                                     "switch",     "synchronized", "this",
                                     "throw",      "throws",       "transient",
                                     "true",       "try",          "void",
                                     "volatile",   "while"
                                 };
    /**
     * Set with Java language reserved keywords (sorted alphabetically)
     */
    public static final Set<String> JAVA_KEYWORDS     = new TreeSet<String>();
    static {
        for (String keyword : JAVA_KEYWORDS_ARR) {
            JAVA_KEYWORDS.add(keyword);
        }
    }
}
