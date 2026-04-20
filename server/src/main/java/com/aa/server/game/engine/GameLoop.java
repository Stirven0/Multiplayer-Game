package com.aa.server.game.engine;

import com.aa.server.game.GameInstance;
import com.aa.server.util.ServerConfig;

/**
 * Hilo dedicado por partida. Fixed timestep con drift correction básica.
 */
public class GameLoop implements Runnable {
    private final GameInstance instance;
    private volatile boolean running = false;
    private Thread thread;

    public GameLoop(GameInstance instance) {
        this.instance = instance;
    }
    
    public boolean isRunning() { return running; }

    public void start() {
        running = true;
        thread = new Thread(this);
        thread.setName("GameLoop-" + instance.hashCode());
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    @Override
    public void run() {
        long tickDuration = ServerConfig.TICK_DURATION_MS;
        long nextTick = System.currentTimeMillis();

        while (running) {
            long now = System.currentTimeMillis();

            if (now >= nextTick) {
                // Procesa lógica
                instance.processTick(ServerConfig.TICK_DURATION_SECONDS);
                instance.broadcastState();

                nextTick += tickDuration;

                // Si vamos muy atrasados, resetear para evitar death spiral
                if (nextTick < now) {
                    nextTick = now + tickDuration;
                }
            } else {
                try {
                    Thread.sleep(Math.max(1, nextTick - now - 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
