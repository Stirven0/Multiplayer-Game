package com.aa.client.render;

import com.aa.client.util.ClientConfig;
import com.aa.shared.model.Vector2;

/**
 * Cámara que sigue al jugador local en el mundo del juego.
 * Convierte coordenadas de mundo a coordenadas de pantalla y
 * aplica interpolación suave para el seguimiento.
 */
public class Camera {
    private double x, y;
    private double viewW, viewH;
    private double mapWidth = Double.MAX_VALUE;
    private double mapHeight = Double.MAX_VALUE;

    /**
     * Construye una cámara con el tamaño de viewport especificado.
     * @param viewW ancho del viewport en píxeles
     * @param viewH alto del viewport en píxeles
     */
    public Camera(double viewW, double viewH) {
        this.viewW = viewW;
        this.viewH = viewH;
    }

    /**
     * Actualiza el tamaño del viewport (ej. al redimensionar la ventana).
     * @param w nuevo ancho del viewport
     * @param h nuevo alto del viewport
     */
    public void setViewportSize(double w, double h) {
        this.viewW = w;
        this.viewH = h;
    }

    /**
     * Establece los límites del mapa para restringir el movimiento de la cámara.
     * @param mapW ancho total del mapa en unidades del mundo
     * @param mapH alto total del mapa en unidades del mundo
     */
    public void setBounds(double mapW, double mapH) {
        this.mapWidth = mapW;
        this.mapHeight = mapH;
    }

    /**
     * Hace que la cámara siga a una posición objetivo con interpolación suave.
     * @param target posición del objetivo en coordenadas de mundo
     */
    public void follow(Vector2 target) {
        double targetX = target.x() - viewW / 2;
        double targetY = target.y() - viewH / 2;
        targetX = clamp(targetX, 0, mapWidth - viewW);
        targetY = clamp(targetY, 0, mapHeight - viewH);
        this.x += (targetX - this.x) * ClientConfig.CAMERA_SMOOTH;
        this.y += (targetY - this.y) * ClientConfig.CAMERA_SMOOTH;
    }

    /**
     * Convierte una coordenada X de mundo a coordenada de pantalla.
     * @param wx coordenada X en el mundo
     * @return coordenada X en la pantalla
     */
    public double worldToScreenX(double wx) { return wx - x; }

    /**
     * Convierte una coordenada Y de mundo a coordenada de pantalla.
     * @param wy coordenada Y en el mundo
     * @return coordenada Y en la pantalla
     */
    public double worldToScreenY(double wy) { return wy - y; }

    /**
     * @return posición X actual de la cámara en el mundo
     */
    public double getX() { return x; }

    /**
     * @return posición Y actual de la cámara en el mundo
     */
    public double getY() { return y; }

    private static double clamp(double val, double min, double max) {
        if (min >= max) return min;
        return Math.max(min, Math.min(max, val));
    }
}