package com.aa.server.auth;

import com.aa.server.db.DatabaseManager;
import com.aa.server.db.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthService {
    private final UserRepository userRepo = new UserRepository();
    private final ConcurrentHashMap<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public AuthService() {
        DatabaseManager.init();
        register("player1", "pass1");
        register("player2", "pass2");
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(this::cleanExpiredTokens, 30, 30, TimeUnit.MINUTES);
    }

    public boolean register(String username, String password) {
        if (userRepo.userExists(username)) return false;
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        String userId = UUID.randomUUID().toString();
        return userRepo.createUser(username, hashed, userId);
    }

    public String login(String username, String password) {
        String hash = userRepo.getPasswordHash(username);
        if (hash != null && BCrypt.checkpw(password, hash)) {
            String token = UUID.randomUUID().toString();
            String userId = userRepo.getUserId(username);
            if (userId == null) return null;
            tokens.put(token, new TokenInfo(userId, System.currentTimeMillis()));
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
        return System.currentTimeMillis() - info.createdAt() > TimeUnit.HOURS.toMillis(2);
    }

    private void cleanExpiredTokens() {
        tokens.entrySet().removeIf(e -> isExpired(e.getValue()));
    }

    private record TokenInfo(String userId, long createdAt) {}
}
