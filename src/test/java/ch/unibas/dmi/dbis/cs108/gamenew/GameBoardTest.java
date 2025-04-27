package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.game.Field;
import ch.unibas.dmi.dbis.cs108.game.GameBoard;
import ch.unibas.dmi.dbis.cs108.server.Server;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  /**
   * Test to ensure that if a field has no connections, it should be considered invalid.
   * This test checks that a field with no neighbors is deemed invalid when trying to validate it.
   */
  @Test
  void testIsValidFieldReturnsFalseIfNoConnection() {
    // Try to validate a field that has no connection to others
    boolean result = board.isValidField("blue10");

    // Assert that the field should be invalid because it has no connection
    assertFalse(result, "Field should be invalid because it is not connected.");
  }

  /**
   * Test to ensure that if a selected field has no neighbor connection, it should be rejected as invalid.
   * This test verifies that the `isValidField` method correctly identifies a selected field without a valid connection.
   */
  @Test
  void testIsValidFieldSelectedFieldHasNoNeighborConnection() {
    Field selected = board.getFieldById("purple1");
    board.addSelectedField(selected);  // Add the field to the selected list

    // Try to validate a non-neighbor field
    boolean result = board.isValidField("blue10");

    // Assert that it should be invalid since it's not connected to the selected field
    assertFalse(result,
        "Should return false if selectedField has no neighbor connection to targetField");
  }

  /**
   * Test to ensure that deselecting a field removes it from the selected fields list.
   * This test verifies that the field is properly deselected and that the color is restored.
   */
  @Test
  void testDeselectFieldsRemovesFromMatch() {
    // Add a selected field to the board
    Field purple1 = board.getFieldById("purple1");
    board.addSelectedField(purple1);

    // Mocking color restoration
    Server.colors = new String[]{null, "orange", "pink", "yellow", "green", "red", "purple"};

    // Call deselectFields to remove the selected field
    String result = board.deselectFields(purple1);

    // Assert that the color of the deselected field is restored and it is no longer in the selection
    assertTrue(result.contains("purple"), "Color purple should be restored.");
    assertTrue(board.selectedFieldsEmpty(), "Field should be removed from selection.");
  }

  /**
   * Test to ensure that deselecting all matched fields clears all selected fields.
   * This test checks that when multiple fields are selected and one is deselected, all selected fields should be cleared.
   */
  @Test
  void testDeselectFieldsRemovesAllAfterMatchedField() {
    Field purple1 = board.getFieldById("purple1");
    Field red1 = board.getFieldById("red1");
    board.addSelectedField(purple1);
    board.addSelectedField(red1);

    // Set the colors in the server for restoration
    Server.colors = new String[]{null, null, "pink", "yellow", "green", "blue", "orange"};

    // Call deselectFields to clear the selection
    String result = board.deselectFields(purple1);

    // Assert that both purple and red colors are restored and all fields are deselected
    assertTrue(result.contains("purple"), "purple should be restored");
    assertTrue(result.contains("red"), "red should be restored");

    // Assert that all selected fields are cleared after deselection
    assertTrue(board.selectedFieldsEmpty(), "All selected fields should be cleared");
  }

  /**
   * Test to ensure that deselecting a field only restores the color of the deselected field.
   * This test verifies that only the deselected field's color is restored without affecting others.
   */
  @Test
  void testDeselectFieldsCoversFoundEqualsTrueOnly() {
    Field purple1 = board.getFieldById("purple1");
    Field red1 = board.getFieldById("red1");

    // Add fields to the selected list
    board.addSelectedField(purple1);
    board.addSelectedField(red1);

    // Set the colors in the server
    Server.colors = new String[]{null, null, "pink", "yellow", "green", "blue", "orange"};

    // Call deselectFields on purple1
    board.deselectFields(purple1);

    // Deselect the red1 field and check the result
    String result = board.deselectFields(red1);

    // Assert that the color for red is restored
    assertTrue(result.contains("red"), "Color red should be restored");
  }

  /**
   * Test to ensure that deselecting multiple fields properly restores the color and clears the selection.
   * This test ensures that all the selected fields are properly cleared when deselected.
   */
  @Test
  void testDeselectFieldsTriggersFoundTrueAndSkipsEquals() {
    Field f1 = board.getFieldById("purple1");
    Field f2 = board.getFieldById("red1");
    Field f3 = board.getFieldById("blue1");

    // Add selected fields to the board
    board.addSelectedField(f1);
    board.addSelectedField(f2);
    board.addSelectedField(f3);

    // Set the colors in the server
    Server.colors = new String[]{null, null, null, null, null, null, null};

    // Call deselectFields on the first selected field
    String result = board.deselectFields(f1);

    // Assert that all fields are deselected and the colors are restored
    assertTrue(result.contains("purple"), "purple should be restored (direct match)");
    assertTrue(result.contains("red"), "red should be restored (after found=true)");
    assertTrue(result.contains("blue"), "blue should be restored (after found=true)");
    assertTrue(board.selectedFieldsEmpty(), "All selected fields should be cleared");
  }

  /**
   * Test to ensure that null neighbors are skipped when adding neighbors to a field.
   * This test checks that null values are ignored when adding neighbors to a field.
   */
  @Test
  void testAddNeighborSkipsNull() {
    Field field = new Field("test1", "blue");
    field.addNeighbor(null);  // Should be ignored

    // Assert that null is not included in the neighbors list
    assertFalse(field.getNeighbors().contains(null), "null should not be in neighbors");
  }

  /**
   * Test to ensure that fields are correctly handled when their neighbors are null.
   * This test verifies that null neighbors are excluded from the neighbor map.
   */
  @Test
  void testInitializeNeighborsSkipsNullNeighbor() {
    // Modify the neighborMap to include a null neighbor
    Map<String, List<String>> neighborMap = new HashMap<>();
    neighborMap.put("white1", Arrays.asList("purple1", null, "orange1"));

    // Reflectively set the neighborMap to include the null neighbor
    try {
      java.lang.reflect.Field field = GameBoard.class.getDeclaredField("neighborMap");
      field.setAccessible(true);
      field.set(board, neighborMap);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    Field testField = board.getFieldById("white1");

    // Assert that no null values are added as neighbors
    for (Field neighbor : testField.getNeighbors()) {
      assertNotNull(neighbor, "Null should not be added as neighbor");
    }
  }

/**
   * Tests that clearSelectedFields() correctly clears both selectedFields and selectedFieldList.
   */
  @Test
  void testClearSelectedFields() {
    // Add fields to the selection
    Field purple1 = board.getFieldById("purple1");
    Field blue1 = board.getFieldById("blue1");
    board.addSelectedField(purple1);
    board.addSelectedField(blue1);

    // Ensure the fields are added
    assertFalse(board.selectedFieldsEmpty(), "selectedFields should not be empty before clearing");

    // Call clearSelectedFields() to clear the selected fields
    board.clearSelectedFields();

    // Verify that both selectedFields and selectedFieldList are empty
    assertTrue(board.selectedFieldsEmpty(), "selectedFields should be empty after clearing");
    assertNull(board.getLastSelectedField(), "getLastSelectedField should return null after clearing");
  }


}
