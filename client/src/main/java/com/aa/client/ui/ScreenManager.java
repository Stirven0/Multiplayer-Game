package com.aa.client.ui;

import com.aa.client.game.GameClient;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenManager {

    private Stage stage;
    private GameClient gameClient;
    private LobbyScreen lobbyScreen;

    public ScreenManager() {
        this.gameClient = new GameClient(this);
    }

    public void init(Stage stage) {
        this.stage = stage;
        this.gameClient = new GameClient(this);
        stage.setTitle(com.aa.client.util.ClientConfig.TITLE);
        showLogin();
        stage.show();
    }

    public void showLobby() {
        this.lobbyScreen = new LobbyScreen(gameClient);
        stage.setScene(lobbyScreen.createScene());
    }

    public LobbyScreen getLobbyScreen() {
        return lobbyScreen;
    }

    public void showLogin() {
        stage.setScene(new LoginScreen(gameClient).createScene());
    }

    public void showGame() {
        stage.setScene(new GameScreen(gameClient).createScene());
    }

    public GameClient getGameClient() {
        return gameClient;
    }
}
