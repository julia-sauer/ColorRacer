package ch.unibas.dmi.dbis.cs108.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class LeaveLobbyDialogController {

    private Stage dialogStage;
    private WelcomeLobbyController welcomeLobbyController;
    private GameLobbyController gameLobbyController;
    protected boolean isLeaving = false;

    public void setWelcomeLobbyController(WelcomeLobbyController controller) {
        this.welcomeLobbyController = controller;
    }

    public void setGameLobbyController(GameLobbyController controller) {
        this.gameLobbyController = controller;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    public void handleLeave(ActionEvent actionEvent) {
        isLeaving = true;
        dialogStage.close(); // Closes the dialog
    }

    @FXML
    public void handleNo(ActionEvent actionEvent) {
        dialogStage.close(); // Closes the dialog without leaving
    }
}
