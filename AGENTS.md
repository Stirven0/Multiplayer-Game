# AGENTS.md — Multiplayer Shooter Game

## Branch workflow
- Todo el trabajo en `develop`. Nunca commitear a `main`.

## Quick start
```bash
mvn clean install -DskipTests              # build & install todo
java -jar server/target/server-1.0-SNAPSHOT.jar   # servidor :8080
mvn javafx:run -pl client                  # lanzar cliente
python tools/test_client.py                 # bot headless (pip install websocket-client)
python tools/load_test.py 5                 # stress test (5 bots)
```

## Test commands
```bash
mvn test -pl server                         # 58 tests server
mvn test -pl server -Dtest="!*IntegrationTest"  # solo unitarias
Xvfb :99 -ac -screen 0 1280x720x24 &       # iniciar display virtual
DISPLAY=:99 mvn test -pl client             # 21 tests UI (requiere Xvfb)
```

## Architecture rules
- **Server-authoritative**: Client NEVER sends positions. Only normalized inputs (-1..1).
- **Thread safety**: Only GameLoop mutates GameState. Network thread enqueues inputs via ConcurrentLinkedQueue.
- **JSON**: Use `JsonUtil.toJson()` / `parseMessage()` exclusively. Never manual parse except for lobby messages in MessageHandler.
- **Broadcast**: Always `state.copy()` before serializing. Never send mutable GameState reference.
- **GameLoop**: Fixed 20Hz timestep. Drift resets `nextTick` to avoid death spiral.

## Gotchas
- **Gson recursion split**: Two Gson instances — `gsonPlain` (no adapter) and `gson` (with `MessageAdapter`). Use the right one.
- **CREATE_ROOM maps to `LoginMessage.class`** in `MessageAdapter.getTargetClass()` — intentional hack. CREATE_ROOM is parsed manually from `JsonObject` in `MessageHandler`. No "arreglar".
- **JUnit 3.8.1 in root `dependencyManagement`** is dead code. Actual testing uses JUnit 5 (Jupiter) from server/pom.xml.
- **No reconnection**: Disconnected player stays as ghost in GameState. Tarea 2 pendiente.
- **PING/PONG**: Server tracks nothing. Client ignores PING, server ignores PONG. No latency tracking.
- **Byte Buddy + JDK 25**: Requiere `-Dnet.bytebuddy.experimental=true` en argLine del surefire plugin para mockear con Mockito.

## File structure
```
shared/   → message/, model/, state/, util/
server/   → network/, handler/, auth/, room/, game/ (engine/, system/, map/), util/
client/   → network/, game/, input/, render/, ui/, asset/, util/
tools/    → Python test scripts
```

## Adding new message types
1. Add enum value to `MessageType`
2. Create message class extending `Message`
3. Add `case TYPE -> NewMessage.class` to `JsonUtil.MessageAdapter.getTargetClass()`
4. Add handler case in `MessageHandler`
5. If needed on client, add handling in `GameClient.handleMessage()`

## Dependencies
- Java 25, Gson 2.10.1, Java-WebSocket 1.5.6, JavaFX 25, jbcrypt 0.4, SLF4J 2.0.12, JUnit 5.10.2, Mockito 5.11.0, TestFX 4.0.18
- No Spring/Hibernate.

## Credentials (dev only)
- `player1` / `pass1`, `player2` / `pass2` (creados en AuthService constructor)
