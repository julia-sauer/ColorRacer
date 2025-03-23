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
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();

        int user1 = UserList.addUser("User1", out1);
        int user2 = UserList.addUser("User2", out2);
        int user3 = UserList.addUser("User3", out3);

        Server.changeNickname(user1, "Player");
        Server.changeNickname(user2, "Player");
        Server.changeNickname(user3, "Player");

        assertEquals("Player", UserList.getUserName(user1));
        assertEquals("Player1", UserList.getUserName(user2));
        assertEquals("Player2", UserList.getUserName(user3));
    }

    @Test
    public void testEmptyNicknameRejected() throws IOException {
        int userId = UserList.addUser("DefaultName", testOut);
        Server.changeNickname(userId, "");

        assertEquals("DefaultName", UserList.getUserName(userId));
        String output = testOut.toString("UTF-8");
        assertTrue(output.contains("CHATInvalid nickname"));
    }

    @Test
    public void testMaxLengthNicknameAccepted() throws IOException {
        int userId = UserList.addUser("Start", testOut);
        String maxLengthNick = "MaxLengthNick15"; // 15 characters

        Server.changeNickname(userId, maxLengthNick);

        assertEquals(maxLengthNick, UserList.getUserName(userId));
        String output = testOut.toString("UTF-8");
        assertTrue(output.contains("NICK" + maxLengthNick));
    }

    @Test
    public void testTooLongNicknameRejected() throws IOException {
        int userId = UserList.addUser("Start", testOut);
        String tooLongNick = "ThisNickIsWayTooLong123";

        Server.changeNickname(userId, tooLongNick);

        assertEquals("Start", UserList.getUserName(userId));
        String output = testOut.toString("UTF-8");
        assertTrue(output.contains("CHATInvalid nickname"));
    }

    @Test
    public void testValidNicknameWithUnderscoreAndDigits() throws IOException {
        int userId = UserList.addUser("Init", testOut);
        String nick = "user_123";

        Server.changeNickname(userId, nick);

        assertEquals(nick, UserList.getUserName(userId));
        String output = testOut.toString("UTF-8");
        assertTrue(output.contains("NICK" + nick));
    }
}
