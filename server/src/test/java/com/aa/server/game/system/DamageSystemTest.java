package com.aa.server.game.system;

import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DamageSystemTest {

    private DamageSystem system;
    private GameState state;

    @BeforeEach
    void setUp() {
        system = new DamageSystem();
        state = new GameState("game-1", "map_01");
    }

    @Test
    @DisplayName("Debe marcar jugador como no vivo cuando HP llega a cero")
    void playerDiesAtZeroHealth() {
        Player player = new Player("p1", "Player1", new Vector2(0, 0));
        player.setHealth(25);
        player.takeDamage(25); // HP = 0
        state.addPlayer(player);

        assertTrue(player.isAlive()); // Aún vivo antes del system

        system.update(state, 0.05f, Collections.emptyList(), null);

        assertFalse(player.isAlive());
    }

    @Test
    @DisplayName("Debe mantener jugador vivo si HP > 0")
    void playerStaysAliveWithHealth() {
        Player player = new Player("p1", "Player1", new Vector2(0, 0));
        player.setHealth(50);
        state.addPlayer(player);

        system.update(state, 0.05f, Collections.emptyList(), null);

        assertTrue(player.isAlive());
    }
}
