package com.aa.shared.message;

import java.util.UUID;

/**
 * Clase base para todos los mensajes del protocolo.
 * Contiene metadatos comunes: tipo, ID de mensaje, timestamp.
 */
public abstract class Message {
    private MessageType type;
    private String messageId;
    private long timestamp;
    private String senderId; // ID del jugador que envía (null si es servidor)
    
    protected Message() {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }
    
    protected Message(MessageType type) {
        this();
        this.type = type;
    }
    
    // Getters y Setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, sender=%s, time=%d]", 
            type, messageId, senderId, timestamp);
    }
}
