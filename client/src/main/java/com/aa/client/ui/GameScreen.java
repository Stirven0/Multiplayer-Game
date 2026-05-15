package com.aa.client.ui;

import com.aa.client.game.GameClient;
import com.aa.client.input.InputHandler;
import com.aa.client.util.ClientConfig;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameScreen {
    private final GameClient gameClient;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final InputHandler inputHandler;
    private boolean paused = false;
    private VBox pauseOverlay;
    private VBox helpOverlay;

    public GameScreen(GameClient gameClient) {
        this.gameClient = gameClient;
        this.canvas = new Canvas(ClientConfig.WIDTH, ClientConfig.HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.inputHandler = gameClient.getInputHandler();
    }

    public Scene createScene(Stage stage) {
        StackPane gameArea = new StackPane(canvas);

        pauseOverlay = new VBox(15);
        pauseOverlay.setAlignment(Pos.CENTER);
        pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        pauseOverlay.setVisible(false);

        Label pauseTitle = new Label("PAUSA");
        pauseTitle.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        Button resumeBtn = new Button("Reanudar");
        resumeBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;");
        resumeBtn.setOnMouseEntered(e -> resumeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;"));
        resumeBtn.setOnMouseExited(e -> resumeBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;"));
        resumeBtn.setOnAction(e -> togglePause());

        Button helpBtn = new Button("Ayuda");
        helpBtn.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;");
        helpBtn.setOnMouseEntered(e -> helpBtn.setStyle("-fx-background-color: #5a5a5a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;"));
        helpBtn.setOnMouseExited(e -> helpBtn.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;"));
        helpBtn.setOnAction(e -> toggleHelp());

        Button leaveBtn = new Button("Abandonar partida");
        leaveBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;");
        leaveBtn.setOnMouseEntered(e -> leaveBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;"));
        leaveBtn.setOnMouseExited(e -> leaveBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 4;"));
        leaveBtn.setOnAction(e -> {
            paused = false;
            pauseOverlay.setVisible(false);
            gameClient.leaveRoom();
            gameClient.getScreenManager().showLobby();
        });

        pauseOverlay.getChildren().addAll(pauseTitle, resumeBtn, helpBtn, leaveBtn);

        helpOverlay = new VBox(10);
        helpOverlay.setAlignment(Pos.CENTER);
        helpOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-padding: 30;");
        helpOverlay.setVisible(false);

        Label helpTitle = new Label("AYUDA - CONTROLES");
        helpTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        String[] lines = {
            "WASD / Flechas ................. Moverse",
            "Mouse ......................... Apuntar",
            "Click izquierdo ............... Disparar",
            "SHIFT ......................... Correr",
            "",
            "Objetivo: Sé el último jugador en pie.",
            "Elimina a tus oponentes para ganar."
        };
        VBox textBox = new VBox(3);
        textBox.setAlignment(Pos.CENTER);
        for (String line : lines) {
            Label l = new Label(line);
            l.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px;");
            textBox.getChildren().add(l);
        }

        Button closeHelpBtn = new Button("Volver");
        closeHelpBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4;");
        closeHelpBtn.setOnAction(e -> toggleHelp());
        helpOverlay.getChildren().addAll(helpTitle, textBox, closeHelpBtn);

        StackPane centerStack = new StackPane(gameArea, pauseOverlay, helpOverlay);

        BorderPane root = new BorderPane();
        root.setTop(TitleBar.create("Partida en curso", stage));
        root.setCenter(centerStack);

        Scene scene = new Scene(root, ClientConfig.WIDTH, ClientConfig.HEIGHT + TitleBar.HEIGHT);

        inputHandler.attach(scene);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                togglePause();
                e.consume();
            }
        });

        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameClient.update(inputHandler, gc);
            }
        };
        loop.start();

        return scene;
    }

    private void togglePause() {
        paused = !paused;
        if (paused) {
            helpOverlay.setVisible(false);
        }
        pauseOverlay.setVisible(paused);
        gameClient.setPaused(paused);
    }

    private void toggleHelp() {
        boolean showing = !helpOverlay.isVisible();
        helpOverlay.setVisible(showing);
        pauseOverlay.setVisible(!showing);
    }
}
