package com.aa.client.ui;

import com.aa.client.game.GameClient;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LobbyScreen {
    private final GameClient gameClient;

    public LobbyScreen(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public Scene createScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Lobby");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        Button createBtn = new Button("Create Room");
        createBtn.setOnAction(e -> gameClient.createRoom("map_01"));

        TextField roomId = new TextField();
        roomId.setPromptText("Room ID");
        roomId.setMaxWidth(200);

        Button joinBtn = new Button("Join Room");
        joinBtn.setOnAction(e -> gameClient.joinRoom(roomId.getText()));

        Button startBtn = new Button("Start Game");
        startBtn.setOnAction(e -> gameClient.startGame());

        root.getChildren().addAll(title, createBtn, roomId, joinBtn, startBtn);
        return new Scene(root, com.aa.client.util.ClientConfig.WIDTH, com.aa.client.util.ClientConfig.HEIGHT);
    }
}