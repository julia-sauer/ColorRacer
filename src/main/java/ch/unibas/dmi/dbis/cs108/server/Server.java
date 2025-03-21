package ch.unibas.dmi.dbis.cs108.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {
    private static final AtomicInteger activeClients = new AtomicInteger(0);
    private static final int maxClients = 4;
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
        int cnt = 0;
        try {
            System.out.println("Waiting for port 8090...");
            ServerSocket echod = new ServerSocket(8090);

            while (true) {
                Socket clientSocket = echod.accept();
                System.out.println("Connection established");
                ClientHandler cH = new ClientHandler(++cnt, clientSocket);
                Thread cHT = new Thread(cH);
                cHT.start();
            }
            
        } catch (IOException e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }
}

