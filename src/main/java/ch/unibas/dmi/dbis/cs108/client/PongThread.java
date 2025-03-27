package ch.unibas.dmi.dbis.cs108.client;

import ch.unibas.dmi.dbis.cs108.network.Command;
import java.io.*;
import java.net.Socket;

/**
 * Eine Klasse, die auf PING-Nachrichten vom Server hört und PONG-Antworten sendet.
 * Diese Klasse soll in einem separaten Thread verwendet werden, um die Verbindung aufrechtzuerhalten.
 */
class PongThread implements Runnable {
    private final Socket clientSocket;
    private boolean running;

    /**
     * Konstruiert einen PongThread mit den angegebenen Input- und OutputStreams.
     *
     * @param clientSocket der BufferedReader von dem gelesen werden soll
     *
     */
    public PongThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.running = true;
    }

    /**
     * Hört auf PING-Nachrichten vom Server und sendet PONG-Antworten.
     * Diese Methode läuft in einer Schleife und liest kontinuierlich Nachrichten aus dem InputStream.
     */
    public void run() {
        try {
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while (running) {
                // Auf PING warten
                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < 15000) {
                    // Wait for a PING message or timeout
                    if (reader.ready()) {
                        String message = reader.readLine();
                        if (message != null && "PING".equals(message.trim())) {
                            System.out.println("PING received");
                            sendCommand(out,  Command.PONG + Command.SEPARATOR);
                            System.out.println("PONG sent");
                            startTime = System.currentTimeMillis(); // Reset the timer
                        }
                    }
                }
                if (System.currentTimeMillis() - startTime >= 15000) {
                    System.out.println("Server lost connection.");
                    running = false;
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR with server connection: " + e.toString());
        }
    }

    /**
     * Sendet einen Befehl über den OutputStream an den Server.
     *
     * @param out Der OutputStream, über den der Befehl gesendet wird.
     * @param command Der zu sendende Befehl.
     * @throws IOException Falls ein Fehler beim Senden des Befehls auftritt.
     */
    private void sendCommand(OutputStream out, String command) throws IOException {
        out.write((command + Command.SEPARATOR).getBytes());
    }

    /**
     * Liest einen Befehl aus dem InputStream.
     *
     * @param in Der InputStream, aus dem der Befehl gelesen wird.
     * @return Der gelesene Befehl als String.
     * @throws IOException Falls ein Fehler beim Lesen des Befehls auftritt.
     */
    private String readCommand(InputStream in) throws IOException {
        byte[] buffer = new byte[256];
        int bytesRead = in.read(buffer);
        return new String(buffer, 0, bytesRead).trim();
    }

    /**
     * Stoppt das Pinging und beendet den Thread.
     */
    public void stopPinging() {
        running = false;
    }
}