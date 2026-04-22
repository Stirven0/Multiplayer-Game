package com.aa.client.asset;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static final Map<String, Image> cache = new HashMap<>();

    public static Image loadImage(String path) {
        return cache.computeIfAbsent(path, p -> {
            try {
                var stream = AssetManager.class.getResourceAsStream("/sprites/" + p);
                return stream != null ? new Image(stream) : null;
            } catch (Exception e) {
                System.err.println("[ASSET] Failed to load: " + p);
                return null;
            }
        });
    }
}