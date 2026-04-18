package com.game.shared.util;

import com.game.shared.message.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Utilidad para serialización/deserialización JSON.
 * Configurada para manejar la jerarquía de mensajes correctamente.
 */
public class JsonUtil {
    
    private static final Gson gson;
    
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting(); // Para debug, quitar en producción
        builder.serializeNulls();    // Explicitamente incluir nulls
        builder.registerTypeAdapter(Message.class, new MessageAdapter());
        builder.registerTypeAdapter(Vector2.class, new Vector2Adapter());
        gson = builder.create();
    }
    
    // ==================== SERIALIZACIÓN ====================
    
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    public static String toJson(Object obj, Type type) {
        return gson.toJson(obj, type);
    }
    
    // ==================== DESERIALIZACIÓN ====================
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
    
    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }
    
    /**
     * Deserializa un mensaje genérico (útil cuando no sabes el tipo).
     * Retorna la subclase correcta según el campo "type".
     */
    public static Message parseMessage(String json) {
        return gson.fromJson(json, Message.class);
    }
    
    /**
     * Deserializa con tipo específico para mejor performance.
     */
    public static MoveMessage parseMoveMessage(String json) {
        return gson.fromJson(json, MoveMessage.class);
    }
    
    public static GameStateMessage parseGameStateMessage(String json) {
        return gson.fromJson(json, GameStateMessage.class);
    }
    
    // ==================== ADAPTADORES PERSONALIZADOS ====================
    
    /**
     * Adaptador para deserializar Message según su tipo.
     */
    private static class MessageAdapter implements JsonSerializer<Message>, JsonDeserializer<Message> {
        
        @Override
        public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = context.serialize(src, src.getClass()).getAsJsonObject();
            // Asegurar que el tipo esté presente
            if (!result.has("type")) {
                result.addProperty("type", src.getType().name());
            }
            return result;
        }
        
        @Override
        public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            
            if (!jsonObject.has("type")) {
                throw new JsonParseException("Message sin campo 'type'");
            }
            
            String typeStr = jsonObject.get("type").getAsString();
            MessageType type;
            try {
                type = MessageType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Tipo de mensaje desconocido: " + typeStr);
            }
            
            // Deserializar según el tipo específico
            Class<? extends Message> targetClass = getTargetClass(type);
            return context.deserialize(json, targetClass);
        }
        
        private Class<? extends Message> getTargetClass(MessageType type) {
            return switch (type) {
                case MOVE_INPUT -> MoveMessage.class;
                case SHOOT_INPUT -> ShootMessage.class;
                case GAME_STATE -> GameStateMessage.class;
                case LOGIN_REQUEST, LOGIN_RESPONSE -> LoginMessage.class;
                case ERROR -> ErrorMessage.class;
                default -> Message.class; // Fallback, puede necesitar ajuste
            };
        }
    }
    
    /**
     * Adaptador para Vector2 (más compacto que objeto con fields).
     */
    private static class Vector2Adapter implements JsonSerializer<com.game.shared.model.Vector2>, 
                                                   JsonDeserializer<com.game.shared.model.Vector2> {
        
        @Override
        public JsonElement serialize(com.game.shared.model.Vector2 src, Type typeOfSrc, 
                                    JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            array.add(src.x());
            array.add(src.y());
            return array;
        }
        
        @Override
        public com.game.shared.model.Vector2 deserialize(JsonElement json, Type typeOfT,
                                                        JsonDeserializationContext context) 
                throws JsonParseException {
            JsonArray array = json.getAsJsonArray();
            double x = array.get(0).getAsDouble();
            double y = array.get(1).getAsDouble();
            return new com.game.shared.model.Vector2(x, y);
        }
    }
    
    // ==================== UTILIDADES ADICIONALES ====================
    
    /**
     * Crea un JsonObject para inspección manual.
     */
    public static JsonObject parseToObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
    
    /**
     * Extrae un campo específico sin parsear todo el objeto.
     */
    public static String extractField(String json, String fieldName) {
        JsonObject obj = parseToObject(json);
        return obj.has(fieldName) ? obj.get(fieldName).getAsString() : null;
    }
}
