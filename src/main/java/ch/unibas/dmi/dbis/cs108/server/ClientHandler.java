package ch.unibas.dmi.dbis.cs108.server;


import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderServer;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code ClientHandler} class processes the communication between server and client.
 * It starts the {@link ProtocolReaderServer}, sends a welcome message and manages the client connection.
 * @author Jana
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int clientNumber;
    private final int userId;
    private PingThread pingThread;
    private InputStream in;
    private OutputStream out;
    private boolean running = true;
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructor of the ClientHandler class
     * @param clientNumber is the unique number of the client
     * @param socket is the socket object for the client connection
     */
    public ClientHandler(int clientNumber, Socket socket, int userId) {
        this.clientNumber = clientNumber;
        this.clientSocket = socket;
        this.userId = userId;
    }

    /**
     * Starts a {@link ProtocolReaderServer} for servers.
     * Outputs a welcome message.
     * Starts the {@link PingThread} and thus the Ping-Pong mechanism.
     * The user is removed from the UserList and the server is notified that a client has left.
     */

    public void run() {
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
            ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, out);

            // Starts a PingThread
            pingThread = new PingThread(clientSocket, clientNumber, in, out);
            pingThread.start();



            // Generates a Thread for reading messages
            ProtocolReaderServer protocolReader = new ProtocolReaderServer(
                    in, userId, out, pingThread, this::disconnectClient // Pass disconnect callback
            );

            Thread readerThread = new Thread(() -> {
                try {
                    protocolReader.readLoop();
                } catch (IOException e) {
                    System.err.println("Error when reading the message from Client " + clientNumber + ": " + e.getMessage());
                }
            });
            readerThread.start();

            String welcomeMsg = "Welcome to the Server!\n"; //Welcome message
            protocolWriterServer.sendInfo(welcomeMsg);


            // TODO
             while (running) {
                //out.write((String.valueOf((char) c)).getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("Connection closed for Client " + clientNumber);
            clientSocket.close();
            if (pingThread != null) {
                pingThread.stopPinging();
            }
            removeUser(clientNumber); // the invocation of the method removeUser
            Server.ClientDisconnected();

        }
        catch (IOException e) {
            System.err.println("Error with Client " + clientNumber + ": " + e.getMessage());
        }
    }
    /**
     * Removes a user from the user list.
     * @param clientNumber The ID of the user to be removed.
     */
    private void removeUser(int clientNumber) {

        UserList.removeUser(clientNumber);
    }
    public void disconnectClient() {
        try {
            running = false;
            if (!clientSocket.isClosed()) {
                clientSocket.close();  // Verbindung trennen
            }
        } catch (IOException e) {
            System.err.println("Error while closing client socket: " + e.getMessage());
        }
        String nickname = UserList.getUserName(userId);
        Lobby userLobby = Server.getLobbyOfPlayer(nickname);
        if (userLobby != null) {
            userLobby.removePlayer(nickname);
            System.out.println("User '" + nickname + "' removed from lobby: " + userLobby.getLobbyName());
        }
        if (pingThread != null) {
            pingThread.stopPinging();
        } // Ping stoppen
        removeUser(userId);       // aus UserList entfernen
        Server.ClientDisconnected(); // Counter runterzählen
    }


}
