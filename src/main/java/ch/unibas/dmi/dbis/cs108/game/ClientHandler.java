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
            writer.println("Gib deinen Namen ein:");
            clientName = reader.readLine();
            System.out.println(clientName + " hat sich verbunden.");

            ChatServer.broadcastMessage(clientName + " ist dem Chat beigetreten!", this);

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("QUIT")) {
                    break;
                }
                ChatServer.broadcastMessage(clientName + ": " + message, this);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            ChatServer.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}
