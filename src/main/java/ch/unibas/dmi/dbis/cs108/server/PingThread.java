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
    private static final long PING_INTERVAL = 15000;
    private volatile boolean pongReceived = false;// 15 seconds

    public PingThread(Socket clientSocket, int clientNumber, InputStream in, OutputStream out) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.in = in;
        this.out = out;
    }
    @Override
    public void run() {
        while (running && !clientSocket.isClosed()) {
            try {
                ProtocolWriterServer.sendCommand(out, "PING "); //Senden von Ping
                pongReceived = false;

                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < PING_INTERVAL) {
                    if (pongReceived) {
                        System.out.println("PONG received from Client " + clientNumber);
                        break;
                    }
                }
                Thread.sleep(100); // sleep briefly instead of busy waiting

                if (!pongReceived) {
                    System.out.println("Connection timed out for Client " + clientNumber);
                    // Clean up: remove user and close the socket
                    UserList.removeUser(clientNumber);
                    Server.ClientDisconnected();
                    clientSocket.close();
                    break;
                }
                // Wait before sending the next PING
                Thread.sleep(PING_INTERVAL);

            } catch (IOException | InterruptedException e) {
                System.err.println("Error, Could not send Command");
                break;
            }
        }
    }

    public void notifyPong(){
        pongReceived = true;
    }

    private boolean hasReceivedPong() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            if(reader.ready()) {
                if (reader.readLine().trim().equals("PONG ")) {
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
            ProtocolWriterServer.sendCommand(out, "PING ");
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
