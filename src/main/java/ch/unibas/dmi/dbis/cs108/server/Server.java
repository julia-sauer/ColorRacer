package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.Dice;
import ch.unibas.dmi.dbis.cs108.game.Field;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.game.GameBoard;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.nio.charset.StandardCharsets;

import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;
import ch.unibas.dmi.dbis.cs108.server.Lobby;

/**
 * The class {@code Server} provides a simple multi-user chat server.
 * It manages client connections, processes messages and takes care of user administration.
 * * The server works on port 8090 and accepts incoming client connections.
 */
public class Server {
    private static final AtomicInteger activeClients = new AtomicInteger(0);
    private static ServerSocket echod;
    public static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());
    public static String[] colors;
    public static int port;
    public static List<Lobby> lobbies = new ArrayList<Lobby>();


    /**
     * Constructor of the Serverclass
     * @param port the port-number on which the Server is
     */
    public Server(int port) {
        this.port = port;
    }

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
    public void start() {
        try {
            out.println("Waiting for port " + port + "...");
            echod = new ServerSocket(port);
            createLobby("Welcome", null);

            while (true) {
                Socket clientSocket = echod.accept();
                activeClients.incrementAndGet();
                int userId = addNewUser("Client" + activeClients.get(), clientSocket.getOutputStream());
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
    public static int addNewUser(String userName, OutputStream ClientOut) { // Neue Methode

        return UserList.addUser(userName, ClientOut);
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
        User user = UserList.getUser(userId);
        if (user == null) {return;}
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, user.getOut());

        if (!newNick.matches("^[a-zA-Z0-9_äöüÄÖÜß]{1,50}$")) {
                try {
                    protocolWriterServer.sendInfo("Invalid nickname! Must be 3–15 characters, using only letters, numbers, or underscores.");
                } catch (IOException e) {
                    System.err.println("Error sending nickname validation message to user " + userId);
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

            try {
                protocolWriterServer.sendCommandAndString(Command.NICK, finalNick);
            } catch (IOException e) {
                System.err.println("Error while sending NICK " + finalNick + " to user " + userId);
            }

    }

    /**
     * This method is called when a client sends a chat message
     * that should be forwarded to all connected clients.
     *
     * <p>The method creates a complete message in the format:
     * <pre>
     * CHAT sender: message
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

    /**
     * This method sends a message from one user to another user. With the {@link UserList} class and
     * the {@link User} class, the method gets the ID and the OutputStream of the user that should
     * receive the message.
     *
     * @param message   The message that should be sent.
     * @param sender    The user that sent the message.
     * @param receiver  The user that should receive the message.
     */
    public static void chatToOne(String message, String sender, String receiver) {
        int receiverId = UserList.getUserId(receiver);
        User receiverObject = UserList.getUser(receiverId);
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, receiverObject.getOut());
        protocolWriterServer.sendWhisper(message, sender, receiver);
    }

    public static void rollTheDice(int userId) {
        User user = UserList.getUser(userId);
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, user.getOut());
        Dice dice = new Dice();
        colors = dice.roll();
        //String[] colors in String umwandeln
        String colorText = Arrays.toString(colors);
        try {
            protocolWriterServer.sendCommandAndString(Command.ROLL, colorText);
        } catch (IOException e) {
            System.err.println("Error sending rolled colors");
        }
    }


    /**
     * Calls the send-method from the ProtocolWriterServer if the chosen field is valid.
     *
     * @param userId The id of the user
     * @param fieldId the id of the chosen field
     */
    public static void checkField(Integer userId, String fieldId) {
        User user = UserList.getUser(userId);
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, user.getOut());
        // GameBoard gameBoard = new GameBoard();  (ersetzt)
        Lobby userLobby = null;
        String nickname = user.getNickname();
        for (Lobby lobby : lobbies) {
            if (lobby.getPlayers().contains(nickname)) {
                userLobby = lobby;
                break;
            }
        }
        if (userLobby == null) return;
        GameBoard gameBoard = userLobby.getGameBoard();
        if(gameBoard.isValidField(fieldId)) {
            Field selectedField = gameBoard.getFieldById(fieldId);
            gameBoard.addSelectedField(selectedField);
            try {
                protocolWriterServer.sendCommandAndString(Command.CHOS, fieldId);
            } catch (IOException e) {
                System.err.println("Error sending " + Command.CHOS + fieldId);
            }
        } else {
            try {
                protocolWriterServer.sendCommandAndString(Command.INFO, "Field is invalid. Choose a Field that touches the current field or an already selected Field and that matches a rolled color.");
            } catch (IOException e) {
                System.err.println("Error sending error message.");
            }
        }
    }

    public static void deselectField(Integer userId, String fieldId) {
        User user = UserList.getUser(userId);
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, user.getOut());
        String fieldColor = fieldId.split("\\d")[0];
        for(int i = 0; i < colors.length; i++) {
            if(colors[i] == null) {
                colors[i] = fieldColor;
                break;
            }
        }
        String newColors = Arrays.toString(colors);
        try {
            protocolWriterServer.sendCommandAndString(Command.DEOS, fieldId + Command.SEPARATOR + newColors);
        } catch (IOException e) {
            System.err.println("Error sending DEOS + fieldId");
        }
    }

    /**
     * moves the player to the last field they selected using CHOS
     * This method finds the lobby the user belongs to and
     * gets the shared GameBoard of that lobby.
     * Executes moveToLastSelected() in the GameBoard
     * sends a message to the client with the final position
     * @param userId userId the ID of the player executing the move
     */
    public static void moveToLastSelectedField(int userId) {
        User user = UserList.getUser(userId);
        if (user == null) return;

        String nickname = user.getNickname();
        Lobby userLobby = null;

        for (Lobby lobby : lobbies) {
            if (lobby.getPlayers().contains(nickname)) {
                userLobby = lobby;
                break;
            }
        }

        if (userLobby == null) return;

        GameBoard board = userLobby.getGameBoard();
        board.moveToLastSelected();
        Field newField = board.getCurrentField();

        ProtocolWriterServer writer = new ProtocolWriterServer(clientWriters, user.getOut());

        try {
            writer.sendInfo(nickname + " moved to the Field " + newField.getFieldId());
        } catch (IOException e) {
            System.err.println("Error sending move info to user " + userId);
        }
        userLobby.advanceTurn(); // next players turn
    }



    /**
     * Broadcasts a message to all connected clients.
     *
     * <p>This method constructs a broadcast message by prepending the {@code BROD} command name
     * and a separator to the given message. It then iterates over all client writers and sends
     * the formatted message to each client.
     *
     * @param message the message to be broadcasted to all clients.
     */
    public static void broadcastToAll(String message) {
        String broadcastMessage = Command.BROD.name() + Command.SEPARATOR + message;
        for (PrintWriter writer : clientWriters) {
            writer.println(broadcastMessage);
            writer.flush();
        }
    }
    /**
     * Creates a new lobby with the given name and adds it to the global list of lobbies.
     *
     * @param lobbyName the name of the lobby to create
     */
    public static void createLobby(String lobbyName, Integer userId) {
        Lobby lobby = new Lobby (lobbyName);
        lobbies.add(lobby);
        if (userId != null) {
            User user = UserList.getUser(userId);
            ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, user.getOut());
            try {
                protocolWriterServer.sendCommandAndString(Command.CRLO, lobbyName);
            } catch (IOException e) {
                System.err.println("Error sending CRLO");
            }
        }
        printAllLobbyStates();
    }
    /**
     * Adds the specified user to the given lobby by name.
     * <p>
     * If the user is currently in the "Welcome" lobby, they will be removed from it automatically
     * before joining the new lobby. If the lobby is full, the user will be notified.
     * If the lobby does not exist, an informational message will be sent to the user.
     *
     * @param lobbyName The name of the lobby to join.
     * @param userId    The ID of the user requesting to join the lobby.
     */
    public static void joinLobby(String lobbyName, int userId) {
        User user = UserList.getUser(userId);
        if (user == null) return;

        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, user.getOut());

        boolean lobbyFound = false;

        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyName().equals(lobbyName)) {
                lobbyFound = true;

                //  Remove user from "Welcome" lobby only
                String userName = user.getNickname();
                for (Lobby otherLobby : lobbies) {
                    if (otherLobby.getLobbyName().equalsIgnoreCase("Welcome") &&
                            otherLobby.getPlayers().contains(userName)) {
                        otherLobby.removePlayer(userName);
                        System.out.println("User '" + userName + "' removed from Welcome lobby.");
                    }
                }

                if (!lobby.isFull()) {
                    boolean success = lobby.addPlayers(userId);
                    if (success) {
                        try {
                            protocolWriterServer.sendCommandAndString(Command.JOIN, lobbyName);
                        } catch (IOException e) {
                            System.err.println("Error sending JOIN to user " + userId);
                        }
                    }
                } else {
                    try {
                        protocolWriterServer.sendInfo("Lobby " + lobbyName + " is full!");
                    } catch (IOException e) {
                        System.err.println("Error sending full lobby info.");
                    }
                }
                break; // No need to keep looping
            }
        }

        if (!lobbyFound) {
            try {
                protocolWriterServer.sendInfo("Lobby " + lobbyName + " does not exist!");
            } catch (IOException e) {
                System.err.println("Error sending 'lobby not found' info.");
            }
        }
    }
    /**
     * Prints the current game state of all active lobbies to the server console.
     * This method skips the special "Welcome" lobby, as it is a placeholder and not used
     * for actual gameplay. For each other lobby, it prints:
     * <ul>
     *     <li>The lobby name</li>
     *     <li>The current game state:
     *         <ul>
     *             <li>1 = open (waiting for players)</li>
     *             <li>2 = running (game in progress)</li>
     *             <li>3 = finished (game ended)</li>
     *         </ul>
     *     </li>
     * </ul>
     * Example output:
     * <pre>
     * [Lobby: cool] Game state: open
     * [Lobby: test] Game state: running
     * </pre>
     * This method is typically used for debugging and monitoring server-side lobby activity.
     */
    public static void printAllLobbyStates() {
        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                continue; // überspringe "Welcome"-Lobby
            }

            int state = lobby.getGameState();
            String stateText = switch (state) {
                case 1 -> "open";
                case 2 -> "running";
                case 3 -> "finished";
                default -> "unknown";
            };

            System.out.println("[Lobby: " + lobby.getLobbyName() + "] Game state: " + stateText);
        }
    }

    /**
     * Returns the lobby that contains the given player.
     *
     * @param playerName the name of the player
     * @return the Lobby object the player is in, or null if not found
     */
    public static Lobby getLobbyOfPlayer(String playerName) {
        for (Lobby lobby : lobbies) {
            if (lobby.getPlayers().contains(playerName)) {
                return lobby;
            }
        }
        return null;
    }

}



