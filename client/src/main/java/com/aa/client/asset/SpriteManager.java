package com.aa.client.asset;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class SpriteManager {
    private static final Map<String, Image> cache = new HashMap<>();

    // Jugadores
    public static Image getPlayerSprite(boolean isLocal, boolean isAlive) {
        if (!isAlive) return load("player/player_dead.png");
        return load(isLocal ? "player/player_blue.png" : "player/player_red.png");
    }

    // Balas
    public static Image getBulletSprite() {
        return load("bullet/bullet_normal.png");
    }

    // Efectos
    public static Image getMuzzleFlash() {
        return load("effects/muzzle_flash.png");
    }

    // UI
    public static Image getCrosshair() {
        return load("ui/crosshair.png");
    }

    // Helper con cache
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

    // Fallback si no hay imagen
    public static Color getPlayerColor(boolean isLocal) {
        return isLocal ? Color.CORNFLOWERBLUE : Color.CRIMSON;
    }
    
    public static Color getBulletColor() {
        return Color.YELLOW;
    }
}