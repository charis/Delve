/*
 * File          : InstrumentationConfig.java
 * Author        : Charis Charitsis
 * Creation Date : 9 December 2020
 * Last Modified : 14 January 2021
 */
package edu.stanford.java.execution;

// Import Java SE classes
import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Placeholder to store information related to how the student program should be
 * instrumented
 */
public class InstrumentationConfig
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The import statements to add on top of any existing ones or {@code null}
     * to add no more import statements
     */
    private final String[]            imports;
    /**
     * A lookup map that is used for text replacement or {@code null} to avoid
     * text replacements.<br>
     * It uses the keys as the text to look for and the corresponding values as
     * replacement to this text.
     */
    private final Map<String, String> replacements;
    /**
     * The selected files for the code instrumentation
     */
    private final Set<File>           instrumentationFiles;
    /**
     * Set with source code files that are required to compile the source code
     * or {@code null} if there are no such dependencies
     */
    private final Set<File>           requiredSourceFiles;
    /**
     * The path to look for user class files, and (optionally) annotation
     * processors and source files or {@code null} to use the current user
     * directory. 
     */
    private final String              classpath;
    /**
     * {@code true} to make a method call to save the state when entering a
     * method or {@code false} otherwise 
     */
    private final boolean             saveStateBefore;
    /**
     * {@code true} to make a method call to save the state when exiting a
     * method or {@code false} otherwise  
     */
    private final boolean             saveStateAfter;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new InstrumentationConfig with the 'configuration' how to
     * implement the code instrumentation.
     * 
     * @param saveStateBefore {@code true} to make a method call to save the
     *                        state when entering a method or {@code false}
     *                        otherwise 
     * @param saveStateAfter {@code true} to make a method call to save the
     *                        state when exiting a method or {@code false}
     *                        otherwise 
     * @param imports The import statements to add on top of any existing ones
     *                or {@code null} to add no more import statements
     * @param replacements A lookup map that is used for text replacement or
     *                     {@code null} to avoid text replacements.<br>
     *                     It uses the keys as the text to look for and the
     *                     corresponding values as replacement to this text.
     * @param instrumentationFiles The selected files for the code
     *                             instrumentation
     * @param requiredSourceFiles Set with source code files that are required
     *                            to compile the code or an empty set if there
     *                            are no such dependencies
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     *                  
     */
    public InstrumentationConfig(boolean             saveStateBefore,
                                 boolean             saveStateAfter,
                                 String[]            imports,
                                 Map<String, String> replacements,
                                 Set<File>           instrumentationFiles,
                                 Set<File>           requiredSourceFiles,
                                 String              classpath) {
        this.saveStateBefore      = saveStateBefore;
        this.saveStateAfter       = saveStateAfter;
        this.imports              = imports;
        this.replacements         = replacements;
        this.instrumentationFiles = instrumentationFiles;
        this.requiredSourceFiles  = requiredSourceFiles;
        this.classpath            = classpath;
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * @return {@code true} to make a method call to save the state when
     *          entering a method or {@code false} otherwise 
     */
    public boolean saveStateBefore() {
        return saveStateBefore;
    }
    
    /**
     * @return {@code true} to make a method call to save the state when exiting
     *         a method or {@code false} otherwise  
     */
    public final boolean saveStateAfter() {
        return saveStateAfter;
    }
    
    /**
     * @return the import statements to add on top of any existing ones or
     *         {@code null} to add no more import statements
     */
    public String[] getImports() {
        return imports;
    }
    
    /**
     * @return a lookup map that is used for text replacement or {@code null} to
     *         avoid text replacements.<br>
     *         It uses the keys as the text to look for and the corresponding
     *         values as replacement to this text
     */
    public Map<String, String> getReplacements() {
        return replacements;
    }
    
    /**
     * @return the selected files for the code instrumentation
     */
    public Set<File> getInstrumentationFiles() {
        return instrumentationFiles;
    }
    
    /**
     * @return a set with source code files that are required to compile the
     *         source code or {@code null} if there are no such dependencies
     */
    public Set<File> getRequiredSourceFiles() {
        return requiredSourceFiles;
    }
    
    /**
     * @return the path to look for user class files, and (optionally)
     *         annotation processors and source files or {@code null} to use
     *         the current user directory
     */
    public String getClasspath() {
       return classpath;
    }
}
