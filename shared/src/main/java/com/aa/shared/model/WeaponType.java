package com.aa.shared.model;

public enum WeaponType {
    PISTOL(   "Pistola",     25, 250, 600, 5000,  0.0, 1, 0.6f),
    SHOTGUN(  "Escopeta",    15, 800, 400, 2000, 15.0, 3, 0.3f),
    RIFLE(    "Rifle",       20, 150, 800, 6000,  1.0, 1, 0.4f),
    SNIPER(   "Francotirador",75,1500,1200,10000, 0.0, 1, 0.1f),
    SMG(      "Subfusil",    12, 100, 500, 3000,  4.0, 1, 0.7f);

    private final String displayName;
    private final double damage;
    private final double fireRateMs;
    private final double bulletSpeed;
    private final double bulletLifetimeMs;
    private final double spreadDeg;
    private final int pellets;
    private final float spawnProbability;

    WeaponType(String displayName, double damage, double fireRateMs,
               double bulletSpeed, double bulletLifetimeMs,
               double spreadDeg, int pellets, float spawnProbability) {
        this.displayName = displayName;
        this.damage = damage;
        this.fireRateMs = fireRateMs;
        this.bulletSpeed = bulletSpeed;
        this.bulletLifetimeMs = bulletLifetimeMs;
        this.spreadDeg = spreadDeg;
        this.pellets = pellets;
        this.spawnProbability = spawnProbability;
    }

    public String getDisplayName() { return displayName; }
    public double getDamage() { return damage; }
    public double getFireRateMs() { return fireRateMs; }
    public double getBulletSpeed() { return bulletSpeed; }
    public double getBulletLifetimeMs() { return bulletLifetimeMs; }
    public double getSpreadDeg() { return spreadDeg; }
    public int getPellets() { return pellets; }
    public float getSpawnProbability() { return spawnProbability; }

    public static WeaponType randomSpawnable() {
        double total = 0;
        for (WeaponType wt : values()) {
            if (wt != PISTOL) total += wt.spawnProbability;
        }
        double r = Math.random() * total;
        double acc = 0;
        for (WeaponType wt : values()) {
            if (wt == PISTOL) continue;
            acc += wt.spawnProbability;
            if (r <= acc) return wt;
        }
        return SHOTGUN;
    }
}
