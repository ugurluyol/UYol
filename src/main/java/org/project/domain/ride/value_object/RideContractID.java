package org.project.domain.ride.value_object;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record RideContractID(UUID value) {
    public RideContractID {
        required("rideContractID", value);
    }

    public static RideContractID newID() {
        return new RideContractID(UUID.randomUUID());
    }
}
