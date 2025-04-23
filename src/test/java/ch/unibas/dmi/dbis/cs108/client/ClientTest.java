package ch.unibas.dmi.dbis.cs108.client;

import ch.unibas.dmi.dbis.cs108.network.Command;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

  private static final int PORT = 8888;
  private ServerSocket serverSocket;
  private ExecutorService serverExecutor;

  @BeforeEach
  void startMockServer() throws IOException {
    serverSocket = new ServerSocket(PORT);
    serverExecutor = Executors.newSingleThreadExecutor();

    // Start a fake server that accepts one connection and prints received data
    serverExecutor.submit(() -> {
      try (Socket client = serverSocket.accept();
          BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
          BufferedWriter out = new BufferedWriter(
              new OutputStreamWriter(client.getOutputStream()))) {

        // Send a welcome message
        out.write("INFO%Welcome to the Server!\n");
        out.flush();

        // Read initial commands
        String nick = in.readLine(); // should be NICK%<username>
        String join = in.readLine(); // should be JOIN%Welcome

        System.out.println("Server received:");
        System.out.println(nick);
        System.out.println(join);

        // Optionally assert server side (e.g., validate protocol)
        assertTrue(nick.startsWith("NICK%"));
        assertEquals("JOIN%Welcome", join);

      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @AfterEach
  void shutdownMockServer() throws IOException {
    if (serverSocket != null && !serverSocket.isClosed()) {
      serverSocket.close();
    }
    if (serverExecutor != null && !serverExecutor.isShutdown()) {
      serverExecutor.shutdownNow();
    }
  }

  @Test
  void testClientSendsNickAndJoin() {
    // GIVEN a client that connects to localhost
    String testUsername = "JUnitTestUser";
    Client client = new Client("localhost", PORT, testUsername);

    // WHEN we start the client in a thread (so it doesn't block on input)
    Thread clientThread = new Thread(() -> {
      client.start();
    });
    clientThread.start();

    // THEN wait a bit to allow communication to complete
    try {
      Thread.sleep(2000);
    } catch (InterruptedException ignored) {
    }

    // No assertion here: validation is in server mock
    // If needed, you can wrap server behavior in a test class to record/log messages for assertions
  }
}

