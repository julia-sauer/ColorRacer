package ch.unibas.dmi.dbis.cs108.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * This is the controller class for the "Select Bike" dialog. This dialog allows the user to choose
 * a bike color before the game starts. When a color is selected, the dialog closes and the selected
 * color can be retrieved via {@link #getSelectedBike()}.
 *
 * @author julia
 */
public class SelectBikeDialogController {

  /**
   * The {@link Button}s for selecting a bike color.
   */
  @FXML
  private Button blackBike, magentaBike, greenBike, darkblueBike;

  /**
   * The dialog stage in which this controller is used.
   */
  private Stage dialogStage;

  /**
   * The selected bike color.
   */
  private String selectedBike;

  /**
   * Sets the dialog stage associated with this controller.
   *
   * @param dialogStage The stage used for the dialog window.
   */

  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  /**
   * Handles the bike selection by determining which {@link Button} was clicked. Sets the selected
   * bike color accordingly and closes the dialog.
   *
   * @param event the {@link ActionEvent} triggered by the {@link Button} press
   */
  @FXML
  private void handleBikeSelection(ActionEvent event) {
    Object source = event.getSource();
    if (source == blackBike) {
      selectedBike = "black";
    } else if (source == magentaBike) {
      selectedBike = "magenta";
    } else if (source == greenBike) {
      selectedBike = "green";
    } else if (source == darkblueBike) {
      selectedBike = "darkblue";
    }
    dialogStage.close();
  }

  /**
   * Returns the bike color selected by the user.
   *
   * @return A string representing the selected bike color, or {@code null} if none was selected.
   */
  public String getSelectedBike() {
    return selectedBike;
  }
}
