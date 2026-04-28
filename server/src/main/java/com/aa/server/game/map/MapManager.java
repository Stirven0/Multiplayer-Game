package com.aa.server.game.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapManager {
    private final Map<String, GameMap> maps = new ConcurrentHashMap<>();

    public MapManager() {
        loadDefaults();
    }

    private void loadDefaults() {
        // Cargar desde JSON en vez de hardcodear
        register(MapLoader.loadFromJson("/maps/map_01.json"));
        register(MapLoader.loadFromJson("/maps/map_02.json"));
        register(MapLoader.loadFromJson("/maps/map_03.json"));
    }

    private void register(GameMap map) {
        maps.put(map.mapId(), map);
    }

    public GameMap getMap(String mapId) {
        return maps.get(mapId);
    }

    public GameMap getDefaultMap() {
        return maps.get("map_01");
    }

    public Map<String, GameMap> getAllMaps() {
        return Map.copyOf(maps);
    }
}