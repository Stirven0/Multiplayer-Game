package com.aa.server.auth;

import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthService {
    private final ConcurrentHashMap<String, String> passwordHashes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public AuthService() {
        // Limpieza periódica de tokens expirados (cada 30 minutos)
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::cleanExpiredTokens, 30, 30, TimeUnit.MINUTES);
        // Usuarios de prueba
        register("player1", "pass1");
        register("player2", "pass2");
    }

    public boolean register(String username, String password) {
        if (passwordHashes.containsKey(username)) return false;
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        passwordHashes.put(username, hashed);
        userIds.put(username, UUID.randomUUID().toString());
        return true;
    }

    public String login(String username, String password) {
        String hash = passwordHashes.get(username);
        if (hash != null && BCrypt.checkpw(password, hash)) {
            String token = UUID.randomUUID().toString();
            tokens.put(token, new TokenInfo(userIds.get(username), System.currentTimeMillis()));
            return token;
        }
        return null;
    }

    public String getUserId(String token) {
        TokenInfo info = tokens.get(token);
        if (info != null && !isExpired(info)) return info.userId();
        tokens.remove(token);
        return null;
    }

    public boolean validateToken(String token) {
        TokenInfo info = tokens.get(token);
        return info != null && !isExpired(info);
    }

    public void logout(String token) {
        tokens.remove(token);
    }

    private boolean isExpired(TokenInfo info) {
        // Tokens expiran después de 2 horas
        return System.currentTimeMillis() - info.createdAt() > TimeUnit.HOURS.toMillis(2);
    }

    private void cleanExpiredTokens() {
        tokens.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private record TokenInfo(String userId, long createdAt) {}
}