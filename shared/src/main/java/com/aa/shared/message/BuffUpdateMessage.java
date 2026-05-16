package com.aa.shared.message;

import java.util.List;

public class BuffUpdateMessage extends Message {
    private List<ActiveBuff> buffs;
    private List<ActiveBuff> debuffs;

    public BuffUpdateMessage() {
        super(MessageType.BUFF_UPDATE);
    }

    public BuffUpdateMessage(List<ActiveBuff> buffs, List<ActiveBuff> debuffs) {
        this();
        this.buffs = buffs;
        this.debuffs = debuffs;
    }

    public List<ActiveBuff> getBuffs() { return buffs; }
    public void setBuffs(List<ActiveBuff> buffs) { this.buffs = buffs; }

    public List<ActiveBuff> getDebuffs() { return debuffs; }
    public void setDebuffs(List<ActiveBuff> debuffs) { this.debuffs = debuffs; }

    public static class ActiveBuff {
        private String type;
        private long remainingMs;

        public ActiveBuff() {}

        public ActiveBuff(String type, long remainingMs) {
            this.type = type;
            this.remainingMs = remainingMs;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public long getRemainingMs() { return remainingMs; }
        public void setRemainingMs(long remainingMs) { this.remainingMs = remainingMs; }
    }
}
