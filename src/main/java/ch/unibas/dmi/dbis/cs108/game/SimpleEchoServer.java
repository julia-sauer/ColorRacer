package ch.unibas.dmi.dbis.cs108.game;
import java.net.*;
import java.io.*;

public class SimpleEchoServer {
    public static void main(String[] args) {
        try {
            System.out.println("Warte auf Port 8090...");
            ServerSocket echod = new ServerSocket(8090);
            Socket socket = echod.accept();
            System.out.println("Verbindung hergestellt");
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            int c;
            while ((c = in.read()) != -1) {
                out.write((char) c);
                System.out.println((char) c);
            }
            System.out.println("Verbindung beendet");
            socket.close();
            echod.close();
        }
        catch (IOException e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }
}
