package org.project.domain.ride.value_object;

import static org.project.domain.shared.util.Utils.required;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record RideTime(LocalDateTime date, LocalTime time) {
  public RideTime {
    required("date", date);
    required("time", time);

    if (!date.isAfter(LocalDateTime.now()) && !time.isAfter(LocalTime.now()))
      throw new IllegalDomainArgumentException("Ride must be scheduled for the future");
  }
}
