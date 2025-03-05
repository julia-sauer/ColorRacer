package ch.unibas.dmi.dbis.cs108.game;

import java.util.Arrays;


public class Dice {
public static String[] roll() {
    String[] colors = new String[6];
    for (int i = 0; i < 6; i++) {
        double randomValue = Math.random();  // generates [0 .. 1)
        double scaled = randomValue * 6;     // gives [0 .. 6)
        int ceiled = (int) Math.ceil(scaled); // gives {1, 2, 3, 4, 5, 6}
        if (ceiled == 1) {
            colors[i] = "yellow";
        }
        if (ceiled == 2) {
            colors[i] = "orange";
        }
        if (ceiled == 3) {
            colors[i] = "red";
        }
        if (ceiled == 4) {
            colors[i] = "pink";
        }
        if (ceiled == 5) {
            colors[i] = "purple";
        }
        if (ceiled == 6) {
            colors[i] = "blue";
        }
    }
    System.out.println(Arrays.toString(colors)); //Zur Überprüfung
    return colors;
}

public static void main(String[] args) {
    roll();
}
}