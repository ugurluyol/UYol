package org.project.domain.ride.value_object;

import static org.project.domain.shared.util.Utils.required;

public record RideDesc(String value) {
    public static final int MAX_RIDE_DESC_SIZE = 128;

    public RideDesc {
        required("rideDescription", value);

        if (value.length() > MAX_RIDE_DESC_SIZE)
            throw new IllegalArgumentException("RideDescription length exceeds maximum length");
    }
}
