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

  private final Socket clientSocket;
  private final int clientNumber;
  private boolean running = true;
  private final InputStream in;
  private final OutputStream out;
  private static final long PING_INTERVAL = 10000;
  private volatile boolean pongReceived = false;// 15 seconds
  private static final List<PrintWriter> clientWriters = Collections.synchronizedList(
      new ArrayList<>());
  /**
   * A hand‑off queue from ProtocolReaderServer.notifyPong() to here.
   */
  private final ArrayBlockingQueue<Boolean> pongQueue = new ArrayBlockingQueue<>(1);


  /**
   * Creates a new {@code PingThread} for a specific client.
   *
   * @param clientSocket The client's socket.
   * @param clientNumber The unique number of the client.
   * @param in           The InputStream for messages.
   * @param out          The OutputStream for messages.
   */

  public PingThread(Socket clientSocket, int clientNumber, InputStream in, OutputStream out) {
    this.clientSocket = clientSocket;
    this.clientNumber = clientNumber;
    this.in = in;
    this.out = out;
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
        // 1) send the PING
        protocolWriterServer.sendCommand(Command.PING);
        System.out.println("PING sent");

        // 2) wait up to PING_INTERVAL_MS for a notifyPong() call
        Boolean pong = pongQueue.poll(PING_INTERVAL, TimeUnit.MILLISECONDS);

        if (pong == null) {
          // no PONG in time ⇒ timeout
          System.out.println("Connection timed out for Client " + clientNumber);
          clientSocket.close();
          break;
        }
        // 3) we got a PONG—now *pause* before sending the next PING
        Thread.sleep(PING_INTERVAL);

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (IOException e) {
        System.err.println("Error sending PING to Client " + clientNumber + ": " + e.getMessage());
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
   * Checks whether a PONG message has been received.
   *
   * @return {@code true} if a PONG message has been received, otherwise {@code false}.
   */
  private boolean hasReceivedPong() {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      if (reader.ready()) {
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
   * Stops the ping thread and interrupts the current thread.
   */
  public void stopPinging() {
    running = false;
    Thread.currentThread().interrupt();
  }
}
