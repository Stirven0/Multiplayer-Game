package com.aa.client.render;

import com.aa.client.util.ClientConfig;
import com.aa.shared.model.Vector2;

public class Camera {
    private double x, y;
    private final double viewW, viewH;

    public Camera(double viewW, double viewH) {
        this.viewW = viewW;
        this.viewH = viewH;
    }

    public void follow(Vector2 target) {
        double targetX = target.x() - viewW / 2;
        double targetY = target.y() - viewH / 2;
        this.x += (targetX - this.x) * ClientConfig.CAMERA_SMOOTH;
        this.y += (targetY - this.y) * ClientConfig.CAMERA_SMOOTH;
    }

    public double worldToScreenX(double wx) { return wx - x; }
    public double worldToScreenY(double wy) { return wy - y; }

    public double getX() { return x; }
    public double getY() { return y; }
}