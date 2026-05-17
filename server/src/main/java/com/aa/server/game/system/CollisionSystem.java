package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.server.game.map.GameMap;
import com.aa.server.util.ServerConfig;
import com.aa.shared.model.Bullet;
import com.aa.shared.model.Player;
import com.aa.shared.state.GameState;

import java.util.List;

public class CollisionSystem implements GameSystem {

    @Override
    public void update(GameState state, float deltaTime, List<PlayerInput> inputs, GameMap map) {
        List<Bullet> bullets = state.getAllBullets();

        for (Bullet bullet : bullets) {
            if (bullet.isExpired()) {
                state.removeBullet(bullet.getId());
                continue;
            }

            bullet.move(deltaTime);

            if (map != null && map.collides(bullet.getPosition(), ServerConfig.BULLET_RADIUS)) {
                state.removeBullet(bullet.getId());
                continue;
            }

            for (Player player : state.getAllPlayers()) {
                if (player.getId().equals(bullet.getOwnerId())) continue;
                if (!player.isAlive()) continue;

                double dist = player.getPosition().distanceTo(bullet.getPosition());
                if (dist < ServerConfig.PLAYER_RADIUS + ServerConfig.BULLET_RADIUS) {
                    boolean wasAlive = player.isAlive();
                    player.takeDamage(bullet.getDamage());
                    state.removeBullet(bullet.getId());

                    if (wasAlive && !player.isAlive()) {
                        Player shooter = state.getPlayer(bullet.getOwnerId());
                        if (shooter != null) {
                            shooter.setKills(shooter.getKills() + 1);
                        }
                        player.setDeaths(player.getDeaths() + 1);
                    }
                    break;
                }
            }
        }
    }
}
