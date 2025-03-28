package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import ch.unibas.dmi.dbis.cs108.network.Command;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.nio.charset.StandardCharsets;

import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;

/**
 * The class {@code Server} provides a simple multi-user chat server.
 * It manages client connections, processes messages and takes care of user administration.
 * * The server works on port 8090 and accepts incoming client connections.
 */
public class Server {
    private static final AtomicInteger activeClients = new AtomicInteger(0);
    private static ServerSocket echod;
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    /**
     * Starts a server that waits for connections and establishes a network connection
     * from client to server.
     * ServerSocket creates a server (here: echod) that runs on port 8090.
     * echod.accept(); waits until client connects.
     * As soon as a client has connected, ‘Connection established’ is printed.
     * while-loop: until the client ends the connection. Saves what comes from the client in c
     * and returns exactly the same (out.write).
     * When client connection is closed, it exits while loop, then connection to client is closed
     * (socket.close) then server is closed (echod.close).
     * @author Jana
     */
    public static void main(String[] args) {
        try {
            out.println("Waiting for port 8090...");
            echod = new ServerSocket(8090);

            while (true) {
                Socket clientSocket = echod.accept();
                activeClients.incrementAndGet();
                int userId = addNewUser("Client" + activeClients.get());
                out.println("Connection established for Client: " + activeClients.get());

                ClientHandler cH = new ClientHandler(activeClients.get(), clientSocket);
                Thread cHT = new Thread(cH);
                cHT.start();

                PrintWriter clientWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                clientWriters.add(clientWriter);

            }

        } catch (IOException e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }
    /**
     * Adds a new user to the user list.
     * @param userName is the name of the new user.
     * @return the unique user-ID.
     * @author milo
     */
    public static int addNewUser(String userName) { // Neue Methode

        return UserList.addUser(userName, out);
    }

    /**
     * Informs the server that a client has left.
     * The active clients are reduced by 1 and the number of clients still active is displayed.
     * If there are no more active clients, a thread is started that waits 60 seconds for new clients.
     * After 60 seconds, the server is shut down.
     */
    public static void ClientDisconnected() {
        activeClients.decrementAndGet();
        out.println("Remaining Clients: " + activeClients.get());

        if (activeClients.get() == 0) {
            out.println("Wait 60 seconds for new clients...");
            new Thread(() -> {
                try {
                    Thread.sleep(60000);
                    if (activeClients.get() == 0) {
                        out.println("No new clients. Server is shutting down.");
                        shutdownServer();
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }

    /**
     * Shuts down the server.
     * @author Jana
     */

    public static void shutdownServer() {
        try {
            echod.close();
            System.exit(0);
        }
        catch (IOException e) {
            System.err.println("Error when closing the Server: " + e.getMessage());
        }
    }

    /**
     * Changes a user's nickname.
     * Checks whether the nickname already exists using UserList and adds a 1 if so.
     * Then calls sendCommand from {@link ProtocolWriterServer}, which sends the message to the client
     * that the nickname has been changed.
     * @param userId The ID of the user who wants to change the nickname.
     * @param newNick The new desired nickname.
     * @author milo
     */

    public static void changeNickname(int userId, String newNick) {
        // Validating of the new nickname (3–50 symbols; only letters, numbers and underscores)
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, out);
        if (!newNick.matches("^[a-zA-Z0-9_]{3,50}$")) {
            User user = UserList.getUser(userId);
            if (user != null) {
                try {
                    protocolWriterServer.sendInfo("Invalid nickname! Must be 3–15 characters, using only letters, numbers, or underscores.");
                } catch (IOException e) {
                    System.err.println("Error sending nickname validation message to user " + userId);
                }
            }
            return;
        }

        // Check for duplicates and adjust with suffix if necessary
        String finalNick = newNick;
        int suffix = 1;
        while (UserList.containsUserName(finalNick)) {
            finalNick = finalNick + suffix;
        }

        // updates nickname
        UserList.updateUserName(userId, finalNick);

        // sends message to client
        User user = UserList.getUser(userId);
        if (user != null) {
            try {
                ProtocolWriterServer.sendCommandAndString(user.getOut(), Command.NICK, finalNick);
            } catch (IOException e) {
                System.err.println("Error while sending NICK " + finalNick + " to user " + userId);
            }
        }
    }

    /**
     * This method is called when a client sends a chat message
     * that should be forwarded to all connected clients.
     *
     * <p>The method creates a complete message in the format:
     * <pre>
     * CHAT <sender>: <message>
     * </pre>
     * and sends it over all {@link PrintWriter} connections
     * known to the server (list {@code clientWriters}).
     *
     * <p>Each client is thus notified that a new message has arrived,
     * including the sender's name.
     *
     * @param message The chat message entered by the client.
     * @param sender The sender's username.
     */

    public void chatToAll(String message, String sender) {
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, out);
        protocolWriterServer.sendChat(message, sender);
        //String chatMessage = Command.CHAT + " " + sender + ": " + message; // Formats the message according to protocol: CHAT <sender>: <message>
        //for (PrintWriter writer : clientWriters) { // Iterates over all registered client output streams and sends the message
        //    writer.println(chatMessage);
        //    writer.flush();
    }
}



