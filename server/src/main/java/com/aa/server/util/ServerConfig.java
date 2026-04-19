package com.aa.server.util;

/**
 * Constantes centralizadas del servidor. Autoridad única de balanceo.
 */
public final class ServerConfig {
    private ServerConfig() {}

    public static final int TICK_RATE = 20;
    public static final float TICK_DURATION_SECONDS = 1.0f / TICK_RATE;
    public static final long TICK_DURATION_MS = 1000L / TICK_RATE;

    public static final double PLAYER_SPEED = 200.0;
    public static final double PLAYER_SPRINT_SPEED = 300.0;
    public static final double BULLET_SPEED = 600.0;
    public static final double FIRE_RATE_MS = 250.0;
    public static final double BULLET_DAMAGE = 25.0;
    public static final double BULLET_LIFETIME_MS = 5000.0;

    public static final int MAX_PLAYERS_PER_ROOM = 10;
    public static final int MIN_PLAYERS_TO_START = 2;
}
