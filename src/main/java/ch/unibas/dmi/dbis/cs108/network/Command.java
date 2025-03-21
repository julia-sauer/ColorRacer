package ch.unibas.dmi.dbis.cs108.network;

/**
 * Diese Klasse enthält statische Methoden, die im Netzwerkprotokoll (Protocol Document) definiert sind.
 * Jeder Protokollbefehl wird durch eine Methode dargestellt, die den Namen des Befehls in der Console ausgibt.
 *
 * @author anasv
 * @since 21.03.25
 */
public enum Command {

    /**
     * Gibt den Befehl "JOIN" aus.
     */
    public static void JOIN() {
        System.out.println("JOIN");
    }

    /**
     * Gibt den Befehl "VELO" aus.
     */
    public static void VELO() {
        System.out.println("VELO");
    }

    /**
     * Gibt den Befehl "RADY" aus.
     */
    public static void RADY() {
        System.out.println("RADY");
    }

    /**
     * Gibt den Befehl "STRT" aus.
     */
    public static void STRT() {
        System.out.println("STRT");
    }

    /**
     * Gibt den Befehl "RSTT" aus.
     */
    public static void RSTT() {
        System.out.println("RSTT");
    }

    /**
     * Gibt den Befehl "QUIT" aus.
     */
    public static void QUIT() {
        System.out.println("QUIT");
    }

    /**
     * Gibt den Befehl "QCNF" aus.
     */
    public static void QCNF() {
        System.out.println("QCNF");
    }

    /**
     * Gibt den Befehl "ROLL" aus.
     */
    public static void ROLL(){
        System.out.println("ROLL");
    }

    /**
     * Gibt den Befehl "CHOS" aus.
     */
    public static void CHOS() {
        System.out.println("CHOS");
    }

    /**
     * Gibt den Befehl "MOVE" aus.
     */
    public static void MOVE() {
        System.out.println("MOVE");
    }

    /**
     * Gibt den Befehl "STAT" aus.
     */
    public static void STAT() {
        System.out.println("STAT");
    }

    /**
     * Gibt den Befehl "CHAT" aus.
     */
    public static void CHAT() {
        System.out.println("CHAT");
    }

    /**
     * Gibt den Befehl "NICK" aus.
     */
    public static void NICK() {
        System.out.println("NICK");
    }

    /**
     * Gibt "PING" aus.
     */
    public static void PING() {
        System.out.println("PING");
    }

    /**
     * Gibt "PONG" aus.
     */
    public static void PONG() {
        System.out.println("PONG");
    }

}
