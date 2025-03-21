package ch.unibas.dmi.dbis.cs108.server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int clientNumber;

    public ClientHandler(int clientNumber, Socket socket) {
        this.clientNumber = clientNumber;
        this.clientSocket = socket;
    }

    public void run() {
        try {
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            String welcomeMsg = "Welcome to the Server!"; //Willkommensnachricht
            out.write(welcomeMsg.getBytes());

            int c;
            while ((c = in.read()) != -1) {
                out.write((char)c);
                System.out.print((char)c);
            }
            System.out.println("Connection closed for Client " + clientNumber);
            clientSocket.close();
            Server.ClientDisconnected();

        }
        catch (IOException e) {
            System.err.println("Fehler bei Client " + clientNumber + ": " + e.getMessage());
        }
    }
}
