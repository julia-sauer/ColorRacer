package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolReaderServer;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterServer;

import java.io.*;
import java.net.Socket;

/**
 * Eine Klasse, die PING-Nachrichten versendet an Clients und auf PONG-Nachrichten vom Client hört.
 * Diese Klasse soll in einem separaten Thread verwendet werden, um die Verbindung aufrechtzuerhalten.
 * @author Jana
 */
public class PingThread extends Thread {
    private final Socket clientSocket;
    private final int clientNumber;
    private static boolean running = true;
    private final InputStream in;
    private final OutputStream out;

    public PingThread(Socket clientSocket, int clientNumber, InputStream in, OutputStream out) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            ProtocolWriterServer.sendCommand(out, "PING"); //Senden von Ping
        } catch (IOException e) {
            System.err.println("Error, Could not send Command");
        }
    }

    public static void pongReceived(OutputStream out, int userId) {
        try {
            ProtocolWriterServer.sendCommand(out, "PING");
        } catch (IOException e) {
            System.err.println("Error, Could not send Command");
        }
        long startTime = System.currentTimeMillis();
        try {
            // Warte 15 Sekunden
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            // Wenn der Thread unterbrochen wird, fangen wir die Ausnahme ab
            System.err.println("Thread interrupted: " + e.getMessage());
            return;
        }
        if (System.currentTimeMillis() - startTime >= 15000) {
            System.out.println("Connection timed out for Client " + userId);
            UserList.removeUser(userId);
            Server.ClientDisconnected();
        }
    }

    public static void stopPinging() {
        running = false;
    }
}
