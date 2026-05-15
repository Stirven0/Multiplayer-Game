package com.aa.client.ui;

import com.aa.client.game.GameClient;
import com.aa.client.util.ClientConfig;
import com.aa.shared.message.GameEndMessage;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameOverScreen {

    private final GameClient gameClient;
    private final GameEndMessage endMsg;

    public GameOverScreen(GameClient gameClient, GameEndMessage endMsg) {
        this.gameClient = gameClient;
        this.endMsg = endMsg;
    }

    public Scene createScene(Stage stage) {
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("Game Over");
        title.setStyle("-fx-text-fill: #e94560; -fx-font-size: 36px; -fx-font-weight: bold;");

        boolean isDraw = endMsg.getWinnerUsername() == null || endMsg.getWinnerUsername().isEmpty() || "Empate".equals(endMsg.getWinnerUsername());
        Label winner = new Label(isDraw ? "Empate!" : "Ganador: " + endMsg.getWinnerUsername());
        winner.setStyle(isDraw ? "-fx-text-fill: #aaa; -fx-font-size: 20px;" : "-fx-text-fill: gold; -fx-font-size: 20px;");

        long seconds = endMsg.getDuration() / 1000;
        Label duration = new Label("Duracion: " + seconds + "s");
        duration.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");

        VBox scoresBox = new VBox(5);
        scoresBox.setAlignment(Pos.CENTER);
        scoresBox.setStyle("-fx-padding: 20; -fx-background-color: #16213e;");
        Label scoresTitle = new Label("Scoreboard");
        scoresTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        scoresBox.getChildren().add(scoresTitle);

        if (endMsg.getScores() != null) {
            for (GameEndMessage.PlayerScore score : endMsg.getScores()) {
                String prefix = score.isWinner() ? "[WINNER] " : "";
                Label scoreLabel = new Label(String.format(
                    "%s%s - Kills: %d  Deaths: %d",
                    prefix, score.getUsername(), score.getKills(), score.getDeaths()
                ));
                scoreLabel.setStyle(score.isWinner()
                    ? "-fx-text-fill: gold; -fx-font-size: 14px;"
                    : "-fx-text-fill: #ccc; -fx-font-size: 14px;");
                scoresBox.getChildren().add(scoreLabel);
            }
        }

        Button backBtn = new Button("Volver al Lobby");
        backBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        backBtn.setOnAction(e -> gameClient.getScreenManager().showLobby());

        content.getChildren().addAll(title, winner, duration, scoresBox, backBtn);

        BorderPane root = new BorderPane();
        root.setTop(TitleBar.create("Game Over", stage));
        root.setCenter(content);

        return new Scene(root, ClientConfig.WIDTH, ClientConfig.HEIGHT + TitleBar.HEIGHT);
    }
}
