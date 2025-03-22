package ch.unibas.dmi.dbis.cs108.network;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Die Klasse {@code ProtocolWriterClient} wandelt die Spieler Eingaben in die entsprechenden
 * Protokollbefehle um, die in den Commands definiert sind und sendet sie wenn nötig an den Server weiter.
 *
 * <p>Spielereingaben und zugehörige Protokollbefehle:
 *  * <ul>
 *  *     <li>{@code connect} → {@code JOIN}</li>
 *  *     <li>{@code leave} → {@code QUIT}</li>
 *  *     <li>{@code message} → {@code CHAT}</li>
 *  *     <li>{@code nicknamechange} → {@code NICK}</li>
 *  * </ul>
 * <p>
 * Diese Klasse verwedet die UFT-8-Kodierung, um eine Plattforübergreifende Kommunikaton sicherzustellen.
 */
public class ProtocolWriterClient {
    /**
     * Der {@code PrintWriter} zum Senden von Nachrichten über die Netzwerkverbindung.
     * Dieser Writer schreibt Protokollbefehle (z.B. {@code CHAT}) in UTF-8 an den Server.
     */
    private final PrintWriter writer; //Der Writer ist "final", weil er nach der Initialisierung nicht mehr verändert wird.

    /**
     * Konstruktor: Intialisiert den {@code PrintWriter} mit UFT-8.
     * @param outputStream der OutputStream, an den die Nachrichten gesendet werden sollen.
     * @throws IOException wenn ein Fehler beim Erstellen des PrintWriters auftritt.
     *
     */
    public ProtocolWriterClient(OutputStream outputStream) throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
    }

    /**
     * Die Methode {@code sendChat} wird für den Chat verwendet.
     * Sie wandelt eine vom Spieler eingegebene Chatnachricht (zB. {@code message Hallo!}) in einen gültigen
     * Protokollbefehl des Formats {@code CHAT <message>} um und sendet sie an den Server.
     * <p>
     * @param message Die Nachricht, die vom Benutzer eingegeben wurde.
     * @author anasv
     * @since 22.03.25
     */
    public void sendChat(String message) {
        if (message == null || message.trim().isEmpty()) {
            System.out.println("Nachricht ist leer!");
            return;
        }
        if (message.length() > 500){
            System.out.println("Message is too long");
            return;
        }
        writer.println(Command.CHAT.name() + Command.SEPARATOR + message); // Sendet die Nachricht im Format "CHAT <message>" an den Server
        writer.flush(); // Sicherstellen, dass Nachricht sofort gesendet wird
    }
}
