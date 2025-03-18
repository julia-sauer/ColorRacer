package ch.unibas.dmi.dbis.cs108.game;
import java.net.*;
import java.io.*;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EchoServer {
    private static final int maxClient = 4;
    private static final AtomicInteger activeClients = new AtomicInteger(0);
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static ServerSocket echod;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        try {
            Thread chatServerThread = new Thread(ChatServer::startChatServer); // Starte den ChatServer in einem separaten Thread
            chatServerThread.start();

            echod = new ServerSocket(8090);
            System.out.println("Warte auf Port 8090...");

            // Starte einen Scheduler, der regelmäßig PING an Clients sendet
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(EchoServer::pingClients, 5, 5, TimeUnit.SECONDS);

            while (running) {
                if (activeClients.get() < maxClient) {
                    Socket socket = echod.accept();
                    System.out.println("Client verbunden: " + socket.getInetAddress());

                    activeClients.incrementAndGet(); // Erhöhe die Anzahl aktiver Clients

                    /*EchoClientThread eC = new EchoClientThread((activeClients.get()), socket);
                    Thread eCT = new Thread(eC);
                    eCT.start();
                     */
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }

                if (activeClients.get() == 0 && !running) {
                    System.out.println("Alle Clients sind getrennt. Server wird heruntergefahren.");
                    shutdownServer();
                    break;
                }
                Thread.sleep(500);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Fehler: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void pingClients() {
        System.out.println("Sende PING an alle Clients...");
        for (ClientHandler client : clients) {
            if (!client.sendPing()) {
                System.out.println("Client antwortet nicht, Verbindung wird getrennt...");
                clients.remove(client);
            }
        }
    }

    public static void ClientDisconnected() { // Methode, die aufgerufen wird, wenn ein Client sich trennt
        int remainingClients = activeClients.updateAndGet(count -> Math.max(0, count - 1));
        System.out.println("Ein Client hat sich getrennt. Aktive Clients: " + remainingClients);

        if (remainingClients == 0) {
            System.out.println("Warte 60 Sekunden auf neue Clients...");
            new Thread(() -> {
                try {
                    Thread.sleep(60000);
                    if (activeClients.get() == 0) {
                        System.out.println("Keine neuen Clients. Server wird heruntergefahren.");
                        shutdownServer();
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }


    public static int getActiveClientCount() {
        return activeClients.get();
    }

    public static void shutdownServer() {
        try {
            System.out.println("Server wird heruntergefahren...");
            running = false;
            echod.close();
            ChatServer.shutdownServer();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Fehler beim Herunterfahren des Servers: " + e.getMessage());
        }
    }
}
