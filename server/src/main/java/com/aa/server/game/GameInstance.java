package com.aa.server.game;

import com.aa.server.game.engine.GameEngine;
import com.aa.server.game.engine.GameLoop;
import com.aa.server.game.map.GameMap;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;
import com.aa.shared.message.GameStateMessage;
import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameInstance {
    private final String gameId;
    private final Room room;
    private final GameState state;
    private final GameEngine engine;
    private final GameLoop loop;
    private final GameMap map;
    private final ConnectionManager connectionManager;
    private final ConcurrentLinkedQueue<PlayerInput> inputQueue = new ConcurrentLinkedQueue<>();
    private volatile int sequence = 0;

    public GameInstance(String gameId, Room room, GameMap map, ConnectionManager connectionManager) {
        this.gameId = gameId;
        this.room = room;
        this.map = map;
        this.connectionManager = connectionManager;
        this.state = new GameState(gameId, room.getMapId());
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
        loop.start();
    }

    public void stop() {
        loop.stop();
    }

    public void queueInput(PlayerInput input) {
        inputQueue.offer(input);
    }

    public boolean hasPlayer(String playerId) {
        return room.getPlayerIds().contains(playerId);
    }

    public void processTick(float deltaTime) {
        List<PlayerInput> inputs = new ArrayList<>();
        while (!inputQueue.isEmpty()) {
            inputs.add(inputQueue.poll());
        }

        state.incrementTick();
        state.setTimestamp(System.currentTimeMillis());
        engine.update(state, deltaTime, inputs);
    }

    public void broadcastState() {
        GameState snapshot = state.copy();
        GameStateMessage msg = new GameStateMessage(snapshot, sequence++);
        connectionManager.broadcastToPlayers(room.getPlayerIds(), msg);
    }

    private Vector2 randomSpawn() {
        double x = Math.random() * (map.width() - 100) + 50;
        double y = Math.random() * (map.height() - 100) + 50;
        return new Vector2(x, y);
    }
}
