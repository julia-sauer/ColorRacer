package ch.unibas.dmi.dbis.cs108.network;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Die Klasse ProtocolWriterClient wandelt die Spieler Eingaben
 * (die im Terminal von den Spieler*innen eingegeben werden kann)
 * in die entsprechenden Protokollbefehle um, die in den Commands definiert sind
 * und sendet sie wenn nötig an den Server weiter.
 * Die Spieleingabe und die Umwandlung in das entsprechende Protokollbefehl aufgelistet:
 * connect -> JOIN
 * leave -> QUIT
 * message -> CHAT
 * nicknamechange -> NICK
 * <p>
 * Diese Klasse verwedet die UFT-8-Kodierung, um eine Plattforübergreifende (Windwos, Linux und Mac) Kommunikaton sicherzustellen.
 */
public class ProtocolWriterClient {
    /**
     * Writer zum Senden von Nachrichten über die Netzwerkverbindung.
     * Wird verwendet, um Protokollbefehle (zB. CHAT) an den Server zu übermitteln.
     * Der Writer ist "final", weil er nach der Initialisierung nicht mehr verändert wird.
     */
    private final PrintWriter writer;

    /**
     * Erstellt eine neue Instaz des protocolWriterClient
     * Konstruktor: Intialisiert den Writer mit UFT-8.
     * @param outputStream der OutputStream, an den die Nachrichten gesendet werden sollen.
     * @throws IOException wenn ein Fehler beim Erstellen des PrintWriters auftritt.
     *
     */
    public ProtocolWriterClient(OutputStream outputStream) throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
    }

    /**
     * Die Methode sendChat verarbeitet eine Benutzereingabe für eine Chat-Nachricht.
     * Diese Methode formatiert die Nachricht entsprechend dem Netzwerkprotokoll
     * und sendet sie an den Server. Die Nachricht darf nicht länger als 500 Zeichen sein.
     * <p>
     * Beispiel: Gibt ein Spieler im Terminal "message Hallo!" ein,
     * wird daraus die Netzwerkzeile "CHAT Hallo!", die an den Server übertragen wird.
     *
     * @param message Die Nachricht, die vom Benutzer eingegeben wurde.
     * @author anasv
     * @since 22.03.25
     */
    public void sendChat(String message) {
        if (message.length() > 500){
            System.out.println("Message is too long");
            return;
        }
        writer.println(Command.CHAT.name() + Command.SEPARATOR + message); // Sendet die Nachricht im Format "CHAT <message>" an den Server
    }
}
