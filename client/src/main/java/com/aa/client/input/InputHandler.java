package com.aa.client.input;

import com.aa.client.render.Camera;
import com.aa.shared.message.MoveMessage;
import com.aa.shared.model.Vector2;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona la entrada del teclado y mouse.
 * Lee teclas pulsadas y posición del mouse para generar mensajes de movimiento
 * y calcular ángulos de disparo.
 */
public class InputHandler {
    private final Set<KeyCode> keys = ConcurrentHashMap.newKeySet();
    private double mouseScreenX;
    private double mouseScreenY;
    private volatile boolean mousePressed;

    /**
     * Vincula los manejadores de eventos a la escena.
     * @param scene escena de JavaFX a la que adjuntar los eventos
     */
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

    /**
     * @return true si hay al menos una tecla de movimiento pulsada (WASD/flechas)
     */
    public boolean isMoving() {
        return keys.contains(KeyCode.W) || keys.contains(KeyCode.A)
            || keys.contains(KeyCode.S) || keys.contains(KeyCode.D)
            || keys.contains(KeyCode.UP) || keys.contains(KeyCode.LEFT)
            || keys.contains(KeyCode.DOWN) || keys.contains(KeyCode.RIGHT);
    }

    /**
     * Construye un mensaje de movimiento con la dirección normalizada
     * y el estado de sprint.
     * @return MoveMessage con dirección y sprint
     */
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

    /**
     * @return true si el botón primario del mouse está presionado
     */
    public boolean isShooting() {
        return mousePressed;
    }

    /** Resetea el estado de disparo. */
    public void clearShoot() {
        mousePressed = false;
    }

    /** @return coordenada X del mouse en pantalla */
    public double getMouseScreenX() { return mouseScreenX; }

    /** @return coordenada Y del mouse en pantalla */
    public double getMouseScreenY() { return mouseScreenY; }

    /**
     * Calcula el ángulo de disparo desde el jugador local hacia la posición del mouse.
     * @param camera cámara para convertir coordenadas del jugador a pantalla
     * @param playerWorldPos posición del jugador en coordenadas de mundo
     * @return ángulo en radianes
     */
    public double getShootAngle(Camera camera, Vector2 playerWorldPos) {
        double screenPlayerX = camera.worldToScreenX(playerWorldPos.x());
        double screenPlayerY = camera.worldToScreenY(playerWorldPos.y());
        return Math.atan2(mouseScreenY - screenPlayerY, mouseScreenX - screenPlayerX);
    }
}