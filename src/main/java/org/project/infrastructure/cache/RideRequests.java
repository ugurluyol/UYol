package org.project.infrastructure.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import org.project.application.dto.ride.RideRequestToDriver;
import org.project.domain.shared.value_objects.DriverID;

import java.util.Optional;

@ApplicationScoped
public class RideRequests {

    private static final int TTL_SECONDS = 300;

    private final ValueCommands<String, RideRequestToDriver> commands;

    RideRequests(Instance<RedisDataSource> redis) {
        this.commands = redis.get().value(String.class, RideRequestToDriver.class);
    }

    public void put(DriverID key, RideRequestToDriver dto) {
        commands.setex(key.value().toString(), TTL_SECONDS, dto);
    }

    public Optional<RideRequestToDriver> get(DriverID key) {
        return Optional.ofNullable(commands.get(key.value().toString()));
    }

    public Optional<RideRequestToDriver> del(DriverID key) {
        return Optional.ofNullable(commands.getdel(key.value().toString()));
    }
}
