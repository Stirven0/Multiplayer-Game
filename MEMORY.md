# MEMORY.md — Punto de control del proyecto

## Última sesión: FASE 3 completada (Mayo 13 2026)

## Estado actual
- **Rama**: `opencode`
- **Build**: `mvn clean package -DskipTests` → BUILD SUCCESS
- **Tests**: 48/58 pasan, 10 fallos pre-existentes sin cambios — sin regresiones
- **Servidor**: Funcionando en `:8080`, verificado
- **Cliente**: Compila OK

## Cambios aplicados

### FASE 0 — Bugfixes de mapas y login
### FASE 1 — Colisiones con mapa
### FASE 2 — Fin de partida y scoreboard

### FASE 3 — Reconexión básica (NUEVO)

| Archivo | Cambio |
|---|---|
| `server/ClientConnection.java` | +`lastActivityTimestamp` (init en constructor), +`updateActivity()`, +`isTimedOut(long)` |
| `server/ServerConfig.java` | +`CONNECTION_TIMEOUT_MS=30_000`, +`TIMEOUT_CHECK_INTERVAL_MS=10_000` |
| `server/GameServer.java` | `onClose()`: tras `connectionManager.remove()`, llama `gameInstanceManager.handleDisconnect(playerId)` si estaba jugando; `onMessage()`: llama `client.updateActivity()`; `onStart()`: arranca `startTimeoutChecker()` |
| `server/GameInstanceManager.java` | +`handleDisconnect(playerId)`: busca game instance y llama `markPlayerDisconnected()` |
| `server/GameInstance.java` | +`markPlayerDisconnected()`: remueve inputs encolados, marca jugador como muerto; `randomSpawn()` ahora es `public` |
| `shared/MessageType.java` | +`RECONNECT` |
| `shared/ReconnectMessage.java` | **NUEVO**: contiene `userId` + `token` |
| `shared/JsonUtil.java` | `getTargetClass()` → `RECONNECT` mapea a `ReconnectMessage.class` |
| `server/MessageHandler.java` | `handle()`: trata `RECONNECT` como ruta pública (sin auth). +`handleReconnect()`: valida token, re-autentica conexión, si hay partida activa respawnea al jugador |

## Próxima sesión: FASE 5 — Documentación

### Tareas pendientes

#### FASE 5: Documentación
1. README.md, diagramas Mermaid, manual de usuario, informe técnico
2. Actualizar AGENTS.md

### FASE 4 — Mejoras UI/UX (Completada)

| Archivo | Cambio |
|---|---|
| `shared/model/Obstacle.java` | **MOVIDO** desde `server/game/map/` a `shared/model/` — ahora es parte del módulo shared para que el cliente pueda usarlo |
| `server/map/GameMap.java` | Import actualizado a `com.aa.shared.model.Obstacle` |
| `server/map/MapLoader.java` | Import actualizado |
| `server/game/GameInstance.java` | `GameState` ahora recibe `obstacles`, `mapWidth`, `mapHeight` del `GameMap` |
| `shared/state/GameState.java` | +`List<Obstacle> obstacles`, +`mapWidth`, +`mapHeight`, incluidos en `copy()` |
| `client/render/Renderer.java` | +`drawObstacles()` (rectángulos grises con borde), +`drawCrosshair()` (sprite o líneas), `drawHud()` ahora es scoreboard completo (esquina superior derecha, ordenado por kills, con K/D), `render()` acepta `mouseScreenX/mouseScreenY` |
| `client/input/InputHandler.java` | +`getMouseScreenX()`, +`getMouseScreenY()` |
| `client/game/GameClient.java` | `render()` pasa coordenadas del mouse; `update()` setea bounds de cámara desde GameState; +`leaveRoom()`; `ROOM_UPDATED` ahora actualiza lobby |
| `client/render/Camera.java` | +`setBounds(mapW, mapH)`, `follow()` clampea a límites del mapa |
| `client/ui/LobbyScreen.java` | +`ListView<String>` para jugadores, +`updatePlayerList()`, +botón "Leave Room" |
| `server/handler/MessageHandler.java` | `handleJoinRoom()` ahora hace `broadcastRoomUpdate()`, +`handleLeaveRoom()`, ruteo de `LEAVE_ROOM` |

## Archivos clave para la FASE 5
- Raíz del proyecto: README.md
- docs/ — diagramas, manual, informe
