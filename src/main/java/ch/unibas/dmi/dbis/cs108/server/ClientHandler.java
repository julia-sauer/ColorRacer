package ch.unibas.dmi.dbis.cs108.server;


import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int clientNumber;
    private PingThread pingThread;
    private InputStream in;
    private OutputStream out;

    /**
     * Konstruktor von der Klasse ClientHandler
     * @param clientNumber
     * @param socket
     */
    public ClientHandler(int clientNumber, Socket socket) {
        this.clientNumber = clientNumber;
        this.clientSocket = socket;
    }

    /**
     * Startet eine ProtocolReader für Server
     * Gibt Willkommensnachricht aus
     * Startet den PingThread und somit den Ping-Pong-Mechanismus
     * int c + while-loop ist von EchoServer, also das was hereinkommt wird auch wieder ausgegeben
     * wenn in.read = -1, also der Client den Server verlassen hat wird der ClientSocket beendet.
     * Und der User wird von der UserList genommen und der Server wird benachrichtigt, dass ein Client gegangen ist.
     * @author Jana
     */
    public void run() {
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();

            // Erstellen Sie einen Thread für das Lesen von Nachrichten
            ProtocolReaderServer protocolReader = new ProtocolReaderServer(in, clientNumber, out);
            Thread readerThread = new Thread(() -> {
                try {
                    protocolReader.readLoop();
                } catch (IOException e) {
                    System.err.println("Fehler beim Lesen von Nachrichten von Client " + clientNumber + ": " + e.getMessage());
                }
            });
            readerThread.start();

            String welcomeMsg = "Welcome to the Server!\n"; //Willkommensnachricht
            out.write(welcomeMsg.getBytes(StandardCharsets.UTF_8));


            // Starten des PingThreads
            pingThread = new PingThread(clientSocket, clientNumber, in, out);
            pingThread.start();


            int c;
            while ((c = in.read()) != -1) {
                out.write((char)c);
                out.write((String.valueOf((char) c)).getBytes(StandardCharsets.UTF_8));
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
