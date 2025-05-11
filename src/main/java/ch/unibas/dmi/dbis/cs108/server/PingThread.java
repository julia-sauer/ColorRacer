package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The class {@code PingThread} sends PING messages to clients and listens for PONG messages from
 * the client. This class should be used in a separate thread to maintain the connection.
 *
 * @author Jana
 */
public class PingThread extends Thread {

    /**
     * The socket through which to communicate with the client.
     */
    private final Socket clientSocket;

    /**
     * A unique identifier for this client, used in logging and cleanup.
     */
    private final int clientNumber;

    /**
     * Flag used to control the thread's main loop; set false to stop pinging.
     */
    private boolean running = true;

    /**
     * Stream from which incoming protocol messages (PONG) are read.
     */
    private final InputStream in;

    /**
     * Stream to which outgoing protocol messages (PING or QCNF) are written.
     */
    private final OutputStream out;

    /**
     * Interval between consecutive PING messages, in milliseconds.
     */
    private static final long PING_INTERVAL = 10000;

    /**
     * Shared list of all client writers for broadcasting, kept in sync.
     */
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    /**
     * A one-slot hand‑off queue to receive PONG notifications from the reader.
     */
    private final ArrayBlockingQueue<Boolean> pongQueue = new ArrayBlockingQueue<>(1);

    /**
     * Callback to invoke when a timeout or {@link IOException} error requires a clean disconnect.
     */
    private final Runnable disconnectCallback;


    /**
     * Creates a new {@code PingThread} for a specific client.
     *
     * @param clientSocket The client's socket.
     * @param clientNumber The unique number of the client.
     * @param in           The InputStream for messages.
     * @param out          The OutputStream for messages.
     */

    public PingThread(Socket clientSocket, int clientNumber, InputStream in, OutputStream out, Runnable disconnectCallback) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.in = in;
        this.out = out;
        this.disconnectCallback = disconnectCallback;
    }

    /**
     * Starts the ping thread, which regularly sends PING messages and waits for PONG responses. If no
     * response is received within the defined time limit, the connection is closed.
     */
    @Override
    public void run() {
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, out);
        while (running && !clientSocket.isClosed()) {
            try {
                protocolWriterServer.sendCommand(Command.PING);
                System.out.println("PING sent");

                Boolean pong = pongQueue.poll(PING_INTERVAL, TimeUnit.MILLISECONDS);
                if (pong == null) {
                    System.out.println("Connection timed out for Client " + clientNumber);
                    protocolWriterServer.sendCommand(Command.QCNF);
                    disconnectCallback.run();
                    break;
                }
                Thread.sleep(PING_INTERVAL);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                System.err.println("Error sending PING to Client " + clientNumber + ": " + e.getMessage());
                disconnectCallback.run();
                break;
            }
        }
    }

    /**
     * Notifies the PingThread that a PONG has been received from the client.
     */
    public void notifyPong() {
        pongQueue.offer(Boolean.TRUE);
    }

    /**
     * Stops the ping thread and interrupts the current thread.
     */
    public void stopPinging() {
        running = false;
        Thread.currentThread().interrupt();
    }
}
