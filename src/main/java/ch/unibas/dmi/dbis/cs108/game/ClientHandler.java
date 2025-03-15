package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            this.writer = writer;

            // Nickname vorschlagen (basierend auf System-Benutzername)**
            String suggestedNickname = System.getProperty("user.name", "User"); // Holt den System-Benutzernamen
            writer.println("Vorgeschlagener Nickname: " + suggestedNickname);
            writer.println("Drücke ENTER, um den Namen zu übernehmen oder gib einen neuen ein:");

            // Benutzer wählt Nickname**
            clientName = reader.readLine().trim();
            if (clientName.isEmpty()) {
                clientName = suggestedNickname; // Falls leer, den Vorschlag übernehmen
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

                // Aktuellen Nickname für Nachrichten verwenden**
                String currentNickname = NicknameManager.getNickname(socket);
                ChatServer.broadcastMessage(currentNickname + ": " + message, this);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            // nickname entfernen, wenn der Client verlässt**
            ChatServer.removeClient(this);
            NicknameManager.removeClient(socket);
        }
    }
    public void sendMessage(String message) {
        writer.println(message);
    }
}
