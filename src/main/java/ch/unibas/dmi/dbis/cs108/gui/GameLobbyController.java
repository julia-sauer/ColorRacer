package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This is the controller class for managing the GameLobby view.
 * This includes chat handling, lobby and player list updates and
 * game controls.
 *
 * @author julia
 */
public class GameLobbyController {

    /**
     * The static instance that can be accessed from anywhere.
     */
    private static GameLobbyController instance;

    /**
     * The root pane of the welcome lobby scene.
     */
    private BorderPane root;

    /**
     * The {@link Label} that displays the lobbyname.
     */
    @FXML
    public Label lobbyNameDisplay;

    /**
     * The {@link TextArea} for displaying chat messages.
     */
    @FXML
    public TextArea chatArea;

    /**
     * The {@link TextField} where the user types a message.
     */
    @FXML
    public TextField txtUsermsg;

    /**
     * The {@link ListView} that displays a list of all players on the server.
     */
    @FXML
    public ListView<String> listlist;

    /**
     * The {@link ListView} that displays a list of all games with the status and the players.
     */
    @FXML
    public ListView<String> gamelist;

    /**
     * The {@link ListView} that displays a list of all lobbies and its players.
     */
    @FXML
    public ListView<String> lobbylist;

    /**
     * The {@link Button} where a user can show they are ready to play.
     */
    @FXML
    public Button readyButton;

    /**
     * The {@link Button} only for the host to start the game.
     */
    @FXML
    public Button startButton;

    /**
     * The {@link Button} only for the host to finish the game earlier.
     */
    @FXML
    public Button finishButton;

    /**
     * The {@link Button} to roll the dices.
     */
    @FXML
    private Button throwDiceButton;

    /**
     * The {@link Button} to move to the chosen field.
     */
    @FXML
    private Button moveButton;

    /**
     * The {@link Button} to skip a turn.
     */
    @FXML
    private Button skipButton;

    /**
     * The {@link ImageView} for the six dices.
     */
    @FXML
    private ImageView dice1, dice2, dice3, dice4, dice5, dice6;

    @FXML
    private AnchorPane gameBoard;

    /** The field button the user clicked most recently. */
    private Button selectedFieldButton;

    /** Tracks all field buttons the user has clicked but not yet moved to. */
    private final Set<Button> selectedFieldButtons = new HashSet<>();

    @FXML
    private Button yellow1, yellow2, yellow3, yellow4, yellow5, yellow6, yellow7;

    @FXML
    private Button red1, red2, red3, red4, red5, red6, red7;

    @FXML
    private Button blue1, blue2, blue3, blue4, blue5, blue6, blue7, blue8, blue9, blue10;

    @FXML
    private Button purple1, purple2, purple3, purple4, purple5, purple6, purple7, purple8, purple9, purple10;

    @FXML
    private Button pink1, pink2, pink3, pink4, pink5, pink6, pink7, pink8, pink9, pink10;

    @FXML
    private Button orange1, orange2, orange3, orange4, orange5, orange6, orange7, orange8, orange9, orange10;

    /**
     * The {@link ProtocolWriterClient} instance to send commands to the server.
     */
    private ProtocolWriterClient protocolWriter;

    private ProtocolReaderClient protocolReader;

    private Client client;

    private String nickname;

    private String lobbyname;

    private boolean isHost = false;

    /**
     * Initializes the controller instance and the lists, and opens the bike selection dialog immediately after joining.
     */
    @FXML
    public void initialize() {
        // Initialize lists with observable array lists
        listlist.setItems(FXCollections.observableArrayList());
        gamelist.setItems(FXCollections.observableArrayList());
        lobbylist.setItems(FXCollections.observableArrayList());
        instance = this;  // Store the instance when initialized

        Platform.runLater(this::handleBikeSelection);
    }

    /**
     * Sets the {@link ProtocolWriterClient} for sending messages and commands through the network.
     *
     * @param protocolWriter The {@link ProtocolWriterClient} instance.
     */
    public void setProtocolWriter(ProtocolWriterClient protocolWriter) {
        this.protocolWriter = protocolWriter;
    }

    /**
     * Sets the nickname of the user that's connected with this {@link GameLobbyController}.
     *
     * @param nickname The nickname that the user has.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Configures the client and its protocol writer for network operations.
     *
     * @param client the client instance
     */
    public void setClient(Client client) {
        this.client = client;
        this.protocolWriter = client.getProtocolWriter();
        this.protocolReader = client.getProtocolReader();
    }

    /**
     * Returns the static instance of this controller.
     *
     * @return The {@link GameLobbyController} instance.
     */
    public static GameLobbyController getInstance() {
        return instance;
    }

    /**
     * This method handles a {@link Button} {@link java.awt.event.ActionEvent} when a user wants the message to be
     * sent to all users.
     */
    @FXML
    private void handleBroadcast() {
        String message = txtUsermsg.getText().trim();
        if (!message.isEmpty()) {
            // Send the message using your existing network protocol
            if (protocolWriter != null) {
                protocolWriter.sendChat("broadcast " + message);
            }
            txtUsermsg.clear();
        }
    }

    /**
     * Handles key press events on the chat input field.
     * When the ENTER key is pressed, the message is sent to the server.
     *
     * @param event the {@code KeyEvent} triggered by user input.
     */
    @FXML
    private void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }

    /**
     * Retrieves the message from the chat input field and sends it over the {@link ProtocolWriterClient}.
     * The message is sent via the injected {@link ProtocolWriterClient} instance. After sending,
     * the input field is cleared with {@code txtUsermsg.clear()}.
     */
    private void sendMessage() {
        String message = txtUsermsg.getText().trim();
        if (!message.isEmpty()) {
            // Send the message using your existing network protocol
            if (protocolWriter != null) {
                protocolWriter.sendChat(message);
            }
            txtUsermsg.clear();
        }
    }

    /**
     * Displays a new chat message in the chat area.
     * This method is typically called from the {@link ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient}
     * when a new message is received.
     * To ensure that the GUI is updated on the JavaFX Application Thread, the update is wrapped
     * in a call to {@link Platform#runLater(Runnable)}.
     *
     * @param message the chat message to be displayed.
     */
    public void displayChat(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }

    /**
     * This method sends a ready status to the server and lets all know that the user is ready to play the game.
     */
    @FXML
    private void handleReady() {
        try {
            if(protocolReader.bike) {
                protocolWriter.sendReadyStatus();
            }
        } catch (IOException e) {
            showError("Failed to send ready status", e.getMessage());
        }
    }

    /**
     * This method sends the {@code STRT} command to the server when all players are ready. Only the host can
     * use this {@link Button} respectively this method.
     */
    @FXML
    private void handleStart() {
        try {
            protocolWriter.sendCommand(Command.STRT);
        } catch (IOException e) {
            showError("Failed to start the game", e.getMessage());
        }
    }

    /**
     * This method sends the {@code FNSH} command to the server. Only the host can use this {@link Button}
     * respectively this method.
     */
    @FXML
    private void handleFinish() {
        try {
            protocolWriter.sendCommand(Command.FNSH);
        } catch (IOException e) {
            showError("Failed to end the game", e.getMessage());
        }
    }

    //TODO javadoc
    @FXML
    private void handleThrowDice() {
        try {
            protocolWriter.sendCommand(Command.ROLL);
        } catch (IOException e) {
            showError("Failed to throw the dices", e.getMessage());
        }
    }

    //TODO javadoc
    @FXML
    private void handleMoveToField() {
        try {
            protocolWriter.sendCommand(Command.MOVE);
            // clear highlights on all selected fields
            for (Button btn : selectedFieldButtons) {
                btn.getStyleClass().remove("field-button-selected");
            }
            selectedFieldButtons.clear();
        } catch (IOException e) {
            showError("Failed to move to the field", e.getMessage());
        }
    }

    //TODO javadoc
    @FXML
    private void handleSkip() {
        try {
            protocolWriter.sendCommand(Command.NEXT);
        } catch (IOException e) {
            showError("Failed to skip your turn", e.getMessage());
        }
    }

    /**
     * This method opens a dialog to ask the user if they want to leave the server. If the user wants to leave
     * a message is sent over the {@link ProtocolWriterClient} and the window closes.
     */
    @FXML
    private void handleLeave() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LeaveLobbyDialogTemplate.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Leave Lobby");
            dialogStage.setScene(new Scene(dialogPane));

            // Set controller
            LeaveLobbyDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setGameLobbyController(this);

            dialogStage.showAndWait();

            if (controller.isLeaving) {
                protocolWriter.sendCommandAndString(Command.QCNF, "YES");
                Platform.exit();
            }
        } catch (IOException e) {
            showError("Failed to open leave lobby dialog", e.getMessage());
        }
    }

    /**
     * This method opens a dialog to change the user's nickname which then gets send over with the
     * {@link ProtocolWriterClient}.
     */
    @FXML
    private void handleNicknameChange() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Choose Your Nickname");
        dialog.setHeaderText("Enter nickname:");
        dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(nickname -> protocolWriter.changeNickname(nickname));
    }

    /**
     * Opens the bike selection dialog when joining a lobby or pressing the {@link MenuItem} 'Change Bike'.
     */
    @FXML
    private void handleBikeSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SelectBikeDialogTemplate.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Select Your Bike");
            dialogStage.setScene(new Scene(dialogPane));

            SelectBikeDialogController bikeDialogController = loader.getController();
            bikeDialogController.setDialogStage(dialogStage);
            bikeDialogController.setGameLobby(this);

            dialogStage.showAndWait();

            String selectedBike = bikeDialogController.getSelectedBike();
            if (selectedBike != null) {
                setBike(selectedBike);
            }

        } catch (IOException e) {
            showError("Failed to open join lobby dialog", e.getMessage());
        }
    }

    /**
     * Sends the selected bike over with the {@link ProtocolWriterClient}.
     *
     * @param color The color of the bike that was chosen.
     */
    public void setBike(String color) {
        protocolWriter.sendBikeColor(color);
    }

    /**
     * Sets the {@link Label} that displays the lobby name.
     *
     * @param name The name of the lobby
     */
    public void setLobbyName(String name) {
        Platform.runLater(() -> lobbyNameDisplay.setText("Lobby: " + name));
        this.lobbyname = name;
    }

    /**
     * Enables/disables start & finish based on whether the user is the host.
     *
     * @param host The boolean whether the user is the host (first player in the lobby) or not
     */
    public void setHost(boolean host) {
        this.isHost = host;
        Platform.runLater(() -> {
            startButton.setDisable(!host);
            startButton.setVisible(host);
            finishButton.setDisable(!host);
        });
    }

    public void gameOngoing(){
        if(isHost){
            finishButton.setVisible(true);
        }
        gameBoard.setVisible(true);
    }

    // TODO implementing when it should set the dices visible.
    /**
     * Sets all dice images visible when pressing the {@link Button} 'Roll'.
     *
     * @param visible true to show dice, false to hide
     */
    public void setDiceVisible(boolean visible) {
        dice1.setVisible(visible);
        dice2.setVisible(visible);
        dice3.setVisible(visible);
        dice4.setVisible(visible);
        dice5.setVisible(visible);
        dice6.setVisible(visible);
    }

    //TODO include this method for all action methods for security.
    /**
     * Shows an error alert with specified header and content.
     *
     * @param header The header text
     * @param content The content text
     */
    private void showError(String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * This method updates the list of all players. First it clears the list which is important when
     * a user quits or changes their nickname, and then it reproduces it with the new updated list version.
     *
     * @param players The list of current player names received from the {@link ProtocolReaderClient}
     */
    public void updatePlayerList(List<String> players) {
        Platform.runLater(() -> {
            try {
                listlist.getItems().clear(); //
                listlist.getItems().addAll(players);
            } catch (Exception e) {
                showError("Failed to update player list", e.getMessage());
            }
        });
    }

    /**
     * This method merges and updates the list of all games by lobby name.
     *
     * @param newGames The formatted list of game lobby entries.
     */
    public void updateGameList(List<String> newGames) {
        Platform.runLater(() -> {
            try {
                gamelist.getItems().clear();
                gamelist.getItems().addAll(newGames);
                // 2) Check *your* lobby’s status
                for (String entry : newGames) {
                    // extractLobbyName(...) must return exactly your lobby’s name
                    if (extractLobbyName(entry).equalsIgnoreCase(lobbyname)) {
                        // get the text after the "]"
                        int end = entry.indexOf("]");
                        String status = entry.substring(end + 1).trim();
                        // status is now e.g. "open", "running", or "finished"
                        if (status.equalsIgnoreCase("running")) {
                            gameOngoing();
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                showError("Failed to update game list", e.getMessage());
            }
        });
    }

    /**
     * This method merges and updates the list of all lobbies and their members by lobby name. It also sets
     * the host of the lobby by looking at the first name in the list in the corresponding lobby.
     *
     * @param newMembers The formatted list of lobby member entries.
     */
    public void updateLobbyList(List<String> newMembers) {
        Platform.runLater(() -> {
            try {
                lobbylist.getItems().clear();
                lobbylist.getItems().addAll(newMembers);

                Map<String, String> lobbyMap = new LinkedHashMap<>();
                for (String entry : newMembers) {
                    lobbyMap.put(extractLobbyName(entry), entry);
                }
                String myEntry = lobbyMap.get(lobbyname);
                if (myEntry != null) {
                    List<String> players = extractPlayers(myEntry);
                    boolean amHost = !players.isEmpty()
                            && players.get(0).equalsIgnoreCase(nickname);
                    setHost(amHost);
                }
            } catch (Exception e) {
                showError("Failed to update lobby list", e.getMessage());
            }
        });
    }

    /**
     * Given an entry like //TODO correct Javadoc
     *   "[Lobby: Foo] Players: [Alice, Bob, Charlie]"
     * returns List.of("Alice","Bob","Charlie").
     */
    private List<String> extractPlayers(String entry) {
        int idx = entry.indexOf("Players:");
        if (idx < 0) return List.of();
        String after = entry.substring(idx + "Players:".length()).trim();
        // strip the brackets
        if (after.startsWith("[") && after.endsWith("]")) {
            after = after.substring(1, after.length() - 1);
        }
        if (after.isBlank()) return List.of();
        // split on pipe, trimming whitespace
        return Arrays.stream(after.split("\\|"))
                .map(String::trim)
                .toList();
    }

    /**
     * Extracts the lobby name from a formatted entry string so the {@code updateGameList} and the
     * {@code updateLobbyList} methods know when a lobby is already registered in the list.
     *
     * @param entry The string containing "[Lobby: name]".
     * @return The extracted lobby name or empty if it's invalid.
     */
    private String extractLobbyName(String entry) {
        if (entry == null || !entry.contains("[Lobby: ")) return "";
        int start = entry.indexOf("[Lobby: ") + 8;
        int end = entry.indexOf("]", start);
        if (end <= start) return "";
        return entry.substring(start, end).trim();
    }

    @FXML
    public void handleFieldChoice(ActionEvent event) {
        Button btn = (Button)event.getSource();
        String fieldId = btn.getId();
        try {
            if (selectedFieldButtons.contains(btn)) {
                protocolWriter.sendFieldChoice(Command.DEOS, fieldId);
            } else {
                protocolWriter.sendFieldChoice(Command.CHOS, fieldId);
            }
        } catch (Exception e) {
            showError("Failed to send field choice", e.getMessage());
        }
    }

    public void highlightField(String fieldId) {
        // look up your Button (either via reflection or a pre‐built Map<String,Button>)
        Button btn = lookupButton(fieldId);
        if (btn == null) return;
        if (!btn.getStyleClass().contains("field-button-selected")) {
            btn.getStyleClass().add("field-button-selected");
        }
        selectedFieldButtons.add(btn);
    }

    /**
     * Called when the server confirms a DEOS (deselect) for fieldId.
     */
    public void unhighlightField(String fieldId) {
        Button btn = lookupButton(fieldId);
        if (btn == null) return;
        btn.getStyleClass().remove("field-button-selected");
        selectedFieldButtons.remove(btn);
    }

    private Button lookupButton(String fieldId) {
        try {
            Field f = getClass().getDeclaredField(fieldId);
            f.setAccessible(true);
            return (Button) f.get(this);
        } catch (Exception e) {
            System.err.println("No field-button with fx:id=" + fieldId);
            return null;
        }
    }

}
