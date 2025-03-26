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
    private final PingThread pingThread;// added reference
    private final InputStream in;
    /**
     * Konstruktor: Initialisiert den BufferedReader und die Benutzer-ID.
     *
     * @param in     der InputStream, von dem die Nachrichten gelesen werden sollen.
     * @param out    der OutputStream, auf den Antworten geschrieben werden.
     * @param userId die eindeutige ID des Benutzers.
     * @throws IOException wenn ein Fehler beim Erstellen des BufferedReaders auftritt.
     */
    public ProtocolReaderServer(InputStream in, int userId, OutputStream out, PingThread pingThread) throws IOException {
        // Initialisierung des BufferedReaders
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.userId = userId;
        this.out = out;
        this.pingThread = pingThread;
        this.in = in;
    }

    /**
     * Startet eine Endlosschleife, die kontinuierlich Nachrichten (Befehle) vom Client liest
     * und sie anhand des Protokolls verarbeitet.
     *
     * <p> Jede Nachricht muss mit einem der definierten {@link Command}-Enum-Werte beginnen.
     * Die Methode dekodiert diese Befehle und führt die entsprechende Serveraktion aus.
     * <p>
     * Das Verarbeiten der Netzwerkbefehle erfolgt mit switch-Statements, die der Server von einem
     * Client erhält. Jeder Befehl (z.B. CHAT, NICK, PING, ...) wird als Textzeile vom Client geschickt.
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
                System.err.println("Unknown command from user ID " + userId + ": " + line);
                continue;
            }
            // Verarbeiten des Befehls mit switch-case

            switch (command) {
                 // Behandelt den JOIN-Befehl eines Clients.
                 // Der Client sendet JOIN <nickname>, um dem Server beizutreten
                case JOIN: {
                    System.out.println("User " + userId + " is joining...");
                    if (parts.length < 2 || parts[1].trim().isEmpty()){
                        ProtocolWriterServer.sendCommand(out, "-ERR Nickname missing");
                        break;
                    }
                    String newNick = parts[1].trim();
                    if (!newNick.matches("^[a-zA-Z0-9_]{3,15}$")) {
                        ProtocolWriterServer.sendCommand(out, "-ERR Invalid nickname: " + newNick);
                        break;
                    }
                    String finalNick = newNick;
                    int suffix = 1;
                    // Überprüfen, ob der Nickname schon vergeben ist, falls ja mit Suffix ergänzen
                    while (UserList.containsUserName(finalNick)) {
                        finalNick = newNick + suffix++;
                    }
                    // Benutzer zur Liste hinzufügen
                    UserList.addUser(finalNick, out);
                    // Willkommensnachricht an alle Clients senden
                    Server.chatToAll("User " + finalNick + " has joined the chat.", "Server");
                    break;
                }
                // Ruft die changeNickname-Methode des Servers auf, wenn NICK erkannt wird.
                case NICK:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        ProtocolWriterServer.sendCommand(out, "-ERR Nickname missing");
                        break;
                    }
                    String newNick = parts[1].trim();
                    Server.changeNickname(userId, newNick);
                    break;
                    // Aufruf der chatToAll methode für das Senden von einer Chatnachricht an alle Clients
                case CHAT:
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.err.println("Empty chat message from user ID " + userId);
                        break;
                    }
                    String message = parts[1].trim();
                    if (message.length() > 500) {
                        System.err.println("Message too long from user ID " + userId);
                        break;
                    }
                    String sender = UserList.getUserName(userId);
                    if (sender != null) {
                        Server.chatToAll(message, sender);
                    } else {
                        System.err.println("Unknown user ID: " + userId);
                    }
                    break;

                case PING:
                    ProtocolWriterServer.sendCommand(out, "+OK PING sent");
                    // Antworte ggf. mit PONG
                    break;

                case PONG:
                    System.out.println("PONG received from Client " + userId);
                    // Verbindung ist aktiv, kein Timeout nötig
                    if (pingThread != null) {
                        pingThread.notifyPong();  // notify the ping thread that the PONG was received
                    }
                    ProtocolWriterServer.sendCommand(out, "+OK PONG received");
                    break;

                case QUIT:
                    ProtocolWriterServer.sendCommand(out, "+OK Quit request received. Please confirm [YES/NO]");
                    // Benutzer entfernen
                    UserList.removeUser(userId);
                    Server.ClientDisconnected();
                    break;

                default:
                    System.out.println("Unknown command from user ID " + userId + ": " + line);
                    break;
            }
        }
    }
}
    


