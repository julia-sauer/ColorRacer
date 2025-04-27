package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.*;
import java.io.*;
import org.mockito.stubbing.OngoingStubbing;

import static ch.unibas.dmi.dbis.cs108.server.Server.protocolWriters;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for testing the rollTheDice method in the Server class.
 * This class contains tests for various scenarios, including game state, player actions, and dice rolls.
 */
class RollTheDiceTest {

  private User mockUser;
  private OutputStream mockOut;
  private ProtocolWriterServer mockWriter;
  private Lobby lobby;
  private GameBoard board;

  /**
   * Setup the test environment before each test.
   * Mocks the user, output stream, and protocol writer, and sets up the game environment.
   */
  @BeforeEach
  void setup() {
    mockOut = mock(OutputStream.class);  // Mock OutputStream
    mockUser = mock(User.class);  // Mock User object

    // Mock User methods
    when(mockUser.getOut()).thenReturn(mockOut);  // Ensuring `getOut()` returns the mock OutputStream
    when(mockUser.getNickname()).thenReturn("testPlayer");  // Mock getNickname()

    // Clear and add the user to UserList
    UserList.clear();
    UserList.addUser("testPlayer", mockOut);  // Adds a real User instance to UserList (mock ignored here)

    // Create a Lobby and add the player
    lobby = new Lobby("TestLobby");
    lobby.addPlayers(1);  // Adds the player to the lobby
    Server.lobbies.add(lobby);  // Add the lobby to Server's lobbies list

    // Get the GameBoard for the player
    board = lobby.getGameBoard("testPlayer");

    // Set available colors
    Server.colors = new String[]{"purple", "yellow", "blue"};

    // Mock ProtocolWriterServer and add it to the server's protocolWriters map
    mockWriter = mock(ProtocolWriterServer.class);
    protocolWriters.clear();
    protocolWriters.put(mockOut, mockWriter);  // Associate mock Writer with the mock OutputStream
  }


  /**
   * Cleanup the test environment after each test.
   * Clears the Server's lobbies and users to ensure tests do not interfere with each other.
   */
  @AfterEach
  void cleanup() {
    Server.lobbies.clear();
    UserList.clear();
    Server.clientWriters.clear();
  }



  /**
   * Test when the game has not started yet or is already finished.
   * Verifies that the appropriate message is sent when the user tries to roll the dice before the game starts.
   */
  @Test
  void testRollTheDiceGameNotStarted() throws Exception {
    lobby.changeGameState(1); // Set the game state to something other than '2' (running)

    UserList.getUser(1).setHasRolled(false); // The user hasn't rolled yet

    // Call rollTheDice
    Server.rollTheDice(1);

    // Verify that the correct message is sent
    verify(mockWriter).sendInfo(contains("The game has not started yet or is already finished."));
  }

  /**
   * Test when the sendInfo method throws an IOException during dice roll.
   * Verifies that the exception is handled and doesn't affect the flow.
   */
  @Test
  void testRollTheDiceInfoIOException() throws Exception {
    lobby.changeGameState(2); // Set the game state to '2' (running)
    UserList.getUser(1).setHasRolled(false); // The user hasn't rolled yet

    // Simulate an IOException when sending info
    doThrow(new IOException("Simulated IO error")).when(mockWriter).sendInfo(anyString());

    // Call rollTheDice
    Server.rollTheDice(1);

    // No exception should be thrown, we are testing the catch block
  }

  /**
   * Test when the sendCommandAndString method throws an IOException during dice roll.
   * Verifies that the exception is handled and doesn't affect the flow.
   */
  @Test
  void testRollTheDiceCommandIOException() throws Exception {
    lobby.changeGameState(2); // Set the game state to '2' (running)
    UserList.getUser(1).setHasRolled(false); // The user hasn't rolled yet

    // Simulate an IOException when sending the rolled colors
    doThrow(new IOException("Simulated IO error")).when(mockWriter).sendCommandAndString(eq(Command.ROLL), anyString());

    // Call rollTheDice
    Server.rollTheDice(1);

    // No exception should be thrown, we are testing the catch block
  }

}
