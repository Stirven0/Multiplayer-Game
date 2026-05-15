package com.aa.client.asset;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static final Map<String, AudioClip> sfxCache = new HashMap<>();
    private static MediaPlayer musicPlayer;
    private static double masterVolume = 0.7;
    private static double sfxVolume = 0.8;
    private static double musicVolume = 0.5;

    // ========== SFX (cortos, se superponen) ==========

    public static void playShoot() {
        playSfx("sfx/shoot_pistol.wav");
    }

    public static void playHit() {
        playSfx("sfx/hit_marker.wav");
    }

    public static void playExplosion() {
        playSfx("sfx/explosion.wav");
    }

    public static void playClick() {
        playSfx("ui/click.wav", 0.3);
    }

    private static void playSfx(String path) {
        playSfx(path, sfxVolume);
    }

    private static void playSfx(String path, double vol) {
        try {
            AudioClip clip = sfxCache.computeIfAbsent(path, p -> {
                var url = AudioManager.class.getResource("/audio/" + p);
                if (url == null) return null;
                try {
                    return new AudioClip(url.toExternalForm());
                } catch (Exception e) {
                    System.err.println("[AUDIO] Error loading SFX " + p + ": " + e.getMessage());
                    return null;
                }
            });
            if (clip != null) {
                clip.play(masterVolume * vol);
            }
        } catch (Exception e) {
            System.err.println("[AUDIO] Error playing SFX: " + e.getMessage());
        }
    }

    // ========== MÚSICA (larga, una a la vez) ==========

    public static void playMusic(String path) {
        try {
            stopMusic();
            var url = AudioManager.class.getResource("/audio/" + path);
            if (url == null) return;
            Media media = new Media(url.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setVolume(masterVolume * musicVolume);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.play();
        } catch (Exception e) {
            System.err.println("[AUDIO] Error playing music: " + e.getMessage());
        }
    }

    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
    }

    public static void setMasterVolume(double v) { masterVolume = v; }
    public static void setSfxVolume(double v) { sfxVolume = v; }
    public static void setMusicVolume(double v) { musicVolume = v; }
}