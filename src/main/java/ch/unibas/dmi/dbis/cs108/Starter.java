package ch.unibas.dmi.dbis.cs108;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.server.Server;
/**
 * This method starts either the Server or the Client
 */
public class Starter {
    public static void main(String[] args) {
        try {
            String input = args[0];
            if (input.equalsIgnoreCase("server")) {
                int port = Integer.parseInt(args[1]);
                Server server = new Server(port);
                server.start();
            } else if (input.equalsIgnoreCase("client")) {

            }
        } catch (Exception e) {
            System.out.println("Your input was incorrect. Please try again! \n\n"
                    + "It has to be done as followed: \n"
                    + "client <hostadress>:<port> [<username>] | server <port> \n\n");
        }
    }
}
