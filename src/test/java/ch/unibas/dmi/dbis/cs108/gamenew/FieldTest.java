package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.game.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Field} class. This class verifies proper initialization, neighbor
 * management, and field identity.
 */
class FieldTest {

  private Field field;
  private Field neighbor;

  /**
   * Set up a base field and a neighboring field before each test.
   */
  @BeforeEach
  void setUp() {
    field = new Field("red1", "red");
    neighbor = new Field("blue1", "blue");
  }

  /**
   * Verifies that a Field is initialized with the correct ID and color, and that it has no
   * neighbors initially.
   */
  @Test
  void testFieldInitialization() {
    assertEquals("red1", field.getFieldId(), "Field ID should be correctly set.");
    assertEquals("red", field.getColor(), "Field color should be correctly set.");
    assertTrue(field.getNeighbors().isEmpty(), "Neighbors should be empty on init.");
  }

  /**
   * Tests that a single neighbor can be added correctly to the Field.
   */
  @Test
  void testAddNeighbor() {
    field.addNeighbor(neighbor);

    Set<Field> neighbors = field.getNeighbors();
    assertEquals(1, neighbors.size(), "Field should have one neighbor.");
    assertTrue(neighbors.contains(neighbor), "Neighbor should be added to the set.");
  }

  /**
   * Tests that multiple neighbors can be added and are tracked correctly.
   */
  @Test
  void testMultipleNeighbors() {
    Field another = new Field("yellow1", "yellow");
    field.addNeighbor(neighbor);
    field.addNeighbor(another);

    assertEquals(2, field.getNeighbors().size(), "Field should have two neighbors.");
    assertTrue(field.getNeighbors().contains(neighbor));
    assertTrue(field.getNeighbors().contains(another));
  }
}
