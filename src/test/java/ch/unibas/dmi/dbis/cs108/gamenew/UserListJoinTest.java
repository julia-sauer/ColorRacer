package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.server.Server;
import ch.unibas.dmi.dbis.cs108.server.UserList;
import ch.unibas.dmi.dbis.cs108.server.User;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class UserListJoinTest {
    @BeforeEach
    public void resetUserList() {
        // Falls nötig, UserList leeren (siehe Hinweis unten)
    }

    @Test
    public void testAddUserWithUniqueNickname() {
        OutputStream dummyOut = new ByteArrayOutputStream();
        int userId = UserList.addUser("Anna", dummyOut);

        User user = UserList.getUser(userId);
        assertNotNull(user);
        assertEquals("Anna", user.getNickname());
    }
    @Test
    public void testDuplicateNicknameGetsSuffix() {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();

        UserList.addUser("Anna", out1);

        String nickname = "Anna";
        String finalNick = nickname;
        int suffix = 1;
        while (UserList.containsUserName(finalNick)) {
            finalNick = nickname + suffix++;
        }

        int userId2 = UserList.addUser(finalNick, out2);
        User user2 = UserList.getUser(userId2);

        assertEquals("Anna1", user2.getNickname());
    }

    @Test
    public void testNicknameExists() {
        OutputStream dummyOut = new ByteArrayOutputStream();
        UserList.addUser("Max", dummyOut);

        assertTrue(UserList.containsUserName("Max"));
        assertFalse(UserList.containsUserName("Moritz"));
    }


}
