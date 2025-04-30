package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The class {@code ProtocolReaderServer} reads incoming messages from the client and forwards them
 * to the server. The class is also responsible for receiving and interpreting messages that a
 * client sends to the server via the network. These messages are text-based and follow a specific
 * network protocol.
 * <p>
 * This class is instantiated per client connection and continuously reads new lines (commands) from
 * the connected client via a {@link BufferedReader}. Each line is regarded as a complete message
 * (ends with a line break ‘\n’). It works closely with the {@link ProtocolWriterServer} to send
 * responses when needed.
 * </p>
 *
 * @author anasv
 * @since 22.03.25
 */
public class ProtocolReaderServer {

    private final BufferedReader reader; // Liest Zeichenzeilen vom Client.
    private final int userId; // Die ID des Clients, der aktuell mit dem Server verbunden ist.
    private final OutputStream out;
    private final PingThread pingThread;// added reference
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(
            new ArrayList<>());
    private static final Logger LOGGER = LogManager.getLogger(ProtocolReaderServer.class);
    private final Runnable disconnectCallback;
    private final ClientHandler clientHandler;

    /**
     * Creates a new {@code ProtocolReaderServer}.
     *
     * @param in     The InputStream from which the messages are to be read.
     * @param out    The OutputStream to which replies are written.
     * @param userId The unique ID of the user.
     * @throws IOException If an error occurs when creating the BufferedReader.
     */
    public ProtocolReaderServer(InputStream in, int userId, OutputStream out, PingThread pingThread,
                                ClientHandler clientHandler, Runnable disconnectCallback) throws IOException {
        // Initialisierung des BufferedReaders
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.userId = userId;
        this.out = out;
        this.pingThread = pingThread;
        this.clientHandler = clientHandler;
        this.disconnectCallback = disconnectCallback;
    }

    /**
     * Checks if the current user is allowed to perform a turn action. Sends an error to the client if
     * it's not their turn.
     *
     * @param writer The {@link ProtocolWriterServer} that is needed to send the information forward.
     * @return True if it's the player's turn, false otherwise.
     */
    private boolean isMyTurn(ProtocolWriterServer writer) throws IOException {
        String nickname = UserList.getUserName(userId);
        Lobby lobby = Server.getLobbyOfPlayer(nickname);

        if (lobby == null || !lobby.isCurrentPlayer(nickname)) {
            writer.sendInfo("-ERR It's not your turn.");
            return false;
        }
        return true;
    }

    /**
     * Starts an endless loop that continuously reads messages (commands) from the client and
     * processes them using the protocol.
     * <p>
     * Each message must begin with one of the defined {@link Command} enum values. The method decodes
     * these commands and executes the corresponding server action.
     * <p>
     * The network commands are processed using switch statements that the server receives from a
     * client. Each command (e.g. CHAT, NICK, PING, ...) is sent as a line of string from the client.
     * The server analyses the string, recognises the command (the first word) and executes suitable
     * actions.
     * </p>
     *
     * @throws IOException If an error occurs when reading the messages.
     */
    public void readLoop() throws IOException {
        String line;
        Server server = new Server(Server.port);
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, out);
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }

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
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        protocolWriterServer.sendInfo("-ERR lobbyName missing");
                        break;
                    }
                    String lobbyName = parts[1].trim();
                    if (!lobbyName.matches("^[a-zA-Z0-9_äöüÄÖÜß]{1,50}$")) {
                        protocolWriterServer.sendInfo("-ERR Invalid lobbyName: " + lobbyName);
                        break;
                    }
                    Server.joinLobby(lobbyName, userId);
                    Server.updateAllClients();
                    break;
                }
                case CRLO: {
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        protocolWriterServer.sendInfo("-ERR lobbyName missing");
                        break;
                    }
                    String lobbyName = parts[1].trim();
                    if (!lobbyName.matches("^[a-zA-Z0-9_äöüÄÖÜß]{1,50}$")) {
                        protocolWriterServer.sendInfo("-ERR Invalid lobbyName: " + lobbyName);
                        break;
                    }
                    Server.createLobby(lobbyName, userId);
                    Server.updateAllClients();
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
                    Server.updateAllClients();
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
                            ProtocolWriterServer writer = new ProtocolWriterServer(Server.clientWriters,
                                    recipient.getOut());
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
                        Server.updateAllClients();
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
                        protocolWriterServer.sendInfo("Disconnecting...");

                        // 🗣️ 1. Alle informieren, bevor die Verbindung geschlossen wird
                        Server.broadcastToAll("+LFT " + nickname + " has left the game");
                        UserList.removeUser(userId);
                        Lobby userLobby = null;
                        for (Lobby lobby : Server.lobbies) {
                            if (lobby.getPlayers().contains(nickname)) {
                                userLobby = lobby;
                            }
                        }
                        if (userLobby != null) {
                            userLobby.removePlayer(nickname);
                        }

                        // 🧹 2. Jetzt sauberen Disconnect durchführen
                        clientHandler.disconnectClient();
//                        if (disconnectCallback != null) {
//                            disconnectCallback.run();
//                            Server.updateAllClients();
//                        }

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
                        Server.chatToOne(whisperMessage, senderName, receiverName);
                    } else {
                        System.err.println("-ERR Unknown user ID: " + userId);
                    }
                    break;

                case ROLL:
                    Server.rollTheDice(userId);
                    break;

                case CHOS:
                    if (!isMyTurn(protocolWriterServer)) {
                        break;
                    }
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR No FieldId from Client " + userId);
                        break;
                    } else {
                        String fieldId = parts[1].trim();
                        Server.checkField(userId, fieldId);
                        break;
                    }

                case MOVE:
                    String nick = UserList.getUserName(userId);
                    Lobby userLob = Server.getLobbyOfPlayer(nick);

                    if (userLob != null) {
                        if (!(userLob.getGameState() == 2)) {
                            protocolWriterServer.sendInfo("The game has not started yet or is already finished.");
                            break;
                        }

                        if (!isMyTurn(protocolWriterServer)) {
                            break;
                        }
                        Server.moveToLastSelectedField(userId);
                        break;
                    }

                case NEXT: {
                    String nickn = UserList.getUserName(userId);
                    Lobby userLobby = Server.getLobbyOfPlayer(nickn);
                    if (userLobby != null) {
                        if (!(userLobby.getGameState() == 2)) {
                            protocolWriterServer.sendInfo("The game has not started yet or is already finished.");
                            break;
                        }

                        if (!isMyTurn(protocolWriterServer)) {
                            break;
                        }

                        protocolWriterServer.sendInfo("You skipped your turn.");
                        userLobby.advanceTurn();  // next player

                        break;
                    } else {
                        protocolWriterServer.sendInfo("-ERR You are not in a valid lobby.");
                        break;
                    }
                }

                case DEOS:
                    if (!isMyTurn(protocolWriterServer)) {
                        break;
                    }
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR No FieldId from Client " + userId);
                        break;
                    } else {
                        String fieldId = parts[1].trim();
                        Server.deselectField(userId, fieldId);
                        break;
                    }

                case VELO: {
                    //checks if user is in a game lobby
                    String sender = UserList.getUserName(userId);
                    Lobby userLobby = null;
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getPlayers().contains(sender)) {
                            userLobby = lobby;
                            break;
                        }
                    }
                    if (userLobby == null || userLobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                        protocolWriterServer.sendInfo(
                                "You are not currently in a lobby or still in the Welcome lobby and therefore can't choose a bike.");
                        break;
                    }

                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        protocolWriterServer.sendInfo(
                                "-ERR No color provided. Please select: black, green, magenta, darkblue.");
                        break;
                    }

                    String color = parts[1].trim().toLowerCase();
                    List<String> validColors = Arrays.asList("black", "green", "magenta", "darkblue");

                    if (!validColors.contains(color)) {
                        protocolWriterServer.sendInfo("-ERR The color " + color
                                + " is not selectable. Please choose one of: black, green, magenta, darkblue.");
                        break;
                    }

                    if (userLobby.getGameState() != 1) {
                        protocolWriterServer.sendInfo("-ERR The game already started or is already finished.");
                        break;
                    }

                    User user = UserList.getUser(userId);
                    if (user != null) {
                        user.setBikeColor(color); // save in User
                        // protocolWriterServer.sendInfo("+OK " + color + " bike is selected");
                        Lobby lobby = Server.getLobbyOfPlayer(sender);
                        assert lobby != null;
                        for (String member : lobby.getPlayers()) {
                            User u = UserList.getUserByName(member);
                            ProtocolWriterServer w = new ProtocolWriterServer(clientWriters,
                                    Objects.requireNonNull(u).getOut());
                            w.sendCommandAndString(Command.VELO, sender + " " + color);
                        }
                        Server.updateAllClients();
                    }
                    break;
                }

                case BROD:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("-ERR Broadcast message missing " + userId);
                        break;
                    }
                    String msg = parts[1].trim();
                    String broadcasterName = UserList.getUserName(userId);
                    Server.broadcastToAll(broadcasterName + " broadcasted: " + msg);
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
                        protocolWriterServer.sendInfo(
                                "You aren't part of a gameLobby. Please create a lobby or join an existing lobby to start a game.");
                        break;
                    }

                    // Prüfen ob ALLE Lobbys "Welcome" heißen
                    boolean onlyInWelcome = playerLobbies.stream()
                            .allMatch(l -> l.getLobbyName().equalsIgnoreCase("Welcome"));

                    if (onlyInWelcome) {
                        protocolWriterServer.sendInfo(
                                "You aren't part of a gameLobby. Please create a lobby or join an existing lobby to start a game.");
                        break;
                    }

                    // Jetzt starte das Spiel in der ersten echten Lobby (≠ Welcome)
                    if (allPlayersReady()) {
                        for (Lobby lobby : playerLobbies) {
                            if (!lobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                                lobby.startGame(userId);
                                Server.updateAllClients();
                                break;
                            }
                        }
                    }
                    if (!allPlayersReady()) {
                        protocolWriterServer.sendInfo("Not all players are ready to play.");
                    }

                    break;
                }

                case RSTT: {
                    String userName = UserList.getUserName(userId);
                    if (userName == null || userName.isBlank()) {
                        System.err.println("-ERR No user for ID " + userId);
                        break;
                    }

                    List<Lobby> playerLobbies = new ArrayList<>();
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getPlayers().contains(userName)) {
                            playerLobbies.add(lobby);
                            break;
                        }
                    }

                    if (playerLobbies.isEmpty()) {
                        protocolWriterServer.sendInfo("You aren't part of a gameLobby.");
                        break;
                    }

                    // Prüfen ob ALLE Lobbys "Welcome" heißen
                    boolean onlyInWelcome = playerLobbies.stream()
                            .allMatch(l -> l.getLobbyName().equalsIgnoreCase("Welcome"));

                    if (onlyInWelcome) {
                        protocolWriterServer.sendInfo("You aren't part of a gameLobby.");
                        break;
                    }

                    for (Lobby lobby : playerLobbies) {
                        if (!lobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                            lobby.restartGame(userId);
                            Server.updateAllClients();
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

                    // Show players in each lobby (excluding Welcome)
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                            break;
                        }
                    }
                    break;
                }
                case LOME: {
                    User user = UserList.getUser(userId);
                    if (user == null) {
                        System.err.println("-ERR No user for ID " + userId);
                        break;
                    }

                    ProtocolWriterServer writer = new ProtocolWriterServer(Server.clientWriters,
                            user.getOut());
                    String username = user.getNickname();

                    // Find lobby the user is in
                    Lobby userLobby = null;
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getPlayers().contains(username)) {
                            userLobby = lobby;
                            break;
                        }
                    }

                    if (userLobby == null || userLobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                        try {
                            writer.sendInfo("You are not currently in a real lobby.");
                        } catch (IOException e) {
                            System.err.println("Error sending no-lobby message to user " + userId);
                        }
                        break;
                    }
                    break;
                }
                case GLST: {
                    User user = UserList.getUser(userId);
                    if (user == null) {
                        System.err.println("-ERR No user found for ID " + userId);
                        break;
                    }
                    // Zähle "echte" Lobbys (≠ Welcome)
                    List<Lobby> realLobbies = Server.lobbies.stream()
                            .filter(l -> !l.getLobbyName().equalsIgnoreCase("Welcome"))
                            .toList();

                    if (realLobbies.isEmpty()) {
                        break;
                    }

                    for (Lobby lobby : realLobbies) {
                        List<String> players = lobby.getPlayers();
                        int state = lobby.getGameState();
                        String stateText = switch (state) {
                            case 1 -> "open";
                            case 2 -> "running";
                            case 3 -> "finished";
                            default -> "unknown";
                        };
                    }

                    break;
                }
                case RADY: {
                    //checks if user is in a game lobby
                    String userName = UserList.getUserName(userId);
                    Lobby userLobby = null;
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getPlayers().contains(userName)) {
                            userLobby = lobby;
                            break;
                        }
                    }
                    if (userLobby == null || userLobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                        protocolWriterServer.sendInfo(
                                "You are not currently in a lobby or in the Welcome lobby and therefore can't write ready.");
                        break;
                    }

                    if (userLobby.getGameState() != 1) {
                        protocolWriterServer.sendInfo("The game already started or is finished.");
                        break;
                    }

                    userLobby.makeReady(userName);
                    System.out.println(userName + " is ready!");
                    String info = userName + " is ready.";
                    Server.broadcastInLobby(info, userName);
                    break;
                }
                case FNSH: {
                    String username = UserList.getUserName(userId);
                    Lobby userLobby = null;
                    for (Lobby lobby : Server.lobbies) {
                        if (lobby.getPlayers().contains(username)) {
                            userLobby = lobby;
                            break;
                        }
                    }
                    if (userLobby == null || userLobby.getLobbyName().equalsIgnoreCase("Welcome")) {
                        protocolWriterServer.sendInfo("You are not currently in a lobby or in the Welcome lobby");
                        break;
                    }
                    if (username.trim().equalsIgnoreCase(userLobby.getHostName().trim()) || !userLobby.winners.isEmpty()) {
                        userLobby.changeGameState(3);
                        for (String player : userLobby.getPlayers()) {
                            User u = UserList.getUserByName(player);
                            if (u != null) {
                                ProtocolWriterServer writer = Server.getOrCreateWriter(u);
                                try {
                                    writer.sendInfo("The game has stopped.");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        Server.updateAllClients();
                        break;
                    }
                    protocolWriterServer.sendInfo("You are not the host of the lobby and cannot end the game.");
                    break;
                }
                case HIGH:
                    server.getHighscoreList(userId);
                    break;
                default:
                    System.out.println("Unknown command from user ID " + userId + ": " + line);
                    break;
            }
        }
    }

    /**
     * This method checks if all players in the game are ready. If it returns true the host is able to
     * start the game.
     *
     * @return True if all players are ready / false if not all players are ready.
     */
    private boolean allPlayersReady() {
        String username = UserList.getUserName(userId);
        Lobby userLobby = null;
        for (Lobby lobby : Server.lobbies) {
            if (lobby.getPlayers().contains(username)) {
                userLobby = lobby;
                break;
            }
        }
        if (userLobby != null) {
            if (!userLobby.readyStatus.isEmpty() && userLobby.readyStatus.values().stream().allMatch(Boolean::booleanValue)) {
                return true;
            }
        }
        return false;
    }
}
    


