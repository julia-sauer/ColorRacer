package ch.unibas.dmi.dbis.cs108.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

/**
 * This is the controller class for handling the "Join" lobby dialog window. This controller allows
 * users to view a list of available lobbies, select one to join, or cancel the dialog.
 *
 * @author julia
 */
public class JoinLobbyDialogController {

    /**
     * The {@link Media} of the click sound that we created.
     */
    private final Media clickMedia =
            new Media(Objects.requireNonNull(getClass().getResource("/audio/Click.mp3")).toExternalForm());
    /**
     * The {@link ListView} showing the names of available lobbies.
     */
    @FXML
    private ListView<String> availableLobbies;
    /**
     * The stage representing the dialog window.
     */
    private Stage dialogStage;
    /**
     * The lobby name selected by the user to join.
     */
    private String selectedLobby;

    /**
     * Sets the dialog stage used for this controller.
     *
     * @param dialogStage The {@link Stage} object for the dialog.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Populates the list view with available lobby names.
     *
     * @param lobbyNames A list of lobby names to display that are available to join.
     */
    public void setAvailableLobbies(List<String> lobbyNames) {
        availableLobbies.setItems(FXCollections.observableArrayList(lobbyNames));
    }

    /**
     * Returns the name of the lobby selected by the user to join.
     *
     * @return The selected lobby name or {@code null} if none was selected.
     */
    public String getSelectedLobby() {
        return selectedLobby;
    }

    /**
     * Handles the "Join" action {@link Button}. Retrieves the selected lobby from the list and closes
     * the dialog.
     */
    @FXML
    private void handleJoin() {
        playClickThen();
        selectedLobby = availableLobbies.getSelectionModel().getSelectedItem();
        dialogStage.close(); // Closes the dialog
    }

    /**
     * Handles the "Cancel" action {@link Button}. Clears any selected lobby and closes the dialog.
     */
    @FXML
    private void handleCancel() {
        playClickThen();
        selectedLobby = null;
        dialogStage.close(); // Closes the dialog without joining
    }

    /**
     * This method plays the sound that we created for a mouse-click or another {@link javafx.event.ActionEvent}.
     */
    /**
     * This method plays the sound that we created for a mouse-click or another {@link javafx.event.ActionEvent}.
     */
    private void playClickThen() {
        try {
            MediaPlayer p = new MediaPlayer(clickMedia);
            p.setOnEndOfMedia(p::dispose);
            p.setVolume(0.5);
            p.play();
        } catch (Exception e) {
            System.err.println("[WARNING] Failed to play click sound: " + e.getMessage());
        }
    }

}