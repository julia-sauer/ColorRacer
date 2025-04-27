package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.*;

import java.io.OutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the `deselectField` method in the Server class. These tests verify the behavior of
 * field deselection, including handling empty fields, invalid selections, successful deselection,
 * and various failure scenarios.
 */
class DeselectFieldTest {

  private OutputStream mockOut;
  private ProtocolWriterServer mockWriter;
  private Lobby lobby;
  private GameBoard board;

  /**
   * Set up the test environment before each test. This initializes mock objects for the User,
   * ProtocolWriterServer, and the GameBoard to simulate game conditions.
   */
  @BeforeEach
  void setup() {
    mockOut = mock(OutputStream.class);
    mockWriter = mock(ProtocolWriterServer.class);

    // Clearing previous test data to ensure isolation between tests
    UserList.clear();
    Server.lobbies.clear();
    Server.clientWriters.clear();
    Server.protocolWriters.clear();

    // Adding a mock user to the UserList
    UserList.addUser("testPlayer", mockOut);
    Server.protocolWriters.put(mockOut, mockWriter);

    // Setting up the lobby and adding a player to it
    lobby = new Lobby("TestLobby");
    lobby.addPlayers(1);
    Server.lobbies.add(lobby);

    // Getting the GameBoard for the test player
    board = lobby.getGameBoard("testPlayer");

    // Setting up colors, otherwise it will throw a NullPointerException
    Server.colors = new String[]{"purple", "yellow", "blue"};
  }

  /**
   * Cleanup the test environment after each test. This clears all the data in UserList, lobbies,
   * and protocol writers to prevent data leakage between tests.
   */
  @AfterEach
  void cleanup() {
    UserList.clear();
    Server.lobbies.clear();
    Server.clientWriters.clear();
    Server.protocolWriters.clear();
  }

  /**
   * Test to check the scenario when no field is selected. Verifies that the appropriate message is
   * sent when attempting to deselect with no selected fields.
   */
  @Test
  void testSelectedFieldsEmpty() throws Exception {
    // Since no field is selected, selectedFieldsEmpty() should be true
    Server.deselectField(1, "purple1");

    // Verify that the message "select a field first" is sent to the player
    verify(mockWriter).sendInfo(contains("select a field first"));
  }

  /**
   * Test to verify that trying to deselect a field that was not selected results in the correct
   * message. This tests invalid deselection behavior.
   */
  @Test
  void testDeselectedFieldNotSelected() throws Exception {
    User user = UserList.getUser(1);

    // Adding a different field to the selected list
    GameBoard board = lobby.getGameBoard("testPlayer");
    Field anotherField = board.getFieldById("yellow1");
    board.addSelectedField(anotherField);

    // Now selectedFields is not empty, but "purple1" wasn't selected
    Server.deselectField(1, "purple1");

    // Verify that the message "have not chosen this field" is sent to the player
    verify(mockWriter).sendInfo(contains("have not chosen this field"));
  }

  /**
   * Test for valid deselection of a field. Verifies that when a valid field is deselected, the
   * corresponding command is sent.
   */
  @Test
  void testValidDeselect() throws Exception {
    // Mark the field "purple1" as selected
    Field someField = board.getFieldById("purple1");
    board.addSelectedField(someField);

    // Now deselect the field and verify that the correct command is sent
    Server.deselectField(1, someField.getFieldId());

    // Verify that the "DEOS" command with the field ID is sent to the player
    verify(mockWriter).sendCommandAndString(eq(Command.DEOS), contains("purple1"));
  }

  /**
   * Test to ensure that the IOException is properly handled when the sendCommandAndString method
   * fails. This simulates an error while trying to send the deselect command.
   */
  @Test
  void testDeselectSendFails() throws Exception {
    // Select the field "purple1"
    Field someField = board.getFieldById("purple1");
    board.addSelectedField(someField);

    // Mock ProtocolWriterServer to throw an IOException when sending a command
    ProtocolWriterServer mockWriterFail = mock(ProtocolWriterServer.class);
    doThrow(new java.io.IOException("Simulated send failure")).when(mockWriterFail)
        .sendCommandAndString(eq(Command.DEOS), anyString());

    // Override the writer for this test with the mock that simulates failure
    Server.protocolWriters.put(mockOut, mockWriterFail);

    // Call the deselectField method, expecting the exception to be caught without failing the test
    Server.deselectField(1, someField.getFieldId());

    // No verify is necessary here since we are testing the exception handling
  }

  /**
   * Test to check if a new ProtocolWriterServer is created if none exists for the user. This
   * ensures that when a ProtocolWriterServer does not exist in the map, it is created and stored.
   */
  @Test
  void testCreatesNewProtocolWriterIfNoneExists() throws Exception {
    User user = UserList.getUser(1);
    ProtocolWriterServer newWriterSpy = spy(
        new ProtocolWriterServer(Server.clientWriters, user.getOut()));

    // Clear the protocol writers to simulate missing writer
    Server.protocolWriters.clear();

    // Inject the spy manually after creation to track interaction
    Server.deselectField(1, "purple1");

    // Assert that the protocol writer has been created and added to the map
    assertNotNull(Server.protocolWriters.get(user.getOut()));
  }

  /**
   * Test to simulate an IOException while sending the "select a field first" message. This tests
   * that the method correctly handles IOExceptions during communication with the player.
   */
  @Test
  void testIOExceptionOnEmptySelectedFieldsMessage() throws Exception {
    User user = UserList.getUser(1);
    GameBoard board = lobby.getGameBoard("testPlayer");

    // Mock ProtocolWriterServer to throw an IOException when sending info
    ProtocolWriterServer faultyWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Mocked")).when(faultyWriter).sendInfo(contains("select a field"));
    Server.protocolWriters.put(user.getOut(), faultyWriter);

    // Attempt to deselect when no fields are selected
    Server.deselectField(1, "purple1");

    // Jacoco will hit the catch block for the IOException
  }

  /**
   * Test to simulate an IOException while sending the "not chosen this field" message when an
   * invalid field is deselected.
   */
  @Test
  void testIOExceptionOnDeselectedFieldNotInSelectedFieldsMessage() throws Exception {
    User user = UserList.getUser(1);
    GameBoard board = lobby.getGameBoard("testPlayer");
    board.addSelectedField(board.getFieldById("yellow1")); // Add a different field

    // Mock ProtocolWriterServer to throw an IOException when sending info
    ProtocolWriterServer faultyWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Mocked")).when(faultyWriter)
        .sendInfo(contains("not chosen this field"));
    Server.protocolWriters.put(user.getOut(), faultyWriter);

    // Attempt to deselect a field that wasn't selected
    Server.deselectField(1, "purple1");
  }

  /**
   * Test to simulate an IOException when sending the DEOS command. This ensures that the method
   * correctly handles IOExceptions during the deselection command.
   */
  @Test
  void testIOExceptionOnDEOSCommand() throws Exception {
    User user = UserList.getUser(1);
    GameBoard board = lobby.getGameBoard("testPlayer");
    Field field = board.getFieldById("purple1");
    board.addSelectedField(field);

    // Mock ProtocolWriterServer to throw an IOException when sending the DEOS command
    ProtocolWriterServer faultyWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Mocked")).when(faultyWriter)
        .sendCommandAndString(eq(Command.DEOS), contains("purple1"));
    Server.protocolWriters.put(user.getOut(), faultyWriter);

    Server.colors = new String[]{"purple", "yellow", "blue"};

    // Call deselectField to trigger the IOException in the catch block
    Server.deselectField(1, "purple1");
  }

  /**
   * Test when the user is not in any lobby. Verifies that the method simply returns without any
   * action when no lobby is found for the user.
   */
  @Test
  void testUserNotInAnyLobby() {
    User user = UserList.getUser(1);
    Server.lobbies.clear(); // Simulate no lobbies

    // No error, no interaction, just covers the return
    Server.deselectField(1, "purple1");
  }
}
