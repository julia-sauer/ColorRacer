package ch.unibas.dmi.dbis.cs108.network;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code Command} enum defines all supported protocol commands exchanged between clients and
 * the server in the networked multiplayer game.
 * <p>
 * These commands are used to structure and identify different types of messages, such as joining a
 * lobby, sending chat messages, performing game actions, and more.
 * </p>
 * <p>
 * Each command represents a specific action or request. When a message is sent over the network,
 * the command is usually prefixed, followed by a separator and any parameters needed.
 * </p>
 * Example format: {@code COMMAND%parameter1%parameter2}
 * <br><br>
 * All messages must use the defined {@link #SEPARATOR} to split values consistently.
 *
 * @author anasv
 * @see ProtocolReaderClient
 * @see ProtocolWriterClient
 * @see ProtocolReaderServer
 * @see ProtocolWriterServer
 * @since 21.03.25
 */
public enum Command {

    /**
     * Server sends informational messages.
     */
    INFO,

    /**
     * Client joins a lobby.
     */
    JOIN,

    /**
     * Client selects a bike.
     */
    VELO,

    /**
     * Client signals they are ready.
     */
    RADY,

    /**
     * Host starts the game.
     */
    STRT,

    /**
     * Host restarts the game.
     */
    RSTT,

    /**
     * Client wants to leave the game.
     */
    QUIT,

    /**
     * Client confirms quit prompt.
     */
    QCNF,

    /**
     * Client requests to roll the dice.
     */
    ROLL,

    /**
     * Client selects a field.
     */
    CHOS,

    /**
     * Client confirms movement.
     */
    MOVE,

    /**
     * Client skips his turn.
     */
    NEXT,

    /**
     * Chat message sent to all members in the same lobby.
     */
    CHAT,

    /**
     * Client requests to change nickname.
     */
    NICK,

    /**
     * Server pings client to check connection.
     */
    PING,

    /**
     * Client responds to ping with pong to confirm it's still alive.
     */
    PONG,

    /**
     * Whisper (a private message) between two clients.
     */
    WISP,

    /**
     * Client deselects a previously selected field.
     */
    DEOS,

    /**
     * Client sends a global broadcast message to all users on the server.
     */
    BROD,

    /**
     * Server sends a list of all users on the server.
     */
    LIST,

    /**
     * Client creates a new lobby.
     */
    CRLO,

    /**
     * Server sends a list of all lobbies with its members.
     */
    LOME,

    /**
     * Server sends a list of all games and their status.
     */
    GLST,

    /**
     * Server sends the Highscore list.
     */
    HIGH,

    /**
     * Server sends the winner list.
     */
    WINN,

    /**
     * Handles that the game has ended. Either the host stopped it or the game was finished.
     */
    FNSH;

    /**
     * The character used to separate command parts in a message.
     * <p>
     * Example usage: {@code Command.CHAT + Command.SEPARATOR + "Hello everyone!"}
     * </p>
     */

    public static final String SEPARATOR = "%"; // blank space als Separator
    private static final Logger LOGGER = LogManager.getLogger(Command.class);
    /**
     * Prints the command "INFO"
     */
    public static void INFO() {
        LOGGER.info("INFO");
    }

    /**
     * Prints the command "JOIN"
     */
    public static void JOIN() {
        LOGGER.info("JOIN");
    }

    /**
     * Prints the command "VELO"
     */
    public static void VELO() {
        LOGGER.info("VELO");
    }

    /**
     * Prints the command "RADY"
     */
    public static void RADY() {
        LOGGER.info("RADY");
    }

    /**
     * Prints the command "STRT"
     */
    public static void STRT() {
        LOGGER.info("STRT");
    }

    /**
     * Prints the command "RSTT"
     */
    public static void RSTT() {
        LOGGER.info("RSTT");
    }

    /**
     * Prints the command "QUIT"
     */
    public static void QUIT() {
        LOGGER.info("QUIT");
    }

    /**
     * Prints the command "QCNF"
     */
    public static void QCNF() {
        LOGGER.info("QCNF");
    }

    /**
     * Prints the command "ROLL"
     */
    public static void ROLL() {
        LOGGER.info("ROLL");
    }

    /**
     * Prints the command "CHOS"
     */
    public static void CHOS() {
        LOGGER.info("CHOS");
    }

    /**
     * Prints the command "MOVE"
     */
    public static void MOVE() {
        LOGGER.info("MOVE");
    }

    /**
     * Prints the command "CHAT"
     */
    public static void CHAT() {
        LOGGER.info("CHAT");
    }

    /**
     * Prints the command "NICK"
     */
    public static void NICK() { LOGGER.info("NICK"); }

    /**
     * Prints the command "PING"
     */
    public static void PING() { LOGGER.info("PING"); }

    /**
     * Prints the command "PONG"
     */
    public static void PONG() {
        LOGGER.info("PONG");
    }

    /**
     * Prints the command "WISP"
     */
    public static void WISP() {
        LOGGER.info("WISP");
    }

    /**
     * Prints the command "DEOS"
     */
    public static void DEOS() {
        LOGGER.info("DEOS");
    }

    /**
     * Prints the command "BROD"
     */
    public static void BROD() {
        LOGGER.info("BROD");
    }

    /**
     * Prints the command "LIST"
     */
    public static void LIST() { LOGGER.info("LIST"); }

    /**
     * Prints the command "CRLO"
     */
    public static void CRLO() {
        LOGGER.info("CRLO");
    }

    /**
     * Prints the command "LOME"
     */
    public static void LOME() { LOGGER.info("LOME"); }

    /**
     * Prints the command "GLST"
     */
    public static void GLST() {
        LOGGER.info("GLST");
    }

    /**
     * Prints the command "NEXT"
     */
    public static void NEXT() {
        LOGGER.info("NEXT");
    }

    /**
     * Prints the command "FNSH"
     */
    public static void FNSH() {
        LOGGER.info("FNSH");
    }

    /**
     * Prints the Command "HIGH"
     */
    public static void HIGH() {
        LOGGER.info("HIGH");
    }

    /**
     * Prints the Command "WINN"
     */
    public static void WINN() {
        LOGGER.info("WINN");
    }
}
