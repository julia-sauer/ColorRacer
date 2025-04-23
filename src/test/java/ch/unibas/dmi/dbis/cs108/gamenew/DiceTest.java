package ch.unibas.dmi.dbis.cs108.gamenew;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.unibas.dmi.dbis.cs108.game.Dice;


public class DiceTest {

  @Test
  void testDiceRollReturnsCorrectColors() {
    Dice mockDice = mock(Dice.class);

    // Simuliere 6 definierte Zufallswerte → ergibt: orange, red, pink, purple, blue, yellow
    when(mockDice.getRandom()).thenReturn(
        0.2, // → ceil(1.2) = 2 → orange
        0.4, // → 3 → red
        0.6, // → 4 → pink
        0.8, // → 5 → purple
        0.99, // → 6 → blue
        0.0 // → 1 → yellow
    );
    // Verwende echten roll()-Code, aber mit gemockten Zufallszahlen
    when(mockDice.roll()).thenCallRealMethod();

    String[] result = mockDice.roll();

    assertArrayEquals(new String[]{
        "orange", "red", "pink", "purple", "blue", "yellow"
    }, result);
  }

  @Test
  void testRollLength() {
    Dice dice = new Dice();
    String[] result = dice.roll();
    assertEquals(6, result.length);
  }

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
