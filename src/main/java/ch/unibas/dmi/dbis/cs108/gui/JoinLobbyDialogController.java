package ch.unibas.dmi.dbis.cs108.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.util.List;

public class JoinLobbyDialogController {

    @FXML
    private ListView<String> availableLobbies;

    private Stage dialogStage;
    private String selectedLobby;
    private WelcomeLobbyController welcomeLobbyController;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setWelcomeLobbyController(WelcomeLobbyController controller) {
        this.welcomeLobbyController = controller;
    }

    public void setAvailableLobbies(List<String> lobbyNames) {
        availableLobbies.setItems(FXCollections.observableArrayList(lobbyNames));
    }

    public String getSelectedLobby() {
        return selectedLobby;
    }

    @FXML
    private void handleJoin() {
        selectedLobby = availableLobbies.getSelectionModel().getSelectedItem();
        dialogStage.close(); // Closes the dialog
    }

    @FXML
    private void handleCancel() {
        selectedLobby = null;
        dialogStage.close(); // Closes the dialog without joining
    }
}
