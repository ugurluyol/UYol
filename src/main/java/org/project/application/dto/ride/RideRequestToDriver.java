package org.project.application.dto.ride;

import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.ride.entities.RideRequest;
import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.value_objects.DriverID;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record RideRequestToDriver(
        UUID driverID,
        String licensePlate,
        String fromLocationDesc,
        double fromLatitude,
        double fromLongitude,
        String toLocationDesc,
        double toLatitude,
        double toLongitude,
        SeatStatus[][] seatMap,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal price,
        String rideDesc,
        RideRule[] rideRules
) {
    public RideRequest toRideRequest() {
        return new RideRequest(
                new RideRequestID(UUID.randomUUID()),
                new DriverID(driverID),
                new LicensePlate(licensePlate()),
                new Route(
                        new Location(fromLocationDesc(), fromLatitude(), fromLongitude()),
                        new Location(toLocationDesc(), toLatitude(), toLongitude())
                ),
                new RideTime(startTime(), endTime()),
                new Price(price()),
                new SeatMap(seatMap()),
                new RideDesc(rideDesc()),
                Set.of(rideRules()),
                LocalDateTime.now()
        );
    }

    public static RideRequestToDriver from(RideRequest rideRequest) {
        return new RideRequestToDriver(
                rideRequest.driverID().value(),
                rideRequest.licensePlate().value(),
                rideRequest.route().from().description(),
                rideRequest.route().from().latitude(),
                rideRequest.route().from().longitude(),
                rideRequest.route().to().description(),
                rideRequest.route().to().latitude(),
                rideRequest.route().to().longitude(),
                rideRequest.seatMap().seats(),
                rideRequest.rideTime().startOfTheTrip(),
                rideRequest.rideTime().endOfTheTrip(),
                rideRequest.price().amount(),
                rideRequest.rideDesc().value(),
                rideRequest.rideRules().toArray(new RideRule[0])
        );
    }
}
