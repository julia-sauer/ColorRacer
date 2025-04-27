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

    /**
     * The text that asks if the user wants to leave the lobby, respectively the server.
     */
    @FXML
    private Text leaveStatement;

    /**
     * A flag indicating whether the user confirmed leaving the lobby/server. Defaults to {@code false},
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

    /**
     * Sets the {@link Text} in the dialog that asks either if the user wants to leave the lobby or
     * if the user wants to leave the server.
     *
     * @param statement The string of the question depending on whether the user wants to leave the lobby or the server.
     */
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