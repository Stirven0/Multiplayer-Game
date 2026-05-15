package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.server.game.map.GameMap;
import com.aa.shared.model.Player;
import com.aa.shared.state.GameState;

import java.util.List;

public class DamageSystem implements GameSystem {

    @Override
    public void update(GameState state, float deltaTime, List<PlayerInput> inputs, GameMap map) {
        for (Player player : state.getAllPlayers()) {
            if (player.getHealth() <= 0 && player.isAlive()) {
                player.setAlive(false);
                // Aquí se puede emitir evento de kill/death para scoreboard
            }
        }
    }
}
