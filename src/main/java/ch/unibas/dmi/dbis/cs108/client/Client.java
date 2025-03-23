package ch.unibas.dmi.dbis.cs108.client;

import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
            InputStream in = sock.getInputStream();
            OutputStream out= sock.getOutputStream();

            ProtocolReaderClient protocolReader = new ProtocolReaderClient(in, out);
            Thread readerThread = new Thread(() -> {
                try{
                    protocolReader.readLoop();
                } catch (IOException e) {
                    System.err.println("Fehler beim Lesen von Nachrichten: " + e.getMessage());
                }
            });
            readerThread.start();

            // stream input
            BufferedReader conin = new BufferedReader(new InputStreamReader(System.in));
            String line = " ";
            while (true) {
                // reading input stream
                line = conin.readLine();
                if (line.equalsIgnoreCase("QUIT")) {
                    //sendQuitMessage(); odr so halt eifach das vom ProtocolReaderClient
                    break;
                } else if (line.startsWith("/nick")){
                    //changeNickname(line.substring(6)); oder das vom Protocol
                } else if(line.equals("PING ")){
                    ProtocolWriterClient.sendCommand(out, "PONG");
                }
            }
            // terminate program
            System.out.println("Terminating ...");
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.toString());
        }
    }
}
