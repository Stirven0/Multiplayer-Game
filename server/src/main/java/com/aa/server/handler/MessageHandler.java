package com.aa.server.handler;

import com.aa.server.auth.AuthService;
import com.aa.server.game.GameInstance;
import com.aa.server.game.GameInstanceManager;
import com.aa.server.game.PlayerInput;
import com.aa.server.network.ClientConnection;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;
import com.aa.server.room.RoomManager;
import com.aa.shared.message.*;
import com.aa.shared.util.JsonUtil;

/**
 * Router central. Deserializa y delega. Ninguna lógica de negocio aquí, solo enrutado y validación básica.
 */
public class MessageHandler {
    private final AuthService authService;
    private final RoomManager roomManager;
    private final GameInstanceManager gameInstanceManager;
    private final ConnectionManager connectionManager;

    public MessageHandler(AuthService authService, RoomManager roomManager,
                          GameInstanceManager gameInstanceManager, ConnectionManager connectionManager) {
        this.authService = authService;
        this.roomManager = roomManager;
        this.gameInstanceManager = gameInstanceManager;
        this.connectionManager = connectionManager;
    }

    public void handle(ClientConnection client, String json) {
        try {
            String typeStr = JsonUtil.extractField(json, "type");
            if (typeStr == null) {
                client.sendError("MISSING_TYPE", "Field 'type' is required", false);
                return;
            }

            MessageType type = MessageType.valueOf(typeStr);

            // Rutas públicas
            switch (type) {
                case LOGIN_REQUEST -> handleLogin(client, json);
                default -> handleAuthenticated(client, json, type);
            }

        } catch (IllegalArgumentException e) {
            client.sendError("INVALID_TYPE", "Unknown message type", false);
        } catch (Exception e) {
            client.sendError("PROCESSING_ERROR", e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private void handleAuthenticated(ClientConnection client, String json, MessageType type) {
        if (!client.isAuthenticated()) {
            client.sendError("NOT_AUTHENTICATED", "Login required", false);
            return;
        }

        switch (type) {
            case CREATE_ROOM -> handleCreateRoom(client, json);
            case JOIN_ROOM -> handleJoinRoom(client, json);
            case GAME_START -> handleStartGame(client, json);
            case MOVE_INPUT -> handleMove(client, json);
            case SHOOT_INPUT -> handleShoot(client, json);
            default -> client.sendError("UNHANDLED", "Message type not implemented: " + type, false);
        }
    }

    private void handleLogin(ClientConnection client, String json) {
        LoginMessage msg = JsonUtil.fromJson(json, LoginMessage.class);
        String token = authService.login(msg.getUsername(), msg.getPassword());
        if (token != null) {
            String userId = authService.getUserId(token);
            client.setUserId(userId);
            connectionManager.authenticate(client.getConnectionId(), userId);

            LoginMessage response = new LoginMessage();
            response.setToken(token);
            client.send(response);
        } else {
            client.sendError("AUTH_FAILED", "Invalid username or password", false);
        }
    }

    private void handleCreateRoom(ClientConnection client, String json) {
        // Parseo manual del mapId (puedes crear una clase CreateRoomMessage en shared)
        String mapId = extractStringField(json, "mapId", "map_01");
        Room room = roomManager.createRoom(client.getPlayerId(), mapId);
        client.setCurrentRoomId(room.getRoomId());
        client.send(JsonUtil.fromJson("{\"type\":\"ROOM_CREATED\",\"roomId\":\"" + room.getRoomId() + "\"}", Message.class));
    }

    private void handleJoinRoom(ClientConnection client, String json) {
        String roomId = extractStringField(json, "roomId", null);
        if (roomId == null) {
            client.sendError("BAD_REQUEST", "roomId required", false);
            return;
        }
        boolean ok = roomManager.joinRoom(roomId, client.getPlayerId());
        if (ok) {
            client.setCurrentRoomId(roomId);
        } else {
            client.sendError("JOIN_FAILED", "Room full or not found", false);
        }
    }

    private void handleStartGame(ClientConnection client, String json) {
        String roomId = client.getCurrentRoomId();
        if (roomId == null) {
            client.sendError("NOT_IN_ROOM", "Join a room first", false);
            return;
        }
        Room room = roomManager.getRoom(roomId);
        if (room == null || !room.isHost(client.getPlayerId())) {
            client.sendError("FORBIDDEN", "Only host can start", false);
            return;
        }
        roomManager.startGame(roomId);
    }

    private void handleMove(ClientConnection client, String json) {
        GameInstance game = gameInstanceManager.getGameByPlayer(client.getPlayerId());
        if (game == null) {
            client.sendError("NOT_IN_GAME", "Not in an active game", false);
            return;
        }
        MoveMessage msg = JsonUtil.fromJson(json, MoveMessage.class);
        msg.normalize(); // Sanitiza vector
        game.queueInput(new PlayerInput(client.getPlayerId(), MessageType.MOVE_INPUT, msg));
    }

    private void handleShoot(ClientConnection client, String json) {
        GameInstance game = gameInstanceManager.getGameByPlayer(client.getPlayerId());
        if (game == null) {
            client.sendError("NOT_IN_GAME", "Not in an active game", false);
            return;
        }
        if (!client.tryShoot()) {
            client.sendError("RATE_LIMIT", "Shooting too fast", false);
            return;
        }
        ShootMessage msg = JsonUtil.fromJson(json, ShootMessage.class);
        game.queueInput(new PlayerInput(client.getPlayerId(), MessageType.SHOOT_INPUT, msg));
    }

    private String extractStringField(String json, String field, String defaultValue) {
        try {
            com.google.gson.JsonObject obj = JsonUtil.parseToObject(json);
            return obj.has(field) ? obj.get(field).getAsString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
