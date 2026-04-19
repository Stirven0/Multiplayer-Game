package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.server.util.ServerConfig;
import com.aa.shared.message.MessageType;
import com.aa.shared.message.MoveMessage;
import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;

import java.util.List;

/**
 * Autoridad absoluta del movimiento. El servidor calcula posiciones.
 */
public class MovementSystem implements GameSystem {

    @Override
    public void update(GameState state, float deltaTime, List<PlayerInput> inputs) {
        for (PlayerInput input : inputs) {
            if (input.type() != MessageType.MOVE_INPUT) continue;

            MoveMessage msg = (MoveMessage) input.message();
            Player player = state.getPlayer(input.playerId());
            if (player == null || !player.isAlive()) continue;

            // Validar y clampar inputs
            double dx = clamp(msg.getDx(), -1.0, 1.0);
            double dy = clamp(msg.getDy(), -1.0, 1.0);

            double speed = msg.isSprinting() ? ServerConfig.PLAYER_SPRINT_SPEED : ServerConfig.PLAYER_SPEED;
            double dist = speed * deltaTime;

            // Normalizar vector si diagonal
            double mag = Math.sqrt(dx * dx + dy * dy);
            if (mag > 1.0) {
                dx /= mag;
                dy /= mag;
            }

            if (mag < 0.01) continue; // Input muerto

            double newX = player.getPosition().x() + dx * dist;
            double newY = player.getPosition().y() + dy * dist;

            // TODO: validar colisiones con GameMap bounds y obstáculos

            player.setPosition(new Vector2(newX, newY));
            player.setDirection(new Vector2(dx, dy).normalize());
        }
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
