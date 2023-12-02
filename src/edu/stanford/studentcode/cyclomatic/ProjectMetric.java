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

/**
 * Stores the metrics for all classes of a project
 */

public class ProjectMetric 
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * List with the metrics for all packages in the project
     */
    private final List<PackageMetric> packageMetrics;
    /**
     * List with the metrics for all classes in this project
     */
    private final List<ClassMetric> classMetrics;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new ProjectMetric instance to store the metrics for all
     * classes.
     * 
     * @param classMetrics List with the metrics for the classes to store
     */
    public ProjectMetric(List<ClassMetric> classMetrics) {
        this.classMetrics = classMetrics;
        packageMetrics = new ArrayList<PackageMetric>();
        Collections.sort(this.classMetrics);
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Given the name of a package and a class in that package it returns the
     * ClassMetric object for it.
     * 
     * @param packageName The name of the package where the class belongs
     * @param className The name of the class to find
     * 
     * @return a ClassMetric object for the class if found or {@code null}
     *         otherwise
     */
    public ClassMetric getClassMetric(String packageName,
                                      String className) {
        for(ClassMetric classMetric : classMetrics) {
            if(classMetric.getPackageName().equals(packageName) && 
               classMetric.getClassName().equals(className)) {
                return classMetric;
            }
        }
        
        return null; // Found no class for the given class name and package name
    }
    
    /**
     * Checks if the project contains a particular package
     * 
     * @param packageName The package name
     * 
     * @return {@code true} if it contains a package by the given name or
     *         {@code false} otherwise
     */
    public boolean contains(String packageName) {
        for (PackageMetric packageMetric : packageMetrics) {
            if(packageMetric.getPackageName().equals(packageName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     *  Adds the given package metric to the project
     *  
     * @param packageMetric The package metric to add
     */
    public void addPackageMetric(PackageMetric packageMetric) {
        packageMetrics.add(packageMetric);
    }
    
    /**
     * Sorts the package metrics
     */
    public void sortPackages() {
        Collections.sort(packageMetrics);
    }
    
    /**
     * @return the metrics for all packages in this project
     */
    public List<PackageMetric> getPackageMetrics() {
        return packageMetrics;
    }
    
    /**
     * Returns the metric for the package with the given name
     * 
     * @param packageName The package name
     * 
     * @return the metric for the package with the given name or {@code null}
     *         if there is no package with the given name
     */
    public PackageMetric getPackageMetric(String packageName) {
        for(PackageMetric packageMetric : packageMetrics) {
            if(packageMetric.getPackageName().equals(packageName)) {
                return packageMetric;
            }
        }
        
        return null;
    }
    
    /**
     * @return the metrics for all classes in this project
     */
    public List<ClassMetric> getClassMetrics() {
        return classMetrics;
    }
}

