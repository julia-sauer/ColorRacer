package ch.unibas.dmi.dbis.cs108.gui;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

public class Confetti extends Pane {
    private final Random rnd = new Random();

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