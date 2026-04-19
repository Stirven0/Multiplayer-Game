package com.aa.shared.state;

import com.aa.shared.model.Player;
import com.aa.shared.model.Bullet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Estado completo del juego en un momento específico.
 * Esta clase es la "fuente de verdad" que el servidor sincroniza.
 * 
 * Nota: Usamos ConcurrentHashMap para thread-safety en el servidor,
 * pero el cliente puede tratarlo como un Map normal.
 */
public class GameState {
    private String gameId;
    private String mapId;
    private long tick; // Número de tick/fram
    private long timestamp; // Timestamp del servidor (ms)
    
    // Usamos Map para acceso O(1) por ID
    private Map<String, Player> players;
    private Map<String, Bullet> bullets;
    
    // Estado de la partida
    private GameStatus status;
    private long startTime;
    private long endTime;
    
    public enum GameStatus {
        WAITING,    // Esperando jugadores
        STARTING,   // Cuenta regresiva
        PLAYING,    // En juego
        FINISHED    // Terminada
    }
    
    public GameState() {
        this.players = new ConcurrentHashMap<>();
        this.bullets = new ConcurrentHashMap<>();
        this.status = GameStatus.WAITING;
        this.tick = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public GameState(String gameId, String mapId) {
        this();
        this.gameId = gameId;
        this.mapId = mapId;
    }
    
    // Métodos de acceso a jugadores
    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }
    
    public void removePlayer(String playerId) {
        players.remove(playerId);
    }
    
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }
    
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }
    
    // Métodos de acceso a balas
    public void addBullet(Bullet bullet) {
        bullets.put(bullet.getId(), bullet);
    }
    
    public void removeBullet(String bulletId) {
        bullets.remove(bulletId);
    }
    
    public Bullet getBullet(String bulletId) {
        return bullets.get(bulletId);
    }
    
    public List<Bullet> getAllBullets() {
        return new ArrayList<>(bullets.values());
    }
    
    // Getters y Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    
    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    
    public long getTick() { return tick; }
    public void setTick(long tick) { this.tick = tick; }
    public void incrementTick() { this.tick++; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    
    /**
     * Crea una copia superficial del estado para serialización.
     * Útil para enviar snapshots sin bloquear el estado real.
     */
    public GameState copy() {
        GameState copy = new GameState();
        copy.gameId = this.gameId;
        copy.mapId = this.mapId;
        copy.tick = this.tick;
        copy.timestamp = this.timestamp;
        copy.status = this.status;
        copy.startTime = this.startTime;
        copy.endTime = this.endTime;
        
        // Copiar jugadores y balas
        for (Player p : this.players.values()) {
            // Crear nuevas instancias para evitar aliasing
            Player pCopy = new Player(p.getId(), p.getUsername(), p.getPosition());
            pCopy.setDirection(p.getDirection());
            pCopy.setHealth(p.getHealth());
            pCopy.setAlive(p.isAlive());
            copy.addPlayer(pCopy);
        }
        
        for (Bullet b : this.bullets.values()) {
            Bullet bCopy = new Bullet(b.getId(), b.getPosition(), b.getDirection(),
                                     b.getSpeed(), b.getOwnerId(), b.getDamage());
            bCopy.setSpawnTime(b.getSpawnTime());
            copy.addBullet(bCopy);
        }
        
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("GameState[%s tick=%d players=%d bullets=%d status=%s]",
            gameId, tick, players.size(), bullets.size(), status);
    }
}
