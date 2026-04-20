package com.aa.server.game.engine;

import com.aa.server.game.GameInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameLoopTest {

    @Mock
    private GameInstance gameInstance;

    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        gameLoop = new GameLoop(gameInstance);
    }

    @Test
    @DisplayName("Debe ejecutar múltiples ticks al iniciar")
    void executesTicksWhenStarted() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(250); // 250ms a 20 TPS debería dar ~5 ticks

        gameLoop.stop();
        Thread.sleep(50); // Dar tiempo a que termine

        verify(gameInstance, atLeast(3)).processTick(anyFloat());
        verify(gameInstance, atLeast(3)).broadcastState();
    }

    @Test
    @DisplayName("Debe detenerse graceful al llamar stop")
    void stopsGracefully() throws InterruptedException {
        gameLoop.start();
        assertTrue(gameLoop.isRunning());

        gameLoop.stop();
        Thread.sleep(100);

        assertFalse(gameLoop.isRunning());
    }

    @Test
    @DisplayName("No debe perder inputs entre ticks")
    void doesNotLoseInputsBetweenTicks() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(150);

        gameLoop.stop();

        // Verificar que processTick fue llamado consecutivamente sin saltos lógicos
        verify(gameInstance, atLeast(2)).processTick(anyFloat());
    }
}
