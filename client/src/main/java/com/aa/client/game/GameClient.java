package com.aa.client.game;

import com.aa.client.asset.AudioManager;
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
import java.net.URI;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;

public class GameClient implements ClientMessageListener {

    private NetworkClient network;
    private final GameClientState state;
    private final InputHandler inputHandler;
    private final Renderer renderer;
    private final Camera camera;
    private final ScreenManager screenManager;
    private volatile String currentRoomId;
    private volatile String currentUsername;
    private volatile boolean connected = false;
    private volatile boolean paused = false;

    public GameClient(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.state = new GameClientState();
        this.network = createNetworkClient();
        this.inputHandler = new InputHandler();
        this.camera = new Camera(ClientConfig.WIDTH, ClientConfig.HEIGHT);
        this.renderer = new Renderer(camera);
    }

    private NetworkClient createNetworkClient() {
        return new NetworkClient(URI.create(ClientConfig.SERVER_URL), this);
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public ScreenManager getScreenManager() {
        return screenManager;
    }

    public void setCurrentRoomId(String roomId) {
        this.currentRoomId = roomId;
    }

    public String getCurrentUsername() { return currentUsername; }
    public void setCurrentUsername(String username) { this.currentUsername = username; }

    /**
     * Conecta y retorna true si tuvo éxito.
     * Debe llamarse desde un hilo background, NO desde JavaFX thread.
     */
    public boolean connect() {
        try {
            System.out.println(
                "[CLIENT] Conectando a " + ClientConfig.SERVER_URL
            );
            if (network.isClosed()) {
                System.out.println("[CLIENT] Recreando NetworkClient (estaba cerrado)");
                network = createNetworkClient();
            }
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

    public void sendLogin(String username, String password, boolean register) {
        System.out.println("[CLIENT] Enviando login para: " + username + " register=" + register);
        network.sendMessage(new LoginMessage(username, password, register));
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

    public void leaveRoom() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "LEAVE_ROOM");
        network.sendJson(obj);
        currentRoomId = null;
    }

    public void requestRoomList() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "ROOM_LIST");
        network.sendJson(obj);
    }

    public void logout() {
        connected = false;
        state.setInGame(false);
        state.setCurrentState(null);
        state.setLocalPlayerId(null);
        currentRoomId = null;
        currentUsername = null;
        network.close();
        screenManager.showLogin();
    }

    public void setPaused(boolean paused) { this.paused = paused; }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    private int frameCount = 0;

    public void update(InputHandler input, GraphicsContext gc) {
        frameCount++;
        if (frameCount % 60 == 0) {
            System.out.println("[UPDATE] frame=" + frameCount + " connected=" + connected + " localPlayerId=" + state.getLocalPlayerId() + " state=" + (state.getCurrentState() != null));
        }
        try {
            if (connected) {
                if (!paused) {
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
                }

                Player local = getLocalPlayer();
                if (local != null) {
                    GameState gs = state.getCurrentState();
                    if (gs != null) {
                        try {
                            camera.setBounds(gs.getMapWidth(), gs.getMapHeight());
                        } catch (Throwable thr) {
                            // ignorar - clase desactualizada, se corrige con mvn clean install
                        }
                    }
                    camera.follow(local.getPosition());
                }
            }

            renderer.render(gc, state.getCurrentState(), state.getLocalPlayerId(),
                input.getMouseScreenX(), input.getMouseScreenY());
        } catch (Throwable t) {
            System.err.println("[CLIENT] Error en update: " + t.getMessage());
            t.printStackTrace();
        }
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
        boolean wasInGame = state.isInGame();
        state.setInGame(false);
        if (wasInGame) {
            Platform.runLater(() -> screenManager.showLobby());
        }
        System.out.println("[CLIENT] Desconectado: " + reason + " wasInGame=" + wasInGame);
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
        try {
            System.out.println("[CLIENT] Recibido: " + msg.getType());

            switch (msg.getType()) {
                case LOGIN_RESPONSE -> {
                    LoginResponseMessage resp = (LoginResponseMessage) msg;
                    if (resp.isSuccess()) {
                        state.setLocalPlayerId(resp.getUserId());
                        currentUsername = resp.getUsername();
                        System.out.println("[CLIENT] Login exitoso, userId: " + resp.getUserId());
                        screenManager.showLobby();
                    } else {
                        String err = resp.getErrorMessage();
                        System.err.println("[CLIENT] Login fallido: " + err);
                        screenManager.showLoginError(err);
                    }
                }
                case ROOM_CREATED -> {
                    RoomCreatedMessage rcm = (RoomCreatedMessage) msg;
                    this.currentRoomId = rcm.getRoomId();
                    System.out.println("[CLIENT] Sala creada: " + rcm.getRoomId());
                    if (screenManager.getLobbyScreen() != null) {
                        screenManager.getLobbyScreen().updateRoomInfo(rcm.getRoomId());
                    }
                }
                case ROOM_UPDATED -> {
                    RoomUpdatedMessage rum = (RoomUpdatedMessage) msg;
                    if (screenManager.getLobbyScreen() != null) {
                        screenManager.getLobbyScreen().updatePlayerList(rum.getPlayerIds());
                    }
                }
                case JOIN_ROOM_RESPONSE -> {
                    JoinRoomResponseMessage jrm = (JoinRoomResponseMessage) msg;
                    if (jrm.isSuccess()) {
                        this.currentRoomId = jrm.getRoomId();
                        System.out.println("[CLIENT] Unido a sala: " + jrm.getRoomId());
                    } else {
                        String err = jrm.getMessage();
                        System.err.println("[CLIENT] Error al unirse: " + err);
                        if (screenManager.getLobbyScreen() != null) {
                            screenManager.getLobbyScreen().setError(err);
                        }
                    }
                }
                case ROOM_LIST_RESPONSE -> {
                    RoomListResponseMessage rlm = (RoomListResponseMessage) msg;
                    if (screenManager.getLobbyScreen() != null) {
                        screenManager.getLobbyScreen().updateRoomList(rlm.getRooms());
                    }
                }
                case GAME_STATE -> {
                    GameStateMessage gsm = (GameStateMessage) msg;
                    state.updateState(gsm.getGameState());
                    if (!state.isInGame()) {
                        state.setInGame(true);
                        screenManager.showGame();
                        AudioManager.stopMusic();
                        AudioManager.playMusic("music/battle_theme_01.mp3");
                    }
                }
                case PING -> {}
                case ERROR -> {
                    ErrorMessage err = (ErrorMessage) msg;
                    System.err.println("[CLIENT] Server error: " + err.getMessage());
                    if (screenManager.getLobbyScreen() != null) {
                        screenManager.getLobbyScreen().setError(err.getMessage());
                    }
                }
                case PLAYER_HIT -> {
                    AudioManager.playHit();
                }
                case GAME_END -> {
                    GameEndMessage gem = (GameEndMessage) msg;
                    state.setInGame(false);
                    state.setCurrentState(null);
                    System.out.println("[CLIENT] Partida terminada, ganador: " + gem.getWinnerUsername());
                    screenManager.showGameOver(gem);
                }
                default -> System.out.println("[CLIENT] Mensaje no manejado: " + msg.getType());
            }
        } catch (Throwable t) {
            System.err.println("[CLIENT] Error en handleMessage: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
