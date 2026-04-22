package com.aa.shared.message;

public class PongMessage extends Message {
    public PongMessage() {
        super(MessageType.PONG);
    }
}