package com.aa.client.ui;

import com.aa.client.game.GameClient;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LobbyScreen {

    private final GameClient gameClient;
    private Label roomInfoLabel;
    private ComboBox<String> mapSelector;

    public LobbyScreen(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public Scene createScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Lobby");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        roomInfoLabel = new Label("No estás en ninguna sala");
        roomInfoLabel.setStyle("-fx-text-fill: #aaa;");

        
        // Selector de mapa
        mapSelector = new ComboBox<>();
        mapSelector.getItems().addAll("map_01", "map_02", "map_03");
        mapSelector.setValue("map_01");

        Button createBtn = new Button("Create Room");
        createBtn.setOnAction(e -> {
            String selectedMap = mapSelector.getValue();
            gameClient.createRoom(selectedMap);
            createBtn.setDisable(true); // evitar doble click
        });

        TextField roomId = new TextField();
        roomId.setPromptText("Room ID");
        roomId.setMaxWidth(200);

        Button useMyRoomBtn = new Button("Usar mi sala");
        useMyRoomBtn.setOnAction(e -> {
            String myRoom = gameClient.getCurrentRoomId();
            if (myRoom != null) {
                roomId.setText(myRoom);
            }
        });

        Button joinBtn = new Button("Join Room");
        joinBtn.setOnAction(e -> {
            String roomIdS = roomId.getText().trim();
            if (roomIdS.isEmpty()) {
                System.err.println("[CLIENT] roomId vacío");
                return;
            }
            gameClient.joinRoom(roomIdS);
        });

        Button startBtn = new Button("Start Game");
        startBtn.setOnAction(e -> gameClient.startGame());

        root
            .getChildren()
            .addAll(
                title,
                roomInfoLabel,
                createBtn,
                mapSelector,
                roomId,
                useMyRoomBtn,
                joinBtn,
                startBtn
            );
        return new Scene(
            root,
            com.aa.client.util.ClientConfig.WIDTH,
            com.aa.client.util.ClientConfig.HEIGHT
        );
    }

    public void updateRoomInfo(String roomId) {
        Platform.runLater(() -> {
            roomInfoLabel.setText("Sala: " + roomId);
        });
    }
}
