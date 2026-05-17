package com.aa.shared.message;

public class ReconnectMessage extends Message {
    private String userId;
    private String token;

    public ReconnectMessage() {
        super(MessageType.RECONNECT);
    }

    public ReconnectMessage(String userId, String token) {
        this();
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}