package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import org.junit.jupiter.api.*;
import java.io.OutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Lobby class. These tests verify the behavior of adding players, changing the
 * game state, handling winners, and more.
 */
public class LobbyTest {

  private Lobby lobby;

  /**
   * Set up the test environment before each test. This initializes the lobby and prepares the
   * necessary data for each test.
   */
  @BeforeEach
  void setup() {
    lobby = new Lobby("TestLobby");
  }

  /**
   * Test to verify that the first player added to the lobby is set as the host. Also checks if the
   * player is added correctly to the player list.
   */
  @Test
  void testAddPlayersAddsFirstAsHost() {
    OutputStream dummyOut = mock(OutputStream.class);

    // Adding user "Alice" to the UserList and adding the player to the lobby
    int userId = UserList.addUser("Alice", dummyOut);
    boolean result = lobby.addPlayers(userId);

    // Assert the player was added and they are the host
    assertTrue(result);
    assertEquals("Alice", lobby.getHostName());
    assertEquals(List.of("Alice"), lobby.getPlayers());
  }

  /**
   * Test to check if the lobby rejects players once it reaches the maximum allowed players (4).
   * Verifies that the lobby does not allow more than 4 players.
   */
  @Test
  void testLobbyRejectsMoreThanMaxPlayers() {
    UserList.clear();

    // Add 4 players, should succeed
    for (int i = 0; i < 4; i++) {
      int id = UserList.addUser("P" + i, mock(OutputStream.class));
      assertTrue(lobby.addPlayers(id), "Should accept player " + i);
    }

    // Add the 5th player, should fail
    int extraId = UserList.addUser("Extra", mock(OutputStream.class));
    assertFalse(lobby.addPlayers(extraId), "Lobby should reject 5th player");
  }

  /**
   * Test to check if the game state can be changed. Verifies that the game state is correctly
   * updated from "open" (1) to "ongoing" (2).
   */
  @Test
  void testChangeGameState() {
    // Initially, the game state should be "open" (1)
    assertEquals(1, lobby.getGameState());

    // Change the game state to "ongoing" (2)
    lobby.changeGameState(2);

    // Assert the game state is now "ongoing" (2)
    assertEquals(2, lobby.getGameState());
  }

  /**
   * Test to verify that the winner is correctly added to the winners list. Verifies that the lobby
   * updates its winner list when a player wins.
   */
  @Test
  void testAddWinner() {
    // Add "Player1" as the winner
    lobby.addWinner("Player1");

    // Assert that "Player1" is added to the winners list
    assertTrue(lobby.winners.contains("Player1"));
  }

  /**
   * Test for creating and managing the ready status list of players. Verifies that all players are
   * initially marked as not ready and can be set to ready.
   */
  @Test
  void testMakeReadyStatusListAndMakeReady() {
    OutputStream dummyOut = mock(OutputStream.class);

    // Add a player "ReadyPlayer"
    int userId = UserList.addUser("ReadyPlayer", dummyOut);
    lobby.addPlayers(userId);

    // Create the ready status list and ensure the player is initially not ready
    lobby.makeReadyStatusList();
    assertFalse(lobby.readyStatus.get("ReadyPlayer"));

    // Set the player as ready
    lobby.makeReady("ReadyPlayer");
    assertTrue(lobby.readyStatus.get("ReadyPlayer"));
  }

  /**
   * Test to check the behavior of the `isHost` method. Verifies that the method correctly
   * identifies the host of the lobby.
   */
  @Test
  void testIsHost() {
    UserList.addUser("Host", mock(OutputStream.class));
    lobby.addPlayers(1);

    // The first player to join should be the host
    assertTrue(lobby.isHost("Host"));
    assertFalse(lobby.isHost("NotHost"));
  }

  /**
   * Test to check the behavior of removing a player from the lobby. Verifies that the player is
   * removed from both the player list and the player order.
   */
  @Test
  void testRemovePlayer() {
    UserList.clear();

    // Add two players "Host" and "Guest"
    OutputStream dummyOut = mock(OutputStream.class);
    int id1 = UserList.addUser("Host", dummyOut);
    int id2 = UserList.addUser("Guest", dummyOut);

    String name1 = UserList.getUserName(id1);
    String name2 = UserList.getUserName(id2);

    lobby.addPlayers(id1);
    lobby.addPlayers(id2);

    // Assert before removing player
    assertEquals(2, lobby.getPlayers().size());

    // Remove "Host" and verify
    lobby.removePlayer(name1);

    // Assert after removal
    assertEquals(1, lobby.getPlayers().size());
    assertTrue(lobby.getPlayers().contains(name2));
    assertFalse(lobby.getPlayers().contains(name1));
  }

  /**
   * Test to check if the game board for a specific player can be retrieved correctly. Verifies that
   * the player's game board is correctly initialized with the field "white1".
   */
  @Test
  void testGetGameBoardForPlayer() {
    OutputStream dummyOut = mock(OutputStream.class);
    int userId = UserList.addUser("Alice", dummyOut); // Store the actual ID

    lobby.addPlayers(userId); // Use the correct user ID

    // Get the game board for "Alice"
    GameBoard board = lobby.getGameBoard("Alice");

    // Assert that the game board is not null and the starting field is "white1"
    assertNotNull(board);
    assertEquals("white1", board.getCurrentField().getFieldId());
  }

  /**
   * Test to verify the behavior of advancing turns in the lobby. Ensures that the turn is advanced
   * correctly and skips the player who has already won.
   */
  @Test
  void testAdvanceTurnSkipsWinner() {
    UserList.clear();

    // Add players "A", "B", and "C"
    int idA = UserList.addUser("A", mock(OutputStream.class));
    int idB = UserList.addUser("B", mock(OutputStream.class));
    int idC = UserList.addUser("C", mock(OutputStream.class));

    String nameA = UserList.getUserName(idA);
    String nameB = UserList.getUserName(idB);
    String nameC = UserList.getUserName(idC);

    lobby.addPlayers(idA);
    lobby.addPlayers(idB);
    lobby.addPlayers(idC);

    // Advance turn and add player B as the winner
    lobby.advanceTurn(); // A's turn
    lobby.addWinner(nameB); // B wins
    lobby.advanceTurn(); // B should be skipped, so it should be C's turn

    // Assert that the current player is now C
    assertTrue(lobby.isCurrentPlayer(nameC),
        "Expected current player to be C, got: " + lobby.getCurrentPlayer());
  }

  /**
   * Test to ensure that only the host can start the game. Verifies that the game does not start
   * when a guest attempts to start it.
   */
  @Test
  void testStartGameOnlyHostCanStart() {
    OutputStream mockOut1 = mock(OutputStream.class);
    OutputStream mockOut2 = mock(OutputStream.class);

    // Add Host and Guest to the lobby
    UserList.addUser("Host", mockOut1);
    UserList.addUser("Guest", mockOut2);

    lobby.addPlayers(1);
    lobby.addPlayers(2);

    // Try to start the game with a guest
    lobby.startGame(2); // Guest tries to start

    // Assert that the game state remains "open" (1)
    assertEquals(1, lobby.getGameState());
  }

  /**
   * Test to check if the game board fields are reset when restarting the game. Verifies that the
   * game state is reset to "running" and all players are moved to the starting field.
   */
  @Test
  void testRestartGameResetsFields() {
    OutputStream mockOut = mock(OutputStream.class);

    // Add Host and Player2 to the lobby
    UserList.addUser("Host", mockOut);
    UserList.addUser("Player2", mockOut);

    lobby.addPlayers(1);
    lobby.addPlayers(2);

    // Move "Host" to a different field
    GameBoard board = lobby.getGameBoard("Host");
    board.setCurrentField(board.getFieldById("red1"));

    // Change game state to "running"
    lobby.changeGameState(2);

    // Restart the game
    lobby.restartGame(1);

    // Assert that the "Host" has been moved back to the starting field
    assertEquals("white1", board.getCurrentField().getFieldId());

    // Assert that the game state is now "running"
    assertEquals(2, lobby.getGameState());
  }
}
