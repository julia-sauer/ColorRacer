package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.server.Server;
import ch.unibas.dmi.dbis.cs108.server.UserList;
import ch.unibas.dmi.dbis.cs108.server.PingThread;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Die Klasse {@code ProtocolReaderServer} liest eingehende Nachrichten vom Client
 * und leitet sie an den Server weiter.
 * Die Klasse {@code ProtocolReaderServer} ist verantwortlich für das Empfangen und Interpretieren
 * von Nachrichten, die ein Client über das Netzwerk an den Server sendet. Diese Nachrichten sind
 * textbasiert und folgen einem bestimmten Netzwerkprotokoll.
 * <p>
 * Diese Klasse wird pro Clientverbindung instanziiert und liest kontinuierlich neue Zeilen
 * (Befehle) vom verbundenen Client über einen {@link BufferedReader}. Jede Zeile wird als eine
 * vollständige Nachricht betrachtet (endet mit einem Zeilenumbruch '\n').
 * </p>
 * @author anasv
 * @since 22.03.25
 */
public class ProtocolReaderServer {
    private final BufferedReader reader; // Liest Zeichenzeilen vom Client.
    private final int userId; // Die ID des Clients, der aktuell mit dem Server verbunden ist.
    private final OutputStream out;

    /**
     * Konstruktor: Initialisiert den BufferedReader und die Benutzer-ID.
     *
     * @param in     der InputStream, von dem die Nachrichten gelesen werden sollen.
     * @param userId die eindeutige ID des Benutzers.
     * @throws IOException wenn ein Fehler beim Erstellen des BufferedReaders auftritt.
     */
    public ProtocolReaderServer(InputStream in, int userId, OutputStream out) throws IOException {
        // Initialisierung des BufferedReaders
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.userId = userId;
        this.out = out;
    }

    /**
     * Startet eine Endlosschleife, die kontinuierlich Nachrichten (Befehle) vom Client liest
     * und sie anhand des Protokolls verarbeitet.
     *
     * <p> Jede Nachricht muss mit einem der definierten {@link Command}-Enum-Werte beginnen.
     * Die Methode dekodiert diese Befehle und führt die entsprechende Serveraktion aus.
     * <p>
     * Das verarbeiten der Netzwerkbefehle erfolgt mit switch-Statements, die der Server von einem
     * Client erhält. Jeder Befehl (zB. CHAT, NICK, PING, ...) wird als Textzeile vom Client geschickt.
     * Der Server analysiert die Zeile, erkennt den Befehl (das erste Wort), und führt passende
     * Aktionen aus.
     * </p>
     * @throws IOException wenn ein Fehler beim Lesen der Nachrichten auftritt.
     */
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
                System.err.println("Unbekannter Befehl von Benutzer-ID " + userId + ": " + line);
                continue;
            }
            // Verarbeiten des Befehls mit switch-case

            switch (command) {
                case JOIN:
                    System.out.println("JOIN empfangen von Benutzer-ID " + userId);
                    // Logik zum Behandeln von JOIN
                    break;
                /**
                 * Ruft die changeNickname-Methode des Servers auf, wenn NICK erkannt wird.
                 */
                case NICK:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Fehlender Nickname von Benutzer-ID " + userId);
                        break;
                    }
                    String newNick = parts[1].trim();
                    Server.changeNickname(userId, newNick);
                    break;

                // UserList.updateUserName(userId, newNick); // Diese Methode muss existieren!!!
                // System.out.println("Benutzer-ID " + userId + " setzt Nickname auf " + newNick);
                // break;

                case CHAT:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Leere Chat-Nachricht von Benutzer-ID " + userId);
                        break;
                    }
                    String message = parts[1].trim();
                    if (message.length() > 500) {
                        System.err.println("Nachricht zu lang von Benutzer-ID " + userId);
                        break;
                    }
                    String sender = UserList.getUserName(userId);
                    if (sender != null) {
                        Server.chatToAll(message, sender);
                    } else {
                        System.err.println("Unbekannter Benutzer-ID: " + userId);
                    }
                    break;

                case PING:
                    System.out.println("PING erhalten von Benutzer-ID " + userId);
                    // Antworte ggf. mit PONG
                    break;

                case PONG:
                    System.out.println("PONG received from Client " + userId);
                    // Verbindung ist aktiv, kein Timeout nötig
                    PingThread.pongReceived(out, userId);
                    break;

                case QUIT:
                    System.out.println("QUIT empfangen von Benutzer-ID " + userId);
                    // Benutzer entfernen
                    UserList.removeUser(userId);
                    break;

                default:
                    System.out.println("Unbekannter Befehl von Benutzer-ID " + userId + ": " + line);
                    break;
            }
        }
    }
}
    


