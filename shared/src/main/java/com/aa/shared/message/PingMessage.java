package com.aa.shared.message;

public class PingMessage extends Message {
    public PingMessage() {
        super(MessageType.PING);
    }
}