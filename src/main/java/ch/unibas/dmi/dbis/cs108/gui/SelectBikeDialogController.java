package ch.unibas.dmi.dbis.cs108.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SelectBikeDialogController {

    @FXML
    private Button blackBike, magentaBike, greenBike, darkblueBike;

    private Stage dialogStage;
    private String selectedBike;
    private GameLobbyController gameLobbyController;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setGameLobby(GameLobbyController controller) {
        this.gameLobbyController = controller;
    }

    @FXML
    private void handleBikeSelection(ActionEvent event) {
        Object source = event.getSource();
        if (source == blackBike) {
            selectedBike = "black";
        } else if (source == magentaBike) {
            selectedBike = "magenta";
        } else if (source == greenBike) {
            selectedBike = "green";
        } else if (source == darkblueBike) {
            selectedBike = "darkblue";
        }
        dialogStage.close();
    }

    public String getSelectedBike() {
        return selectedBike;
    }
}
