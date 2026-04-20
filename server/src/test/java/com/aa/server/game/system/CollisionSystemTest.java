package com.aa.server.game.system;

import com.aa.shared.model.Bullet;
import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CollisionSystemTest {

    private CollisionSystem system;
    private GameState state;
    private Player shooter;
    private Player target;

    @BeforeEach
    void setUp() {
        system = new CollisionSystem();
        state = new GameState("game-1", "map_01");
        
        shooter = new Player("p1", "Shooter", new Vector2(0, 0));
        target = new Player("p2", "Target", new Vector2(100, 0));
        target.setHealth(100);
        
        state.addPlayer(shooter);
        state.addPlayer(target);
    }

    @Test
    @DisplayName("Debe detectar colisión bala-jugador y aplicar daño")
    void bulletHitsPlayer() {
        // Bala moviéndose hacia (100,0)
        Bullet bullet = new Bullet("b1", new Vector2(90, 0), new Vector2(1, 0), 500, "p1", 25);
        state.addBullet(bullet);

        system.update(state, 0.05f, Collections.emptyList());

        assertTrue(state.getAllBullets().isEmpty()); // Bala destruida
        assertEquals(75.0, target.getHealth(), 0.01); // 100 - 25
    }

    @Test
    @DisplayName("No debe colisionar bala con su dueño")
    void bulletIgnoresOwner() {
        Bullet bullet = new Bullet("b1", new Vector2(5, 0), new Vector2(1, 0), 500, "p1", 25);
        state.addBullet(bullet);

        system.update(state, 0.05f, Collections.emptyList());

        assertEquals(1, state.getAllBullets().size()); // Sigue viva
        assertEquals(100, shooter.getHealth(), 0.01);
    }

    @Test
    @DisplayName("Debe eliminar balas expiradas")
    void expiredBulletRemoved() {
        Bullet bullet = new Bullet("b1", new Vector2(500, 500), new Vector2(1, 0), 100, "p1", 25);
        bullet.setSpawnTime(System.currentTimeMillis() - 10_000); // Expirada
        state.addBullet(bullet);

        system.update(state, 0.05f, Collections.emptyList());

        assertTrue(state.getAllBullets().isEmpty());
    }

    @Test
    @DisplayName("Debe mover bala según su velocidad")
    void bulletMovesCorrectly() {
        Bullet bullet = new Bullet("b1", new Vector2(0, 0), new Vector2(1, 0), 100, "p1", 25);
        state.addBullet(bullet);

        system.update(state, 1.0f, Collections.emptyList());

        assertEquals(100.0, bullet.getPosition().x(), 0.01);
    }
}
