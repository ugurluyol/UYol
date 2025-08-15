package org.project.application.dto.ride;

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
) {}