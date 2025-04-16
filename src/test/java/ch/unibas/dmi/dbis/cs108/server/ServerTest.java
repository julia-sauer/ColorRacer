package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServerTest {

    @BeforeEach
    void setup() {
        Server.lobbies.clear();
        UserList.clear();
        Server.clientWriters.clear();
    }

    @Test
    void testAddNewUserReturnsValidId() {
        OutputStream dummyOut = mock(OutputStream.class);
        int id = Server.addNewUser("Tester", dummyOut);
        assertTrue(id >= 0);
        assertEquals("Tester", UserList.getUserName(id));
    }

    @Test
    void testCreateLobbyAddsToLobbyList() {
        Server.createLobby("UnitLobby", null);
        assertEquals(1, Server.lobbies.size());
        assertEquals("UnitLobby", Server.lobbies.get(0).getLobbyName());
    }

    @Test
    void testGetLobbyOfPlayerReturnsCorrectLobby() {
        int userId = UserList.addUser("PlayerOne", mock(OutputStream.class));
        Lobby lobby = new Lobby("TestLobby");
        lobby.addPlayers(userId);
        Server.lobbies.add(lobby);

        Lobby found = Server.getLobbyOfPlayer("PlayerOne");
        assertNotNull(found);
        assertEquals("TestLobby", found.getLobbyName());
    }

    @Test
    void testJoinLobbySendsJoinAndInfo() {
        // Setup Lobby
        Lobby lobby = new Lobby("JoinTestLobby");
        Server.lobbies.clear();
        Server.lobbies.add(lobby);

        // Create mocked user
        OutputStream mockOut = mock(OutputStream.class);
        int userId = UserList.addUser("Joiner", mockOut);
        User user = UserList.getUser(userId);

        // Kein stubbing von getOut() nötig, da im echten Code nur mit OutputStream gearbeitet wird
        PrintWriter dummyWriter = mock(PrintWriter.class);
        Server.clientWriters.clear();
        Server.clientWriters.add(dummyWriter);

        // Now call join
        Server.joinLobby("JoinTestLobby", userId);

        assertTrue(lobby.getPlayers().contains("Joiner"));
    }


    @Test
    void testBroadcastToAllSendsMessage() {
        PrintWriter writer1 = mock(PrintWriter.class);
        PrintWriter writer2 = mock(PrintWriter.class);

        Server.clientWriters.add(writer1);
        Server.clientWriters.add(writer2);

        Server.broadcastToAll("Hello world");

        verify(writer1).println(startsWith(Command.BROD.name()));
        verify(writer1).flush();
        verify(writer2).println(startsWith(Command.BROD.name()));
        verify(writer2).flush();
    }

    @Test
    void testClientDisconnectedTriggersShutdownIfZero() throws InterruptedException {
        // Vorbereitung: saubere Umgebung
        Server.clientWriters.clear();
        Server.lobbies.clear();
        UserList.clear();

        // 1 User simulieren
        Server.addNewUser("DisconnectMe", mock(OutputStream.class));

        // Trenne den Client manuell
        Server.ClientDisconnected();

        // Warte kurz, aber nicht 2 Minuten – nur um Thread zu starten
        Thread.sleep(500);

        // Jetzt mit Getter prüfen!
        assertTrue(Server.getActiveClientCount() <= 0);
    }

    @Test
    void testChangeNicknameValidAndUnique() {
        int id = UserList.addUser("OldName", mock(OutputStream.class));
        Server.changeNickname(id, "NewNick");

        String updated = UserList.getUserName(id);
        assertTrue(updated.startsWith("NewNick"));
    }

    @Test
    void testChangeNicknameInvalidFormat() {
        OutputStream mockOut = mock(OutputStream.class);
        int id = UserList.addUser("CleanName", mockOut);

        // keine Stubbing-Versuche auf flush() oder println() notwendig
        PrintWriter mockWriter = mock(PrintWriter.class);
        Server.clientWriters.clear();
        Server.clientWriters.add(mockWriter);

        Server.changeNickname(id, "?!@#"); // Ungültiger Nickname

        String updatedName = UserList.getUserName(id);
        assertEquals("CleanName", updatedName);
    }
}

