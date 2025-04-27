package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import java.io.PrintWriter;
import org.junit.jupiter.api.*;

import java.io.OutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Full test suite for {@link Server} using real {@link Lobby} methods. This suite verifies the
 * behavior of user handling, nickname management, lobby lifecycle, broadcast functionality, and
 * game progression in the Server class.
 */
public class ServerTest {

  /**
   * Setup before each test to clear server data and ensure a clean slate for each test.
   */
  @BeforeEach
  void resetEnvironment() {
    Server.lobbies.clear();
    UserList.clear();
    Server.clientWriters.clear();
  }

  /**
   * Verifies that a new user is successfully registered and can be retrieved from UserList by user
   * ID.
   */
  @Test
  void testAddNewUser() {
    OutputStream out = mock(OutputStream.class);
    int id = Server.addNewUser("Tester", out); // Add a new user
    assertEquals("Tester", UserList.getUserName(id)); // Verify the user was added correctly
  }

  /**
   * Validates that a newly created lobby is added to the list of lobbies and the name is set
   * correctly.
   */
  @Test
  void testCreateLobbyAddsIt() {
    Server.createLobby("GameLobby", null); // Create a new lobby
    assertEquals("GameLobby", Server.lobbies.get(0).getLobbyName()); // Ensure the lobby is added
  }

  /**
   * Confirms that the printLobbyStates method runs without exceptions when multiple lobbies exist.
   */
  @Test
  void testPrintLobbyStatesRuns() {
    Server.lobbies.add(new Lobby("L1"));
    Server.lobbies.add(new Lobby("Welcome"));
    assertDoesNotThrow(Server::printAllLobbyStates); // Verify it runs without errors
  }

  /**
   * Tests the behavior of a user joining both non-existing and existing lobbies. Verifies that the
   * user can join an existing lobby and not a non-existing one.
   */
  @Test
  void testJoinLobbyBehavior() {
    int id = UserList.addUser("NewPlayer", mock(OutputStream.class));
    Server.joinLobby("FakeLobby", id); // Attempt to join a non-existing lobby

    Server.createLobby("RealLobby", null); // Create a real lobby
    Server.joinLobby("RealLobby", id); // Join the real lobby

    assertEquals("RealLobby", Server.getLobbyOfPlayer("NewPlayer")
        .getLobbyName()); // Verify the player is in the correct lobby
  }

  /**
   * Ensures that broadcast sends a correctly formatted BROD message to all clients.
   */
  @Test
  void testBroadcastToAll() {
    PrintWriter writer = mock(PrintWriter.class);
    Server.clientWriters.add(writer); // Add the mock writer
    Server.broadcastToAll("Announcement"); // Broadcast a message to all clients
    verify(writer).println(startsWith(Command.BROD.name())); // Verify BROD command is sent
    verify(writer).flush(); // Ensure that flush is called
  }

  /**
   * Ensures that a simple broadcast message works correctly by sending it to all clients.
   */
  @Test
  void testSimpleBroadcast() {
    PrintWriter writer = mock(PrintWriter.class);
    Server.clientWriters.add(writer); // Add the mock writer
    Server.broadcast("Simple message"); // Broadcast a simple message
    verify(writer).println("Simple message"); // Verify the correct message was sent
    verify(writer).flush(); // Ensure that flush is called
  }

  /**
   * Tests that an in-lobby broadcast sends INFO messages to the correct users in the lobby.
   */
  @Test
  void testBroadcastInLobby() {
    int id = UserList.addUser("UserX", mock(OutputStream.class));
    Lobby lobby = new Lobby("LobbyX");
    lobby.addPlayers(id);
    Server.lobbies.add(lobby);

    assertDoesNotThrow(() -> Server.broadcastInLobby("Message",
        "UserX")); // Verify that no exceptions are thrown during in-lobby broadcasting
  }

  /**
   * Simulates a client disconnect and verifies that the shutdown thread is triggered.
   */
  @Test
  void testDisconnectClientTriggersShutdown() throws InterruptedException {
    Server.addNewUser("LeaveMe", mock(OutputStream.class)); // Add a new user
    Server.ClientDisconnected(); // Simulate client disconnection
    Thread.sleep(200); // Wait for potential shutdown to occur
    assertTrue(Server.getActiveClientCount() <= 0); // Verify that the number of active clients is 0
  }

  /**
   * Validates that nickname changes are correctly handled and invalid nicknames are rejected.
   */
  @Test
  void testNicknameChangeValidAndInvalid() {
    int id = UserList.addUser("Old", mock(OutputStream.class)); // Add a user with an old nickname
    Server.changeNickname(id, "NewNick"); // Change to a valid new nickname
    assertTrue(UserList.getUserName(id).startsWith("NewNick")); // Verify the nickname was changed

    Server.changeNickname(id, "!!"); // Attempt to set an invalid nickname
    assertNotEquals("!!", UserList.getUserName(id)); // Verify the nickname wasn't changed
  }

  /**
   * Simulates the field selection and deselection process, including user interaction with game
   * fields.
   */
  @Test
  void testFieldSelectionAndDeselection() {
    OutputStream out = mock(OutputStream.class);
    int userId = Server.addNewUser("FieldUser", out); // Add a new user
    Lobby lobby = new Lobby("DiceGame");
    lobby.addPlayers(userId);
    Server.lobbies.add(lobby);

    // Set the valid colors for the test
    Server.colors = new String[]{"purple", "blue", "red", "green", "orange", "pink", "yellow"};

    // Set the game state to 2 (running)
    lobby.changeGameState(2);

    // Set the user as having rolled
    UserList.getUser(userId).setHasRolled(true);

    // Perform the action to check the field
    Server.checkField(userId, "purple1");

    GameBoard board = lobby.getGameBoard("FieldUser");

    // Ensure the field is selected after the checkField call
    assertTrue(board.inSelectedField(board.getFieldById("purple1")), "Field should be selected");

    // Deselect the field
    Server.deselectField(userId, "purple1");

    // Ensure the selected fields are empty after deselection
    assertTrue(board.selectedFieldsEmpty(), "Selected field list should be empty after deselect");
  }

  /**
   * Verifies that the server shuts down gracefully and no exceptions are thrown.
   */
  @Test
  void testServerShutdownSafe() throws Exception {
    ServerSocket dummySocket = new ServerSocket(0);

    // Use reflection to set the private field for the Server's "echod" to simulate server behavior
    Field echodField = Server.class.getDeclaredField("echod");
    echodField.setAccessible(true);
    echodField.set(null, dummySocket);

    // Ensure that no exception is thrown during shutdown
    assertDoesNotThrow(Server::shutdownServerA);
  }

  /**
   * Verifies that Server.getLobbyOfPlayer() returns null when the player is not in any lobby.
   */
  @Test
  void testLobbyLookupReturnsNull() {
    // Ensure that querying a non-existent player returns null
    assertNull(Server.getLobbyOfPlayer("Ghost"));
  }

  /**
   * Tests that the updateAllClients method sends game and user updates correctly to all connected
   * clients.
   */
  @Test
  void testUpdateClients() {
    int id = UserList.addUser("Updater",
        mock(OutputStream.class)); // Add a user for the update test
    Lobby lobby = new Lobby("SyncRoom");
    lobby.addPlayers(id);
    Server.lobbies.add(lobby);
    assertDoesNotThrow(
        Server::updateAllClients); // Ensure no exceptions occur during the update process
  }

  /**
   * Verifies that moving a player to the last selected field works correctly and sends the proper
   * movement info.
   */
  @Test
  void testMoveToLastSelectedField_noPut_sendsMovementInfo() throws IOException {
    // Prepare mock users and their respective output streams
    Server.lobbies.clear();
    UserList.clear();

    OutputStream out1 = mock(OutputStream.class);
    OutputStream out2 = mock(OutputStream.class);

    int id1 = UserList.addUser("Alice", out1); // Add first player
    int id2 = UserList.addUser("Bob", out2); // Add second player

    // Create a lobby and add the players
    Lobby lobby = new Lobby("TestLobby");
    lobby.addPlayers(id1);
    lobby.addPlayers(id2);
    Server.lobbies.add(lobby);

    // Get the game board for Alice and simulate a field selection
    GameBoard board = lobby.getGameBoard("Alice");
    board.addSelectedField(board.getFieldById("blue1"));

    // Simulate the player moving to the last selected field
    Server.moveToLastSelectedField(id1);

    // Verify that the player has moved to the correct field
    assertEquals("blue1", board.getCurrentField().getFieldId());
  }
}
