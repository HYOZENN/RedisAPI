package io.hyozen.redis.handler;

import java.util.HashMap;
import java.util.Map;

public class HandlerRegistry {

    private final Map<String, StringHandler> stringHandlers = new HashMap<>();
    private final Map<String, JsonHandler> jsonHandlers = new HashMap<>();

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
