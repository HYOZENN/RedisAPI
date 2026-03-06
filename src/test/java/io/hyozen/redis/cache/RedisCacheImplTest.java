package io.hyozen.redis.cache;

import io.hyozen.redis.connection.RedisConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisCacheImplTest {

    @Mock
    private RedisConnection connection;

    @Mock
    private Jedis jedis;

    private final ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<>();

    private RedisCache cache;

    @BeforeEach
    void setUp() {
        storage.clear();
        cache = new RedisCacheImpl(connection, "ns:", 60L);

        when(connection.getJedis()).thenReturn(jedis);

        doAnswer(inv -> storage.get(inv.getArgument(0))).when(jedis).get(anyString());

        doAnswer(inv -> {
            storage.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jedis).set(anyString(), anyString());

        doAnswer(inv -> {
            storage.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jedis).set(anyString(), anyString(), any(SetParams.class));

        doAnswer(inv -> {
            storage.remove(inv.getArgument(0));
            return 1L;
        }).when(jedis).del(anyString());

        doAnswer(inv -> storage.containsKey(inv.getArgument(0))).when(jedis).exists(anyString());

        when(jedis.scan(anyString(), any(ScanParams.class))).thenAnswer(inv -> {
            List<String> keys = storage.keySet().stream().toList();
            return new ScanResult<>(ScanParams.SCAN_POINTER_START, keys);
        });
    }

    @Test
    @DisplayName("get returns empty when key absent")
    void getReturnsEmptyWhenAbsent() {
        assertEquals(Optional.empty(), cache.get("missing"));
    }

    @Test
    @DisplayName("get returns value after set")
    void getReturnsValueAfterSet() {
        cache.set("k1", "v1");
        assertEquals(Optional.of("v1"), cache.get("k1"));
        verify(jedis).set(eq("ns:k1"), eq("v1"), any(SetParams.class));
        verify(jedis).get("ns:k1");
    }

    @Test
    @DisplayName("set with TTL uses full key and SetParams")
    void setWithTtlUsesSetParams() {
        cache.set("k2", "v2", 120L);
        assertEquals(Optional.of("v2"), cache.get("k2"));
        verify(jedis).set(eq("ns:k2"), eq("v2"), any(SetParams.class));
    }

    @Test
    @DisplayName("delete removes key")
    void deleteRemovesKey() {
        cache.set("k3", "v3");
        assertTrue(cache.exists("k3"));
        cache.delete("k3");
        assertEquals(Optional.empty(), cache.get("k3"));
        assertFalse(cache.exists("k3"));
    }

    @Test
    @DisplayName("exists returns true when key present")
    void existsReturnsTrueWhenKeyPresent() {
        cache.set("k4", "v4");
        assertTrue(cache.exists("k4"));
    }

    @Test
    @DisplayName("exists returns false when key absent")
    void existsReturnsFalseWhenKeyAbsent() {
        assertFalse(cache.exists("absent"));
    }

    @Test
    @DisplayName("deleteByPattern calls scan and del")
    void deleteByPatternCallsScanAndDel() {
        cache.set("a", "1");
        cache.set("b", "2");
        cache.deleteByPattern("*");
        verify(jedis, atLeastOnce()).scan(anyString(), any(ScanParams.class));
        assertEquals(Optional.empty(), cache.get("a"));
        assertEquals(Optional.empty(), cache.get("b"));
    }

    @Test
    @DisplayName("cache with empty prefix uses key as-is")
    void cacheWithEmptyPrefixUsesKeyAsIs() {
        RedisCache noPrefix = new RedisCacheImpl(connection, "", null);
        when(connection.getJedis()).thenReturn(jedis);
        storage.clear();
        noPrefix.set("raw", "value");
        assertEquals(Optional.of("value"), noPrefix.get("raw"));
        verify(jedis).set("raw", "value");
    }
}
