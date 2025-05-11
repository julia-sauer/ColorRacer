package ch.unibas.dmi.dbis.cs108.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

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
     * The instance of this controller. Stored so other parts of the client can update the view.
     */
    private static HighscoreListDialogController instance;
    /**
     * The {@link Media} of the click sound that we created.
     */
    private final Media clickMedia = new Media(Objects.requireNonNull(getClass().getResource("/audio/Click.mp3")).toExternalForm());
    /**
     * The {@link ListView} that displays the highscore entries.
     */
    @FXML
    public ListView<String> highscoreListView;
    /**
     * The stage representing the dialog window.
     */
    private Stage dialogStage;

    /**
     * Constructor called by FXMLLoader when creating the controller.
     * Stores the created instance for later retrieval.
     */
    public HighscoreListDialogController() {
        instance = this;
    }

    /**
     * Returns the instance of this controller.
     *
     * @return The HighscoreListDialogController instance.
     */
    public static HighscoreListDialogController getInstance() {
        return instance;
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

    /**
     * Updates the ListView with the given highscore entries.
     * <p>
     * Ensures the update runs on the JavaFX Application Thread using {@link Platform#runLater(Runnable)}.
     * </p>
     *
     * @param highscoreList the list of highscore entries to display
     */
    public void setHighscoreList(List<String> highscoreList) {
        // make sure we’re on FX‑thread
        Platform.runLater(() -> highscoreListView.setItems(FXCollections.observableArrayList(highscoreList)));
    }

    /**
     * Handles the action to close the dialog window.
     */
    @FXML
    public void handleClose() {
        playClickThen();
        dialogStage.close();
    }

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
            System.err.println("[WARNING] Failed to play click sound in HighscoreListDialogController: " + e.getMessage());
        }
    }

}
