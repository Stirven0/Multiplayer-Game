package com.aa.client.ui;

import com.aa.client.game.GameClient;
import com.aa.client.input.InputHandler;
import com.aa.client.render.Renderer;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;

public class GameScreen {
    private final GameClient gameClient;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final InputHandler inputHandler;

    public GameScreen(GameClient gameClient) {
        this.gameClient = gameClient;
        this.canvas = new Canvas(com.aa.client.util.ClientConfig.WIDTH, com.aa.client.util.ClientConfig.HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.inputHandler = gameClient.getInputHandler();
    }

    public Scene createScene() {
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        // Input
        inputHandler.attach(scene);

        // Game Loop
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameClient.update(inputHandler, gc);
            }
        };
        loop.start();

        return scene;
    }
}