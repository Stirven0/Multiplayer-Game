package com.aa.client.ui;

import com.aa.client.game.GameClient;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenManager {
    private Stage stage;
    private final GameClient gameClient;

    public ScreenManager() {
        this.gameClient = new GameClient(this);
    }

    public void init(Stage stage) {
        this.stage = stage;
        stage.setTitle(com.aa.client.util.ClientConfig.TITLE);
        showLogin();
        stage.show();
    }

    public void showLogin() {
        stage.setScene(new LoginScreen(gameClient).createScene());
    }

    public void showLobby() {
        stage.setScene(new LobbyScreen(gameClient).createScene());
    }

    public void showGame() {
        stage.setScene(new GameScreen(gameClient).createScene());
    }

    public GameClient getGameClient() {
        return gameClient;
    }
}