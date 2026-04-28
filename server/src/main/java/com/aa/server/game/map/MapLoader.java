package com.aa.server.game.map;

import com.aa.shared.model.Vector2;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapLoader {
    
    public static GameMap loadFromJson(String resourcePath) {
        try (InputStreamReader reader = new InputStreamReader(
                MapLoader.class.getResourceAsStream(resourcePath))) {
            
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            
            String id = json.get("id").getAsString();
            double width = json.get("width").getAsDouble();
            double height = json.get("height").getAsDouble();
            
            List<Obstacle> obstacles = new ArrayList<>();
            JsonArray obsArray = json.getAsJsonArray("obstacles");
            for (var elem : obsArray) {
                JsonObject o = elem.getAsJsonObject();
                obstacles.add(new Obstacle(
                    o.get("x").getAsDouble(),
                    o.get("y").getAsDouble(),
                    o.get("w").getAsDouble(),
                    o.get("h").getAsDouble()
                ));
            }
            
            return new GameMap(id, width, height, obstacles);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load map: " + resourcePath, e);
        }
    }
}