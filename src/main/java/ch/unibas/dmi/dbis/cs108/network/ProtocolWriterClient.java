package ch.unibas.dmi.dbis.cs108.network;

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
 *
 */
public class ProtocolWriterClient {
}
