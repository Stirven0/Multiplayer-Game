package com.aa.server.game.map;

import com.aa.shared.model.Vector2;
import java.util.List;

public class GameMap {
    private final String mapId;
    private final double width;
    private final double height;
    private final List<Obstacle> obstacles;

    public GameMap(String mapId, double width, double height, List<Obstacle> obstacles) {
        this.mapId = mapId;
        this.width = width;
        this.height = height;
        this.obstacles = List.copyOf(obstacles);
    }

    public boolean isInsideBounds(Vector2 pos) {
        return pos.x() >= 0 && pos.x() <= width && pos.y() >= 0 && pos.y() <= height;
    }

    public boolean collides(Vector2 pos, double radius) {
        for (Obstacle o : obstacles) {
            if (o.intersectsCircle(pos, radius)) return true;
        }
        return false;
    }

    public String mapId() { return mapId; }
    public double width() { return width; }
    public double height() { return height; }
    public List<Obstacle> obstacles() { return obstacles; }
}
