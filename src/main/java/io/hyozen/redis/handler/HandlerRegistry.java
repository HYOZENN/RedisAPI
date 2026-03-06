package io.hyozen.redis.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry of channel handlers for pub/sub message dispatch.
 * Safe to call {@link #register(RedisChannelRegistrar)} and {@link #getStringHandler(String)} / {@link #getJsonHandler(String)} from different threads.
 */
public class HandlerRegistry {

    private final Map<String, StringHandler> stringHandlers = new ConcurrentHashMap<>();
    private final Map<String, JsonHandler> jsonHandlers = new ConcurrentHashMap<>();

    public void register(RedisChannelRegistrar registrar) {
        registrar.registerHandlers(stringHandlers, jsonHandlers);
    }

    public StringHandler getStringHandler(String channel) {
        return stringHandlers.get(channel);
    }

    public JsonHandler getJsonHandler(String channel) {
        return jsonHandlers.get(channel);
    }
}
