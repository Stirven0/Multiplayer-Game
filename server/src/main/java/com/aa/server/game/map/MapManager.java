package com.aa.server.game.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapManager {
    private final Map<String, GameMap> maps = new ConcurrentHashMap<>();

    public MapManager() {
        loadDefaults();
    }

    private void loadDefaults() {
        GameMap defaultMap = new GameMap(
            "map_01",
            2000, 2000,
            java.util.List.of(
                new Obstacle(400, 400, 300, 100),
                new Obstacle(900, 800, 100, 300),
                new Obstacle(1200, 1200, 400, 400)
            )
        );
        maps.put(defaultMap.mapId(), defaultMap);
    }

    public GameMap getMap(String mapId) {
        return maps.get(mapId);
    }

    public GameMap getDefaultMap() {
        return maps.get("map_01");
    }
}
