/*
 * File          : ClassMetric.java
 * Author        : Charis Charitsis
 * Creation Date : 18 July 2021
 * Last Modified : 19 September 2021
 */
package edu.stanford.studentcode.cyclomatic;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;

/**
 * Placeholder that holds the metrics for a particular packages.
 */
public class PackageMetric implements Comparable<PackageMetric>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The package name
     */
    private final String packageName;
    /**
     * The metrics for all classes in this package
     */
    private final List<ClassMetric> classMetrics;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new PackageMetric.
     * 
     * @param packageName the package name
     */
    public PackageMetric(String packageName) {
        this.packageName = packageName;
        classMetrics = new ArrayList<ClassMetric>();
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this PackageMetric with another PackageMetric object for order.
     * 
     * @param other The other PackageMetric to be compared with this one
     * 
     * @return the value {@code 0}/{@code -1}/{@code 1} if this package has the
     *         same/more/fewer class metrics that the other package 
     */
    @Override
    public int compareTo(PackageMetric other) {
        if (classMetrics.size() < other.classMetrics.size()) {
            return 1;
        }
        else if (classMetrics.size() < other.classMetrics.size()) {
            return 1;
        }
        else {
            return 0;
        }
    }
    
    /**
     * Adds the given class metric to the package
     * 
     * @param classMetric The class metric to add
     */
    public void addClassMetric(ClassMetric classMetric) {
        classMetrics.add(classMetric);
    }
    
    /**
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * @return the metrics for all classes in this package
     */
    public List<ClassMetric> getClassMetrics() {
        return classMetrics;
    }
}
