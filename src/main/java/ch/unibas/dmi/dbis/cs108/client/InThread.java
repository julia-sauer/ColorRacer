package ch.unibas.dmi.dbis.cs108.client;

import java.io.*;

/**
 * Eine lauffähige Klasse, die Daten aus einem InputStream liest und sie in die Standardausgabe schreibt.
 * Diese Klasse soll in einem separaten Thread verwendet werden, um kontinuierlich von einem Netzwerksocket zu lesen.
 *
 * @author julia
 * @since 21.03.2025
 */
class InThread implements Runnable {
    private InputStream in;

    /**
     * Konstruiert einen InThread mit dem angegebenen InputStream.
     *
     * @param in den InputStream, aus dem gelesen werden soll
     */
    public InThread(InputStream in) {
        this.in = in;
    }

    /**
     * Liest Daten aus dem InputStream und schreibt sie in die Standardausgabe.
     * Diese Methode läuft in einer Schleife und liest kontinuierlich Bytes aus dem InputStream
     * bis das Ende des Streams erreicht ist (angezeigt durch einen Rückgabewert von -1 von read()).
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