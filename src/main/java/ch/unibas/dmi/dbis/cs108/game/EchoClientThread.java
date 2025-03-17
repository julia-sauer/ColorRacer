package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class EchoClientThread implements Runnable {
    private int name;
    private Socket socket;

    public EchoClientThread(int name, Socket socket) { // Constructor
        this.name = name;
        this.socket = socket;
    }

    public void run() {
        String msg = "EchoServer: Verbindung " + name;
        System.out.println(msg + " hergestellt");

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            System.out.println("Client " + name + " verbunden.");
            writer.println("Hey Homie! Willkommen auf dem Server!");

            String message;
            while ((message = reader.readLine()) != null) {
                message = message.trim();

                // 🚨 Reject empty messages
                if (message.isEmpty()) {
                    writer.println("ERROR: Nachricht darf nicht leer sein.");
                    continue;
                }

                // 🚨 Validate allowed characters
                if (!message.matches("[A-Za-z0-9_?!.,:;()\\- ]+")) {
                    writer.println("ERROR: Ungültige Zeichen in der Nachricht.");
                    continue;
                }

                // 🚨 Prevent overly long messages
                if (message.length() > 500) {
                    writer.println("ERROR: Nachricht zu lang (max 500 Zeichen).");
                    continue;
                }

                System.out.println("Empfangen: " + message);
                writer.println(message); // Echo back validated message

                // Handle QUIT command
                if (message.equalsIgnoreCase("QUIT")) {
                    writer.println("Verbindung wird beendet...");
                    socket.close();
                    EchoServer.ClientDisconnected();

                    if (EchoServer.getActiveClientCount() == 0) {
                        System.out.println("Letzter Client hat QUIT gesendet. Server wird heruntergefahren");
                        EchoServer.shutdownServer();
                    }
                    return;
                }
            }

        } catch (IOException e) {
            System.err.println("Fehler beim Client " + name + ": " + e.getMessage());

        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Schliessen des Sockets für Client " + name + ": " + e.getMessage());
            }
            EchoServer.ClientDisconnected(); // Notify Server that client left
        }
    }
}
