package com.aa.server.game.engine;

import com.aa.server.game.PlayerInput;
import com.aa.server.game.map.GameMap;
import com.aa.server.game.system.*;
import com.aa.shared.state.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordina la ejecución de systems en orden determinista.
 */
public class GameEngine {
    private final List<GameSystem> systems = new ArrayList<>();

    public GameEngine() {
        systems.add(new MovementSystem());
        systems.add(new ShootingSystem());
        systems.add(new CollisionSystem());
        systems.add(new DamageSystem());
    }

    public void update(GameState state, float deltaTime, List<PlayerInput> inputs, GameMap map) {
        for (GameSystem system : systems) {
            system.update(state, deltaTime, inputs, map);
        }
    }
}
