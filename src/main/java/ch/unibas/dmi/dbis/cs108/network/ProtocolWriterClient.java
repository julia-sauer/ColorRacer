package ch.unibas.dmi.dbis.cs108.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * The {@code ProtocolWriterClient} class handles the client's outgoing communication.
 * It formats and sends protocol-specific commands (defined in {@link Command}) to the server.
 * All data is sent in UTF-8 encoding to ensure cross-platform compatibility.
 */
public class ProtocolWriterClient {
    /**
     * The {@link PrintWriter} for sending messages via the network connection.
     * This Writer writes protocol commands (e.g. {@code CHAT}) in UTF-8 to the server.
     */
    private final PrintWriter writer; //Der Writer ist "final", weil er nach der Initialisierung nicht mehr verändert wird.

    /** Logger for logging errors or debugging information. */
    private static final Logger LOGGER = LogManager.getLogger(ProtocolWriterClient.class);

    /**
     * Constructs a new {@code ProtocolWriterClient} with UTF-8 encoding.
     *
     * @param outputStream The OutputStream to which the messages are to be sent.
     * @throws IOException If an error occurs when creating the PrintWriter.
     */
    public ProtocolWriterClient(OutputStream outputStream) throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
    }


    /**
     * The method {@code sendChat} is used for the chat.
     * It converts a chat message entered by the user(e.g. {@code message Hallo!}) into a valid
     * protocol command of the format {@code CHAT <message>} and sends it to the server.
     *
     * @param message The message entered by the user.
     */
    public void sendChat(String message) {
        if (message == null || message.trim().isEmpty()) {
            System.out.println("Message is null or empty!");
            return;
        }
        if (!message.matches("[a-zA-ZäöüÄÖÜß0-9.,!?\\s\\p{So}]{1,500}$")){
            System.out.println("Message contains illegal characters!");
            return;
        }
        if (message.length() > 500){
            System.out.println("Message is too long");
            return;
        }
        if (message.startsWith("whisper")){ // checks if the message is private
            sendWhisper(message);
            return;
        }
        if (message.startsWith("broadcast")){ // checks if the message is a broadcast
            String[] parts = message.split(" ", 2);
            String actualMessage = parts[1].trim();
            sendToServer(Command.BROD + Command.SEPARATOR + actualMessage);
            return;
        }
        sendToServer(Command.CHAT + Command.SEPARATOR + message);
    }

    /**
     * Sends a basic command (e.g., {@code QUIT}, {@code PING}) to the server.
     *
     * @param command The command that should be sent
     * @throws IOException If the message could not be delivered.
     */
    public void sendCommand(Command command) throws IOException {
        sendToServer(command + Command.SEPARATOR);
        //System.out.println(command + Command.SEPARATOR + "sent");
    }

    /**
     * Sends a command with an additional string to the server.
     *
     * @param command The command that should be sent to the server.
     * @param text The string that should be sent in addition to the command.
     * @throws IOException If there is an error while sending the command with the message to the server.
     */
    public void sendCommandAndString(Command command, String text) throws IOException {
        sendToServer(command + Command.SEPARATOR + text);
        //System.out.println(command + Command.SEPARATOR + text + " sent");
    }

    /**
     * Sends a {@code NICK} command to the server with the new nickname.
     *
     * @param newnickname The new nickname that the user selected.
     * @param out The OutputStream with which it is sent.
     */
    public void changeNickname(String newnickname, OutputStream out) {
        try {
            sendCommandAndString(Command.NICK, newnickname);
        } catch (IOException e) {
            System.err.println("Error, could not send NICK " + newnickname + " to Server");
        }
    }

    /**
     * Sends a {@code QUIT} command to the server indicating client termination.
     *
     * @param out The OutputStream to which the command is written.
     */
    public void leave(OutputStream out) {
        try {
            sendCommand(Command.QUIT);
        } catch (IOException e) {
            System.err.println("Error, could not send QUIT to Server");
        }
    }

    /**
     * Sends a {@code JOIN} command to the server to enter a specific lobby
     *
     * @param lobbyName The nickname entered by the user
     */
    public void sendJoin(String lobbyName){
        if (lobbyName == null || lobbyName.trim().isEmpty()) {
            System.out.println("LobbyName is null or empty!");
            return;
        }
        if (lobbyName.length() > 30){
            System.out.println(" LobbyName is too long");
            return;
        }
        if (!lobbyName.matches("^[a-zA-Z0-9_äöüÄÖÜß]{1,50}$")) {
            System.out.println("lobbyName must be 3–50 characters, only letters, digits, or _");
            return;
        }
        // Sends: JOIN <nickname>
        sendToServer(Command.JOIN + Command.SEPARATOR + lobbyName);
    }

    /**
     * This method sends a raw string to the server.
     *
     * @param message The message that the server should receive.
     */
    public void sendToServer(String message) {
        //LOGGER.error("client: {}", message);
        writer.println(message);
        writer.flush();
        //System.exit(1);
    }

    /**
     * Sends a private message (whisper) to another user.
     * The message must follow the pattern: {@code whisper <nickname> <message>}.
     *
     * @param nicknameAndMessage The nickname of the user, who needs to receive the message.
     */

    public void sendWhisper(String nicknameAndMessage) {
        String[] parts = nicknameAndMessage.split(" ", 3);
        String receiverNickname = parts[1].trim();
        String message = parts[2].trim();

        if (message == null || message.trim().isEmpty()) {
            System.out.println("Message is null or empty!");
            return;
        }
        if (!message.matches("[a-zA-ZäöüÄÖÜß0-9.,!?\\s\\p{So}]{1,500}$")){
            System.out.println("Message contains illegal characters!");
            return;
        }
        if (message.length() > 500){
            System.out.println("Message is too long");
            return;
        }
        sendToServer(Command.WISP + Command.SEPARATOR + receiverNickname + Command.SEPARATOR + message);
    }
        // Create a whisper message with recipientId and message content
        // Send this message to the server

    /**
     * Sends a field selection command with a specified {@code fieldId}.
     *
     * @param command The protocol command ({@code CHOS} or {@code DEOS}) to be sent to the server.
     * @param fieldId Contains the ID of the chosen field.
     */
    public void sendFieldChoice(Command command, String fieldId) {
        sendToServer(command + Command.SEPARATOR + fieldId);
    }

    /**
     * Sends a {@code VELO} command with the selected color of the bike.
     *
     * @param color The selected color; "black", "green", "magenta", or "darkblue"
     */
    public void sendBikeColor(String color) {
        sendToServer(Command.VELO + Command.SEPARATOR + color);
    }

    /**
     * Marks the player as ready by sending a {@code RADY} command to the server.
     *
     * @throws IOException If there is an error while sending the command to the server.
     */
    public void sendReadyStatus() throws IOException {
        sendCommand(Command.RADY);
    }
}


