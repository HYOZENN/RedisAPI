package io.hyozen.redis.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RedisConfigTest {

    @Test
    void builderUsesDefaults() {
        RedisConfig config = RedisConfig.builder().build();
        assertEquals(RedisConfig.DEFAULT_HOST, config.getHost());
        assertEquals(RedisConfig.DEFAULT_PORT, config.getPort());
        assertEquals(RedisConfig.DEFAULT_PASSWORD, config.getPassword());
        assertEquals(RedisConfig.DEFAULT_TIMEOUT_MS, config.getTimeoutMs());
        assertEquals(RedisConfig.DEFAULT_POOL_SIZE, config.getPoolSize());
        assertEquals(List.of("messaging:*"), config.getSubscribePatterns());
        assertArrayEquals(new String[]{"messaging:*"}, config.getSubscribePatternsArray());
    }

    @Test
    void builderOverrides() {
        RedisConfig config = RedisConfig.builder()
                .host("redis.example.com")
                .port(6380)
                .password("secret")
                .timeoutMs(5000)
                .poolSize(16)
                .subscribePatterns(List.of("messaging:*", "app:*"))
                .build();
        assertEquals("redis.example.com", config.getHost());
        assertEquals(6380, config.getPort());
        assertEquals("secret", config.getPassword());
        assertEquals(5000, config.getTimeoutMs());
        assertEquals(16, config.getPoolSize());
        assertEquals(List.of("messaging:*", "app:*"), config.getSubscribePatterns());
    }

    @Test
    void emptyPatternsFallbackToDefault() {
        RedisConfig config = RedisConfig.builder()
                .subscribePatterns(List.of())
                .build();
        assertEquals(RedisConfig.DEFAULT_PATTERNS, config.getSubscribePatterns());
    }
}
