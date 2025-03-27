package ch.unibas.dmi.dbis.cs108.network;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This class sends messages to all connected clients
 * and can also send simple commands to a specific OutputStream.
 */
public class ProtocolWriterServer {
    private final List<PrintWriter> clientWriters;
    /**
     * Constructor: Initializes the list of PrintWriters.
     *
     * @param clientWriters the list of PrintWriters for all connected clients.
     */
    public ProtocolWriterServer(List<PrintWriter> clientWriters) {
        this.clientWriters = clientWriters;
    }
    /**
     * Sends a chat message to all clients.
     *
     * @param message The message to be sent.
     * @param sender The name of the sender.
     */
    public void sendChat(String message, String sender){
        String formatted = Command.CHAT + Command.SEPARATOR + sender + Command.SEPARATOR + message;
        for (PrintWriter writer : clientWriters) {
            writer.println(formatted);
            writer.flush();
        }
    }

    /**
     * Sends the command on the desired output stream.
     *
     * @param out the OutputStream that sends the command.
     * @param command the command string to send
     * @throws IOException if the message could not be sent.
     * @author Jana
     */
    public static void sendCommand(OutputStream out, Command command) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(command + Command.SEPARATOR);
        System.out.println(command + Command.SEPARATOR + "sent");
    }

    public static void sendCommandAndString(OutputStream out, Command command, String text) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(command + Command.SEPARATOR + text);
        System.out.println(command + Command.SEPARATOR + "sent");
    }

    public static void sendInfo(OutputStream out, String msg) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(Command.INFO + Command.SEPARATOR + msg);
    }
}
