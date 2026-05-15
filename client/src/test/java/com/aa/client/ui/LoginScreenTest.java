package com.aa.client.ui;

import com.aa.client.game.GameClient;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
class LoginScreenTest {

    private LoginScreen loginScreen;

    @Start
    void start(Stage stage) {
        GameClient gameClient = mock(GameClient.class);
        when(gameClient.getScreenManager()).thenReturn(mock(ScreenManager.class));
        loginScreen = new LoginScreen(gameClient);
        stage.setScene(loginScreen.createScene(stage));
        stage.show();
    }

    @Test
    void shouldShowTitle(FxRobot robot) {
        assertTrue(robot.lookup(".label").tryQuery().isPresent());
    }

    @Test
    void shouldHaveUsernameField() {
        verifyThat(".text-field", Node::isVisible);
    }

    @Test
    void shouldHaveLoginButton() {
        verifyThat("Connect & Login", Node::isVisible);
    }

    @Test
    void shouldHaveToggleLink(FxRobot robot) {
        assertTrue(robot.lookup(n -> n instanceof Hyperlink).tryQuery().isPresent());
    }

    @Test
    void shouldToggleMode(FxRobot robot) {
        robot.lookup(n -> n instanceof Hyperlink).tryQuery().ifPresent(n ->
            robot.interact(() -> {
                if (n instanceof Hyperlink h) h.fire();
            })
        );
        robot.sleep(100);
        verifyThat("Register & Login", Node::isVisible);
    }

    @Test
    void shouldShowHelpOverlay(FxRobot robot) {
        robot.clickOn("Ayuda");
        robot.sleep(100);
        verifyThat("AYUDA - CONTROLES", Node::isVisible);
    }

    @Test
    void shouldShowError(FxRobot robot) {
        loginScreen.setError("Error de prueba");
        robot.sleep(200);
        verifyThat("Error de prueba", Node::isVisible);
    }

    @Test
    void shouldHaveExitButton() {
        verifyThat("Salir", Node::isVisible);
    }

    @Test
    void shouldHaveHelpButton() {
        verifyThat("Ayuda", Node::isVisible);
    }
}
