package com.aa.client.asset;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor de sprites del cliente.
 * Proporciona métodos estáticos para obtener imágenes de jugadores,
 * balas, efectos y elementos de interfaz, con caché para evitar recargas.
 */
public class SpriteManager {
    private static final Map<String, Image> cache = new HashMap<>();

    /**
     * Obtiene el sprite del jugador según si es local y si está vivo.
     * @param isLocal true si es el jugador del cliente local
     * @param isAlive true si el jugador está vivo
     * @return imagen del sprite, o null si no se encuentra
     */
    public static Image getPlayerSprite(boolean isLocal, boolean isAlive) {
        if (!isAlive) return load("player/player_dead.png");
        return load(isLocal ? "player/player_blue.png" : "player/player_red.png");
    }

    /**
     * Obtiene el sprite de una bala normal.
     * @return imagen del sprite de la bala, o null si no se encuentra
     */
    public static Image getBulletSprite() {
        return load("bullet/bullet_normal.png");
    }

    /**
     * Obtiene el sprite del destello de disparo.
     * @return imagen del destello, o null si no se encuentra
     */
    public static Image getMuzzleFlash() {
        return load("effects/muzzle_flash.png");
    }

    /**
     * Obtiene el sprite de la mira o crosshair.
     * @return imagen del crosshair, o null si no se encuentra
     */
    public static Image getCrosshair() {
        return load("ui/crosshair.png");
    }

    /**
     * Carga una imagen desde /sprites/ con caché.
     * @param path ruta relativa del sprite dentro de /sprites/
     * @return imagen cargada, o null si no existe o hay error
     */
    private static Image load(String path) {
        return cache.computeIfAbsent(path, p -> {
            var stream = SpriteManager.class.getResourceAsStream("/sprites/" + p);
            if (stream == null) {
                System.err.println("[SPRITE] Missing: " + p);
                return null;
            }
            try {
                Image img = new Image(stream);
                if (img.isError()) {
                    System.err.println("[SPRITE] Error loading: " + p + " -> " + img.getException());
                    return null;
                }
                return img;
            } catch (Exception e) {
                System.err.println("[SPRITE] Exception loading " + p + ": " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Color de fallback para el jugador cuando no hay sprite.
     * @param isLocal true para color del jugador local, false para oponentes
     * @return color correspondiente
     */
    public static Color getPlayerColor(boolean isLocal) {
        return isLocal ? Color.CORNFLOWERBLUE : Color.CRIMSON;
    }

    /**
     * Color de fallback para las balas cuando no hay sprite.
     * @return color amarillo
     */
    public static Color getBulletColor() {
        return Color.YELLOW;
    }
}