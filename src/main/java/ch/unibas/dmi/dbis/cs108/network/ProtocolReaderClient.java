package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.server.Server;
import ch.unibas.dmi.dbis.cs108.server.UserList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;

public class ProtocolReaderClient {
    private final BufferedReader reader; // Liest Zeichenzeilen vom Client.
    private final InputStream in;
    private final OutputStream out;


    public ProtocolReaderClient(InputStream in, OutputStream out) throws IOException {
        this.in = in;
        this.out = out;
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * Die Methode {@code readLoop} liest kontinuierlich Zeilen vom Server
     * <p>Im Fall eines {@code CHAT}-Befehls wird die empfangene Nachricht analysiert, und anschließend
     * durch {@link #displayChat(String, String)} angezeigt.</p>
     * Format für CHAT-Nachrichten vom Server:
     * <pre>
     * CHAT <sender> <message>
     * </pre>
     * @throws IOException  IOException Wenn ein Lesefehler vom Server auftritt
     */
    public void readLoop() throws IOException {
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
            // Verarbeiten des Befehls mit switch-case

            switch (command) {

                case CHAT:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        out.write("CHAT received: [empty]\n".getBytes(StandardCharsets.UTF_8));
                        break;
                    }
                    String message = parts[1].trim();
                    out.write(("CHAT received: " + message + "\n").getBytes(StandardCharsets.UTF_8));
                    break;

                case PING:
                    System.out.println("PING received from Server.");
                    ProtocolWriterClient.sendCommand(out, "PONG ");
                    break;

                /**
                 * Wenn Command NICK erkannt wird, wird ausgegeben, zu was der Nickname gewechselt wurde.
                 */
                case NICK:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No Nickname received.");
                        break;
                    }
                    String newNick = parts[1].trim();
                    System.out.println("Nickname changed to " + newNick);
                    break;

                default:
                    System.out.println("Unkown command from Server: " + line);
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
