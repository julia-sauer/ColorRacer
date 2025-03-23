package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderServer;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;

import java.io.*;
import java.net.Socket;

/**
 * Eine Klasse, die PING-Nachrichten versendet an Clients und auf PONG-Nachrichten vom Client hört.
 * Diese Klasse soll in einem separaten Thread verwendet werden, um die Verbindung aufrechtzuerhalten.
 * @author Jana
 */
public class PingThread extends Thread {
    private final Socket clientSocket;
    private final int clientNumber;
    private static boolean running = true;
    private final InputStream in;
    private final OutputStream out;

    public PingThread(Socket clientSocket, int clientNumber, InputStream in, OutputStream out) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            ProtocolWriterServer.sendCommand(out, "PING"); //Senden von Ping

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 15000) {
                if (hasReceivedPong()) {
                    System.out.println("PONG received from Client " + clientNumber);
                    break;
                }
            }

            if (System.currentTimeMillis() - startTime >= 15000) {
                System.out.println("Connection timed out for Client " + clientNumber);
                clientSocket.close();
                return; //Beendet Schleife
            }

        } catch (IOException e) {
            System.err.println("Error, Could not send Command");
        }
    }

    private boolean hasReceivedPong() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            if(reader.ready()) {
                if (reader.readLine().trim().equals("PONG")) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking for PONG");
        }
        return false;
    }

    public static void pongReceived(OutputStream out, int userId) {
        System.out.println("PONG received form Client " + userId);
        try {
            ProtocolWriterServer.sendCommand(out, "PING");
            System.out.println("Next PING sent to Client " + userId);
        } catch (IOException e) {
            System.err.println("Error, Could not send Command");
        }
    }

    public static void stopPinging() {
        running = false;
        Thread.currentThread().interrupt();
    }
}
