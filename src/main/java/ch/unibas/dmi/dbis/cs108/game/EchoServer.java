package ch.unibas.dmi.dbis.cs108.game;
import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EchoServer {
    private static final int maxClient = 4;
    private static final AtomicInteger activeClients = new AtomicInteger(0);
    private static ServerSocket echod;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        try {
            echod = new ServerSocket(8090);
            System.out.println("Warte auf Port 8090...");

            while (running) {
                if (activeClients.get() < maxClient) {
                    Socket socket = echod.accept();
                    System.out.println("Client verbunden: " + socket.getInetAddress());

                    activeClients.incrementAndGet(); //Increase count when Client connects

                    EchoClientThread eC = new EchoClientThread((activeClients.get()), socket);
                    Thread eCT = new Thread(eC);
                    eCT.start();
                }

                if (activeClients.get() == 0 && !running) {
                    System.out.println("All Clients are disconnected. Server is being closed.");
                    shutdownServer();
                    break;
                }
                Thread.sleep(500);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    public static void ClientDisconnected() {
        int remainingClients = activeClients.decrementAndGet();
        System.out.println("One client disconnected. Active clients: " + remainingClients);
        if (remainingClients == 0) {
            running = false;
            shutdownServer();
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
            System.exit(0);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
