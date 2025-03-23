package ch.unibas.dmi.dbis.cs108.network;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

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
            System.out.println("Message is null or empty!");
            return;
        }
        if (message.length() > 500){
            System.out.println("Message is too long");
            return;
        }
        writer.println(Command.CHAT.name() + Command.SEPARATOR + message); // Sendet die Nachricht im Format "CHAT <message>" an den Server
        writer.flush(); // Sicherstellen, dass Nachricht sofort gesendet wird
    }

    /**
     * Sendet den Command auf dem gewünschten OutputStream.
     *
     * @param out
     * @param command
     * @throws IOException wenn die Nachricht nicht gesendet werden konnte.
     * @author Julia
     */
    public static void sendCommand(OutputStream out, String command) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        try {
            writer.write(command + Command.SEPARATOR);
            writer.flush();
            System.out.println(command + " sent");
        } catch (IOException e) {
            System.err.println("Error, Could not send Command");
        }
    }

    /**
     * Ruft sendCommand auf für den Command NICK um den neuen Nickname dem Server zu senden.
     * @param newnickname
     * @param out
     */
    public void changeNickname(String newnickname, OutputStream out) {
        try {
            sendCommand(out, "NICK " + newnickname);
        } catch (IOException e) {
            System.err.println("Error, could not send NICK " + newnickname + " to Server");
        }
    }

    public void leave(OutputStream out) {
        try {
            sendCommand(out, "QUIT ");
        } catch (IOException e) {
            System.err.println("Error, could not send QUIT to Server");
        }
    }

    /**
     * Sendet den JOIN-Befehl mit dem gewünschten Nicknamen an den Server.
     * Der Server entscheidet, ob der Nickname akzeptiert oder verändert wird (z.B. bei Duplikaten).
     *
     * @param nickname Der vom Spieler eingegebene Nickname
     * @author anasv
     */
    public void sendJoin(String nickname){
        if (nickname == null || nickname.trim().isEmpty()) {
            System.out.println("Nickname is null or empty!");
            return;
        }
        if (nickname.length() > 15){
            System.out.println("Nickname is too long");
            return;
        }
        if (!nickname.matches("^[a-zA-Z0-9_]{3,15}$")) {
            System.out.println("Nickname must be 3–15 characters, only letters, digits, or _");
            return;
        }
        // Sendet: JOIN <nickname>
        writer.println(Command.JOIN.name() + Command.SEPARATOR + nickname);
        writer.flush();
    }

}


