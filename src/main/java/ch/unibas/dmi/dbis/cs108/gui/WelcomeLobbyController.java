package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import ch.unibas.dmi.dbis.cs108.server.Server;
import ch.unibas.dmi.dbis.cs108.server.UserList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WelcomeLobbyController {

    @FXML
    private BorderPane root;

    // Static instance that can be accessed from anywhere
    private static WelcomeLobbyController instance;

    @FXML
    private ListView<String> listlist;

    @FXML
    private ListView<String> gamelist;

    @FXML
    private ListView<String> lobbylist;

    @FXML
    private Button createLobbyButton;

    @FXML
    private Button joinLobbyButton;

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

    @FXML
    private MenuItem leaveOption;

    @FXML
    private MenuItem nicknameChangeOption;

    private ProtocolWriterClient protocolWriter;
    private Client client;
    private Stage primaryStage;
    private Timeline updateTimeline;



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

        // Start the update mechanism with a small initial delay to allow everything to be set up
        Timeline initialDelay = new Timeline(new KeyFrame(Duration.millis(300), event -> startUpdateTimeline()));
        initialDelay.play();
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
    public void updatePlayerList(List<String> fetchedPlayers) {
        Platform.runLater(() -> {
            try {
                // Merge the current items and the fetched players
                ObservableList<String> currentItems = listlist.getItems();
                Set<String> newSet = new LinkedHashSet<>(currentItems);
                newSet.addAll(fetchedPlayers);
                // Only update if there is a change
                if (!currentItems.equals(FXCollections.observableArrayList(newSet))) {
                    listlist.setItems(FXCollections.observableArrayList(newSet));
                }
            } catch (Exception e) {
                System.err.println("Error updating player list: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // This method is called to update the game lobby list (gamelist)
    public void updateGameList(List<String> games) {
        Platform.runLater(() -> {
            try {
                ObservableList<String> currentItems = gamelist.getItems();
                Set<String> newSet = new LinkedHashSet<>(currentItems);
                newSet.addAll(games);

                if (!currentItems.equals(FXCollections.observableArrayList(newSet))) {
                    gamelist.setItems(FXCollections.observableArrayList(newSet));
                }
            } catch (Exception e) {
                System.err.println("Error updating game list: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // This method is called to update the lobby member list (lobbylist)
    public void updateLobbyList(String lobbyName, List<String> members) {
        Platform.runLater(() -> {
            try {
                ObservableList<String> currentItems = lobbylist.getItems();
                Set<String> newSet = new LinkedHashSet<>(currentItems);
                newSet.add(lobbyName);

                if (!currentItems.equals(FXCollections.observableArrayList(newSet))) {
                    lobbylist.setItems(FXCollections.observableArrayList(newSet));
                }
            } catch (Exception e) {
                System.err.println("Error updating lobby members: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Starts a Timeline that periodically (every second) polls for updated data.
    // In a push model this may be unnecessary, but is useful for testing timing.
    private void startUpdateTimeline() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // For demonstration, we assume methods that fetch updated lists exist.
            // Replace these calls with your mechanism to request the latest data.
            List<String> updatedPlayers = UserList.getAllUsernames();
            List<String> updatedGames = Server.getUpdatedGames();
            List<String> updatedLobbyList = Server.getUpdatedLobbyMembers();

            updatePlayerList(updatedPlayers);
            updateGameList(updatedGames);
            updateLobbyList("currentLobby", updatedLobbyList);
        }));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
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

        dialog.showAndWait().ifPresent(nickname -> {
            protocolWriter.changeNickname(nickname);
        });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("joinLobbyDialog.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(new Scene(dialogPane));

            JoinLobbyDialogController controller = loader.getController();
            controller.setAvailableLobbies(listlist.getItems());
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
}
