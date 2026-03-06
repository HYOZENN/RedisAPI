package io.hyozen.redis.handler;

import java.util.Map;

/**
 * Callback to register pub/sub channel handlers. Implementations add entries to the given maps:
 * channel name -> handler for raw string messages, or channel name -> handler for JSON messages.
 * <p>
 * Messages that look like JSON (e.g. start with "{") are dispatched to the JSON handler for that channel
 * if present; otherwise the string handler is used. Both maps are thread-safe.
 * </p>
 */
public interface RedisChannelRegistrar {
    /**
     * Registers handlers by putting them into the provided maps.
     *
     * @param stringHandlers map of channel name -> handler for string messages
     * @param jsonHandlers   map of channel name -> handler for JSON messages (JsonObject)
     */
    void registerHandlers(
            Map<String, StringHandler> stringHandlers,
            Map<String, JsonHandler> jsonHandlers
    );
}
