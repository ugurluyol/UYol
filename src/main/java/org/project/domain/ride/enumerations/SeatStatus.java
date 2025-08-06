package org.project.domain.ride.enumerations;

public enum SeatStatus {
  EMPTY,
  DRIVER,
  MALE_OCCUPIED,
  FEMALE_OCCUPIED;

  public boolean isOccupied() {
    return this != EMPTY;
  }

  public boolean isAvailable() {
    return this == EMPTY;
  }
}
