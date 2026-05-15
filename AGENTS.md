# AGENTS.md — Multiplayer Shooter Game

## Branch workflow
- All work on `opencode` branch (already checked out).
- Never commit to `main`.

## Quick start
```bash
mvn clean install -DskipTests              # build & install everything (needed so client gets latest shared .jar)
java -jar server/target/server-1.0-SNAPSHOT.jar   # start server :8080
mvn javafx:run -pl client                  # launch client
python tools/test_client.py                 # headless test bot (pip install websocket-client)
python tools/load_test.py 5                 # stress test
```

## Test commands
```bash
mvn test -pl server                         # all server tests
mvn test -pl server -Dtest="!*IntegrationTest"  # unit only
mvn test -pl server -Dtest="com.aa.server.game.system.*"
```

## Architecture rules
- **Server-authoritative**: Client NEVER sends positions. Only normalized inputs (-1..1).
- **Thread safety**: Only GameLoop mutates GameState. Network thread enqueues inputs via ConcurrentLinkedQueue.
- **JSON**: Use `JsonUtil.toJson()` / `parseMessage()` exclusively. Never manual parse except for lobby messages in MessageHandler.
- **Broadcast**: Always `state.copy()` before serializing. Never send mutable GameState reference.
- **GameLoop**: Fixed 20Hz timestep. Drift resets `nextTick` to avoid death spiral.

## Gotchas (will save you hours)
- **Gson recursion split**: Two Gson instances — `gsonPlain` (no adapter) and `gson` (with `MessageAdapter`). Use the right one.
- **CREATE_ROOM maps to `LoginMessage.class`** in `MessageAdapter.getTargetClass()` — intentional hack. CREATE_ROOM is parsed manually from `JsonObject` in `MessageHandler`, not through the adapter. Don't "fix" this.
- **JUnit 3.8.1 in root `dependencyManagement`** is dead code. Actual testing uses JUnit 5 (Jupiter) from server/pom.xml.
- **Map collision not wired**: `GameMap`/`Obstacle` exist but `MovementSystem` and `CollisionSystem` do not check them yet. Tarea 1 pendiente.
- **No reconnection**: Disconnected player stays as ghost in GameState. Tarea 2 pendiente.
- **PING/PONG**: Server tracks nothing. Client ignores PING, server ignores PONG. No latency tracking.
- **`RoomManager`** does not broadcast on join/leave (only on create). Lobby screen can appear stale.

## File structure (key packages)
```
shared/   → message/, model/ (Vector2 record, Player, Bullet), state/ (GameState), util/ (JsonUtil)
server/   → network/ (GameServer :8080), handler/ (MessageHandler router), auth/ (BCrypt), room/, game/ (GameInstance + engine/ + system/ + map/), util/
client/   → network/ (NetworkClient), game/ (GameClient), input/ (InputHandler WASD+mouse), render/ (Renderer+Canvas), ui/ (ScreenManager)
tools/    → Python test scripts
```

## Adding new message types
1. Add enum value to `MessageType`
2. Create message class extending `Message`
3. Add `case TYPE -> NewMessage.class` to `JsonUtil.MessageAdapter.getTargetClass()` (switch statement)
4. Add handler case in `MessageHandler`
5. If needed on client, add handling in `GameClient.handleMessage()`

## Dependencies
- Java 25, Gson 2.10.1, Java-WebSocket 1.5.6, JavaFX 25, jbcrypt 0.4, SLF4J 2.0.12, JUnit 5.10.2, Mockito 5.11.0
- No Spring/Hibernate. No heavy frameworks.

## Credentials (dev only)
- `player1` / `pass1`, `player2` / `pass2` (created in AuthService constructor)
