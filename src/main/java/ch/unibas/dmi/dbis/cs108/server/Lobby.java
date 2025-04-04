package ch.unibas.dmi.dbis.cs108.server;


import java.util.ArrayList;
import java.util.List;

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
     * <ul>
     *   <li>1 = open</li>
     *   <li>2 = ongoing</li>
     *   <li>3 = finished</li>
     * </ul>
     *
     * @param state the new state to set
     */
    public synchronized void changeGameState(int state) {
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
     * Logic to run when this lobby is executed in a thread.
     * You can implement countdowns, game loops, or timeouts here.
     */
    @Override
    public void run() {
        // Placeholder for game logic or timer functionality
    }
}
