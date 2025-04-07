package ch.unibas.dmi.dbis.cs108.server;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class manages a list of users connected to the server.
 * It provides methods for adding, removing, and retrieving user information.
 * @author milo
 */
public class UserList {
    private static final ConcurrentHashMap<Integer, User> userMap = new ConcurrentHashMap<>();
    private static final AtomicInteger userIdCounter = new AtomicInteger(0);

    /**
     * Adds a new user to the list.
     * @param userName The name of the user to be added.
     * @param out The OutputStream for communicating with the user.
     * @return The unique user ID assigned to the new user.
     */

    public static int addUser(String userName, OutputStream out) {
        int userId = userIdCounter.incrementAndGet();
        userMap.put(userId, new User(userId, userName, out));
        return userId;
    }

    /**
     * Removes a user from the list.
     * @param userId The ID of the user to be removed.
     * @return The name of the removed user, or null if the user was not found.
     */

    public static String removeUser(int userId) {
        User user = userMap.remove(userId);
        return (user != null) ? user.getNickname() : null;
    }

    /**
     * Retrieves a user's name based on their ID.
     * @param userId The ID of the user to retrieve.
     * @return The user's name, or null if the user is not found.
     */

    public static String getUserName(int userId) {
        User user = userMap.get(userId);
        return (user != null) ? user.getNickname() : null;
    }

    /**
     * Returns the total number of users in the list.
     * @return The number of users.
     */

    public static int getUserCount() {
        return userMap.size();
    }

    /**
     * Checks whether a nickname already exists in the UserList.
     * @param nickname The nickname to check.
     * @return true if the nickname already exists, false otherwise.
     */

    public static boolean containsUserName(String nickname) {
        return userMap.values().stream()
                .anyMatch(user -> user.getNickname().equals(nickname));
    }

    /**
     * Updates the user's new nickname
     * @param userId the user's ID so the server knows which name to change
     * @param newNickname is the new nickname the user has chosen
     */

    public static void updateUserName(int userId, String newNickname) {
        User user = userMap.get(userId);
        if (user != null) {
            user.setNickname(newNickname);
        }
    }

    /**
     * Returns the User object for a user ID.
     * @param userId The user ID.
     * @return The User object or null if not found.
     */

    public static User getUser(int userId) {
        return userMap.get(userId);
    }

    /**
     * This method can be used to retrieve the user's ID if only the user's nickname is known. This is done
     * with a for-loop, which checks all User-objects. If the specified nickname matches one in the list,
     * the ID of the user is returned.
     * @param nickname  The nickname of the user from whom the ID is to be retrieved.
     * @return          The ID of the user with the specific nickname.
     */
    public static int getUserId(String nickname) {
        for (User user : userMap.values()) {
            if (user.getNickname().equalsIgnoreCase(nickname)) {
                return user.getId();
            }
        }
        // Handle case where user is not found, e.g., throw exception or return a special value.
        System.err.println("User with nickname '" + nickname + "' not found.");
        return -1;
    }
    /**
     * Gives back the user-object that corresponds to the given nickname.
     *
     * @param nickname nickname of searched user.
     * @return the user-object or null, if no user with such a nickname exists.
     */
    public static User getUserByName(String nickname) {
        for (User user : userMap.values()) {
            if (user.getNickname().equalsIgnoreCase(nickname)) {
                return user;
            }
        }
        System.err.println("User with nickname '" + nickname + "' not found.");
        return null;
    }
    /**
     * Returns a list of all currently connected users' nicknames.
     *
     * @return List of usernames
     */
    public static List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        for (User user : userMap.values()) {
            usernames.add(user.getNickname());
        }
        return usernames;
    }

}
