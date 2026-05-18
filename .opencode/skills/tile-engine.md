# Tile Engine Skill

## Description
Working with Tiled .tmj maps, tile rendering, solid tile collision extraction, and map loading.

## When to use
- Creating or modifying Tiled .tmj map files
- Changing tile rendering or placeholder colors
- Adding new solid tile types or collision logic
- Debugging map loading or tile culling

## Tile Engine Architecture

- Maps stored in Tiled JSON format (.tmj) in `server/src/main/resources/maps/`
- `TiledMapLoader.java` parses .tmj with Gson, extracts obstacles via horizontal run merging
- `TileMap` / `TileLayer` / `TileSet` / `TileObject` in shared module
- `TileRenderer.java` in client: viewport-culled rendering, TileColors placeholders
- `GameState.tileMap` sent to client each tick for rendering
- `MapManager` tries TMJ first, falls back to legacy JSON

## Steps for adding a new TMJ map

### 1. Create map file
Place `map_XX.tmj` in `server/src/main/resources/maps/`.

### 2. Register in `MapManager.java`
```java
register(TiledMapLoader.loadFromTmj("/maps/map_XX.tmj"));
```

### 3. Define tile properties
In the TMJ tileset, mark solid tiles with:
```json
"properties": [{"name": "solid", "type": "bool", "value": true}]
```

### 4. Add placeholder color in `TileColors.java`
```java
Map.entry(tileId, Color.rgb(R, G, B))
```

## Testing tile maps
```bash
mvn clean install -DskipTests && mvn test -pl server
```
The TMJ loads at startup; check logs for "No TMJ map found" to debug loading issues.
