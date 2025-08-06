package org.project.domain.ride.value_object;

import static org.project.domain.shared.util.Utils.required;

import java.util.UUID;

public record RideID(UUID value) {
  public RideID {
    required("rideID", value);
  }

  public static RideID newID() {
    return new RideID(UUID.randomUUID());
  }
}
