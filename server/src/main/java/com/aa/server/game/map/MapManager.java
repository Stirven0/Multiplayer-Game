package com.aa.server.game.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapManager {
    private final Map<String, GameMap> maps = new ConcurrentHashMap<>();

    public MapManager() {
        loadDefaults();
    }

    private void loadDefaults() {
        register(MapLoader.loadFromJson("/maps/map_01.json"));
        register(MapLoader.loadFromJson("/maps/map_02.json"));
        register(MapLoader.loadFromJson("/maps/map_03.json"));
        register(MapLoader.loadFromJson("/maps/map_04.json"));
    }

    private void register(GameMap map) {
        maps.put(map.mapId(), map);
    }

    public GameMap getMap(String mapId) {
        GameMap m = maps.get(mapId);
        if (m == null) {
            System.err.println("[MAP] Map not found: " + mapId + ", using default");
            return getDefaultMap();
        }
        return m;
    }

    public GameMap getDefaultMap() {
        GameMap m = maps.get("map_01");
        if (m == null) {
            // fallback: pick first available
            m = maps.values().stream().findFirst().orElse(null);
        }
        return m;
    }

    public Map<String, GameMap> getAllMaps() {
        return Map.copyOf(maps);
    }
}