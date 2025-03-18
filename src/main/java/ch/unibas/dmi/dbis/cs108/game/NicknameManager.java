package ch.unibas.dmi.dbis.cs108.game;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameManager {
    private static final ConcurrentHashMap<String, String> clientNicknames = new ConcurrentHashMap<>();

    // Generate a default nickname based on the system username
    public static String generateDefaultNickname() {
        return System.getProperty("user.name", "User"); // Gets system username
    }

    // Check if a nickname is already in use
    public static boolean isNicknameTaken(String nickname) {
        return clientNicknames.containsValue(nickname);
    }

    // Validate nickname format
    public static boolean isValidNickname(String nickname) {
        return nickname.matches("[A-Za-z0-9_-]{3,15}"); // Only letters, numbers, '-', and '_'
    }

    // Set nickname for a client
    public static void setNickname(Socket socket, String nickname) {
        if (!isValidNickname(nickname)) {
            throw new IllegalArgumentException("Invalid nickname. Only letters, numbers, '-', and '_' are allowed.");
        }
        if (isNicknameTaken(nickname)) {
            throw new IllegalArgumentException("Nickname already in use!");
        }
        clientNicknames.put(socket.toString(), nickname);
    }

    // Get nickname of a client
    public static String getNickname(Socket socket) {
        return clientNicknames.getOrDefault(socket.toString(), "Unknown");
    }

    // Change a client's nickname
    public static void changeNickname(Socket socket, String newNickname) {
        if (!isValidNickname(newNickname)) {
            throw new IllegalArgumentException("Invalid nickname format.");
        }
        if (isNicknameTaken(newNickname)) {
            throw new IllegalArgumentException("Nickname already in use!");
        }
        if (clientNicknames.containsKey(socket.toString())) {
            clientNicknames.put(socket.toString(), newNickname);
        }
    }

    // Remove a client from the nickname list
    public static void removeClient(Socket socket) {
        clientNicknames.remove(socket.toString());
    }
}

