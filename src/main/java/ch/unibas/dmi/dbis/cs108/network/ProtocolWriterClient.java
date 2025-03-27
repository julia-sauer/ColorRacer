package ch.unibas.dmi.dbis.cs108.network;

import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * The class {@code ProtocolWriterClient} converts the client's input into the corresponding
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
     * The {@code PrintWriter} for sending messages via the network connection.
     * This Writer writes protocol commands (e.g. {@code CHAT}) in UTF-8 to the server.
     */
    private final PrintWriter writer; //Der Writer ist "final", weil er nach der Initialisierung nicht mehr verändert wird.


    /**
     * Constructor: Initialises the {@code PrintWriter} with UFT-8.
     * @param outputStream the OutputStream to which the messages are to be sent.
     * @throws IOException if an error occurs when creating the PrintWriter.
     */
    public ProtocolWriterClient(OutputStream outputStream) throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
    }


    /**
     * The method{@code sendChat} is used for the chat.
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
        if (message.length() > 500){
            System.out.println("Message is too long");
            return;
        }
        writer.println(Command.CHAT + Command.SEPARATOR + message); // Sends a message in format "CHAT <message>" to the server
        writer.flush(); // Secures that the message is sent immediately
    }

    /**
     * Sends the command on the desired OutputStream.
     *
     * @param out the OutputStream for sending a command
     * @param command the command that should be sent
     * @throws IOException if the message could not be delivered.
     * @author Julia
     */
    public static void sendCommand(OutputStream out, String command) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(command + Command.SEPARATOR);
        System.out.println(command + Command.SEPARATOR + "sent");
    }

    /**
     * Uses the method {@code sendCommand} for the command NICK to send the new nickname to the server.
     * @param newnickname the new nickname that the user selected.
     * @param out the OutputStream with which it is sent.
     */
    public void changeNickname(String newnickname, OutputStream out) {
        try {
            sendCommand(out, Command.NICK + Command.SEPARATOR + newnickname);
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
            sendCommand(out, Command.QUIT + Command.SEPARATOR);
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
        if (!nickname.matches("^[a-zA-Z0-9_]{3,50}$")) {
            System.out.println("Nickname must be 3–15 characters, only letters, digits, or _");
            return;
        }
        // Sends: JOIN <nickname>
        writer.println(Command.JOIN + Command.SEPARATOR + nickname);
        writer.flush();
    }

}


