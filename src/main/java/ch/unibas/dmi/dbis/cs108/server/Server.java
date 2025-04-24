package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.Dice;
import ch.unibas.dmi.dbis.cs108.game.Field;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.game.GameBoard;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;

/**
 * The class {@code Server} provides a simple multi-user chat server. It manages client connections,
 * processes messages and takes care of user administration. The server works on a given port and
 * accepts incoming client connections.
 *
 * @author Jana
 */
public class Server {

  private static final AtomicInteger activeClients = new AtomicInteger(0);
  private static ServerSocket echod;
  public static final List<PrintWriter> clientWriters = Collections.synchronizedList(
      new ArrayList<>());
  public static String[] colors;
  public static int port;
  public static List<Lobby> lobbies = new ArrayList<Lobby>();
  private static int podestPlace = 1;
  public static Map<OutputStream, ProtocolWriterServer> protocolWriters = new HashMap<>();

  /**
   * Constructor of the server class
   *
   * @param port the port-number on which the Server is
   */
  public Server(int port) {
    this.port = port;
  }

  /**
   * Starts a server that waits for connections and establishes a network connection from client to
   * server. ServerSocket creates a server (here: echod) that runs on port 8090. echod.accept();
   * waits until client connects. As soon as a client has connected, ‘Connection established’ is
   * printed. When client connection is closed, it exits while loop, then connection to client is
   * closed (socket.close) then server is closed (echod.close).
   */

  public void start() {
    try {
      out.println("Waiting for port " + port + "...");
      echod = new ServerSocket(port);
      createLobby("Welcome", null);

      while (true) {
        Socket clientSocket = echod.accept();
        activeClients.incrementAndGet();
        int clientNumber = activeClients.get();
        int userId = addNewUser("Client" + activeClients.get(), clientSocket.getOutputStream());
        out.println("Connection established for Client: " + activeClients.get());

        ClientHandler cH = new ClientHandler(clientNumber, clientSocket, userId);
        Thread cHT = new Thread(cH);
        cHT.start();

        PrintWriter clientWriter = new PrintWriter(
            new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
        clientWriters.add(clientWriter);

      }

    } catch (IOException e) {
      System.err.println(e.toString());
      System.exit(1);
    }
  }

  /**
   * Adds a new user to the user list.
   *
   * @param userName is the name of the new user.
   * @return the unique user-ID.
   */
  public static int addNewUser(String userName, OutputStream ClientOut) {

    return UserList.addUser(userName, ClientOut);
  }

  /**
   * Informs the server that a client has left. The active clients are reduced by 1 and the number
   * of clients still active is displayed. If there are no more active clients, a thread is started
   * that waits 60 seconds for new clients. After 60 seconds, the server is shut down.
   */
  public static void ClientDisconnected() {
    activeClients.decrementAndGet();
    out.println("Remaining Clients: " + activeClients.get());

    if (activeClients.get() == 0) {
      out.println("Wait 2 min for new clients...");
      new Thread(() -> {
        try {
          Thread.sleep(120000);
          if (activeClients.get() == 0) {
            out.println("No new clients. Server is shutting down.");
            shutdownServer();
          }
        } catch (InterruptedException ignored) {
        }
      }).start();
    }
  }

  /**
   * Shuts down the server.
   */

  public static void shutdownServer() {
    try {
      echod.close();
      System.exit(0);
    } catch (IOException e) {
      System.err.println("Error when closing the Server: " + e.getMessage());
    }
  }

  /**
   * Changes a user's nickname. Checks whether the nickname already exists using UserList and adds a
   * 1 if so. Then calls sendCommand from {@link ProtocolWriterServer}, which sends the message to
   * the client that the nickname has been changed.
   *
   * @param userId  The ID of the user who wants to change the nickname.
   * @param newNick The new desired nickname.
   */

  public static void changeNickname(int userId, String newNick) {
    // Validating of the new nickname (3–50 symbols; only letters, numbers and underscores)
    User user = UserList.getUser(userId);
    if (user == null) {
      return;
    }
    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters,
        user.getOut());

    if (!newNick.matches("^[a-zA-Z0-9_äöüÄÖÜß]{1,50}$")) {
      try {
        protocolWriterServer.sendInfo(
            "Invalid nickname! Must be 3–15 characters, using only letters, numbers, or underscores.");
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
   * This method sends a message from one user to another user. With the {@link UserList} class and
   * the {@link User} class, the method gets the ID and the OutputStream of the user that should
   * receive the message.
   *
   * @param message  The message that should be sent.
   * @param sender   The user that sent the message.
   * @param receiver The user that should receive the message.
   */
  public static void chatToOne(String message, String sender, String receiver) {
    int receiverId = UserList.getUserId(receiver);
    User receiverObject = UserList.getUser(receiverId);
    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters,
        receiverObject.getOut());
    protocolWriterServer.sendWhisper(message, sender, receiver);
  }
//TODO write Test
  public static void rollTheDice(int userId) {
    User user = UserList.getUser(userId);
    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters,
        user.getOut());
    Lobby userlobby = getLobbyOfPlayer(user.getNickname());
    if (userlobby.getLobbyName().equalsIgnoreCase("Welcome")) {
      try {
        protocolWriterServer.sendInfo("You are not in a game. You are in the Welcomelobby.");
      } catch (IOException e) {
        System.err.println("Error sending Info that player is not in a correct lobby");
      }
      return;
    }
    if (!(userlobby.getGameState() == 2)) {
      try {
        protocolWriterServer.sendInfo("The game has not started yet or is already finished.");
      } catch (IOException e) {
        System.err.println("Error sending info");
      }
      return;
    }
    if (!userlobby.isCurrentPlayer(user.getNickname())) {
      try {
        protocolWriterServer.sendInfo("It's not your turn!");
      } catch (IOException e) {
        System.err.println("Error sending Info that it's not this players turn.");
      }
      return;
    }

    if (user.hasRolled()) {
      try {
        protocolWriterServer.sendInfo("You already rolled.");
      } catch (IOException e) {
        System.err.println("Error while sending Rolled Message to user " + userId);
      }
      return;
    }
    Dice dice = new Dice();
    colors = dice.roll();
    user.setHasRolled(true);
    //String[] colors to String
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
   * @param userId  The id of the user
   * @param fieldId the id of the chosen field
   */
  //TODO write test
  public static void checkField(Integer userId, String fieldId) {
    User user = UserList.getUser(userId);
    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, user.getOut());
    if (!user.hasRolled()) {
      try {
        protocolWriterServer.sendInfo("You need to roll first.");
      } catch (IOException e) {
        System.err.println("Error while sending Rolled Message to user " + userId);
      }
      return;
    }
    String nickname = user.getNickname();
    Lobby userLobby = null;
    for (Lobby lobby : lobbies) {
      if (lobby.getPlayers().contains(nickname)) {
        userLobby = lobby;
        break;
      }
    }
    if (userLobby == null) {
      return;
    }
    GameBoard gameBoard = userLobby.getGameBoard(nickname);
    if (gameBoard.isValidField(fieldId)) {
      Field selectedField = gameBoard.getFieldById(fieldId);
      gameBoard.addSelectedField(selectedField);
      try {
        protocolWriterServer.sendCommandAndString(Command.CHOS, fieldId);
      } catch (IOException e) {
        System.err.println("Error sending " + Command.CHOS + fieldId);
      }
    } else {
      try {
        protocolWriterServer.sendCommandAndString(Command.INFO,
            "Field is invalid. Choose a Field that touches the current field or an already selected Field and that matches a rolled color.");
      } catch (IOException e) {
        System.err.println("Error sending error message.");
      }
    }
  }
//TODO wrie test
  public static void deselectField(Integer userId, String fieldId) {
    User user = UserList.getUser(userId);
    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters,
        user.getOut());

    String nickname = user.getNickname();
    Lobby userLobby = null;
    for (Lobby lobby : lobbies) {
      if (lobby.getPlayers().contains(nickname)) {
        userLobby = lobby;
        break;
      }
    }
    if (userLobby == null) {
      return;
    }
    GameBoard gameBoard = userLobby.getGameBoard(nickname);
    Field deselectedField = gameBoard.getFieldById(fieldId);
    //see if selectedFields is empty
    if (gameBoard.selectedFieldsEmpty()) {
      try {
        protocolWriterServer.sendInfo("You need to select a field first.");
      } catch (IOException e) {
        System.err.println("Error sending Info message for empty selectedFields.");
      }
      return;
    }
    //see if deselectedField is in selectedFields
    if (!gameBoard.inSelectedField(deselectedField)) {
      try {
        protocolWriterServer.sendInfo("You have not chosen this field.");
      } catch (IOException e) {
        System.err.println("Error sending Info message for deselectedField not in selectedFields");
      }
      return;
    }
    //remove deselectedField from selectedFields and every Field that comes after
    String newColors = gameBoard.deselectFields(deselectedField);
    try {
      protocolWriterServer.sendCommandAndString(Command.DEOS,
          fieldId + Command.SEPARATOR + newColors);
    } catch (IOException e) {
      System.err.println("Error sending DEOS + fieldId");
    }
  }

  /**
   * moves the player to the last field they selected using CHOS This method finds the lobby the
   * user belongs to and gets the shared GameBoard of that lobby. Executes moveToLastSelected() in
   * the GameBoard sends a message to the client with the final position
   *
   * @param userId userId the ID of the player executing the move
   */
  //TODO write Test
  public static void moveToLastSelectedField(int userId) throws IOException {
    User user = UserList.getUser(userId);
    ProtocolWriterServer writer1 = new ProtocolWriterServer(clientWriters, user.getOut());
    if (user == null) {
      return;
    }

    String nickname = user.getNickname();
    Lobby userLobby = null;

    for (Lobby lobby : lobbies) {
      if (lobby.getPlayers().contains(nickname)) {
        userLobby = lobby;
        break;
      }
    }

    if (userLobby == null) {
      return;
    }

    GameBoard board = userLobby.getGameBoard(nickname);
    if (board.selectedFieldsEmpty()) {
      try {
        writer1.sendInfo(
            "You need to select at least one field first. If you dont want or cant move use 'next'.");
      } catch (IOException e) {
        System.err.println("Error sending Info that no field is selected.");
      }
      return;
    }
    board.moveToLastSelected();
    Field newField = board.getCurrentField();
    for (String playerName : userLobby.getPlayers()) {
      User otherUser = UserList.getUserByName(playerName);
      if (otherUser == null) {
        continue;
      }

      ProtocolWriterServer writer = new ProtocolWriterServer(clientWriters, otherUser.getOut());
      try {
        writer.sendCommandAndString(Command.INFO,
            "+POS " + nickname + " moved to the Field " + newField.getFieldId());
      } catch (IOException e) {
        System.err.println("Error sending move info to " + playerName);
      }
    }

    if (newField.getFieldId().equals("purple2") || newField.getFieldId().equals("pink10")) {
      won(userId);
    }

    userLobby.advanceTurn(); // next players turn
  }

  /**
   * Broadcasts a message to all connected clients.
   *
   * <p>This method constructs a broadcast message by prepending the {@code BROD} command name
   * and a separator to the given message. It then iterates over all client writers and sends the
   * formatted message to each client.
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
    Lobby lobby = new Lobby(lobbyName);
    lobbies.add(lobby);
    if (userId != null) {
      User user = UserList.getUser(userId);
      ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters,
          user.getOut());
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
   * before joining the new lobby. If the lobby is full, the user will be notified. If the lobby
   * does not exist, an informational message will be sent to the user.
   *
   * @param lobbyName The name of the lobby to join.
   * @param userId    The ID of the user requesting to join the lobby.
   */
  public static void joinLobby(String lobbyName, int userId) {
    User user = UserList.getUser(userId);
    if (user == null) {
      return;
    }

    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters,
        user.getOut());

    boolean lobbyFound = false;

    for (Lobby lobby : lobbies) {
      if (lobby.getLobbyName().equals(lobbyName)) {
        lobbyFound = true;

        String userName = user.getNickname();
        for (Lobby otherLobby : lobbies) {
          if (otherLobby.getPlayers().contains(userName)) {
            otherLobby.removePlayer(userName);
            System.out.println(
                "User '" + userName + "' removed from lobby: " + otherLobby.getLobbyName());
          }
        }

        if (!lobby.isFull()) {
          boolean success = lobby.addPlayers(userId);
          if (success) {
            try {
              protocolWriterServer.sendCommandAndString(Command.JOIN, lobbyName);
              for (String member : lobby.getPlayers()) {
                User u = UserList.getUserByName(member);
                assert u != null;
                String color = u.getBikeColor();    // you stored this in VELO handler
                if (color != null) {
                  protocolWriterServer.sendCommandAndString(Command.VELO,
                      member + " " + color); // replays every members bike color
                }
              }
              if (!lobbyName.equalsIgnoreCase("Welcome")) {
                protocolWriterServer.sendInfo(
                    "Please select a bike using: selectbike <black/magenta/green/darkblue> and then enter ready");
              }
            } catch (IOException e) {
              System.err.println("Error sending JOIN or INFO to user " + userId);
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
   * Prints the current game state of all active lobbies to the server console. This method skips
   * the special "Welcome" lobby, as it is a placeholder and not used for actual gameplay. For each
   * other lobby, it prints:
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
        continue; // skip "Welcome"-Lobby
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

  /**
   * This method gets called as soon as a player is at the finish line.
   *
   * @param userId The ID of the user that is currently on its turn.
   */
  //TODO write Test
  public static void won(int userId) {
    User user = UserList.getUser(userId);
    String nickname = user.getNickname();
    Lobby userlobby = getLobbyOfPlayer(user.getNickname());
    for (String players : userlobby.getPlayers()) {
      User users = UserList.getUserByName(players);
      if (users != null) {
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(Server.clientWriters, users.getOut());
        try {
          if (userlobby.getPodestPlace() == 1) {
            protocolWriterServer.sendInfo(nickname + " won the game!");
          } else {
            protocolWriterServer.sendInfo(
                    nickname + " is on the " + userlobby.getPodestPlace() + ". place!");
          }
        } catch (IOException e) {
          System.err.println("Could not send Info.");
        }

        userlobby.incrementPodestPlace(); //podestPlace gets incremented by 1.

        userlobby.addWinner(nickname);

        if (userlobby.getPlayers().size() - userlobby.winners.size() == 1) {
          for (String player : userlobby.getPlayers()) {
            if (!userlobby.winners.contains(player)) {
              userlobby.addWinner(player);
              try {
                protocolWriterServer.sendInfo(
                        player + " is on the " + userlobby.getPodestPlace() + ". place!");
              } catch (IOException e) {
                System.err.println("Could not send Info.");
              }
              break;
            }
          }
          try {
            protocolWriterServer.sendCommand(Command.FNSH);
          } catch (IOException e) {
            System.err.println("Could not send Command.");
          }
      }
    }


      Highscore highscore = new Highscore();
      highscore.addHighscoreEntry(userlobby.getLobbyName(), new ArrayList<>(userlobby.winners));

      userlobby.resetPodestPlae();
    }

  }

  /**
   * Sends updated player and lobby information to all connected clients. This method constructs and
   * broadcasts three types of updates: A player list update (command: {@code LIST}) containing all
   * currently connected usernames. A game lobby list update (command: {@code GLST}) containing each
   * non-Welcome lobby's name, game state, and players. Lobby-specific member lists (command:
   * {@code LOME}) that show members in each lobby, sent to all clients (could be refined per
   * lobby). These messages are formatted as simple protocol strings and dispatched using
   * {@link #broadcast(String)}.
   */
  public static void updateAllClients() {
    List<String> allPlayers = UserList.getAllUsernames();
    String playerListMessage = Command.LIST + Command.SEPARATOR + allPlayers.toString();

    // builds the game lobby list update message
    List<Lobby> realLobbies = Server.lobbies.stream()
        .filter(l -> !l.getLobbyName().equalsIgnoreCase("Welcome"))
        .toList();
    List<String> lobbyInfo = new ArrayList<>();
    List<String> lobbyMembers = new ArrayList<>();
    for (Lobby lobby : realLobbies) {
      List<String> players = lobby.getPlayers();
      String memberlist = String.join(" | ", players);
      String stateText = switch (lobby.getGameState()) {
        case 1 -> "open";
        case 2 -> "running";
        case 3 -> "finished";
        default -> "unknown";
      };
      lobbyInfo.add(
          "[Lobby: " + lobby.getLobbyName() + "] " + stateText); // + " | Players: " + players);
      lobbyMembers.add("[Lobby: " + lobby.getLobbyName() + "] " + "Players: " + memberlist);
    }
    String gameListMessage = "GLST" + Command.SEPARATOR + lobbyInfo.toString();
    String lobbyMemberMessage = "LOME" + Command.SEPARATOR + lobbyMembers.toString();
    broadcast(playerListMessage);
    broadcast(gameListMessage);
    broadcast(lobbyMemberMessage);
  }

  /**
   * Sends a single text message to all connected clients. This method writes the given message
   * string to each {@link PrintWriter} in the list of client connections, followed by a flush to
   * ensure delivery.
   *
   * @param message The text message to send to all clients
   */
  public static void broadcast(String message) {
    for (PrintWriter writer : clientWriters) {
      writer.println(message);
      writer.flush();
    }
  }

  /**
   * Sends a single informational message to all clients in the same lobby. This method writes the
   * given message string to each {@link PrintWriter} in the list of the lobby, followed by a flush
   * to ensure delivery.
   *
   * @param message  The text message to send to all clients
   * @param nickname The nickname that belongs in the lobby and who is the trigger of the message.
   */
  public static void broadcastInLobby(String message, String nickname) {
    Lobby userLobby = null;
    for (Lobby lobby : lobbies) {
      if (lobby.getPlayers().contains(nickname)) {
        userLobby = lobby;
        break;
      }
    }
    for (String player : userLobby.players) {
      User u = UserList.getUserByName(player);
      if (u != null) {
        ProtocolWriterServer writer = new ProtocolWriterServer(clientWriters, u.getOut());
        try {
          writer.sendInfo(message);
        } catch (IOException e) {
          System.err.println("Error sending turn info to " + player);
        }
      }
    }
  }

  public static int getActiveClientCount() {
    return activeClients.get();
  }

  public static void shutdownServerA() {
    try {
      if (echod != null && !echod.isClosed()) {
        echod.close();
      }
      System.exit(0);
    } catch (IOException e) {
      System.err.println("Error when closing the Server: " + e.getMessage());
    }
  }

  /**
   * Reads the data in the Highscore.txt-file and sends it to the Client via the
   * ProtocolWirterServer
   *
   * @param userId The Id from the user that sent the request for the HighscoreList.
   */
  public void getHighscoreList(int userId) {
    User user = UserList.getUser(userId);
    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(clientWriters, user.getOut());

    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Highscore.txt")){
      if (inputStream != null) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder highscoreList = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          highscoreList.append(line).append("|");
        }
        reader.close();
        protocolWriterServer.sendData(highscoreList.toString());
      } else {
        protocolWriterServer.sendInfo("Highscore file not found.");
      }
    } catch (IOException e) {
      System.err.println("Error sending Highscore file.");
    }
  }

}



