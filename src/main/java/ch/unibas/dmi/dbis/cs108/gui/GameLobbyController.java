package ch.unibas.dmi.dbis.cs108.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

public class GameLobbyController {

    @FXML
    public Label lobbyNameDisplay;
    @FXML
    public TextArea chatArea;
    @FXML
    public TextField txtUsermsg;
    @FXML
    public ListView<String> listlist;
    @FXML
    public ListView<String> gamelist;
    @FXML
    public ListView<String> lobbylist;
    @FXML
    public Button readyButton;
    @FXML
    public Button startButton;
    @FXML
    public Button finishButton;

    @FXML
    private Button throwDiceButton;

    @FXML
    private Button moveButton;

    @FXML
    private Button skipButton;

    @FXML
    private ImageView dice1, dice2, dice3, dice4, dice5, dice6;

    @FXML
    private void handleBroadcast() {
        String message = txtUsermsg.getText().trim();
        if (!message.isEmpty()) {
            chatArea.appendText("You: " + message + "\n");
            txtUsermsg.clear();
            // TODO: Send message to server
        }
    }

    @FXML
    private void handleEnterPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER -> handleBroadcast();
        }
    }

    @FXML
    private void handleReady() {
        // TODO: Implement ready logic
        System.out.println("Player clicked READY.");
    }

    @FXML
    private void handleStart() {
        // TODO: Implement game start logic
        System.out.println("Game START requested.");
    }

    @FXML
    private void handleFinish() {
        // TODO: Implement game finish logic
        System.out.println("Game FINISHED.");
    }

    @FXML
    private void handleThrowDice() {
        // TODO: Dice rolling logic
        System.out.println("DICE thrown.");
    }

    @FXML
    private void handleMoveToField() {
        // TODO: Movement logic
        System.out.println("MOVE to field.");
    }

    @FXML
    private void handleSkip() {
        // TODO: Skip turn logic
        System.out.println("TURN skipped.");
    }

    @FXML
    private void handleLeave() {
        // TODO: Logic to leave the server
        System.out.println("Leaving the server...");
    }

    @FXML
    private void handleNicknameChange() {
        // TODO: Nickname change dialog
        System.out.println("Changing nickname...");
    }

    @FXML
    private void handleBikeSelection() {
        // TODO: Bike selection logic
        System.out.println("Changing bike...");
    }

    // ========== Optional Helper Methods ==========

    public void addToChat(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }

    public void setLobbyName(String name) {
        Platform.runLater(() -> lobbyNameDisplay.setText("Lobby: " + name));
    }

    public void clearChat() {
        Platform.runLater(chatArea::clear);
    }

    public void setDiceVisible(boolean visible) {
        dice1.setVisible(visible);
        dice2.setVisible(visible);
        dice3.setVisible(visible);
        dice4.setVisible(visible);
        dice5.setVisible(visible);
        dice6.setVisible(visible);
    }
}
