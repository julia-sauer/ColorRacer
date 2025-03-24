package ch.unibas.dmi.dbis.cs108.server;

import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Diese Klasse verwaltet eine Liste von Benutzern, die mit dem Server verbunden sind.
 * Sie stellt Methoden zum Hinzufügen, Entfernen und Abrufen von Benutzerinformationen bereit.
 * @author milo
 */
public class UserList {
    private static final ConcurrentHashMap<Integer, User> userMap = new ConcurrentHashMap<>();
    private static final AtomicInteger userIdCounter = new AtomicInteger(0);

    /**
     * Fügt einen neuen Benutzer zur Liste hinzu.
     * @param userName Der Name des hinzuzufügenden Benutzers.
     * @param out Der OutputStream zur Kommunikation mit dem Benutzer.
     * @return Die eindeutige Benutzer-ID, die dem neuen Benutzer zugewiesen wurde.
     */
    public static int addUser(String userName, OutputStream out) {
        int userId = userIdCounter.incrementAndGet();
        userMap.put(userId, new User(userId, userName, out));
        return userId;
    }

    /**
     * Entfernt einen Benutzer aus der Liste.
     * @param userId Die ID des zu entfernenden Benutzers.
     * @return Der Name des entfernten Benutzers, oder null, wenn der Benutzer nicht gefunden wurde.
     */
    public static String removeUser(int userId) {
        User user = userMap.remove(userId);
        return (user != null) ? user.getNickname() : null;
    }

    /**
     * Ruft den Namen eines Benutzers anhand seiner ID ab.
     * @param userId Die ID des abzurufenden Benutzers.
     * @return Der Name des Benutzers, oder null, wenn der Benutzer nicht gefunden wurde.
     */
    public static String getUserName(int userId) {
        User user = userMap.get(userId);
        return (user != null) ? user.getNickname() : null;
    }

    /**
     * Gibt die Gesamtanzahl der Benutzer in der Liste zurück.
     * @return Die Anzahl der Benutzer.
     */
    public static int getUserCount() {
        return userMap.size();
    }

    /**
     * Überprüft, ob ein Nickname bereits in der UserList vorhanden ist.
     * @param nickname Der zu überprüfende Nickname.
     * @return true, wenn der Nickname bereits existiert, sonst false.
     */
    public static boolean containsUserName(String nickname) {
        return userMap.values().stream()
                .anyMatch(user -> user.getNickname().equals(nickname));
    }

    /**
     * Ändert den Nickname eines Benutzers basierend auf der Benutzer-ID.
     * @param userId Die ID des Benutzers.
     * @param newNickname Der neue Nickname.
     */
    public static void updateUserName(int userId, String newNickname) {
        User user = userMap.get(userId);
        if (user != null) {
            user.setNickname(newNickname);
        }
    }

    /**
     * Gibt das User-Objekt zu einer Benutzer-ID zurück.
     * @param userId Die ID des Benutzers.
     * @return Das User-Objekt oder null, wenn nicht gefunden.
     */
    public static User getUser(int userId) {
        return userMap.get(userId);
    }
}
