package ch.unibas.dmi.dbis.cs108.server;


import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * The {@code ClientHandler} class processes the communication between server and client.
 * It starts the protocol reader, sends a welcome message and manages the client connection.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int clientNumber;
    private PingThread pingThread;
    private InputStream in;
    private OutputStream out;

    /**
     * Constructor of the ClientHandler class
     * @param clientNumber is the unique number of the client
     * @param socket is the socket object for the client connection
     */
    public ClientHandler(int clientNumber, Socket socket) {
        this.clientNumber = clientNumber;
        this.clientSocket = socket;
    }

    /**
     * Starts a ProtocolReader for servers.
     * Outputs a welcome message.
     * Starts the PingThread and thus the Ping-Pong mechanism.
     * int c + while-loop is from EchoServer, so what comes in is also output again.
     * If in.read = -1, i.e. the client has left the server, the ClientSocket is terminated.
     * The user is removed from the UserList and the server is notified that a client has left.
     * @author Jana
     */

    public void run() {
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();

            // Starten des PingThreads
            //pingThread = new PingThread(clientSocket, clientNumber, in, out);
            //pingThread.start();



            // Erstellen Sie einen Thread für das Lesen von Nachrichten
            ProtocolReaderServer protocolReader = new ProtocolReaderServer(in, clientNumber, out, pingThread);
            Thread readerThread = new Thread(() -> {
                try {
                    protocolReader.readLoop();
                } catch (IOException e) {
                    System.err.println("Error when reading the message from Client " + clientNumber + ": " + e.getMessage());
                }
            });
            readerThread.start();

            String welcomeMsg = "Welcome to the Server!\n"; //Willkommensnachricht
            out.write(welcomeMsg.getBytes(StandardCharsets.UTF_8));

            int c;
             while ((c = in.read()) != -1) {
               out.write((char)c);
                //out.write((String.valueOf((char) c)).getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("Connection closed for Client " + clientNumber);
            clientSocket.close();
            PingThread.stopPinging();
            removeUser(clientNumber); // Aufruf der Methode removeUser
            Server.ClientDisconnected();

        }
        catch (IOException e) {
            System.err.println("Error with Client " + clientNumber + ": " + e.getMessage());
        }
    }
    /**
     * Removes a user from the user list.
     * @param clientNumber The ID of the user to be removed.
     * @author milo
     */
    private void removeUser(int clientNumber) {

        UserList.removeUser(clientNumber);
    }
}
