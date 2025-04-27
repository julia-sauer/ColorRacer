package ch.unibas.dmi.dbis.cs108.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;
import java.nio.file.Paths;

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
     * The file path of where the {@code highscore.txt} file is saved.
     */
    private static final String FILE_PATH = getHighscoreFilePath();

    /**
     * The stage representing the dialog window.
     */
    private Stage dialogStage;

    /**
     * The {@link ListView} that displays the highscore entries.
     */
    @FXML
    public ListView<String> highscoreListView;

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
        loadHighscores();
    }

    private static String getHighscoreFilePath() {
        String basePath = Paths.get("..", "..").toAbsolutePath().normalize().toString();
        return Paths.get(basePath, "src", "main", "resources", "highscore.txt").toString();
    }

    /**
     * Loads the highscores from {@code highscore.txt} and displays them in the {@link ListView}.
     * <p>
     * If the file does not exist or an error occurs during reading, an appropriate message is shown instead.
     * </p>
     */
    private void loadHighscores() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
            List<String> lines = reader.lines().toList();
            if (lines.isEmpty()) {
                highscoreListView.setItems(FXCollections.observableArrayList("Highscore list is empty."));
            } else {
                highscoreListView.setItems(FXCollections.observableArrayList(lines));
            }
        } catch (IOException e) {
            highscoreListView.setItems(FXCollections.observableArrayList("Error loading highscore: " + e.getMessage()));
        }
    }

    /**
     * Handles the action to close the dialog window.
     */
    @FXML
    public void handleClose() {
        dialogStage.close();
    }
}
