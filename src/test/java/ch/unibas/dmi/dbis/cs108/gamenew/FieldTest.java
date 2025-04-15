package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.game.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    private Field field;
    private Field neighbor;

    @BeforeEach
    void setUp() {
        field = new Field("red1", "red");
        neighbor = new Field("blue1", "blue");
    }

    @Test
    void testFieldInitialization() {
        assertEquals("red1", field.getFieldId());
        assertEquals("red", field.getColor());
        assertTrue(field.getNeighbors().isEmpty(), "Neighbors should be empty on init");
    }

    @Test
    void testAddNeighbor() {
        field.addNeighbor(neighbor);

        Set<Field> neighbors = field.getNeighbors();
        assertEquals(1, neighbors.size());
        assertTrue(neighbors.contains(neighbor));
    }

    @Test
    void testMultipleNeighbors() {
        Field another = new Field("yellow1", "yellow");
        field.addNeighbor(neighbor);
        field.addNeighbor(another);

        assertEquals(2, field.getNeighbors().size());
    }
}
