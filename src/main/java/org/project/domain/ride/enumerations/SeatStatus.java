package org.project.domain.ride.enumerations;

public enum SeatStatus {
  EMPTY,
  DRIVER,
  MALE_OCCUPIED,
  FEMALE_OCCUPIED;

  public boolean isOccupied() {
    return this == MALE_OCCUPIED || this == FEMALE_OCCUPIED;
  }

  public boolean isAvailable() {
    return this == EMPTY;
  }

  public boolean isDriverSeat() {
    return this == DRIVER;
  }
}
