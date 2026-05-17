package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.server.game.map.GameMap;
import com.aa.shared.model.Player;
import com.aa.shared.state.GameState;

import java.util.List;

public class UpgradeSystem implements GameSystem {

    public static final int[] UPGRADE_THRESHOLDS = {2, 5, 9, 14, 20};
    public static final double[] DAMAGE_BONUS = {0.05, 0.10, 0.15, 0.20, 0.30};
    public static final double[] FIRE_RATE_BONUS = {0.05, 0.10, 0.15, 0.20, 0.30};
    public static final double[] SPEED_BONUS = {0.05, 0.10, 0.15, 0.20, 0.25};
    public static final double[] MAX_HEALTH_BONUS = {5, 10, 15, 20, 30};
    public static final double[] DAMAGE_REDUCTION = {0.05, 0.10, 0.15, 0.20, 0.25};

    @Override
    public void update(GameState state, float deltaTime, List<PlayerInput> inputs, GameMap map) {
        for (Player player : state.getAllPlayers()) {
            int kills = player.getKills();
            int currentPoints = player.getUpgradePoints();

            int earned = calculatePoints(kills);
            if (earned > currentPoints) {
                player.setUpgradePoints(earned);
            }
        }
    }

    private int calculatePoints(int kills) {
        int points = 0;
        for (int threshold : UPGRADE_THRESHOLDS) {
            if (kills >= threshold) points++;
        }
        return points;
    }

    public int getUpgradeLevel(int points) {
        return Math.min(points, UPGRADE_THRESHOLDS.length);
    }

    public double getDamageMultiplier(Player player) {
        int level = getUpgradeLevel(player.getUpgradePoints());
        if (level <= 0) return 1.0;
        return 1.0 + DAMAGE_BONUS[level - 1];
    }

    public double getFireRateMultiplier(Player player) {
        int level = getUpgradeLevel(player.getUpgradePoints());
        if (level <= 0) return 1.0;
        return 1.0 - FIRE_RATE_BONUS[level - 1];
    }

    public double getSpeedMultiplier(Player player) {
        int level = getUpgradeLevel(player.getUpgradePoints());
        if (level <= 0) return 1.0;
        return 1.0 + SPEED_BONUS[level - 1];
    }

    public double getMaxHealthBonus(Player player) {
        int level = getUpgradeLevel(player.getUpgradePoints());
        if (level <= 0) return 0;
        return MAX_HEALTH_BONUS[level - 1];
    }

    public double getDamageReduction(Player player) {
        int level = getUpgradeLevel(player.getUpgradePoints());
        if (level <= 0) return 0;
        return DAMAGE_REDUCTION[level - 1];
    }
}
