package io.hyozen.redis.handler;

@FunctionalInterface
public interface StringHandler {
    void handle(String message) throws Exception;
}
