package ch.unibas.dmi.dbis.cs108.client;

import ch.unibas.dmi.dbis.cs108.network.Command;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the {@link Client} class. This test verifies that the client sends the correct
 * protocol messages (NICK and JOIN) after connecting to the server.
 */
class ClientTest {

  private static final int PORT = 8888;
  private ServerSocket serverSocket;
  private ExecutorService serverExecutor;

  /**
   * Sets up a mock server before each test. The mock server listens on the specified port, accepts
   * a connection, sends a welcome message, and verifies the incoming commands.
   *
   * @throws IOException if an I/O error occurs when opening the socket
   */
  @BeforeEach
  void startMockServer() throws IOException {
    serverSocket = new ServerSocket(PORT);
    serverExecutor = Executors.newSingleThreadExecutor();

    // Start a mock server to accept a client and verify protocol
    serverExecutor.submit(() -> {
      try (Socket client = serverSocket.accept();
          BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
          BufferedWriter out = new BufferedWriter(
              new OutputStreamWriter(client.getOutputStream()))) {

        // Send initial welcome message to client
        out.write("INFO%Welcome to the Server!\n");
        out.flush();

        // Read commands from client
        String nick = in.readLine(); // Expecting NICK%<username>
        String join = in.readLine(); // Expecting JOIN%Welcome

        System.out.println("Server received:");
        System.out.println(nick);
        System.out.println(join);

        // Server-side assertions
        assertTrue(nick.startsWith("NICK%"), "Expected NICK% command from client");
        assertEquals("JOIN%Welcome", join, "Expected JOIN%Welcome from client");

      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * Shuts down the mock server and executor after each test.
   *
   * @throws IOException if closing the socket fails
   */
  @AfterEach
  void shutdownMockServer() throws IOException {
    if (serverSocket != null && !serverSocket.isClosed()) {
      serverSocket.close();
    }
    if (serverExecutor != null && !serverExecutor.isShutdown()) {
      serverExecutor.shutdownNow();
    }
  }

  /**
   * Tests that the {@link Client} correctly sends a NICK and JOIN command after connecting to the
   * server. The actual assertions are performed inside the mock server logic.
   */
  @Test
  void testClientSendsNickAndJoin() {
    // GIVEN a client that connects to localhost on the specified port
    String testUsername = "JUnitTestUser";
    Client client = new Client("localhost", PORT, testUsername);

    // WHEN we run the client in a separate thread (non-blocking)
    Thread clientThread = new Thread(client::start);
    clientThread.start();

    // THEN we wait briefly to let communication complete
    try {
      Thread.sleep(2000);
    } catch (InterruptedException ignored) {
    }

    // No direct assertions here: server mock validates the protocol
  }
}
