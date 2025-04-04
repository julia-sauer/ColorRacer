package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.gui.ChatController;
import javafx.application.Platform;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * The ProtocolReaderClient class processes incoming messages from the server
 * and controls the interaction with the client.
 */
public class ProtocolReaderClient {
    private final BufferedReader reader; // Liest Zeichenzeilen vom Client.
    private final InputStream in;
    private final OutputStream out;
    private ChatController chatController; // Reference to the GUI controller


    /**
     * Creates a new ProtocolReaderClient.
     *
     * @param in The InputStream from which messages are read.
     * @param out The OutputStream to which responses are written.
     * @throws IOException If an error occurs when creating the reader.
     */
    public ProtocolReaderClient(InputStream in, OutputStream out) throws IOException {
        this.in = in;
        this.out = out;
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * The method {@code readLoop} continuously reads lines from the server
     <p>In the case of a {@code CHAT} command, the received message is analysed and then displayed.
     * displayed by {@link #displayChat(String, String)}.</p>
     * Format for CHAT messages from the server:
     * <pre>
     * CHAT sender message
     * </pre>
     * @throws IOException if a read error occurs from the server
     */
    public void readLoop() throws IOException {
        ProtocolWriterClient protocolWriterClient = new ProtocolWriterClient(out);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(Command.SEPARATOR, 3); //did limit from 2 to 3
            String rawCommand = parts[0];
            Command command;

            try {
                command = Command.valueOf(rawCommand);
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown command from server " + line);
                continue;
            }
            // Processing the command with switch-case

            switch (command) {
                case JOIN:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No lobbyname received.");
                        break;
                    }
                    String lobbyName = parts[1].trim();
                    if(lobbyName.equals("Welcome")) {
                        break;
                    } else {
                        System.out.println("You joined: " + lobbyName);
                    }
                    break;

                case CRLO:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                    System.err.println("Error: No lobbyname received.");
                    break;
                    }
                    String lobbyname = parts[1].trim();
                    System.out.println("You created a Lobby: " + lobbyname);
                    break;

                case CHAT:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.out.println("CHAT received: [empty]");
                        break;
                    }
                    // TODO: check
                    //  String[] chatParts = parts[1].split(Command.SEPARATOR, 2);
                    System.out.println(Arrays.toString(parts));
                    if(parts.length == 3) {
                        System.out.println("CHAT received: " + parts[2]); //Fallback
                        String sender = parts[1];
                        String message = parts[2];
                        displayChat(message, sender);
                    } else {
                        // TODO: handle wrong number of parameters
                    }
                    break;

                case PING:
                    //System.out.println("PING received from Server");
                    protocolWriterClient.sendCommand(Command.PONG);
                    break;

                // If the command NICK is recognised, the new nickname is processed
                case NICK:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No Nickname received.");
                        break;
                    }
                    String newNick = parts[1].trim();
                    System.out.println("Your nickname is " + newNick);
                    break;

                case INFO:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No Info received.");
                        break;
                    }
                    String msg = parts[1].trim();
                    System.out.println(msg);
                    break;

                case WISP:
                    String nicknameAndMessage = String.join(" ", parts[1]);
                    String[] nicknameAndMessageParts = nicknameAndMessage.split(Command.SEPARATOR, 2);
                    String whisperMessage = nicknameAndMessageParts[1].trim();
                    if (nicknameAndMessageParts.length < 2 || nicknameAndMessageParts[1].trim().isEmpty()) {
                        System.out.println("WISP received: [empty]");
                        break;
                    }
                    String[] chatPart = nicknameAndMessageParts[1].split(Command.SEPARATOR, 2);
                    //if(chatPart.length < 2) {
                    //    System.out.println("WISP received: " + nicknameAndMessageParts[1]); //Fallback}
                    if (chatPart.length < 2) {
                        String sender = nicknameAndMessageParts[0];
                        String message = nicknameAndMessageParts[1];
                        displayWhisp(message, sender);
                    }
                    break;

                case CHOS:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No fieldId received.");
                        break;
                    }
                    String fieldId = parts[1].trim();
                    System.out.println("Field " + fieldId + " selected.");
                    break;

                case ROLL:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No colors received.");
                        break;
                    }
                    String colors = parts[1].trim();
                    System.out.println("Colors " + colors + " rolled.");
                    break;

                case DEOS:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No fieldId received.");
                        break;
                    }
                    String deselectedFieldId = parts[1].trim();
                    String newcolors = parts[2].trim();
                    System.out.println("Field " + deselectedFieldId + " deselected.");
                    System.out.println("Your colors are " + newcolors);
                    break;

                case BROD:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.out.println("[Broadcast] (empty)");
                        break;
                    }
                    String brodMsg = parts[1].trim();
                    System.out.println(brodMsg);
                    break;


                default:
                    System.out.println("Unknown command from Server: " + line);
                    break;
            }
        }
    }

    /**
     * This method prints the chat message in the correct format.
     * If a ChatController is set, updates the GUI via Platform.runLater.
     * @param message The message that should be sent.
     * @param sender  The nickname of the user that sent the message.
     */
    private void displayChat(String message, String sender) {
        String formattedMessage = sender + ": " + message;
        if (chatController != null) {
            Platform.runLater(() -> chatController.displayChat(formattedMessage));
        } else {
            System.out.println("+CHT " + formattedMessage);
        }
    }

    /**
     * This method prints the incoming whisper-message to the user with the right format.
     * If a ChatController is set, updates the GUI via Platform.runLater.
     * @param message The message that should be sent to the user.
     * @param sender  The nickname of the user that sent the message.
     */
    private void displayWhisp(String message, String sender) {
        // Display the whisper message from the sender
        String formattedMessage = "Whisper from " + sender + ": " + message;
        if (chatController != null) {
            Platform.runLater(() -> chatController.displayChat(formattedMessage));
        } else {
            System.out.println(formattedMessage);
        }

    }

    /**
     * Setter for the ChatController.
     *
     * @param controller The ChatController instance.
     */
    public void setChatController(ChatController controller) {
        this.chatController = controller;
    }
}
