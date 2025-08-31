package org.project.infrastructure.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import org.project.domain.ride.entities.RideRequest;
import org.project.domain.ride.value_object.RideRequestID;
import org.project.domain.shared.value_objects.DriverID;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class RideRequests {

    private static final int TTL_SECONDS = 300;

    private final KeyCommands<String> keyCommands;

    private final ValueCommands<String, RideRequest> commands;

    RideRequests(Instance<RedisDataSource> redis) {
        this.commands = redis.get().value(String.class, RideRequest.class);
        this.keyCommands = redis.get().key();
    }

    public void put(DriverID driverID, RideRequest rideRequest) {
        String redisKey = key(driverID, rideRequest.id());
        commands.setex(redisKey, TTL_SECONDS, rideRequest);
    }

    public Optional<RideRequest> get(DriverID driverID, RideRequestID rideRequestID) {
        return Optional.ofNullable(commands.get(key(driverID, rideRequestID)));
    }

    public Optional<RideRequest> del(DriverID driverID, RideRequestID rideRequestID) {
        return Optional.ofNullable(commands.getdel(key(driverID, rideRequestID)));
    }

    public List<RideRequest> pageOf(DriverID driverID) {
        return keyCommands.keys(driverRequestsKey(driverID)).stream()
                .map(commands::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private static String driverRequestsKey(DriverID driverID) {
        return "ride_request:{" + driverID.value() + "}:*";
    }

    private static String key(DriverID driverID, RideRequestID rideRequestID) {
        return "ride_request:{" + driverID.value() + "}:" + rideRequestID.value();
    }
}
