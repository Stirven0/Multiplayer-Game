package com.aa.shared.message;

public class IdleWarningMessage extends Message {
    private int seconds;

    public IdleWarningMessage() {}

    public IdleWarningMessage(int seconds) {
        super(MessageType.IDLE_WARNING);
        this.seconds = seconds;
    }

    public int getSeconds() { return seconds; }
}
