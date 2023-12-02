/*
 * File          : MemoryClassLoader.java
 * Author        : Charis Charitsis
 * Creation Date : 23 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.java.compile;

// Import Java SE classes
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * ClassLoader that loads .class bytes from memory.
 */
public class MemoryClassLoader extends URLClassLoader
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * Map where the key is the classname and the value is the class bytes.
     */
    private final Map<String, byte[]> classbyteLookup;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a new MemoryClassLoader.
     * 
     * @param classbyteLookup Map where the key is the classname and the value
     *                        is the class bytes.
     */
    public MemoryClassLoader(Map<String, byte[]> classbyteLookup) {
        this(classbyteLookup,
             null,
             ClassLoader.getSystemClassLoader());
    }
    
    /**
     * Constructs a new MemoryClassLoader.
     * 
     * @param classbyteLookup Map where the key is the classname and the value
     *                        is the class bytes.
     * @param classpath The classpath to consider when loading the classes
     */
    public MemoryClassLoader(Map<String, byte[]> classbyteLookup,
                             String              classpath) {
        this(classbyteLookup,
             classpath,
             ClassLoader.getSystemClassLoader());
    }
    
    /**
     * Constructs a new MemoryClassLoader.
     * 
     * @param classbyteLookup Map where the key is the classname and the value
     *                        is the class bytes.
     * @param classpath The classpath to consider when loading the classes
     * @param parent The parent class loader for delegation
     */
    public MemoryClassLoader(Map<String, byte[]> classbyteLookup,
                             String              classpath,
                             ClassLoader         parent) {
        super(toURLs(classpath), parent);
        this.classbyteLookup = classbyteLookup;
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Returns a list with all loaded classes.
     *  
     * @return a list with all loaded classes.
     * 
     * @throws ClassNotFoundException if a class which is supposed to be loaded
     *                                is not found
     */
    public List<Class<?>> loadAllClasses()
           throws ClassNotFoundException {
        List<Class<?>> classes =
                       new ArrayList<Class<?>>(classbyteLookup.size());
        for (String fullyQualifiedClassName : classbyteLookup.keySet()) {
            classes.add(loadClass(fullyQualifiedClassName));
        }
        return classes;
    }
    
    // -------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S   //
    // -------------------------------------------------------------------- //
    /**
     * Finds and loads the class with the specified name.
     * 
     * @param fullyQualifiedClassName The fully qualified class name
     * 
     * @throws ClassNotFoundException if the class could not be found,or if the
     *                                loader is closed
     */
    @Override
    protected Class<?> findClass(String fullyQualifiedClassName)
              throws ClassNotFoundException {
        byte[] classbytes = classbyteLookup.get(fullyQualifiedClassName);
        if (classbytes != null) {
            // Clear the bytes in the map; we don't need it anymore
            classbyteLookup.put(fullyQualifiedClassName, null);
            return defineClass(fullyQualifiedClassName,
                               classbytes,
                               0,
                               classbytes.length);
        }
        else {
            return super.findClass(fullyQualifiedClassName);
        }
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Splits a classpath to the URLs of the individual paths that it consists
     * of.
     *  
     * @param classpath The classpath to process
     * 
     * @return an array with the URLs of the individual paths that the classpath
     *         consists of
     */
    private static URL[] toURLs(String classpath) {
        if (classpath == null) {
            return new URL[0];
        }
        
        List<URL> list = new ArrayList<URL>();
        StringTokenizer tokenizer = new StringTokenizer(classpath,
                                                        File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            File file = new File(token);
            
            if (file.exists()) {
                try {
                    list.add(file.toURI().toURL());
                } 
                catch (MalformedURLException mue) {
                    // Do nothing
                }
            }
            else {
                try {
                    list.add(new URL(token));
                }
                catch (MalformedURLException mue) {
                    // Do nothing
                }
            }
        }
        
        return list.toArray(new URL[0]);
    }
}