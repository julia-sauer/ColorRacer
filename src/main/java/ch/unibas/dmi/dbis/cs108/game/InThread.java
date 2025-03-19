package ch.unibas.dmi.dbis.cs108.game;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class InThread implements Runnable {
    private final BufferedReader reader; //  Use only BufferedReader

    // Constructor for InputStream (Main Use Case)
    public InThread(InputStream in) {
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    // Constructor for BufferedReader (Alternative)
    public InThread(BufferedReader reader) {
        this.reader = reader;
    }

    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Properly handles UTF-8 messages
            }
        } catch (IOException e) {
            System.err.println("Error reading input stream: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


