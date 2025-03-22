package ch.unibas.dmi.dbis.cs108.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Diese Klasse verwaltet eine Liste von Benutzern, die mit dem Server verbunden sind.
 * Sie stellt Methoden zum Hinzufügen, Entfernen und Abrufen von Benutzerinformationen bereit.
 * @author milo
 */
public class UserList {
    private static final ConcurrentHashMap<Integer, String> userMap = new ConcurrentHashMap<>();
    private static final AtomicInteger userIdCounter = new AtomicInteger(0);

    /**
     * Fügt einen neuen Benutzer zur Liste hinzu.
     * @param userName Der Name des hinzuzufügenden Benutzers.
     * @return Die eindeutige Benutzer-ID, die dem neuen Benutzer zugewiesen wurde.
     */
    public static int addUser(String userName) {
        int userId = userIdCounter.incrementAndGet();
        userMap.put(userId, userName);
        return userId;
    }

    /**
     * Entfernt einen Benutzer aus der Liste.
     * @param userId Die ID des zu entfernenden Benutzers.
     * @return Der Name des entfernten Benutzers, oder null, wenn der Benutzer nicht gefunden wurde.
     */
    public static String removeUser(int userId) {
        return userMap.remove(userId);
    }

    /**
     * Ruft den Namen eines Benutzers anhand seiner ID ab.
     * @param userId Die ID des abzurufenden Benutzers.
     * @return Der Name des Benutzers, oder null, wenn der Benutzer nicht gefunden wurde.
     */
    public static String getUserName(int userId) {
        return userMap.get(userId);
    }

    /**
     * Gibt die Gesamtanzahl der Benutzer in der Liste zurück.
     * @return Die Anzahl der Benutzer.
     */
    public static int getUserCount() {
        return userMap.size();
    }
}
