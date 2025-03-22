package ch.unibas.dmi.dbis.cs108.server;


import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int clientNumber;
    private PingThread pingThread;

    public ClientHandler(int clientNumber, Socket socket) {
        this.clientNumber = clientNumber;
        this.clientSocket = socket;
    }

    public void run() {
        // Starten des PingThreads
        pingThread = new PingThread(clientSocket, clientNumber);
        pingThread.start();
        try {
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            String welcomeMsg = "Welcome to the Server!\n"; //Willkommensnachricht
            out.write(welcomeMsg.getBytes());

            int c;
            while ((c = in.read()) != -1) {
                out.write((char)c);
                System.out.print((char)c);
            }

            System.out.println("Connection closed for Client " + clientNumber);
            clientSocket.close();
            PingThread.stopPinging();
            removeUser(clientNumber); // Aufruf der Methode removeUser
            Server.ClientDisconnected();

        }
        catch (IOException e) {
            System.err.println("Fehler bei Client " + clientNumber + ": " + e.getMessage());
        }
    }
    /**
     * Entfernt einen Benutzer aus der Benutzerliste.
     * @param clientNumber Die ID des zu entfernenden Benutzers.
     * @author milo
     */
    private void removeUser(int clientNumber) {
        UserList.removeUser(clientNumber);
    }
}
