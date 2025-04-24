package ch.unibas.dmi.dbis.cs108.server;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Highscore} class. These tests ensure that highscore entries are
 * properly added to and read from the file.
 */
class HighscoreTest {

  private static final String FILE_PATH = "highscore.txt";
  private static final String BACKUP_PATH = "highscore_backup.txt";

  /**
   * Creates a backup of the original highscore file before each test and prepares a controlled file
   * content for testing.
   */
  @BeforeEach
  void backupOriginalFile() throws IOException {
    File original = new File(FILE_PATH);
    File backup = new File(BACKUP_PATH);

    // If the original file exists, back it up
    if (original.exists()) {
      Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } else {
      // If not, create an empty backup file
      backup.createNewFile();
    }

    // Prepare initial content for the test file
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
      writer.write("Spiel 1 (TestLobby):\n");
      writer.write("1. place: Alice\n");
      writer.write("2. place: Bob\n");
    }
  }

  /**
   * Restores the original highscore file after each test by copying from the backup.
   */
  @AfterEach
  void restoreOriginalFile() throws IOException {
    File original = new File(FILE_PATH);
    File backup = new File(BACKUP_PATH);

    if (backup.exists()) {
      Files.copy(backup.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
      backup.delete(); // Clean up after test
    }
  }

  /**
   * Tests whether a new highscore entry is correctly appended to the file.
   */
  @Test
  void testAddHighscoreEntry() {
    Highscore highscore = new Highscore();
    highscore.addHighscoreEntry("NewLobby", List.of("Charlie", "Dana"));

    // Verify content was added correctly
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
      List<String> lines = reader.lines().toList();

      assertTrue(lines.contains("Spiel 2 (NewLobby):"));
      assertTrue(lines.contains("1. place: Charlie"));
      assertTrue(lines.contains("2. place: Dana"));
    } catch (IOException e) {
      fail("IOException while reading test highscore file: " + e.getMessage());
    }
  }

  /**
   * Verifies that the highscore counter is properly incremented when a new entry is added.
   */
  @Test
  void testLoadHighscore() {
    Highscore highscore = new Highscore();
    highscore.addHighscoreEntry("AnotherLobby", List.of("Eva"));

    // Confirm that the second entry (Spiel 2) is properly written
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
      List<String> lines = reader.lines().toList();
      assertTrue(lines.contains("Spiel 2 (AnotherLobby):"));
      assertTrue(lines.contains("1. place: Eva"));
    } catch (IOException e) {
      fail("IOException while reading test highscore file: " + e.getMessage());
    }
  }
}
