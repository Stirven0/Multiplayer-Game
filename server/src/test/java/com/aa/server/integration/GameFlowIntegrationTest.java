package com.aa.server.integration;

import com.aa.server.auth.AuthService;
import com.aa.server.game.GameInstanceManager;
import com.aa.server.game.map.MapManager;
import com.aa.server.handler.MessageHandler;
import com.aa.server.network.ClientConnection;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;
import com.aa.server.room.RoomManager;
import com.aa.server.room.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameFlowIntegrationTest {

    @Mock
    private ClientConnection host;

    @Mock
    private ClientConnection guest;

    private AuthService authService;
    private RoomManager roomManager;
    private GameInstanceManager gameInstanceManager;
    private ConnectionManager connectionManager;
    private MessageHandler handler;

    @BeforeEach
    void setUp() {
        authService = new AuthService(); // Real
        connectionManager = new ConnectionManager();
        MapManager mapManager = new MapManager(); // Real
        gameInstanceManager = new GameInstanceManager(connectionManager, mapManager);
        roomManager = new RoomManager(gameInstanceManager);
        handler = new MessageHandler(authService, roomManager, gameInstanceManager, connectionManager);

        // Setup mocks básicos
        when(host.getConnectionId()).thenReturn("conn-host");
        when(guest.getConnectionId()).thenReturn("conn-guest");
    }

    @Test
    @DisplayName("Flujo completo: Login → Crear → Unir → Iniciar → Mover")
    void fullGameFlow() throws InterruptedException {
        // 1. Login Host
        handler.handle(host, "{\"type\":\"LOGIN_REQUEST\",\"username\":\"player1\",\"password\":\"pass1\"}");
        verify(host).setUserId(anyString());

        // Simular autenticación manual (como haría ConnectionManager)
        when(host.isAuthenticated()).thenReturn(true);
        when(host.getPlayerId()).thenReturn("player1");

        // 2. Login Guest
        handler.handle(guest, "{\"type\":\"LOGIN_REQUEST\",\"username\":\"player2\",\"password\":\"pass2\"}");
        when(guest.isAuthenticated()).thenReturn(true);
        when(guest.getPlayerId()).thenReturn("player2");

        // 3. Host crea sala
        handler.handle(host, "{\"type\":\"CREATE_ROOM\",\"mapId\":\"map_01\"}");
        ArgumentCaptor<String> roomIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(host).setCurrentRoomId(roomIdCaptor.capture());
        String roomId = roomIdCaptor.getValue();

        // 4. Guest se une
        handler.handle(guest, String.format("{\"type\":\"JOIN_ROOM\",\"roomId\":\"%s\"}", roomId));
        Room room = roomManager.getRoom(roomId);
        assertEquals(2, room.getPlayerCount());

        // 5. Host inicia partida
        handler.handle(host, "{\"type\":\"GAME_START\"}");
        assertEquals(RoomStatus.PLAYING, room.getStatus());

        // Esperar a que GameLoop arranque
        Thread.sleep(100);

        // 6. Host envía movimiento
        handler.handle(host, "{\"type\":\"MOVE_INPUT\",\"dx\":1.0,\"dy\":0.0,\"sprinting\":false}");

        // 7. Host dispara
        handler.handle(host, "{\"type\":\"SHOOT_INPUT\",\"angle\":0.0}");

        // Esperar procesamiento
        Thread.sleep(200);

        // Verificar que GameInstance existe y procesó
        assertNotNull(gameInstanceManager.getGameByPlayer("player1"));
        assertNotNull(gameInstanceManager.getGameByPlayer("player2"));
    }
}
