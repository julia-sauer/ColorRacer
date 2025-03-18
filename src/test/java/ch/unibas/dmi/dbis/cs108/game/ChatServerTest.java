package ch.unibas.dmi.dbis.cs108.game;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ChatServerTest {
    private static final int PORT = 8090;
    private static ExecutorService serverExecutor;

    @BeforeAll
    static void startServer() {
        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.execute(ChatServer::startChatServer);
        try {
            Thread.sleep(1000); // Give server time to start
        } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void stopServer() {
        ChatServer.shutdownServer();
        serverExecutor.shutdown();
    }

    @Test
    void testSingleClientConnection() throws IOException {
        try (Socket socket = new Socket("localhost", PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println("Hello Server");
            String response = reader.readLine();
            assertNotNull(response);
        }
    }

    @Test
    void testMultipleClients() throws Exception {
        int clientCount = 3;
        ExecutorService clientExecutor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch latch = new CountDownLatch(clientCount);

        for (int i = 0; i < clientCount; i++) {
            clientExecutor.execute(() -> {
                try (Socket socket = new Socket("localhost", PORT);
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    writer.println("Test Message");
                    String response = reader.readLine();
                    assertNotNull(response);
                } catch (IOException e) {
                    fail("Client failed to connect");
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        clientExecutor.shutdown();
    }
}