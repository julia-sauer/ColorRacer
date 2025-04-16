package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
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

public class WelcomeLobbyController {

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

    private ProtocolWriterClient protocolWriter;
    private Client client;
    private Stage primaryStage;

    public BorderPane getRoot() {
        return root;
    }

    @FXML
    public void initialize() {
        // Initialize lists with observable array lists
        listlist.setItems(FXCollections.observableArrayList());
        gamelist.setItems(FXCollections.observableArrayList());
        lobbylist.setItems(FXCollections.observableArrayList());
        instance = this;  // Store the instance when initialized
    }

    public static WelcomeLobbyController getInstance() {
        return instance;
    }

    // Start updates when client is set
    public void setClient(Client client) {
        this.client = client;
        this.protocolWriter = client.getProtocolWriter();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setProtocolWriter(ProtocolWriterClient protocolWriter) {
        this.protocolWriter = protocolWriter;
    }

    // This method is called to update the player list (listlist)
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

    // This method is called to update the game lobby list (gamelist)
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

    // This method is called to update the lobby member list (lobbylist)
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

    private String extractLobbyName(String entry) {
        if (entry == null || !entry.contains("[Lobby: ")) return "";
        int start = entry.indexOf("[Lobby: ") + 8;
        int end = entry.indexOf("]", start);
        if (start < 0 || end < 0 || end <= start) return "";
        return entry.substring(start, end).trim();
    }

    @FXML
    private void handleLeave() throws IOException {
        protocolWriter.sendCommand(Command.QUIT);
    }

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

    private List<String> getAvailableLobbyNames() {
        return gamelist.getItems()
                .stream()
                .map(entry -> entry.split("]")[0].replace("[Lobby: ", "").trim()) // extract "LobbyOne" from "[Lobby: LobbyOne] open | Players: [...]"
                .toList();
    }

    public void joinLobby(String lobbyName) {
        protocolWriter.sendJoin(lobbyName);
    }

    private void showError(String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

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

    public void switchToGameLobby(String lobbyName) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameLobbyTemplate.fxml"));
                BorderPane gameLobbyRoot = loader.load();

                // Get controller and pass lobby name or other data if needed
                GameLobbyController gameLobbyController = loader.getController();
                gameLobbyController.setLobbyName(lobbyName);
                // You can pass additional data here: client, protocolWriter, etc.

                // Replace current scene
                Scene scene = primaryStage.getScene();
                scene.setRoot(gameLobbyRoot);
                primaryStage.setFullScreen(true);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to load GameLobbyTemplate.fxml");
            }
        });
    }
}