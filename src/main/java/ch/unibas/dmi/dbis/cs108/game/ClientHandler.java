package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String clientName;
    private volatile boolean running = true;
    private ScheduledExecutorService pingScheduler;
    private volatile boolean awaitingPong = false; // Kontrolliert, ob auf eine PONG-Antwort gewartet wird

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);


            // Nickname vorschlagen (basierend auf System-Benutzername)
            String suggestedNickname = System.getProperty("user.name", "User"); // Holt den System-Benutzernamen
            writer.println("Suggested nickname: " + suggestedNickname);
            writer.println("Press ENTER, to take the suggested nickname or type in a new one:");

            // Benutzer wählt Nickname
            clientName = reader.readLine().trim();
            if (clientName.isEmpty()) {
                clientName = suggestedNickname; // Falls leer, den Vorschlag übernehmen
            }
            // Versichert, dass Nickname nur aus sicheren Zeichen besteht
            if (!clientName.matches("[A-Za-z0-9_-]{3,15}")) {
                writer.println("ERROR: Invalid nickname. Use only letters, numbers, '-' and '_'. Spaces are not allowed.");
                clientName = suggestedNickname; // Default to system username
            }
            // Nickname im NicknameManager speichern
            NicknameManager.setNickname(socket, clientName);
            System.out.println(clientName + " has joined.");
            ChatServer.broadcastMessage(clientName + " has joined the chat!", this);

            startPingScheduler(); // Starte den Ping-Mechanismus

            String message;
            while (running && (message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("QUIT")) {
                    break;
                }

                // Nickname ändern
                if (message.startsWith("/nick "))                {
                    String newNickname = message.substring(6).trim();
                    NicknameManager.changeNickname(socket, newNickname);
                    writer.println("Your new nickname is: " + newNickname);
                    clientName = newNickname; // Aktualisiere den lokalen Namen
                    continue;
                }
               //  Message Validation (Add Here!)
                if (!message.matches("[A-Za-z0-9_?!.,:;()\\-]+")) {
                    writer.println("ERROR: Invalid symbols in message.");
                    continue; // Überspringt ungültige Nachrichten
                }
                if (message.length() > 500) {
                    writer.println("ERROR: message too long.");
                    continue;
                }
                // Aktuellen Nickname für Nachrichten verwenden
                String currentNickname = NicknameManager.getNickname(socket);
                ChatServer.broadcastMessage(currentNickname + ": " + message, this);
            }
        } catch (IOException e) {
            //  Benachrichtige den Client vor dem Trennen der Verbindung
            if (writer != null) {
                writer.println("ERROR: A network problem has occurred . Please reconnect.");
            }
            //  Log the error on the server side
            System.err.println("Client connection error (" + (clientName != null ? clientName : "Unknown") + "): " + e.getMessage());



        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Log socket closure errors (but don't crash)
                System.err.println("Error closing socket for " + clientName + ": " + e.getMessage());
            }

            // Remove the client properly after disconnecting
            ChatServer.removeClient(this);
            NicknameManager.removeClient(socket);
        }

    }

    // Starte das Ping-System, das alle 5 Sekunden einen Ping sendet
    private void startPingScheduler() {
        pingScheduler = Executors.newScheduledThreadPool(1);
        pingScheduler.scheduleAtFixedRate(() -> {
            if (awaitingPong) { // Falls keine Antwort vom letzten Ping kam → Verbindung verloren
                System.out.println("Client " + clientName + " does not answer. Connection closed...");
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                sendMessage("PING");
                awaitingPong = true; // Warten auf PONG
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public boolean sendPing() {
        try {
            writer.println("PING");
            socket.setSoTimeout(3000); // Timeout auf 3 Sekunden setzen
            String response = reader.readLine();
            socket.setSoTimeout(0); // Timeout zurücksetzen
            return "PONG".equalsIgnoreCase(response);
        } catch (IOException e) {
            return false;
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        } else {
            System.err.println("ERROR: Could not send message because the writer is null.");
        }
    }
}
