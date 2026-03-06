package io.hyozen.redis.api;

import io.hyozen.redis.config.RedisConfig;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@Tag("integration")
class RedisServiceIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static RedisConfig config;

    @BeforeAll
    static void beforeAll() {
        config = RedisConfig.builder()
                .host(redis.getHost())
                .port(redis.getFirstMappedPort())
                .subscribePatterns(RedisConfig.DEFAULT_PATTERNS)
                .build();
    }

    @Test
    @DisplayName("Connect, publish, register handler, receive message, use cache, disconnect")
    void fullCycle() throws InterruptedException {
        RedisService service = new RedisServiceImpl();
        service.connect(config);
        assertTrue(service.isConnected());

        // Cache
        var cache = service.createCache("messaging:test:", 60L);
        cache.set("key1", "value1");
        assertEquals(Optional.of("value1"), cache.get("key1"));
        assertTrue(cache.exists("key1"));
        cache.delete("key1");
        assertEquals(Optional.empty(), cache.get("key1"));

        // Pub/sub: register handler and wait for message
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();
        service.registerChannelHandler((stringHandlers, jsonHandlers) -> {
            stringHandlers.put("messaging:echo", msg -> {
                received.set(msg);
                latch.countDown();
            });
        });
        service.publish("messaging:echo", "hello-from-test");
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Should receive message");
        assertEquals("hello-from-test", received.get());

        service.disconnect();
        assertFalse(service.isConnected());
    }

    @Test
    @DisplayName("getCache returns same default cache instance")
    void getCacheReturnsSameInstance() {
        RedisService service = new RedisServiceImpl();
        service.connect(config);
        var cache1 = service.getCache();
        var cache2 = service.getCache();
        assertSame(cache1, cache2);
        service.disconnect();
    }
}
