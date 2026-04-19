package com.aa.server.util;

/**
 * Limitador de tasa simple por jugador (thread-safe).
 */
public class RateLimiter {
    private final long intervalMs;
    private long lastAction = 0;

    public RateLimiter(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if (now - lastAction >= intervalMs) {
            lastAction = now;
            return true;
        }
        return false;
    }
}
