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
        bar.setStyle("-fx-background-color: linear-gradient(to right, #0d1117, #161b22); -fx-padding: 0 12; -fx-min-height: " + HEIGHT + "px; -fx-alignment: center-left;");

        Label title = new Label(text);
        title.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minBtn = null;
        if (showMinimize) {
            Button mb = new Button("─");
            String baseMin = "-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 10; -fx-min-height: " + HEIGHT + "px;";
            String hoverMin = "-fx-background-color: #21262d; -fx-text-fill: #f0f6fc; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 10; -fx-min-height: " + HEIGHT + "px;";
            mb.setStyle(baseMin);
            mb.setOnMouseEntered(e -> mb.setStyle(hoverMin));
            mb.setOnMouseExited(e -> mb.setStyle(baseMin));
            mb.setOnAction(e -> stage.setIconified(true));
            minBtn = mb;
        }

        Button closeBtn = new Button("✕");
        String baseClose = "-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 10; -fx-min-height: " + HEIGHT + "px;";
        String hoverClose = "-fx-background-color: #da3633; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 10; -fx-min-height: " + HEIGHT + "px;";
        closeBtn.setStyle(baseClose);
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(hoverClose));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(baseClose));
        closeBtn.setOnAction(e -> Platform.exit());

        if (showMinimize && minBtn != null) {
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
