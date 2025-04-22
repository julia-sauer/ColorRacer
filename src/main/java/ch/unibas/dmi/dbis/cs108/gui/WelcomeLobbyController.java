package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
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
        listlist.setItems(FXCollections.observableArrayList()); // Initialize lists with observable array lists
        gamelist.setItems(FXCollections.observableArrayList());
        lobbylist.setItems(FXCollections.observableArrayList());
        instance = this;  // Store the instance when initialized
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
            } catch (Exception e) {
                showError("Failed to update lobby list", e.getMessage());
            }
        });
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
            dialogStage.initOwner(primaryStage);
            dialogStage.setTitle("Leave Lobby");
            dialogStage.setScene(new Scene(dialogPane));

            LeaveLobbyDialogController controller = loader.getController();// Sets controller
            controller.setDialogStage(dialogStage);

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

            JoinLobbyDialogController controller = loader.getController(); // Set controller
            controller.setAvailableLobbies(getAvailableLobbyNames());
            controller.setDialogStage(dialogStage);

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
     * This method extracts lobby names from the game list entries.
     *
     * @return The list of lobby names
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
                gameLobbyController.setPrimaryStage(primaryStage);
                // sets the lists
                gameLobbyController.listlist.setItems(listlist.getItems());
                gameLobbyController.gamelist.setItems(gamelist.getItems());
                gameLobbyController.lobbylist.setItems(lobbylist.getItems());

                primaryStage.setMaximized(true);
                Scene scene = new Scene(gameLobbyRoot);
                primaryStage.setScene(scene);
//
//                // Gets screen bounds
//                Screen screen = Screen.getPrimary();
//                Rectangle2D bounds = screen.getVisualBounds();
//
//                // Sets full screen bounds (without fullscreen mode)
//                primaryStage.setX(bounds.getMinX());
//                primaryStage.setY(bounds.getMinY());
//                primaryStage.setWidth(bounds.getWidth());
//                primaryStage.setHeight(bounds.getHeight());
//
//                // Bind the root pane to the scene dimensions
//                gameLobbyRoot.prefWidthProperty().bind(scene.widthProperty());
//                gameLobbyRoot.prefHeightProperty().bind(scene.heightProperty());
//
//                // removes white gap background effect
//                scene.setFill(null);

                // Show the window
                primaryStage.show();
            } catch (IOException e) {
                showError("Failed to load the GameLobby" + lobbyName, e.getMessage());
            }
        });
    }
}