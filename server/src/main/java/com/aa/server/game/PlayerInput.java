package com.aa.server.game;

import com.aa.shared.message.Message;
import com.aa.shared.message.MessageType;

/**
 * Input envuelto para pasar del hilo de red al GameLoop.
 */
public record PlayerInput(String playerId, MessageType type, Message message) {}
