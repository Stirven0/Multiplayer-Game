package com.aa.server.game.system;

import com.aa.server.game.PlayerInput;
import com.aa.server.game.map.GameMap;
import com.aa.shared.model.Obstacle;
import com.aa.server.util.ServerConfig;
import com.aa.shared.message.MessageType;
import com.aa.shared.message.MoveMessage;
import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovementSystemTest {

    private MovementSystem system;
    private GameState state;
    private Player player;

    @BeforeEach
    void setUp() {
        system = new MovementSystem();
        state = new GameState("game-1", "map_01");
        player = new Player("p1", "Player1", new Vector2(100, 100));
        state.addPlayer(player);
    }

    @Test
    @DisplayName("Debe mover jugador a la derecha con velocidad base")
    void moveRight() {
        MoveMessage msg = new MoveMessage(1.0, 0.0, false);
        system.update(state, 1.0f, List.of(new PlayerInput("p1", MessageType.MOVE_INPUT, msg)), null);

        Vector2 pos = state.getPlayer("p1").getPosition();
        assertEquals(100.0 + ServerConfig.PLAYER_SPEED, pos.x(), 0.01);
        assertEquals(100.0, pos.y(), 0.01);
    }

    @Test
    @DisplayName("Debe normalizar vector diagonal para evitar velocidad mayor")
    void diagonalMovementIsNormalized() {
        MoveMessage msg = new MoveMessage(1.0, 1.0, false);
        system.update(state, 1.0f, List.of(new PlayerInput("p1", MessageType.MOVE_INPUT, msg)), null);

        Vector2 pos = state.getPlayer("p1").getPosition();
        double distance = new Vector2(100, 100).distanceTo(pos);
        assertEquals(ServerConfig.PLAYER_SPEED, distance, 0.01);
    }

    @Test
    @DisplayName("Debe clampar inputs mayores a 1.0")
    void clampsExcessiveInput() {
        MoveMessage msg = new MoveMessage(5.0, 0.0, false);
        system.update(state, 1.0f, List.of(new PlayerInput("p1", MessageType.MOVE_INPUT, msg)), null);

        Vector2 pos = state.getPlayer("p1").getPosition();
        assertEquals(100.0 + ServerConfig.PLAYER_SPEED, pos.x(), 0.01);
    }

    @Test
    @DisplayName("Debe aplicar velocidad de sprint cuando está activo")
    void sprintIncreasesSpeed() {
        MoveMessage msg = new MoveMessage(1.0, 0.0, true);
        system.update(state, 1.0f, List.of(new PlayerInput("p1", MessageType.MOVE_INPUT, msg)), null);

        Vector2 pos = state.getPlayer("p1").getPosition();
        assertEquals(100.0 + ServerConfig.PLAYER_SPRINT_SPEED, pos.x(), 0.01);
    }

    @Test
    @DisplayName("No debe mover jugador muerto")
    void deadPlayerDoesNotMove() {
        player.setHealth(0);
        MoveMessage msg = new MoveMessage(1.0, 0.0, false);
        system.update(state, 1.0f, List.of(new PlayerInput("p1", MessageType.MOVE_INPUT, msg)), null);

        assertEquals(100.0, state.getPlayer("p1").getPosition().x(), 0.01);
    }

    @Test
    @DisplayName("Debe ignorar input con magnitud insignificante")
    void ignoreSmallInput() {
        MoveMessage msg = new MoveMessage(0.005, 0.0, false);
        system.update(state, 1.0f, List.of(new PlayerInput("p1", MessageType.MOVE_INPUT, msg)), null);

        assertEquals(100.0, state.getPlayer("p1").getPosition().x(), 0.01);
    }

    @Test
    @DisplayName("Debe rechazar movimiento si colisiona con obstáculo")
    void cannotMoveIntoObstacle() {
        // Muro vertical que bloquea todo el lado derecho desde x=100
        GameMap map = new GameMap("test", "test", 500, 500, List.of(new Obstacle(100, 0, 400, 500)));
        player = new Player("p1", "Player1", new Vector2(50, 100));
        state = new GameState("game-1", "test");
        state.addPlayer(player);

        MoveMessage msg = new MoveMessage(1.0, 0.0, false);
        system.update(state, 1.0f, List.of(new PlayerInput("p1", MessageType.MOVE_INPUT, msg)), map);

        Vector2 pos = state.getPlayer("p1").getPosition();
        assertEquals(50, pos.x(), 0.01, "Player should be blocked by obstacle");
    }

    @Test
    @DisplayName("Debe clampear posición dentro de los límites del mapa")
    void clampedToMapBounds() {
        GameMap map = new GameMap("test", "test", 200, 200, List.of());
        player = new Player("p1", "Player1", new Vector2(190, 100));
        state = new GameState("game-1", "test");
        state.addPlayer(player);

        // Intentar moverse fuera del límite derecho (map.width=200)
        MoveMessage msg = new MoveMessage(1.0, 0.0, false);
        system.update(state, 1.0f, List.of(new PlayerInput("p1", MessageType.MOVE_INPUT, msg)), map);

        Vector2 pos = state.getPlayer("p1").getPosition();
        assertTrue(pos.x() <= map.width(), "Player X should be clamped to map width");
    }
}
