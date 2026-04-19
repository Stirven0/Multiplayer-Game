package com.aa.server.game.map;

import com.aa.shared.model.Vector2;

public record Obstacle(double x, double y, double width, double height) {
    public boolean contains(Vector2 point) {
        return point.x() >= x && point.x() <= x + width &&
               point.y() >= y && point.y() <= y + height;
    }

    public boolean intersectsCircle(Vector2 center, double radius) {
        // AABB vs Circle simplificado
        double closestX = clamp(center.x(), x, x + width);
        double closestY = clamp(center.y(), y, y + height);
        double dx = center.x() - closestX;
        double dy = center.y() - closestY;
        return (dx * dx + dy * dy) < (radius * radius);
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
