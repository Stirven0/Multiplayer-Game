package com.aa.shared.message;

public class RoomCreatedMessage extends Message {
    private String roomId;
    private String hostId;
    private String mapId;

    public RoomCreatedMessage() {
        super(MessageType.ROOM_CREATED);
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }
    
    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
}