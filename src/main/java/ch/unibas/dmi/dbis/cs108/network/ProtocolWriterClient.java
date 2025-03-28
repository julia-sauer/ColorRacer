package ch.unibas.dmi.dbis.cs108.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * The class ProtocolWriterClient converts the client's input into the corresponding
 * protocol commands defined in the Commands and, if necessary, forwards them to the server.
 *
 * <p>Users input and the corresponding protocol commands:
 *  * <ul>
 *  *     <li>{@code connect} → {@code JOIN}</li>
 *  *     <li>{@code leave} → {@code QUIT}</li>
 *  *     <li>{@code message} → {@code CHAT}</li>
 *  *     <li>{@code nicknamechange} → {@code NICK}</li>
 *  * </ul>
 * <p>
 * This class uses the UFT-8-encoding to secure a cross-platform communication.
 */
public class ProtocolWriterClient {
    /**
     * The {@link PrintWriter} for sending messages via the network connection.
     * This Writer writes protocol commands (e.g. {@code CHAT}) in UTF-8 to the server.
     */
    private final PrintWriter writer; //Der Writer ist "final", weil er nach der Initialisierung nicht mehr verändert wird.

    private static final Logger LOGGER = LogManager.getLogger(ProtocolWriterClient.class);

    /**
     * Constructor: Initialises the {@link PrintWriter} with UFT-8.
     * @param outputStream the OutputStream to which the messages are to be sent.
     * @throws IOException if an error occurs when creating the PrintWriter.
     */
    public ProtocolWriterClient(OutputStream outputStream) throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
    }


    /**
     * The method {@code sendChat} is used for the chat.
     * It converts a chat message entered by the user(e.g. {@code message Hallo!}) into a valid
     * protocol command of the format {@code CHAT <message>} and sends it to the server.
     * <p>
     * @param message The message entered by the user.
     * @author anasv
     * @since 22.03.25
     */
    public void sendChat(String message) {
        if (message == null || message.trim().isEmpty()) {
            System.out.println("Message is null or empty!");
            return;
        }
        if (message.matches("^[a-zA-Z0-9_äöüÄÖÜß\\p{So}]{3,50}$")){
            System.out.println("Message contains illegal characters!");
            return;
        }
        if (message.length() > 500){
            System.out.println("Message is too long");
            return;
        }
        sendToServer(Command.CHAT + Command.SEPARATOR + message);
    }

    /**
     * Sends the command on the desired OutputStream.
     *
     * @param command the command that should be sent
     * @throws IOException if the message could not be delivered.
     * @author Julia
     */
    public void sendCommand(Command command) throws IOException {
        sendToServer(command + Command.SEPARATOR);
        System.out.println(command + Command.SEPARATOR + "sent");
    }

    /**
     * Sends a command with an aditional String.
     * @param command
     * @param text
     * @throws IOException
     */
    public void sendCommandAndString(Command command, String text) throws IOException {
        sendToServer(command + Command.SEPARATOR + text);
        //System.out.println(command + Command.SEPARATOR + text + " sent");
    }

    /**
     * Calls sendCommand for the Command NICK, to send the new nickname to the server.
     * @param newnickname the new nickname, the User choosed.
     * @param out the OutputStream.
     * Uses the method {@code sendCommand} for the command NICK to send the new nickname to the server.
     * @param newnickname the new nickname that the user selected.
     * @param out the OutputStream with which it is sent.
     */
    public void changeNickname(String newnickname, OutputStream out) {
        try {
            sendCommandAndString(Command.NICK, newnickname);
        } catch (IOException e) {
            System.err.println("Error, could not send NICK " + newnickname + " to Server");
        }
    }

    /**
     * Sends a {@code QUIT} command to the server to terminate the connection.
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
     * Sends the {@code JOIN} command with the desired nickname to the server.
     * The server decides whether the nickname is accepted or changed (e.g. in the case of duplicates).
     *
     * @param nickname The nickname entered by the user
     * @author anasv
     */
    public void sendJoin(String nickname){
        if (nickname == null || nickname.trim().isEmpty()) {
            System.out.println("Nickname is null or empty!");
            return;
        }
        if (nickname.length() > 15){
            System.out.println("Nickname is too long");
            return;
        }
        if (!nickname.matches("^[a-zA-Z0-9_äöüÄÖÜß]{3,50}$")) {
            System.out.println("Nickname must be 3–50 characters, only letters, digits, or _");
            return;
        }
        // Sends: JOIN <nickname>
        sendToServer(Command.JOIN + Command.SEPARATOR + nickname);
    }


    public void sendToServer(String message) {
        //LOGGER.error("client: {}", message);
        writer.println(message);
        writer.flush();
        //System.exit(1);
    }

}


