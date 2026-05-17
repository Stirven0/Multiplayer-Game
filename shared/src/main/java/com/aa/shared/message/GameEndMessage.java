package com.aa.shared.message;

import java.util.List;

public class GameEndMessage extends Message {
    private String gameId;
    private String winnerId;
    private String winnerUsername;
    private List<PlayerScore> scores;
    private long duration;

    public GameEndMessage() {
        super(MessageType.GAME_END);
    }

    public GameEndMessage(String gameId, String winnerId, String winnerUsername,
                          List<PlayerScore> scores, long duration) {
        this();
        this.gameId = gameId;
        this.winnerId = winnerId;
        this.winnerUsername = winnerUsername;
        this.scores = scores;
        this.duration = duration;
    }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }

    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }

    public List<PlayerScore> getScores() { return scores; }
    public void setScores(List<PlayerScore> scores) { this.scores = scores; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public static class PlayerScore {
        private String playerId;
        private String username;
        private int kills;
        private int deaths;
        private boolean winner;

        public PlayerScore() {}

        public PlayerScore(String playerId, String username, int kills, int deaths, boolean winner) {
            this.playerId = playerId;
            this.username = username;
            this.kills = kills;
            this.deaths = deaths;
            this.winner = winner;
        }

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public int getKills() { return kills; }
        public void setKills(int kills) { this.kills = kills; }

        public int getDeaths() { return deaths; }
        public void setDeaths(int deaths) { this.deaths = deaths; }

        public boolean isWinner() { return winner; }
        public void setWinner(boolean winner) { this.winner = winner; }
    }
}