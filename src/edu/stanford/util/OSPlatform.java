/*
 * File          : OSPlatform.java
 * Author        : Charis Charitsis
 * Creation Date : 12 July 2015
 * Last Modified : 2 July 2018
 */
package edu.stanford.util;

/**
 * Utility class to find out about the OS platform
 */
public class OSPlatform
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       C   O   N   S   T   A   N   T   S   //
    // --------------------------------------------------------------------- //
    /** The OS name */
    private static final String OS_NAME = System.getProperty("os.name");
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * @return the OS name or an empty string in case of an unknown OS name
     */
    public static final String getOSName() {
        if (OS_NAME == null) { // Just in case
           return "";
        }
        return OS_NAME;
    }
    
    /**
     * @return {@code true} in case of Windows OS or {@code false} otherwise
     */
    public static boolean isWindows() {
        return osNameIncludesKeyword("win"); 
    }
    
    /**
     * @return {@code true} in case of Mac OS or {@code false} otherwise
     */
    public static boolean isMac() {
        return osNameIncludesKeyword("mac"); 
    }
    
    /**
     * @return {@code true} in case of UNIX/Linux OS or {@code false} otherwise
     */
    public static boolean isUnix() {
        return osNameIncludesKeyword("nix") ||
               osNameIncludesKeyword("nux") || 
               osNameIncludesKeyword("aix"); 
    }
    
    /**
     * @return {@code true} in case of Solaris OS or {@code false} otherwise
     */
    public static boolean isSolaris() {
        return osNameIncludesKeyword("sunos") ||
               osNameIncludesKeyword("solaris"); 
    }
    
    /**
     * @return {@code true} in case of Unix-based OS (i.e., Mac OS, UNIX/Linux,
     *         Solaris, HP-UX) or {@code false} otherwise
     */
    public static boolean isUnixBased() {
        return isMac()     ||
               isUnix()    ||
               isSolaris() ||
               osNameIncludesKeyword("hp-ux");
    }
    
    // ------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       M   E   T   H   O   D   S   //
    // ------------------------------------------------------------- //
    /**
     * Checks if the OS name includes the given keyword or not. The search is
     * case insensitive.
     * 
     * @param keyword The keyword to search for in the OS name
     * 
     * @return {@code true} if the OS name contains the given keyword (case
     *         insensitive comparison) or {@code false} otherwise
     */
    private static boolean osNameIncludesKeyword(String keyword) {
        String osName = getOSName().toLowerCase();
        keyword = keyword.toLowerCase();
        
        return osName.contains(keyword);
    }
}
