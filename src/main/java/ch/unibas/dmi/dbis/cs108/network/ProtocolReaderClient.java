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
                    pingCheck();
                    System.out.println("PING erhalten von Server.");
                    ProtocolWriterClient.sendCommand(out, "PONG");
                    break;

                /**
                 * Wenn Comman NICK erkannt wird, wird ausgegeben, zu was der Nickname gewechselt wrude.
                 */
                case NICK:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Fehler: Kein Nickname erhalten.");
                        break;
                    }
                    String newNick = parts[1].trim();
                    System.out.println("Nickname changed to " + newNick);
                    break;

                default:
                    System.out.println("Unbekannter Befehl von Server: " + line);
                    break;
            }
        }
    }

    /**
     * Diese Methode wird von Client aufgerufen, wenn nicknamechange vom Benutzer eingegeben wrid.
     * Diese Methode ruft dann im ProtocolWriterClient die Methode sendCommand auf mit den Parametern
     * out, NICK und den newnickname
     * @param newnickname
     * @ Jana
     */
    public void changeNickname(String newnickname) {
        try {
            ProtocolWriterClient.sendCommand(out, "NICK" + newnickname);
        } catch (IOException e) {
            System.err.println("Error, could not send NICK " + newnickname + " to Server");
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
