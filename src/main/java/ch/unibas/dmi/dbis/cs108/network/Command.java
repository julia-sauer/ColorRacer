package ch.unibas.dmi.dbis.cs108.network;

/**
 * This class contains static methods that are defined in the network protocol (Protocol Document).
 * Each protocol command is represented by a method that outputs the name of the command in the console.
 *
 * @author anasv
 * @since 21.03.25
 */
public enum Command {

    /**
     * Here are all enum-constants defined
     */
    INFO,
    JOIN,
    VELO,
    RADY,
    STRT,
    RSTT,
    QUIT,
    QCNF,
    ROLL,
    CHOS,
    MOVE,
    STAT,
    CHAT,
    NICK,
    PING,
    PONG,
    WISP,
    DEOS,
    BROD;

    public static final String SEPARATOR = "%"; // blank space als Separator

    /**
     * Prints the command "INFO"
     */
    public static void INFO() {
        System.out.println("INFO");
    }
    /**
     * Prints the command "JOIN"
     */
    public static void JOIN() {
        System.out.println("JOIN");
    }

    /**
     * Prints the command "VELO"
     */
    public static void VELO() {
        System.out.println("VELO");
    }

    /**
     * Prints the command "RADY"
     */
    public static void RADY() {
        System.out.println("RADY");
    }

    /**
     * Prints the command "STRT"
     */
    public static void STRT() {
        System.out.println("STRT");
    }

    /**
     * Prints the command "RSTT"
     */
    public static void RSTT() {
        System.out.println("RSTT");
    }

    /**
     * Prints the command "QUIT"
     */
    public static void QUIT() {
        System.out.println("QUIT");
    }

    /**
     * Prints the command "QCNF"
     */
    public static void QCNF() {
        System.out.println("QCNF");
    }

    /**
     * Prints the command "ROLL"
     */
    public static void ROLL(){
        System.out.println("ROLL");
    }

    /**
     * Prints the command "CHOS"
     */
    public static void CHOS() {
        System.out.println("CHOS");
    }

    /**
     * Prints the command "MOVE"
     */
    public static void MOVE() {
        System.out.println("MOVE");
    }

    /**
     * Prints the command "STAT"
     */
    public static void STAT() {
        System.out.println("STAT");
    }

    /**
     * Prints the command "CHAT"
     */
    public static void CHAT() {
        System.out.println("CHAT");
    }

    /**
     * Prints the command "NICK"
     */
    public static void NICK() {
        System.out.println("NICK");
    }

    /**
     * Prints the command "PING"
     */
    public static void PING() {
        System.out.println("PING");
    }

    /**
     * Prints the command "PONG"
     */
    public static void PONG() {
        System.out.println("PONG");
    }

    /**
     * Prints the command "WISP"
     */
    public static void WISP(){
        System.out.println("WISP");
    }

    /**
     * Prints the command "DEOS"
     */
    public static void DEOS(){
        System.out.println("DEOS");
    }

    /**
     * Prints the command "BROD"
     */
    public static void BROD() {
        System.out.println("BROD");
    }

}
