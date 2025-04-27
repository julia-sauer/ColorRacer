package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for testing the checkField method in the Server class.
 * This class contains tests for various scenarios, including user actions and game state.
 */
class CheckFieldTest {

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
    mockOut = mock(OutputStream.class);
    mockUser = mock(User.class);
    when(mockUser.getOut()).thenReturn(mockOut);
    when(mockUser.getNickname()).thenReturn("testPlayer");

    UserList.clear();
    UserList.addUser("testPlayer", mockOut); // Adds a real User instance to UserList (mock is ignored here)

    lobby = new Lobby("TestLobby");
    lobby.addPlayers(1);
    Server.lobbies.add(lobby);

    board = lobby.getGameBoard("testPlayer");
    Server.colors = new String[]{"purple", "yellow", "blue"};

    mockWriter = mock(ProtocolWriterServer.class);
    Server.protocolWriters.clear();
    Server.protocolWriters.put(mockOut, mockWriter);
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
   * Test the scenario where the user has not rolled yet.
   * Verifies that the appropriate message is sent when the user tries to check a field without rolling first.
   */
  @Test
  void testCheckFieldUserHasNotRolled() throws Exception {
    // Set the game state to 2 (game running)
    lobby.changeGameState(2);  // Ensure the game has started

    // Set the user as not having rolled
    UserList.getUser(1).setHasRolled(false);

    // Perform the action to check the field
    Server.checkField(1, "purple1");

    // Verify that the "You need to roll first" message is sent
    verify(mockWriter).sendInfo(contains("You need to roll first."));
  }

  /**
   * Test the scenario where the user checks a valid field.
   * Verifies that the correct message is sent when the user rolls and selects a valid field.
   */
  @Test
  void testCheckFieldValidField() throws Exception {
    // Setup the user as having rolled
    UserList.getUser(1).setHasRolled(true);

    // Set the game state to 2 (game running)
    lobby.changeGameState(2);

    // Create a mock ProtocolWriterServer to return when needed
    ProtocolWriterServer mockWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Simulated IO exception")).when(mockWriter).sendCommandAndString(eq(Command.CHOS), anyString());
    Server.protocolWriters.put(mockOut, mockWriter);

    // Perform the action to check the field
    Server.checkField(1, "purple1");

    // Verify that sendCommandAndString was called with the expected arguments
    verify(mockWriter).sendCommandAndString(eq(Command.CHOS), contains("purple1"));
  }

  /**
   * Test the scenario where the user checks an invalid field.
   * Verifies that the correct error message is sent when the user selects an invalid field.
   */
  @Test
  void testCheckFieldInvalidField() throws Exception {
    lobby.changeGameState(2); // Set the game state to 2

    UserList.getUser(1).setHasRolled(true); // User has rolled

    // Perform the action to check the field
    Server.checkField(1, "nonExistingField");

    // Verify that the appropriate error message is sent for an invalid field
    verify(mockWriter).sendCommandAndString(eq(Command.INFO), contains("Field is invalid"));
  }

  /**
   * Test the scenario where the sendInfo method throws an IOException.
   * This test ensures that an IOException does not cause the method to throw an uncaught exception.
   */
  @Test
  void testCheckFieldRollMessageThrowsIOException() throws Exception {
    User user = UserList.getUser(1);
    user.setHasRolled(false);

    ProtocolWriterServer mockWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Simulated IO error")).when(mockWriter).sendInfo(anyString());
    Server.protocolWriters.put(user.getOut(), mockWriter);

    // Ensure no exception is thrown
    Server.checkField(1, "purple1");
    // No verify needed here as the exception is simulated
  }

  /**
   * Test the scenario where the user is not in any lobby.
   * Verifies that the method simply returns if the user is not part of any lobby.
   */
  @Test
  void testCheckFieldUserNotInLobby() throws Exception {
    User user = UserList.getUser(1);
    user.setHasRolled(true);

    // Remove all lobbies → User is not in any lobby
    Server.lobbies.clear();

    // The method should simply return without doing anything
    Server.checkField(1, "purple1");
    // No exception, covers line `if (userLobby == null)`
  }

  /**
   * Test the scenario where the sendCommandAndString method throws an IOException while sending CHOS.
   * This test ensures that an IOException during sending CHOS is properly handled.
   */
  @Test
  void testCheckFieldCHOSThrowsIOException() throws Exception {
    User user = UserList.getUser(1);
    user.setHasRolled(true);

    GameBoard board = lobby.getGameBoard("testPlayer");
    Field purple1 = board.getFieldById("purple1");
    board.setCurrentField(purple1); // Set the currentField

    Server.colors = new String[]{"purple", "yellow", "blue"};

    ProtocolWriterServer mockWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Simulated CHOS error"))
        .when(mockWriter).sendCommandAndString(eq(Command.CHOS), anyString());
    Server.protocolWriters.put(user.getOut(), mockWriter);

    Server.checkField(1, "purple1");
    // No verify needed, as we are testing the exception handling
  }

  /**
   * Test the scenario where the sendCommandAndString method throws an IOException for an invalid field check.
   * Verifies that the IOException is properly handled during the error message sending.
   */
  @Test
  void testCheckFieldInvalidFieldIOException() throws Exception {
    User user = UserList.getUser(1);
    user.setHasRolled(true);

    ProtocolWriterServer mockWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Simulated INFO error"))
        .when(mockWriter).sendCommandAndString(eq(Command.INFO), anyString());
    Server.protocolWriters.put(user.getOut(), mockWriter);

    // Perform the action for an invalid field
    Server.checkField(1, "nonexistentField");
    // Expecting no exception – tests that the catch block is executed
  }
}
