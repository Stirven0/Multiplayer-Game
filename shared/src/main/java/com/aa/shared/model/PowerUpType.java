package com.aa.shared.model;

public enum PowerUpType {
    SPEED("Speed Boost"),
    DAMAGE_BOOST("Daño+" ),
    FIRE_RATE("Cadencia+"),
    SHIELD("Escudo"),
    HEALTH_PACK("Vida"),
    SLOW("Lentitud"),
    WEAKNESS("Debilidad");

    private final String displayName;

    PowerUpType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
    public boolean isDebuff() {
        return this == SLOW || this == WEAKNESS;
    }
}
