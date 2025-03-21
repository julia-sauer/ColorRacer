package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import java.util.regex.Pattern;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8090;
    private static final Pattern VALID_NICKNAME = Pattern.compile("^[a-zA-Z0-9_]{3,15}$"); // Erlaubt Buchstaben, Zahlen, Unterstrich, 3-15 Zeichen
    private static final List<String> messageList = new ArrayList<>();

    public static void main (String[] args) {
        try {
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            Socket socket = new Socket(serverAddress, PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            // Standard-Nickname basierend auf dem System-Benutzernamen
            String defaultNickname = System.getProperty("user.name", "User");
            String name;
            do {
                System.out.print("Enter your nickname (Press Enter to use '" + defaultNickname + "'): ");
                name = console.readLine().trim();
                if (name.isEmpty()) {
                    name = defaultNickname;
                }
                if (!VALID_NICKNAME.matcher(name).matches()) {
                    System.out.println("Invalid nickname! Use 3-15 characters: letters, numbers, or _");
                }
            } while (!VALID_NICKNAME.matcher(name).matches());

            writer.println(name); // Sende validierten Nickname an den Server

            // Starte den Ping-Mechanismus
            startPing(writer);

            // Listener-Thread für Server-Nachrichten
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = reader.readLine()) != null) {
                        if (serverMessage.equals("PING")) {
                            writer.println("PONG"); // Antworte auf PING mit PONG
                        } else {
                            messageList.add(serverMessage); // Nachricht speichern
                            System.out.println(serverMessage); // Normale Nachricht
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server lost.");
                }
            });
            listenerThread.start();

            // Hauptschleife für Benutzereingaben
            String userInput;
            while ((userInput = console.readLine()) != null) {
                writer.println(userInput);
                if (userInput.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

            // Cleanup und Socket schließen
            socket.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Methode zum regelmäßigen Senden von Pings an den Server
    private static void startPing(PrintWriter writer) {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                writer.println("PING");
            }
        }, 5000, 5000); // Alle 5 Sekunden einen Ping senden
    }
}
