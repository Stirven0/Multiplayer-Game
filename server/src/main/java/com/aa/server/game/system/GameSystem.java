package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.shared.state.GameState;

import java.util.List;

public interface GameSystem {
    void update(GameState state, float deltaTime, List<PlayerInput> inputs);
}
