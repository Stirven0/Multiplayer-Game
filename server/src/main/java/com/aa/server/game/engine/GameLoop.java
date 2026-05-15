package com.aa.server.game.engine;

import com.aa.server.game.GameInstance;
import com.aa.server.network.ClientConnection;
import com.aa.server.network.ConnectionManager;
import com.aa.server.util.ServerConfig;
import com.aa.shared.message.GameEndMessage;
import com.aa.shared.model.Player;

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
                instance.processTick(ServerConfig.TICK_DURATION_SECONDS);

                if (instance.isFinished()) {
                    instance.broadcastState();
                    broadcastGameEnd();
                    instance.stop();
                    Runnable cb = instance.getOnGameEndCallback();
                    if (cb != null) cb.run();
                    return;
                }

                instance.broadcastState();
                nextTick += tickDuration;

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

    private void broadcastGameEnd() {
        GameEndMessage endMsg = instance.createGameEndMessage();
        ConnectionManager cm = instance.getConnectionManager();
        for (Player p : instance.getState().getAllPlayers()) {
            ClientConnection c = cm.getByPlayerId(p.getId());
            if (c != null) c.send(endMsg);
        }
    }
}