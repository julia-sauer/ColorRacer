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
            while (running) {
                ProtocolWriterServer.sendCommand(out, Command.PING.name()); //Senden von Ping

                //Auf Pong warten
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < 15000) {
                    if (in.available() > 0) {
                        String response = ProtocolReaderServer.readCommand(in);
                        if ("PONG".equals(response)) {
                            System.out.println("Pong received");
                            ProtocolWriterServer.sendCommand(out, "PING"); //Pong empfangen, Ping wird gesendet
                            startTime = System.currentTimeMillis();
                        }
                    }
                }

                //Wenn kein PONG empfangen wird
                if (System.currentTimeMillis() - startTime >= 15000) {
                    System.out.println("Connection timed out for Client " + clientNumber);
                    running = false;
                    clientSocket.close();
                    Server.ClientDisconnected();
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error with Client " + clientNumber + ": " + e.getMessage());
        }
    }

    public static void stopPinging() {
        running = false;
    }
}
