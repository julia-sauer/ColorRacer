package ch.unibas.dmi.dbis.cs108.network;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
/**
 * The ProtocolReaderClient class processes incoming messages from the server
 * and controls the interaction with the client.
 */
public class ProtocolReaderClient {
    private final BufferedReader reader; // Liest Zeichenzeilen vom Client.
    private final InputStream in;
    private final OutputStream out;

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
     * CHAT <sender> <message>
     * </pre>
     * @throws IOException if a read error occurs from the server
     */
    public void readLoop() throws IOException {
        ProtocolWriterClient protocolWriterClient = new ProtocolWriterClient(out);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(Command.SEPARATOR, 2);
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

                case CHAT:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.out.println("CHAT received: [empty]");
                        break;
                    }
                    String[] chatParts = parts[1].split(Command.SEPARATOR, 2);
                    if(chatParts.length < 2) {
                        System.out.println("CHAT received: " + parts[1]); //Fallback
                    } else {
                        String sender = chatParts[0];
                        String message = chatParts[1];
                        displayChat(message, sender);
                    }
                    break;

                case PING:
                    System.out.println("PING received from Server");
                    protocolWriterClient.sendCommand(Command.PONG);
                    break;

                // If the command NICK is recognised, the new nickname is processed
                case NICK:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No Nickname received.");
                        break;
                    }
                    String newNick = parts[1].trim();
                    System.out.println("Nickname changed to " + newNick);
                    break;

                case INFO:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No Info received.");
                        break;
                    }
                    String msg = parts[1].trim();
                    System.out.println(msg);
                    break;

                default:
                    System.out.println("Unknown command from Server: " + line);
                    break;
            }
        }
    }

    /**
     * Gibt eine empfangene Chatnachricht formatiert in der Konsole aus.
     * @param message Die eigentliche Chatnachricht.
     * @param sender  Der Benutzername des Absenders.
     * @author anasv
     */
    private void displayChat(String message, String sender) {
        System.out.println("+CHT " + sender + ": " + message);
    }

}
