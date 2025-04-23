package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.game.Field;
import ch.unibas.dmi.dbis.cs108.game.GameBoard;
import ch.unibas.dmi.dbis.cs108.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {

  GameBoard board;

  @BeforeEach
  void setUp() {
    board = new GameBoard();
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", "red", "purple"};
  }

  @Test
  void testInitialCurrentFieldIsWhite1() {
    assertEquals("white1", board.getCurrentField().getFieldId());
  }

  @Test
  void testSetCurrentFieldChangesField() {
    Field newField = board.getFieldById("blue1");
    board.setCurrentField(newField);
    assertEquals("blue1", board.getCurrentField().getFieldId());
  }

  @Test
  void testAddAndMoveToLastSelected() {
    Field target = board.getFieldById("purple1"); // neighbor of white1
    board.addSelectedField(target);
    assertFalse(board.selectedFieldsEmpty());

    board.moveToLastSelected();
    assertEquals("purple1", board.getCurrentField().getFieldId());
    assertTrue(board.selectedFieldsEmpty());
  }

  @Test
  void testIsValidFieldReturnsTrueForNeighborAndAvailableColor() {
    boolean valid = board.isValidField("purple1"); // purple is in colors[], neighbor of white1
    assertTrue(valid);
  }

  @Test
  void testIsValidFieldReturnsFalseIfColorNotAvailable() {
    // simulate that purple is no longer available
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", "red", null};

    boolean valid = board.isValidField("purple1");
    assertFalse(valid);
  }

  @Test
  void testDeselectFieldsRestoresColors() {
    Field f1 = board.getFieldById("purple1");
    board.addSelectedField(f1);

    // simulate move (consume a color)
    Server.colors = new String[]{null, "orange", "pink", "yellow", "green", "red", "blue"};

    String newColors = board.deselectFields(f1);
    assertTrue(newColors.contains("purple"));
    assertTrue(board.selectedFieldsEmpty());
  }

  @Test
  void testIsValidFieldReturnsTrueForNeighborOfSelectedField() {
    // Aktuelles Feld: white1
    // white1 → purple1 (direkter Nachbar)
    // purple1 → red1 (indirekter Nachbar, über selectedField erreichbar)

    // 1. Füge purple1 als selected field hinzu
    Field selected = board.getFieldById("purple1");
    board.addSelectedField(selected);

    // 2. Stelle sicher, dass die Farbe "red" verfügbar ist
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", "red", "purple"};

    // 3. Teste red1 (Nachbar von purple1, aber nicht direkt von white1)
    boolean valid = board.isValidField("red1");

    assertTrue(valid, "red1 should be valid because it is neighbor of selected field purple1");
  }

}

