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
        String msg = "EchoServer: Connection " + name;
        System.out.println(msg + " established");

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            System.out.println("Client " + name + " connected.");
            writer.println("Hey Homie! Welcome to our server!");

            String message;
            while ((message = reader.readLine()) != null) {
                message = message.trim();

                //  Reject empty messages
                if (message.isEmpty()) {
                    writer.println("ERROR: Message is empty.");
                    continue;
                }

                //  Validate allowed characters
                if (!message.matches("[A-Za-z0-9_?!.,:;()\\- ]+")) {
                    writer.println("ERROR: Invalid symbols.");
                    continue;
                }

                //  Prevent overly long messages
                if (message.length() > 500) {
                    writer.println("ERROR: Message too long (max 500 symbols).");
                    continue;
                }

                System.out.println("Received: " + message);
                writer.println(message); // Echo back validated message

                // Handle QUIT command
                if (message.equalsIgnoreCase("QUIT")) {
                    writer.println("Connection closed...");
                    socket.close();
                    Server.ClientDisconnected();

                    if (Server.getActiveClientCount() == 0) {
                        System.out.println("Last client sent QUIT. Server is closing");
                        Server.shutdownServer();
                    }
                    return;
                }
            }

        } catch (IOException e) {
            System.err.println("Error with client " + name + ": " + e.getMessage());

        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error with closing the socket for client " + name + ": " + e.getMessage());
            }
            Server.ClientDisconnected(); // Meldet dem Server, dass ein Client die Verbindung getrennt hat
        }
    }
}
