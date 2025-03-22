package ch.unibas.dmi.dbis.cs108.client;

import java.io.*;
import java.net.Socket;
import ch.unibas.dmi.dbis.cs108.network.Command;

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

            while (running) {
                // Auf PING warten
                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < 15000) {
                    if (in.available() > 0) {
                        String response = readCommand(in);
                        if (Command.PING.name().equals(response)) {
                            System.out.println(response);
                            sendCommand(out, Command.PONG.name()); //Senden von Pong
                            System.out.println("PONG sent");
                            startTime = System.currentTimeMillis(); //Restart the time setter
                            break; //Pong empfangen, Schleife wird verlassen
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
    private void sendCommand(OutputStream out, String command) throws IOException {
        out.write((command + Command.SEPARATOR).getBytes());
    }

    private String readCommand(InputStream in) throws IOException {
        byte[] buffer = new byte[256];
        int bytesRead = in.read(buffer);
        return new String(buffer, 0, bytesRead).trim();
    }

    public void stopPinging() {
        running = false;
    }
}