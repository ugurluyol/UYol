package org.project.application.dto.ride;

import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.enumerations.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideRequestToDriver(
        String driverID,
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
) {}
