package ch.unibas.dmi.dbis.cs108.client;

import java.io.*;

/**
 * Eine Klasse, die auf PING-Nachrichten vom Server hört und PONG-Antworten sendet.
 * Diese Klasse soll in einem separaten Thread verwendet werden, um die Verbindung aufrechtzuerhalten.
 */
class PongThread implements Runnable {
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Konstruiert einen PongThread mit den angegebenen Input- und OutputStreams.
     *
     * @param in der BufferedReader von dem gelesen werden soll
     * @param out der PrintWriter, in den geschrieben werden soll
     */
    public PongThread(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Hört auf PING-Nachrichten vom Server und sendet PONG-Antworten.
     * Diese Methode läuft in einer Schleife und liest kontinuierlich Nachrichten aus dem InputStream.
     */
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if ("PING".equals(message)) {
                    out.println("PONG");
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: " + e.toString());
        }
    }
}