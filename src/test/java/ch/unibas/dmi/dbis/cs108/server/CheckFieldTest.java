package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckFieldTest {

  private User mockUser;
  private OutputStream mockOut;
  private ProtocolWriterServer mockWriter;
  private Lobby lobby;
  private GameBoard board;


  @BeforeEach
  void setup() {
    mockOut = mock(OutputStream.class);
    mockUser = mock(User.class);
    when(mockUser.getOut()).thenReturn(mockOut);
    when(mockUser.getNickname()).thenReturn("testPlayer");

    UserList.clear();
    UserList.addUser("testPlayer", mockOut); // Hier wird eine echte User-Instanz gemacht, mock ignoriert

    lobby = new Lobby("TestLobby");
    lobby.addPlayers(1);
    Server.lobbies.add(lobby);

    board = lobby.getGameBoard("testPlayer");
    Server.colors = new String[]{"purple", "yellow", "blue"};

    mockWriter = mock(ProtocolWriterServer.class);
    Server.protocolWriters.clear();
    Server.protocolWriters.put(mockOut, mockWriter);
  }



  @AfterEach
  void cleanup() {
    Server.lobbies.clear();
    UserList.clear();
    Server.clientWriters.clear();
  }

  @Test
  void testCheckFieldUserHasNotRolled() throws Exception {
    UserList.getUser(1).setHasRolled(false); // Der echte User!

    Server.checkField(1, "purple1");

    verify(mockWriter).sendInfo(contains("You need to roll first."));
  }



  @Test
  void testCheckFieldValidField() throws Exception {
    UserList.getUser(1).setHasRolled(true); // Jetzt darf er

    Server.checkField(1, "purple1");

    verify(mockWriter).sendCommandAndString(eq(Command.CHOS), contains("purple1"));
  }


  @Test
  void testCheckFieldInvalidField() throws Exception {
    UserList.getUser(1).setHasRolled(true); // Jetzt darf er

    Server.checkField(1, "nonExistingField");

    verify(mockWriter).sendCommandAndString(eq(Command.INFO), contains("Field is invalid"));
  }
  @Test
  void testCheckFieldRollMessageThrowsIOException() throws Exception {
    User user = UserList.getUser(1);
    user.setHasRolled(false);

    ProtocolWriterServer mockWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Simulated IO error")).when(mockWriter).sendInfo(anyString());
    Server.protocolWriters.put(user.getOut(), mockWriter);

    // Sollte keine Exception werfen
    Server.checkField(1, "purple1");
    // Hier kein verify, da wir IOException forcieren – Jacoco springt in den catch
  }
  @Test
  void testCheckFieldUserNotInLobby() throws Exception {
    User user = UserList.getUser(1);
    user.setHasRolled(true);

    // Entferne alle Lobbies → User ist in keiner
    Server.lobbies.clear();

    // Sollte einfach returnen
    Server.checkField(1, "purple1");
    // Keine Exception, deckt Zeile `if (userLobby == null)`
  }
  @Test
  void testCheckFieldCHOSThrowsIOException() throws Exception {
    User user = UserList.getUser(1);
    user.setHasRolled(true);

    GameBoard board = lobby.getGameBoard("testPlayer");
    Field purple1 = board.getFieldById("purple1");
    board.setCurrentField(purple1); // currentField gesetzt

    Server.colors = new String[]{"purple", "yellow", "blue"};

    ProtocolWriterServer mockWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Simulated CHOS error"))
        .when(mockWriter).sendCommandAndString(eq(Command.CHOS), anyString());
    Server.protocolWriters.put(user.getOut(), mockWriter);

    Server.checkField(1, "purple1");
    // Kein verify nötig – Ziel: catch-Block wird durchlaufen
  }
  @Test
  void testCheckFieldInvalidFieldIOException() throws Exception {
    User user = UserList.getUser(1);
    user.setHasRolled(true);

    ProtocolWriterServer mockWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Simulated INFO error"))
        .when(mockWriter).sendCommandAndString(eq(Command.INFO), anyString());
    Server.protocolWriters.put(user.getOut(), mockWriter);

    Server.checkField(1, "nonexistentField");
    // Erwartung: keine Exception → catch wird getestet
  }
}
