package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 8090;
    private static final int MAX_CLIENTS = 4;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static ServerSocket echod;
    private static boolean running = true;

    public static void startChatServer () {
        try {
            echod = new ServerSocket(PORT);
            System.out.println("ChatServer läuft auf Port: " + PORT + "...");

            while (running) {
                if (clients.size() < MAX_CLIENTS) {
                    Socket socket = echod.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());

                    ClientHandler clientHandler = new ClientHandler(socket);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void broadcastMessage(String message, ClientHandler sender) {
        message = message.trim(); // Remove unnecessary spaces

        //  Reject empty messages
        if (message.isEmpty()) {
            sender.sendMessage("ERROR: Message cannot be empty.");
            return;
        }

        //  Validate allowed characters (prevent SQL injection, special control characters)
        if (!message.matches("[A-Za-z0-9_?!.,:;()\\- ]+")) {
            sender.sendMessage("ERROR: Invalid characters in message.");
            return;
        }

        //  Prevent overly long messages (spam prevention)
        if (message.length() > 500) {
            sender.sendMessage("ERROR: Message is too long (max 500 characters).");
            return;
        }

        // Send validated message to all clients
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }


    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Ein Client hat sich getrennt. Aktive Clients: " + clients.size());

        if (clients.isEmpty()) {
            System.out.println("Alle Clients haben sich getrennt. Server wird heruntergefahren!");
            shutdownServer();
        }
    }

    public static void shutdownServer() {
        try {
            System.out.println("Shutting down ChatServer...");
            running = false;
            echod.close();
            System.exit(0);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

