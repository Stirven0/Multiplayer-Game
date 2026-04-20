package com.aa.server.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    @DisplayName("Debe permitir la primera acción siempre")
    void firstActionAlwaysAllowed() {
        RateLimiter limiter = new RateLimiter(1000);
        assertTrue(limiter.tryAcquire());
    }

    @Test
    @DisplayName("Debe bloquear acciones dentro del intervalo")
    void blocksActionsWithinInterval() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(200); // 200ms
        
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
    }

    @Test
    @DisplayName("Debe permitir acción después del intervalo")
    void allowsActionAfterInterval() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(50); // 50ms para test rápido
        
        assertTrue(limiter.tryAcquire());
        Thread.sleep(60);
        assertTrue(limiter.tryAcquire());
    }

    @Test
    @DisplayName("Debe ser thread-safe bajo alta contención")
    void threadSafeUnderContention() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(10_000); // Intervalo largo, solo 1 debe pasar
        int[] allowed = {0};

        Thread t1 = new Thread(() -> { if (limiter.tryAcquire()) allowed[0]++; });
        Thread t2 = new Thread(() -> { if (limiter.tryAcquire()) allowed[0]++; });
        Thread t3 = new Thread(() -> { if (limiter.tryAcquire()) allowed[0]++; });

        t1.start(); t2.start(); t3.start();
        t1.join(); t2.join(); t3.join();

        assertEquals(1, allowed[0]);
    }
}
