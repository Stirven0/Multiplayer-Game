package com.aa.client.ui;

import com.aa.client.game.GameClient;
import com.aa.client.mcp.ClientMcpServer;
import com.aa.shared.message.GameEndMessage;
import javafx.stage.StageStyle;
import javafx.stage.Stage;

/**
 * Gestiona las pantallas (escenas) de la aplicación.
 * Se encarga de la navegación entre login, lobby, juego y fin de partida.
 */
public class ScreenManager {

    private Stage stage;
    private GameClient gameClient;
    private LobbyScreen lobbyScreen;
    private LoginScreen loginScreen;
    private volatile ClientMcpServer mcpServer;

    /** Crea el gestor de pantallas e inicializa el GameClient asociado. */
    public ScreenManager() {
        this.gameClient = new GameClient(this);
    }

    /**
     * Inicializa el Stage principal, configura estilo sin decoraciones y muestra la pantalla de login.
     * @param stage Stage principal de JavaFX
     */
    public void init(Stage stage) {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(com.aa.client.util.ClientConfig.TITLE);
        showLogin();
        stage.show();
    }

    /** Cambia a la pantalla de lobby (sala de espera). */
    public void showLobby() {
        gameClient.setCurrentRoomId(null);
        this.lobbyScreen = new LobbyScreen(gameClient);
        stage.setScene(lobbyScreen.createScene(stage));
    }

    /**
     * @return la pantalla de lobby actual, o null si no se ha mostrado
     */
    public LobbyScreen getLobbyScreen() {
        return lobbyScreen;
    }

    /** Cambia a la pantalla de login. */
    public void showLogin() {
        this.loginScreen = new LoginScreen(gameClient);
        stage.setScene(loginScreen.createScene(stage));
    }

    /**
     * Muestra un mensaje de error en la pantalla de login.
     * @param msg mensaje de error a mostrar
     */
    public void showLoginError(String msg) {
        if (loginScreen != null) loginScreen.setError(msg);
    }

    /** Cambia a la pantalla del juego en curso. */
    public void showGame() {
        stage.setScene(new GameScreen(gameClient).createScene(stage));
    }

    /**
     * Cambia a la pantalla de fin de partida con los resultados.
     * @param endMsg mensaje con los datos de finalización de la partida
     */
    public void showGameOver(GameEndMessage endMsg) {
        stage.setScene(new GameOverScreen(gameClient, endMsg).createScene(stage));
    }

    /**
     * @return el GameClient asociado a este gestor
     */
    public GameClient getGameClient() {
        return gameClient;
    }

    public void enableMcpMode() {
        System.out.println("[SCREEN] MCP mode enabled");
    }

    public ClientMcpServer getMcpServer() { return mcpServer; }
    public void setMcpServer(ClientMcpServer mcpServer) { this.mcpServer = mcpServer; }

    /** @return el Stage principal de la aplicación */
    public Stage getStage() { return stage; }

    /** Alterna entre modo ventana y pantalla completa. */
    public void toggleFullScreen() {
        stage.setFullScreen(!stage.isFullScreen());
    }

    /** @return true si la ventana está en pantalla completa */
    public boolean isFullScreen() {
        return stage.isFullScreen();
    }
}
