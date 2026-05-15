package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.server.game.map.GameMap;
import com.aa.server.util.ServerConfig;
import com.aa.shared.message.MessageType;
import com.aa.shared.message.ShootMessage;
import com.aa.shared.model.Bullet;
import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ShootingSystem implements GameSystem {
    private final Map<String, Long> lastShotTime = new HashMap<>();
    private final AtomicInteger bulletCounter = new AtomicInteger(0);

    @Override
    public void update(GameState state, float deltaTime, List<PlayerInput> inputs, GameMap map) {
        long now = System.currentTimeMillis();

        for (PlayerInput input : inputs) {
            if (input.type() != MessageType.SHOOT_INPUT) continue;

            String pid = input.playerId();
            Long last = lastShotTime.get(pid);
            if (last != null && (now - last) < ServerConfig.FIRE_RATE_MS) continue;

            Player player = state.getPlayer(pid);
            if (player == null || !player.isAlive()) continue;

            ShootMessage msg = (ShootMessage) input.message();
            double angle = msg.getAngle();

            Vector2 dir = new Vector2(Math.cos(angle), Math.sin(angle));
            Vector2 spawn = player.getPosition().add(dir.multiply(20)); // Offset desde centro

            String bulletId = pid + "_b_" + bulletCounter.incrementAndGet();
            Bullet bullet = new Bullet(
                bulletId, spawn, dir, ServerConfig.BULLET_SPEED,
                pid, ServerConfig.BULLET_DAMAGE
            );
            bullet.setMaxLifetime((long) ServerConfig.BULLET_LIFETIME_MS);

            state.addBullet(bullet);
            lastShotTime.put(pid, now);
        }
    }
}
