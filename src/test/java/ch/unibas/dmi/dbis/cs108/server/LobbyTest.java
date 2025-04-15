package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.game.GameBoard;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LobbyTest {

    private Lobby lobby;

    @BeforeEach
    void setup() {
        lobby = new Lobby("TestLobby");
    }

    @Test
    void testAddPlayersAddsFirstAsHost() {
        UserList.addUser("Alice", mock(OutputStream.class));
        boolean result = lobby.addPlayers(1);
        assertTrue(result);
        assertEquals("Alice", lobby.getHostName());
        assertEquals(List.of("Alice"), lobby.getPlayers());
    }

    @Test
    void testLobbyRejectsMoreThanMaxPlayers() {
        for (int i = 1; i <= Lobby.MAX_PLAYERS; i++) {
            UserList.addUser("Player" + i, mock(OutputStream.class));
            assertTrue(lobby.addPlayers(i));
        }
        UserList.addUser("Extra", mock(OutputStream.class));
        assertFalse(lobby.addPlayers(Lobby.MAX_PLAYERS + 1));
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
        UserList.addUser("ReadyPlayer", mock(OutputStream.class));
        lobby.addPlayers(1);
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
        UserList.addUser("Alice", mock(OutputStream.class));
        UserList.addUser("Bob", mock(OutputStream.class));
        lobby.addPlayers(1);
        lobby.addPlayers(2);

        assertEquals(2, lobby.getPlayers().size());
        lobby.removePlayer("Alice");
        assertEquals(1, lobby.getPlayers().size());
        assertFalse(lobby.getPlayers().contains("Alice"));
    }

    @Test
    void testGetGameBoardForPlayer() {
        UserList.addUser("Alice", mock(OutputStream.class));
        lobby.addPlayers(1);

        GameBoard board = lobby.getGameBoard("Alice");
        assertNotNull(board);
        assertEquals("white1", board.getCurrentField().getFieldId());
    }

    @Test
    void testAdvanceTurnSkipsWinner() {
        UserList.addUser("A", mock(OutputStream.class));
        UserList.addUser("B", mock(OutputStream.class));
        UserList.addUser("C", mock(OutputStream.class));

        lobby.addPlayers(1); // A
        lobby.addPlayers(2); // B
        lobby.addPlayers(3); // C

        lobby.addWinner("B"); // Skip B

        // Set up A as current
        lobby.advanceTurn(); // Should skip B and go to C
        String expected = "C";
        assertTrue(lobby.isCurrentPlayer(expected));
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
}

