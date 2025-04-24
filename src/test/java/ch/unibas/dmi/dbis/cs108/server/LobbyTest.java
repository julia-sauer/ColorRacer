package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.GameBoard;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.OutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class LobbyTest {

  @Mock
  private UserList userList;

  @Mock
  private Server server;

  @Mock
  private ProtocolWriterServer protocolWriterServer;

  @InjectMocks
  private Lobby lobby;

  private int userId;

  @BeforeEach
  void setup() throws NoSuchFieldException, IllegalAccessException {
    MockitoAnnotations.openMocks(this);
    lobby = new Lobby("TestLobby");
    userId = 1;
    setPrivateField(lobby, "gamestate", 1);
    setPrivateField(lobby, "lobbyname", "TestLobby");
    setPrivateField(lobby, "players", new ArrayList<>(List.of("host", "player2")));
  }

  private void setPrivateField(Object instance, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
    Field field = instance.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(instance, value);
  }


  @Test
  void testAddPlayersAddsFirstAsHost() {
    OutputStream dummyOut = mock(OutputStream.class);
    int userId = UserList.addUser("Alice", dummyOut); // Speichere tatsächliche ID
    boolean result = lobby.addPlayers(userId);

    assertTrue(result);
    assertEquals("Alice", lobby.getHostName());
    assertEquals(List.of("Alice"), lobby.getPlayers());
  }


  @Test
  void testLobbyRejectsMoreThanMaxPlayers() {
    UserList.clear();
    for (int i = 0; i < 4; i++) {
      int id = UserList.addUser("P" + i, mock(OutputStream.class));
      assertTrue(lobby.addPlayers(id), "Should accept player " + i);
    }

    int extraId = UserList.addUser("Extra", mock(OutputStream.class));
    assertFalse(lobby.addPlayers(extraId), "Lobby should reject 5th player");
  }


  @Test
  void testChangeGameState() {
    assertEquals(1, lobby.getGameState());
    lobby.changeGameState(2);
    assertEquals(2, lobby.getGameState());
  }

  @Test
  void testAddWinner() {
    lobby.addWinner("Player1");
    assertTrue(lobby.winners.contains("Player1"));
  }

  @Test
  void testMakeReadyStatusListAndMakeReady() {
    OutputStream dummyOut = mock(OutputStream.class);
    int userId = UserList.addUser("ReadyPlayer", dummyOut);
    lobby.addPlayers(userId);

    lobby.makeReadyStatusList();

    assertFalse(lobby.readyStatus.get("ReadyPlayer"));
    lobby.makeReady("ReadyPlayer");
    assertTrue(lobby.readyStatus.get("ReadyPlayer"));
  }


  @Test
  void testIsHost() {
    UserList.addUser("Host", mock(OutputStream.class));
    lobby.addPlayers(1);
    assertTrue(lobby.isHost("Host"));
    assertFalse(lobby.isHost("NotHost"));
  }

  @Test
  void testRemovePlayer() {
    UserList.clear();
    OutputStream dummyOut = mock(OutputStream.class);

    int id1 = UserList.addUser("Host", dummyOut);
    int id2 = UserList.addUser("Guest", dummyOut);

    String name1 = UserList.getUserName(id1);
    String name2 = UserList.getUserName(id2);

    lobby.addPlayers(id1);
    lobby.addPlayers(id2);

    System.out.println("Before remove: " + lobby.getPlayers());

    lobby.removePlayer(name1); // 🔧 nicht "Host", sondern das was wirklich gespeichert ist

    System.out.println("After remove: " + lobby.getPlayers());

    assertEquals(1, lobby.getPlayers().size());
    assertTrue(lobby.getPlayers().contains(name2));
    assertFalse(lobby.getPlayers().contains(name1));
  }


  @Test
  void testGetGameBoardForPlayer() {
    OutputStream dummyOut = mock(OutputStream.class);
    int userId = UserList.addUser("Alice", dummyOut); // Richtige ID speichern
    lobby.addPlayers(userId); // Diese ID verwenden

    GameBoard board = lobby.getGameBoard("Alice");
    assertNotNull(board);
    assertEquals("white1", board.getCurrentField().getFieldId());
  }

  @Test
  void testAdvanceTurnSkipsWinner() {
    UserList.clear();

    int idA = UserList.addUser("A", mock(OutputStream.class));
    int idB = UserList.addUser("B", mock(OutputStream.class));
    int idC = UserList.addUser("C", mock(OutputStream.class));

    String nameA = UserList.getUserName(idA);
    String nameB = UserList.getUserName(idB);
    String nameC = UserList.getUserName(idC);

    lobby.addPlayers(idA);
    lobby.addPlayers(idB);
    lobby.addPlayers(idC);

    lobby.advanceTurn();         // A
    lobby.addWinner(nameB);      // B wins
    lobby.advanceTurn();         // B should be skipped → C

    assertTrue(lobby.isCurrentPlayer(nameC),
        "Expected current player to be C, got: " + lobby.getCurrentPlayer());
  }


  @Test
  void testStartGameOnlyHostCanStart() {
    OutputStream mockOut1 = mock(OutputStream.class);
    OutputStream mockOut2 = mock(OutputStream.class);
    UserList.addUser("Host", mockOut1); // ID 1
    UserList.addUser("Guest", mockOut2); // ID 2
    lobby.addPlayers(1);
    lobby.addPlayers(2);

    lobby.startGame(2); // Guest tries to start
    assertEquals(1, lobby.getGameState()); // should remain "open"
  }

  @Test
  void testRestartGameResetsFields() {
    OutputStream mockOut = mock(OutputStream.class);
    UserList.addUser("Host", mockOut);
    UserList.addUser("Player2", mockOut);

    lobby.addPlayers(1);
    lobby.addPlayers(2);

    // Move player to a new field
    GameBoard board = lobby.getGameBoard("Host");
    board.setCurrentField(board.getFieldById("red1"));

    lobby.changeGameState(2); // Set to "running"
    lobby.restartGame(1); // Restart

    assertEquals("white1", board.getCurrentField().getFieldId()); // should reset
    assertEquals(2, lobby.getGameState()); // state should be "running"
  }

  @Test
  void testStartGame_NonHostUser() throws IOException {
    User hostUser = mock(User.class);
    User nonHostUser = mock(User.class);

    when(hostUser.getOut()).thenReturn(mock(OutputStream.class));
    when(nonHostUser.getOut()).thenReturn(mock(OutputStream.class));

    when(userList.getUserName(userId)).thenReturn("nonHost");
    when(userList.getUser(userId)).thenReturn(nonHostUser);
    when(protocolWriterServer.sendInfo(anyString())).thenReturn(true);

    lobby.startGame(userId);

    verify(protocolWriterServer, times(1)).sendInfo("Only the host can start the game.");
  }

  @Test
  void testStartGame_GameAlreadyStarted() throws IOException, NoSuchFieldException, IllegalAccessException {
    User hostUser = mock(User.class);

    when(hostUser.getOut()).thenReturn(mock(OutputStream.class));

    when(userList.getUserName(userId)).thenReturn("host");
    when(userList.getUser(userId)).thenReturn(hostUser);

    setPrivateField(lobby, "gamestate", 2);

    lobby.startGame(userId);

    verify(protocolWriterServer, times(1)).sendInfo("Game has already started or is finished.");
  }

  @Test
  void testStartGame_WelcomeLobby() throws IOException, NoSuchFieldException, IllegalAccessException {
    User hostUser = mock(User.class);

    when(hostUser.getOut()).thenReturn(mock(OutputStream.class));

    when(userList.getUserName(userId)).thenReturn("host");
    when(userList.getUser(userId)).thenReturn(hostUser);

    setPrivateField(lobby, "lobbyName", "Welcome");

    lobby.startGame(userId);

    verify(protocolWriterServer, times(1)).sendInfo("You are not in a real lobby. Please join or create a lobby to start a game.");
  }

  @Test
  void testStartGame_NotEnoughPlayers() throws IOException, NoSuchFieldException, IllegalAccessException {
    User hostUser = mock(User.class);

    when(hostUser.getOut()).thenReturn(mock(OutputStream.class));

    when(userList.getUserName(userId)).thenReturn("host");
    when(userList.getUser(userId)).thenReturn(hostUser);

    setPrivateField(lobby, "players", new ArrayList<>(List.of("host")));

    lobby.startGame(userId);

    verify(protocolWriterServer, times(1)).sendInfo("At least 2 players are required to start the game.");
  }

  @Test
  void testStartGame_SuccessfulStart() throws IOException {
    User hostUser = mock(User.class);
    User player2 = mock(User.class);

    when(hostUser.getOut()).thenReturn(mock(OutputStream.class));
    when(player2.getOut()).thenReturn(mock(OutputStream.class));

    when(userList.getUserName(userId)).thenReturn("host");
    when(userList.getUser(userId)).thenReturn(hostUser);
    when(userList.getUserByName("host")).thenReturn(hostUser);
    when(userList.getUserByName("player2")).thenReturn(player2);

    lobby.startGame(userId);

    assertEquals(2, lobby.getGameState());
    verify(protocolWriterServer, times(2)).sendCommand(Command.STRT);
  }
}

