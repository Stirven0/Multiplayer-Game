package com.aa.server.room;

import com.aa.server.game.GameInstanceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomManagerTest {

    @Mock
    private GameInstanceManager gameInstanceManager;

    private RoomManager roomManager;

    @BeforeEach
    void setUp() {
        roomManager = new RoomManager(gameInstanceManager);
    }

    @Test
    @DisplayName("Debe crear sala con host asignado")
    void createRoomAssignsHost() {
        Room room = roomManager.createRoom("host-1", "map_01");

        assertNotNull(room);
        assertEquals("host-1", room.getHostId());
        assertEquals("map_01", room.getMapId());
        assertTrue(room.isHost("host-1"));
        assertEquals(1, room.getPlayerCount());
    }

    @Test
    @DisplayName("Debe permitir unir jugador a sala existente")
    void joinRoomAddsPlayer() {
        Room room = roomManager.createRoom("host-1", "map_01");
        boolean joined = roomManager.joinRoom(room.getRoomId(), "player-2");

        assertTrue(joined);
        assertEquals(2, room.getPlayerCount());
    }

    @Test
    @DisplayName("Debe rechazar unirse a sala inexistente")
    void joinNonExistentRoomFails() {
        assertFalse(roomManager.joinRoom("fake-room", "player-1"));
    }

    @Test
    @DisplayName("Debe rechazar unirse si sala está llena (10 jugadores)")
    void joinFullRoomFails() {
        Room room = roomManager.createRoom("host-1", "map_01");

        for (int i = 2; i <= 10; i++) {
            assertTrue(roomManager.joinRoom(room.getRoomId(), "player-" + i));
        }

        assertEquals(10, room.getPlayerCount());
        assertFalse(roomManager.joinRoom(room.getRoomId(), "player-11"));
    }

    @Test
    @DisplayName("Debe iniciar partida si hay mínimo 2 jugadores")
    void startGameWithMinPlayers() {
        Room room = roomManager.createRoom("host-1", "map_01");
        roomManager.joinRoom(room.getRoomId(), "player-2");

        roomManager.startGame(room.getRoomId());

        assertEquals(RoomStatus.PLAYING, room.getStatus());
        verify(gameInstanceManager).createAndStart(room);
    }

    @Test
    @DisplayName("No debe iniciar partida con menos de 2 jugadores")
    void startGameWithOnePlayerDoesNothing() {
        Room room = roomManager.createRoom("host-1", "map_01");
        roomManager.startGame(room.getRoomId());

        assertNotEquals(RoomStatus.PLAYING, room.getStatus());
        verify(gameInstanceManager, never()).createAndStart(any());
    }

    @Test
    @DisplayName("Debe eliminar sala cuando el último jugador sale")
    void removeEmptyRoom() {
        Room room = roomManager.createRoom("host-1", "map_01");
        String roomId = room.getRoomId();

        roomManager.leaveRoom(roomId, "host-1");

        assertNull(roomManager.getRoom(roomId));
    }
}
