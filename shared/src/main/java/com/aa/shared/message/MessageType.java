package com.aa.shared.message;

/**
 * Tipos de mensajes soportados en el protocolo.
 * Separados por categoría para claridad.
 */
public enum MessageType {
    // Autenticación
    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    LOGOUT,
    
    // Lobby/Salas
    CREATE_ROOM,
    JOIN_ROOM,
    LEAVE_ROOM,
    ROOM_LIST,
    ROOM_LIST_RESPONSE,
    ROOM_CREATED,
    ROOM_UPDATED,
    ROOM_UPDATE,
    JOIN_ROOM_RESPONSE,
    GAME_START,
    
    // Inputs del jugador (Cliente → Servidor)
    MOVE_INPUT,      // Movimiento continuo
    SHOOT_INPUT,     // Disparo
    ROTATE_INPUT,    // Cambio de dirección
    USE_ABILITY,     // Habilidad especial (futuro)
    
    // Estado del juego (Servidor → Cliente)
    GAME_STATE,      // Snapshot completo
    DELTA_STATE,     // Cambios parciales (futuro)
    ENTITY_SPAWN,    // Nueva entidad creada
    ENTITY_DESTROY,  // Entidad eliminada
    PLAYER_HIT,      // Jugador dañado
    PLAYER_DEATH,    // Jugador muerto
    GAME_END,        // Partida terminada con resultados
    
    // Sistema
    PING,            // Latencia
    PONG,
    ERROR,           // Error genérico
    DISCONNECT,      // Desconexión forzada
    RECONNECT        // Reconexión después de timeout
}
