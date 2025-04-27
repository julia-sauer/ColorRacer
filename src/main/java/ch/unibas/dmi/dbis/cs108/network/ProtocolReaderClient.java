package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.gui.GameLobbyController;
import ch.unibas.dmi.dbis.cs108.gui.WelcomeLobbyController;
import javafx.application.Platform;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ProtocolReaderClient} class is responsible for reading and processing messages from
 * the server. It interprets server commands and triggers appropriate actions, such as updating the
 * user interface via {@link WelcomeLobbyController} or {@link GameLobbyController} , printing
 * messages, and responding to server pings. This class works closely with
 * {@link ProtocolWriterClient} to send responses when needed.
 */
public class ProtocolReaderClient {

    private final BufferedReader reader; // reads character lines from client.
    private final InputStream in;
    private final OutputStream out;
    private WelcomeLobbyController welcomeLobbyController; // Reference to the GUI welcomeLobbyController
    private GameLobbyController gameLobbyController;
    public boolean bike = false;
    public boolean gameLobby = false;

    /**
     * Creates a new {@code ProtocolReaderClient}.
     *
     * @param in  The InputStream from which messages are read.
     * @param out The OutputStream to which responses are written.
     * @throws IOException If an error occurs when creating the reader.
     */
    public ProtocolReaderClient(InputStream in, OutputStream out) throws IOException {
        this.in = in;
        this.out = out;
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * The method {@code readLoop} continuously reads lines from the server In the case of a
     * {@code CHAT} command, the received message is analysed and then displayed by
     * {@link #displayChat(String, String)}. Format for CHAT messages from the server:
     * <pre>
     * CHAT sender message
     * </pre>
     *
     * @throws IOException If a read error occurs from the server
     */
    public void readLoop() throws IOException {
        ProtocolWriterClient protocolWriterClient = new ProtocolWriterClient(out);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(Command.SEPARATOR, 3); //did limit from 2 to 3
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
                case JOIN:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No lobbyname received.");
                        break;
                    }
                    String lobbyName = parts[1].trim();
                    if (lobbyName.equals("Welcome")) {
                        break;
                    } else {
                        System.out.println("You joined: " + lobbyName);
                        //WelcomeLobbyController.getInstance().switchToGameLobby(lobbyName);
                    }
                    break;

                case CRLO:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No lobbyname received.");
                        break;
                    }
                    String lobbyname = parts[1].trim();
                    System.out.println("You created a Lobby: " + lobbyname);
                    display("You created a Lobby: " + lobbyname);
                    break;

                case CHAT:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.out.println("CHAT received: [empty]");
                        break;
                    }
                    //System.out.println(Arrays.toString(parts)); // Überprüfung über das Terminal
                    if (parts.length == 3) {
                        //System.out.println("CHAT received: " + parts[2]); // Überprüfung über das Terminal
                        String sender = parts[1];
                        String message = parts[2];
                        displayChat(message, sender);
                    } else {
                        System.err.println("Parameters are missing (either message or sender)");
                    }
                    break;

                case PING:
                    //System.out.println("PING received from Server");
                    protocolWriterClient.sendCommand(Command.PONG);
                    break;

                case NICK:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No Nickname received.");
                        break;
                    }
                    String newNick = parts[1].trim();
                    System.out.println("Your nickname is " + newNick);
                    waitForControllerAndUpdate(() -> WelcomeLobbyController.getInstance().nickname = newNick);
                    display("Your nickname is " + newNick);
                    break;

                case INFO:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No Info received.");
                        break;
                    }
                    String msg = parts[1].trim();
                    System.out.println(msg);
                    display(msg);
                    if (parts[1].startsWith("+POS ")) {
                        // parse "+POS Alice moved to the Field red3"
                        String[] tok = parts[1].split(" ");
                        String whoMoved = tok[1];
                        String fieldId = tok[tok.length - 1];
                        waitForControllerAndUpdate(() ->
                                GameLobbyController.getInstance().updatePlayerPosition(whoMoved, fieldId)
                        );
                        break;
                    }
                    break;

                case WISP:
                    String nickname = String.join(" ", parts[1]);
                    String message = String.join(" ", parts[2]);
                    String[] nicknameAndMessageParts = new String[]{nickname, message};
                    if (nicknameAndMessageParts.length < 2 || nicknameAndMessageParts[1].trim().isEmpty()) {
                        System.out.println("WISP received: [empty]");
                        break;
                    }
                    String[] chatPart = nicknameAndMessageParts[1].split(Command.SEPARATOR, 2);
                    if (chatPart.length < 2) {
                        String sender = nicknameAndMessageParts[0];
                        String whispermessage = nicknameAndMessageParts[1];
                        displayWhisp(whispermessage, sender);
                    }
                    break;

                case CHOS:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No fieldId received.");
                        break;
                    }
                    String fieldId = parts[1].trim();
                    String newColors = parts[2].trim();
                    System.out.println("Field " + fieldId + " selected.");
                    System.out.println("Your colors are: " + newColors);
                    List<String> colorlist = parseListFromString(newColors);
                    String[] colorlistArray = colorlist.toArray(new String[0]);
                    Platform.runLater(() ->
                            GameLobbyController.getInstance().updateDice(colorlistArray)
                    );
                    Platform.runLater(() ->
                            GameLobbyController.getInstance().highlightField(fieldId)
                    );
                    break;

                case MOVE:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No position update received.");
                        break;
                    }
                    String positionMessage = parts[1].trim();
                    System.out.println("+POS " + positionMessage);
                    break;

                case ROLL:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No colors received.");
                        break;
                    }
                    String colors = parts[1].trim();
                    System.out.println("Colors " + colors + " rolled.");
                    List<String> cols = parseListFromString(parts[1]);
                    String[] colorArray = cols.toArray(new String[0]);
                    Platform.runLater(() ->
                            GameLobbyController.getInstance().updateDice(colorArray)
                    );
                    break;

                case DEOS:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No fieldId received.");
                        break;
                    }
                    String deselectedFieldId = parts[1].trim();
                    String colorsDice = parts[2].trim();
                    System.out.println("Field " + deselectedFieldId + " deselected.");
                    System.out.println("Your colors are " + colorsDice);
                    List<String> colorsList = parseListFromString(colorsDice);
                    String[] colorsListArray = colorsList.toArray(new String[0]);
                    Platform.runLater(() ->
                            GameLobbyController.getInstance().unhighlightField(deselectedFieldId)
                    );
                    Platform.runLater(() ->
                            GameLobbyController.getInstance().updateDice(colorsListArray)
                    );
                    break;

                case BROD:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.out.println("[Broadcast] (empty)");
                        break;
                    }
                    String brodMsg = parts[1].trim();
                    //System.out.println(brodMsg); // Überprüfung über das Terminal
                    if (!gameLobby) {
                        welcomeLobbyController.displayChat(brodMsg);
                        break;
                    }
                    gameLobbyController.displayChat(brodMsg);
                    break;

                case STRT:
                    System.out.println("The game starts now!");
                    gameLobbyController.startButton.setDisable(true);
                    gameLobbyController.finishButton.setDisable(false);
                    break;

                case RSTT:
                    System.out.println("The game restarts!");
                    gameLobbyController.finishButton.setDisable(false);
                    gameLobbyController.restartButton.setDisable(true);
                    gameLobbyController.resetPlayerPositions();
                    break;

                case VELO:
                    setBike(true);
                    String[] pc = parts[1].split(" ");
                    String who = pc[0];
                    String color = pc[1];
                    waitForControllerAndUpdate(() ->
                            GameLobbyController.getInstance().addPlayerBike(who, color)
                    );
                    break;

                case FNSH:
                    gameLobbyController.restartButton.setDisable(false);
                    gameLobbyController.finishButton.setDisable(true);
                    protocolWriterClient.sendCommand(Command.FNSH);
                    break;

                case LIST:
                    // Expect the format: PLST::[player1, player2, ...]
                    String playersStr = parts[1];
                    List<String> players = parseListFromString(playersStr);
                    if (!gameLobby) {
                        waitForControllerAndUpdate(
                                () -> WelcomeLobbyController.getInstance().updatePlayerList(players));
                        break;
                    }
                    waitForControllerAndUpdate(
                            () -> GameLobbyController.getInstance().updatePlayerList(players));
                    break;

                case GLST:
                    String gamesStr = parts[1];
                    List<String> games = parseListFromString(gamesStr);
                    if (!gameLobby) {
                        waitForControllerAndUpdate(
                                () -> WelcomeLobbyController.getInstance().updateGameList(games));
                        break;
                    }
                    waitForControllerAndUpdate(() -> GameLobbyController.getInstance().updateGameList(games));
                    break;

                case LOME:
                    // Expect: LOME::lobbyName::[member1, member2, ...]
                    String lobbyMembersStr = parts[1];
                    List<String> members = parseListFromString(lobbyMembersStr);
                    if (!gameLobby) {
                        waitForControllerAndUpdate(
                                () -> WelcomeLobbyController.getInstance().updateLobbyList(members));
                        break;
                    }
                    waitForControllerAndUpdate(
                            () -> GameLobbyController.getInstance().updateLobbyList(members));
                    break;

                case HIGH:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Error: No Data received.");
                        break;
                    }
                    String data = parts[1].trim();
                    System.out.println(data.replace("|", "\n"));
                    display(data.replace("|", "\n"));
                    break;

                default:
                    System.out.println("Unknown command from Server: " + line);
                    break;
            }
        }
    }

    /**
     * This method constructs a formatted string in the form {@code "sender: message"} and prints it
     * to the terminal. It also updates the chat GUI by invoking the {@link WelcomeLobbyController}'s
     * {@code displayChat} method. The messages are still printed in the terminal because it helps in
     * debugging by ensuring that messages are visible in the console as well as in the graphical
     * user's interface.
     *
     * @param message The chat message content received from the server.
     * @param sender  The nickname of the user who sent the message.
     */
    private void displayChat(String message, String sender) {
        String formattedMessage = sender + ": " + message;
        //System.out.println("+CHT " + formattedMessage); // Überprüfung über das Terminal
        if (!gameLobby) {
            welcomeLobbyController.displayChat(formattedMessage);
        } else {
            gameLobbyController.displayChat(formattedMessage);
        }
    }

    /**
     * This method constructs a formatted string indicating that a message is a whisper. It shows in
     * the form {@code "Whisper from sender: message"} and prints it to the terminal, and it also
     * updates the GUI by invoking the {@link WelcomeLobbyController}'s {@code displayChat} method.
     * The messages are still printed in the terminal because it helps in debugging by ensuring that
     * messages are visible in the console as well as in the graphical user's interface.
     *
     * @param message The whisper message content.
     * @param sender  The nickname of the user who sent the whisper.
     */
    private void displayWhisp(String message, String sender) {
        String formattedMessage = "Whisper from " + sender + ": " + message;
        //System.out.println("+CHT " + formattedMessage); // Überprüfung über das Terminal
        if (!gameLobby) {
            welcomeLobbyController.displayChat(formattedMessage);
        } else {
            gameLobbyController.displayChat(formattedMessage);
        }
    }

    /**
     * This method sends an informational message from the server to the controller application and
     * displays it in the Chat.
     *
     * @param message The message that should be displayed in the Chat GUI.
     */
    private void display(String message) {
        if (!gameLobby) {
            waitForControllerAndUpdate(() -> WelcomeLobbyController.getInstance().displayChat(message));
        } else {
            waitForControllerAndUpdate(() -> GameLobbyController.getInstance().displayChat(message));
        }
    }

    /**
     * Sets the {@link WelcomeLobbyController} that will be used to update the GUI with incoming
     * messages. This method is called during the initialization of the GUI, ensuring that the
     * {@link ProtocolReaderClient} can forward them to the GUI for display.
     *
     * @param controller The {@link WelcomeLobbyController} instance to be set.
     */
    public void setWelcomeController(WelcomeLobbyController controller) {
        this.welcomeLobbyController = controller;
    }

    /**
     * Sets the {@link GameLobbyController} that will be used to update the GUI with incoming
     * messages. This method is called during the change from the WelcomeLobby to the GameLobby and
     * ensures that the {@link ProtocolReaderClient} can forward them to the GUI for display.
     *
     * @param controller The {@link GameLobbyController} instance to be set.
     */
    public void setGameLobbyController(GameLobbyController controller) {
        this.gameLobbyController = controller;
    }

    /**
     * This method changes a boolean field so the client knows it needs to change the display of
     * messages to the GameLobby. It is called in the {@link WelcomeLobbyController} when the GUI
     * changes to the {@link GameLobbyController}.
     */
    public void changesController(boolean gameLobby) {
        this.gameLobby = gameLobby;
    }

    /**
     * This method checks if a bike has already been selected. It returns true if the player selects a
     * bike, and false if they haven't selected a bike.
     *
     * @param bike The boolean that indicates when a bike has been selected. the boolean indicating
     *             whether the player has already selected a bike (true) or not (false).
     */
    public void setBike(boolean bike) {
        if (bike) {
            this.bike = true;
        }
    }

    /**
     * Parses a string representation of a list (e.g., "[elem1, elem2, ...]") into a
     * {@link List<String>}. This method trims the input string, removes the square brackets if
     * present, splits the content by commas, and trims each element individually before adding it to
     * the result list.
     *
     * @param listStr the string to parse, typically in the format "[elem1, elem2, ...]"
     * @return a {@code List<String>} containing the individual elements without brackets or
     * whitespace
     */
    private List<String> parseListFromString(String listStr) {
        listStr = listStr.trim();
        // Remove leading and trailing brackets if they exist
        if (listStr.startsWith("[") && listStr.endsWith("]")) {
            listStr = listStr.substring(1, listStr.length() - 1);
        }
        String[] elements = listStr.split(",");
        List<String> result = new ArrayList<>();
        for (String elem : elements) {
            if (!elem.trim().isEmpty()) {
                result.add(elem.trim());
            }
        }
        return result;
    }

    /**
     * Ensures the {@link WelcomeLobbyController} is initialized before performing a UI update. This
     * method spawns a background thread that waits (up to ~500ms) for the
     * {@code WelcomeLobbyController} to become available. Once available, it executes the given
     * update task on the JavaFX Application Thread using {@link Platform#runLater(Runnable)}.
     *
     * @param updateAction the action to perform on the GUI once the controller is ready
     */
    private void waitForControllerAndUpdate(Runnable updateAction) {
        new Thread(() -> {
            // Wait up to 500ms for the controller to be ready
            int attempts = 0;
            while (WelcomeLobbyController.getInstance() == null && attempts < 5) {
                try {
                    Thread.sleep(100);
                    attempts++;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            Platform.runLater(updateAction);
        }).start();
    }
}
