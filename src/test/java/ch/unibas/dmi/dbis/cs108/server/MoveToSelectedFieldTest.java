package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.*;

import java.io.OutputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the moveToLastSelectedField method in the Server class. These tests check the
 * behavior of moving a player to the last selected field, including edge cases such as no selected
 * fields, user not being in a lobby, and IO exceptions.
 */
public class MoveToSelectedFieldTest {

  private OutputStream mockOut;
  private ProtocolWriterServer mockWriter;
  private User user;
  private GameBoard board;
  private Lobby lobby;

  /**
   * Setup method to initialize mock objects, create a lobby, add a player, and setup the game board
   * with a selected field.
   */
  @BeforeEach
  void setup() {
    mockOut = mock(OutputStream.class);
    mockWriter = mock(ProtocolWriterServer.class);

    // Clear previous data and setup new environment for each test
    Server.clientWriters.clear();
    Server.protocolWriters.clear();
    Server.lobbies.clear();
    UserList.clear();

    Server.colors = new String[]{"purple", "yellow", "blue"};

    // Add a new user to the UserList and get the corresponding User object
    int userId = UserList.addUser("testPlayer", mockOut);
    user = UserList.getUser(userId);
    Server.protocolWriters.put(user.getOut(), mockWriter);

    // Create a new lobby, add the user, and assign a game board
    lobby = new Lobby("TestLobby");
    lobby.addPlayers(userId);
    Server.lobbies.add(lobby);

    board = lobby.getGameBoard("testPlayer");

    // Select a field in the game board for the user to move to later
    Field field = board.getFieldById("purple1");
    board.addSelectedField(field);
  }

  /**
   * Cleanup method to clear all data after each test to ensure tests do not affect each other.
   */
  @AfterEach
  void cleanup() {
    Server.lobbies.clear();
    Server.protocolWriters.clear();
    Server.clientWriters.clear();
    UserList.clear();
  }

  /**
   * Test to check behavior when the user is null. Verifies that no crash occurs when trying to move
   * to the last selected field with an invalid userId.
   */
  @Test
  void testUserIsNull() throws Exception {
    Server.moveToLastSelectedField(-1); // Simulate invalid user ID
    // Nothing to verify, just check that no crash happens
  }

  /**
   * Test when there are no selected fields. Verifies that the user is prompted to select a field
   * before moving.
   */
  @Test
  void testNoSelectedFields() throws Exception {
    board.moveToLastSelected(); // Clears the selected fields
    Server.moveToLastSelectedField(1); // Try to move without selecting any fields

    // Verify that an info message is sent asking the player to select a field
    verify(mockWriter).sendInfo(contains("select at least one field"));
  }

  /**
   * Test when the user is not in any lobby. Verifies that the method does nothing when there is no
   * lobby associated with the user.
   */
  @Test
  void testUserNotInLobby() throws Exception {
    Server.lobbies.clear(); // Simulate no lobbies

    Server.moveToLastSelectedField(1); // Try to move when the user isn't in a lobby
    // The method should simply return without any interaction
  }

  /**
   * Test when a player moves to a winning field. Simulates the scenario where the user moves to a
   * field that triggers a win.
   */
  @Test
  void testMoveTriggersWin() throws Exception {
    // Simulate moving to a field that triggers the win condition
    Field winningField = board.getFieldById("purple2"); // Assume "purple2" triggers a win
    board.moveToLastSelected(); // Move to the last selected field
    board.addSelectedField(winningField); // Add the winning field to selected fields

    // Call the method to move the player to the last selected field
    Server.moveToLastSelectedField(1);
    // No need for assertions as the win effect is handled within the won() method
  }

  /**
   * Test to check that the method handles IOException during broadcasting. Verifies that no crash
   * occurs even if an IOException is thrown during the sending of commands.
   */
  @Test
  void testIOExceptionDuringBroadcast() throws Exception {
    // Simulate an IOException when sending the command string
    doThrow(new IOException("fail")).when(mockWriter).sendCommandAndString(any(), any());

    Server.moveToLastSelectedField(1); // Try moving to the last selected field
    // Expect no crash, we are testing that the catch block is hit during IOException
  }
}
