package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import javafx.stage.StageStyle;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.util.*;

/**
 * This class is the controller for the Welcome Lobby view. Manages display and interaction for
 * player lists, game lists, lobby member lists, chat, and transitioning to specific game lobbies.
 *
 * @author julia
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
    public ListView<String> listList;

    /**
     * The {@link ListView} that displays a list of all games with the status and the players.
     */
    @FXML
    private ListView<String> gameList;

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

    /**
     * The client that uses this Welcome Lobby.
     */
    private Client client;

    /**
     * The primary stage for the scene setup.
     */
    private Stage primaryStage;

    /**
     * The nickname of the user that uses this Welcome Lobby.
     */
    public String nickname;

    /**
     * The {@link MediaPlayer} that plays the background music on a loop.
     */
    private MediaPlayer bgmPlayer;

    /**
     * The {@link Media} of the click sound that we created.
     */
    private final Media clickMedia =
            new Media(Objects.requireNonNull(getClass().getResource("/audio/Click.mp3")).toExternalForm());

    /**
     * The “Fullscreen Mode” {@link MenuItem} in the {@link MenuBar}.
     */
    @FXML
    private CheckMenuItem fullscreenMode;

    /**
     * This method initializes the lists and stores the static instance reference. It also starts the {@link MediaPlayer}
     * that plays the background music on a loop.
     */
    @FXML
    public void initialize() {
        listList.setItems(FXCollections.observableArrayList());
        gameList.setItems(FXCollections.observableArrayList());
        lobbylist.setItems(FXCollections.observableArrayList());
        instance = this;

        Media bgm = new Media(Objects.requireNonNull(getClass().getResource("/audio/WelcomeLobbyMusic.mp3")).toExternalForm());

        bgmPlayer = new MediaPlayer(bgm);
        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgmPlayer.setVolume(0.5);
        bgmPlayer.play();
    }

    /**
     * This method plays the sound that we created for a mouse-click or another {@link javafx.event.ActionEvent}.
     */
    private void playClickThen() {
        MediaPlayer p = new MediaPlayer(clickMedia);
        p.setOnEndOfMedia(p::dispose);
        p.setVolume(0.5);
        p.play();
    }

    /**
     * Returns the static instance of this controller.
     *
     * @return The {@link WelcomeLobbyController} instance.
     */
    public static WelcomeLobbyController getInstance() {
        return instance;
    }

    /**
     * Configures the client and its {@link ProtocolWriterClient} for network operations.
     *
     * @param client The client instance.
     */
    public void setClient(Client client) {
        this.client = client;
        this.protocolWriter = client.getProtocolWriter();
    }

    /**
     * Sets the primary application stage for modal dialogs.
     *
     * @param stage The primary Stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Directly sets the {@link ProtocolWriterClient} for server communication.
     *
     * @param protocolWriter ProtocolWriterClient instance
     */
    public void setProtocolWriter(ProtocolWriterClient protocolWriter) {
        this.protocolWriter = protocolWriter;
    }

    /**
     * Sets the nickname of the user that's connected with this {@link WelcomeLobbyController}.
     *
     * @param nickname The nickname that the user uses.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * This method updates the list of all players. First it clears the list which is important when a
     * user quits or changes their nickname, and then it reproduces it with the new updated list
     * version.
     *
     * @param players The list of current player names received from the {@link ProtocolReaderClient}.
     */
    public void updatePlayerList(List<String> players) {
        Platform.runLater(() -> {
            try {
                listList.getItems().clear(); //
                listList.getItems().addAll(players);
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
                gameList.getItems().clear();
                gameList.getItems().addAll(newGames);
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
     * This method opens a dialog to ask the user if they want to leave the server. If the user wants
     * to leave a {@code QCNF} command is sent over the {@link ProtocolWriterClient} to the server and the window closes.
     */
    @FXML
    private void handleLeave() {
        try {
            playClickThen();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/LeaveLobbyDialogTemplate.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setTitle("Leave Lobby");
            dialogStage.setScene(new Scene(dialogPane));

            LeaveLobbyDialogController controller = loader.getController();// Sets controller
            controller.setDialogStage(dialogStage);
            controller.setLeaveStatement("Would you like to leave the server?");

            dialogStage.showAndWait();

            if (controller.isLeaving) {
                client.disconnect();
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
        playClickThen();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Choose Your Nickname");
        dialog.setHeaderText("Enter nickname:");
        dialog.setContentText("Name:");
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);

        dialog.getDialogPane()
                .getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/layout/styles/defaultStyle.css")).toExternalForm());

        dialog.showAndWait().ifPresent(nickname -> protocolWriter.changeNickname(nickname));
    }

    /**
     * Handles key press events on the chat input field. When the ENTER key is pressed, the message is
     * sent to the server.
     *
     * @param event The {@code KeyEvent} triggered by user input.
     */
    @FXML
    private void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            playClickThen();
            sendMessage();
        }
    }

    /**
     * Retrieves the message from the chat input field and sends it over the
     * {@link ProtocolWriterClient}. The message is sent via the injected {@link ProtocolWriterClient}
     * instance. After sending, the input field is cleared with {@code txtUsermsg.clear()}.
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
     * Displays a new chat message in the {@code chatArea}. This method is called from the
     * {@link ProtocolReaderClient} when a new message is received.
     * To ensure that the GUI is updated on the JavaFX Application Thread, the update is wrapped in a
     * call to {@link Platform#runLater(Runnable)}.
     *
     * @param message The chat message to be displayed.
     */
    public void displayChat(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }

    /**
     * This method opens a dialog where the user can input a name to which a lobby is created and
     * sends this with the command {@code CRLO} over to the server with the {@link ProtocolWriterClient}.
     */
    @FXML
    private void handleCreateLobby() {
        playClickThen();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Lobby");
        dialog.setHeaderText("Enter lobby name:");
        dialog.setContentText("Name:");

        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);

        dialog.getDialogPane()
                .getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/layout/styles/defaultStyle.css")).toExternalForm());

        dialog.showAndWait().ifPresent(lobbyName -> {
            try {
                protocolWriter.sendCommandAndString(Command.CRLO, lobbyName);
            } catch (IOException e) {
                showError("Failed to create lobby", e.getMessage());
            }
        });
    }

    /**
     * This method opens a dialog where the user can select and join a lobby. The request gets send
     * over to the {@code joinLobby} method.
     */
    @FXML
    private void handleJoinLobby() {
        try {
            playClickThen();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/joinLobbyDialog.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
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

    /**
     * Retrieves a list of available lobby names that are currently open (not running)
     * and have fewer than 4 players.
     *
     * <p>This method combines information from the {@code gameList} and {@code lobbylist} GUI components:
     * <ul>
     *   <li>It checks the {@code gameList} for lobbies marked as "open".</li>
     *   <li>It then cross-references the {@code lobbylist} to ensure the lobby has less than 4 players.</li>
     * </ul>
     *
     * @return A list of lobby names that are open and not full.
     */
    private List<String> getAvailableLobbyNames() {
        Map<String, Integer> playerCountMap = new HashMap<>();

        // Build map of lobby name → player count from lobbylist
        for (String entry : lobbylist.getItems()) {
            try {
                String lobbyName = entry.split("]")[0].replace("[Lobby: ", "").trim();
                String playersPart = entry.substring(entry.indexOf("Players:") + 8).trim();

                if (playersPart.startsWith("[") && playersPart.endsWith("]")) {
                    playersPart = playersPart.substring(1, playersPart.length() - 1);
                }

                int playerCount = playersPart.isBlank() ? 0 : playersPart.split("\\|").length;
                playerCountMap.put(lobbyName, playerCount);
            } catch (Exception e) {
                System.err.println("Failed to parse lobbylist entry: " + entry);
            }
        }

        // Now filter gameList entries by open status and player count < 4
        return gameList.getItems().stream()
                .map(String::trim)
                .filter(entry -> entry.contains("open")) // only non-running
                .map(entry -> entry.split("]")[0].replace("[Lobby: ", "").trim())
                .filter(lobbyName -> playerCountMap.getOrDefault(lobbyName, 0) < 4)
                .toList();
    }

    /**
     * This method sends a {@code JOIN} command over to the server with the {@link ProtocolWriterClient} and activates
     * the method that changes to the Game Lobby view.
     *
     * @param lobbyName The name of the lobby the user wants to join.
     */
    public void joinLobby(String lobbyName) {
        protocolWriter.sendJoin(lobbyName);
    }

    /**
     * Shows an error alert with specified header and content.
     *
     * @param header  The header text.
     * @param content The content text.
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
     * This method handles a {@link Button} next to the chat. This button handles when a user wants the
     * message to be sent to all users.
     */
    @FXML
    private void handleBroadcast() {
        playClickThen();
        String message = txtUsermsg.getText().trim();
        if (!message.isEmpty()) {
            if (protocolWriter != null) {
                protocolWriter.sendChat("broadcast " + message);
            }
            txtUsermsg.clear();
        }
    }

    /**
     * Handles the action to display the highscore list.
     * <p>
     * This method loads the HighscoreListDialogTemplate.fxml file and shows it in a modal dialog.
     * It also passes the created Stage to the {@link HighscoreListDialogController} so the dialog
     * can be programmatically closed from within the controller.
     * </p>
     * <p>
     * The dialog is set to be modal to the primary stage, ensuring user focus remains
     * within the highscore list window until it is closed.
     * </p>
     */
    @FXML
    public void handleHighscoreList() {
        try {
            playClickThen();
            protocolWriter.sendCommand(Command.HIGH);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/HighscoreListDialogTemplate.fxml"));
            VBox dialogPane = fxmlLoader.load();

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Highscore List");
            dialogStage.setScene(new Scene(dialogPane));

            HighscoreListDialogController highscoreListDialogController = fxmlLoader.getController();
            highscoreListDialogController.setDialogStage(dialogStage);

            dialogStage.showAndWait();
        } catch (IOException e) {
            showError("Error reading highscore", e.getMessage());
        }
    }

    /**
     * Toggles the stage’s fullscreen state whenever the “Fullscreen Mode” {@link CheckMenuItem} is clicked.
     */
    @FXML
    private void onToggleFullscreen() {
        primaryStage.setFullScreen(fullscreenMode.isSelected());
    }

    /**
     * This method transitions the scene to the Game Lobby view for the given lobby and sets all instances needed for
     * the Welcome Lobby.
     *
     * @param lobbyName The name of the lobby the user joined.
     */
    public void switchToGameLobby(String lobbyName) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/GameLobbyTemplate.fxml"));
                BorderPane gameLobbyRoot = loader.load();

                GameLobbyController gameLobbyController = loader.getController();
                gameLobbyController.setLobbyName(lobbyName);
                gameLobbyController.setProtocolWriter(protocolWriter);
                client.setGameLobbyController(gameLobbyController);
                gameLobbyController.setClient(client);
                gameLobbyController.setNickname(nickname);
                gameLobbyController.setPrimaryStage(primaryStage);
                // sets the lists
                gameLobbyController.listList.setItems(listList.getItems());
                gameLobbyController.gameList.setItems(gameList.getItems());
                gameLobbyController.lobbylist.setItems(lobbylist.getItems());

                if (bgmPlayer != null) {
                    bgmPlayer.stop();
                    bgmPlayer.dispose();
                }
                //primaryStage.setMaximized(true);
                Scene scene = new Scene(gameLobbyRoot);
                primaryStage.setScene(scene);

                // Show the window
                primaryStage.show();
            } catch (IOException e) {
                showError("Failed to load the GameLobby" + lobbyName, e.getMessage());
            }
        });
    }
}