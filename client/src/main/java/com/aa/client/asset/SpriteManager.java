package com.aa.client.asset;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Gestiona sprites y fallback a formas geométricas si no hay imagen.
 */
public class SpriteManager {
    private static final Image PLAYER_SPRITE = AssetManager.loadImage("player.png");

    public static Image getPlayerSprite() {
        return PLAYER_SPRITE;
    }

    public static Color getPlayerColor(boolean isLocal) {
        return isLocal ? Color.CORNFLOWERBLUE : Color.CRIMSON;
    }

    public static Color getBulletColor() {
        return Color.YELLOW;
    }
}