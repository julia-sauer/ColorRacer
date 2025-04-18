package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * The {@link ProtocolWriterClient} instance to send commands to the server.
     */
    private ProtocolWriterClient protocolWriter;

    private ProtocolReaderClient protocolReader;

    private Client client;

    private String nickname;

    private String lobbyname;

    private boolean isHost;

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

    @FXML
    private void handleStart() {
        // TODO: Implement game start logic
    }

    @FXML
    private void handleFinish() {
        // TODO: Implement game finish logic
    }

    @FXML
    private void handleThrowDice() {
        // TODO: Dice rolling logic
    }

    @FXML
    private void handleMoveToField() {
        // TODO: Movement logic
    }

    @FXML
    private void handleSkip() {
        // TODO: Skip turn logic
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
        this.nickname = nickname;
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
     * Enables/disables start & finish based on whether I'm host.
     */
    public void setHost(boolean host) {
        this.isHost = host;
        Platform.runLater(() -> {
            startButton.setDisable(!host);
            finishButton.setDisable(!host);
        });
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

    public void setHostButtonVisible(boolean visible) {
        startButton.setDisable(false);
        finishButton.setDisable(false);
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
            } catch (Exception e) {
                showError("Failed to update game list", e.getMessage());
            }
        });
    }

    /**
     * This method merges and updates the list of all lobbies and their members by lobby name.
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
     * Given an entry like
     *   "[Lobby: Foo] Players: [Alice, Bob, Charlie]"
     * returns List.of("Alice","Bob","Charlie").
     */
    private List<String> extractPlayers(String entry) {
        int idx = entry.indexOf("Host:");
        if (idx < 0) return List.of();
        String after = entry.substring(idx + "Host:".length()).trim();
        return parseListFromString(after);
    }

    /**
     * Helper to parse "[a, b, c]" into List<String>.
     */
    private List<String> parseListFromString(String listStr) {
        String s = listStr.strip();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length()-1);
        }
        if (s.isBlank()) return List.of();
        return Arrays.stream(s.split(","))
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
        if (start < 0 || end < 0 || end <= start) return "";
        return entry.substring(start, end).trim();
    }
}
