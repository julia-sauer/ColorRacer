package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.Command;
import java.io.*;
import java.net.Socket;

/**
 * Eine Klasse, die PING-Nachrichten versendet an Clients und auf PONG-Nachrichten vom Client hört.
 * Diese Klasse soll in einem separaten Thread verwendet werden, um die Verbindung aufrechtzuerhalten.
 */
public class PingThread extends Thread {
    private final Socket clientSocket;
    private final int clientNumber;
    private boolean running;

    public PingThread(Socket clientSocket, int clientNumber) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.running = true;
    }

    public void run() {
        try {
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            while (running) {
                sendCommand(out, Command.PING.name()); //Senden von Ping

                //Auf Pong warten
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < 6000) {
                    if (in.available() > 0) {
                        String response = readCommand(in);
                        if (Command.PONG.name().equals(response)) {
                            break; //Pong empfangen, Schleife wird verlassen
                        }
                    }
                }

                //WEnn kein PONG empfangen wird
                if (System.currentTimeMillis() - startTime >= 6000) {
                    System.out.println("Connection timed out for Client " + clientNumber);
                    running = false;
                    clientSocket.close();
                    Server.ClientDisconnected();
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error with Client " + clientNumber + ": " + e.getMessage());
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
