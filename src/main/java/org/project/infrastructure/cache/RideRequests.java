package org.project.infrastructure.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import org.project.application.pagination.PageRequest;
import org.project.domain.ride.entities.RideRequest;
import org.project.domain.ride.value_object.RideRequestID;
import org.project.domain.shared.value_objects.DriverID;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class RideRequests {

    private static final int TTL_SECONDS = 300;
    private static final String INDEX_KEY = "ride_requests:index";

    private final ValueCommands<String, RideRequest> commands;
    private final SortedSetCommands<String, String> sortedSet;

    RideRequests(Instance<RedisDataSource> redis) {
        RedisDataSource ds = redis.get();
        this.commands = ds.value(String.class, RideRequest.class);
        this.sortedSet = ds.sortedSet(String.class, String.class);
    }

    public void put(DriverID driverID, RideRequest rideRequest) {
        String redisKey = key(driverID, rideRequest.id());
        commands.setex(redisKey, TTL_SECONDS, rideRequest);

        double score = rideRequest.rideTime().startOfTheTrip().toEpochSecond(ZoneOffset.UTC);
        sortedSet.zadd(INDEX_KEY, score, redisKey);
    }

    public Optional<RideRequest> get(DriverID driverID, RideRequestID rideRequestID) {
        return Optional.ofNullable(commands.get(key(driverID, rideRequestID)));
    }

    public Optional<RideRequest> del(DriverID driverID, RideRequestID rideRequestID) {
        String redisKey = key(driverID, rideRequestID);
        sortedSet.zrem(INDEX_KEY, redisKey);
        return Optional.ofNullable(commands.getdel(redisKey));
    }

    public List<RideRequest> pageOf(PageRequest pageRequest) {
        int start = pageRequest.offset();
        int stop = start + pageRequest.limit() - 1;

        List<String> keys = sortedSet.zrange(INDEX_KEY, start, stop);
        return keys.stream()
                .map(commands::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private static String key(DriverID driverID, RideRequestID rideRequestID) {
        return "ride_request:{" + driverID.value() + "}:" + rideRequestID.value();
    }
}
