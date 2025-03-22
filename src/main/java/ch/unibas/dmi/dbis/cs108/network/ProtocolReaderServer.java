package ch.unibas.dmi.dbis.cs108.network;

import ch.unibas.dmi.dbis.cs108.server.Server;
import ch.unibas.dmi.dbis.cs108.server.UserList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Die Klasse {@code ProtocolReaderServer} liest eingehende Nachrichten vom Client
 * und leitet sie an den Server weiter.
 */
public class ProtocolReaderServer {
    public static String readCommand(InputStream in) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.readLine();
        } catch (IOException e) {
            System.err.println("Error in reading command");
            return null;
        }
    }
}
