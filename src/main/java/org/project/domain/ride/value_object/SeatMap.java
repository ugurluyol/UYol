package org.project.domain.ride.value_object;

import static org.project.domain.shared.util.Utils.required;

import java.util.ArrayList;
import java.util.List;

import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record SeatMap(List<SeatStatus> seats) {

  public SeatMap {
    required("seats", seats);
    if (seats.isEmpty())
      throw new IllegalDomainArgumentException("Seat list cannot be empty");

    if (seats.size() < 2 || seats.size() > 12)
      throw new IllegalDomainArgumentException("Invalid seats count: min 2, max 12");

    if (seats.getFirst() != SeatStatus.DRIVER)
      throw new IllegalDomainArgumentException("Where is a driver?...");

    for (int i = 1; i < seats.size(); i++) {
      if (seats.get(i) == SeatStatus.DRIVER)
        throw new IllegalDomainArgumentException("Like how? There is no more than one driver");
    }
  }

  public static SeatMap ofEmpty(int totalSeats) {
    if (totalSeats <= 1)
      throw new IllegalDomainArgumentException("Seat count must be greater than 1, as first seat is for the driver");

    List<SeatStatus> seats = new ArrayList<>(totalSeats);
    seats.add(SeatStatus.DRIVER);
    for (int i = 1; i < totalSeats; i++) {
      seats.add(SeatStatus.EMPTY);
    }

    return new SeatMap(seats);
  }

  public List<SeatStatus> seats() {
    return new ArrayList<>(seats);
  }

  public SeatStatus status(int index) {
    return seats.get(index);
  }

  public SeatMap updateStatus(int index, SeatStatus newStatus) {
    required("newStatus", newStatus);
    if (index < 1)
      throw new IllegalDomainArgumentException("You cannot change index below 1");

    if (newStatus == SeatStatus.DRIVER)
      throw new IllegalDomainArgumentException("There is no second driver");

    List<SeatStatus> updated = new ArrayList<>(seats);
    updated.set(index, newStatus);
    return new SeatMap(updated);
  }

  public boolean isAvailable(int index) {
    return seats.get(index) == SeatStatus.EMPTY;
  }

  public SeatMap occupy(int index, SeatStatus occupantStatus) {
    required("occupantStatus", occupantStatus);
    if (index < 1)
      throw new IllegalDomainArgumentException("You cannot change index below 1");

    if (occupantStatus == SeatStatus.DRIVER)
      throw new IllegalDomainArgumentException("There is no second driver");

    if (!occupantStatus.isOccupied())
      throw new IllegalDomainArgumentException("Seat must be occupied with valid occupant");

    return updateStatus(index, occupantStatus);
  }

  public int size() {
    return seats.size();
  }

  public List<Integer> occupiedIndexes() {
    List<Integer> occupied = new ArrayList<>();
    for (int i = 0; i < seats.size(); i++) {
      if (seats.get(i).isOccupied()) {
        occupied.add(i);
      }
    }
    return occupied;
  }
}
