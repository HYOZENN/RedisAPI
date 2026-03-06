package io.hyozen.redis.subscriber;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.hyozen.redis.handler.HandlerRegistry;
import io.hyozen.redis.handler.JsonHandler;
import io.hyozen.redis.handler.StringHandler;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPubSub;

public class RedisSubscriber extends JedisPubSub {

    private final HandlerRegistry registry;
    private final Logger logger;

    public RedisSubscriber(HandlerRegistry registry, Logger logger) {
        this.registry = registry;
        this.logger = logger;
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        try {
            if (message != null && message.trim().startsWith("{")) {
                JsonHandler jsonHandler = registry.getJsonHandler(channel);
                if (jsonHandler != null) {
                    JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                    jsonHandler.handle(json);
                    return;
                }
            }
            StringHandler stringHandler = registry.getStringHandler(channel);
            if (stringHandler != null) {
                stringHandler.handle(message);
            }
        } catch (Exception e) {
            logger.error("Handler error for channel {}: {}", channel, e.getMessage(), e);
        }
    }

    public void shutdown() {
        if (isSubscribed()) {
            unsubscribe();
        }
    }
}
