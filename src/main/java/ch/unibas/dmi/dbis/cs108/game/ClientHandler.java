package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);


            // Nickname vorschlagen (basierend auf System-Benutzername)**
            String suggestedNickname = System.getProperty("user.name", "User"); // Holt den System-Benutzernamen
            writer.println("Vorgeschlagener Nickname: " + suggestedNickname);
            writer.println("Drücke ENTER, um den Namen zu übernehmen oder gib einen neuen ein:");

            // Benutzer wählt Nickname**
            clientName = reader.readLine().trim();
            if (clientName.isEmpty()) {
                clientName = suggestedNickname; // Falls leer, den Vorschlag übernehmen
            }
            // Ensure nickname contains only safe characters
            if (!clientName.matches("[A-Za-z0-9_-]{3,15}")) {
                writer.println("ERROR: Invalid nickname. Use only letters, numbers, '-' and '_'. No spaces allowed.");
                clientName = suggestedNickname; // Default to system username
            }
            // Nickname im NicknameManager speichern**
            NicknameManager.setNickname(socket, clientName);
            System.out.println(clientName + " hat sich verbunden.");
            ChatServer.broadcastMessage(clientName + " ist dem Chat beigetreten!", this);

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("QUIT")) {
                    break;
                }

                // Nickname ändern**
                if (message.startsWith("/nick "))                {
                    String newNickname = message.substring(6).trim();
                    NicknameManager.changeNickname(socket, newNickname);
                    writer.println("Dein neuer Nickname ist: " + newNickname);
                    clientName = newNickname; // Aktualisiere den lokalen Namen
                    continue;
                }
               //  Message Validation (Add Here!)
                if (!message.matches("[A-Za-z0-9_?!.,:;()\\-]+")) {
                    writer.println("ERROR: Invalid characters in message.");
                    continue; // Skip invalid messages
                }
                if (message.length() > 500) {
                    writer.println("ERROR: Message too long.");
                    continue;
                }
                // Aktuellen Nickname für Nachrichten verwenden**
                String currentNickname = NicknameManager.getNickname(socket);
                ChatServer.broadcastMessage(currentNickname + ": " + message, this);
            }
        } catch (IOException e) {
            //  Notify the client before disconnecting
            if (writer != null) {
                writer.println("ERROR: A network error occurred. Please reconnect.");
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

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        } else {
            System.err.println("Error: Attempted to send a message, but writer is null.");
        }
    }
}
