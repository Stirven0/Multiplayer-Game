# Multiplayer Shooter Game

Juego de disparos multijugador en 2D con arquitectura servidor-autoritario.
Cliente JavaFX, servidor WebSocket en Java 25.

## Requisitos

- Java 25 (JDK)
- Maven 3.9+
- Python 3 (para scripts de test)
- Xvfb (opcional, para ejecutar tests UI)

## Quick start

```bash
# Compilar e instalar todo (compartido → servidor → cliente)
mvn clean install -DskipTests

# Terminal 1: Servidor
java -jar server/target/server-1.0-SNAPSHOT.jar

# Terminal 2: Cliente (JavaFX)
mvn javafx:run -pl client

# Terminal 3: Test bot (Python)
pip install websocket-client
python tools/test_client.py
```

## Estructura del proyecto

```
shared/     → message/, model/, state/, util/  (código compartido servidor+cliente)
server/     → network/, handler/, auth/, room/, game/, util/
client/     → network/, game/, input/, render/, ui/, asset/, util/
tools/      → Scripts Python para testing
```

## Tests

```bash
# Servidor (58 tests)
mvn test -pl server

# Servidor — solo unitarias
mvn test -pl server -Dtest="!*IntegrationTest"

# Cliente — UI tests (requiere Xvfb)
DISPLAY=:99 mvn test -pl client

# Servidor + Cliente
mvn test -pl server,client
```

## Arquitectura

- **Server-authoritative**: El cliente nunca envía posiciones; solo inputs normalizados (-1..1)
- **GameLoop a 20 Hz**: Timestep fijo, deriva corregida tick a tick
- **Thread-safe**: Solo el GameLoop muta GameState; hilo de red encola inputs vía `ConcurrentLinkedQueue`
- **Broadcast**: Siempre `state.copy()` antes de serializar — nunca se envía referencia mutable

## Licencia

MIT
