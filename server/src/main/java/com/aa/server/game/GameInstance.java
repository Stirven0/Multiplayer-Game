package com.aa.server.game;

import com.aa.server.game.engine.GameEngine;
import com.aa.server.game.engine.GameLoop;
import com.aa.server.game.map.GameMap;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;
import com.aa.shared.message.GameEndMessage;
import com.aa.shared.message.GameStateMessage;
import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class GameInstance {
    private final String gameId;
    private final Room room;
    private final GameState state;
    private final GameEngine engine;
    private final GameLoop loop;
    private final GameMap map;
    private final ConnectionManager connectionManager;
    private final ConcurrentLinkedQueue<PlayerInput> inputQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> disconnectQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> reconnectQueue = new ConcurrentLinkedQueue<>();
    private volatile int sequence = 0;
    private Runnable onGameEndCallback;

    public GameInstance(String gameId, Room room, GameMap map, ConnectionManager connectionManager) {
        this.gameId = gameId;
        this.room = room;
        this.map = map;
        this.connectionManager = connectionManager;
        this.state = new GameState(gameId, room.getMapId());
        this.state.setObstacles(map.obstacles());
        this.state.setMapWidth(map.width());
        this.state.setMapHeight(map.height());
        this.engine = new GameEngine();

        // Spawn players
        for (String playerId : room.getPlayerIds()) {
            Player p = new Player(playerId, playerId, randomSpawn());
            state.addPlayer(p);
        }

        this.loop = new GameLoop(this);
    }

    public void start() {
        state.setStatus(GameState.GameStatus.PLAYING);
        state.setStartTime(System.currentTimeMillis());
        loop.start();
    }

    public void stop() {
        loop.stop();
    }

    public void setOnGameEndCallback(Runnable callback) {
        this.onGameEndCallback = callback;
    }

    public Runnable getOnGameEndCallback() {
        return onGameEndCallback;
    }

    public void queueInput(PlayerInput input) {
        inputQueue.offer(input);
    }
		
    public boolean hasPendingInputs() {
        return !inputQueue.isEmpty();
    }

    public ConnectionManager getConnectionManager() { return connectionManager; }

    public GameState getState() { return state; }

    public boolean hasPlayer(String playerId) {
        return room.getPlayerIds().contains(playerId);
    }

    public void processTick(float deltaTime) {
        if (state.getStatus() == GameState.GameStatus.FINISHED) return;

        // Procesar desconexiones encoladas desde hilos externos
        String disconnectedId;
        while ((disconnectedId = disconnectQueue.poll()) != null) {
            Player p = state.getPlayer(disconnectedId);
            if (p != null && p.isAlive()) {
                p.setAlive(false);
                p.setHealth(0);
            }
        }

        // Procesar reconexiones encoladas desde hilos externos
        String reconnectedId;
        while ((reconnectedId = reconnectQueue.poll()) != null) {
            Player p = state.getPlayer(reconnectedId);
            if (p != null && !p.isAlive()) {
                p.setAlive(true);
                p.setHealth(100);
                p.setPosition(randomSpawn());
            }
        }

        List<PlayerInput> inputs = new ArrayList<>();
        while (!inputQueue.isEmpty()) {
            inputs.add(inputQueue.poll());
        }

        state.incrementTick();
        state.setTimestamp(System.currentTimeMillis());
        engine.update(state, deltaTime, inputs, map);

        // Detectar fin de partida: <= 1 jugador vivo
        long aliveCount = state.getAllPlayers().stream().filter(Player::isAlive).count();
        if (aliveCount <= 1) {
            state.setStatus(GameState.GameStatus.FINISHED);
            state.setEndTime(System.currentTimeMillis());
        }
    }

    public void broadcastState() {
        GameState snapshot = state.copy();
        GameStateMessage msg = new GameStateMessage(snapshot, sequence++);
        connectionManager.broadcastToPlayers(room.getPlayerIds(), msg);
    }

    public boolean isFinished() {
        return state.getStatus() == GameState.GameStatus.FINISHED;
    }

    public GameEndMessage createGameEndMessage() {
        Player winner = null;
        for (Player p : state.getAllPlayers()) {
            if (p.isAlive()) {
                winner = p;
                break;
            }
        }

        String finalWinnerId = winner != null ? winner.getId() : "";
        String finalWinnerUsername = winner != null ? winner.getUsername() : "Empate";

        List<GameEndMessage.PlayerScore> scores = state.getAllPlayers().stream()
            .sorted(Comparator.comparingInt(Player::getKills).reversed())
            .map(p -> new GameEndMessage.PlayerScore(
                p.getId(), p.getUsername(), p.getKills(), p.getDeaths(),
                p.getId().equals(finalWinnerId)))
            .collect(Collectors.toList());

        long duration = state.getEndTime() - state.getStartTime();
        return new GameEndMessage(gameId, finalWinnerId, finalWinnerUsername, scores, duration);
    }

    public void markPlayerDisconnected(String playerId) {
        inputQueue.removeIf(in -> in.playerId().equals(playerId));
        disconnectQueue.offer(playerId);
    }

    public void queueReconnect(String playerId) {
        reconnectQueue.offer(playerId);
    }

    public Vector2 randomSpawn() {
        double x = Math.random() * (map.width() - 100) + 50;
        double y = Math.random() * (map.height() - 100) + 50;
        return new Vector2(x, y);
    }
}
