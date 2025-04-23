package ch.unibas.dmi.dbis.cs108.server;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HighscoreTest {

  private static final String FILE_PATH = "highscore.txt";
  private static final String BACKUP_PATH = "highscore_backup.txt";

  @BeforeEach
  void backupOriginalFile() throws IOException {
    File original = new File(FILE_PATH);
    File backup = new File(BACKUP_PATH);

    if (original.exists()) {
      Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } else {
      backup.createNewFile();
    }

    // Inhalt vorbereiten
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
      writer.write("Spiel 1 (TestLobby):\n");
      writer.write("1. place: Alice\n");
      writer.write("2. place: Bob\n");
    }
  }

  @AfterEach
  void restoreOriginalFile() throws IOException {
    File original = new File(FILE_PATH);
    File backup = new File(BACKUP_PATH);

    if (backup.exists()) {
      Files.copy(backup.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
      backup.delete();
    }
  }

  @Test
  void testAddHighscoreEntry() {
    Highscore highscore = new Highscore();
    highscore.addHighscoreEntry("NewLobby", List.of("Charlie", "Dana"));

    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
      List<String> lines = reader.lines().toList();

      assertTrue(lines.contains("Spiel 2 (NewLobby):"));
      assertTrue(lines.contains("1. place: Charlie"));
      assertTrue(lines.contains("2. place: Dana"));
    } catch (IOException e) {
      fail("IOException while reading test highscore file: " + e.getMessage());
    }
  }

  @Test
  void testLoadHighscore() {
    Highscore highscore = new Highscore();
    // Sollte 2 sein, da bereits "Spiel 1" vorhanden war
    highscore.addHighscoreEntry("AnotherLobby", List.of("Eva"));
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
      List<String> lines = reader.lines().toList();
      assertTrue(lines.contains("Spiel 2 (AnotherLobby):"));
      assertTrue(lines.contains("1. place: Eva"));
    } catch (IOException e) {
      fail("IOException while reading test highscore file: " + e.getMessage());
    }
  }
}
