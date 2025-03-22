package ch.unibas.dmi.dbis.cs108.network;

import java.io.*;
import java.util.List;

/**
 * Diese Klasse sendet Nachrichten an alle verbundenen Clients
 * und kann auch einfache Befehle an einen bestimmten OutputStream senden.
 */
public class ProtocolWriterServer {
    private final List<PrintWriter> clientWriters;
    /**
     * Konstruktor: Initialisiert die Liste der PrintWriter.
     *
     * @param clientWriters die Liste der PrintWriter für alle verbundenen Clients.
     */
    public ProtocolWriterServer(List<PrintWriter> clientWriters) {
        this.clientWriters = clientWriters;
    }
    /**
     * Sendet eine Chat-Nachricht an alle Clients.
     *
     * @param message Die zu sendende Nachricht.
     * @param sender Der Name des Senders.
     */
    public void sendChat(String message, String sender){
        String formatted = Command.CHAT.name() + Command.SEPARATOR + sender + Command.SEPARATOR + message;
        for (PrintWriter writer : clientWriters) {
            writer.println(formatted);
            writer.flush();
        }
    }

    /**
     * Sendet den Command auf dem gewünschten OutputStream.
     *
     * @param out
     * @param command
     * @throws IOException wenn die Nachricht nicht gesendet werden konnte.
     * @author Jana
     */
    public static void sendCommand(OutputStream out, String command) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        try {
            writer.write(command + Command.SEPARATOR);
            writer.flush();
            System.out.println(command + " sent");
        } catch (IOException e) {
            System.err.println("Error, Could not send Command");
        }
    }
}
