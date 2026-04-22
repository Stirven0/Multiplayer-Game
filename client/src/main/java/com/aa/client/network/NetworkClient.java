package com.aa.client.network;

import com.aa.shared.message.Message;
import com.aa.shared.util.JsonUtil;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class NetworkClient extends WebSocketClient {
    private final ClientMessageListener listener;

    public NetworkClient(URI uri, ClientMessageListener listener) {
        super(uri);
        this.listener = listener;
    }

    public boolean connectBlocking(int timeoutMs) throws InterruptedException {
        boolean connected = connectBlocking(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        return connected;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("[NET] WS abierto: " + handshake.getHttpStatus());
        listener.onConnected();
    }

    @Override
    public void onMessage(String json) {
        try {
            Message msg = JsonUtil.parseMessage(json);
            listener.onMessageReceived(msg);
        } catch (Exception e) {
            System.err.println("[NET] Error parseando: " + json);
            e.printStackTrace();
            listener.onError("PARSE_ERROR", e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[NET] WS cerrado: " + code + " / " + reason);
        listener.onDisconnected(reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[NET] WS error: " + ex.getMessage());
        listener.onError("WS_ERROR", ex.getMessage());
    }

    public void sendMessage(Message msg) {
        if (!isOpen()) {
            System.err.println("[NET] No se puede enviar, WS no está abierto. Msg: " + msg.getType());
            return;
        }
        String json = JsonUtil.toJson(msg);
        System.out.println("[NET] Enviando: " + json);
        send(json);
    }

    public void sendJson(JsonObject json) {
        if (!isOpen()) {
            System.err.println("[NET] No se puede enviar JSON, WS no está abierto");
            return;
        }
        String str = json.toString();
        System.out.println("[NET] Enviando JSON: " + str);
        send(str);
    }
}