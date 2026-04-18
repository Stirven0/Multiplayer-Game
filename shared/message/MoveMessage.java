package com.game.shared.message;

/**
 * Mensaje de movimiento del jugador.
 * Enviado por el cliente continuamente (o en cambios de estado).
 */
public class MoveMessage extends Message {
    private double dx; // Delta X (-1 a 1, normalizado)
    private double dy; // Delta Y (-1 a 1, normalizado)
    private boolean sprinting;
    
    public MoveMessage() {
        super(MessageType.MOVE_INPUT);
    }
    
    public MoveMessage(double dx, double dy, boolean sprinting) {
        this();
        this.dx = dx;
        this.dy = dy;
        this.sprinting = sprinting;
    }
    
    // Getters y Setters
    public double getDx() { return dx; }
    public void setDx(double dx) { this.dx = dx; }
    
    public double getDy() { return dy; }
    public void setDy(double dy) { this.dy = dy; }
    
    public boolean isSprinting() { return sprinting; }
    public void setSprinting(boolean sprinting) { this.sprinting = sprinting; }
    
    /**
     * Normaliza el vector de dirección si es necesario.
     */
    public void normalize() {
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        if (magnitude > 1.0) {
            this.dx /= magnitude;
            this.dy /= magnitude;
        }
    }
}
