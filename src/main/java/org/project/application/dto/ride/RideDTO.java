package org.project.application.dto.ride;

import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.RideStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideDTO(
    String id,
    String driverId,
    String ownerId,
    String fromLocationDesc,
    double fromLatitude,
    double fromLongitude,
    String toLocationDesc,
    double toLatitude,
    double toLongitude,
    LocalDateTime startTime,
    LocalDateTime endTime,
    BigDecimal price,
    RideStatus status
) {
    public static RideDTO from(Ride ride) {
        return new RideDTO(
                ride.id().value().toString(),
                ride.rideOwner().driverID().toString(),
                ride.rideOwner().ownerID().toString(),
                ride.route().from().description(),
                ride.route().from().latitude(),
                ride.route().from().longitude(),
                ride.route().to().description(),
                ride.route().to().latitude(),
                ride.route().to().longitude(),
                ride.rideTime().startOfTheTrip(),
                ride.rideTime().endOfTheTrip(),
                ride.price().amount(),
                ride.status()
        );
    }
}