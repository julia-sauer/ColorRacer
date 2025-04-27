package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.GameBoard;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import java.io.OutputStream;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class WonTest {

  private Lobby lobby;
  private User mockUser;
  private User secondMockUser;
  private ProtocolWriterServer mockWriter;

  @BeforeEach
  void setUp() {
    // Set up mock user and add them to the UserList
    OutputStream mockOut = mock(OutputStream.class);
    mockUser = mock(User.class);
    when(mockUser.getOut()).thenReturn(mockOut);
    when(mockUser.getNickname()).thenReturn("testPlayer");

    // Add the mock user to UserList
    UserList.clear();
    UserList.addUser("testPlayer", mockOut); // Make sure the user is added

    // Create the lobby and add players
    lobby = new Lobby("TestLobby");
    lobby.addPlayers(1); // Adds testPlayer
    Server.lobbies.add(lobby);

    // Set up a game board
    GameBoard board = lobby.getGameBoard("testPlayer");
    Server.colors = new String[]{"purple", "yellow", "blue"};

    // Mock ProtocolWriterServer for user
    mockWriter = mock(ProtocolWriterServer.class);
    Server.protocolWriters.put(mockOut, mockWriter);
  }


  @AfterEach
  void cleanup() {
    // Clear the lobby and users after each test to prevent state leaks
    Server.lobbies.clear();
    UserList.clear();
    Server.protocolWriters.clear();
  }


}
