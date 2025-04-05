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
                    String nickname = String.join(" ", parts[1]);
                    String message = String.join(" ", parts[2]);
                    String[] nicknameAndMessageParts = new String[]{nickname, message};
                    if (nicknameAndMessageParts.length < 2 || nicknameAndMessageParts[1].trim().isEmpty()) {
                        System.out.println("WISP received: [empty]");
                        break;
                    }
                    String[] chatPart = nicknameAndMessageParts[1].split(Command.SEPARATOR, 2);
                    //if(chatPart.length < 2) {
                    //    System.out.println("WISP received: " + nicknameAndMessageParts[1]); //Fallback}
                    if (chatPart.length < 2) {
                        String sender = nicknameAndMessageParts[0];
                        String whispermessage = nicknameAndMessageParts[1];
                        displayWhisp(whispermessage, sender);
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

                case STRT:
                    System.out.println(" Spiel wurde gestartet!");
                    break;

                default:
                    System.out.println("Unknown command from Server: " + line);
                    break;
            }
        }
    }

    /**
     * This method constructs a formatted string in the form {@code "sender: message"} and prints it to the terminal.
     * It also updates the chat GUI by invoking the controller's {@code displayChat} method.
     * The messages are still printed in the terminal because it helps in debugging
     * by ensuring that messages are visible in the console as well as in the graphical interface.
     *
     * @param message the chat message content received from the server.
     * @param sender  the nickname of the user who sent the message.
     */
    private void displayChat(String message, String sender) {
        String formattedMessage = sender + ": " + message;
        System.out.println("+CHT " + formattedMessage); //so it is still printed in the terminal to check
        chatController.displayChat(formattedMessage);

    }

    /**
     * This method constructs a formatted string indicating that a message is a whisper. It shows in the
     * form {@code "Whisper from sender: message"} and prints it to the terminal, and it also updates
     * the GUI by invoking the controller's {@code displayChat} method.
     * The messages are still printed in the terminal because it helps in debugging
     * by ensuring that messages are visible in the console as well as in the graphical interface.
     *
     * @param message the whisper message content.
     * @param sender  the nickname of the user who sent the whisper.
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
     * Sets the {@link ChatController} that will be used to update the GUI with incoming messages.
     * This method is called during the initialization of the GUI, ensuring that the {@link ProtocolReaderClient}
     * can forward them to the GUI for display.
     *
     * @param controller the {@link ChatController} instance to be set.
     */
    public void setChatController(ChatController controller) {
        this.chatController = controller;
    }

}
