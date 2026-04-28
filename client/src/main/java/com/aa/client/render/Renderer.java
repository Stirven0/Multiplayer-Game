package com.aa.client.render;

import com.aa.client.asset.SpriteManager;
import com.aa.shared.model.Bullet;
import com.aa.shared.model.Player;
import com.aa.shared.state.GameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class Renderer {
    private final Camera camera;

    public Renderer(Camera camera) {
        this.camera = camera;
    }

    public void render(GraphicsContext gc, GameState state, String localPlayerId) {
        // Limpiar
        gc.setFill(Color.DARKSLATEGRAY);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        if (state == null) return;

        // Dibujar grid de fondo (mundo)
        drawGrid(gc);

        // Dibujar jugadores
        for (Player p : state.getAllPlayers()) {
            boolean isLocal = p.getId().equals(localPlayerId);
            drawPlayer(gc, p, isLocal);
        }

        // Dibujar balas
        for (Bullet b : state.getAllBullets()) {
            drawBullet(gc, b);
        }
        // HUD
        drawHud(gc, state, localPlayerId);
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);
        double startX = -camera.getX() % 100;
        double startY = -camera.getY() % 100;
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();

        for (double x = startX; x < w; x += 100) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = startY; y < h; y += 100) {
            gc.strokeLine(0, y, w, y);
        }
    }

    private void drawPlayer(GraphicsContext gc, Player p, boolean isLocal) {
        double sx = camera.worldToScreenX(p.getPosition().x());
        double sy = camera.worldToScreenY(p.getPosition().y());
        double size = 32;

        Image sprite = SpriteManager.getPlayerSprite(isLocal, p.isAlive());
        if (sprite != null) {
            gc.drawImage(sprite, sx - size/2, sy - size/2, size, size);
        } else {
            gc.setFill(SpriteManager.getPlayerColor(isLocal));
            gc.fillOval(sx - size/2, sy - size/2, size, size);
        }

        // Dirección (línea)
        double dirLen = 20;
        double dx = p.getDirection().x() * dirLen;
        double dy = p.getDirection().y() * dirLen;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeLine(sx, sy, sx + dx, sy + dy);

        // Nombre
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(p.getUsername(), sx, sy - size);

        // HP bar
        if (p.isAlive()) {
            double barW = 30;
            double barH = 4;
            gc.setFill(Color.BLACK);
            gc.fillRect(sx - barW/2, sy + size/2 + 2, barW, barH);
            gc.setFill(p.getHealth() > 50 ? Color.LIMEGREEN : Color.ORANGERED);
            gc.fillRect(sx - barW/2, sy + size/2 + 2, barW * (p.getHealth()/100.0), barH);
        }
    }

    private void drawBullet(GraphicsContext gc, Bullet b) {
    double sx = camera.worldToScreenX(b.getPosition().x());
    double sy = camera.worldToScreenY(b.getPosition().y());
    
    Image sprite = SpriteManager.getBulletSprite();
    if (sprite != null) {
        gc.drawImage(sprite, sx - 4, sy - 4, 8, 8);
    } else {
        gc.setFill(SpriteManager.getBulletColor());
        gc.fillOval(sx - 3, sy - 3, 6, 6);
    }
}
    private void drawHud(GraphicsContext gc, GameState state, String localPlayerId) {
        Player local = state.getPlayer(localPlayerId);
        if (local == null) return;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Tick: " + state.getTick(), 10, 20);
        gc.fillText("HP: " + (int)local.getHealth(), 10, 40);
        gc.fillText("Players: " + state.getAllPlayers().size(), 10, 60);
    }
}