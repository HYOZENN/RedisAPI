package io.hyozen.redis.handler;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandlerRegistryTest {

    @Test
    void registerAndGetStringHandler() {
        HandlerRegistry registry = new HandlerRegistry();
        registry.register((stringHandlers, jsonHandlers) -> {
            stringHandlers.put("messaging:test", msg -> assertEquals("hello", msg));
        });
        StringHandler h = registry.getStringHandler("messaging:test");
        assertNotNull(h);
        assertDoesNotThrow(() -> h.handle("hello"));
        assertNull(registry.getStringHandler("unknown"));
    }

    @Test
    void registerAndGetJsonHandler() {
        HandlerRegistry registry = new HandlerRegistry();
        registry.register((stringHandlers, jsonHandlers) -> {
            jsonHandlers.put("messaging:json", json -> assertEquals("v", json.get("k").getAsString()));
        });
        JsonHandler h = registry.getJsonHandler("messaging:json");
        assertNotNull(h);
        JsonObject obj = new JsonObject();
        obj.addProperty("k", "v");
        assertDoesNotThrow(() -> h.handle(obj));
        assertNull(registry.getJsonHandler("unknown"));
    }
}
