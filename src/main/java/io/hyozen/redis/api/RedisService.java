package io.hyozen.redis.api;

import io.hyozen.redis.cache.RedisCache;
import io.hyozen.redis.config.RedisConfig;
import io.hyozen.redis.connection.RedisConnection;
import io.hyozen.redis.handler.RedisChannelRegistrar;

public interface RedisService {

    void connect(RedisConfig config);

    void disconnect();

    void publish(String channel, String message);

    void registerChannelHandler(RedisChannelRegistrar registrar);

    boolean isConnected();

    RedisConnection getConnection();

    RedisCache getCache();

    RedisCache createCache(String prefix, Long defaultTtlSeconds);
}
