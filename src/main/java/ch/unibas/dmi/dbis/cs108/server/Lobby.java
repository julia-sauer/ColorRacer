package ch.unibas.dmi.dbis.cs108.server;


import ch.unibas.dmi.dbis.cs108.game.Field;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import ch.unibas.dmi.dbis.cs108.game.GameBoard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a game lobby that holds a list of players and manages the game state.
 * <p>
 * Each lobby is identified by a unique name and keeps track of the players in it. The lobby can be
 * run as a separate thread (e.g., to manage game state, timers, etc.).
 *
 * @author milo
 */
public class Lobby implements Runnable {

  /**
   * Maximum number of players allowed in a lobby.
   */
  public static final int MAX_PLAYERS = 4;

  /**
   * List of players' names currently in the lobby.
   */
  public final List<String> players;

  /**
   * The name of the lobby.
   */
  private String lobbyName;

  /**
   * Mapping from player names to their personal game board.
   */
  private final Map<String, GameBoard> playerGameBoards;

  /**
   * The order of players according to their join order.
   */
  public final List<String> playerOrder = new ArrayList<>();

  /**
   * Index of the current player in {@link #playerOrder} whose turn it is.
   */
  int currentPlayerIndex = 0;

//  private final Map<String, String> selectedColors = new HashMap<>();
  /**
   * The name of the host (player who joined the lobby first).
   */
  private String hostName;

  /**
   * A list of winners at the end of the game.
   */
  public final List<String> winners = new ArrayList<>();

  /**
   * The first podium place to assign when a player finishes.
   */
  private int podestPlace = 1;

  /**
   * Readiness status of each player, keyed by player name (true = ready).
   */
  public final Map<String, Boolean> readyStatus = new ConcurrentHashMap<>();

  /**
   * Current state of the game: 1 = open (waiting for players), 2 = running (game in progress), 3 =
   * finished (game ended).
   */
  private int gamestate;

  /**
   * Constructs a new Lobby instance with the given name.
   *
   * @param lobbyName the name of the lobby
   */
  public Lobby(String lobbyName) {
    this.players = new ArrayList<>();
    this.lobbyName = lobbyName;
    this.playerGameBoards = new HashMap<>();
    this.gamestate = 1; // Default state: open
    this.podestPlace = 1;
    this.currentPlayerIndex = -1;
  }

  /**
   * Adds a player to the lobby based on their user ID. The first player added becomes the host. The
   * player is only added if the lobby is not full and the user isn't already in it.
   *
   * @param userId the unique ID of the user to add
   * @return true if the player was added successfully, false otherwise
   */
  public boolean addPlayers(int userId) {
    if (players.size() >= MAX_PLAYERS) {
      System.out.println("Lobby is full: " + players.size() + " / " + MAX_PLAYERS);
      return false;
    }

    String userName = UserList.getUserName(userId);
    if (userName != null && !players.contains(userName)) {
      players.add(userName);
      playerOrder.add(userName);
      playerGameBoards.put(userName, new GameBoard());

      if (hostName == null) {
        hostName = userName;
      }
      makeReadyStatusList();
      return true;
    }

    return false;
  }


  /**
   * This method creates a map which shows all players in the lobby ands states if they are ready to
   * play. At the beginning all players are not ready.
   */
  public void makeReadyStatusList() {
    for (String username : players) {
      if (!readyStatus.containsKey(username)) {
        readyStatus.put(username, false);
      }
    }
  }

  /**
   * Marks a player as ready by setting their status to {@code true} in the {@code readyStatus} map.
   * It checks if the specified username exists in the map before updating their readiness. If the
   * username is not found, no changes are made.
   *
   * @param username the name of the player to mark as ready
   */
  //TODO write test
  public void makeReady(String username) {
    if (readyStatus.containsKey(username)) {
      readyStatus.put(username, true);
    }
  }

  /**
   * Checks whether the lobby is full.
   *
   * @return true if the lobby has reached MAX_PLAYERS, false otherwise
   */
  public boolean isFull() {

    return players.size() >= MAX_PLAYERS;
  }

  /**
   * Changes the state of the game. 1 = open 2 = ongoing 3 = finished
   *
   * @param state the new state to set
   */
  public synchronized void changeGameState(int state) {
    System.out.println(
        "[Lobby: " + lobbyName + "] Game state changed from " + gamestate + " to " + state);
    this.gamestate = state;
  }

  /**
   * Gets the current game state.
   *
   * @return the current game state (1=open, 2=ongoing, 3=finished)
   */
  public synchronized int getGameState() {
    return gamestate;
  }

  /**
   * Gets the name of this lobby.
   *
   * @return the lobby name
   */
  public String getLobbyName() {
    return lobbyName;
  }


  /**
   * Gets a copy of the list of players currently in the lobby.
   *
   * @return list of player usernames
   */
  public List<String> getPlayers() {
    return new ArrayList<>(players);
  }

  /**
   * Removes a player from the lobby by their nickname.
   * <p>
   * This method is thread-safe and ensures that the internal player list is updated safely. It
   * prevents the host from being removed.
   *
   * @param playerName The nickname of the player to remove.
   */
  public synchronized void removePlayer(String playerName) {
    players.remove(playerName);
    playerOrder.remove(playerName); // Player removed from sequence too
//    selectedColors.remove(playerName);
    playerGameBoards.remove(playerName);
    readyStatus.remove(playerName);
    winners.remove(playerName);

    if (playerName.equalsIgnoreCase(hostName)) {
      if (!players.isEmpty()) {
        hostName = players.getFirst();
      } else {
        hostName = null;
      }
    }
  }

  /**
   * Starts the game for this lobby if all conditions are met. Only the host can start a game.
   * <p>
   * Conditions: The game must be started by the host of the lobby (the player that first joined the
   * lobby). The game must not already be running or finished. The lobby must not be the "Welcome"
   * lobby. At least 2 players must be present. If any condition fails, a corresponding message is
   * sent to the requesting client.
   *
   * @param userId the ID of the user who requested to start the game
   */
  //TODO write test
  public synchronized void startGame(int userId) {
    // Verify that the requesting user is the host.
    String requester = UserList.getUserName(userId);
    if (!isHost(requester)) {
      User user = UserList.getUser(userId);
      if (user != null) {
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(Server.clientWriters,
            user.getOut());
        try {
          protocolWriterServer.sendInfo("Only the host can start the game.");
        } catch (IOException e) {
          System.err.println("Error sending host-only message to user " + userId);
        }
      }
      return;
    }

    User user = UserList.getUser(userId);
      if (user == null) {
          return;
      }

    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(Server.clientWriters,
        user.getOut());

    // Game already started or finished
    if (gamestate != 1) {
      try {
        protocolWriterServer.sendInfo("Game has already started or is finished.");
      } catch (IOException e) {
        System.err.println("Error sending game state message to user " + userId);
      }
      return;
    }

    // Welcome-lobby is not a valid game lobby
    if (lobbyName.equalsIgnoreCase("Welcome")) {
      try {
        protocolWriterServer.sendInfo(
            "You are not in a real lobby. Please join or create a lobby to start a game.");
      } catch (IOException e) {
        System.err.println("Error sending Welcome lobby warning to user " + userId);
      }
      return;
    }

    // Not enough players to start
    if (players.size() < 2) {
      try {
        protocolWriterServer.sendInfo("At least 2 players are required to start the game.");
      } catch (IOException e) {
        System.err.println("Error sending player count warning to user " + userId);
      }
      return;
    }

    changeGameState(2);
    System.out.println("[Lobby: " + lobbyName + "] Game is starting...");

    for (String playerName : players) {
      User u = UserList.getUserByName(playerName);
      if (u != null) {
        ProtocolWriterServer playerWriter = new ProtocolWriterServer(Server.clientWriters,
            u.getOut());
        try {
          playerWriter.sendCommand(Command.STRT);
        } catch (IOException e) {
          System.err.println("Error sending STRT to " + playerName);
        }
      }
    }
    currentPlayerIndex = -1;

    advanceTurn();
    new Thread(this).start(); // Start game thread
  }

  /**
   * This method restarts the Game when the host and only the host types 'restart'. If the game
   * state is 1 the game can not be restarted.
   *
   * @param userId The userId of the player who typed restart.
   */
  //TODO write test
  public synchronized void restartGame(int userId) {
    // Verify that the requesting user is the host.
    String requester = UserList.getUserName(userId);
    if (!isHost(requester)) {
      User user = UserList.getUser(userId);
      if (user != null) {
        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(Server.clientWriters,
            user.getOut());
        try {
          protocolWriterServer.sendInfo("Only the host can start the game.");
        } catch (IOException e) {
          System.err.println("Error sending host-only message to user " + userId);
        }
      }
      return;
    }

    User user = UserList.getUser(userId);
      if (user == null) {
          return;
      }

    ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(Server.clientWriters,
        user.getOut());

    if (gamestate == 1) {
      try {
        protocolWriterServer.sendInfo("Game hasn't started yet. You need to start a game first.");
      } catch (IOException e) {
        System.err.println("Error sending game state message to user " + userId);
      }
      return;
    }

    // Not enough players to restart
    if (players.size() < 2) {
      try {
        protocolWriterServer.sendInfo("At least 2 players are required to restart the game.");
      } catch (IOException e) {
        System.err.println("Error sending player count warning to user " + userId);
      }
      return;
    }

    // Reset the currentField for each player
    for (String playerName : players) {
      GameBoard board = getGameBoard(playerName);
      board.setCurrentField(board.getFieldById("white1"));
    }

    for (String player : players) {
      User u = UserList.getUserByName(player);
      if (u != null) {
        u.setHasRolled(false);
        ProtocolWriterServer writer = new ProtocolWriterServer(Server.clientWriters, u.getOut());
        try {
          writer.sendCommand(Command.RSTT);
        } catch (IOException e) {
          System.err.println("Error sending RSTT");
        }
      }
    }

//    changeGameState(3); //ends the current game
    changeGameState(1);
    winners.clear();
    startGame(userId);

  }

  /**
   * Logic to run when this lobby is executed in a separate thread. You can implement countdowns,
   * game loops, or timeouts here.
   */
  @Override
  public void run() {
    System.out.println("[Lobby: " + lobbyName + "] Game loop started.");

    try {
      // Simulate a 5-minute game session for testing purposes
      Thread.sleep(5 * 60 * 1000);
    } catch (InterruptedException e) {
      System.err.println("[Lobby: " + lobbyName + "] Game loop was interrupted.");
    }

    changeGameState(3); // Game finished
    System.out.println("[Lobby: " + lobbyName + "] Game ended.");
  }

  /**
   * Creates a gameboard specific for the player with playerName
   *
   * @param playerName Player name (of the Player for whom this Gameboard is).
   * @return the gameboard
   */
  public GameBoard getGameBoard(String playerName) {
    return playerGameBoards.get(playerName);
  }

  /**
   * Checks whether it is the given player's current turn.
   *
   * @param name Player name
   * @return true if it's his turn
   */
  //TODO write test
  public boolean isCurrentPlayer(String name) {
    if (playerOrder.isEmpty() || currentPlayerIndex < 0 || currentPlayerIndex >= playerOrder.size()) {
      return false;
    }
    return playerOrder.get(currentPlayerIndex).equals(name);
  }


  /**
   * Sets the next player as active (after turn or NEXT). It also sends the information message
   * whose turn it is to all players in the lobby.
   */
  //TODO test
  public void advanceTurn() {
    System.out.println("Advancing turn. Current index before increment: " + currentPlayerIndex);
    int safety = 0;
    String currentPlayer;

    if (currentPlayerIndex < 0) {
      currentPlayerIndex = 0;
    }

    do {
      currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
      currentPlayer = playerOrder.get(currentPlayerIndex);
      safety++;

      // Sicherheitsabbruch: falls alle Spieler gewonnen haben
      if (safety > playerOrder.size()) {
        System.out.println("No eligible players left to take a turn.");
        return; // beende advanceTurn(), kein gültiger Spieler mehr da
      }
    } while (winners.contains(currentPlayer));

    GameBoard board = getGameBoard(currentPlayer);
    Field currentField = board.getCurrentField();

    User currentUser = UserList.getUserByName(currentPlayer);
    if (currentUser != null) {
      currentUser.setHasRolled(false); // resets dice status
    }

    for (String player : players) {
      User u = UserList.getUserByName(player);
      if (u != null) {
        ProtocolWriterServer writer = new ProtocolWriterServer(Server.clientWriters, u.getOut());
        try {
          writer.sendCommandAndString(Command.INFO, "It's " + currentPlayer + "'s turn");
          writer.sendCommandAndString(Command.INFO,
              currentPlayer + " is at: " + currentField.getFieldId());
        } catch (IOException e) {
          System.err.println("Error sending turn info to " + player);
        }
      }
    }
  }

  /**
   * Adds a player who is at the finish line to the set winners.
   *
   * @param nickname The nickname of the player at the finish line.
   */
  public void addWinner(String nickname) {
    winners.add(nickname);
  }


  /**
   * Returns the host's username.
   *
   * @return the host's name
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * Checks whether a given username is the host.
   *
   * @param userName the name of the user to check
   * @return true if the user is the host, false otherwise
   */
  public boolean isHost(String userName) {
    return hostName != null && hostName.equals(userName);
  }

  /**
   * This method updates the list of the players in one lobby when someone changes their nickname.
   *
   * @param oldUsername The username that was previously used.
   * @param newUsername The username that was selected.
   */
  public void updateUsername(String oldUsername, String newUsername) {
    if (players.contains(oldUsername) && !oldUsername.equals(newUsername)) {
      int index = players.indexOf(oldUsername);
      players.set(index, newUsername);
    }
  }

  /**
   * Returns the currently active player (i.e., the one whose turn it is).
   *
   * @return the username of the current player or null if no player is set
   */
  public String getCurrentPlayer() {
    if (playerOrder.isEmpty() || currentPlayerIndex < 0
        || currentPlayerIndex >= playerOrder.size()) {
      return null;
    }
    return playerOrder.get(currentPlayerIndex);
  }

  /**
   * Resets the podium place counter back to 1.
   */
  public void resetPodestPlace() {
    this.podestPlace = 1;
  }

  /**
   * Returns the next podium place to assign.
   *
   * @return the podium place
   */
  public int getPodestPlace() {
    return podestPlace;
  }

  /**
   * Increments the podium place counter.
   */
  public void incrementPodestPlace() {
    this.podestPlace++;
  }

  void setCurrentPlayerIndex(int index) {
    this.currentPlayerIndex = index;
  }

}
