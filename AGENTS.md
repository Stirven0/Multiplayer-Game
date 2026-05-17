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
mvn test -pl server                         # 57 tests server
mvn test -pl server -Dtest="!*IntegrationTest"  # solo unitarias
Xvfb :99 -ac -screen 0 1280x720x24 &       # iniciar display virtual (solo Linux)
DISPLAY=:99 mvn test -pl client             # 20 tests UI (Linux con Xvfb; en Windows: `mvn test -pl client` directo)
mvn test -pl server,client                  # ambos módulos
```

## Architecture rules
- **Server-authoritative**: Client NEVER sends positions. Only normalized inputs (-1..1).
- **Thread safety**: Only GameLoop mutates GameState. Network thread enqueues inputs via ConcurrentLinkedQueue.
- **JSON**: Use `JsonUtil.toJson()` / `parseMessage()` exclusively. Never manual parse except for lobby messages in MessageHandler.
- **Broadcast**: Always `state.copy()` before serializing. Never send mutable GameState reference.
- **GameLoop**: Fixed 30Hz timestep (configurable en `ServerConfig.TICK_RATE`). Drift resets `nextTick` to avoid death spiral. (README dice 20Hz — está desactualizado, el real es 30.)
- **Weapon System**: 5 tipos (PISTOL/SHOTGUN/RIFLE/SNIPER/SMG), 2 slots (primaria/secundaria), Q para swap. Stats embebidos en enum `WeaponType`. Pickups spawn aleatorios en mapa.
- **Power-ups**: 7 tipos (Speed/Damage+/FireRate/Shield/Health, Slow/Debilidad como debuffs). Buffs 8s, debuffs 4s, respawn 15s. Se recogen automáticamente al colisionar.
- **Upgrade System**: 5 niveles por kills acumulados en partida (2/5/9/14/20). Mejoras pasivas: daño, cadencia, velocidad, HP max, reducción daño. Persiste al morir.
- **DB persistence**: Tabla `player_stats` (total_kills, total_deaths, total_wins, total_games, upgrade_points). Stats persistidos vía `DatabaseManager.savePlayerStats()` al terminar partida.

## Gotchas
- **Gson recursion split**: Two Gson instances — `gsonPlain` (no adapter) and `gson` (with `MessageAdapter`). Use the right one.
- **CREATE_ROOM maps to `LoginMessage.class`** in `MessageAdapter.getTargetClass()` — intentional hack. CREATE_ROOM is parsed manually from `JsonObject` in `MessageHandler`. No "arreglar".
- **JUnit 3.8.1 in root `dependencyManagement`** is dead code. Actual testing uses JUnit 5 (Jupiter) from server/pom.xml.
- **Reconnection (server-only)**: Server valida token + encola reactivación en GameInstance. Cliente nunca inicia reconexión — al desconectar resetea a lobby.
- **PING/PONG**: Server tracks nothing. Client ignores PING, server ignores PONG. No latency tracking.
- **Byte Buddy + JDK 25**: Requiere `-Dnet.bytebuddy.experimental=true` en argLine del surefire plugin para mockear con Mockito.
- **ServerConfig hardcodes values**: No carga .env pese a existir `.env.example`. Editar ServerConfig.java para cambiar TICK_RATE, PLAYER_SPEED, BULLET_DAMAGE, IDLE_THRESHOLD, etc.
- **DatabaseManager usa System.getProperty()**: Pasar `-DDB_URL=...` para DB distinta, o acepta default `jdbc:sqlite:shooter.db`. `.env.example` no se lee. HikariCP + SQLite (PostgreSQL también soportado). `initForTest()` usa SQLite in-memory.
- **No CI/CD**: No .github, no Actions, no pre-commit hooks.
- **Unused MessageType values**: `ROTATE_INPUT`, `USE_ABILITY`, `DELTA_STATE`, `ENTITY_SPAWN`, `ENTITY_DESTROY`, `PLAYER_HIT`, `PLAYER_DEATH` definidos en enum pero sin cablear en MessageAdapter ni handlers.

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
