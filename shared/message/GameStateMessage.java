package com.game.shared.message;

import com.game.shared.state.GameState;

/**
 * Mensaje que contiene el estado completo del juego.
 * Enviado por el servidor periódicamente (snapshots).
 */
public class GameStateMessage extends Message {
    private GameState gameState;
    private int sequenceNumber; // Para detectar desorden/duplicados
    private boolean isDelta; // false = full state, true = partial (futuro)
    
    public GameStateMessage() {
        super(MessageType.GAME_STATE);
        this.isDelta = false;
    }
    
    public GameStateMessage(GameState gameState, int sequenceNumber) {
        this();
        this.gameState = gameState;
        this.sequenceNumber = sequenceNumber;
    }
    
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    
    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    
    public boolean isDelta() { return isDelta; }
    public void setDelta(boolean delta) { isDelta = delta; }
}
