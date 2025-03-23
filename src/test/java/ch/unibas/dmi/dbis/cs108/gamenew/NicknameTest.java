package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.server.Server;
import ch.unibas.dmi.dbis.cs108.server.UserList;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test class for nickname functionality using real OutputStreams (no Mockito).
 */
public class NicknameTest {

    private ByteArrayOutputStream testOut;
    private PrintStream originalErr;

    @BeforeEach
    public void setup() {
        testOut = new ByteArrayOutputStream();
        originalErr = System.err;
        System.setErr(new PrintStream(new ByteArrayOutputStream())); // suppress unwanted stderr
    }

    @AfterEach
    public void cleanup() {
        System.setErr(originalErr);
    }

    @Test
    public void testValidNicknameUpdatesUserList() throws IOException {
        int userId = UserList.addUser("OldName", testOut);

        Server.changeNickname(userId, "NewNick");

        assertEquals("NewNick", UserList.getUserName(userId));
        String output = testOut.toString("UTF-8");
        assertTrue(output.contains("NICKNewNick"));
    }

    @Test
    public void testInvalidNicknameDoesNotUpdate() throws IOException {
        int userId = UserList.addUser("Initial", testOut);

        Server.changeNickname(userId, "!!");

        assertEquals("Initial", UserList.getUserName(userId));
        String output = testOut.toString("UTF-8");
        assertTrue(output.contains("CHATInvalid nickname"));
    }

    @Test
    public void testDuplicateNicknamesGetSuffix() throws IOException {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();

        int user1 = UserList.addUser("User1", out1);
        int user2 = UserList.addUser("User2", out2);

        Server.changeNickname(user1, "Name");
        Server.changeNickname(user2, "Name");

        assertEquals("Name", UserList.getUserName(user1));
        assertEquals("Name1", UserList.getUserName(user2));
    }
}
