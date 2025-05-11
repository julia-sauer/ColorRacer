package ch.unibas.dmi.dbis.cs108.server;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HighscoreTest {

  @Test
  void testAddSingleEntry() {
    Highscore highscore = new Highscore();
    highscore.addHighscoreEntry("TestUser", 7);

    List<String> list = highscore.getHighscoreList();
    assertFalse(list.isEmpty());
    assertTrue(list.get(0).contains("TestUser"));
  }
}
