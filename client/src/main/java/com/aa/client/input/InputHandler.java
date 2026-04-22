package com.aa.client.input;

import com.aa.client.render.Camera;
import com.aa.shared.message.MoveMessage;
import com.aa.shared.model.Vector2;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InputHandler {
    private final Set<KeyCode> keys = ConcurrentHashMap.newKeySet();
    private double mouseScreenX;
    private double mouseScreenY;
    private volatile boolean mousePressed;

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> keys.add(e.getCode()));
        scene.setOnKeyReleased(e -> keys.remove(e.getCode()));
        scene.setOnMouseMoved(e -> {
            mouseScreenX = e.getX();
            mouseScreenY = e.getY();
        });
        scene.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) mousePressed = true;
        });
        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) mousePressed = false;
        });
    }

    public boolean isMoving() {
        return keys.contains(KeyCode.W) || keys.contains(KeyCode.A)
            || keys.contains(KeyCode.S) || keys.contains(KeyCode.D)
            || keys.contains(KeyCode.UP) || keys.contains(KeyCode.LEFT)
            || keys.contains(KeyCode.DOWN) || keys.contains(KeyCode.RIGHT);
    }

    public MoveMessage getMoveMessage() {
        double dx = 0, dy = 0;
        if (keys.contains(KeyCode.W) || keys.contains(KeyCode.UP)) dy -= 1;
        if (keys.contains(KeyCode.S) || keys.contains(KeyCode.DOWN)) dy += 1;
        if (keys.contains(KeyCode.A) || keys.contains(KeyCode.LEFT)) dx -= 1;
        if (keys.contains(KeyCode.D) || keys.contains(KeyCode.RIGHT)) dx += 1;

        // Normalizar
        double mag = Math.hypot(dx, dy);
        if (mag > 0) {
            dx /= mag;
            dy /= mag;
        }

        boolean sprint = keys.contains(KeyCode.SHIFT);
        return new MoveMessage(dx, dy, sprint);
    }

    public boolean isShooting() {
        return mousePressed;
    }

    public void clearShoot() {
        mousePressed = false;
    }

    /**
     * Calcula ángulo del mouse respecto al jugador local en coordenadas de mundo.
     */
    public double getShootAngle(Camera camera, Vector2 playerWorldPos) {
        double screenPlayerX = camera.worldToScreenX(playerWorldPos.x());
        double screenPlayerY = camera.worldToScreenY(playerWorldPos.y());
        return Math.atan2(mouseScreenY - screenPlayerY, mouseScreenX - screenPlayerX);
    }
}