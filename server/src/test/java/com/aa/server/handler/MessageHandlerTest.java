package com.aa.server.handler;

import com.aa.server.auth.AuthService;
import com.aa.server.game.GameInstance;
import com.aa.server.game.GameInstanceManager;
import com.aa.server.network.ClientConnection;
import com.aa.server.room.Room;
import com.aa.server.room.RoomManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageHandlerTest {

    @Mock
    private AuthService authService;

    @Mock
    private RoomManager roomManager;

    @Mock
    private GameInstanceManager gameInstanceManager;

    @Mock
    private ClientConnection client;

    private MessageHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MessageHandler(authService, roomManager, gameInstanceManager, mock(com.aa.server.network.ConnectionManager.class));
    }

    @Test
    @DisplayName("Debe rechazar mensaje sin campo type")
    void rejectsMissingType() {
        handler.handle(client, "{}");
        verify(client).sendError(eq("MISSING_TYPE"), anyString(), eq(false));
    }

    @Test
    @DisplayName("Debe rechazar mensaje con type inválido")
    void rejectsInvalidType() {
        handler.handle(client, "{\"type\":\"UNKNOWN_TYPE\"}");
        verify(client).sendError(eq("INVALID_TYPE"), anyString(), eq(false));
    }

    @Test
    @DisplayName("LOGIN_REQUEST debe autenticar y retornar token")
    void loginSuccess() {
        when(authService.login("user", "pass")).thenReturn("token-123");
        when(authService.getUserId("token-123")).thenReturn("uid-1");

        String json = "{\"type\":\"LOGIN_REQUEST\",\"username\":\"user\",\"password\":\"pass\"}";
        handler.handle(client, json);

        verify(client).setUserId("uid-1");
        verify(authService).login("user", "pass");
    }

    @Test
    @DisplayName("LOGIN_REQUEST inválido debe enviar error")
    void loginFailure() {
        when(authService.login("user", "wrong")).thenReturn(null);

        String json = "{\"type\":\"LOGIN_REQUEST\",\"username\":\"user\",\"password\":\"wrong\"}";
        handler.handle(client, json);

        verify(client).sendError(eq("AUTH_FAILED"), anyString(), eq(false));
    }

    @Test
    @DisplayName("Debe rechazar mensajes autenticados sin login previo")
    void rejectsUnauthenticated() {
        when(client.isAuthenticated()).thenReturn(false);

        String json = "{\"type\":\"MOVE_INPUT\",\"dx\":1.0,\"dy\":0.0}";
        handler.handle(client, json);

        verify(client).sendError(eq("NOT_AUTHENTICATED"), anyString(), eq(false));
    }

    @Test
    @DisplayName("MOVE_INPUT debe encolar en GameInstance si jugador está en partida")
    void routesMoveInput() {
        when(client.isAuthenticated()).thenReturn(true);
        when(client.getPlayerId()).thenReturn("p1");

        GameInstance game = mock(GameInstance.class);
        when(gameInstanceManager.getGameByPlayer("p1")).thenReturn(game);

        String json = "{\"type\":\"MOVE_INPUT\",\"dx\":1.0,\"dy\":0.0}";
        handler.handle(client, json);

        verify(game).queueInput(any());
    }

    @Test
    @DisplayName("SHOOT_INPUT debe respetar rate limit del cliente")
    void shootRateLimitedByClient() {
        when(client.isAuthenticated()).thenReturn(true);
        when(client.getPlayerId()).thenReturn("p1");
        when(client.tryShoot()).thenReturn(false);

        String json = "{\"type\":\"SHOOT_INPUT\",\"angle\":0.0}";
        handler.handle(client, json);

        verify(client).sendError(eq("RATE_LIMIT"), anyString(), eq(false));
        verify(gameInstanceManager, never()).getGameByPlayer(any());
    }

    @Test
    @DisplayName("CREATE_ROOM debe delegar a RoomManager")
    void createRoomRoutesCorrectly() {
        when(client.isAuthenticated()).thenReturn(true);
        when(client.getPlayerId()).thenReturn("p1");

        Room room = mock(Room.class);
        when(room.getRoomId()).thenReturn("room-1");
        when(roomManager.createRoom("p1", "map_01")).thenReturn(room);

        String json = "{\"type\":\"CREATE_ROOM\",\"mapId\":\"map_01\"}";
        handler.handle(client, json);

        verify(roomManager).createRoom("p1", "map_01");
        verify(client).setCurrentRoomId("room-1");
    }
}
