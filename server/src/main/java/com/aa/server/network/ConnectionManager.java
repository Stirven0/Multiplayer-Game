package com.aa.server.network;

import org.java_websocket.WebSocket;
import com.aa.shared.message.Message;
import com.aa.shared.util.JsonUtil;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro central de conexiones. Indexado por connectionId y playerId.
 */
public class ConnectionManager {
    private final ConcurrentHashMap<String, ClientConnection> byConnectionId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClientConnection> byPlayerId = new ConcurrentHashMap<>();

    public void register(WebSocket socket) {
        ClientConnection conn = new ClientConnection(socket);
        socket.setAttachment(conn.getConnectionId());
        byConnectionId.put(conn.getConnectionId(), conn);
    }

    public void authenticate(String connectionId, String playerId) {
        ClientConnection conn = byConnectionId.get(connectionId);
        if (conn != null) {
            conn.setPlayerId(playerId);
            conn.setAuthenticated(true);
            byPlayerId.put(playerId, conn);
        }
    }

    public void remove(String connectionId) {
        ClientConnection conn = byConnectionId.remove(connectionId);
        if (conn != null && conn.getPlayerId() != null) {
            byPlayerId.remove(conn.getPlayerId());
        }
    }

    public ClientConnection getByConnectionId(String id) {
        return byConnectionId.get(id);
    }

    public ClientConnection getByPlayerId(String id) {
        return byPlayerId.get(id);
    }

    public void broadcastToPlayers(Collection<String> playerIds, String json) {
        for (String pid : playerIds) {
            ClientConnection c = byPlayerId.get(pid);
            if (c != null && c.isOpen()) {
                c.send(JsonUtil.fromJson(json, Message.class)); // Re-serialización temporal, mejor usar sendRaw
            }
        }
    }

    public void broadcastToPlayers(Collection<String> playerIds, Message message) {
        String json = JsonUtil.toJson(message);
        for (String pid : playerIds) {
            ClientConnection c = byPlayerId.get(pid);
            if (c != null && c.isOpen()) {
                // Envío directo evitando doble serialización
                c.send(message);
            }
        }
    }
}
