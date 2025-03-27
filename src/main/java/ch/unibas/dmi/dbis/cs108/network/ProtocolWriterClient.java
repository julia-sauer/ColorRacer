package ch.unibas.dmi.dbis.cs108.network;

import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * Die Klasse {@code ProtocolWriterClient} wandelt die Spieler Eingaben in die entsprechenden
 * Protokollbefehle um, die in den Commands definiert sind und sendet sie, wenn nötig, an den Server weiter.
 *
 * <p>Spielereingaben und zugehörige Protokollbefehle:
 *  * <ul>
 *  *     <li>{@code connect} → {@code JOIN}</li>
 *  *     <li>{@code leave} → {@code QUIT}</li>
 *  *     <li>{@code message} → {@code CHAT}</li>
 *  *     <li>{@code nicknamechange} → {@code NICK}</li>
 *  * </ul>
 * <p>
 * Diese Klasse verwendet die UFT-8-Kodierung, um eine plattformübergreifende Kommunikation sicherzustellen.
 */
public class ProtocolWriterClient {
    /**
     * Der {@code PrintWriter} zum Senden von Nachrichten über die Netzwerkverbindung.
     * Dieser Writer schreibt Protokollbefehle (z.B. {@code CHAT}) in UTF-8 an den Server.
     */
    private final PrintWriter writer; //Der Writer ist "final", weil er nach der Initialisierung nicht mehr verändert wird.


    /**
     * Konstruktor: Initialisiert den {@code PrintWriter} mit UFT-8.
     * @param outputStream der OutputStream, an den die Nachrichten gesendet werden sollen.
     * @throws IOException wenn ein Fehler beim Erstellen des PrintWriters auftritt.
     *
     */
    public ProtocolWriterClient(OutputStream outputStream) throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
    }


    /**
     * Die Methode {@code sendChat} wird für den Chat verwendet.
     * Sie wandelt eine vom Spieler eingegebene Chatnachricht (z.B. {@code message Hallo!}) in einen gültigen
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
        writer.println(Command.CHAT + Command.SEPARATOR + message); // Sendet die Nachricht im Format "CHAT <message>" an den Server
        writer.flush(); // Sicherstellen, dass Nachricht sofort gesendet wird
    }

    /**
     * Sends a command to the Server.
     *
     * @param out the OutputStream to send a command
     * @param command the Command to send
     * @throws IOException if the command could not be sent.
     * @author Julia
     */
    public static void sendCommand(OutputStream out, Command command) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(command + Command.SEPARATOR);
        System.out.println(command + Command.SEPARATOR + "sent");
    }

    /**
     * Sends a command with an aditional String.
     * @param out
     * @param command
     * @param text
     * @throws IOException
     */
    public static void sendCommandAndString(OutputStream out, Command command, String text) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(command + Command.SEPARATOR + text);
        //System.out.println(command + Command.SEPARATOR + text + " sent");
    }

    /**
     * Calls sendCommand for the Command NICK, to send the new nickname to the server.
     * @param newnickname the new nickname, the User choosed.
     * @param out the OutputStream.
     */
    public void changeNickname(String newnickname, OutputStream out) {
        try {
            sendCommandAndString(out, Command.NICK, newnickname);
        } catch (IOException e) {
            System.err.println("Error, could not send NICK " + newnickname + " to Server");
        }
    }

    /**
     * Sendet einen {@code QUIT}-Befehl an den Server, um die Verbindung zu beenden.
     *
     * @param out Der OutputStream, auf den der Befehl geschrieben wird.
     */
    public void leave(OutputStream out) {
        try {
            sendCommand(out, Command.QUIT);
        } catch (IOException e) {
            System.err.println("Error, could not send QUIT to Server");
        }
    }

    /**
     * Sendet den {@code JOIN}-Befehl mit dem gewünschten Nicknamen an den Server.
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
        if (!nickname.matches("^[a-zA-Z0-9_]{3,50}$")) {
            System.out.println("Nickname must be 3–15 characters, only letters, digits, or _");
            return;
        }
        // Sendet: JOIN <nickname>
        writer.println(Command.JOIN + Command.SEPARATOR + nickname);
        writer.flush();
    }

}


