package com.aa.shared.model;

public class PowerUpPickup {
    private String id;
    private Vector2 position;
    private PowerUpType type;
    private long spawnTime;

    public PowerUpPickup() {
        this.spawnTime = System.currentTimeMillis();
    }

    public PowerUpPickup(String id, Vector2 position, PowerUpType type) {
        this();
        this.id = id;
        this.position = position;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Vector2 getPosition() { return position; }
    public void setPosition(Vector2 position) { this.position = position; }

    public PowerUpType getType() { return type; }
    public void setType(PowerUpType type) { this.type = type; }

    public long getSpawnTime() { return spawnTime; }
    public void setSpawnTime(long spawnTime) { this.spawnTime = spawnTime; }
}
