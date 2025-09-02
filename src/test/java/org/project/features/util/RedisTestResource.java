package org.project.features.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

public class RedisTestResource implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> redisContainer;

    @Override
    public Map<String, String> start() {
        redisContainer = new GenericContainer<>("redis:latest")
                .withExposedPorts(6379);

        redisContainer.start();

        String redis_host = redisContainer.getHost();
        int redis_port = redisContainer.getMappedPort(6379);
        String redis_url = "redis://%s:%s".formatted(redis_host, redis_port);

        return Map.of("quarkus.redis.hosts", redis_url);
    }

    @Override
    public void stop() {
        if (redisContainer != null) {
            redisContainer.stop();
            redisContainer.close();
            redisContainer = null;
        }
    }
}
