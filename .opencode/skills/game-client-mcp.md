# Game Client MCP Skill

## Description
Working with the JavaFX client's --mcp mode, which exposes an embedded MCP server for AI agent control.

## When to use
- Debugging the client MCP server
- Adding new client-side MCP tools
- Testing screenshot capture or key simulation
- Modifying how the AI agent interacts with the client

## Architecture
The client MCP server runs alongside the JavaFX game loop. It exposes tools that let an AI "see" the game viewport and "press keys" as if it were a human player.

## Steps

### 1. Run client in MCP mode
```bash
mvn javafx:run -pl client -Dexec.args="--mcp"
```

### 2. Client MCP tools available
| Tool | Description |
|------|-------------|
| `screenshot` | Captures current canvas as base64 PNG |
| `get_hud_info` | Health, weapon, ammo, kills, buffs |
| `get_player_pos` | Player world coordinates |
| `send_key` | Simulate key press (WASD, Q, E, F, click) |
| `get_game_state` | Raw game state from last server message |

### 3. Adding a new client MCP tool
1. Add method in `ClientMcpServer.java` with `@Tool` annotation
2. Register in the tool registration block
3. Test with `--mcp` flag
