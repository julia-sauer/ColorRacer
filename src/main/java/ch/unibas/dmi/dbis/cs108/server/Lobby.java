package ch.unibas.dmi.dbis.cs108.server;


import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import ch.unibas.dmi.dbis.cs108.game.GameBoard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

/**
 * Represents a game lobby that holds a list of players and manages the game state.
 * <p>
 * Each lobby is identified by a unique name and keeps track of the players in it.
 * The lobby can be run as a separate thread (e.g., to manage game state, timers, etc.).
 */
public class Lobby implements Runnable {

    public static final int MAX_PLAYERS = 4;
    private final List<String> players;
    private Server server;
    private final String lobbyName;
    private final Map<String, GameBoard> playerGameBoards;
    private final List<String> playerOrder = new ArrayList<>(); // reihenfolge der Spieler entsprechend Join-Reihenfolge
    private int currentPlayerIndex = 0;
    private final Map<String, String> selectedColors = new HashMap<>();




    /**
     * Current state of the game:
     * 1 = open (waiting for players),
     * 2 = running (game in progress),
     * 3 = finished (game ended).
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
    }

    /**
     * Adds a player to the lobby based on their user ID.
     * The player is only added if the lobby is not full and the user isn't already in it.
     *
     * @param userId the unique ID of the user to add
     * @return true if the player was added successfully, false otherwise
     */
    public boolean addPlayers(int userId) {
        if (isFull()) {
            return false;
        }
        String userName = UserList.getUserName(userId);
        if (userName != null && !players.contains(userName)) {
            players.add(userName);
            playerOrder.add(userName); // Speichert Join-Reihenfolge
            playerGameBoards.put(userName, new GameBoard()); //Erstellt GameBoard für einzelne Spieler
            return true;
        }
        return false;
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
     * Changes the state of the game.
     * 1 = open
     * 2 = ongoing
     * 3 = finished
     *
     * @param state the new state to set
     */
    public synchronized void changeGameState(int state) {
        System.out.println("[Lobby: " + lobbyName + "] Game state changed from " + gamestate + " to " + state);
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
     * This method is thread-safe and ensures that the internal player list is updated safely.
     *
     * @param playerName The nickname of the player to remove.
     */
    public synchronized void removePlayer(String playerName) {
        players.remove(playerName);
        playerOrder.remove(playerName); // Spieler auch aus Reihenfolge entfernen
        selectedColors.remove(playerName);
        playerGameBoards.remove(playerName);
    }
    /**
     * Starts the game for this lobby if all conditions are met.
     * <p>
     * Conditions:
     * The game must not already be running or finished.
     * The lobby must not be the "Welcome" lobby.
     * At least 2 players must be present.
     * If any condition fails, a corresponding message is sent to the requesting client.
     *
     * @param userId the ID of the user who requested to start the game
     */
    public synchronized void startGame(int userId) {
        User user = UserList.getUser(userId);
        if (user == null) return;

        ProtocolWriterServer protocolWriterServer = new ProtocolWriterServer(Server.clientWriters, user.getOut());

        // Game already started or finished
        if (gamestate != 1) {
            try {
                protocolWriterServer.sendInfo("Game has already started or is finished.");
            } catch (IOException e) {
                System.err.println("Error sending game state message to user " + userId);
            }
            return;
        }

        // Welcome lobby is not a valid game lobby
        if (lobbyName.equalsIgnoreCase("Welcome")) {
            try {
                protocolWriterServer.sendInfo("You are not in a real lobby. Please join or create a lobby to start a game.");
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
                ProtocolWriterServer playerWriter = new ProtocolWriterServer(Server.clientWriters, u.getOut());
                try {
                    playerWriter.sendCommand(Command.STRT);
                    if (playerOrder.get(0).equals(playerName)) { // zeigt bei Spielstart an, wer beginnt
                        playerWriter.sendCommandAndString(Command.INFO, "It's " + playerName + "'s turn");
                    }
                } catch (IOException e) {
                    System.err.println("Error sending STRT to " + playerName);
                }
            }
        }

        new Thread(this).start(); // Start game thread
    }

    /**
     * Logic to run when this lobby is executed in a separate thread.
     * You can implement countdowns, game loops, or timeouts here.
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
     * creates a gamboard
     * @return the gameboard
     */
    public GameBoard getGameBoard(String playerName) {
        return playerGameBoards.get(playerName);
    }

    /** Checks whether it is the given player's current turn.
     * @param name Player name
     * @return true if it's his turn
     */
    public boolean isCurrentPlayer(String name) {
        if (playerOrder.isEmpty()) return false;
        return playerOrder.get(currentPlayerIndex).equals(name);
    }

    /**
     * Sets the next player as active (after turn or NEXT).
     * It also sends the information message whose turn it is to all players in the lobby.
     */
    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
        String currentPlayer = playerOrder.get(currentPlayerIndex);
        for (String player : players) {
            User u = UserList.getUserByName(player);
            if (u != null) {
                ProtocolWriterServer writer = new ProtocolWriterServer(Server.clientWriters, u.getOut());
                try {
                    writer.sendCommandAndString(Command.INFO, "It's " + currentPlayer + "'s turn");
                } catch (IOException e) {
                    System.err.println("Error sending turn info to " + player);
                }
            }
        }
    }

}
