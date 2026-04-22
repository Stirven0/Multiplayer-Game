package com.aa.client.game;

import com.aa.client.input.InputHandler;
import com.aa.client.network.ClientMessageListener;
import com.aa.client.network.NetworkClient;
import com.aa.client.render.Camera;
import com.aa.client.render.Renderer;
import com.aa.client.ui.ScreenManager;
import com.aa.client.util.ClientConfig;
import com.aa.shared.message.*;
import com.aa.shared.model.Player;
import com.aa.shared.state.GameState;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;

import java.net.URI;

public class GameClient implements ClientMessageListener {
    private final NetworkClient network;
    private final GameClientState state;
    private final InputHandler inputHandler;
    private final Renderer renderer;
    private final Camera camera;
    private final ScreenManager screenManager;
    private volatile boolean connected = false;

    public GameClient(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.state = new GameClientState();
        this.network = new NetworkClient(URI.create(ClientConfig.SERVER_URL), this);
        this.inputHandler = new InputHandler();
        this.camera = new Camera(ClientConfig.WIDTH, ClientConfig.HEIGHT);
        this.renderer = new Renderer(camera);
    }

    /**
     * Conecta y retorna true si tuvo éxito.
     * Debe llamarse desde un hilo background, NO desde JavaFX thread.
     */
    public boolean connect() {
        try {
            System.out.println("[CLIENT] Conectando a " + ClientConfig.SERVER_URL);
            boolean ok = network.connectBlocking(5000);
            if (ok) {
                System.out.println("[CLIENT] Conectado exitosamente");
                return true;
            } else {
                System.err.println("[CLIENT] Timeout de conexión");
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendLogin(String username, String password) {
        System.out.println("[CLIENT] Enviando login para: " + username);
        network.sendMessage(new LoginMessage(username, password));
    }

    public void createRoom(String mapId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "CREATE_ROOM");
        obj.addProperty("mapId", mapId);
        network.sendJson(obj);
    }

    public void joinRoom(String roomId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "JOIN_ROOM");
        obj.addProperty("roomId", roomId);
        network.sendJson(obj);
    }

    public void startGame() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "GAME_START");
        network.sendJson(obj);
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public void update(InputHandler input, GraphicsContext gc) {
        if (!connected) return;

        if (input.isMoving()) {
            network.sendMessage(input.getMoveMessage());
        }
        if (input.isShooting()) {
            Player local = getLocalPlayer();
            if (local != null) {
                double angle = input.getShootAngle(camera, local.getPosition());
                network.sendMessage(new ShootMessage(angle));
                input.clearShoot();
            }
        }

        Player local = getLocalPlayer();
        if (local != null) {
            camera.follow(local.getPosition());
        }

        renderer.render(gc, state.getCurrentState(), state.getLocalPlayerId());
    }

    private Player getLocalPlayer() {
        GameState gs = state.getCurrentState();
        if (gs == null || state.getLocalPlayerId() == null) return null;
        return gs.getPlayer(state.getLocalPlayerId());
    }

    // ==================== Network Callbacks ====================

    @Override
    public void onConnected() {
        connected = true;
        System.out.println("[CLIENT] Callback onConnected");
    }

    @Override
    public void onDisconnected(String reason) {
        connected = false;
        System.out.println("[CLIENT] Desconectado: " + reason);
    }

    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> handleMessage(msg));
    }

    @Override
    public void onError(String code, String description) {
        System.err.println("[CLIENT] Error " + code + ": " + description);
    }

    private void handleMessage(Message msg) {
        System.out.println("[CLIENT] Recibido: " + msg.getType());

        switch (msg.getType()) {
            case LOGIN_RESPONSE -> {
                LoginResponseMessage resp = (LoginResponseMessage) msg; // Necesitas crear esta clase
                state.setLocalPlayerId(resp.getUserId());
                System.out.println("[CLIENT] Login exitoso, userId: " + resp.getUserId());
                screenManager.showLobby();
            }
            case GAME_STATE -> {
                GameStateMessage gsm = (GameStateMessage) msg;
                state.updateState(gsm.getGameState());
                if (!state.isInGame()) {
                    state.setInGame(true);
                    screenManager.showGame();
                }
            }
            case ERROR -> {
                ErrorMessage err = (ErrorMessage) msg;
                System.err.println("[CLIENT] Server error: " + err.getMessage());
            }
            default -> {
                System.out.println("[CLIENT] Mensaje no manejado: " + msg.getType());
            }
        }
    }
}