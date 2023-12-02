/*
 * File          : StreamReader.java
 * Author        : Charis Charitsis
 * Creation Date : 16 February 2015
 * Last Modified : 19 August 2018
 */
package edu.stanford.util.runtime;

// Import Java SE classes
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;

/**
 *  A bridge from byte streams to character streams: It reads bytes and decodes
 *  them into characters using the default charset. 
 */
public class StreamReader extends Thread
{
    // ----------------------------------------------------- //
    //   P  R  I  V  A  T  E     C  O  N  S  T  A  N  T  S   //
    // ----------------------------------------------------- //
    /** End of input stream */
    private static final String END_OF_STREAM = null;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The input stream */
    private final InputStream inputStream;
    /** The stream type */
    private final String      streamType;
    /** The input buffer */
    private       String[]    buffer;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a new StreamReader
     * 
     * @param inputStream The input stream
     * @param streamType The stream type
     */
    public StreamReader(InputStream inputStream,
                        String      streamType) {
        this.inputStream = inputStream;
        this.streamType  = streamType;
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    @Override
    public void run() {
        InputStreamReader inStreamReader = new InputStreamReader(inputStream);
        BufferedReader    bufferedReader = new BufferedReader(inStreamReader);
        
        List<String> tempBuffer = new ArrayList<String>();  
        String  currLine;
        try {
            while ((currLine = bufferedReader.readLine()) != END_OF_STREAM) {
                System.out.println(streamType + ">" + currLine);
                tempBuffer.add(currLine);
            }
        }
        catch (IOException ioe) {
            System.err.println("Error reading stream. Details:" + NEW_LINE
                             + ioe.getMessage());
        }
        finally {
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {
            }
            
            try {
                inStreamReader.close();
            }
            catch (IOException ignored) {
            }
        }
        
        buffer = tempBuffer.toArray(new String[0]);
    }
    
    // -------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S   //
    // -------------------------------------------------------------------- //
    /**
     * @return the stream buffer after the stream 
     */
    protected String[] getBuffer() {
        return buffer;
    }
}
