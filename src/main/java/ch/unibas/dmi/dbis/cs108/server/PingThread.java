package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The class {@code PingThread} sends PING messages to clients and listens for PONG messages from the client.
 * This class should be used in a separate thread to maintain the connection.
 * @author Jana
 */
public class PingThread extends Thread {
    private final Socket clientSocket;
    private final int clientNumber;
    private static boolean running = true;
    private final InputStream in;
    private final OutputStream out;
    private static final long PING_INTERVAL = 5000;
    private volatile boolean pongReceived = false;// 15 seconds
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());


    /**
     * Creates a new {@code PingThread} for a specific client.
     *
     * @param clientSocket The client's socket.
     * @param clientNumber The unique number of the client.
     * @param in The InputStream for messages.
     * @param out The OutputStream for messages.
     */

    public PingThread(Socket clientSocket, int clientNumber, InputStream in, OutputStream out) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.in = in;
        this.out = out;
    }
    /**
     * Starts the ping thread, which regularly sends PING messages and waits for PONG responses.
     * If no response is received within the defined time limit, the connection is closed.
     */
    @Override
    public void run() {
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, out);
        while (running && !clientSocket.isClosed()) {
            try {
                protocolWriterServer.sendCommand(Command.PING); //Sends Ping
                pongReceived = false;

                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < PING_INTERVAL) {
                    if (pongReceived) {
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

    /**
     * Notifies the PingThread that a PONG has been received from the client.
     */
    public void notifyPong(){
        pongReceived = true;
    }

    /**
     * Checks whether a PONG message has been received.
     *
     * @return {@code true} if a PONG message has been received, otherwise {@code false}.
     */
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

    /**
     * Processes a received PONG message and sends the next PING to the client.
     *
     * @param out The OutputStream of the client.
     * @param userId The ID of the client that sent the PONG.
     */
    /*
    public static void pongReceived(OutputStream out, int userId) {
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, out);
        try {
            protocolWriterServer.sendCommand(Command.PING);
            System.out.println("Next PING sent to Client " + userId);
        } catch (IOException e) {
            System.err.println("Error, Could not send Command");
        }
    }
    */


    /**
     * Stops the ping thread and interrupts the current thread.
     */
    public static void stopPinging() {
        running = false;
        Thread.currentThread().interrupt();
    }
}
