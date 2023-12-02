/*
 * File          : ClassMetric.java
 * Author        : Charis Charitsis
 * Creation Date : 18 July 2021
 * Last Modified : 19 September 2021
 */
package edu.stanford.studentcode.cyclomatic;

// Import Java SE classes
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// Import custom classes
import edu.stanford.util.StatsUtil;

/**
 * Placeholder that holds the metrics for a particular class.
 */
public class ClassMetric implements Comparable<ClassMetric>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The class name */
    private final String             className;
    /** The package name */
    private final String             packageName;
    /** List with the metrics for the methods in the class */
    private final List<MethodMetric> methodMetrics;
    /** Total number of instructions in the methods of the class */
    private final int                numOfMethodInstructions;
    /** The cyclomatic complexity values for the methods in the class */
    private final List<Integer>      cyclomaticComplexities;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new ClassMetric.
     * 
     * @param className The class name
     * @param packageName The class package name. Its default if there's no
     *                    package name
     * @param methodMetrics An list of method metric
     */
    public ClassMetric(String             className,
                       String             packageName,
                       List<MethodMetric> methodMetrics) {
        this.className     = className;
        this.packageName   = packageName;
        this.methodMetrics = methodMetrics;
        
        int totalNumOfInstuctions = 0;
        for (MethodMetric methodMetric : methodMetrics) {
            totalNumOfInstuctions += methodMetric.getNumOfInstructions();
        }
        numOfMethodInstructions = totalNumOfInstuctions;
        Collections.sort(this.methodMetrics);
        
        cyclomaticComplexities = new ArrayList<Integer>();
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this ClassMetric with another ClassMetric object for order.
     * 
     * @param other The other ClassMetric to be compared with this one
     * 
     * @return the value {@code 0}/{@code -1}/{@code 1} if this class has the
     *         same/more/less method instructions than the other class.
     */
    @Override
    public int compareTo(ClassMetric other) {
        if (numOfMethodInstructions < other.numOfMethodInstructions) {
            return 1;
        }
        else if (numOfMethodInstructions > other.numOfMethodInstructions) {
            return -1;
        }
        else {
            return 0;
        }
    }
    
    /**
     * Finds and returns an MethodMetric Object given the name of the method.
     * 
     * @param methodName the name of the method that has to be found
     * 
     * @return a MethodMetric object if a method is found or {@code null} if
     *         there are no methods found with that name
     */
    public MethodMetric getMethodMetric(String methodName) {
        for (MethodMetric methodMetric : methodMetrics) {
            if(methodMetric.getMethodName().equals(methodName)) {
                return methodMetric;
            }
        }
        
        return null; // No match
    }
    
    /**
     * @return the class name
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * @return a list with the metrics for the methods in the class
     */
    public List<MethodMetric> getMethodMetrics() {
        return methodMetrics;
    }
    
    /**
     * @return the total number of instructions in the methods of the class
     */
    public int getNumOfMethodInstructions() {
        return numOfMethodInstructions;
    }
    
    /**
     * @return the cyclomatic complexity values for the methods in the class
     */
    public List<Integer> getCyclomaticComplexities() {
        return cyclomaticComplexities;
    }
    
    /**
     * @return the average cyclomatic complexity of the methods in the class
     */
    public double getAvgCyclomaticComplexity() {
        if (cyclomaticComplexities.isEmpty()) { // Check if already calculated
            retrieveCyclomaticComplexities();
        }
        
        // Get the average complexity. Round to 2 decimal digits
        return StatsUtil.getMean(cyclomaticComplexities, 2);
    }
    
    /**
     * @return the median cyclomatic complexity of the methods in the class
     */
    public double getMedianCyclomaticComplexity() {
        if (cyclomaticComplexities.isEmpty()) { // Check if already calculated
            retrieveCyclomaticComplexities();
        }
        
        return StatsUtil.getMedianValue(cyclomaticComplexities);
    }
    
    /**
     * @return the standard deviation for the cyclomatic complexity for the
     *         methods in the class
     */
    public double getCyclomaticComplexityStdDev() {
        if (cyclomaticComplexities.isEmpty()) { // Check if already calculated
            retrieveCyclomaticComplexities();
        }
        
        // Get the std dev for the complexity. Round to 2 decimal digits
        return StatsUtil.getStdDeviation(cyclomaticComplexities, 2);
    }
    
    /**
     * @return the maximum value among the cyclomatic complexity values for the
     *         methods of the class or -1 if there are no methods
     */
    public int getMaxCyclomaticComplexity() {
        if (cyclomaticComplexities.isEmpty()) { // Check if already calculated
            retrieveCyclomaticComplexities();
        }
        
        if (cyclomaticComplexities.isEmpty()) { // No methods\
            return -1;
        }
        
        return cyclomaticComplexities.get(0);
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Retrieves the cyclomatic complexities for the methods in the class and
     * sorts them in descending order (i.e., higher cyclomatic complexity values
     * come first).
     */
    private void retrieveCyclomaticComplexities() {
        for (MethodMetric methodMetric : methodMetrics) {
            cyclomaticComplexities.add(methodMetric.getCyclomaticComplexity());
        }
    }
}