package ch.unibas.dmi.dbis.cs108.server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Highscore {

  private static final String FILE_PATH = "highscore.txt";
  private List<String> highscoreList;
  private Integer gameNumber;

  public Highscore() {
    highscoreList = new ArrayList<>();
    gameNumber = 1;
    loadHighscore();
  }

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

  public synchronized void addHighscoreEntry(String lobbyname, List<String> winners) {
    highscoreList.add("Spiel " + gameNumber + " (" + lobbyname + "):");
    for (int i = 0; i < winners.size(); i++) {
      highscoreList.add((i + 1) + ". place: " + winners.get(i));
    }
    gameNumber++;
    saveHighscore();
  }

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

  public int getGameNumber() {
    return gameNumber;
  }

}