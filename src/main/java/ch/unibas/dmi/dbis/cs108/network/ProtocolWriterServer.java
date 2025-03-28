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
    private final PrintWriter writer; //Der Writer ist "final", weil er nach der Initialisierung nicht mehr verändert wird.

    /**
     * Constructor: Initialises the list of PrintWriters.
     *
     * @param clientWriters the list of PrintWriters for all connected clients.
     */
    public ProtocolWriterServer(List<PrintWriter> clientWriters, OutputStream outputStream) {
        this.clientWriters = clientWriters;
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

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
     * Sends a command on the desired output stream.
     *
     * @param command the command String to send
     * @throws IOException if the command could not be sent.
     * @author Jana
     */
    public void sendCommand(Command command) throws IOException {
        writer.println(command + Command.SEPARATOR);
        System.out.println(command + Command.SEPARATOR + "sent");
    }

    /**
     * Sends a message with the Command INFO to the client.
     * @param msg the String that needs to be sent to the client.
     * @throws IOException is the error-handling if the message could not be sent.
     */
    public void sendInfo(String msg) throws IOException {
        writer.println(Command.INFO + Command.SEPARATOR + msg);
        System.out.println(Command.INFO + Command.SEPARATOR + "sent");
    }

    /**
     * Sends a command with additional text to the client.
     * @param out the OutputStream that sends the command.
     * @param command the command that should be sent to the client.
     * @param text the String that should be sent with the command.
     * @throws IOException is the error-handling if the command could not be sent.
     */
    public static void sendCommandAndString(OutputStream out, Command command, String text) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(command + Command.SEPARATOR + text);
        System.out.println(command + Command.SEPARATOR + "sent");
    }
}
