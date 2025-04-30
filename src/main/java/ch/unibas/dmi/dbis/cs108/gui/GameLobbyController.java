package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
 * This is the controller class for managing the Game Lobby view. This includes chat handling, lobby
 * and player list updates, game controls and an integration with the bike selection dialog.
 *
 * @author julia
 */
public class GameLobbyController {

    /**
     * The static instance that can be accessed from anywhere.
     */
    private static GameLobbyController instance;

    /**
     * The {@link Label} for displaying the lobbyname.
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
    public ListView<String> listList;

    /**
     * The {@link ListView} that displays a list of all games with the status and the players.
     */
    @FXML
    public ListView<String> gameList;

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
     * The {@link MenuItem} only for the host to start the game.
     */
    @FXML
    public MenuItem startButton;

    /**
     * The {@link MenuItem} only for the host to finish the game earlier.
     */
    @FXML
    public MenuItem finishButton;

    /**
     * The {@link MenuItem} for restarting a game.
     */
    @FXML
    public MenuItem restartButton;

    /**
     * The {@link Menu} for the buttons only the host is allowed to use (to start, finish or restart a game).
     */
    @FXML
    public Menu hostButtons;

    /**
     * The {@link Menu} for the bike selection dialog.
     */
    @FXML
    public Menu bikeOption;

    /**
     * The {@link Button} that lets the player roll the dice.
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
     * The {@link ImageView}s used to show the six dice rolled.
     */
    @FXML
    private ImageView dice1, dice2, dice3, dice4, dice5, dice6;

    /**
     * A map that stores the preloaded dice images by color name.
     */
    private Map<String, Image> diceImages;

    /**
     * The game board pane where the field buttons and bike icons are displayed.
     */
    @FXML
    private AnchorPane gameBoard;

    /**
     * Tracks all field buttons that have been selected by the user but not yet confirmed.
     */
    private final List<Button> selectedFieldButtons = new ArrayList<>();

    /**
     * The colored field {@link Button}s grouped by color.
     */
    @FXML
    @SuppressWarnings("unused")
    private Button yellow1, yellow2, yellow3, yellow4, yellow5, yellow6, yellow7;
    @FXML
    @SuppressWarnings("unused")
    private Button red1, red2, red3, red4, red5, red6, red7;
    @FXML
    @SuppressWarnings("unused")
    private Button blue1, blue2, blue3, blue4, blue5, blue6, blue7, blue8, blue9, blue10;
    @FXML
    @SuppressWarnings("unused")
    private Button purple1, purple2, purple3, purple4, purple5, purple6, purple7, purple8, purple9, purple10;
    @FXML
    @SuppressWarnings("unused")
    private Button pink1, pink2, pink3, pink4, pink5, pink6, pink7, pink8, pink9, pink10;
    @FXML
    @SuppressWarnings("unused")
    private Button orange1, orange2, orange3, orange4, orange5, orange6, orange7, orange8, orange9, orange10;

    /**
     * The white starting field button for all players.
     */
    @FXML
    @SuppressWarnings("unused")
    private Button white1;

    /**
     * The {@link ProtocolWriterClient} instance to send commands to the server.
     */
    private ProtocolWriterClient protocolWriter;

    /**
     * The {@link ProtocolReaderClient} used to read incoming data from the server.
     */
    private ProtocolReaderClient protocolReader;

    /**
     * The nickname of the local player using this controller.
     */
    private String nickname;

    /**
     * The name of the lobby this player is currently in.
     */
    private String lobbyname;

    /**
     * The client that uses this Lobby.
     */
    private Client client;

    /**
     * Maps each player's nickname to their corresponding {@link ImageView} bike icon.
     */
    private final Map<String, ImageView> playerBikes = new HashMap<>();

    /**
     * Maps each selectable bike color to its corresponding {@link Image} graphic.
     */
    private final Map<String, Image> bikeImages = new HashMap<>();

    /**
     * The primary stage for the scene setup.
     */
    @FXML
    private Stage primaryStage;

    /**
     * Initializes the controller instance and the lists, and opens the bike selection dialog
     * immediately after joining. It also sets the Map for the six images for the dice colors and for the 4 bike color images.
     */
    @FXML
    public void initialize() {
        listList.setItems(FXCollections.observableArrayList());
        gameList.setItems(
                FXCollections.observableArrayList()); // Initializes lists with observable array lists
        lobbylist.setItems(FXCollections.observableArrayList());
        instance = this;  // Store the instance when initialized
        diceImages = new HashMap<>(); // Load all dice‐color images into a map:
        diceImages.put("yellow", new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/dice_yellow_dummy.png"))));
        diceImages.put("orange", new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/dice_orange_dummy.png"))));
        diceImages.put("red",
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dice_red_dummy.png"))));
        diceImages.put("pink",
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dice_pink_dummy.png"))));
        diceImages.put("purple", new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/dice_purple_dummy.png"))));
        diceImages.put("blue",
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dice_blue_dummy.png"))));
        bikeImages.put("black",
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/blackbike.png"))));
        bikeImages.put("magenta",
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/magentabike.png"))));
        bikeImages.put("green",
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/greenbike.png"))));
        bikeImages.put("darkblue",
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/darkbluebike.png"))));

        moveButton.setDisable(true);   // start out disabled

        Platform.runLater(this::handleBikeSelection); //starts bike selection right at joining
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
        ImageView bike = playerBikes.remove(this.nickname);
        if (bike != null) {
            playerBikes.put(nickname, bike);
        }
        this.nickname = nickname;
    }

    /**
     * Configures the client and its {@link ProtocolWriterClient} for network operations.
     *
     * @param client The client instance.
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
     * This method handles a {@link Button} {@link java.awt.event.ActionEvent} when a user wants the
     * message to be sent to all users.
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
     * Handles key press events on the chat input field. When the ENTER key is pressed, the message is
     * sent to the server.
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
     * Displays a new chat message in the chat area. This method is typically called from the
     * {@link ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient} when a new message is received.
     * To ensure that the GUI is updated on the JavaFX Application Thread, the update is wrapped in a
     * call to {@link Platform#runLater(Runnable)}.
     *
     * @param message the chat message to be displayed.
     */
    public void displayChat(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }

    /**
     * This method sends a ready status to the server and let all know that the user is ready to play
     * the game.
     */
    @FXML
    private void handleReady() {
        try {
            if (protocolReader.bike) {
                protocolWriter.sendReadyStatus();
                readyButton.setVisible(false);
            }
        } catch (IOException e) {
            showError("Failed to send ready status", e.getMessage());
        }
    }

    /**
     * This method sends the {@code STRT} command to the server when all players are ready. Only the
     * host can use this {@link Button} respectively this method.
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
     * Handles the restart action triggered from the GUI.
     * Sends a {@code RSTT} command to the server and resets all player bike positions.
     * This method is typically bound to a restart button in the UI using JavaFX's {@code @FXML} annotation.
     * If an I/O error occurs while sending the command, an error dialog is shown to the user.
     */
    @FXML
    private void handleRestart() {
        try {
            protocolWriter.sendCommand(Command.RSTT);
            // Reset all bike positions on restart
        } catch (IOException e) {
            showError("Failed to restart the game", e.getMessage());
        }
    }

    /**
     * Resets all player bike positions to the starting field "white1".
     */
    public void resetPlayerPositions() {
        // Ensure UI update runs on JavaFX Application Thread
        Platform.runLater(() -> {
            for (String player : playerBikes.keySet()) {
                // Move each bike back to white1
                updatePlayerPosition(player, "white1");
            }
        });
    }

    /**
     * This method sends the {@code FNSH} command to the server. Only the host can use this
     * {@link Button} respectively this method.
     */
    @FXML
    private void handleFinish() {
        try {
            protocolWriter.sendCommand(Command.FNSH);
            finishButton.setDisable(true);
            restartButton.setDisable(false);
        } catch (IOException e) {
            showError("Failed to end the game", e.getMessage());
        }
    }

    /**
     * Sends the {@code ROLL} command to the server to request the dice roll. This method is triggered
     * when the user clicks the "Roll" {@link Button}.
     */
    @FXML
    private void handleThrowDice() {
        try {
            protocolWriter.sendCommand(Command.ROLL);
        } catch (IOException e) {
            showError("Failed to throw the dices", e.getMessage());
        }
    }

    /**
     * Updates the dice display based on the colors received from the server. This method is called
     * from the {@link ProtocolReaderClient} after receiving the {@code ROLL} command from the server.
     * It updates each {@link ImageView} with the corresponding colored dice image.
     *
     * @param colors An array of color names corresponding to the dice roll results.
     */
    public void updateDice(String[] colors) {
        Platform.runLater(() -> {
            ImageView[] views = {dice1, dice2, dice3, dice4, dice5, dice6};

            for (int i = 0; i < views.length; i++) {
                if (i < colors.length && colors[i] != null) {
                    // Try to load the image
                    Image img = diceImages.get(colors[i].toLowerCase());
                    if (img != null) {
                        views[i].setImage(img);
                        views[i].setVisible(true);
                    } else {
                        // Color string was non-null, but no image found
                        views[i].setImage(null);
                        views[i].setVisible(false);
                    }
                } else {
                    // No color at this index (or it's null) ⇒ hide the die
                    views[i].setImage(null);
                    views[i].setVisible(false);
                }
            }
        });
    }

    /**
     * Sends the {@code MOVE} command to the server to confirm movement to the selected field(s).
     * After the move, all highlighted fields are cleared, the dice are hidden and the move button is disabled.
     */
    @FXML
    private void handleMoveToField() {
        try {
            protocolWriter.sendCommand(Command.MOVE);
            for (Button btn : selectedFieldButtons) { //clears highlight of the buttons
                btn.getStyleClass().remove("field-button-selected");
            }
            selectedFieldButtons.clear();
            setDiceVisible(false);
            moveButton.setDisable(true);
        } catch (IOException e) {
            showError("Failed to move to the field", e.getMessage());
        }
    }

    /**
     * Sends the {@code NEXT} command to the server to skip the current turn. After using this
     * {@link Button} the dice are hidden again as the player has finished his turn.
     */
    @FXML
    private void handleSkip() {
        try {
            protocolWriter.sendCommand(Command.NEXT);
            setDiceVisible(false);
        } catch (IOException e) {
            showError("Failed to skip your turn", e.getMessage());
        }
    }

    /**
     * This method opens a dialog to ask the user if they want to leave the server. If the user wants
     * to leave a {@code QCNF} command is sent over the {@link ProtocolWriterClient} to the server and the window closes.
     */
    @FXML
    private void handleLeave() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LeaveLobbyDialogTemplate.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Leave Server");
            dialogStage.setScene(new Scene(dialogPane));

            LeaveLobbyDialogController controller = loader.getController(); // sets controller
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
     * This method opens a dialog to ask the user if they want to leave the lobby. If the user wants
     * to leave the {@code switchToWelcomeLobby} method is called and the window closes.
     */
    @FXML
    private void handleLeaveLobby() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LeaveLobbyDialogTemplate.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Leave Lobby");
            dialogStage.setScene(new Scene(dialogPane));

            LeaveLobbyDialogController controller = loader.getController(); // sets controller
            controller.setDialogStage(dialogStage);
            controller.setLeaveStatement("Would you like to leave this lobby?");

            dialogStage.showAndWait();

            if (controller.isLeaving) {
                protocolWriter.sendJoin("Welcome");
            }
        } catch (IOException e) {
            showError("Failed to open leave lobby dialog", e.getMessage());
        }
    }

    /**
     * This method transitions the scene to the WelcomeLobby view and sets all instances of
     * the network back to the {@link WelcomeLobbyController}.
     */
    public void switchToWelcomeLobby() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/WelcomeLobbyTemplate.fxml"));
                BorderPane welcomeLobby = loader.load();

                WelcomeLobbyController welcomeController = loader.getController();
                // Set all instances
                welcomeController.setClient(client);
                welcomeController.setPrimaryStage(primaryStage);
                welcomeController.setProtocolWriter(client.getProtocolWriter());
                welcomeController.setNickname(nickname);
                client.setWelcomeController(welcomeController);
                client.getProtocolReader().setWelcomeController(welcomeController);

                // Setup and show the GUI
                primaryStage.setScene(new Scene(welcomeLobby));
                primaryStage.setTitle("Welcome Lobby");
                primaryStage.show();
            } catch (IOException e) {
                showError("Failed to load the WelcomeLobby", e.getMessage());
            }
        });
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
     * Opens the bike selection dialog when joining a lobby or pressing the {@link MenuItem} 'Change
     * Bike'.
     */
    @FXML
    private void handleBikeSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SelectBikeDialogTemplate.fxml"));
            VBox dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Select Your Bike");
            dialogStage.setScene(new Scene(dialogPane));

            SelectBikeDialogController bikeDialogController = loader.getController();
            bikeDialogController.setDialogStage(dialogStage);

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
        Image newBike = bikeImages.get(color.toLowerCase());
        if (newBike == null) {
            return;
        }
        ImageView bikeView = playerBikes.get(nickname);
        if (bikeView != null) {
            bikeView.setImage(newBike);
        }
        // place at starting field:
        updatePlayerPosition(nickname, "white1");
    }

    /**
     * Called when ANY player picks a bike so all users know what bikes the other players have so it
     * can be shown on the gameboard. Creates an ImageView for that player (if not already) and places
     * it on the start field.
     *
     * @param player The player that has chosen a bike color.
     * @param color  The color that the player has chosen for his bike.
     */
    public void addPlayerBike(String player, String color) {
        Platform.runLater(() -> {
            if (playerBikes.containsKey(player)) {
                return;  // already have them
            }
            Image img = bikeImages.get(color.toLowerCase());
            if (img == null) {
                return;
            }
            ImageView iv = new ImageView(img);
            iv.setFitWidth(46);
            iv.setFitHeight(32);
            playerBikes.put(player, iv);
            gameBoard.getChildren().add(iv);
            // place at starting field:
            updatePlayerPosition(player, "white1");
        });
    }

    /**
     * Moves the existing bike ImageView of a player to the right fieldId and sets the rotation
     * so the bike stands in the right direction.
     *
     * @param player  The name of the player that has moved his position.
     * @param fieldId The field to which the player moved his bike.
     */
    public void updatePlayerPosition(String player, String fieldId) {
        ImageView iv = playerBikes.get(player);
        if (iv == null) {
            return;
        }
        Button fld = lookupButton(fieldId);
        if (fld == null) {
            return;
        }
        double x = fld.getLayoutX() + (fld.getWidth() - iv.getFitWidth()) / 2;
        double y = fld.getLayoutY() + (fld.getHeight() - iv.getFitHeight()) / 2;

        // make the bike image use the very same rotation
        DoubleBinding bikeAngle = (DoubleBinding) Bindings
                .when(fld.rotateProperty().isEqualTo(180)).then(0.0)
                .otherwise(fld.rotateProperty());
        iv.rotateProperty().bind(bikeAngle);

        // mirror horizontally when button at 180°
        iv.scaleXProperty().bind(
                Bindings.when(fld.rotateProperty().isEqualTo(180))
                        .then(-1.0)
                        .otherwise(1.0)
        );

        Platform.runLater(() -> iv.relocate(x, y));
    }

    /**
     * Sets the {@link Label} that displays the lobby name.
     *
     * @param name The name of the lobby.
     */
    public void setLobbyName(String name) {
        Platform.runLater(() -> lobbyNameDisplay.setText("Lobby: " + name));
        this.lobbyname = name;
    }

    /**
     * This method sets the host and also sets his special {@link Menu} buttons which only the host can
     * use.
     *
     * @param host The boolean whether the user is the host (first player in the lobby) or not
     */
    public void setHost(boolean host) {
        Platform.runLater(() -> hostButtons.setDisable(!host));
    }

    /**
     * Activates the game board GUI when the game status becomes "running". This method is called when
     * the game officially starts. It ensures that the game board is visible to all players.
     */
    public void gameOngoing() {
        gameBoard.setVisible(true);
        throwDiceButton.setVisible(true);
        moveButton.setVisible(true);
        skipButton.setVisible(true);
        bikeOption.setDisable(true);
    }

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
     * This method updates the list of all players. First it clears the list which is important when a
     * user quits or changes their nickname, and then it reproduces it with the new updated list
     * version.
     *
     * @param players The list of current player names received from the {@link ProtocolReaderClient}.
     */
    public void updatePlayerList(List<String> players) {
        Platform.runLater(() -> {
            try {
                listList.getItems().clear();
                listList.getItems().addAll(players);
            } catch (Exception e) {
                showError("Failed to update player list", e.getMessage());
            }
        });
    }

    /**
     * This method merges and updates the list of all games by lobby name. It calls the method {@code gameOngoing} when
     * the lobby has the status 'running' so it sets everything visible that is needed.
     *
     * @param newGames The formatted list of game lobby entries.
     */
    public void updateGameList(List<String> newGames) {
        Platform.runLater(() -> {
            try {
                gameList.getItems().clear();
                gameList.getItems().addAll(newGames);
                for (String entry : newGames) { // checks the users lobby status
                    if (extractLobbyName(entry).equalsIgnoreCase(lobbyname)) {
                        int end = entry.indexOf("]");
                        String status = entry.substring(end + 1).trim();
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
     * This method merges and updates the list of all lobbies and their members by lobby name. It also
     * sets the host of the lobby by looking at the first name in the list in the corresponding
     * lobby.
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
                            && players.getFirst().equalsIgnoreCase(nickname);
                    setHost(amHost);
                }
            } catch (Exception e) {
                showError("Failed to update lobby list", e.getMessage());
            }
        });
    }

    /**
     * Extracts a list of player names from a formatted lobby entry string. The expected input format
     * is for example: {@code "[Lobby: LobbyName] Players: Jana | Ana | Flo | Julia"}. This
     * method will extract the part after {@code "Players:"} and split
     * the names by the pipe character {@code |}, trimming any extra spaces.
     *
     * @param entry The full lobby entry string containing the list of players.
     * @return A list of player names. Returns an empty list if the format is invalid or no players found.
     */
    private List<String> extractPlayers(String entry) {
        int idx = entry.indexOf("Players:");
        if (idx < 0) {
            return List.of();
        }
        String after = entry.substring(idx + "Players:".length()).trim();
        if (after.startsWith("[") && after.endsWith("]")) {
            after = after.substring(1, after.length() - 1);
        }
        if (after.isBlank()) {
            return List.of();
        }
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
        if (entry == null || !entry.contains("[Lobby: ")) {
            return "";
        }
        int start = entry.indexOf("[Lobby: ") + 8;
        int end = entry.indexOf("]", start);
        if (end <= start) {
            return "";
        }
        return entry.substring(start, end).trim();
    }

    /**
     * Handles a field selection or deselection when a field {@link Button} is clicked. If the clicked
     * {@link Button} is already in the set of {@code selectedFieldButtons}, it sends a {@code DEOS}
     * (deselect) command to the server. Otherwise, it sends a {@code CHOS} (select) command to the
     * server. This method does not update the UI immediately. It waits for confirmation from the
     * server before highlighting or removing the highlighting from the {@link Button} via
     * {@link #highlightField(String)} or {@link #unhighlightField(String)}.
     *
     * @param event The action event triggered by clicking a field {@link Button}.
     */
    @FXML
    public void handleFieldChoice(ActionEvent event) {
        Button btn = (Button) event.getSource();
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

    /**
     * Highlights a field {@link Button} in the GUI by its field ID. This method is called when the
     * server confirms a field selection with the command {@code CHOS}. The corresponding
     * {@link Button} will be visually marked and tracked in {@code selectedFieldButtons}.
     *
     * @param fieldId The {@code fx:id} of the {@link Button} representing the selected field.
     */
    public void highlightField(String fieldId) {
        Button btn = lookupButton(fieldId);
        if (btn == null) {
            return;
        }
        if (!selectedFieldButtons.contains(btn)) {
            btn.getStyleClass().add("field-button-selected");

        }
        selectedFieldButtons.add(btn);
        moveButton.setDisable(false);
    }

    /**
     * Removes the highlight of a field {@link Button} in the GUI by its field ID. This method is
     * called when the server confirms a deselection with the command {@code DEOS}. The corresponding
     * {@link Button} will be visually reset and removed from the {@code selectedFieldButtons}. All fields that
     * have been selected after the field that is now deselected also lose their highlight because they
     * automatically get deselected as well.
     *
     * @param fieldId The {@code fx:id} of the {@link Button} representing the deselected field.
     */
    public void unhighlightField(String fieldId) {
        Button btn = lookupButton(fieldId);
        if (btn == null) {
            return;
        }
        int idx = selectedFieldButtons.indexOf(btn);
        if (idx == -1) {
            return; // not in our list
        }
        for (int i = selectedFieldButtons.size() - 1; i >= idx; i--) {
            Button toUnhighlight = selectedFieldButtons.get(i);
            toUnhighlight.getStyleClass().remove("field-button-selected");
            selectedFieldButtons.remove(i);
        }
        moveButton.setDisable(selectedFieldButtons.isEmpty());
    }

    /**
     * Looks up a {@link Button} field in the controller using reflection based on its {@code fx:id}.
     * This method is used to dynamically access field buttons from their string ID values (e.g.,
     * "red1", "yellow2").
     *
     * @param fieldId The {@code fx:id} of the desired button.
     * @return The {@link Button} instance matching the given ID, or {@code null} if not found.
     */
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/HighscoreListDialogTemplate.fxml"));
            VBox dialogPane = fxmlLoader.load();

            Stage dialogStage = new Stage();
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
}
