package com.aa.server.game;

import com.aa.server.game.engine.GameEngine;
import com.aa.server.game.engine.GameLoop;
import com.aa.server.game.map.GameMap;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;
import com.aa.server.util.ServerConfig;
import com.aa.shared.message.GameEndMessage;
import com.aa.shared.message.GameStateMessage;
import com.aa.shared.message.IdleWarningMessage;
import com.aa.shared.message.KickedIdleMessage;
import com.aa.shared.model.Player;
import com.aa.shared.model.Vector2;
import com.aa.shared.state.GameState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Instancia de una partida en el servidor.
 * Gestiona el estado del juego, la cola de entrada de jugadores,
 * la detección de inactividad, desconexiones y el bucle de juego.
 */
public class GameInstance {
    private final String gameId;
    private final Room room;
    private final GameState state;
    private final GameEngine engine;
    private final GameLoop loop;
    private final GameMap map;
    private final ConnectionManager connectionManager;
    private final ConcurrentLinkedQueue<PlayerInput> inputQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> disconnectQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> reconnectQueue = new ConcurrentLinkedQueue<>();
    private volatile int sequence = 0;
    private Runnable onGameEndCallback;
    private BiConsumer<String, com.aa.shared.message.Message> messageSender;

    // Idle tracking
    private final Map<String, Long> lastInputTime = new HashMap<>();
    private final Map<String, Long> idleWarningStart = new HashMap<>();
    private final Map<String, Long> lastWarningSend = new HashMap<>();

    /**
     * Construye la instancia de partida, crea jugadores con posiciones aleatorias
     * e inicializa el bucle de juego.
     * @param gameId identificador único de la partida
     * @param room sala a la que pertenece la partida
     * @param mapa mapa de la partida
     * @param connectionManager gestor de conexiones para broadcast
     */
    public GameInstance(String gameId, Room room, GameMap map, ConnectionManager connectionManager) {
        this.gameId = gameId;
        this.room = room;
        this.map = map;
        this.connectionManager = connectionManager;
        this.state = new GameState(gameId, room.getMapId());
        this.state.setObstacles(map.obstacles());
        this.state.setMapWidth(map.width());
        this.state.setMapHeight(map.height());
        this.engine = new GameEngine();

        // Spawn players
        for (String playerId : room.getPlayerIds()) {
            Player p = new Player(playerId, playerId, randomSpawn());
            state.addPlayer(p);
        }

        this.loop = new GameLoop(this);
    }

    /** Inicia la partida (cambia estado a PLAYING y arranca el bucle). */
    public void start() {
        state.setStatus(GameState.GameStatus.PLAYING);
        state.setStartTime(System.currentTimeMillis());
        loop.start();
    }

    /** Detiene el bucle de juego. */
    public void stop() {
        loop.stop();
    }

    /** Establece el callback a ejecutar al terminar la partida. */
    public void setOnGameEndCallback(Runnable callback) {
        this.onGameEndCallback = callback;
    }

    /** @return callback de fin de partida */
    public Runnable getOnGameEndCallback() {
        return onGameEndCallback;
    }

    /** Establece el enviador de mensajes para notificaciones individuales (inactividad, etc.). */
    public void setMessageSender(BiConsumer<String, com.aa.shared.message.Message> sender) {
        this.messageSender = sender;
    }

    /** Encola una entrada de jugador para procesar en el próximo tick. */
    public void queueInput(PlayerInput input) {
        inputQueue.offer(input);
    }

    /** @return true si hay entradas pendientes por procesar */
    public boolean hasPendingInputs() {
        return !inputQueue.isEmpty();
    }

    /** @return el gestor de conexiones */
    public ConnectionManager getConnectionManager() { return connectionManager; }

    /** @return el estado actual del juego */
    public GameState getState() { return state; }

    /** @return true si el jugador está en la sala */
    public boolean hasPlayer(String playerId) {
        return room.getPlayerIds().contains(playerId);
    }

    /** @return conjunto de IDs de jugadores en la sala */
    public java.util.Set<String> getRoomPlayerIds() {
        return room.getPlayerIds();
    }

    /**
     * Procesa un tick del juego: entrada de jugadores, desconexiones,
     * reconexiones, actualización del motor y detección de fin de partida.
     * @param deltaTime tiempo transcurrido desde el último tick en segundos
     */
    public void processTick(float deltaTime) {
        if (state.getStatus() == GameState.GameStatus.FINISHED) return;
        long now = System.currentTimeMillis();

        // Procesar desconexiones encoladas desde hilos externos
        String disconnectedId;
        while ((disconnectedId = disconnectQueue.poll()) != null) {
            Player p = state.getPlayer(disconnectedId);
            if (p != null && p.isAlive()) {
                p.setAlive(false);
                p.setHealth(0);
            }
        }

        // Procesar reconexiones encoladas desde hilos externos
        String reconnectedId;
        while ((reconnectedId = reconnectQueue.poll()) != null) {
            Player p = state.getPlayer(reconnectedId);
            if (p != null && !p.isAlive()) {
                p.setAlive(true);
                p.setHealth(100);
                p.setPosition(randomSpawn());
            }
        }

        List<PlayerInput> inputs = new ArrayList<>();
        while (!inputQueue.isEmpty()) {
            PlayerInput input = inputQueue.poll();
            inputs.add(input);
            lastInputTime.put(input.playerId(), now);
        }

        state.incrementTick();
        state.setTimestamp(now);
        engine.update(state, deltaTime, inputs, map);

        // Detectar fin de partida: <= 1 jugador vivo
        long aliveCount = state.getAllPlayers().stream().filter(Player::isAlive).count();
        if (aliveCount <= 1) {
            state.setStatus(GameState.GameStatus.FINISHED);
            state.setEndTime(now);
        }

        // Detectar jugadores inactivos (solo durante la partida)
        if (state.getStatus() == GameState.GameStatus.PLAYING) {
            checkIdlePlayers(now);
        }
    }

    /**
     * Verifica jugadores inactivos y envía advertencias progresivas.
     * Si el jugador supera IDLE_THRESHOLD_MS sin input, comienza una
     * cuenta regresiva de IDLE_WARNING_DURATION_MS antes de expulsarlo.
     */
    private void checkIdlePlayers(long now) {
        for (Player p : state.getAllPlayers()) {
            if (!p.isAlive()) continue;
            String pid = p.getId();

            Long lastTime = lastInputTime.get(pid);
            long idleTime = now - (lastTime != null ? lastTime : state.getStartTime());

            if (idleTime < ServerConfig.IDLE_THRESHOLD_MS) {
                if (idleWarningStart.remove(pid) != null) {
                    lastWarningSend.remove(pid);
                    if (messageSender != null) {
                        messageSender.accept(pid, new IdleWarningMessage(0));
                    }
                }
                continue;
            }

            Long warningStart = idleWarningStart.get(pid);
            if (warningStart != null) {
                long warningElapsed = now - warningStart;
                if (warningElapsed >= ServerConfig.IDLE_WARNING_DURATION_MS) {
                    idleWarningStart.remove(pid);
                    lastWarningSend.remove(pid);
                    if (messageSender != null) {
                        messageSender.accept(pid, new KickedIdleMessage());
                    }
                    markPlayerDisconnected(pid);
                } else {
                    long lastSend = lastWarningSend.getOrDefault(pid, 0L);
                    if (now - lastSend >= 1000) {
                        lastWarningSend.put(pid, now);
                        int remaining = (int)((ServerConfig.IDLE_WARNING_DURATION_MS - warningElapsed) / 1000) + 1;
                        if (messageSender != null) {
                            messageSender.accept(pid, new IdleWarningMessage(remaining));
                        }
                    }
                }
            } else {
                idleWarningStart.put(pid, now);
                lastWarningSend.put(pid, now);
                if (messageSender != null) {
                    messageSender.accept(pid, new IdleWarningMessage(ServerConfig.IDLE_WARNING_DURATION_SECONDS));
                }
            }
        }
    }

    /**
     * Envía una copia del estado del juego a todos los jugadores de la sala.
     */
    public void broadcastState() {
        GameState snapshot = state.copy();
        GameStateMessage msg = new GameStateMessage(snapshot, sequence++);
        connectionManager.broadcastToPlayers(room.getPlayerIds(), msg);
    }

    /** @return true si la partida ha terminado */
    public boolean isFinished() {
        return state.getStatus() == GameState.GameStatus.FINISHED;
    }

    /**
     * Crea el mensaje de fin de partida con las puntuaciones finales.
     * @return GameEndMessage con ganador y puntuaciones ordenadas
     */
    public GameEndMessage createGameEndMessage() {
        Player winner = null;
        for (Player p : state.getAllPlayers()) {
            if (p.isAlive()) {
                winner = p;
                break;
            }
        }

        String finalWinnerId = winner != null ? winner.getId() : "";
        String finalWinnerUsername = winner != null ? winner.getUsername() : "Empate";

        List<GameEndMessage.PlayerScore> scores = state.getAllPlayers().stream()
            .sorted(Comparator.comparingInt(Player::getKills).reversed())
            .map(p -> new GameEndMessage.PlayerScore(
                p.getId(), p.getUsername(), p.getKills(), p.getDeaths(),
                p.getId().equals(finalWinnerId)))
            .collect(Collectors.toList());

        long duration = state.getEndTime() - state.getStartTime();
        return new GameEndMessage(gameId, finalWinnerId, finalWinnerUsername, scores, duration);
    }

    /**
     * Marca a un jugador como desconectado (lo elimina de la cola de entrada
     * y lo encola para procesar su muerte en el próximo tick).
     */
    public void markPlayerDisconnected(String playerId) {
        inputQueue.removeIf(in -> in.playerId().equals(playerId));
        disconnectQueue.offer(playerId);
    }

    /** Encola una solicitud de reconexión para un jugador. */
    public void queueReconnect(String playerId) {
        reconnectQueue.offer(playerId);
    }

    /**
     * Genera una posición de aparición aleatoria dentro del mapa.
     * @return Vector2 con coordenadas aleatorias
     */
    public Vector2 randomSpawn() {
        double x = Math.random() * (map.width() - 100) + 50;
        double y = Math.random() * (map.height() - 100) + 50;
        return new Vector2(x, y);
    }
}
