package ch.unibas.dmi.dbis.cs108.server;

import java.io.OutputStream;

/**
 * This class represents a connected user. It stores the user ID, the current nickname, and an
 * output stream used for communication with the client. Used in user management (UserList) to send
 * targeted messages to specific clients or to manage their data.
 *
 * @author Milo
 */


public class User {

    private final int id; // clear user-ID
    private String nickname; // current nickname of users
    private final OutputStream out; // OutputStream for communication with client
    private String bikeColor;
    private boolean hasRolled;
    private int rollCount;

    /**
     * Constructor for creating a new user.
     *
     * @param id       The unique user ID.
     * @param nickname The user's nickname.
     * @param out      The output stream for sending messages to the client.
     */

    public User(int id, String nickname, OutputStream out) {
        this.id = id;
        this.nickname = nickname;
        this.out = out;
        this.hasRolled = false;
        this.rollCount = 0;
    }

    /**
     * Returns the user ID.
     *
     * @return The user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the user's current nickname.
     *
     * @return The nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets a new nickname for the user.
     *
     * @param nickname The new nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the OutputStream used for communication with the client.
     *
     * @return The OutputStream
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * Sets the bike color selected by the player.
     *
     * @param bikeColor the selected bike color (e.g., "green", "black")
     */
    public void setBikeColor(String bikeColor) {
        this.bikeColor = bikeColor;
    }

    /**
     * Returns the selected bike color of the player.
     *
     * @return the selected bike color, or null if none is selected
     */
    public String getBikeColor() {
        return bikeColor;
    }

    /**
     * That method gives back whether hasRolled is true or false. Is ture when User already rolled.
     *
     * @return Returns whether hasRolled is true or false -> returns true or false.
     */
    public boolean hasRolled() {
        return hasRolled;
    }

    /**
     * This method is used to set the variable to true or false
     *
     * @param hasRolled Is either true or false, depending on how to change hasRolled.
     */
    public void setHasRolled(boolean hasRolled) {
        this.hasRolled = hasRolled;
    }

    /**
     * This method increases the rollCount of the user by 1.
     */
    public void setRollCount() {
        rollCount += 1;
    }

    /**
     * This method returns the rollCount of the user.
     * @return How often the user rolled the dice.
     */
    public int getRollCount() {
        return rollCount;
    }

}
