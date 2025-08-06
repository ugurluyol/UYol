package org.project.domain.fleet.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record SeatCount(int value) {
  public SeatCount {
    if (value <= 1 || value > 12)
      throw new IllegalDomainArgumentException("Seat count must be between 2 and 12");
  }
}
