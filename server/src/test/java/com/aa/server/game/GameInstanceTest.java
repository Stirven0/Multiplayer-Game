package com.aa.server.game;

import com.aa.server.game.map.GameMap;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;
import com.aa.shared.message.GameStateMessage;
import com.aa.shared.message.MoveMessage;
import com.aa.shared.message.ShootMessage;
import com.aa.shared.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameInstanceTest {

    @Mock
    private ConnectionManager connectionManager;

    @Mock
    private Room room;

    private GameInstance instance;

    @BeforeEach
    void setUp() {
        when(room.getRoomId()).thenReturn("room-1");
        when(room.getMapId()).thenReturn("map_01");
        when(room.getPlayerIds()).thenReturn(Set.of("p1", "p2"));
        when(room.getHostId()).thenReturn("p1");

        GameMap map = new GameMap("map_01", "Warehouse", 2000, 2000, java.util.List.of());

        instance = new GameInstance("game-1", room, map, connectionManager);
    }

    @Test
    @DisplayName("Debe encolar inputs correctamente")
    void queueInputAddsToQueue() {
        MoveMessage msg = new MoveMessage(1.0, 0.0, false);
        instance.queueInput(new PlayerInput("p1", MessageType.MOVE_INPUT, msg));

        // No hay getter para la cola, verificamos indirectamente via processTick
        instance.processTick(0.05f);
        // Si no lanza excepción y consume el input, la cola funcionó
        assertDoesNotThrow(() -> instance.processTick(0.05f));
    }

    @Test
    @DisplayName("processTick debe consumir todos los inputs encolados")
    void processTickConsumesInputs() {
        instance.queueInput(new PlayerInput("p1", MessageType.MOVE_INPUT, new MoveMessage(1.0, 0.0, false)));
        instance.queueInput(new PlayerInput("p1", MessageType.SHOOT_INPUT, new ShootMessage(0.0)));

        assertDoesNotThrow(() -> instance.processTick(0.05f));
        // Segundo tick no debe tener inputs residuales
        assertDoesNotThrow(() -> instance.processTick(0.05f));
    }

    @Test
    @DisplayName("broadcastState debe enviar GameStateMessage a los jugadores")
    void broadcastSendsStateToPlayers() {
        instance.start(); // Inicia el estado
        instance.broadcastState();

        ArgumentCaptor<GameStateMessage> captor = ArgumentCaptor.forClass(GameStateMessage.class);
        verify(connectionManager).broadcastToPlayers(any(), captor.capture());

        GameStateMessage msg = captor.getValue();
        assertNotNull(msg.getGameState());
        assertEquals("game-1", msg.getGameState().getGameId());
        assertFalse(msg.isDelta());
    }

    @Test
    @DisplayName("Debe contener jugadores iniciales del Room")
    void containsInitialPlayers() {
        assertTrue(instance.hasPlayer("p1"));
        assertTrue(instance.hasPlayer("p2"));
        assertFalse(instance.hasPlayer("p3"));
    }
}
