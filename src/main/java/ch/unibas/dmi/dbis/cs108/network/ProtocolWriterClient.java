package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.gui.GameLobbyController;
import ch.unibas.dmi.dbis.cs108.gui.WelcomeLobbyController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * The {@code ProtocolWriterClient} class handles the client's outgoing communication. It formats
 * and sends protocol-specific commands (defined in {@link Command}) to the server. All data is sent
 * in UTF-8 encoding to ensure cross-platform compatibility.
 */
public class ProtocolWriterClient {

  /**
   * The {@link PrintWriter} for sending messages via the network connection. This Writer writes
   * protocol commands (e.g. {@code CHAT}) in UTF-8 to the server.
   */
  private final PrintWriter writer; //Der Writer ist "final", weil er nach der Initialisierung nicht mehr verändert wird.

  /**
   * Logger for logging errors or debugging information.
   */
  private static final Logger LOGGER = LogManager.getLogger(ProtocolWriterClient.class);

  private WelcomeLobbyController welcomeLobbyController; // Reference to the GUI welcomeLobbyController
  private GameLobbyController gameLobbyController;

  /**
   * Constructs a new {@code ProtocolWriterClient} with UTF-8 encoding.
   *
   * @param outputStream The OutputStream to which the messages are to be sent.
   * @throws IOException If an error occurs when creating the PrintWriter.
   */
  public ProtocolWriterClient(OutputStream outputStream) throws IOException {
    writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
  }


  /**
   * The method {@code sendChat} is used for the chat. It converts a chat message entered by the
   * user(e.g. {@code message Hallo!}) into a valid protocol command of the format
   * {@code CHAT <message>} and sends it to the server. It also detects if it is a whisper or a
   * broadcast message and sends it accordingly forward.
   *
   * @param message The message entered by the user.
   */
  public void sendChat(String message) {
    if (message == null || message.trim().isEmpty()) {
      System.out.println("Message is null or empty!");
      return;
    }
    if (!message.matches("[a-zA-ZäöüÄÖÜß0-9.,!?_'\\s\\p{So}]{1,500}$")) {
      System.out.println("Message contains illegal characters!");
      return;
    }
    if (message.length() > 500) {
      System.out.println("Message is too long");
      return;
    }
    if (message.startsWith("whisper")) { // checks if the message is private
      sendWhisper(message);
      return;
    }
    if (message.startsWith("broadcast")) { // checks if the message is a broadcast
      String[] parts = message.split(" ", 2);
      String actualMessage = parts[1].trim();
      sendToServer(Command.BROD + Command.SEPARATOR + actualMessage);
      return;
    }
    sendToServer(Command.CHAT + Command.SEPARATOR + message);
  }

  /**
   * Sends a basic command (e.g., {@code QUIT}, {@code PING}) to the server.
   *
   * @param command The command that should be sent
   * @throws IOException If the message could not be delivered.
   */
  public void sendCommand(Command command) throws IOException {
    sendToServer(command + Command.SEPARATOR);
    //System.out.println(command + Command.SEPARATOR + "sent");
  }

  /**
   * Sends a command with an additional string to the server.
   *
   * @param command The command that should be sent to the server.
   * @param text    The string that should be sent in addition to the command.
   * @throws IOException If there is an error while sending the command with the message to the
   *                     server.
   */
  public void sendCommandAndString(Command command, String text) throws IOException {
    sendToServer(command + Command.SEPARATOR + text);
    //System.out.println(command + Command.SEPARATOR + text + " sent");
  }

  /**
   * Sends a {@code NICK} command to the server with the new nickname.
   *
   * @param newnickname The new nickname that the user selected.
   */
  public void changeNickname(String newnickname) {
    try {
      sendCommandAndString(Command.NICK, newnickname);
    } catch (IOException e) {
      System.err.println("Error, could not send NICK " + newnickname + " to Server");
    }
  }

  /**
   * Sends a {@code QUIT} command to the server indicating client termination.
   */
  public void leave() {
    try {
      sendCommand(Command.QUIT);
    } catch (IOException e) {
      System.err.println("Error, could not send QUIT to Server");
    }
  }

  /**
   * Sends a {@code JOIN} command to the server to enter a specific lobby
   *
   * @param lobbyName The nickname entered by the user
   */
  public void sendJoin(String lobbyName) {
    if (lobbyName == null || lobbyName.trim().isEmpty()) {
      System.out.println("LobbyName is null or empty!");
      return;
    }
    if (lobbyName.length() > 30) {
      System.out.println(" LobbyName is too long");
      return;
    }
    if (!lobbyName.matches("^[a-zA-Z0-9_äöüÄÖÜß]{1,50}$")) {
      System.out.println("lobbyName must be 3–50 characters, only letters, digits, or _");
      return;
    }
    // Sends: JOIN <nickname>
    sendToServer(Command.JOIN + Command.SEPARATOR + lobbyName);
  }

  /**
   * This method sends a raw string to the server.
   *
   * @param message The message that the server should receive.
   */
  public void sendToServer(String message) {
    //LOGGER.error("client: {}", message);
    writer.println(message);
    writer.flush();
    //System.exit(1);
  }

  /**
   * Sends a private message (whisper) to another user. The message must follow the pattern:
   * {@code whisper <nickname> <message>}.
   *
   * @param nicknameAndMessage The nickname of the user, who needs to receive the message.
   */
  public void sendWhisper(String nicknameAndMessage) {

    if (nicknameAndMessage == null || nicknameAndMessage.trim().isEmpty()) {
      System.out.println("Usage: whisper <receiver> <message>");
      return;
    }

    String[] parts = nicknameAndMessage.split(" ", 3);
    if (parts.length < 3) {
      System.out.println("Usage: whisper <receiver> <message>");
      return;
    }

    String receiverNickname = parts[1].trim();
    String message = parts[2].trim();

    if (message == null || message.trim().isEmpty()) {
      System.out.println("Message is null or empty!");
      return;
    }
    if (!message.matches("[a-zA-ZäöüÄÖÜß0-9.,!?_'\\s\\p{So}]{1,500}$")) {
      System.out.println("Message contains illegal characters!");
      return;
    }
    if (message.length() > 500) {
      System.out.println("Message is too long");
      return;
    }
    sendToServer(Command.WISP + Command.SEPARATOR + receiverNickname + Command.SEPARATOR + message);

    // Display whisper in sender's GUI
    if (welcomeLobbyController != null) {
      welcomeLobbyController.displayChat("Whisper sent to " + receiverNickname + ": " + message);
    }
  }
  // Create a whisper message with recipientId and message content
  // Send this message to the server

  /**
   * Sends a field selection command with a specified {@code fieldId}.
   *
   * @param command The protocol command ({@code CHOS} or {@code DEOS}) to be sent to the server.
   * @param fieldId Contains the ID of the chosen field.
   */
  public void sendFieldChoice(Command command, String fieldId) {
    sendToServer(command + Command.SEPARATOR + fieldId);
  }

  /**
   * Sends a {@code VELO} command with the selected color of the bike.
   *
   * @param color The selected color; "black", "green", "magenta", or "darkblue"
   */
  public void sendBikeColor(String color) {
    sendToServer(Command.VELO + Command.SEPARATOR + color);
  }

  /**
   * Marks the player as ready by sending a {@code RADY} command to the server.
   *
   * @throws IOException If there is an error while sending the command to the server.
   */
  public void sendReadyStatus() throws IOException {
    sendCommand(Command.RADY);
  }

  /**
   * Sets the {@link WelcomeLobbyController} that will be used to update the GUI with incoming
   * messages. This method is called during the initialization of the GUI, ensuring that the
   * {@link ProtocolWriterClient} can forward them to the GUI for display.
   *
   * @param controller The {@link WelcomeLobbyController} instance to be set.
   */
  public void setWelcomeController(WelcomeLobbyController controller) {
    this.welcomeLobbyController = controller;
  }

  /**
   * Sets the {@link GameLobbyController} that will be used to update the GUI with incoming
   * messages. This method is called during the change from the WelcomeLobby to the GameLobby and
   * ensures that the {@link ProtocolWriterClient} can forward messages to the GUI for display.
   *
   * @param controller The {@link GameLobbyController} instance to be set.
   */
  public void setGameLobbyController(GameLobbyController controller) {
    this.gameLobbyController = controller;
  }
}


