package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.server.util.ServerConfig;
import com.aa.shared.message.MessageType;
import com.aa.shared.message.ShootMessage;
import com.aa.shared.model.Bullet;
import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShootingSystemTest {

    private ShootingSystem system;
    private GameState state;
    private Player player;

    @BeforeEach
    void setUp() {
        system = new ShootingSystem();
        state = new GameState("game-1", "map_01");
        player = new Player("p1", "Player1", new Vector2(100, 100));
        state.addPlayer(player);
    }

    @Test
    @DisplayName("Debe crear bala al disparar")
    void shootCreatesBullet() {
        ShootMessage msg = new ShootMessage(0.0); // derecha
        system.update(state, 0.05f, List.of(new PlayerInput("p1", MessageType.SHOOT_INPUT, msg)));

        List<Bullet> bullets = state.getAllBullets();
        assertEquals(1, bullets.size());
        
        Bullet b = bullets.get(0);
        assertEquals("p1", b.getOwnerId());
        assertEquals(ServerConfig.BULLET_SPEED, b.getSpeed(), 0.01);
    }

    @Test
    @DisplayName("Debe respetar rate limiting y bloquear disparos rápidos")
    void rateLimitPreventsSpam() {
        ShootMessage msg = new ShootMessage(0.0);
        PlayerInput input = new PlayerInput("p1", MessageType.SHOOT_INPUT, msg);

        system.update(state, 0.05f, List.of(input));
        system.update(state, 0.05f, List.of(input)); // Inmediatamente después

        assertEquals(1, state.getAllBullets().size());
    }

    @Test
    @DisplayName("No debe disparar jugador muerto")
    void deadPlayerCannotShoot() {
        player.setHealth(0);
        ShootMessage msg = new ShootMessage(0.0);
        system.update(state, 0.05f, List.of(new PlayerInput("p1", MessageType.SHOOT_INPUT, msg)));

        assertTrue(state.getAllBullets().isEmpty());
    }

    @Test
    @DisplayName("Debe spawnear bala con offset desde el jugador")
    void bulletSpawnOffsetFromPlayer() {
        ShootMessage msg = new ShootMessage(Math.PI); // izquierda
        system.update(state, 0.05f, List.of(new PlayerInput("p1", MessageType.SHOOT_INPUT, msg)));

        Bullet b = state.getAllBullets().get(0);
        assertTrue(b.getPosition().x() < 100); // Offset hacia la izquierda
    }
}
