package ch.unibas.dmi.dbis.cs108;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * This method starts either the Server or the Client
 */
public class Starter {
    //public static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            String input = args[0];
            if (input.equalsIgnoreCase("server")) {
                int port = Integer.parseInt(args[1]);
                Server server = new Server(port);
                server.start();
                //LOGGER.info("Server started");
            } else if (input.equalsIgnoreCase("client")) {
                String[] hostPort = args[1].split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);
                String username = null;
                if(args.length > 2) {
                    username = args[2];
                }
                Client client = new Client(host, port, username);
                client.start();
                //LOGGER.info("Client started");
            }
        } catch (Exception e) {
            System.out.println("Your input was incorrect. Please try again! \n\n"
                    + "It has to be done as followed: \n"
                    + "client <hostadress>:<port> [<username>] | server <port> \n\n");
        }
    }
}
