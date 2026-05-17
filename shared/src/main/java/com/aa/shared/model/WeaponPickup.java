package com.aa.shared.model;

public class WeaponPickup {
    private String id;
    private Vector2 position;
    private WeaponType weaponType;

    public WeaponPickup() {}

    public WeaponPickup(String id, Vector2 position, WeaponType weaponType) {
        this.id = id;
        this.position = position;
        this.weaponType = weaponType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Vector2 getPosition() { return position; }
    public void setPosition(Vector2 position) { this.position = position; }

    public WeaponType getWeaponType() { return weaponType; }
    public void setWeaponType(WeaponType weaponType) { this.weaponType = weaponType; }
}
