package ch.unibas.dmi.dbis.cs108.client;

import ch.unibas.dmi.dbis.cs108.gui.ChatController;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import ch.unibas.dmi.dbis.cs108.server.User;
import ch.unibas.dmi.dbis.cs108.server.UserList;

import java.net.*;
import java.io.*;

/**
 * The {@code Client} class establishes a connection to a server and enables communication
 * via a TCP/IP network. It handles sending and receiving messages according to a defined
 * protocol using {@link ProtocolReaderClient} and {@link ProtocolWriterClient}.
 * The client reads user's input and allows the user to interact with a multiplayer game server by sending
 * commands such as creating or joining lobbies, chatting, selecting game options,
 * and controlling gameplay flow.
 */
public class Client {

    /** The IP address or hostname of the server */
    private final String host;

    /** The port number on which the server is listening */
    private final int port;

    /** The username chosen by the client */
    private final String username;

    /** Responsible for reading protocol messages sent from the server */
    private ProtocolReaderClient protocolReader;

    /** Responsible for writing protocol messages to the server */
    private ProtocolWriterClient protocolWriterClient;

    /**
     * Constructs a new {@code Client} with the specified host, port, and username.
     * If the username is {@code null} or empty, a default name with the system's username is used.
     *
     * @param host The ip-address of the host.
     * @param port The port number of the Server
     * @param username The username the user wants.
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
     * Starts the client, connects to the server, and begins processing user input.
     * A new thread is created to handle incoming server messages.
     * The main thread handles user input and sends the corresponding commands via the
     * {@link ProtocolWriterClient} to the server.
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

            System.out.println("Available commands:");
            System.out.println("- nicknamechange <newnickname>" + "   -> to change your nickname");
            System.out.println("- message <your message>" + "   -> to send a message to others in your lobby");
            System.out.println("- whisper <receiver> <your message>" + "   -> to send a private message to another player");
            System.out.println("- broadcast <your broadcast>" + "   -> to send a message to all users on the server");
            System.out.println("- createlobby <lobbyname>" + "   -> to create a lobby");
            System.out.println("- connect <lobbyname>" + "   -> to connect to an existing lobby");
            System.out.println("- list" + "   -> to list all players on the server");
            System.out.println("- lobbymembers" + "   -> to list all players in your lobby");
            System.out.println("- gamelist" + "   -> to list all ongoing games and lobbies on the server");
            System.out.println("- selectbike <black/magenta/green/darkblue> " + "   -> to select a bike color");
            System.out.println("- ready" + "   -> to signal that you are ready to play");
            System.out.println("- start" + "   -> to start a game");
            System.out.println("- throwdice" + "   -> to throw a dice");
            System.out.println("- fieldchoice <fieldid>" + "   -> to select a field");
            System.out.println("- movetofield" + "   -> to move to the selected field");
            System.out.println("- next" + "   -> to skip your turn");
            System.out.println("- finish" + "   -> to end the game (if you are the host)");
            System.out.println("- leave" + "   -> to quit the server");
            System.out.println("- restart" + "   -> to start a new game");

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
                    if (line.length() <= 15) {
                        System.out.println("Please provide a nickname. Usage: nicknamechange <newname>");
                        continue;
                    }
                    protocolClient.changeNickname(line.substring(15), out);
                } else if (line.startsWith("connect")) {
                    if (line.length() <= 8) {
                        System.out.println("Please provide a lobby name. Usage: connect <lobbyname>");
                        continue;
                    }
                    String lobbyName = line.substring(8).trim();
                    protocolClient.sendJoin(lobbyName);
                } else if (line.startsWith("message")) {
                    if (line.length() <= 8) {
                        System.out.println("Please provide a message. Usage: message <your message>");
                        continue;
                    }
                    String message = line.substring(8).trim();
                    protocolClient.sendChat(message);
                } else if (line.startsWith("whisper")) {
                    // sends a whisper-message to another user
                    if (line.length() <= 8) {
                        System.out.println("Please provide a receiver and message. Usage: whisper <user> <message>");
                        continue;
                    }
                    String receiverNameAndMessage = line.substring(8).trim();
                    protocolClient.sendWhisper(receiverNameAndMessage);
                } else if (line.startsWith("selectbike")) {
                    if (line.length() <= 11) {
                        System.out.println("Please provide a bike name. Usage: selectbike <bike>");
                        continue;
                    }
                    String color = line.substring(11).trim();
                    protocolClient.sendBikeColor(color);
                } else if (line.startsWith("fieldchoice")) {
                    if (line.length() <= 12) {
                        System.out.println("Please provide a field ID. Usage: fieldchoice <fieldId>");
                        continue;
                    }
                    String fieldId = line.substring(12).trim();
                    protocolClient.sendFieldChoice(Command.CHOS, fieldId);
                } else if (line.startsWith("movetofield")) {
                    protocolClient.sendCommand(Command.MOVE);
                } else if (line.startsWith("next")) {
                    protocolClient.sendCommand(Command.NEXT);
                } else if (line.startsWith("throwdice")) {
                    protocolClient.sendCommand(Command.ROLL);
                } else if (line.startsWith("deselect")) {
                    String fieldId = line.substring(9).trim();
                    protocolClient.sendFieldChoice(Command.DEOS, fieldId);
                } else if (line.startsWith("broadcast")) {
                    if (line.length() <= 10) {
                        System.out.println("Please provide a message. Usage: broadcast <message>");
                        continue;
                    }
                    String broadcastmessage = line.substring(10).trim();
                    protocolClient.sendCommandAndString(Command.BROD, broadcastmessage);
                } else if (line.startsWith("createlobby")) {
                    if (line.length() <= 12) {
                        System.out.println("Please provide a lobby name. Usage: createlobby <name>");
                        continue;
                    }
                    String lobbyName = line.substring(12).trim();
                    protocolClient.sendCommandAndString(Command.CRLO, lobbyName);
                } else if (line.startsWith("start")) {
                    protocolClient.sendCommand(Command.STRT);
                } else if (line.equalsIgnoreCase("list")) {
                    protocolClient.sendCommand(Command.LIST);
                } else if (line.equalsIgnoreCase("lobbymembers")) {
                    protocolClient.sendCommand(Command.LOME);
                } else if (line.equalsIgnoreCase("gamelist")) {
                    protocolClient.sendCommand(Command.GLST);
                } else if (line.equalsIgnoreCase("ready")) {
                    if (username != null && protocolReader.bike) {
                        protocolClient.sendReadyStatus();
                    } else {
                        System.out.println("Error: You must select a bike before getting ready. Use 'selectbike <color>' command.");
                    }
                } else if (line.equalsIgnoreCase("finish")) {
                    protocolClient.sendCommand(Command.FNSH);
                } else if (line.equalsIgnoreCase("restart")) {
                    protocolClient.sendCommand(Command.RSTT);
                } else { // if an unknown command is being usedgame
                    System.out.println("Unknown command. Use: connect | nicknamechange | message | leave");

                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Sets the {@link ChatController} for this client by forwarding it to the {@link ProtocolReaderClient}.
     * This allows the {@link ProtocolReaderClient} to update the GUI with incoming messages.
     *
     * @param chatController The ChatController instance to be used for GUI updates.
     */
    public void setChatController(ChatController chatController) {
        if (protocolReader != null) {
            protocolReader.setChatController(chatController);
        }
        if (protocolWriterClient != null){
            protocolWriterClient.setChatController(chatController);
        }
    }

    /**
     * Returns the {@link ProtocolReaderClient} used by this client.
     * The {@link ProtocolReaderClient} handles incoming messages from the server.
     *
     * @return The {@link ProtocolReaderClient} instance.
     */
    public ProtocolReaderClient getProtocolReader(){
        return protocolReader;
    }

    /**
     * Returns the {@link ProtocolWriterClient} used by this client.
     * The {@link ProtocolWriterClient} is responsible for sending messages to the server.
     *
     * @return The {@link ProtocolWriterClient} instance.
     */
    public ProtocolWriterClient getProtocolWriter() {
        return protocolWriterClient;
    }
}
