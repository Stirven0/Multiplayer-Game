package com.aa.server.handler;

import com.aa.server.auth.AuthService;
import com.aa.server.game.GameInstance;
import com.aa.server.game.GameInstanceManager;
import com.aa.server.game.PlayerInput;
import com.aa.server.network.ClientConnection;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;
import com.aa.server.room.RoomManager;
import com.aa.server.util.ServerConfig;
import com.aa.shared.message.*;
import com.aa.shared.util.JsonUtil;
import java.util.ArrayList;

/**
 * Router central. Deserializa y delega. Ninguna lógica de negocio aquí, solo enrutado y validación básica.
 */
public class MessageHandler {

    private final AuthService authService;
    private final RoomManager roomManager;
    private final GameInstanceManager gameInstanceManager;
    private final ConnectionManager connectionManager;

    public MessageHandler(
        AuthService authService,
        RoomManager roomManager,
        GameInstanceManager gameInstanceManager,
        ConnectionManager connectionManager
    ) {
        this.authService = authService;
        this.roomManager = roomManager;
        this.gameInstanceManager = gameInstanceManager;
        this.connectionManager = connectionManager;
    }

    public void handle(ClientConnection client, String json) {
        try {
            String typeStr = JsonUtil.extractField(json, "type");
            if (typeStr == null) {
                client.sendError(
                    "MISSING_TYPE",
                    "Field 'type' is required",
                    false
                );
                return;
            }

            MessageType type = MessageType.valueOf(typeStr);

            // Rutas públicas
            switch (type) {
                case LOGIN_REQUEST -> handleLogin(client, json);
                case RECONNECT -> handleReconnect(client, json);
                default -> handleAuthenticated(client, json, type);
            }
        } catch (IllegalArgumentException e) {
            client.sendError("INVALID_TYPE", "Unknown message type", false);
        } catch (Exception e) {
            client.sendError("PROCESSING_ERROR", e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private void handleAuthenticated(
        ClientConnection client,
        String json,
        MessageType type
    ) {
        if (!client.isAuthenticated()) {
            client.sendError("NOT_AUTHENTICATED", "Login required", false);
            return;
        }

        switch (type) {
            case CREATE_ROOM -> handleCreateRoom(client, json);
            case JOIN_ROOM -> handleJoinRoom(client, json);
            case LEAVE_ROOM -> handleLeaveRoom(client, json);
            case ROOM_LIST -> handleRoomList(client);
            case GAME_START -> handleStartGame(client, json);
            case MOVE_INPUT -> handleMove(client, json);
            case SHOOT_INPUT -> handleShoot(client, json);
            case PING, PONG -> {
                /* heartbeat, no hacer nada */
            }
            default -> {
                // Solo loggear, no enviar error al cliente por mensajes desconocidos
                System.out.println("[HANDLER] Mensaje no manejado: " + type);
            }
        }
    }

    private void handleLogin(ClientConnection client, String json) {
        LoginMessage msg = JsonUtil.fromJson(json, LoginMessage.class);
        LoginResponseMessage response = new LoginResponseMessage();

        if (msg.isRegister()) {
            boolean registered = authService.register(msg.getUsername(), msg.getPassword());
            if (!registered) {
                response.setSuccess(false);
                response.setErrorMessage("El usuario ya existe");
                client.send(response);
                return;
            }
            System.out.println("[AUTH] Registrado: " + msg.getUsername());
        }

        // Verificar si el usuario ya está conectado desde otro cliente
        boolean alreadyConnected = false;
        for (ClientConnection c : connectionManager.getAll()) {
            if (c.isAuthenticated() && msg.getUsername().equals(c.getUsername()) && c != client) {
                alreadyConnected = true;
                break;
            }
        }
        if (alreadyConnected) {
            response.setSuccess(false);
            response.setErrorMessage("El usuario ya está conectado desde otro cliente");
            client.send(response);
            return;
        }

        String token = authService.login(msg.getUsername(), msg.getPassword());
        if (token != null) {
            String userId = authService.getUserId(token);
            client.setUserId(userId);
            client.setUsername(msg.getUsername());
            connectionManager.authenticate(client.getConnectionId(), userId);

            response.setSuccess(true);
            response.setToken(token);
            response.setUserId(userId);
            response.setUsername(msg.getUsername());

            System.out.println(
                "[AUTH] Login exitoso: " + msg.getUsername() + " -> " + userId
            );
        } else {
            response.setSuccess(false);
            response.setErrorMessage("Usuario o contraseña incorrectos");
        }
        client.send(response);
    }

    private void handleCreateRoom(ClientConnection client, String json) {
        String mapId = extractStringField(json, "mapId", "map_01");
        Room room = roomManager.createRoom(client.getPlayerId(), mapId);
        client.setCurrentRoomId(room.getRoomId());

        // Enviar respuesta formal
        RoomCreatedMessage response = new RoomCreatedMessage();
        response.setRoomId(room.getRoomId());
        response.setHostId(room.getHostId());
        response.setMapId(room.getMapId());
        client.send(response);

        // Notificar a otros (si hubiera)
        broadcastRoomUpdate(room);
    }

    private void broadcastRoomUpdate(Room room) {
        RoomUpdatedMessage update = new RoomUpdatedMessage();
        update.setRoomId(room.getRoomId());
        update.setPlayerIds(new ArrayList<>(room.getPlayerIds()));
        update.setStatus(room.getStatus().name());

        for (String pid : room.getPlayerIds()) {
            ClientConnection c = connectionManager.getByPlayerId(pid);
            if (c != null) c.send(update);
        }
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
            Room room = roomManager.getRoom(roomId);
            if (room != null) broadcastRoomUpdate(room);
        } else {
            client.sendError("JOIN_FAILED", "Room full or not found", false);
        }
    }

    private void handleRoomList(ClientConnection client) {
        java.util.List<RoomListResponseMessage.RoomInfo> infos = new java.util.ArrayList<>();
        for (Room r : roomManager.listOpenRooms()) {
            infos.add(new RoomListResponseMessage.RoomInfo(
                r.getRoomId(), r.getHostId(), r.getMapId(),
                r.getPlayerCount(), com.aa.server.util.ServerConfig.MAX_PLAYERS_PER_ROOM,
                r.getStatus().name()
            ));
        }
        client.send(new RoomListResponseMessage(infos));
    }

    private void handleLeaveRoom(ClientConnection client, String json) {
        String roomId = client.getCurrentRoomId();
        if (roomId == null) return;
        roomManager.leaveRoom(roomId, client.getPlayerId());
        client.setCurrentRoomId(null);
        Room room = roomManager.getRoom(roomId);
        if (room != null) broadcastRoomUpdate(room);
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
        if (room.getPlayerCount() < ServerConfig.MIN_PLAYERS_TO_START) {
            client.sendError("NOT_ENOUGH_PLAYERS",
                "Need at least " + ServerConfig.MIN_PLAYERS_TO_START + " players to start", false);
            return;
        }
        roomManager.startGame(roomId);
    }

    private void handleMove(ClientConnection client, String json) {
        GameInstance game = gameInstanceManager.getGameByPlayer(
            client.getPlayerId()
        );
        if (game == null) {
            client.sendError("NOT_IN_GAME", "Not in an active game", false);
            return;
        }
        MoveMessage msg = JsonUtil.fromJson(json, MoveMessage.class);
        msg.normalize(); // Sanitiza vector
        game.queueInput(
            new PlayerInput(client.getPlayerId(), MessageType.MOVE_INPUT, msg)
        );
    }

    private void handleShoot(ClientConnection client, String json) {
        GameInstance game = gameInstanceManager.getGameByPlayer(
            client.getPlayerId()
        );
        if (game == null) {
            client.sendError("NOT_IN_GAME", "Not in an active game", false);
            return;
        }
        if (!client.tryShoot()) {
            client.sendError("RATE_LIMIT", "Shooting too fast", false);
            return;
        }
        ShootMessage msg = JsonUtil.fromJson(json, ShootMessage.class);
        game.queueInput(
            new PlayerInput(client.getPlayerId(), MessageType.SHOOT_INPUT, msg)
        );
    }

    private void handleReconnect(ClientConnection client, String json) {
        ReconnectMessage msg = JsonUtil.fromJson(json, ReconnectMessage.class);
        if (!authService.validateToken(msg.getToken())) {
            client.sendError("RECONNECT_FAILED", "Token invalido o expirado", false);
            return;
        }

        String userId = msg.getUserId();
        String playerId = userId; // userId == playerId en este sistema

        // Re-autenticar la nueva conexion con el playerId existente
        client.setPlayerId(playerId);
        client.setUserId(userId);
        client.setAuthenticated(true);
        connectionManager.authenticate(client.getConnectionId(), playerId);

        GameInstance game = gameInstanceManager.getGameByPlayer(playerId);
        if (game != null && !game.isFinished()) {
            game.queueReconnect(playerId);
            System.out.println("[HANDLER] Reconnect exitoso: " + playerId);
            LoginResponseMessage resp = new LoginResponseMessage();
            resp.setSuccess(true);
            resp.setToken(msg.getToken());
            resp.setUserId(userId);
            resp.setUsername(userId);
            client.send(resp);
        } else {
            client.sendError("RECONNECT_FAILED", "No hay partida activa", false);
        }
    }

    private String extractStringField(
        String json,
        String field,
        String defaultValue
    ) {
        try {
            com.google.gson.JsonObject obj = JsonUtil.parseToObject(json);
            return obj.has(field) ? obj.get(field).getAsString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
