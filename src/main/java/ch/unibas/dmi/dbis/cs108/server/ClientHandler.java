package ch.unibas.dmi.dbis.cs108.server;


import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderServer;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code ClientHandler} class handles an individual client connection to the server.
 * It manages communication, initiates a {@link ProtocolReaderServer} for incoming messages,
 * starts a {@link PingThread} for connection health monitoring, and handles disconnection logic.
 * This class is run in its own thread for each client.
 *
 * @author Jana
 */
public class ClientHandler implements Runnable {

    /** The socket for communication with the client. */
    private final Socket clientSocket;

    /** The unique identifying number assigned to the client. */
    private final int clientNumber;

    /** The user ID corresponding to this client. */
    private final int userId;

    /** The ping thread responsible for sending PINGs and detecting lost connections. */
    private PingThread pingThread;

    /** Input stream for reading data from the client. */
    private InputStream in;

    /** Output stream for sending data to the client. */
    private OutputStream out;

    /** Flag indicating if the handler is still running. */
    private boolean running = true;

    /** List of all client output streams for broadcasting messages. */
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructs a new {@link  ClientHandler} for the given client.
     *
     * @param clientNumber The client’s unique number on the server
     * @param socket The socket connected to the client
     * @param userId The user ID for identification
     */
    public ClientHandler(int clientNumber, Socket socket, int userId) {
        this.clientNumber = clientNumber;
        this.clientSocket = socket;
        this.userId = userId;
    }

    /**
     * The main run method of this client handler.
     * <ul>
     *     <li>Initializes input/output streams</li>
     *     <li>Starts the {@link PingThread} for health monitoring</li>
     *     <li>Starts the {@link ProtocolReaderServer} to handle client messages</li>
     *     <li>Sends a welcome message</li>
     *     <li>Handles clean disconnection on error or manual leave</li>
     * </ul>
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
            Server.updateAllClients();
            Server.ClientDisconnected();

        }
        catch (IOException e) {
            System.err.println("Error with Client " + clientNumber + ": " + e.getMessage());
        }
    }
    /**
     * Removes a user from the {@link UserList} based on their client number.
     *
     * @param clientNumber The ID of the user to be removed
     */
    private void removeUser(int clientNumber) {

        UserList.removeUser(clientNumber);
    }

    /**
     * Called when the client is detected to be disconnected or times out.
     * This method handles full cleanup:
     * <ul>
     *     <li>Closes socket</li>
     *     <li>Removes user from lobby and user list</li>
     *     <li>Stops ping thread</li>
     *     <li>Notifies the server of disconnection</li>
     * </ul>
     */
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
