package ch.unibas.dmi.dbis.cs108.client;

import java.io.*;
class InThread implements Runnable {
    InputStream in;
    public InThread(InputStream in) {
        this.in = in;
    }
    public void run() {
        int len;
        byte[] b = new byte[100];
        try {
            while (true) {
                if ((len=in.read(b))== -1) {
                    break;
                }
                System.out.write(b, 0, len);
            }
        }
        catch (IOException e) {
            System.err.println("ERROR: " + e.toString()); }
    }
}