package ch.unibas.dmi.dbis.cs108.server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;

/**
 * This class writes in to the highscore file when the winner order is determined.
 *
 * @author Jana
 */
public class Highscore {

    private static final String FILE_PATH = getHighscoreFilePath();
    private final List<String> highscoreList;
    private Integer gameNumber;

    /**
     * The constructor of the Highscore class
     */
    public Highscore() {
        highscoreList = new ArrayList<>();
        gameNumber = 1;
        loadHighscore();
    }

    /**
     * This method creates the correct file path of the highscore.txt file.
     *
     * @return the file path of the highscore.txt
     */
    private static String getHighscoreFilePath() {
        String basePath = Paths.get("..", "..").toAbsolutePath().normalize().toString();
        return Paths.get(basePath, "src", "main", "resources", "highscore.txt").toString();
    }

    /**
     * This method loads/reads the highscore.txt file. If the line starts with "Spiel" the game number is incremented by one.
     */
    private void loadHighscore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                highscoreList.add(line);
                if (line.startsWith("Spiel")) {
                    gameNumber++;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Highscore file not found. A new one is created.");
        } catch (IOException e) {
            System.err.println("Error while reading Highscore file");
        }
    }

    /**
     * This methods adds a new entry in to the highscore.txt. And calls the method saveHighscore.
     *
     * @param lobbyname The name of the lobby in which the game was played.
     * @param winners   The list of the winner order.
     */
    public synchronized void addHighscoreEntry(String lobbyname, List<String> winners) {
        highscoreList.add("Spiel " + gameNumber + " (" + lobbyname + "):");
        for (int i = 0; i < winners.size(); i++) {
            highscoreList.add((i + 1) + ". place: " + winners.get(i));
        }
        gameNumber++;
        saveHighscore();
    }

    /**
     * This mehtod save the highscore.txt. It acutally writes the entry in to the file.
     */
    private void saveHighscore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String entry : highscoreList) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error while saving Highscore");
        }
    }
}