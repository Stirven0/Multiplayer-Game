package com.aa.client.network;

import com.aa.shared.message.Message;

public interface ClientMessageListener {
    void onConnected();
    void onDisconnected(String reason);
    void onMessageReceived(Message message);
    void onError(String code, String description);
}