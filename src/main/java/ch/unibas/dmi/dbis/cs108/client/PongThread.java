package ch.unibas.dmi.dbis.cs108.client;

import ch.unibas.dmi.dbis.cs108.network.Command;
import java.io.*;
import java.net.Socket;

/**
 * A class that listens for PING messages from the server and sends PONG responses.
 * This class should be used in a separate thread to maintain the connection.
 * @deprecated
 */
class PongThread implements Runnable {
    private final Socket clientSocket;
    private boolean running;

    /**
     * Constructs a PongThread with the specified input and output streams.
     *
     * @param clientSocket the BufferedReader to read from
     */
    public PongThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.running = true;
    }

    /**
     * Listens for PING messages from the server and sends PONG responses.
     * This method runs in a loop and continuously reads messages from the InputStream.
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
     * Sends a command to the server via the OutputStream.
     *
     * @param out The OutputStream via which the command is sent.
     * @param command The command to be sent.
     * @throws IOException If an error occurs when sending the command.
     */
    private void sendCommand(OutputStream out, String command) throws IOException {
        out.write((command + Command.SEPARATOR).getBytes());
        out.flush();
    }

    /**
     * Reads a command from the InputStream.
     *
     * @param in The InputStream from which the command is read.
     * @return The command read as a string.
     * @throws IOException If an error occurs when reading the command.
     */
    private String readCommand(InputStream in) throws IOException {
        byte[] buffer = new byte[256];
        int bytesRead = in.read(buffer);
        return new String(buffer, 0, bytesRead).trim();
    }

    /**
     * Stops the pinging and ends the thread.
     */
    public void stopPinging() {
        running = false;
    }
}