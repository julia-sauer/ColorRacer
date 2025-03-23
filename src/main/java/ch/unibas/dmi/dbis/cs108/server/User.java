package ch.unibas.dmi.dbis.cs108.server;

import java.io.OutputStream;

/**
 * Diese Klasse repräsentiert einen verbundenen Benutzer.
 * Sie speichert die Benutzer-ID, den aktuellen Nicknamen und einen OutputStream,
 * der für die Kommunikation mit dem Client verwendet wird.
 * Wird in der Benutzerverwaltung (UserList) verwendet, um gezielt Nachrichten
 * an bestimmte Clients zu senden oder deren Daten zu verwalten.
 *
 * @author Milo
 */
public class User {
    private final int id; // Eindeutige Benutzer-ID
    private String nickname; // Aktueller Nickname des Benutzers
    private final OutputStream out; // OutputStream zur Kommunikation mit dem Client

    /**
     * Konstruktor zum Erstellen eines neuen Benutzers.
     *
     * @param id Die eindeutige Benutzer-ID
     * @param nickname Der Nickname des Benutzers
     * @param out Der OutputStream zum Senden von Nachrichten an den Client
     */
    public User(int id, String nickname, OutputStream out) {
        this.id = id;
        this.nickname = nickname;
        this.out = out;
    }

    /**
     * Gibt die Benutzer-ID zurück.
     * @return Die ID des Benutzers
     */
    public int getId() {
        return id;
    }

    /**
     * Gibt den aktuellen Nicknamen des Benutzers zurück.
     * @return Der Nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Setzt einen neuen Nicknamen für den Benutzer.
     * @param nickname Der neue Nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Gibt den OutputStream zurück, der für die Kommunikation mit dem Client verwendet wird.
     * @return Der OutputStream
     */
    public OutputStream getOut() {
        return out;
    }
}
