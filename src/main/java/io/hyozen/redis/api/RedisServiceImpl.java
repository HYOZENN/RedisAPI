package io.hyozen.redis.api;

import io.hyozen.redis.cache.RedisCache;
import io.hyozen.redis.cache.RedisCacheImpl;
import io.hyozen.redis.config.RedisConfig;
import io.hyozen.redis.connection.RedisConnection;
import io.hyozen.redis.handler.HandlerRegistry;
import io.hyozen.redis.handler.RedisChannelRegistrar;
import io.hyozen.redis.subscriber.RedisSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisServiceImpl implements RedisService {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final Logger logger;
    private RedisConnection connection;
    private HandlerRegistry registry;
    private RedisSubscriber subscriber;
    private RedisConfig config;
    private RedisCache defaultCache;

    public RedisServiceImpl() {
        this(DEFAULT_LOGGER);
    }

    public RedisServiceImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void connect(RedisConfig config) {
        this.config = config;
        this.connection = new RedisConnection(config, logger);
        this.registry = new HandlerRegistry();
        this.subscriber = new RedisSubscriber(registry, logger);
        connection.connect();
        connection.psubscribe(subscriber, config.getSubscribePatternsArray());
    }

    @Override
    public void disconnect() {
        if (subscriber != null) {
            subscriber.shutdown();
        }
        if (connection != null) {
            connection.shutdown();
        }
        connection = null;
        registry = null;
        subscriber = null;
        defaultCache = null;
    }

    @Override
    public void publish(String channel, String message) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        connection.publish(channel, message);
    }

    @Override
    public void registerChannelHandler(RedisChannelRegistrar registrar) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        registry.register(registrar);
    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    @Override
    public RedisConnection getConnection() {
        return connection;
    }

    @Override
    public RedisCache getCache() {
        if (defaultCache == null) {
            defaultCache = createCache("", null);
        }
        return defaultCache;
    }

    @Override
    public RedisCache createCache(String prefix, Long defaultTtlSeconds) {
        if (connection == null) {
            throw new IllegalStateException("Not connected");
        }
        return new RedisCacheImpl(connection, prefix, defaultTtlSeconds);
    }
}
