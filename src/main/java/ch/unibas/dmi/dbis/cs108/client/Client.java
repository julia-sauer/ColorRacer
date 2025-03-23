package ch.unibas.dmi.dbis.cs108.client;

import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import java.net.*;
import java.io.*;

/**
 * Die Client-Klasse stellt eine Verbindung zu einem Server her und ermöglicht die Kommunikation über ein Netzwerk.
 * Diese Klasse verwendet das ProtocolReaderClient- und ProtocolWriterClient-Objekt, um Nachrichten zu lesen und zu senden.
 *
 */
public class Client {
    /**
     * Die main-Methode initialisiert die Verbindung zum Server und startet einen Thread, um Nachrichten zu lesen.
     * Sie liest auch Eingaben von der Konsole und sendet entsprechende Befehle an den Server.
     *
     * @param args Argumente, die die Hostadresse und den Port des Servers enthalten.
     * @author Julia
     */
    public static void main(String[] args) {
        try {
            // Verbindung zum Server herstellen
            Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
            InputStream in = sock.getInputStream();
            OutputStream out= sock.getOutputStream();

            // ProtocolReaderClient-Objekt erstellen und Thread starten
            ProtocolReaderClient protocolReader = new ProtocolReaderClient(in, out);
            Thread readerThread = new Thread(() -> {
                try{
                    protocolReader.readLoop();
                } catch (IOException e) {
                    System.err.println("Error when reading messages: " + e.getMessage());
                }
            });
            readerThread.start();

            // System-Benutzername abrufen
            String systemUsername = System.getProperty("user.name");
            String defaultNickname = "Guest_" + systemUsername;
            // Default-Nickname an den Server senden
            ProtocolWriterClient.sendCommand(out, "NICK " + defaultNickname);
            System.out.println("Your suggested nickname is " + defaultNickname + ". If you want to change it, please type in your chat and replace the dots with the desired name: nicknamechange ...");

            // Eingaben von der Konsole lesen
            BufferedReader conin = new BufferedReader(new InputStreamReader(System.in));
            ProtocolWriterClient protocolClient = new ProtocolWriterClient(out);  // Methodeimplementation im WriterClient

            String line = " ";
            while (true) {
                line = conin.readLine();
                if (line.equalsIgnoreCase("leave")) {
                    // Verbindung beendet
                    System.out.println("Terminating ...");
                    in.close();
                    out.close();
                    sock.close();
                    break;
                } else if (line.startsWith("nicknamechange")){
                    // Überprüft, ob Benutzer nicknamechange eingegeben hat.
                    protocolClient.changeNickname(line.substring(15), out); //sollte im WriterClient implementiert sein
                } else if (line.startsWith("connect")) {
                    // JOIN-Befehl senden
                    String nickname = line.substring(8).trim();
                    protocolClient.sendJoin(nickname);
                } else if (line.startsWith("message")) {
                    // Chatnachricht senden
                String message = line.substring(8).trim();
                protocolClient.sendChat(message);
                } else { // ungültiger Befehl
                System.out.println("Unknown command. Use: connect | nicknamechange | message | QUIT");

                }
            }
            // Programm beenden
            System.out.println("Terminating ...");
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
