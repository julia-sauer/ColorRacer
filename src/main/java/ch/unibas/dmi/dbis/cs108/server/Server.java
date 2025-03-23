package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;

import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;


public class Server {
    private static final AtomicInteger activeClients = new AtomicInteger(0);
    private static ServerSocket echod;
    /**
     * Startet einen Server, der auf Verbindungen wartet und stellt Netzwerkverbindung
     * von Client zu Server dar.
     * ServerSocket erstellt einen Server (hier: echod) der auf Port 8090 läuft.
     * echod.accept(); wartet bis sich Client verbindet.
     * Sobald sich ein Client verbunden hat wird "Connection established" ausgegeben.
     * while-Schleife: Solange bis Client Verbindung beendet. Speichert was von Client kommt in c
     * und gibt genau das Gleiche zurück (out.write).
     * Wenn Client Verbindung beendet geht es aus while-Schleife, dann wir Verbindung zu Client beendet
     * (socket.close) dann wird Server geschlossen (echod.close).
     * @author Jana
     */
    public static void main(String[] args) {
        try {
            out.println("Waiting for port 8090...");
            echod = new ServerSocket(8090);

            while (true) {
                Socket clientSocket = echod.accept();
                activeClients.incrementAndGet();
                int userId = addNewUser("Client" + activeClients.get()); // Änderung: Aufruf der neuen Methode
                out.println("Connection established for Client: " + activeClients.get());

                ClientHandler cH = new ClientHandler(activeClients.get(), clientSocket);
                Thread cHT = new Thread(cH);
                cHT.start();
            }

        } catch (IOException e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }
    /**
     * Fügt einen neuen Benutzer zur Benutzerliste hinzu.
     * @param userName Der Name des neuen Benutzers.
     * @return Die eindeutige Benutzer-ID.
     * @author milo
     */
    public static int addNewUser(String userName) { // Neue Methode

        return UserList.addUser(userName, out);
    }

    /**
     * Informiert den Server, dass ein Client gegangen ist.
     * Die aktiven Clients werden um 1 reuziert und es wird ausgegeben wie viel Clients noch aktiv sind.
     * Falls es keine aktiven Clients mehr gibt, wird ein Tread gestartet der 60s auf neue Clients wartet.
     * Nach 60s wird der Server abgeschalten.
     */
    public static void ClientDisconnected() {
        activeClients.decrementAndGet();
        out.println("Remaining Clients: " + activeClients.get());

        if (activeClients.get() == 0) {
            out.println("Wait 60 seconds for new clients...");
            new Thread(() -> {
                try {
                    Thread.sleep(60000);
                    if (activeClients.get() == 0) {
                        out.println("No new clients. Server is shutting down.");
                        shutdownServer();
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }

    /**
     * Schaltet den Server ab.
     * @author Jana
     */
    public static void shutdownServer() {
        try {
            echod.close();
            System.exit(0);
        }
        catch (IOException e) {
            System.err.println("Fehler beim Schliessen des Servers: " + e.getMessage());
        }
    }


    /**
     * Ändert den Nickname eines Users.
     * Überprüft mit Hilfe UserList, ob Nickname bereis vorhanden uns fügt eine 1 hinzu falls ja.
     * Ruft dann sendCommand von ProtocolWriterServer auf, der die Nachricht an den Client sendet,
     * dass der Nickname geändert wurde.
     * @param userId
     * @param newNick
     */
    public static void changeNickname(int userId, String newNick) {
        String finalNick = newNick;
        int suffix = 1;
        while (UserList.containsUserName(finalNick)) {
            finalNick = finalNick + suffix;
        }
        UserList.updateUserName(userId, finalNick);
        try {
            ProtocolWriterServer.sendCommand(out, "NICK" + finalNick);
        } catch (IOException e) {
            System.err.println("Error while sending NICK " + finalNick);
        }
    }
}

