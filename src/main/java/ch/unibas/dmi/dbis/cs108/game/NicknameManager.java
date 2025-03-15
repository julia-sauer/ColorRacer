package ch.unibas.dmi.dbis.cs108.game;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameManager {
    private static final ConcurrentHashMap<Socket, String> clientNicknames = new ConcurrentHashMap<>();

    // Setzt den Nickname basierend auf dem System-Benutzernamen
    public static String generateDefaultNickname() {
        return System.getProperty("user.name", "User"); // Holt den System-Benutzernamen
    }

    // Nickname setzen
    public static void setNickname(Socket socket, String nickname) {
        clientNicknames.put(socket, nickname);
    }

    // Nickname abrufen
    public static String getNickname(Socket socket) {
        return clientNicknames.getOrDefault(socket, "Unbekannt");
    }

    // Nickname ändern
    public static void changeNickname(Socket socket, String newNickname) {
        if (clientNicknames.containsKey(socket)) {
            clientNicknames.put(socket, newNickname);
        }
    }

    // Client entfernen
    public static void removeClient(Socket socket) {
        clientNicknames.remove(socket);
    }
}

