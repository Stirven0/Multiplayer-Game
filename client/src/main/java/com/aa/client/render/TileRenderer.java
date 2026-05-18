package com.aa.client.render;

import com.aa.client.render.Camera;
import com.aa.shared.model.TileLayer;
import com.aa.shared.model.TileMap;
import javafx.scene.canvas.GraphicsContext;

public class TileRenderer {

    private final Camera camera;

    public TileRenderer(Camera camera) {
        this.camera = camera;
    }

    public void render(GraphicsContext gc, TileMap tileMap, double canvasWidth, double canvasHeight) {
        if (tileMap == null) return;

        int tileW = tileMap.getTileWidth();
        int tileH = tileMap.getTileHeight();

        int startCol = (int) (camera.getX() / tileW);
        int startRow = (int) (camera.getY() / tileH);
        int endCol = startCol + (int) (canvasWidth / tileW) + 2;
        int endRow = startRow + (int) (canvasHeight / tileH) + 2;

        for (TileLayer layer : tileMap.getLayers()) {
            if (!layer.isVisible()) continue;
            if (!"tilelayer".equals(layer.getType())) continue;

            int[][] data = layer.getData();
            if (data == null) continue;

            int rows = Math.min(data.length, tileMap.getHeight());
            int cols = data.length > 0 ? Math.min(data[0].length, tileMap.getWidth()) : 0;

            for (int row = Math.max(0, startRow); row <= Math.min(endRow, rows - 1); row++) {
                for (int col = Math.max(0, startCol); col <= Math.min(endCol, cols - 1); col++) {
                    int tileId = data[row][col];
                    if (tileId == 0) continue;

                    double sx = camera.worldToScreenX(col * tileW);
                    double sy = camera.worldToScreenY(row * tileH);

                    gc.setFill(TileColors.getColor(tileId));
                    gc.fillRect(sx, sy, tileW + 1, tileH + 1);

                    if (isWall(tileId)) {
                        gc.setStroke(javafx.scene.paint.Color.rgb(60, 68, 76));
                        gc.setLineWidth(0.5);
                        gc.strokeRect(sx, sy, tileW, tileH);
                    }
                }
            }
        }
    }

    private boolean isWall(int tileId) {
        return tileId == 2 || tileId == 3;
    }
}
