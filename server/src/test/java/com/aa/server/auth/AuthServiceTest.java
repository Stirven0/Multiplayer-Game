package com.aa.server.auth;

import com.aa.server.db.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeAll
    static void initDb() {
        DatabaseManager.initForTest();
    }

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    @DisplayName("Debe autenticar con credenciales válidas y retornar token")
    void loginWithValidCredentialsReturnsToken() {
        String token = authService.login("player1", "pass1");
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(authService.validateToken(token));
    }

    @Test
    @DisplayName("Debe rechazar credenciales inválidas")
    void loginWithInvalidCredentialsReturnsNull() {
        assertNull(authService.login("player1", "wrongpass"));
    }

    @Test
    @DisplayName("Debe obtener userId a partir de token válido")
    void getUserIdWithValidToken() {
        String token = authService.login("player1", "pass1");
        String userId = authService.getUserId(token);
        assertNotNull(userId);
    }

    @Test
    @DisplayName("Debe retornar null para token inexistente")
    void getUserIdWithInvalidTokenReturnsNull() {
        assertNull(authService.getUserId("fake-token-123"));
    }

    @Test
    @DisplayName("Debe permitir registrar nuevo usuario y luego loguear")
    void registerAndLoginNewUser() {
        authService.register("newplayer", "newpass");
        String token = authService.login("newplayer", "newpass");
        assertNotNull(token);
        assertTrue(authService.validateToken(token));
    }
}
