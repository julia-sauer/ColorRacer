package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8090;

    public static void main (String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            System.out.println(reader.readLine());
            String name = console.readLine();
            writer.println(name);

            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = reader.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Verbindung zum Server verloren.");
                }
            });
            listenerThread.start();

            String userInput;
            while ((userInput = console.readLine()) != null) {
                writer.println(userInput);
                if (userInput.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}
