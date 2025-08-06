package org.project.domain.ride.value_object;

import static org.project.domain.shared.util.Utils.required;

import java.time.LocalDateTime;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record RideTime(LocalDateTime startOfTheTrip, LocalDateTime endOfTheTrip) {
  public RideTime {
    required("startOfTheTrip", startOfTheTrip);
    required("endOfTheTrip", endOfTheTrip);

    if (!startOfTheTrip.isAfter(LocalDateTime.now()))
      throw new IllegalDomainArgumentException("Start of the trip must be in the future");

    if (!endOfTheTrip.isAfter(startOfTheTrip))
      throw new IllegalDomainArgumentException("End of the trip must be after start time");
  }
}
