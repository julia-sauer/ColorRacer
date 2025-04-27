package ch.unibas.dmi.dbis.cs108.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * This is the controller class for the "Leave Lobby" confirmation dialog. This dialog is shown when
 * the user wants to leave the current lobby. The user can either confirm the action or cancel it.
 *
 * @author julia
 */
public class LeaveLobbyDialogController {

    /**
     * The stage representing the dialog window.
     */
    private Stage dialogStage;

    @FXML
    private Text leaveStatement;

    /**
     * A flag indicating whether the user confirmed leaving the lobby. Defaults to {@code false},
     * becomes {@code true} when "Leave" is clicked.
     */
    protected boolean isLeaving = false;

    /**
     * Sets the {@link Stage} for the dialog.
     *
     * @param dialogStage The stage of the dialog window.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setLeaveStatement(String statement) {
        leaveStatement.setText(statement);
    }

    /**
     * Handles the "Leave" action {@link Button}. Sets {@code isLeaving} to {@code true} and closes
     * the dialog.
     */
    @FXML
    public void handleLeave() {
        isLeaving = true;
        dialogStage.close(); // Closes the dialog
    }

    /**
     * Handles the "No" action {@link Button}. Simply closes the dialog without changing
     * {@code isLeaving}.
     */
    @FXML
    public void handleNo() {
        dialogStage.close(); // Closes the dialog without leaving
    }
}
