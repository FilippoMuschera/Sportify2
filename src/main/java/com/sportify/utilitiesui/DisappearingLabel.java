package com.sportify.utilitiesui;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class DisappearingLabel {
    private DisappearingLabel(){

    }
    private static Timeline createBlinker(Node node) {
        Timeline blink = new Timeline(
                new KeyFrame(Duration.seconds(2), new KeyValue(node.visibleProperty(), true, Interpolator.DISCRETE)));
        blink.setCycleCount(1);

        return blink;
    }

    private static FadeTransition createFader(Node node) {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(event -> node.setVisible(false));
        return fade;
    }

    public static SequentialTransition createTransition(Label label){
        Timeline blinker = createBlinker(label);
        FadeTransition fader = createFader(label);
        return new SequentialTransition(label, blinker, fader);
    }
}
