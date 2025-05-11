package ch.unibas.dmi.dbis.cs108.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

/**
 * This is the Controller class for displaying the list of winners in a dialog when the game has been won.
 * This controller manages a dialog stage that shows ranked winners,
 * and it includes a confetti animation for celebratory effects.
 *
 * @author julia
 */
public class WinnersListController {

    /**
     * The root pane container loaded from the FXML file.
     * Used as the parent for GUI elements, including confetti.
     */
    @FXML
    private StackPane rootPane;

    /**
     * The confetti animation component used to celebrate winners.
     */
    private Confetti confetti;
    /**
     * The stage representing the dialog window.
     */
    private Stage dialogStage;

    /**
     * The {@link ListView} that displays the winner ranks.
     */
    @FXML
    public ListView<String> winnersList;

    /**
     * The {@link Media} of the click sound that we created.
     */
    private final Media clickMedia =
            new Media(Objects.requireNonNull(getClass().getResource("/audio/Click.mp3")).toExternalForm());

    /**
     * Populates the {@link ListView} with the winners ranks.
     *
     * @param winners A list of the players that shows their ranks.
     */
    public void setWinnersList(List<String> winners) {
        winnersList.setItems(FXCollections.observableArrayList(winners));
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
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded. It prepares the confetti animation
     * and adds it to the GUI.
     */
    @FXML
    public void initialize() {
        confetti = new Confetti();

        confetti.setPickOnBounds(false);
        confetti.setMouseTransparent(true);

        // Fill entire rootPane
        confetti.prefWidthProperty().bind(rootPane.widthProperty());
        confetti.prefHeightProperty().bind(rootPane.heightProperty());

        rootPane.getChildren().add(confetti); // goes above VBox
    }

    /**
     * Launches a confetti animation bursting from the sides of the screen.
     * Used to visually celebrate the winners.
     */
    public void launchConfettiFromSides() {
        confetti.burstFromSides(150);
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
