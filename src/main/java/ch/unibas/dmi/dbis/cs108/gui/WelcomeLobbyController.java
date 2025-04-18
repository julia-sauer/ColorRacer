package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.server.Lobby;
import ch.unibas.dmi.dbis.cs108.server.Server;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;

/**
 * This class is the controller for the Welcome Lobby view. Manages display and interaction
 * for player lists, game lists, lobby member lists, chat, and transitioning
 * to specific game lobbies.
 */
public class WelcomeLobbyController {

    /**
     * The root pane of the welcome lobby scene.
     */
    @FXML
    private BorderPane root;

    /**
     * The static instance that can be accessed from anywhere.
     */
    private static WelcomeLobbyController instance;

    /**
     * The {@link ListView} that displays a list of all players on the server.
     */
    @FXML
    public ListView<String> listlist;

    /**
     * The {@link ListView} that displays a list of all games with the status and the players.
     */
    @FXML
    private ListView<String> gamelist;

    /**
     * The {@link ListView} that displays a list of all lobbies and its players.
     */
    @FXML
    private ListView<String> lobbylist;

    /**
     * The {@link TextArea} for displaying chat messages.
     */
    @FXML
    private TextArea chatArea;

    /**
     * The {@link TextField} where the user types a message.
     */
    @FXML
    private TextField txtUsermsg;

    /**
     * The {@link ProtocolWriterClient} instance to send commands to the server.
     */
    private ProtocolWriterClient protocolWriter;

    private Client client;

    private Stage primaryStage;

    public String nickname;

    /**
     * This method initializes the lists and stores the static instance reference.
     */
    @FXML
    public void initialize() {
        // Initialize lists with observable array lists
        listlist.setItems(FXCollections.observableArrayList());
        gamelist.setItems(FXCollections.observableArrayList());
        lobbylist.setItems(FXCollections.observableArrayList());
        instance = this;  // Store the instance when initialized
    }

    /**
     * Returns the root BorderPane of this view.
     *
     * @return root BorderPane
     */
    public BorderPane getRoot() {
        return root;
    }

    /**
     * Returns the static instance of this controller.
     *
     * @return WelcomeLobbyController instance
     */
    public static WelcomeLobbyController getInstance() {
        return instance;
    }

    /**
     * Configures the client and its protocol writer for network operations.
     *
     * @param client the client instance
     */
    public void setClient(Client client) {
        this.client = client;
        this.protocolWriter = client.getProtocolWriter();
    }

    /**
     * Sets the primary application stage for modal dialogs.
     *
     * @param stage primary Stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Directly sets the protocol writer for server communication.
     *
     * @param protocolWriter ProtocolWriterClient instance
     */
    public void setProtocolWriter(ProtocolWriterClient protocolWriter) {
        this.protocolWriter = protocolWriter;
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
                System.err.println("Fehler beim Aktualisieren der Spieler-Liste: " + e.getMessage());
                e.printStackTrace();
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
                ObservableList<String> currentItems = gamelist.getItems();
                Map<String, String> lobbyMap = new LinkedHashMap<>();

                // Parse current items into a map: lobbyName -> fullString
                for (String entry : currentItems) {
                    String lobbyName = extractLobbyName(entry);
                    lobbyMap.put(lobbyName, entry);
                }

                // Update or add from newGames
                for (String entry : newGames) {
                    String lobbyName = extractLobbyName(entry);
                    lobbyMap.put(lobbyName, entry); // replaces old if name matches
                }

                gamelist.setItems(FXCollections.observableArrayList(lobbyMap.values()));
            } catch (Exception e) {
                System.err.println("Error updating game list: " + e.getMessage());
                e.printStackTrace();
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
                ObservableList<String> currentItems = lobbylist.getItems();
                Map<String, String> lobbyMap = new LinkedHashMap<>();

                for (String entry : currentItems) {
                    String lobbyName = extractLobbyName(entry);
                    lobbyMap.put(lobbyName, entry);
                }

                for (String entry : newMembers) {
                    String lobbyName = extractLobbyName(entry);
                    lobbyMap.put(lobbyName, entry); // replaces if already exists
                }

                lobbylist.setItems(FXCollections.observableArrayList(lobbyMap.values()));
            } catch (Exception e) {
                System.err.println("Error updating lobby members: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Extracts the lobby name from a formatted entry string so the {@code updateGameList} and the
     * {@code updateLobbyList} methods know when a lobby is already registered in the list.
     *
     * @param entry The string containing "[Lobby: name]"
     * @return The extracted lobby name or empty if it's invalid
     */
    private String extractLobbyName(String entry) {
        if (entry == null || !entry.contains("[Lobby: ")) return "";
        int start = entry.indexOf("[Lobby: ") + 8;
        int end = entry.indexOf("]", start);
        if (start < 0 || end < 0 || end <= start) return "";
        return entry.substring(start, end).trim();
    }

    /**
     * This method sends the QUIT command to leave the server.
     *
     * @throws IOException on network errors if sending the command does not work.
     */
    @FXML
    private void handleLeave() throws IOException {
        protocolWriter.sendCommand(Command.QUIT);
    }

    /**
     * This method opens a dialog to change the user's nickname which then gets send over to the
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
     * Handles key press events on the chat input field.
     * When the ENTER key is pressed, the message is sent to the server.
     *
     * @param event The {@code KeyEvent} triggered by user input.
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
     * This method opens a dialog where the user can input a name to which a lobby is created and sends this
     * over to the server with the {@link ProtocolWriterClient}.
     */
    @FXML
    private void handleCreateLobby() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Lobby");
        dialog.setHeaderText("Enter lobby name:");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(lobbyName -> {
            try {
                protocolWriter.sendCommandAndString(Command.CRLO, lobbyName);
            } catch (IOException e) {
                showError("Failed to create lobby", e.getMessage());
            }
        });
    }

    /**
     * This method opens a dialog where the user can select and join a lobby. The request gets send over to the
     * {@code joinLobby} method.
     */
    @FXML
    private void handleJoinLobby() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/joinLobbyDialog.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setTitle("Join Lobby");
            dialogStage.setScene(new Scene(dialogPane));

            // Set controller
            JoinLobbyDialogController controller = loader.getController();
            controller.setAvailableLobbies(getAvailableLobbyNames());
            controller.setDialogStage(dialogStage);
            controller.setWelcomeLobbyController(this);

            dialogStage.showAndWait();

            String selectedLobby = controller.getSelectedLobby();
            if (selectedLobby != null) {
                joinLobby(selectedLobby);
            }
        } catch (IOException e) {
            showError("Failed to open join lobby dialog", e.getMessage());
        }
    }

    // TODO Watch if lobbies are actually available to join.
    /**
     * This method extracts lobby names from the gamelist entries.
     *
     * @return list of lobby names
     */
    private List<String> getAvailableLobbyNames() {
        return gamelist.getItems()
                .stream()
                .map(entry -> entry.split("]")[0].replace("[Lobby: ", "").trim())
                .toList();
    }

    /**
     * This method sends a JOIN command over with the {@link ProtocolWriterClient} and activates the method that
     * changes to the GameLobby view.
     *
     * @param lobbyName The name of the lobby the user wants to join.
     */
    public void joinLobby(String lobbyName) {
        protocolWriter.sendJoin(lobbyName);
        switchToGameLobby(lobbyName);
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
     * This method transitions the scene to the GameLobby view for the given lobby.
     *
     * @param lobbyName The name of the lobby the user joined.
     */
    public void switchToGameLobby(String lobbyName) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameLobbyTemplate.fxml"));
                BorderPane gameLobbyRoot = loader.load();

                GameLobbyController gameLobbyController = loader.getController();
                gameLobbyController.setLobbyName(lobbyName);
                gameLobbyController.setProtocolWriter(protocolWriter);
                client.setGameLobbyController(gameLobbyController);
                gameLobbyController.setClient(client);
                gameLobbyController.setNickname(nickname);
                // add additional data here: client, protocolWriter, protocolReader

                gameLobbyController.listlist.setItems(listlist.getItems());
                gameLobbyController.gamelist.setItems(gamelist.getItems());
                gameLobbyController.lobbylist.setItems(lobbylist.getItems());

                // Replaces current WelcomeLobby scene
                Scene scene = primaryStage.getScene();
                scene.setRoot(gameLobbyRoot);
                primaryStage.setMaximized(true);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to load GameLobbyTemplate.fxml");
            }
        });
    }
}