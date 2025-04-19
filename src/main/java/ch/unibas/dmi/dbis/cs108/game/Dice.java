package ch.unibas.dmi.dbis.cs108.game;

/**
 * The class Dice provides a method to roll the six dice by random generating
 * six numbers between 1 and 6 which then are connected to colors.
 * @author Jana
 */
public class Dice {

    /**
     * This method generates random numbers between 1 and 6.
     * The numbers are assigned to colors which then result in an array of six colors.
     * @return array with six rolled colors
     */
    public String[] roll() {
        String[] colors = new String[6];
        for (int i = 0; i < 6; i++) {
            double randomValue = getRandom();
            // generates [0 .. 1)
            double scaled = randomValue * 6;     // gives [0 .. 6)
            int ceiled = (int) (randomValue * 6) + 1;
            // gives {1, 2, 3, 4, 5, 6}
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
        return colors;
    }

    /**
     * Method that return number between 0 and 1
     * @return number between 0 and 1
     */
    public double getRandom() {
        return Math.random();
    }
}