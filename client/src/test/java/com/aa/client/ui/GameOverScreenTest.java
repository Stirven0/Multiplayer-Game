package com.aa.client.ui;

import com.aa.client.game.GameClient;
import com.aa.shared.message.GameEndMessage;
import com.aa.shared.message.GameEndMessage.PlayerScore;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
class GameOverScreenTest {

    @Start
    void start(Stage stage) {
        GameClient gameClient = mock(GameClient.class);
        when(gameClient.getScreenManager()).thenReturn(mock(ScreenManager.class));

        List<PlayerScore> scores = List.of(
            new PlayerScore("p1", "Player 1", 5, 1, true),
            new PlayerScore("p2", "Player 2", 2, 3, false)
        );
        GameEndMessage msg = new GameEndMessage("game1", "p1", "Player 1", scores, 120_000);
        Scene scene = new GameOverScreen(gameClient, msg).createScene(stage);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void shouldShowTitle() {
        verifyThat("GAME OVER", Node::isVisible);
    }

    @Test
    void shouldShowWinner() {
        verifyThat("Ganador: Player 1", Node::isVisible);
    }

    @Test
    void shouldShowScoreboardTitle() {
        verifyThat("SCOREBOARD", Node::isVisible);
    }

    @Test
    void shouldShowPlayerNames() {
        verifyThat("Player 1", Node::isVisible);
        verifyThat("Player 2", Node::isVisible);
    }

    @Test
    void shouldShowBackButton() {
        verifyThat("Volver al Lobby", Node::isVisible);
    }

    @Test
    void shouldCreateDrawSceneWithoutError(FxRobot robot) {
        robot.interact(() -> {
            GameClient gc = mock(GameClient.class);
            when(gc.getScreenManager()).thenReturn(mock(ScreenManager.class));
            List<PlayerScore> drawScores = List.of(
                new PlayerScore("p1", "Player 1", 1, 1, false),
                new PlayerScore("p2", "Player 2", 1, 1, false)
            );
            GameEndMessage msg = new GameEndMessage("game2", "", "Empate", drawScores, 60_000);
            assertDoesNotThrow(() -> new GameOverScreen(gc, msg).createScene(new Stage()));
        });
    }
}
