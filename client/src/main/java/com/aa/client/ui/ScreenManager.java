package com.aa.client.ui;

import com.aa.client.game.GameClient;
import com.aa.shared.message.GameEndMessage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenManager {

    private Stage stage;
    private GameClient gameClient;
    private LobbyScreen lobbyScreen;
    private LoginScreen loginScreen;

    public ScreenManager() {
        this.gameClient = new GameClient(this);
    }

    public void init(Stage stage) {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(com.aa.client.util.ClientConfig.TITLE);
        showLogin();
        stage.show();
    }

    public void showLobby() {
        gameClient.setCurrentRoomId(null);
        this.lobbyScreen = new LobbyScreen(gameClient);
        stage.setScene(lobbyScreen.createScene(stage));
    }

    public LobbyScreen getLobbyScreen() {
        return lobbyScreen;
    }

    public void showLogin() {
        this.loginScreen = new LoginScreen(gameClient);
        stage.setScene(loginScreen.createScene(stage));
    }

    public void showLoginError(String msg) {
        if (loginScreen != null) loginScreen.setError(msg);
    }

    public void showGame() {
        stage.setScene(new GameScreen(gameClient).createScene(stage));
    }

    public void showGameOver(GameEndMessage endMsg) {
        stage.setScene(new GameOverScreen(gameClient, endMsg).createScene(stage));
    }

    public GameClient getGameClient() {
        return gameClient;
    }
}
