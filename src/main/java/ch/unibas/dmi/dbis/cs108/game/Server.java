package ch.unibas.dmi.dbis.cs108.game;
import java.net.*;
import java.io.*;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger();
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
            System.out.println("Waiting for port 8090...");

            // Starte einen Scheduler, der regelmäßig PING an Clients sendet
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(Server::pingClients, 5, 5, TimeUnit.SECONDS);

            while (running) {
                if (activeClients.get() < maxClient) {
                    Socket socket = echod.accept();
                    System.out.println("Client connected: " + socket.getInetAddress());

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
                    System.out.println("All clients are disconnected. Server is shutting down.");
                    shutdownServer();
                    break;
                }
                Thread.sleep(500);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void pingClients() {
        System.out.println("Send PING to all clients...");
        for (ClientHandler client : clients) {
            if (!client.sendPing()) {
                System.out.println("Client does not answer, connection lost...");
                clients.remove(client);
            }
        }
    }

    public static void ClientDisconnected() { // Methode, die aufgerufen wird, wenn ein Client sich trennt
        System.out.println("A client has disconnected. Active clients: " + activeClients.get());

        if (activeClients.get() == 0) {
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


    public static int getActiveClientCount() {
        return activeClients.get();
    }

    public static void shutdownServer() {
        try {
            System.out.println("Server is shutting down...");
            running = false;
            echod.close();
            ChatServer.shutdownServer();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error with with closing the server: " + e.getMessage());
        }
    }
}
