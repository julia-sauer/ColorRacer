package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.net.Socket;

public class EchoClientThread implements Runnable {
    private int name;
    private Socket socket;

    public EchoClientThread(int name, Socket socket) { //constructor
        this.name = name;
        this.socket = socket;
    }
    public void run() {
        String msg = "EchoServer: Verbindung " + name;
        System.out.println(msg + " hergestellt");
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);

            System.out.println("Client " + name + " verbunden.");
            writer.println("Hey Homie! Willkommen auf dem Server!");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            /**String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Empfangen: " + message);
                if (message.equalsIgnoreCase("QUIT")) {
                    writer.println("Verbindung wird beendet...");
                    socket.close();
                    EchoServer.ClientDisconnected();

                    if (EchoServer.getActiveClientCount() == 0) {
                        System.out.println("Letzter Client hat QUIT gesendet. Server wird heruntergefahren");
                        EchoServer.shutdownServer();
                    }
                    return;
                }
                writer.println(message); //Echo back normal message
            }**/

            int c;
            while((c = in.read()) != -1){
                out.write((char) c);
                System.out.print((char) c);
            }
            System.out.println("Terminate " + name);

        }
        catch (IOException e) {
            System.err.println(e.toString());
        }
        finally {
            try {
                socket.close();
            }
            catch (IOException e) {
                System.err.println("Fehler beim Schliessen des Sockets für Client " + name);
            }
            EchoServer.ClientDisconnected(); //Notify Server that client left
        }
    }
}
