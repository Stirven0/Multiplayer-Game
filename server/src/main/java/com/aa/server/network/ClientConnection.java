package com.aa.server.network;

import com.aa.server.util.RateLimiter;
import com.aa.shared.message.ErrorMessage;
import com.aa.shared.message.Message;
import com.aa.shared.util.JsonUtil;
import org.java_websocket.WebSocket;

import java.util.UUID;

/**
 * Wrapper thread-safe de una conexión WebSocket.
 */
public class ClientConnection {
    private final WebSocket socket;
    private final String connectionId;
    private volatile String userId;
    private volatile String playerId;
    private volatile String username;
    private volatile boolean authenticated = false;
    private volatile String currentRoomId;
    private final RateLimiter shootLimiter = new RateLimiter(250);
    private volatile long lastActivityTimestamp;

    public ClientConnection(WebSocket socket) {
        this.socket = socket;
        this.connectionId = UUID.randomUUID().toString();
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    public void updateActivity() {
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    public long getLastActivityTimestamp() { return lastActivityTimestamp; }

    public boolean isTimedOut(long timeoutMs) {
        return System.currentTimeMillis() - lastActivityTimestamp > timeoutMs;
    }

    public void send(Message message) {
        if (socket != null && socket.isOpen()) {
            socket.send(JsonUtil.toJson(message));
        }
    }

    public void sendError(String code, String msg, boolean fatal) {
        send(new ErrorMessage(code, msg, fatal));
    }

    public boolean tryShoot() {
        return shootLimiter.tryAcquire();
    }

    public boolean isOpen() {
        return socket != null && socket.isOpen();
    }

    public void close() {
        if (socket != null && socket.isOpen()) {
            socket.close();
        }
    }

    // Getters & Setters
    public String getConnectionId() { return connectionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCurrentRoomId() { return currentRoomId; }
    public void setCurrentRoomId(String currentRoomId) { this.currentRoomId = currentRoomId; }
}
