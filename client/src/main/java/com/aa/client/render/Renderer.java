package com.aa.client.render;

import com.aa.client.asset.SpriteManager;
import com.aa.shared.model.Bullet;
import com.aa.shared.model.Obstacle;
import com.aa.shared.model.Player;
import com.aa.shared.state.GameState;
import java.util.List;
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

    public void render(GraphicsContext gc, GameState state, String localPlayerId, double mouseScreenX, double mouseScreenY) {
        double cw = gc.getCanvas().getWidth();
        double ch = gc.getCanvas().getHeight();
        System.out.println("[RENDER] canvas=" + cw + "x" + ch + " state=" + (state != null ? state.getTick() : "null"));

        // Fondo: DARKSLATEGRAY sólido primero
        gc.setFill(Color.DARKSLATEGRAY);
        gc.fillRect(0, 0, cw, ch);

        if (state == null) {
            // Debug: mostrar texto "NO STATE" en rojo
            gc.setFill(Color.RED);
            gc.setFont(Font.font(24));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("NO STATE", cw/2, ch/2);
            return;
        }

        // Dibujar grid de fondo (mundo)
        drawGrid(gc);

        // Dibujar obstáculos del mapa
        drawObstacles(gc, state.getObstacles());

        // Dibujar jugadores
        for (Player p : state.getAllPlayers()) {
            boolean isLocal = p.getId().equals(localPlayerId);
            drawPlayer(gc, p, isLocal);
        }

        // Dibujar balas
        for (Bullet b : state.getAllBullets()) {
            drawBullet(gc, b);
        }
        // Crosshair
        drawCrosshair(gc, mouseScreenX, mouseScreenY);
        // HUD
        drawHud(gc, state, localPlayerId);
    }

    private void drawObstacles(GraphicsContext gc, List<Obstacle> obstacles) {
        if (obstacles == null) return;
        gc.setFill(Color.rgb(60, 60, 60));
        gc.setStroke(Color.rgb(80, 80, 80));
        gc.setLineWidth(2);
        for (Obstacle o : obstacles) {
            double sx = camera.worldToScreenX(o.x());
            double sy = camera.worldToScreenY(o.y());
            double sw = o.width();
            double sh = o.height();
            gc.fillRect(sx, sy, sw, sh);
            gc.strokeRect(sx, sy, sw, sh);
        }
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
    private void drawCrosshair(GraphicsContext gc, double mx, double my) {
        Image crosshair = SpriteManager.getCrosshair();
        if (crosshair != null) {
            gc.drawImage(crosshair, mx - 16, my - 16, 32, 32);
        } else {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeLine(mx - 10, my, mx + 10, my);
            gc.strokeLine(mx, my - 10, mx, my + 10);
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

        // Scoreboard (esquina superior derecha)
        double sbX = gc.getCanvas().getWidth() - 180;
        double sbY = 10;
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(sbX - 5, sbY - 5, 175, 20 + state.getAllPlayers().size() * 18);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Jugador", sbX, sbY + 12);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("K/D", sbX + 160, sbY + 12);

        int i = 1;
        java.util.List<Player> sorted = new java.util.ArrayList<>(state.getAllPlayers());
        sorted.sort(java.util.Comparator.comparingInt(Player::getKills).reversed());
        for (Player p : sorted) {
            double y = sbY + 12 + i * 18;
            boolean isLocalP = p.getId().equals(localPlayerId);
            gc.setFill(isLocalP ? Color.YELLOW : Color.LIGHTGRAY);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(p.getUsername(), sbX, y);
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText(p.getKills() + "/" + p.getDeaths(), sbX + 160, y);
            i++;
        }
    }
}