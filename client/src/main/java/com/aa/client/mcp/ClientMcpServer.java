package com.aa.client.mcp;

import com.aa.client.game.GameClient;
import com.aa.client.game.GameClientState;
import com.aa.client.input.InputHandler;
import com.aa.client.render.Renderer;
import com.aa.shared.message.BuffUpdateMessage;
import com.aa.shared.model.Player;
import com.aa.shared.state.GameState;
import com.aa.shared.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;

import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientMcpServer {

    private final GameClient gameClient;
    private final InputHandler inputHandler;
    private final Renderer renderer;
    private final Canvas canvas;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private volatile boolean running = false;
    private McpAsyncServer server;

    public ClientMcpServer(GameClient gameClient, InputHandler inputHandler, Renderer renderer, Canvas canvas) {
        this.gameClient = gameClient;
        this.inputHandler = inputHandler;
        this.renderer = renderer;
        this.canvas = canvas;
    }

    public void start() {
        if (running) return;
        running = true;

        new Thread(() -> {
            try {
                McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

                server = McpServer.async(new StdioServerTransportProvider(jsonMapper))
                    .serverInfo("multiplayer-client", "1.0.0")
                    .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .build())
                    .build();

                registerTools();
                System.err.println("[CLIENT-MCP] MCP Server ready on stdio");
                Thread.currentThread().join();
            } catch (Exception e) {
                System.err.println("[CLIENT-MCP] Error: " + e.getMessage());
                e.printStackTrace();
            }
        }, "client-mcp-thread").start();
    }

    public void stop() {
        running = false;
        if (server != null) {
            try { server.close(); } catch (Exception ignored) {}
        }
    }

    private McpSchema.Tool toolDef(String name, String description) {
        return toolDef(name, description, Map.of());
    }

    private McpSchema.Tool toolDef(String name, String description, Map<String, Object> properties) {
        Map<String, Object> schemaProps = new HashMap<>();
        List<String> required = new java.util.ArrayList<>();

        for (var entry : properties.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> propDef = (Map<String, Object>) entry.getValue();
            schemaProps.put(entry.getKey(), propDef);
            if (propDef.get("required") == Boolean.TRUE) {
                required.add(entry.getKey());
            }
        }

        return McpSchema.Tool.builder()
            .name(name)
            .description(description)
            .inputSchema(new McpSchema.JsonSchema("object", schemaProps, required, false, Map.of(), Map.of()))
            .build();
    }

    private void registerTools() {
        addTool("screenshot",
            "Capture the current game viewport as a base64-encoded BGRA_8888 pixel buffer",
            Map.of(),
            (exchange, args) -> {
                String result = captureScreenshot();
                if (result == null) {
                    return Mono.just(errorResult("No canvas available"));
                }
                return Mono.just(successResult(result));
            });

        addTool("get_hud_info",
            "Get HUD information: health, weapon, ammo, kills, shield, upgrades",
            Map.of(),
            (exchange, args) -> {
                JsonObject info = new JsonObject();
                GameClientState clientState = gameClient.getClientState();
                GameState gs = clientState.getCurrentState();
                Player me = gs != null ? gs.getPlayer(clientState.getLocalPlayerId()) : null;

                if (me != null) {
                    info.addProperty("health", me.getHealth());
                    info.addProperty("max_health", me.getMaxHealth());
                    info.addProperty("shield", me.getShield());
                    info.addProperty("alive", me.isAlive());
                    info.addProperty("kills", me.getKills());
                    info.addProperty("deaths", me.getDeaths());
                    info.addProperty("current_weapon", me.getCurrentWeapon().getDisplayName());
                    info.addProperty("weapon_slot", me.getCurrentWeaponSlot() == 0 ? "primary" : "secondary");
                    if (me.getPrimaryWeapon() != null) info.addProperty("primary_weapon", me.getPrimaryWeapon().getDisplayName());
                    if (me.getSecondaryWeapon() != null) info.addProperty("secondary_weapon", me.getSecondaryWeapon().getDisplayName());
                    info.addProperty("upgrade_points", me.getUpgradePoints());

                    List<BuffUpdateMessage.ActiveBuff> buffs = gameClient.getActiveBuffs();
                    JsonArray buffsArr = new JsonArray();
                    if (buffs != null) {
                        for (BuffUpdateMessage.ActiveBuff b : buffs) {
                            JsonObject bo = new JsonObject();
                            bo.addProperty("type", b.getType());
                            bo.addProperty("remaining_ms", (int) b.getRemainingMs());
                            buffsArr.add(bo);
                        }
                    }
                    info.add("active_buffs", buffsArr);
                } else {
                    info.addProperty("status", "not_in_game");
                }

                return Mono.just(successResult(gson.toJson(info)));
            });

        addTool("get_player_position",
            "Get the player's current world position coordinates",
            Map.of(),
            (exchange, args) -> {
                GameClientState clientState = gameClient.getClientState();
                GameState gs = clientState.getCurrentState();
                Player me = gs != null ? gs.getPlayer(clientState.getLocalPlayerId()) : null;

                if (me == null) {
                    return Mono.just(errorResult("Player not found"));
                }
                JsonObject pos = new JsonObject();
                pos.addProperty("x", me.getPosition().x());
                pos.addProperty("y", me.getPosition().y());
                return Mono.just(successResult(gson.toJson(pos)));
            });

        addTool("get_game_state",
            "Get the complete game state as seen by the client",
            Map.of(),
            (exchange, args) -> {
                GameClientState clientState = gameClient.getClientState();
                GameState gs = clientState.getCurrentState();
                if (gs == null) {
                    return Mono.just(errorResult("No game state"));
                }
                return Mono.just(successResult(JsonUtil.toJson(gs)));
            });

        Map<String, Object> keyProp = new HashMap<>();
        keyProp.put("type", "string");
        keyProp.put("description", "Key to press (W, A, S, D, Q, E, F, SPACE, SHIFT, CLICK)");
        keyProp.put("required", true);

        Map<String, Object> actionProp = new HashMap<>();
        actionProp.put("type", "string");
        actionProp.put("description", "Action: press, release, or click");
        actionProp.put("default", "click");

        addTool("send_key",
            "Simulate a key press",
            Map.of("key", keyProp, "action", actionProp),
            (exchange, args) -> {
                String key = ((String) args.get("key")).toUpperCase();
                String action = args.containsKey("action") ? ((String) args.get("action")).toLowerCase() : "click";

                switch (key) {
                    case "W" -> simulateKey(KeyCode.W, action);
                    case "A" -> simulateKey(KeyCode.A, action);
                    case "S" -> simulateKey(KeyCode.S, action);
                    case "D" -> simulateKey(KeyCode.D, action);
                    case "SPACE" -> simulateKey(KeyCode.SPACE, action);
                    case "SHIFT" -> simulateKey(KeyCode.SHIFT, action);
                    case "Q" -> {
                        inputHandler.triggerSwapWeapon();
                        javafx.application.Platform.runLater(() ->
                            gameClient.update(inputHandler, canvas.getGraphicsContext2D()));
                    }
                    case "E" -> {
                        inputHandler.triggerSkillSlot0();
                        javafx.application.Platform.runLater(() ->
                            gameClient.update(inputHandler, canvas.getGraphicsContext2D()));
                    }
                    case "F" -> {
                        inputHandler.triggerSkillSlot1();
                        javafx.application.Platform.runLater(() ->
                            gameClient.update(inputHandler, canvas.getGraphicsContext2D()));
                    }
                    case "CLICK" -> {
                        inputHandler.triggerShoot();
                        javafx.application.Platform.runLater(() ->
                            gameClient.update(inputHandler, canvas.getGraphicsContext2D()));
                    }
                    default -> {
                        return Mono.just(errorResult("Unknown key: " + key));
                    }
                }

                return Mono.just(successResult("Key " + key + " " + action));
            });
    }

    private void addTool(String name, String description,
                         Map<String, Object> properties,
                         java.util.function.BiFunction<McpAsyncServerExchange, Map<String, Object>, Mono<McpSchema.CallToolResult>> handler) {
        McpSchema.Tool tool = toolDef(name, description, properties);
        server.addTool(new McpServerFeatures.AsyncToolSpecification(tool, handler)).subscribe();
    }

    private void simulateKey(KeyCode code, String action) {
        javafx.event.EventType<javafx.scene.input.KeyEvent> type;

        switch (action) {
            case "press" -> type = javafx.scene.input.KeyEvent.KEY_PRESSED;
            case "release" -> type = javafx.scene.input.KeyEvent.KEY_RELEASED;
            default -> {
                simulateKey(code, "press");
                simulateKey(code, "release");
                return;
            }
        }

        javafx.scene.input.KeyEvent event = new javafx.scene.input.KeyEvent(
            type, code.getChar(), code.getName(), code, false, false, false, false);
        if (canvas.getScene() != null) {
            javafx.application.Platform.runLater(() ->
                canvas.getScene().getRoot().fireEvent(event));
        }
    }

    private String captureScreenshot() {
        if (canvas == null) return null;
        try {
            WritableImage snapshot = canvas.snapshot(null, null);
            PixelReader reader = snapshot.getPixelReader();
            int w = (int) snapshot.getWidth();
            int h = (int) snapshot.getHeight();

            byte[] buffer = new byte[w * h * 4];
            reader.getPixels(0, 0, w, h,
                javafx.scene.image.PixelFormat.getByteBgraInstance(), buffer, 0, w * 4);

            JsonObject imgInfo = new JsonObject();
            imgInfo.addProperty("width", w);
            imgInfo.addProperty("height", h);
            imgInfo.addProperty("pixels_base64", Base64.getEncoder().encodeToString(buffer));
            imgInfo.addProperty("format", "BGRA_8888");
            imgInfo.addProperty("note", "Each pixel is 4 bytes: B, G, R, A. Total size: " + buffer.length + " bytes");

            return gson.toJson(imgInfo);
        } catch (Exception e) {
            System.err.println("[CLIENT-MCP] Screenshot error: " + e.getMessage());
            return null;
        }
    }

    private static McpSchema.CallToolResult successResult(String text) {
        return new McpSchema.CallToolResult(
            List.of(new McpSchema.TextContent(text)), false);
    }

    private static McpSchema.CallToolResult errorResult(String text) {
        return new McpSchema.CallToolResult(
            List.of(new McpSchema.TextContent("ERROR: " + text)), true);
    }
}
