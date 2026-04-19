package com.aa.server.game;

import com.aa.server.game.map.GameMap;
import com.aa.server.game.map.MapManager;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;

import java.util.concurrent.ConcurrentHashMap;

public class GameInstanceManager {
    private final ConcurrentHashMap<String, GameInstance> instances = new ConcurrentHashMap<>();
    private final ConnectionManager connectionManager;
    private final MapManager mapManager;

    public GameInstanceManager(ConnectionManager connectionManager, MapManager mapManager) {
        this.connectionManager = connectionManager;
        this.mapManager = mapManager;
    }

    public void createAndStart(Room room) {
        GameMap map = mapManager.getMap(room.getMapId());
        if (map == null) map = mapManager.getDefaultMap();

        GameInstance instance = new GameInstance(room.getRoomId(), room, map, connectionManager);
        instances.put(room.getRoomId(), instance);
        instance.start();
    }

    public GameInstance getGameByPlayer(String playerId) {
        return instances.values().stream()
                .filter(g -> g.hasPlayer(playerId))
                .findFirst()
                .orElse(null);
    }

    public void endGame(String roomId) {
        GameInstance gi = instances.remove(roomId);
        if (gi != null) gi.stop();
    }
}
