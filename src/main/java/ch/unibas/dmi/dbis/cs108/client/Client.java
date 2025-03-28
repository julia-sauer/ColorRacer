package ch.unibas.dmi.dbis.cs108.client;

import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import java.net.*;
import java.io.*;

/**
 * The Client class establishes a connection to a server and enables communication via a network.
 * This class uses {@link ProtocolReaderClient} and {@link ProtocolWriterClient} objects to read and send messages.
 *
 */
public class Client {
    /**
     * The main method initializes the connection to the server and starts a thread to read messages.
     * It also reads input from the console and sends corresponding commands to the server.
     *
     * @param args Arguments containing the host address and port of the server.
     * @author Julia
     */
    public static void main(String[] args) {
        try {
            // Verbindung zum Server herstellen
            Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
            InputStream in = sock.getInputStream();
            OutputStream out= sock.getOutputStream();

            ProtocolWriterClient protocolWriterClient = new ProtocolWriterClient(out);

            // ProtocolReaderClient-Objekt erstellen und Thread starten
            ProtocolReaderClient protocolReader = new ProtocolReaderClient(in, out);
            Thread readerThread = new Thread(() -> {
                try{
                    protocolReader.readLoop();
                } catch (IOException e) {
                    System.err.println("Error when reading messages: " + e.getMessage());
                }
            });
            readerThread.start();

            // taking system's username
            String systemUsername = System.getProperty("user.name");
            String defaultNickname = "Guest_" + systemUsername;
            // Sends Default-Nickname to server
            protocolWriterClient.sendCommandAndString(Command.NICK, defaultNickname);
            try {
                Thread.sleep(2000); //So the Welcomemessage comes before the System.out.println's that come after that.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Your suggested nickname is " + defaultNickname + ". If you want to change it, please type in your chat and replace the dots with the desired name: nicknamechange ...");
            // Overview of all commands which concerns the user:
            System.out.println("Available commands:");
            System.out.println("- connect <nickname>");
            System.out.println("- nicknamechange <newnickname>");
            System.out.println("- message <your message>");
            System.out.println("- leave");
            // reading input
            BufferedReader conin = new BufferedReader(new InputStreamReader(System.in));
            ProtocolWriterClient protocolClient = new ProtocolWriterClient(out);  // Methodenimplementation im WriterClient

            String line = " ";
            while (true) {
                line = conin.readLine();
                if (line.equalsIgnoreCase("leave")) {
                    // closing connection and sending a QUIT-command
                    protocolClient.leave(out);
                    break;
                } else if (line.startsWith("nicknamechange")){
                    // changing nickname
                    protocolClient.changeNickname(line.substring(15), out);
                } else if (line.startsWith("connect")) {
                    // sends join-command
                    String nickname = line.substring(8).trim();
                    protocolClient.sendJoin(nickname);
                } else if (line.startsWith("message")) {
                    // sends a chat-message
                String message = line.substring(8).trim();
                protocolClient.sendChat(message);
                } else { // if an unknown command is being used
                System.out.println("Unknown command. Use: connect | nicknamechange | message | leave");

                }
            }
            // closing connection
            //System.out.println("Terminating ...");
            //in.close();
            //out.close();
            //sock.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
