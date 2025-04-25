package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.*;

import java.io.OutputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class MoveToSelectedFieldTest {

  private OutputStream mockOut;
  private ProtocolWriterServer mockWriter;
  private User user;
  private GameBoard board;
  private Lobby lobby;

  @BeforeEach
  void setup() {
    mockOut = mock(OutputStream.class);
    mockWriter = mock(ProtocolWriterServer.class);

    Server.clientWriters.clear();
    Server.protocolWriters.clear();
    Server.lobbies.clear();
    UserList.clear();

    Server.colors = new String[]{"purple", "yellow", "blue"};

    int userId = UserList.addUser("testPlayer", mockOut);
    user = UserList.getUser(userId);
    Server.protocolWriters.put(user.getOut(), mockWriter);

    lobby = new Lobby("TestLobby");
    lobby.addPlayers(userId);
    Server.lobbies.add(lobby);

    board = lobby.getGameBoard("testPlayer");
    Field field = board.getFieldById("purple1");
    board.addSelectedField(field);
  }

  @AfterEach
  void cleanup() {
    Server.lobbies.clear();
    Server.protocolWriters.clear();
    Server.clientWriters.clear();
    UserList.clear();
  }

  @Test
  void testUserIsNull() throws Exception {
    Server.moveToLastSelectedField(-1); // no such user
    // nothing to verify, just check no crash
  }

  @Test
  void testNoSelectedFields() throws Exception {
    board.moveToLastSelected(); // clears fields
    Server.moveToLastSelectedField(1);

    verify(mockWriter).sendInfo(contains("select at least one field"));
  }


  @Test
  void testUserNotInLobby() throws Exception {
    Server.lobbies.clear(); // user is not in any lobby
    Server.moveToLastSelectedField(1);
    // should simply return without errors
  }

  @Test
  void testMoveTriggersWin() throws Exception {
    // Simulate move to winning field
    Field winningField = board.getFieldById("purple2"); // purple2 triggers win
    board.moveToLastSelected();
    board.addSelectedField(winningField);

    Server.moveToLastSelectedField(1);
    // no assert needed unless won() effect observable
  }

  @Test
  void testIOExceptionDuringBroadcast() throws Exception {
    doThrow(new IOException("fail")).when(mockWriter).sendCommandAndString(any(), any());
    Server.moveToLastSelectedField(1);
    // expect no crash even if IOException thrown
  }
}
