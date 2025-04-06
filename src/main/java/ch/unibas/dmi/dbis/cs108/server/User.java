package ch.unibas.dmi.dbis.cs108.server;

import java.io.OutputStream;

/**
 * This class represents a connected user.
 * It stores the user ID, the current nickname, and an output stream
 * used for communication with the client.
 * Used in user management (UserList) to send targeted messages
 * to specific clients or to manage their data.
 *
 * @author Milo
 */


public class User {
    private final int id; // Eindeutige Benutzer-ID
    private String nickname; // Aktueller Nickname des Benutzers
    private final OutputStream out; // OutputStream zur Kommunikation mit dem Client
    private String bikeColor;

    /**
     * Constructor for creating a new user.
     *
     * @param id The unique user ID.
     * @param nickname The user's nickname.
     * @param out The output stream for sending messages to the client.
     */

    public User(int id, String nickname, OutputStream out) {
        this.id = id;
        this.nickname = nickname;
        this.out = out;
    }

    /**
     * Returns the user ID.
     * @return The user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the user's current nickname.
     * @return The nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets a new nickname for the user.
     * @param nickname The new nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the OutputStream used for communication with the client.
     * @return The OutputStream
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * Sets the bike color selected by the player.
     * @param bikeColor the selected bike color (e.g., "green", "black")
     */
    public void setBikeColor(String bikeColor) {
        this.bikeColor = bikeColor;
    }

    /**
     * Returns the selected bike color of the player.
     * @return the selected bike color, or null if none is selected
     */
    public String getBikeColor() {
        return bikeColor;
    }

}
