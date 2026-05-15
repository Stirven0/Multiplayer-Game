package com.aa.client.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class TitleBar {
    public static final int HEIGHT = 35;

    public static HBox create(String text, Stage stage) {
        return create(text, stage, false);
    }

    public static HBox create(String text, Stage stage, boolean showMinimize) {
        HBox bar = new HBox(8);
        bar.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 6 12; -fx-min-height: " + HEIGHT + "px; -fx-alignment: center-left;");

        Label title = new Label(text);
        title.setStyle("-fx-text-fill: #ccc; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 6;");
        closeBtn.setOnAction(e -> Platform.exit());

        if (showMinimize) {
            Button minBtn = new Button("─");
            minBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #aaa; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 6;");
            minBtn.setOnAction(e -> stage.setIconified(true));
            bar.getChildren().addAll(title, spacer, minBtn, closeBtn);
        } else {
            bar.getChildren().addAll(title, spacer, closeBtn);
        }

        final double[] offset = new double[2];
        bar.setOnMousePressed(e -> {
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });
        bar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - offset[0]);
            stage.setY(e.getScreenY() - offset[1]);
        });

        return bar;
    }
}
