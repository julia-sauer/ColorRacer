package ch.unibas.dmi.dbis.cs108.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {
    private static final AtomicInteger activeClients = new AtomicInteger(0);
    private static ServerSocket echod;
    /**
     * Startet einen Server, der auf Verbindungen wartet und stellt Netzwerkverbindung
     * von Client zu Server dar.
     * ServerSocket erstellt einen Server (hier: echod) der auf Port 8090 läuft.
     * echod.accept(); wartet bis sich Client verbindet.
     * Sobald sich ein Client verbunden hat wird "Connection established" ausgegeben.
     *
     * while-Schleife: Solange bis Client Verbindung beendet. Speichert was von Client kommt in c
     * und gibt genau das Gleiche zurück (out.write).
     * Wenn Client Verbindung beendet geht es aus while-Schleife, dann wir Verbindung zu Client beendet
     * (socket.close) dann wird Server geschlossen (echod.close).
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println("Waiting for port 8090...");
            echod = new ServerSocket(8090);

            while (true) {
                Socket clientSocket = echod.accept();
                activeClients.incrementAndGet();
                System.out.println("Connection established for Client: " + activeClients.get());
                activeClients.incrementAndGet();

                ClientHandler cH = new ClientHandler(activeClients.get(), clientSocket);
                Thread cHT = new Thread(cH);
                cHT.start();
            }

        } catch (IOException e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }

    public static void ClientDisconnected() {
        activeClients.decrementAndGet();
        System.out.println("Remaining Clients: " + activeClients.get());

        if(activeClients.get() == 0) {
            System.out.println("Wait 60 seconds for new clients...");
            new Thread(() -> {
                try {
                    Thread.sleep(60000);
                    if (activeClients.get() == 0) {
                        System.out.println("No new clients. Server is shutting down.");
                        shutdownServer();
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }

    public static void shutdownServer() {
        try {
            echod.close();
            System.exit(0);
        }
        catch (IOException e) {
            System.err.println("Fehler beim Schliessen des Servers: " + e.getMessage());
        }
    }
}

