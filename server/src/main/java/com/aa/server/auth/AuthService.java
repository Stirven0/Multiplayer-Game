package com.aa.server.auth;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Auth básica en memoria. En producción: BD + hash de passwords.
 */
public class AuthService {
    private final ConcurrentHashMap<String, String> passwords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<>();

    public AuthService() {
        // Usuarios de prueba
        register("player1", "pass1");
        register("player2", "pass2");
        register("player3", "pass3");
    }

    public void register(String username, String password) {
        passwords.put(username, password);
        userIds.put(username, UUID.randomUUID().toString());
    }

    public String login(String username, String password) {
        if (password.equals(passwords.get(username))) {
            String token = UUID.randomUUID().toString();
            String uid = userIds.get(username);
            tokens.put(token, uid);
            return token;
        }
        return null;
    }

    public String getUserId(String token) {
        return tokens.get(token);
    }

    public boolean validateToken(String token) {
        return tokens.containsKey(token);
    }
}
