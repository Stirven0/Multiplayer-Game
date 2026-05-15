package com.aa.client.render;

import com.aa.client.util.ClientConfig;
import com.aa.shared.model.Vector2;

public class Camera {
    private double x, y;
    private final double viewW, viewH;
    private double mapWidth = Double.MAX_VALUE;
    private double mapHeight = Double.MAX_VALUE;

    public Camera(double viewW, double viewH) {
        this.viewW = viewW;
        this.viewH = viewH;
    }

    public void setBounds(double mapW, double mapH) {
        this.mapWidth = mapW;
        this.mapHeight = mapH;
    }

    public void follow(Vector2 target) {
        double targetX = target.x() - viewW / 2;
        double targetY = target.y() - viewH / 2;
        targetX = clamp(targetX, 0, mapWidth - viewW);
        targetY = clamp(targetY, 0, mapHeight - viewH);
        this.x += (targetX - this.x) * ClientConfig.CAMERA_SMOOTH;
        this.y += (targetY - this.y) * ClientConfig.CAMERA_SMOOTH;
    }

    public double worldToScreenX(double wx) { return wx - x; }
    public double worldToScreenY(double wy) { return wy - y; }

    public double getX() { return x; }
    public double getY() { return y; }

    private static double clamp(double val, double min, double max) {
        if (min >= max) return min;
        return Math.max(min, Math.min(max, val));
    }
}