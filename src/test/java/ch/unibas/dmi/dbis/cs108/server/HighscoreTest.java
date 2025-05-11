package ch.unibas.dmi.dbis.cs108.server;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the {@link Highscore} class. This test ensures basic functionality of adding a
 * single highscore entry.
 */
class HighscoreTest {

  /**
   * Tests that a single entry can be added to the highscore list, and verifies that it appears
   * correctly in the list.
   */
  @Test
  void testAddSingleEntry() {
    Highscore highscore = new Highscore();
    highscore.addHighscoreEntry("TestUser", 7);

    List<String> list = highscore.getHighscoreList();
    assertFalse(list.isEmpty());
    assertTrue(list.get(0).contains("TestUser"));
  }
}

