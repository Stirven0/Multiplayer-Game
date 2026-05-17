package com.aa.client.ui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.testfx.api.FxAssert.verifyThat;

/**
 * Pruebas de UI para la barra de título personalizada (TitleBar).
 */
@ExtendWith(ApplicationExtension.class)
class TitleBarTest {

    private Stage stageRef;

    @Start
    void start(Stage stage) {
        stageRef = stage;
        HBox titleBar = TitleBar.create("Test Title", stage);
        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        stage.setScene(new Scene(root, 400, 100));
        stage.show();
    }

    @Test
    void shouldShowTitle() {
        verifyThat("Test Title", Node::isVisible);
    }

    @Test
    void shouldHaveCloseButton() {
        verifyThat("✕", Node::isVisible);
    }

    @Test
    void shouldNotHaveMinimizeByDefault(FxRobot robot) {
        // Por defecto no se crea el botón de minimizar
        assertFalse(robot.lookup("─").tryQuery().isPresent());
    }

    @Test
    void shouldShowMinimizeWhenRequested(FxRobot robot) {
        robot.interact(() -> {
            HBox titleBar = TitleBar.create("Min Test", stageRef, true);
            BorderPane root = new BorderPane();
            root.setTop(titleBar);
            stageRef.setScene(new Scene(root, 400, 100));
        });
        verifyThat("─", Node::isVisible);
    }

    @Test
    void shouldShowTitleInScene() {
        verifyThat("Test Title", n -> n.getParent() != null);
    }
}
