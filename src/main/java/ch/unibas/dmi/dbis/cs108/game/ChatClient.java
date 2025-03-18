package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8090;

    public static void main (String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));


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
                    System.out.println("Connection to server is lost");
                    System.exit(0); // Exit program when connection is lost
                }
            });
            listenerThread.start();


            String userInput;
            while ((userInput = console.readLine()) != null) {
                userInput = userInput.trim(); // Remove spaces at the start and end

                //  Reject empty messages
                if (userInput.isEmpty()) {
                    System.out.println("ERROR: Message cannot be empty.");
                    continue;
                }

                //  Validate allowed characters (prevent special injections)
                if (!userInput.matches("[A-Za-z0-9_?!.,:;()\\- ]+")) {
                    System.out.println("ERROR: Invalid characters in message.");
                    continue;
                }

                //  Prevent overly long messages
                if (userInput.length() > 500) {
                    System.out.println("ERROR: Message is too long (max 500 characters).");
                    continue;
                }

                writer.println(userInput);

                if (userInput.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }
}
