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
     * All enum constants are defined here
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
    PONG;

    public static final String SEPARATOR = "%"; // blank space als Separator

    /**
     * Outputs the command “INFO”.
     */
    public static void INFO() {
        System.out.println("INFO");
    }
    /**
     * Outputs the command “JOIN”.
     */
    public static void JOIN() {

        System.out.println("JOIN");
    }

    /**
     * Outputs the command “VELO”.
     */
    public static void VELO() {

        System.out.println("VELO");
    }

    /**
     * Outputs the command “RADY”.
     */
    public static void RADY() {
        System.out.println("RADY");
    }

    /**
     * Outputs the command “STRT”.
     */
    public static void STRT() {
        System.out.println("STRT");
    }

    /**
     * Outputs the command “RSTT”.
     */
    public static void RSTT() {
        System.out.println("RSTT");
    }

    /**
     * Outputs the command “QUIT”.
     */
    public static void QUIT() {
        System.out.println("QUIT");
    }

    /**
     * Outputs the command “QCNF”.
     */
    public static void QCNF() {
        System.out.println("QCNF");
    }

    /**
     * Outputs the command “ROLL”.
     */
    public static void ROLL(){
        System.out.println("ROLL");
    }

    /**
     * Outputs the command “CHOS”.
     */
    public static void CHOS() {
        System.out.println("CHOS");
    }

    /**
     * Outputs the command “MOVE”.
     */
    public static void MOVE() {
        System.out.println("MOVE");
    }

    /**
     * Outputs the command “STAT”.
     */
    public static void STAT() {
        System.out.println("STAT");
    }

    /**
     * Outputs the command “CHAT”.
     */
    public static void CHAT() {
        System.out.println("CHAT");
    }

    /**
     * Outputs the command “NICK”.
     */
    public static void NICK() {
        System.out.println("NICK");
    }

    /**
     * Outputs "PING" .
     */
    public static void PING() {
        System.out.println("PING");
    }

    /**
     * Outputs "PONG" .
     */
    public static void PONG() {
        System.out.println("PONG");
    }

}
