package ch.unibas.dmi.dbis.cs108.client;

import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
            InputStream in = sock.getInputStream();
            OutputStream out= sock.getOutputStream();

            // create server reading thread

            PongThread pongThread = new PongThread(sock);
            Thread pongT = new Thread(pongThread);
            pongT.start();

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
                } else {
                    //sendMessage(line); oder das vom Protocol
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
