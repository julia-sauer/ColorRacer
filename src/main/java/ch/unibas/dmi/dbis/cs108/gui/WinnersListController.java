package ch.unibas.dmi.dbis.cs108.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class WinnersListController {

    @FXML private StackPane rootPane;  // the container from FXML
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
     * Populates the {@link ListView} with the podium ranks.
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

    public void launchConfettiFromSides() {
        confetti.burstFromSides(60);
    }

    /**
     * Handles the action to close the dialog window.
     */
    @FXML
    public void handleClose() {
        dialogStage.close();
    }

}
