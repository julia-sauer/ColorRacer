package ch.unibas.dmi.dbis.cs108.game;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class EchoServerTest {
    private static ExecutorService serverThread;

    @BeforeAll
    static void startServer() {
        serverThread = Executors.newSingleThreadExecutor();
        serverThread.submit(() -> {
            Server.main(new String[]{});
        });

        try {
            Thread.sleep(1000); // Warte auf Serverstart
        } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void stopServer() {
        Server.shutdownServer();
        serverThread.shutdown();
    }

    @Test
    void testClientConnection() {
        try (Socket socket = new Socket("localhost", 8090);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            writer.println("Hello Server");
            String response = reader.readLine();
            assertNotNull(response, "Server should respond");
            assertEquals("Hello Server", response, "Server should echo the message");
        } catch (IOException e) {
            fail("Connection failed: " + e.getMessage());
        }
    }

    @Test
    void testMultipleClients() {
        try {
            Socket client1 = new Socket("localhost", 8090);
            Socket client2 = new Socket("localhost", 8090);
            assertTrue(client1.isConnected() && client2.isConnected(), "Both clients should connect successfully");

            client1.close();
            client2.close();
        } catch (IOException e) {
            fail("Clients failed to connect: " + e.getMessage());
        }
    }
}