package io.hyozen.redis.handler;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface JsonHandler {
    void handle(JsonObject message) throws Exception;
}
