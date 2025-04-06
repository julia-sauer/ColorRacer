package ch.unibas.dmi.dbis.cs108.client;

import ch.unibas.dmi.dbis.cs108.gui.ChatController;
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

    private final String host;
    private final int port;
    private final String username;

    private ProtocolReaderClient protocolReader;
    private ProtocolWriterClient protocolWriterClient;

    /**
     * Constructor for class Client
     * @param host The ip-adress of the host.
     * @param port the port number of the Server
     * @param username the username the user wants.
     */
    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        if(username != null && !username.isEmpty()) {
            this.username = username;
        } else {
            String systemusername = System.getProperty("user.name");
            this.username = "Guest_" + systemusername;
        }
    }
    /**
     * The main method initializes the connection to the server and starts a thread to read messages.
     * It also reads input from the console and sends corresponding commands to the server.
     *
     * @author Julia
     */
    public void start() {
        try {
            // Verbindung zum Server herstellen
            Socket sock = new Socket(host, port);
            InputStream in = sock.getInputStream();
            OutputStream out= sock.getOutputStream();

            this.protocolWriterClient = new ProtocolWriterClient(out);

            // ProtocolReaderClient-Objekt erstellen und Thread starten
            protocolReader = new ProtocolReaderClient(in, out);
            Thread readerThread = new Thread(() -> {
                try{
                    protocolReader.readLoop();
                } catch (IOException e) {
                    System.err.println("Error when reading messages: " + e.getMessage());
                }
            });
            readerThread.start();

            // Sends username to server
            protocolWriterClient.sendCommandAndString(Command.NICK, username);
            protocolWriterClient.sendJoin("Welcome");
            try {
                Thread.sleep(2000); //Welcomemessage comes before the System.out.println's that come after that.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println("Your suggested nickname is " + defaultNickname + ". If you want to change it, please type in your chat and replace the dots with the desired name: nicknamechange ...");
            // Overview of all commands which concerns the user:
            System.out.println("Available commands:");
            System.out.println("- connect <lobbyname>");
            System.out.println("- nicknamechange <newnickname>");
            System.out.println("- message <your message>");
            System.out.println("- leave");
            System.out.println("- whisper <receiver> <your message>");
            System.out.println("- selectbike");
            System.out.println("- throwdice");
            System.out.println("- fieldchoice <fieldid>");
            System.out.println("- movetofield");
            System.out.println("- broadcast <your broadcast>");
            System.out.println("- createlobby <lobbyname>");
            System.out.println("- start");
            System.out.println("- list");
            System.out.println("- lobbymembers");
            System.out.println("- gamelist");

            // reading input
            BufferedReader conin = new BufferedReader(new InputStreamReader(System.in));
            ProtocolWriterClient protocolClient = new ProtocolWriterClient(out);  // Methodenimplementation im WriterClient

            String line = " ";
            while (true) {
                line = conin.readLine();
                if (line.equalsIgnoreCase("leave")) {
                    // closing connection and sending a QUIT-command
                    protocolClient.leave(out);
                } else if (line.equalsIgnoreCase("YES")) {
                    protocolClient.sendCommandAndString(Command.QCNF, "YES");
                    System.out.println("You confirmed to leave the game.");
                    break;
                } else if (line.equalsIgnoreCase("NO")) {
                    protocolClient.sendCommandAndString(Command.QCNF, "NO");
                } else if (line.startsWith("nicknamechange")) {
                    // changing nickname
                    protocolClient.changeNickname(line.substring(15), out);
                } else if (line.startsWith("connect")) {
                    // sends join-command
                    String lobbyName = line.substring(8).trim();
                    protocolClient.sendJoin(lobbyName);
                } else if (line.startsWith("message")) {
                    // sends a chat-message
                    String message = line.substring(8).trim();
                    protocolClient.sendChat(message);
                } else if (line.startsWith("whisper")) {
                    // sends a whisper-message to another user
                    String receiverNameAndMessage = line.substring(8).trim();
                    protocolClient.sendWhisper(receiverNameAndMessage);
                } else if (line.startsWith("selectbike")) {
                    String color = line.substring(11).trim();
                    protocolClient.sendBikeColor(color);
                } else if (line.startsWith("fieldchoice")) {
                    String fieldId = line.substring(12).trim();
                    protocolClient.sendFieldChoice(Command.CHOS, fieldId);
                } else if (line.startsWith("movetofield")) {
                    protocolClient.sendCommand(Command.MOVE);
                } else if (line.startsWith("throwdice")) {
                    protocolClient.sendCommand(Command.ROLL);
                } else if (line.startsWith("deselect")) {
                    String fieldId = line.substring(9).trim();
                    protocolClient.sendFieldChoice(Command.DEOS, fieldId);
                } else if (line.startsWith("broadcast")) {
                    String broadcastmessage = line.substring(10).trim();
                    protocolClient.sendCommandAndString(Command.BROD, broadcastmessage);
                } else if (line.startsWith("createlobby")) {
                    String lobbyName = line.substring(12).trim();
                    protocolClient.sendCommandAndString(Command.CRLO, lobbyName);
                } else if (line.startsWith("start")) {
                    protocolClient.sendCommand(Command.STRT);
                }else if (line.equalsIgnoreCase("list")) {
                    protocolClient.sendCommand(Command.LIST);
                }else if (line.equalsIgnoreCase("lobbymembers")) {
                    protocolClient.sendCommand(Command.LOME);
                }else if (line.equalsIgnoreCase("gamelist")) {
                        protocolClient.sendCommand(Command.GLST);
                } else { // if an unknown command is being usedgame
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

    /**
     * Sets the {@link ChatController} for this client by forwarding it to the {@link ProtocolReaderClient}.
     * This allows the {@link ProtocolReaderClient} to update the GUI with incoming messages.
     *
     * @param chatController the ChatController instance to be used for GUI updates.
     */
    public void setChatController(ChatController chatController) {
        if (protocolReader != null) {
            protocolReader.setChatController(chatController);
        }
    }

    /**
     * Retrieves the {@link ProtocolReaderClient} used by this client.
     * The {@link ProtocolReaderClient} handles incoming messages from the server.
     *
     * @return the {@link ProtocolReaderClient} instance, or {@code null} if it has not been initialized.
     */
    public ProtocolReaderClient getProtocolReader(){
        return protocolReader;
    }

    /**
     * Retrieves the {@link ProtocolWriterClient} used by this client.
     * The {@link ProtocolWriterClient} is responsible for sending messages to the server.
     *
     * @return the {@link ProtocolWriterClient} instance, or {@code null} if it has not been initialized.
     */
    public ProtocolWriterClient getProtocolWriter() {
        return protocolWriterClient;
    }
}
