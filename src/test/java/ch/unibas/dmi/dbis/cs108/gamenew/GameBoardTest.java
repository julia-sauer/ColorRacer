package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.game.Field;
import ch.unibas.dmi.dbis.cs108.game.GameBoard;
import ch.unibas.dmi.dbis.cs108.server.Server;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link GameBoard} class. Covers all functional branches, edge cases, and
 * ensures full code coverage for game logic.
 */
class GameBoardTest {

  GameBoard board;

  /**
   * Initializes a fresh GameBoard before each test and resets the server colors.
   */
  @BeforeEach
  void setUp() {
    board = new GameBoard();
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", "red", "purple"};
  }

  /**
   * Verifies that the default starting field is "white1".
   */
  @Test
  void testInitialCurrentFieldIsWhite1() {
    assertEquals("white1", board.getCurrentField().getFieldId());
  }

  /**
   * Checks if setting the current field manually works.
   */
  @Test
  void testSetCurrentFieldChangesField() {
    Field newField = board.getFieldById("blue1");
    board.setCurrentField(newField);
    assertEquals("blue1", board.getCurrentField().getFieldId());
  }

  /**
   * Ensures that a selected field can be moved to and that the selection clears afterward.
   */
  @Test
  void testAddAndMoveToLastSelected() {
    Field target = board.getFieldById("purple1");
    board.addSelectedField(target);
    assertFalse(board.selectedFieldsEmpty());
    board.moveToLastSelected();
    assertEquals("purple1", board.getCurrentField().getFieldId());
    assertTrue(board.selectedFieldsEmpty());
  }

  /**
   * Tests if a valid field with matching color and direct connection is accepted.
   */
  @Test
  void testIsValidFieldReturnsTrueForNeighborAndAvailableColor() {
    boolean valid = board.isValidField("purple1");
    assertTrue(valid);
  }

  /**
   * Tests that a field is rejected if its color is not available anymore.
   */
  @Test
  void testIsValidFieldReturnsFalseIfColorNotAvailable() {
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", "red", null};
    boolean valid = board.isValidField("purple1");
    assertFalse(valid);
  }

  /**
   * Verifies that deselecting a field restores its color and clears the selection.
   */
  @Test
  void testDeselectFieldsRestoresColors() {
    Field f1 = board.getFieldById("purple1");
    board.addSelectedField(f1);
    Server.colors = new String[]{null, "orange", "pink", "yellow", "green", "red", "blue"};
    String newColors = board.deselectFields(f1);
    assertTrue(newColors.contains("purple"));
    assertTrue(board.selectedFieldsEmpty());
  }

  /**
   * Checks if a field connected via selectedField is considered valid.
   */
  @Test
  void testIsValidFieldReturnsTrueForNeighborOfSelectedField() {
    Field selected = board.getFieldById("purple1");
    board.addSelectedField(selected);
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", "red", "purple"};
    boolean valid = board.isValidField("red1");
    assertTrue(valid);
  }

  /**
   * Ensures that removing a selected field updates both internal structures.
   */
  @Test
  void testRemoveSelectedFieldRemovesFromSetAndList() {
    Field f1 = board.getFieldById("purple1");
    board.addSelectedField(f1);
    assertFalse(board.selectedFieldsEmpty());
    assertEquals(f1, board.getLastSelectedField());
    board.removeSelectedField(f1);
    assertTrue(board.selectedFieldsEmpty());
    assertNull(board.getLastSelectedField());
  }

  /**
   * Verifies that {@code inSelectedField()} correctly identifies membership.
   */
  @Test
  void testInSelectedField() {
    Field field = board.getFieldById("purple1");
    assertFalse(board.inSelectedField(field));
    board.addSelectedField(field);
    assertTrue(board.inSelectedField(field));
    board.removeSelectedField(field);
    assertFalse(board.inSelectedField(field));
  }

  /**
   * Ensures a field reachable via selectedField is rejected when the color is unavailable.
   */
  @Test
  void testIsValidFieldReturnsFalseIfColorNotAvailableButConnectedNeighbor() {
    Field selected = board.getFieldById("purple1");
    board.addSelectedField(selected);
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", null, "purple"};
    boolean valid = board.isValidField("red1");
    assertFalse(valid);
  }

  /**
   * Covers the else-branch with {@code return true} when color matches but connection is indirect.
   */
  @Test
  void testIsValidFieldTriggersReturnTrueInElseBranch() {
    Field selected = board.getFieldById("purple1");
    board.addSelectedField(selected);
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", "red", "purple"};
    boolean valid = board.isValidField("red1");
    assertTrue(valid);
  }

  /**
   * Verifies rejection when field is neither direct nor connected.
   */
  @Test
  void testIsValidFieldReturnsFalseIfNotConnectedAtAll() {
    Field distant = board.getFieldById("blue10");
    Server.colors = new String[]{"blue", "orange", "pink", "yellow", "green", "red", "purple"};
    boolean valid = board.isValidField("blue10");
    assertFalse(valid);
  }

  /**
   * Confirms that selected field list is initially empty.
   */
  @Test
  void testSelectedFieldsEmptyWhenListEmpty() {
    assertTrue(board.selectedFieldsEmpty());
  }

  /**
   * Ensures {@code getLastSelectedField()} returns null if no field is selected.
   */
  @Test
  void testGetLastSelectedFieldReturnsNullIfEmpty() {
    assertNull(board.getLastSelectedField());
  }

  /**
   * Ensures {@code moveToLastSelected()} does nothing if no field is selected.
   */
  @Test
  void testMoveToLastSelectedDoesNothingIfEmpty() {
    Field start = board.getCurrentField();
    board.moveToLastSelected();
    assertEquals(start, board.getCurrentField());
  }

  /**
   * Tests the full removal of all selected fields starting from a middle field.
   */
  @Test
  void testDeselectFieldsRemovesMultipleFieldsCorrectly() {
    Field first = board.getFieldById("purple1");
    Field second = board.getFieldById("red1");
    board.addSelectedField(first);
    board.addSelectedField(second);

    Server.colors = new String[]{null, "orange", "pink", "yellow", "green", "red", "purple"};

    String newColors = board.deselectFields(first);

    assertTrue(newColors.contains("purple"));
    assertTrue(newColors.contains("red"));
    assertTrue(board.selectedFieldsEmpty());
  }

  /**
   * Ensures that deselecting a non-existent field has no effect.
   */
  @Test
  void testDeselectFieldsWhenFieldNotFound() {
    Field unselectedField = new Field("nonexistent1", "yellow");

    String beforeColors = Arrays.toString(Server.colors);

    String afterColors = board.deselectFields(unselectedField);

    assertEquals(beforeColors, afterColors,
        "Colors should remain unchanged if field was not found");
  }
}
