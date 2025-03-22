package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.server.Server;
import ch.unibas.dmi.dbis.cs108.server.UserList;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ProtocolReaderClient {
    private final BufferedReader reader; // Liest Zeichenzeilen vom Client.
    private final InputStream in;
    private final OutputStream out;


    public ProtocolReaderClient(InputStream in, OutputStream out) throws IOException {
        this.in = in;
        this.out = out;
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
    public void readLoop() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(Command.SEPARATOR, 2);
            String rawCommand = parts[0];
            Command command;

            try {
                command = Command.valueOf(rawCommand);
            } catch (IllegalArgumentException e) {
                System.err.println("Unbekannter Befehl von Server " + line);
                continue;
            }
            // Verarbeiten des Befehls mit switch-case

            switch (command) {

                case CHAT:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Leere Chat-Nachricht von Server.");
                        break;
                    }
                    String message = parts[1].trim();
                    System.out.println("Nachricht erhalten: " + message);
                    break;

                case PING:
                    System.out.println("PING erhalten von Server.");
                    ProtocolWriterClient.sendCommand(out, "PONG");
                    break;

                default:
                    System.out.println("Unbekannter Befehl von Server: " + line);
                    break;
            }
        }
    }

    public static String readCommand(InputStream in) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.readLine();
        } catch (IOException e) {
            System.out.println("Error in reading command");
            return null; // Gibt null zurück, wenn ein Fehler auftritt
        }
    }

    public void pingCheck() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 15000) {

        }
        if (System.currentTimeMillis() - startTime >= 15000) {
            System.out.println("Connection timed out for Server.");
        }
    }
}
