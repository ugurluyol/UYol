package org.project.domain.fleet.value_objects;

public record SeatCount(int value) {
  public SeatCount {
    if (value <= 1 || value > 12)
      throw new IllegalArgumentException("Seat count must be between 2 and 12");
  }
}
