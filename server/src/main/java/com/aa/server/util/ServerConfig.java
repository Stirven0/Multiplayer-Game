package com.aa.server.util;

/**
 * Constantes centralizadas del servidor.
 * Contiene parámetros de balanceo del juego, red y detección de inactividad.
 */
public final class ServerConfig {
    private ServerConfig() {}

    /** Ticks del bucle de juego por segundo (30 Hz). */
    public static final int TICK_RATE = 30;
    /** Duración de cada tick en segundos. */
    public static final float TICK_DURATION_SECONDS = 1.0f / TICK_RATE;
    /** Duración de cada tick en milisegundos. */
    public static final long TICK_DURATION_MS = 1000L / TICK_RATE;

    /** Velocidad de movimiento normal del jugador (px/s). */
    public static final double PLAYER_SPEED = 200.0;
    /** Velocidad de movimiento al correr (px/s). */
    public static final double PLAYER_SPRINT_SPEED = 300.0;
    /** Velocidad de las balas (px/s). */
    public static final double BULLET_SPEED = 600.0;
    /** Intervalo mínimo entre disparos (ms). */
    public static final double FIRE_RATE_MS = 250.0;
    /** Daño por impacto de bala. */
    public static final double BULLET_DAMAGE = 25.0;
    /** Tiempo de vida máximo de una bala (ms). */
    public static final double BULLET_LIFETIME_MS = 5000.0;

    /** Radio de colisión del jugador. */
    public static final double PLAYER_RADIUS = 15.0;
    /** Radio de colisión de la bala. */
    public static final double BULLET_RADIUS = 3.0;

    /** Máximo de jugadores por sala. */
    public static final int MAX_PLAYERS_PER_ROOM = 10;
    /** Mínimo de jugadores para iniciar partida. */
    public static final int MIN_PLAYERS_TO_START = 2;

    /** Timeout de conexión en ms (0 = desactivado, usa idle de juego). */
    public static final long CONNECTION_TIMEOUT_MS = 0;
    /** Intervalo para verificar timeouts de conexión (ms). */
    public static final long TIMEOUT_CHECK_INTERVAL_MS = 10_000;

    /** Tiempo sin input antes de mostrar advertencia de inactividad (ms). */
    public static final long IDLE_THRESHOLD_MS = 30_000;
    /** Duración de la cuenta regresiva antes de expulsar por inactividad (ms). */
    public static final long IDLE_WARNING_DURATION_MS = 10_000;
    /** Duración de la cuenta regresiva de inactividad en segundos. */
    public static final int IDLE_WARNING_DURATION_SECONDS = 10;
}
