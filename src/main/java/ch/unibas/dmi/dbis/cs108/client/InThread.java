package ch.unibas.dmi.dbis.cs108.client;

import java.io.*;

/**
 * An executable class that reads data from an InputStream and writes it to the standard output.
 * This class should be used in a separate thread to continuously read from a network socket.
 *
 * @author julia
 * @since 03/21/2025
 */
class InThread implements Runnable {
    private InputStream in;
    /**
     * Constructs an InThread with the specified InputStream.
     *
     * @param in the InputStream from which to read
     */

    public InThread(InputStream in) {
        this.in = in;
    }

    /**
     * Reads data from the InputStream and writes it to the standard output.
     * This method runs in a loop and continuously reads bytes from the InputStream
     * until the end of the stream is reached (indicated by a return value of -1 from read()).
     */
    public void run() {
        byte[] buffer = new byte[1024]; // Increased buffer size
        int bytesRead;
        try {
            while ((bytesRead = in.read(buffer)) != -1) {
                System.out.write(buffer, 0, bytesRead);
                System.out.flush(); // Ensure output is flushed
            }
        } catch (IOException e) {
            System.err.println("ERROR: " + e.toString());
        }
    }
}