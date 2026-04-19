package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.shared.model.Bullet;
import com.aa.shared.model.Player;
import com.aa.shared.state.GameState;

import java.util.List;

public class CollisionSystem implements GameSystem {

    @Override
    public void update(GameState state, float deltaTime, List<PlayerInput> inputs) {
        List<Bullet> bullets = state.getAllBullets();

        for (Bullet bullet : bullets) {
            if (bullet.isExpired()) {
                state.removeBullet(bullet.getId());
                continue;
            }

            // Mover bala
            bullet.move(deltaTime);

            // Colisión con jugadores
            for (Player player : state.getAllPlayers()) {
                if (player.getId().equals(bullet.getOwnerId())) continue;
                if (!player.isAlive()) continue;

                double dist = player.getPosition().distanceTo(bullet.getPosition());
                if (dist < 20.0) { // Radio aproximado de impacto
                    player.takeDamage(bullet.getDamage());
                    state.removeBullet(bullet.getId());
                    break;
                }
            }

            // TODO: colisión con obstáculos del mapa
        }
    }
}
