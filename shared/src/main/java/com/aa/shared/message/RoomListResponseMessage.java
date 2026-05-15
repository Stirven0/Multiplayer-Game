package com.aa.shared.message;

import java.util.List;

public class RoomListResponseMessage extends Message {
    private List<RoomInfo> rooms;

    public RoomListResponseMessage() {
        super(MessageType.ROOM_LIST_RESPONSE);
    }

    public RoomListResponseMessage(List<RoomInfo> rooms) {
        this();
        this.rooms = rooms;
    }

    public List<RoomInfo> getRooms() { return rooms; }
    public void setRooms(List<RoomInfo> rooms) { this.rooms = rooms; }

    public static class RoomInfo {
        private String roomId;
        private String hostId;
        private String mapId;
        private int playerCount;
        private int maxPlayers;
        private String status;

        public RoomInfo() {}

        public RoomInfo(String roomId, String hostId, String mapId, int playerCount, int maxPlayers, String status) {
            this.roomId = roomId;
            this.hostId = hostId;
            this.mapId = mapId;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.status = status;
        }

        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        public String getHostId() { return hostId; }
        public void setHostId(String hostId) { this.hostId = hostId; }
        public String getMapId() { return mapId; }
        public void setMapId(String mapId) { this.mapId = mapId; }
        public int getPlayerCount() { return playerCount; }
        public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }
        public int getMaxPlayers() { return maxPlayers; }
        public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
