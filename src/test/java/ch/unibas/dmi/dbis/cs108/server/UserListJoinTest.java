package ch.unibas.dmi.dbis.cs108.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Unit tests for joining users using {@link UserList}. This class ensures nicknames are handled
 * correctly, including uniqueness and suffixing logic.
 */
public class UserListJoinTest {

  /**
   * Setup method called before each test. Currently empty; can be used to clear/reset the UserList
   * if the implementation allows it.
   */
  @BeforeEach
  public void resetUserList() {
    // Optionally clear UserList here if reset support is added in the future.
  }

  /**
   * Test that adding a user with a unique nickname stores the user correctly in the UserList.
   */
  @Test
  public void testAddUserWithUniqueNickname() {
    OutputStream dummyOut = new ByteArrayOutputStream();
    int userId = UserList.addUser("Anna", dummyOut); // Add a user with unique nickname

    User user = UserList.getUser(userId);
    assertNotNull(user, "User should not be null after adding.");
    assertEquals("Anna", user.getNickname(), "Nickname should match the one provided.");
  }

  /**
   * Test that when a duplicate nickname is attempted, the system assigns a unique suffix. For
   * example: if "Anna" already exists, the next becomes "Anna1".
   */
  @Test
  public void testDuplicateNicknameGetsSuffix() {
    OutputStream out1 = new ByteArrayOutputStream();
    OutputStream out2 = new ByteArrayOutputStream();

    UserList.addUser("Anna", out1); // First user gets "Anna"

    // Check for the next available nickname suffix
    String nickname = "Anna";
    String finalNick = nickname;
    int suffix = 1;
    while (UserList.containsUserName(finalNick)) {
      finalNick = nickname + suffix++;
    }

    int userId2 = UserList.addUser(finalNick, out2);
    User user2 = UserList.getUser(userId2);

    assertEquals("Anna1", user2.getNickname(), "Nickname should be suffixed to ensure uniqueness.");
  }

  /**
   * Tests if {@code containsUserName()} correctly identifies registered nicknames.
   */
  @Test
  public void testNicknameExists() {
    OutputStream dummyOut = new ByteArrayOutputStream();
    UserList.addUser("Max", dummyOut);

    assertTrue(UserList.containsUserName("Max"),
        "Max should be recognized as a registered nickname.");
    assertFalse(UserList.containsUserName("Moritz"),
        "Moritz should not be found since it was not added.");
  }

}
