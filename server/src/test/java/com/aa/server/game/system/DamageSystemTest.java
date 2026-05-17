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
        player.setHealth(50);
        state.addPlayer(player);

        assertTrue(player.isAlive());

        system.update(state, 0.05f, java.util.List.of(), null);

        assertEquals(50, player.getHealth());
        assertTrue(player.isAlive());

        player.takeDamage(25);
        assertTrue(player.isAlive());
        assertEquals(25, player.getHealth());

        system.update(state, 0.05f, java.util.List.of(), null);

        assertEquals(25, player.getHealth());
        assertTrue(player.isAlive());

        player.takeDamage(25);

        system.update(state, 0.05f, java.util.List.of(), null);

        assertFalse(player.isAlive());
        assertEquals(0, player.getHealth());
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
