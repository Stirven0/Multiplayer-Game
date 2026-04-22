package com.aa.client.game;

import com.aa.shared.state.GameState;
import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Estado thread-safe. Escrito desde hilo de red, leído desde JavaFX thread.
 */
public class GameClientState {
    private final AtomicReference<GameState> currentState = new AtomicReference<>();
    private volatile String localPlayerId;
    private volatile boolean inGame = false;

    public void updateState(GameState state) {
        currentState.set(state);
    }

    public GameState getCurrentState() {
        return currentState.get();
    }

    public String getLocalPlayerId() {
        return localPlayerId;
    }

    public void setLocalPlayerId(String id) {
        this.localPlayerId = id;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    /** Helper para ejecutar en FX thread desde el network callback. */
    public static void runLater(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}