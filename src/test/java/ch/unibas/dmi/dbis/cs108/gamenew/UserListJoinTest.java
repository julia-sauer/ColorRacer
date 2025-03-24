package ch.unibas.dmi.dbis.cs108.gamenew;

package ch.unibas.dmi.dbis.cs108.server;

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
}
