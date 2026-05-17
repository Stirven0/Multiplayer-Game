# ENV-REVIEW.md — Informe de viabilidad del entorno

**Fecha:** 2026-05-16
**Arquitectura:** `linux/aarch64` (Android kernel 5.10)
**Estado:** ✅ VIABLE

---

## Especificaciones

| Componente | Versión |
|---|---|
| JDK | OpenJDK 25.0.1 |
| Maven | 3.9.9 |
| Python | 3.13.3 |
| Xvfb | Disponible |
| pip (venv tools/) | websocket-client 1.9.0 |

---

## Build

```
mvn clean install -DskipTests   →   BUILD SUCCESS  (57.2s)
```

| Módulo | Tiempo | Resultado |
|--------|--------|-----------|
| shared | 7.7s | ✅ |
| server | 34.6s | ✅ (shade: warnings overlapping no críticos) |
| client | 13.1s | ✅ (JavaFX linux-aarch64 nativo descargado) |

---

## Tests

| Suite | Comando | Pasaron |
|-------|---------|---------|
| Server (unitarias) | `mvn test -pl server -Dtest="!*IntegrationTest"` | **57/57** |
| Server (completas) | `mvn test -pl server` | *No ejecutado* (depende de puerto 8080 libre) |
| Client UI (headless) | `DISPLAY=:99 mvn test -pl client` | **20/20** |

> **Nota:** MEMORY.md reportaba 21 tests de cliente — hoy se obtuvieron 20. Posible test renombrado o eliminado.

---

## Servidor

| Aspecto | Resultado |
|---------|-----------|
| Puerto | 8080 |
| DB | SQLite vía HikariCP (shooter.db creado) |
| Log | `[MAIN] Server iniciado en puerto 8080` |
| Shutdown | Graceful (shutdown hook detiene HikariPool) |

---

## Load test (2 bots)

```
python tools/load_test.py 2   →   35.2s, 2/2 bots, 1132 mensajes
```

---

## Cliente JavaFX

| Aspecto | Resultado |
|---------|-----------|
| Compilación | ✅ |
| Lanzamiento (`mvn javafx:run`) | ✅ Inicia correctamente |
| Tests headless (Xvfb) | ✅ 20/20 |

---

## Problemas conocidos (no bloqueantes)

| Issue | Nota |
|-------|------|
| `WARNING: Unknown module: javafx.graphics` en tests | `--add-exports`/`--add-opens` en surefire esperan módulo nombrado. No afecta. |
| `WARNING: Restricted methods will be blocked` (SQLite/JavaFX native) | `System::load` por SQLite + JavaFX native loader. En JDK futuro requerirá `--enable-native-access=ALL-UNNAMED`. |
| shade-plugin overlapping resources | Múltiples JARs en uber-jar. No afecta runtime. |
| `mvn javafx:run -pl client` timeout por falta de input | El cliente arranca pero espera conexión al server — comportamiento normal. |
