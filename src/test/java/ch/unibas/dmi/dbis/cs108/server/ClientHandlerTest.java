package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for ClientHandler to verify welcome message is sent.
 */
public class ClientHandlerTest {

  @Test
  void testRunSendsWelcomeMessage() throws Exception {
    // Arrange: Setup mocked socket with input/output
    Socket mockSocket = mock(Socket.class);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    InputStream inStream = new ByteArrayInputStream(new byte[0]); // no real input

    when(mockSocket.getOutputStream()).thenReturn(outStream);
    when(mockSocket.getInputStream()).thenReturn(inStream);
    when(mockSocket.isClosed()).thenReturn(false);

    ClientHandler clientHandler = new ClientHandler(1, mockSocket, 1);

    // Act: Run handler in a thread
    Thread handlerThread = new Thread(clientHandler::run);
    handlerThread.start();

    // Wait until the output contains the welcome message or timeout
    boolean messageSent = false;
    long timeoutMillis = 1000;
    long startTime = System.currentTimeMillis();

    while (System.currentTimeMillis() - startTime < timeoutMillis) {
      if (outStream.toString().contains("Welcome to the Server!")) {
        messageSent = true;
        break;
      }
      Thread.sleep(50); // check every 50ms
    }

    clientHandler.disconnectClient(); // stop the handler

    // Assert: Verify message
    assertTrue(messageSent, "Expected welcome message to be sent.");
  }
}
