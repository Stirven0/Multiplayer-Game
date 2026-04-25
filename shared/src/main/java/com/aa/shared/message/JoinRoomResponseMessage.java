package com.aa.shared.message;

public class JoinRoomResponseMessage extends Message {
    private String roomId;
    private boolean success;
    private String message;

    public JoinRoomResponseMessage() {
        super(MessageType.JOIN_ROOM_RESPONSE);
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}