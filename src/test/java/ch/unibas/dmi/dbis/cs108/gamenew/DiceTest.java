package ch.unibas.dmi.dbis.cs108.gamenew;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.unibas.dmi.dbis.cs108.game.Dice;

/**
 * Unit tests for the {@link Dice} class. These tests verify the behavior and correctness of the
 * color dice rolls.
 */
public class DiceTest {

  /**
   * Tests whether the dice correctly maps mocked random values to expected colors. This test uses
   * Mockito to simulate deterministic random values and checks if the returned colors match the
   * expected order.
   */
  @Test
  void testDiceRollReturnsCorrectColors() {
    Dice mockDice = mock(Dice.class);

    // Simulate 6 predefined random values to return specific colors.
    // Mapping logic (based on internal random and ceil mapping):
    // 0.2  → ceil(1.2) → index 2 → orange
    // 0.4  → ceil(1.4) → index 3 → red
    // 0.6  → ceil(1.6) → index 4 → pink
    // 0.8  → ceil(1.8) → index 5 → purple
    // 0.99 → ceil(1.99) → index 6 → blue
    // 0.0  → ceil(1.0) → index 1 → yellow
    when(mockDice.getRandom()).thenReturn(
        0.2, 0.4, 0.6, 0.8, 0.99, 0.0
    );

    // Call the real roll() method, using mocked random values
    when(mockDice.roll()).thenCallRealMethod();

    String[] result = mockDice.roll();

    // Validate that the returned color sequence matches the expected output
    assertArrayEquals(new String[]{
        "orange", "red", "pink", "purple", "blue", "yellow"
    }, result);
  }

  /**
   * Tests that the roll() method always returns exactly 6 colors.
   */
  @Test
  void testRollLength() {
    Dice dice = new Dice();
    String[] result = dice.roll();
    assertEquals(6, result.length, "Dice roll should return exactly 6 colors.");
  }

  /**
   * Tests that the dice roll only produces valid color values. Valid colors include: yellow,
   * orange, red, pink, purple, blue.
   */
  @Test
  void testColorsAreValid() {
    Dice dice = new Dice();
    String[] result = dice.roll();
    for (String color : result) {
      assertTrue(
          color.matches("yellow|orange|red|pink|purple|blue"),
          "Unexpected color: " + color
      );
    }
  }
}
