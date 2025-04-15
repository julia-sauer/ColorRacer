package ch.unibas.dmi.dbis.cs108.gui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class JoinLobbyDialogController {
    @FXML
    private ListView<String> availableLobbies;

    private Stage dialogStage;
    private String selectedLobby;
    private WelcomeLobbyController welcomeLobbyController;

    @FXML
    public void initialize() {
        // Initialize ListView selection listener
        availableLobbies.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectedLobby = newValue
        );
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setWelcomeLobbyController(WelcomeLobbyController controller) {
        this.welcomeLobbyController = controller;
    }

    public void setAvailableLobbies(ObservableList<String> lobbies) {
        availableLobbies.setItems(lobbies);
    }

    public String getSelectedLobby() {
        return selectedLobby;
    }

    @FXML
    private void handleJoin() {
        if (selectedLobby != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        selectedLobby = null;
        dialogStage.close();
    }
}
