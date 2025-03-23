package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import ch.unibas.dmi.dbis.cs108.server.Server;
import ch.unibas.dmi.dbis.cs108.server.UserList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * A ChatTest class.
 * Checks if the input of one Client arrives at the other client in the correct same form.
 */
public class ChatTest {
    /*
     * Streams to store system.out and system.err content
     */

    private List<PrintWriter> clientWriters;
    private ByteArrayOutputStream serverOutput;
    private ByteArrayOutputStream clientOutput;
    private ByteArrayInputStream clientInput;


    /**
     * This method is executed before each test.
     * It sets up a server output stream and client input and output streams
     */
    @BeforeEach
    public void setUp() throws Exception {
        //Setup server output stream
        serverOutput = new ByteArrayOutputStream();
        clientWriters = new ArrayList<>();
        clientWriters.add(new PrintWriter(new OutputStreamWriter(serverOutput, StandardCharsets.UTF_8), true));

        //Setup client input and output streams
        clientInput = new ByteArrayInputStream("CHAT TestMessage".getBytes(StandardCharsets.UTF_8));
        clientOutput = new ByteArrayOutputStream();
    }

    /**
     * This is a normal JUnit-Test.
     */
    @Test
    public void testChatFunctionality() throws IOException {
        //Simulate client sending a chat message
        ProtocolWriterClient protocolWriterClient = new ProtocolWriterClient(clientOutput);
        protocolWriterClient.sendChat("TestMessage");

        //Simulate server reading the chat message
        ProtocolReaderClient protocolReaderClient = new ProtocolReaderClient(clientInput, clientOutput);
        protocolReaderClient.readLoop();

        //Verify that the server received the chat message
        String serverOutputMessage = serverOutput.toString(StandardCharsets.UTF_8.name());
        assertTrue(serverOutputMessage.contains("CHAT TestMessage"));
    }
}
