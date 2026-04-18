package com.game.shared.model;

/**
 * Representa una bala en el juego.
 * Mutable: la posición cambia cada tick.
 */
public class Bullet {
    private String id;
    private Vector2 position;
    private Vector2 direction;
    private double speed;
    private String ownerId; // ID del jugador que disparó
    private double damage;
    private long spawnTime; // Timestamp de creación (ms)
    private long maxLifetime; // Tiempo máximo de vida (ms)
    
    public Bullet() {
        this.spawnTime = System.currentTimeMillis();
        this.maxLifetime = 5000; // 5 segundos por defecto
    }
    
    public Bullet(String id, Vector2 position, Vector2 direction, 
                  double speed, String ownerId, double damage) {
        this();
        this.id = id;
        this.position = position;
        this.direction = direction.normalize();
        this.speed = speed;
        this.ownerId = ownerId;
        this.damage = damage;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Vector2 getPosition() { return position; }
    public void setPosition(Vector2 position) { this.position = position; }
    
    public Vector2 getDirection() { return direction; }
    public void setDirection(Vector2 direction) { this.direction = direction.normalize(); }
    
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }
    
    public long getSpawnTime() { return spawnTime; }
    public void setSpawnTime(long spawnTime) { this.spawnTime = spawnTime; }
    
    public long getMaxLifetime() { return maxLifetime; }
    public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }
    
    /**
     * Calcula la nueva posición después de deltaTime segundos.
     * No muta el estado, retorna nueva posición.
     */
    public Vector2 calculateNextPosition(double deltaTime) {
        Vector2 velocity = direction.multiply(speed * deltaTime);
        return position.add(velocity);
    }
    
    /**
     * Mueve la bala según su velocidad y deltaTime.
     */
    public void move(double deltaTime) {
        this.position = calculateNextPosition(deltaTime);
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > maxLifetime;
    }
    
    @Override
    public String toString() {
        return String.format("Bullet[%s by %s @ (%.1f, %.1f)]", 
            id, ownerId, position.x(), position.y());
    }
}
