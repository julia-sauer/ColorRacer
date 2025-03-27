/**
 * A test class for verifying that the chat message format is correct
 * when using the ProtocolWriterClient, and for simulating a full chat
 * flow between a sender and receiver.
 */
package ch.unibas.dmi.dbis.cs108.gamenew;

import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ChatTest {

    private ByteArrayOutputStream serverOutput;
    private ByteArrayInputStream clientInput;
    private ByteArrayOutputStream clientOutput;

    /**
     * Set up basic I/O streams before each test
     */
    @BeforeEach
    public void setUp() {
        serverOutput = new ByteArrayOutputStream();
        clientInput = new ByteArrayInputStream("CHAT TestMessage".getBytes(StandardCharsets.UTF_8));
        clientOutput = new ByteArrayOutputStream();
    }

    /**
     * Tests that the ProtocolWriterClient correctly formats a chat message.
     */
    @Test
    public void testChatMessageFormat() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ProtocolWriterClient writer = new ProtocolWriterClient(outStream);

        writer.sendChat("TestMessage");

        String written = outStream.toString("UTF-8").trim();
        assertTrue(written.startsWith(Command.CHAT + Command.SEPARATOR), "Message should start with CHAT");
        assertTrue(written.contains("TestMessage"), "Message should contain the original content");
    }

    /**
     * Simulates a complete chat flow where one client sends a message and another client receives it.
     */
    @Test
    public void testChatFunctionality() throws IOException {
        // Client sends chat message
        ByteArrayOutputStream toServer = new ByteArrayOutputStream();
        ProtocolWriterClient writer = new ProtocolWriterClient(toServer);
        writer.sendChat("TestMessage");

        // Simulate sending that message to server (and server to another client)
        ByteArrayInputStream simulatedServerInput = new ByteArrayInputStream(toServer.toByteArray());
        ByteArrayOutputStream simulatedClientOut = new ByteArrayOutputStream();

        // Receiver processes the message
        ProtocolReaderClient reader = new ProtocolReaderClient(simulatedServerInput, simulatedClientOut);
        reader.readLoop();

        String received = simulatedClientOut.toString("UTF-8");
        assertTrue(received.contains("TestMessage"), "Receiver should see the original message");
    }
}
