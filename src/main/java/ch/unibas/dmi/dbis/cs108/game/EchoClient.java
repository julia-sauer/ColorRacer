package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class EchoClient {
    public static void main(String[] args) {
        try {
            Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
            BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            // Start a separate thread to listen for server messages
            InThread th = new InThread(reader);
            Thread iT = new Thread(th);
            iT.start();

            String line;
            while (true) {
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
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}




