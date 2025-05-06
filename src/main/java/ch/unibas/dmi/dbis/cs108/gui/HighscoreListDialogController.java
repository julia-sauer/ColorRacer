package ch.unibas.dmi.dbis.cs108.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller class for the Highscore List Dialog.
 * <p>
 * This controller is responsible for displaying the content of the {@code highscore.txt} file
 * in a JavaFX {@link ListView}. It is tied to the {@code HighscoreListDialogTemplate.fxml} layout.
 * </p>
 *
 * @author julia
 */
public class HighscoreListDialogController {

    /**
     * The stage representing the dialog window.
     */
    private Stage dialogStage;

    /**
     * The {@link ListView} that displays the highscore entries.
     */
    @FXML
    public ListView<String> highscoreListView;

    private static HighscoreListDialogController instance;
    public static HighscoreListDialogController getInstance() {
        return instance;
    }

    /** this will be called by FXMLLoader when it creates the controller */
    public HighscoreListDialogController() {
        instance = this;
    }

    /**
     * Sets the dialog stage used for this controller.
     *
     * @param dialogStage The {@link Stage} object for the dialog.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Initializes the controller after the FXML file has been loaded.
     * This method automatically loads the highscores from the file and populates the list.
     */
    @FXML
    private void initialize() {
        highscoreListView.setItems(FXCollections.observableArrayList());
    }

    public void setHighscoreList(List<String> highscoreList) {
        // make sure we’re on FX‑thread
        Platform.runLater(() -> {
            highscoreListView.setItems(FXCollections.observableArrayList(highscoreList));
        });
    }

    /**
     * Handles the action to close the dialog window.
     */
    @FXML
    public void handleClose() {
        dialogStage.close();
    }
}
