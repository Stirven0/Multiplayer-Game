package com.aa.shared.message;

/**
 * Mensaje de disparo.
 * El ángulo determina la dirección de la bala.
 */
public class ShootMessage extends Message {
    private double angle; // Ángulo en radianes (0 = derecha, π/2 = abajo)
    private String weaponId; // Para futuro: diferentes armas
    
    public ShootMessage() {
        super(MessageType.SHOOT_INPUT);
    }
    
    public ShootMessage(double angle) {
        this();
        this.angle = angle;
    }
    
    public ShootMessage(double angle, String weaponId) {
        this(angle);
        this.weaponId = weaponId;
    }
    
    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
    
    public String getWeaponId() { return weaponId; }
    public void setWeaponId(String weaponId) { this.weaponId = weaponId; }
}
