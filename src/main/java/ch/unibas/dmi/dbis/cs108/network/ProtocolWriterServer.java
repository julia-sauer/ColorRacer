package ch.unibas.dmi.dbis.cs108.network;

import java.io.IOException;
import java.io.OutputStream;

public class ProtocolWriterServer {
    /**
     * Sendet den Command auf dem gewünschten OutputStream.
     *
     * @param out
     * @param command
     * @throws IOException wenn die Nachricht nicht gesendet werden konnte.
     * @author Jana
     */
    public static void sendCommand(OutputStream out, String command) throws IOException {
        try {
            out.write((command + Command.SEPARATOR).getBytes());
            out.flush();
            System.out.println(command + " sent");
        } catch (IOException e) {
            System.err.println("Error, Could not send Command");
        }
    }
}
