package io.hyozen.redis.handler;

import java.util.Map;

public interface RedisChannelRegistrar {
    void registerHandlers(
            Map<String, StringHandler> stringHandlers,
            Map<String, JsonHandler> jsonHandlers
    );
}
