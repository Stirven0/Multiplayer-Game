package com.aa.server.network;

import com.aa.server.auth.AuthService;
import com.aa.server.game.GameInstanceManager;
import com.aa.server.handler.MessageHandler;
import com.aa.server.room.RoomManager;
import com.aa.server.game.map.MapManager;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * Punto de entrada WebSocket. Delega todo a MessageHandler.
 */
public class GameServer extends WebSocketServer {
    private final ConnectionManager connectionManager;
    private final MessageHandler messageHandler;

    public GameServer(InetSocketAddress address) {
        super(address);
        this.connectionManager = new ConnectionManager();

        AuthService authService = new AuthService();
        MapManager mapManager = new MapManager();
        GameInstanceManager gameInstanceManager = new GameInstanceManager(connectionManager, mapManager);
        RoomManager roomManager = new RoomManager(gameInstanceManager);

        this.messageHandler = new MessageHandler(authService, roomManager, gameInstanceManager, connectionManager);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connectionManager.register(conn);
        System.out.println("[NET] Connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String connId = conn.getAttachment();
        if (connId != null) {
            connectionManager.remove(connId);
            System.out.println("[NET] Disconnected: " + connId);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String connId = conn.getAttachment();
        ClientConnection client = connectionManager.getByConnectionId(connId);
        if (client != null) {
            messageHandler.handle(client, message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("[NET] Server listening on " + getPort());
    }
}
