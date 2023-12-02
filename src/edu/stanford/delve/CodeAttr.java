/*
 * File          : CodeAttr.java
 * Author        : Charis Charitsis
 * Creation Date : 13 November 2020
 * Last Modified : 1 December 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;import java.util.Set;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.body.Comment;
import edu.stanford.javaparser.body.Method;
import edu.stanford.javaparser.body.MethodCall;
import edu.stanford.util.MathUtil;
import edu.stanford.util.StatsUtil;

/**
 * Represents a code attribute such as the number of lines, the number of
 * methods etc.
 */
public class CodeAttr
{
    // ------------------------------------------------- //
    //   E   N   U   M   E   R   A   T   I   O   N   S   //
    // ------------------------------------------------- //
    /**
     * The code attribute types
     */
    protected enum Type {
        /** Tracks the number of pure lines of code */
        NUM_OF_LINES         ("Pure code lines"),
        /** Tracks the number of methods */
        NUM_OF_METHODS       ("Methods"),
        /** Tracks the number of instance variables */
        NUM_OF_IVARS         ("Instance variables"),
        /** Tracks the number of comment lines */
        NUM_OF_COMMENT_CHARS ("Comments (characters)"),
        /**
         * Tracks the ratio of the method calls over the total methods.<br>
         * Only calls to user defined methods count towards the method calls.
         */
        METHOD_CALL_RATIO    ("Method reusability ratio");
        
        /** The name of the code attribute type */
        private final String name;
        
        /**
         * Creates a new Type
         * 
         * @param name The name of the code attribute type
         */
        private Type(String name) {
            this.name = name;
        }
        
        /**
         * @return the name of the code attribute type
         */
        protected String getName() {
            return name;
        }
    }
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The type of the code attribute  */
    private final Type         type;
    /**
     * List with the measured values in the Java files for the code attribute.
     */
    private final List<Number> values;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a new CodeAttr instance.
     * 
     * @param type The code attribute type
     * @param javaFiles The Java files to track the code attribute for 
     */
    protected CodeAttr(Type          type,
                       Set<JavaFile> javaFiles) {
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' is null");
        }
        if (javaFiles == null) {
            throw new IllegalArgumentException("Argument 'javaFiles' is null");
        }
        
        this.type = type;
        values    = getValues(javaFiles);
    }
    
    // -------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S   //
    // -------------------------------------------------------------------- //
    /**
     * @return the description/name of the code attribute
     */
    protected String getName() {
        return type.getName();
    }
    
    /**
     * @return a list with the measured values in the Java files for the code
     *         attribute
     */
    protected List<Number> getValues() {
        return values;
    }
    
    /**
     * @return the attribute average value
     */
    protected double getMeanValue() {
        return StatsUtil.getMean(values, 1);
    }
    
    /**
     * @return the attribute median value
     */
    protected double getMedianValue() {
        double median = type == Type.METHOD_CALL_RATIO ?
                        StatsUtil.getMedianValue(values):
                        StatsUtil.getMedianValue(values);
        return MathUtil.round(median, 0); // round to nearest integer value
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Given a set of Java files, it processes them, tracks the code attribute
     * and returns the measured values.
     *  
     * @param javaFiles The Java files to track the code attribute for
     *  
     * @return the measured values in the given Java files for the code
     *         attribute
     */
    private List<Number> getValues(Set<JavaFile> javaFiles) {
        List<Number> values = new ArrayList<Number>();
        
        for (JavaFile javaFile : javaFiles) {
            switch (type) {
                case NUM_OF_LINES:
                     values.add(javaFile.getNumOfCodeLines());
                     break;
                
                case NUM_OF_METHODS:
                     values.add(javaFile.getNumOfMethods());
                     break;
                
                case NUM_OF_IVARS:
                     values.add(javaFile.getInstanceVariables().size());
                     break;
                
                case NUM_OF_COMMENT_CHARS:
                     int totalChars = 0;
                     for (Comment comment : javaFile.getComments()) {
                         String text = comment.getText();
                         // If a line contains ';' it is most likely commented
                         // out code. Exclude this from the comments.
                         if (!text.contains(";")) {
                             totalChars += text.length();
                         }
                     }
                     values.add(totalChars);
                     break;
                
                case METHOD_CALL_RATIO: // Those are double values, not integers
                     Set<String> methodNames = new HashSet<String>();
                     int numOfMethods = javaFile.getNumOfMethods();
                     if (numOfMethods == 0) { // Just in case
                         continue;
                     }
                     for (Method method : javaFile.getMethods()) {
                         methodNames.add(method.getName());
                     }
                     
                     List<MethodCall> methodCalls;
                     try {
                         methodCalls = javaFile.getMethodCalls();
                     }
                     catch (ErrorException ee) {
                         continue;
                     }
                     
                     int internalCalls = 0;
                     for (MethodCall methodCall : methodCalls) {
                         if (methodNames.contains(methodCall.getName())) {
                             internalCalls++;
                         }
                     }
                     
                     values.add(internalCalls / (double)numOfMethods);
                     break;
            }
        }
        
        return values;
    }
}
