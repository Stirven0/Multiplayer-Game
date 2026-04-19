package com.aa.server.auth;

/**
 * Datos de sesión persistente durante la conexión.
 */
public record PlayerSession(String userId, String username, String token) {}
