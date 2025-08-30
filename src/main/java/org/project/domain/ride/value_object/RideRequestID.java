package org.project.domain.ride.value_object;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record RideRequestID(UUID value) {
    public RideRequestID {
        required("rideRequestID", value);
    }

    public static RideRequestID fromString(String value) {
        return new RideRequestID(UUID.fromString(value));
    }
}
