package ch.unibas.dmi.dbis.cs108.gui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

/**
 * A visual JavaFX component for displaying animated confetti effects.
 * <p>
 * This pane can be added to a layout to create a confetti burst animation,
 * used for a celebratory effect such as when the winners are displayed.
 *
 * @author julia
 */
public class Confetti extends Pane {

    /**
     * Random number generator used for assigning random positions, colors,
     * directions, durations, and other effects to individual confetti pieces.
     */
    private final Random rnd = new Random();

    /**
     * Creates a burst of animated confetti rectangles entering from both sides of the pane.
     * <p>
     * Each confetti piece is given a random color, position, rotation, and movement path.
     * The pieces fly toward the center and fall down, fading out as they go. Once faded,
     * they are removed from the scene graph.
     *
     * @param count The number of confetti pieces to generate and animate.
     */
    public void burstFromSides(int count) {
        Bounds bounds = getLayoutBounds();

        for (int i = 0; i < count; i++) {
            Rectangle piece = new Rectangle(6, 12, Color.hsb(rnd.nextDouble() * 360, 0.8, 1.0));
            piece.setArcHeight(3);
            piece.setArcWidth(3);

            boolean fromLeft = rnd.nextBoolean();
            double startX = fromLeft ? -20 : bounds.getWidth() + 20;
            double startY = rnd.nextDouble() * bounds.getHeight();
            piece.setTranslateX(startX);
            piece.setTranslateY(startY);

            // Random drift across dialog
            double targetX = bounds.getWidth() / 2 + rnd.nextGaussian() * 80;
            double targetY = bounds.getHeight() + 40;

            // Spin animation
            RotateTransition spin = new RotateTransition(Duration.seconds(1 + rnd.nextDouble()), piece);
            spin.setByAngle(360 * (rnd.nextBoolean() ? 1 : -1));
            spin.setCycleCount(Animation.INDEFINITE);

            // Movement path
            TranslateTransition move = new TranslateTransition(Duration.seconds(2.5 + rnd.nextDouble()), piece);
            move.setToX(targetX);
            move.setToY(targetY);

            // Fade out
            FadeTransition fade = new FadeTransition(Duration.seconds(1.0), piece);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.seconds(1.5 + rnd.nextDouble()));

            // Clean up
            fade.setOnFinished(e -> getChildren().remove(piece));

            getChildren().add(piece);
            spin.play();
            move.play();
            fade.play();
        }
    }
}