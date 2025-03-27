package ch.unibas.dmi.dbis.cs108.network;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        String formatted = Command.CHAT + Command.SEPARATOR + sender + Command.SEPARATOR + message;
        for (PrintWriter writer : clientWriters) {
            writer.println(formatted);
            writer.flush();
        }
    }

    /**
     * Sendet den Command auf dem gewünschten OutputStream.
     *
     * @param out der OutputStream der den Command sendet.
     * @param command der Command-String zum Senden
     * @throws IOException wenn die Nachricht nicht gesendet werden konnte.
     * @author Jana
     */
    public static void sendCommand(OutputStream out, Command command) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(command + Command.SEPARATOR);
        System.out.println(command + Command.SEPARATOR + "sent");
    }

    public static void sendInfo(OutputStream out, String msg) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(Command.INFO + Command.SEPARATOR + msg);
    }
}
