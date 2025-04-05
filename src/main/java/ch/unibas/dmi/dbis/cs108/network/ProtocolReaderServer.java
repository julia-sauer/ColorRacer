package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The class ProtocolReaderServer reads incoming messages from the client
 * and forwards them to the server.
 * The class is also responsible for receiving and interpreting
 * of messages that a client sends to the server via the network. These messages are
 * text-based and follow a specific network protocol.
 * <p>
 * This class is instantiated per client connection and continuously reads new lines
 * (commands) from the connected client via a {@link BufferedReader}. Each line is regarded as a
 * complete message (ends with a line break ‘\n’).
 * </p>
 * @author anasv
 * @since 22.03.25
 */
public class ProtocolReaderServer {
    private final BufferedReader reader; // Liest Zeichenzeilen vom Client.
    private final int userId; // Die ID des Clients, der aktuell mit dem Server verbunden ist.
    private final OutputStream out;
    private final PingThread pingThread;// added reference
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    private static final Logger LOGGER = LogManager.getLogger(ProtocolReaderServer.class);

    /**
     * Constructor: Initialises the BufferedReader and the user ID.
     *
     * @param in the InputStream from which the messages are to be read.
     * @param out the OutputStream to which replies are written.
     * @param userId the unique ID of the user.
     * @throws IOException if an error occurs when creating the BufferedReader.
     */
    public ProtocolReaderServer(InputStream in, int userId, OutputStream out, PingThread pingThread) throws IOException {
        // Initialisierung des BufferedReaders
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.userId = userId;
        this.out = out;
        this.pingThread = pingThread;
    }

    /**
     * Starts an endless loop that continuously reads messages (commands) from the client
     * and processes them using the protocol.
     *
     <p> Each message must begin with one of the defined {@link Command} enum values.
     * The method decodes these commands and executes the corresponding server action.
     * <p>
     * The network commands are processed using switch statements that the server receives from a
     * client. Each command (e.g. CHAT, NICK, PING, ...) is sent as a line of text from the client.
     * The server analyses the line, recognises the command (the first word) and executes suitable
     * actions.
     * </p>
     * @throws IOException if an error occurs when reading the messages.
     */
    public void readLoop() throws IOException {
        String line;
        Server server = new Server(Server.port);
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, out);
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            //LOGGER.error("server:  {}", line);

            String[] parts = line.split(Command.SEPARATOR, 2);
            String rawCommand = parts[0];
            Command command;

            try {
                command = Command.valueOf(rawCommand);
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown command from user ID " + userId + ": " + line);
                continue;
            }
            // Processes the command with switch-case

            switch (command) {
                 // Handels the JOIN-command from the client
                case JOIN: {
                    if (parts.length < 2 || parts[1].trim().isEmpty()){
                        protocolWriterServer.sendInfo("-ERR lobbyName missing");
                        break;
                    }
                    String lobbyName = parts[1].trim();
                    if (!lobbyName.matches("^[a-zA-Z0-9_äöüÄÖÜß]{1,50}$")) {
                        protocolWriterServer.sendInfo("-ERR Invalid lobbyName: " + lobbyName);
                        break;
                    }
                    Server.joinLobby(lobbyName, userId);
                    break;
                }
                case CRLO: {
                    if (parts.length < 2 || parts[1].trim().isEmpty()){
                        protocolWriterServer.sendInfo("-ERR lobbyName missing");
                        break;
                    }
                    String lobbyName = parts[1].trim();
                    if (!lobbyName.matches("^[a-zA-Z0-9_äöüÄÖÜß]{1,50}$")) {
                        protocolWriterServer.sendInfo("-ERR Invalid lobbyName: " + lobbyName);
                        break;
                    }
                    Server.createLobby(lobbyName, userId);
                    break;
                }
                // Ruft die changeNickname-Methode des Servers auf, wenn NICK erkannt wird.
                case NICK:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        protocolWriterServer.sendInfo("-ERR Nickname missing");
                        break;
                    }
                    String newNick = parts[1].trim();
                    Server.changeNickname(userId, newNick);
                    break;
                    // Aufruf der chatToAll methode für das Senden von einer Chatnachricht an alle Clients
                case CHAT: {
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR Empty chat message from user ID " + userId);
                        break;
                    }

                    String message = parts[1].trim();
                    if (message.length() > 500) {
                        System.err.println("-ERR Message too long from user ID " + userId);
                        break;
                    }

                    String sender = UserList.getUserName(userId);
                    if (sender == null) {
                        System.err.println("-ERR Unknown user ID: " + userId);
                        break;
                    }

                    Lobby userLobby = null;
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getPlayers().contains(sender)) {
                            userLobby = lobby;
                            break;
                        }
                    }

                    if (userLobby == null) {
                        protocolWriterServer.sendInfo("You are not currently in a lobby.");
                        break;
                    }

                    String lobbyName = userLobby.getLobbyName();
                    List<String> lobbyPlayers = userLobby.getPlayers();

                    for (String recipientName : lobbyPlayers) {
                        User recipient = UserList.getUserByName(recipientName);
                        if (recipient != null) {
                            ProtocolWriterServer writer = new ProtocolWriterServer(Server.clientWriters, recipient.getOut());
                            writer.sendChat(message, sender);
                        }
                    }

                    System.out.println("[" + lobbyName + "] " + sender + ": " + message);
                    break;
                }


                case PONG:
                    System.out.println("PONG received from Client " + userId);
                    // Connection is active, no timeout necessary
                    if (pingThread != null) {
                        pingThread.notifyPong();  // notify the ping thread that the PONG was received
                    }
                    //protocolWriterServer.sendInfo("OK PONG received");
                    break;

                case QUIT:
                    protocolWriterServer.sendInfo(" Quit request received. Please confirm [YES/NO]");
                    // Removes user

                    break;

                case QCNF:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR No confirmation received from user ID" + userId);
                        break;
                    }
                    String confirmation = parts[1].trim().toUpperCase();
                    String nickname = UserList.getUserName(userId);
                    if ("YES".equals(confirmation)) {
                        UserList.removeUser(userId); // removes the Player
                        Server.broadcastToAll("+LFT " + nickname + " has left the game");
                        Server.ClientDisconnected();
                    } else if ("NO".equals(confirmation)) {
                        // do nothing, just notify user
                        protocolWriterServer.sendInfo("You are still in the game.");
                    } else {
                        System.err.println("-ERR Invalid QCNF response from user ID " + userId);
                    }
                    break;

                case WISP:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR Empty chat message from user ID " + userId);
                        break;
                    }
                    String nicknameAndMessage = String.join(" ", parts[1]);
                    String[] nicknameAndMessageParts = nicknameAndMessage.split(Command.SEPARATOR, 2);
                    String whisperMessage = nicknameAndMessageParts[1].trim();
                    if (whisperMessage.length() > 500) {
                        System.err.println("-ERR Message too long from user ID " + userId);
                        break;
                    }
                    String senderName = UserList.getUserName(userId);
                    String receiverName = nicknameAndMessageParts[0].trim();
                    if (senderName != null && receiverName != null) {
                        server.chatToOne(whisperMessage, senderName, receiverName);
                    } else {
                        System.err.println("-ERR Unknown user ID: " + userId);
                    }
                    break;

                case ROLL:
                    Server.rollTheDice(userId);
                    break;

                case CHOS:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR No FieldId from Client " + userId);
                        break;
                    } else {
                        String fieldId = parts[1].trim();
                        Server.checkField(userId, fieldId);
                        break;
                    }

                case DEOS:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR No FieldId from Client " + userId);
                        break;
                    } else {
                        String fieldId = parts[1].trim();
                        Server.deselectField(userId, fieldId);
                        break;
                    }
                case BROD:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR Broadcast message missing " + userId);
                        break;
                    }
                    String msg = parts[1].trim();
                    String broadcasterName = UserList.getUserName(userId);
                    Server.broadcastToAll(broadcasterName + ": " + msg);
                    break;

                case STRT: {
                    String userName = UserList.getUserName(userId);
                    if (userName == null || userName.isBlank()) {
                        System.err.println("-ERR No user for ID " + userId);
                        break;
                    }

                    List<Lobby> playerLobbies = new ArrayList<>();
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getPlayers().contains(userName)) {
                            playerLobbies.add(lobby);
                        }
                    }

                    if (playerLobbies.isEmpty()) {
                        protocolWriterServer.sendInfo("You aren't part of a gameLobby. Please create a lobby or join an existing lobby to start a game.");
                        break;
                    }

                    // Prüfen ob ALLE Lobbys "Welcome" heißen
                    boolean onlyInWelcome = playerLobbies.stream()
                            .allMatch(l -> l.getLobbyName().equalsIgnoreCase("Welcome"));

                    if (onlyInWelcome) {
                        protocolWriterServer.sendInfo("You aren't part of a gameLobby. Please create a lobby or join an existing lobby to start a game.");
                        break;
                    }

                    // Jetzt starte das Spiel in der ersten echten Lobby (≠ Welcome)
                    for (Lobby lobby : playerLobbies) {
                        if (!lobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                            lobby.startGame(userId);
                            break;
                        }
                    }

                    break;
                }
                case LIST: {
                    User user = UserList.getUser(userId);
                    if (user == null) {
                        System.err.println("-ERR No user found for ID " + userId);
                        break;
                    }

                    ProtocolWriterServer writer = new ProtocolWriterServer(Server.clientWriters, user.getOut());

                    // Get all connected usernames
                    List<String> allUsers = UserList.getAllUsernames();
                    try {
                        writer.sendInfo("Connected users (" + allUsers.size() + "): " + allUsers);
                    } catch (IOException e) {
                        System.err.println("Error sending user list to user " + userId);
                        break;
                    }

                    // Show players in each lobby (excluding Welcome)
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getLobbyName().equalsIgnoreCase("Welcome")) continue;

                        List<String> players = lobby.getPlayers();
                        try {
                            writer.sendInfo("[Lobby: " + lobby.getLobbyName() + "] Players (" + players.size() + "): " + players);
                        } catch (IOException e) {
                            System.err.println("Error sending lobby list to user " + userId);
                        }
                    }

                    break;
                }

                default:
                    System.out.println("Unknown command from user ID " + userId + ": " + line);
                    break;
            }
        }
    }
}
    


