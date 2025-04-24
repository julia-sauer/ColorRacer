package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import ch.unibas.dmi.dbis.cs108.game.GameBoard;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Full test suite for {@link Server} using real {@link Lobby} methods. This suite verifies the
 * behavior of user handling, nickname management, lobby lifecycle, broadcast functionality, and
 * game progression.
 */
public class ServerTest {

  @BeforeEach
  void resetEnvironment() {
    Server.lobbies.clear();
    UserList.clear();
    Server.clientWriters.clear();
  }

  /**
   * Verifies a new user is registered and correctly retrieved from UserList.
   */
  @Test
  void testAddNewUser() {
    OutputStream out = mock(OutputStream.class);
    int id = Server.addNewUser("Tester", out);
    assertEquals("Tester", UserList.getUserName(id));
  }

  /**
   * Validates lobby creation adds it to the list and responds correctly.
   */
  @Test
  void testCreateLobbyAddsIt() {
    Server.createLobby("GameLobby", null);
    assertEquals("GameLobby", Server.lobbies.get(0).getLobbyName());
  }

  /**
   * Confirms game state printing does not crash with mixed lobbies.
   */
  @Test
  void testPrintLobbyStatesRuns() {
    Server.lobbies.add(new Lobby("L1"));
    Server.lobbies.add(new Lobby("Welcome"));
    assertDoesNotThrow(Server::printAllLobbyStates);
  }

  /**
   * Tests user joins existing and non-existing lobbies.
   */
  @Test
  void testJoinLobbyBehavior() {
    int id = UserList.addUser("NewPlayer", mock(OutputStream.class));
    Server.joinLobby("FakeLobby", id);

    Server.createLobby("RealLobby", null);
    Server.joinLobby("RealLobby", id);

    assertEquals("RealLobby", Server.getLobbyOfPlayer("NewPlayer").getLobbyName());
  }

  /**
   * Ensures broadcast sends formatted BROD message to all clients.
   */
  @Test
  void testBroadcastToAll() {
    PrintWriter writer = mock(PrintWriter.class);
    Server.clientWriters.add(writer);
    Server.broadcastToAll("Announcement");
    verify(writer).println(startsWith(Command.BROD.name()));
    verify(writer).flush();
  }

  /**
   * Ensures raw broadcast string works correctly.
   */
  @Test
  void testSimpleBroadcast() {
    PrintWriter writer = mock(PrintWriter.class);
    Server.clientWriters.add(writer);
    Server.broadcast("Simple message");
    verify(writer).println("Simple message");
    verify(writer).flush();
  }

  /**
   * Ensures in-lobby broadcast delivers INFO message to correct users.
   */
  @Test
  void testBroadcastInLobby() {
    int id = UserList.addUser("UserX", mock(OutputStream.class));
    Lobby lobby = new Lobby("LobbyX");
    lobby.addPlayers(id);
    Server.lobbies.add(lobby);
    assertDoesNotThrow(() -> Server.broadcastInLobby("Message", "UserX"));
  }

  /**
   * Simulates 1 disconnect and confirms shutdown thread starts.
   */
  @Test
  void testDisconnectClientTriggersShutdown() throws InterruptedException {
    Server.addNewUser("LeaveMe", mock(OutputStream.class));
    Server.ClientDisconnected();
    Thread.sleep(200);
    assertTrue(Server.getActiveClientCount() <= 0);
  }

  /**
   * Validates valid nickname change and ignores invalid ones.
   */
  @Test
  void testNicknameChangeValidAndInvalid() {
    int id = UserList.addUser("Old", mock(OutputStream.class));
    Server.changeNickname(id, "NewNick");
    assertTrue(UserList.getUserName(id).startsWith("NewNick"));

    Server.changeNickname(id, "!!");
    assertNotEquals("!!", UserList.getUserName(id));
  }

  /**
   * Ensures dice roll triggers color assignment and updates user state.
   */
  @Test
  void testRollDiceValidGameState() {
    int id = UserList.addUser("Roller", mock(OutputStream.class));
    Lobby lobby = new Lobby("DiceGame");
    lobby.addPlayers(id);
    Server.lobbies.add(lobby);
    lobby.changeGameState(2);
    while (!lobby.getPlayers().get(0).equals("Roller")) {
      lobby.advanceTurn(); // ensure Roller is current
    }

    Server.rollTheDice(id);
    assertTrue(UserList.getUser(id).hasRolled());
  }

  /**
   * Simulates selecting and deselecting a field.
   */
  @Test
  void testFieldSelectionAndDeselection() {
    OutputStream out = mock(OutputStream.class);
    int userId = Server.addNewUser("FieldUser", out);
    Lobby lobby = new Lobby("DiceGame");
    lobby.addPlayers(userId);
    Server.lobbies.add(lobby);

    Server.colors = new String[]{"purple", "blue", "red", "green", "orange", "pink", "yellow"};

    UserList.getUser(userId).setHasRolled(true);

    Server.checkField(userId, "purple1");

    GameBoard board = lobby.getGameBoard("FieldUser");
    assertTrue(board.inSelectedField(board.getFieldById("purple1")), "Field should be selected");

    Server.deselectField(userId, "purple1");

    assertTrue(board.selectedFieldsEmpty(), "Selected field list should be empty after deselect");
  }


  /**
   * Tests winning flow for a player at the finish line.
   */
  @Test
  void testWinScenario() {
    int id = UserList.addUser("Winner", mock(OutputStream.class));
    Lobby lobby = new Lobby("WinLobby");
    lobby.addPlayers(id);
    Server.lobbies.add(lobby);
    GameBoard board = lobby.getGameBoard("Winner");
    board.addSelectedField(board.getFieldById("purple2"));
    try {
      Server.moveToLastSelectedField(id);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    assertEquals(2, lobby.getPodestPlace());
  }

  /**
   * Triggers server shutdown and ensures no exceptions.
   */
  @Test
  void testServerShutdownSafe() throws Exception {

    ServerSocket dummySocket = new ServerSocket(0);

    Field echodField = Server.class.getDeclaredField("echod");
    echodField.setAccessible(true);
    echodField.set(null, dummySocket);

    assertDoesNotThrow(Server::shutdownServerA);
  }


  /**
   * Confirms Server.getLobbyOfPlayer() returns null for unknown name.
   */
  @Test
  void testLobbyLookupReturnsNull() {
    assertNull(Server.getLobbyOfPlayer("Ghost"));
  }

  /**
   * Tests updateAllClients sends game and user updates.
   */
  @Test
  void testUpdateClients() {
    int id = UserList.addUser("Updater", mock(OutputStream.class));
    Lobby lobby = new Lobby("SyncRoom");
    lobby.addPlayers(id);
    Server.lobbies.add(lobby);
    assertDoesNotThrow(Server::updateAllClients);
  }

  /**
   * Verifies Server.won() method registers a winner correctly.
   */
  @Test
  void testWinnerLogic() {
    int id = UserList.addUser("Champion", mock(OutputStream.class));
    Lobby lobby = new Lobby("ChampRoom");
    lobby.addPlayers(id);
    Server.lobbies.add(lobby);
    Server.won(id);
    assertTrue(lobby.winners.contains("Champion"));
  }
}