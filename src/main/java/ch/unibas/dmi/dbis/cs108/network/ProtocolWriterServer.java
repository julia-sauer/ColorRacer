package ch.unibas.dmi.dbis.cs108.network;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The {@code ProtocolWriterServer} class is responsible for sending protocol messages
 * from the server to connected clients. It supports broadcasting messages, sending
 * individual commands, and whispering to specific clients.
 * All messages are encoded in UTF-8 to ensure cross-platform compatibility.
 */
public class ProtocolWriterServer {

    /**
     * A list of writers connected to all clients.
     * This is used for broadcasting messages to all clients.
     */
    private final List<PrintWriter> clientWriters;

    /** The writer associated with a specific client connection. */
    private final PrintWriter writer;



    /**
     * Constructs a {@code ProtocolWriterServer} with the provided client list and output stream.
     *
     * @param clientWriters A list of all client PrintWriters to allow broadcast communication.
     * @param outputStream  The OutputStream for a specific client.
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
    public void sendChat(String message, String sender) {
        String formatted = Command.CHAT + Command.SEPARATOR + sender + Command.SEPARATOR + message;
        writer.println(formatted);   //  Use the class-level writer
        writer.flush();
    }

    /**
     * Sends a command without arguments to the associated client.
     *
     * @param command The protocol command to be sent to the client.
     * @throws IOException If the command could not be sent.
     */
    public void sendCommand(Command command) throws IOException {
        sendToClient(command + Command.SEPARATOR);
        System.out.println(command + " sent");
    }

    /**
     * Sends an informational message to the client using the {@code INFO} protocol command.
     *
     * @param msg The String that needs to be sent to the client.
     * @throws IOException The error-handling if the message could not be sent.
     */
    public void sendInfo(String msg) throws IOException {
        sendToClient(Command.INFO + Command.SEPARATOR + msg);
        System.out.println(Command.INFO + " sent");
    }

    /**
     * Sends a command with additional text to the client.
     *
     * @param command The command that should be sent to the client.
     * @param text The String that should be sent with the command.
     * @throws IOException The error-handling if the command could not be sent.
     */
    public void sendCommandAndString(Command command, String text) throws IOException {
        sendToClient(command + Command.SEPARATOR + text);
        System.out.println(command + " sent");
        writer.flush();
    }

    /**
     * This method sends an already formatted string message to the client, more specifically
     * to the {@link ProtocolReaderClient}.
     *
     * @param message The message that should be sent to the client.
     */
    public void sendToClient(String message) {
        writer.println(message);
        writer.flush();
    }

    /**
     * This method sends a private (whisper) message to a specific user using the {@code WISP} command.
     *
     * @param message The message that should be sent.
     * @param sender The nickname of the user who sent the message.
     * @param receiver The nickname of the user who receives the message.
     */
    public void sendWhisper(String message, String sender, String receiver) {
        String formatted = Command.WISP + Command.SEPARATOR + sender + Command.SEPARATOR + message;
        if (receiver != null) {
            writer.println(formatted);
            writer.flush();
        }
    }
}
