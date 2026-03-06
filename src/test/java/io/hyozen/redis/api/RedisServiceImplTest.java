package io.hyozen.redis.api;

import io.hyozen.redis.cache.RedisCache;
import io.hyozen.redis.connection.RedisConnection;
import io.hyozen.redis.handler.RedisChannelRegistrar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {

    private RedisServiceImpl service;

    @Mock
    private RedisConnection connection;

    @BeforeEach
    void setUp() {
        service = new RedisServiceImpl();
    }

    @Test
    @DisplayName("publish throws when not connected")
    void publishThrowsWhenNotConnected() {
        assertThrows(IllegalStateException.class, () -> service.publish("ch", "msg"));
    }

    @Test
    @DisplayName("registerChannelHandler throws when not connected")
    void registerChannelHandlerThrowsWhenNotConnected() {
        RedisChannelRegistrar registrar = (stringHandlers, jsonHandlers) -> {};
        assertThrows(IllegalStateException.class, () -> service.registerChannelHandler(registrar));
    }

    @Test
    @DisplayName("createCache throws when not connected")
    void createCacheThrowsWhenNotConnected() {
        assertThrows(IllegalStateException.class, () -> service.createCache("prefix", 60L));
    }

    @Test
    @DisplayName("getCache throws when not connected")
    void getCacheThrowsWhenNotConnected() {
        assertThrows(IllegalStateException.class, () -> service.getCache());
    }

    @Test
    @DisplayName("getConnection returns null when not connected")
    void getConnectionReturnsNullWhenNotConnected() {
        assertNull(service.getConnection());
    }

    @Test
    @DisplayName("isConnected returns false when not connected")
    void isConnectedReturnsFalseWhenNotConnected() {
        assertFalse(service.isConnected());
    }

    @Test
    @DisplayName("getCache returns same instance when connection is set")
    void getCacheReturnsSameInstanceWhenConnected() throws Exception {
        injectConnection(service, connection);

        RedisCache cache1 = service.getCache();
        RedisCache cache2 = service.getCache();
        assertSame(cache1, cache2);
    }

    @Test
    @DisplayName("createCache returns new instances when connection is set")
    void createCacheReturnsNewInstancesWhenConnected() throws Exception {
        injectConnection(service, connection);

        RedisCache cache1 = service.createCache("p1", 60L);
        RedisCache cache2 = service.createCache("p2", null);
        assertNotSame(cache1, cache2);
    }

    private static void injectConnection(RedisServiceImpl target, RedisConnection connection) throws Exception {
        Field f = RedisServiceImpl.class.getDeclaredField("connection");
        f.setAccessible(true);
        f.set(target, connection);
    }
}
