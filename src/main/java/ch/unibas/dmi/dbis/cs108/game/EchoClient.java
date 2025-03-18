package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class EchoClient {
    private static boolean running = true;
    private static PrintWriter writer;
    private static BufferedReader reader;
    private static Socket sock;
    private static long lastPongTime = System.currentTimeMillis();
    private static final int PING_INTERVAL = 5000; // Alle 5 Sekunden
    private static final int TIMEOUT = 15000; // Timeout nach 15 Sekunden

    public static void main(String[] args) {
        try {
            sock = new Socket(args[0], Integer.parseInt(args[1]));
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            // Starte Thread zum Empfangen von Nachrichten
            InThread th = new InThread(reader);
            Thread iT = new Thread(th);
            iT.start();

            // Starte regelmäßigen Ping an den Server
            startPing();

            String line;
            while (running) {
                line = console.readLine().trim(); // Trim spaces

                //  Reject empty messages
                if (line.isEmpty()) {
                    System.out.println("ERROR: Message cannot be empty.");
                    continue;
                }

                //  Validate allowed characters
                if (!line.matches("[A-Za-z0-9_?!.,:;()\\- ]+")) {
                    System.out.println("ERROR: Invalid characters in message.");
                    continue;
                }

                //  Prevent overly long messages
                if (line.length() > 500) {
                    System.out.println("ERROR: Message is too long (max 500 characters).");
                    continue;
                }

                writer.println(line); // Send message
                if (line.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

            // Terminate program and close resources
            System.out.println("Terminating...");
            reader.close();
            writer.close();
            console.close();
            sock.close();
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private static void listenForMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equals("PING")) {
                    writer.println("PONG"); // Antworte auf Ping
                } else if (message.equals("PONG")) {
                    lastPongTime = System.currentTimeMillis(); // Server antwortet noch
                } else {
                    System.out.println(message); // Normale Nachrichten ausgeben
                }
            }
        } catch (IOException e) {
            System.err.println("Verbindung verloren.");
        } finally {
            try {
                sock.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void startPing() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastPongTime > TIMEOUT) {
                    System.err.println("Server antwortet nicht. Verbindung wird getrennt.");
                    try {
                        sock.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    writer.println("PING");
                }
            }
        }, 0, PING_INTERVAL);
    }
}




