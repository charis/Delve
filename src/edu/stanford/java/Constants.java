/*
 * File          : Constants.java
 * Author        : Charis Charitsis
 * Creation Date : 18 September 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.java;

/**
 * Contains constants used in package edu.stanford.java
 */
public class Constants
{
    /** The time limit in order to analyze a source code file */
    public static final int     TIMEOUT_IN_SEC              = 5;
    /**
     * Student ID prefix in case the student ID does not start with a valid Java
     * identifier 
     */
    public static final String  STUDENT_ID_PREFIX           = "student_";
    
    /** The name of the directory where the results are stored */
    public static final String  RESULT_DIR_NAME             = "results";
    /** The name of the file with the student IDs to skip */
    public static final String  SKIP_ID_FILENAME            = "skipID.txt";
    
    /**
     * User prompt to get the classpath to compile and run the program
     * submissions
     */
    public static final String  GET_CLASSPATH  = 
                "==> Give the classpath to copile and ru the student program "
              + "(or empty input for current-directory classpath): ";
    /**
     * User prompt to get the root directory with the submission folder where
     * each such folder contains the code snapshots for a given student
     */
    public static final String  GET_SUBMISSION_FOLDER_ROOT  = 
                                  "==> Give the path of the root directory "
                                + "with the submission folders: ";
    /**
     * User prompt to get the file with the method groups to split
     */
    public static final String  GET_FILE_WITH_METHOD_GROUPS = 
                                  "==> Give the file with the method groups to "
                                + "to split: ";
    /** The name of the file with the equivalent method candidate sets */
    public static final String  CANDIDATE_SETS_FILENAME     = "candidates.txt";
    /** The name of the file with the weak-equivalent methods */
    public static final String  WEAK_EQUIVALENT_METHODS_FILENAME = 
                                                 "weak-equivalent-methods.txt";
    /** The name of the file with the hard-equivalent methods */
    public static final String  HARD_EQUIVALENT_METHODS_FILENAME = 
                                                 "hard-equivalent-methods.txt";
}
