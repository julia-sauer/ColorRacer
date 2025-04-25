package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.*;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import java.io.IOException;
import org.junit.jupiter.api.*;

import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class DeselectFieldTest {

  private OutputStream mockOut;
  private ProtocolWriterServer mockWriter;
  private Lobby lobby;
  private GameBoard board;

  @BeforeEach
  void setup() {
    mockOut = mock(OutputStream.class);
    mockWriter = mock(ProtocolWriterServer.class);

    UserList.clear();
    Server.lobbies.clear();
    Server.clientWriters.clear();
    Server.protocolWriters.clear();

    UserList.addUser("testPlayer", mockOut);
    Server.protocolWriters.put(mockOut, mockWriter);

    lobby = new Lobby("TestLobby");
    lobby.addPlayers(1);
    Server.lobbies.add(lobby);

    board = lobby.getGameBoard("testPlayer");

    // Farben setzen, sonst NullPointer!
    Server.colors = new String[]{"purple", "yellow", "blue"};
  }

  @AfterEach
  void cleanup() {
    UserList.clear();
    Server.lobbies.clear();
    Server.clientWriters.clear();
    Server.protocolWriters.clear();
  }

  @Test
  void testSelectedFieldsEmpty() throws Exception {
    // Noch kein Feld ausgewählt => selectedFieldsEmpty() == true

    Server.deselectField(1, "purple1");

    verify(mockWriter).sendInfo(contains("select a field first"));
  }

  @Test
  void testDeselectedFieldNotSelected() throws Exception {
    User user = UserList.getUser(1);

    GameBoard board = lobby.getGameBoard("testPlayer");
    Field anotherField = board.getFieldById("yellow1");
    board.addSelectedField(anotherField); // irgendein anderes Feld ausgewählt

    // jetzt selectedFields NICHT leer, aber falsches Feld
    Server.deselectField(1, "purple1"); // purple1 ist NICHT ausgewählt

    verify(mockWriter).sendInfo(contains("have not chosen this field"));
  }


  @Test
  void testValidDeselect() throws Exception {
    // Feld markieren (richtig auswählen)
    Field someField = board.getFieldById("purple1");
    board.addSelectedField(someField); // jetzt selectedFields enthält purple1

    Server.deselectField(1, someField.getFieldId());

    verify(mockWriter).sendCommandAndString(eq(Command.DEOS), contains("purple1"));
  }

  @Test
  void testDeselectSendFails() throws Exception {
    // Wieder ein gültiges Szenario aufbauen
    Field someField = board.getFieldById("purple1");
    board.addSelectedField(someField);

    ProtocolWriterServer mockWriterFail = mock(ProtocolWriterServer.class);
    doThrow(new java.io.IOException("Simulated send failure")).when(mockWriterFail)
        .sendCommandAndString(eq(Command.DEOS), anyString());

    // Jetzt überschreiben wir den writer für diesen Test
    Server.protocolWriters.put(mockOut, mockWriterFail);

    Server.deselectField(1, someField.getFieldId());

    // Hier kein verify nötig, weil Exception erwartet → catch wird getroffen
  }
  @Test
  void testCreatesNewProtocolWriterIfNoneExists() throws Exception {
    User user = UserList.getUser(1);
    ProtocolWriterServer newWriterSpy = spy(new ProtocolWriterServer(Server.clientWriters, user.getOut()));
    Server.protocolWriters.clear(); // simulate missing writer

    // Inject spy manually after creation to track interaction
    Server.deselectField(1, "purple1");

    // It should be in the map now:
    assertNotNull(Server.protocolWriters.get(user.getOut()));
  }

  @Test
  void testIOExceptionOnEmptySelectedFieldsMessage() throws Exception {
    User user = UserList.getUser(1);
    GameBoard board = lobby.getGameBoard("testPlayer");

    ProtocolWriterServer faultyWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Mocked")).when(faultyWriter).sendInfo(contains("select a field"));
    Server.protocolWriters.put(user.getOut(), faultyWriter);

    // selectedFields is empty
    Server.deselectField(1, "purple1");

    // Jacoco hits the catch
  }

  @Test
  void testIOExceptionOnDeselectedFieldNotInSelectedFieldsMessage() throws Exception {
    User user = UserList.getUser(1);
    GameBoard board = lobby.getGameBoard("testPlayer");
    board.addSelectedField(board.getFieldById("yellow1")); // add a different field

    ProtocolWriterServer faultyWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Mocked")).when(faultyWriter).sendInfo(contains("not chosen this field"));
    Server.protocolWriters.put(user.getOut(), faultyWriter);

    Server.deselectField(1, "purple1");
  }

  @Test
  void testIOExceptionOnDEOSCommand() throws Exception {
    User user = UserList.getUser(1);
    GameBoard board = lobby.getGameBoard("testPlayer");
    Field field = board.getFieldById("purple1");
    board.addSelectedField(field);

    ProtocolWriterServer faultyWriter = mock(ProtocolWriterServer.class);
    doThrow(new IOException("Mocked")).when(faultyWriter)
        .sendCommandAndString(eq(Command.DEOS), contains("purple1"));
    Server.protocolWriters.put(user.getOut(), faultyWriter);

    Server.colors = new String[]{"purple", "yellow", "blue"};

    Server.deselectField(1, "purple1");
  }
  @Test
  void testUserNotInAnyLobby() {
    User user = UserList.getUser(1);
    Server.lobbies.clear(); // simulate no lobbies

    // No error, no interaction, just covers the return
    Server.deselectField(1, "purple1");
  }

}
