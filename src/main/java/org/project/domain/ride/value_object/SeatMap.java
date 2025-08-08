package org.project.domain.ride.value_object;

import static org.project.domain.shared.util.Utils.required;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record SeatMap(List<SeatStatus> seats) {

  public SeatMap {
    required("seats", seats);
    if (seats.isEmpty())
      throw new IllegalDomainArgumentException("Seat list cannot be empty");

    if (seats.size() < 2 || seats.size() > 64)
      throw new IllegalDomainArgumentException("Invalid seats count: min 2, max 64");

    if (seats.getFirst() != SeatStatus.DRIVER)
      throw new IllegalDomainArgumentException("First seat must be for the driver");

    for (int i = 1; i < seats.size(); i++) {
      required("seat", seats.get(i));

      if (seats.get(i) == SeatStatus.DRIVER)
        throw new IllegalDomainArgumentException("There can be only one driver");
    }
  }

  public static SeatMap of(SeatStatus... seats) {
    return new SeatMap(Arrays.asList(seats));
  }

  public static SeatMap ofEmpty(int totalSeats) {
    if (totalSeats < 2 || totalSeats > 64)
      throw new IllegalDomainArgumentException("Seat count must be between 2 and 64");

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
    if (index < 0 || index >= seats.size())
      throw new IllegalDomainArgumentException("Invalid seat index: " + index);
    return seats.get(index);
  }

  private SeatMap updateStatus(int index, SeatStatus newStatus) {
    required("newStatus", newStatus);
    if (index < 0 || index >= seats.size())
      throw new IllegalDomainArgumentException("Invalid seat index: " + index);

    if (index == 0 && newStatus != SeatStatus.DRIVER)
      throw new IllegalDomainArgumentException("First seat must always be for the driver");

    if (index != 0 && newStatus == SeatStatus.DRIVER)
      throw new IllegalDomainArgumentException("There can be only one driver");

    List<SeatStatus> updated = new ArrayList<>(seats);
    updated.set(index, newStatus);
    return new SeatMap(updated);
  }

  public boolean isAvailable(int index) {
    if (index < 0 || index >= seats.size())
      return false;
    return seats.get(index) == SeatStatus.EMPTY;
  }

  public SeatMap occupy(int index, SeatStatus occupantStatus) {
    required("occupantStatus", occupantStatus);
    if (index <= 0 || index >= seats.size())
      throw new IllegalDomainArgumentException("Invalid seat index: " + index);

    if (!occupantStatus.isOccupied())
      throw new IllegalDomainArgumentException("Seat must be occupied with valid occupant");

    if (!isAvailable(index))
      throw new IllegalDomainArgumentException("Seat is already occupied");

    return updateStatus(index, occupantStatus);
  }

  public SeatMap changePassenger(int index, SeatStatus newOccupantStatus) {
    required("newOccupantStatus", newOccupantStatus);
    if (index <= 0 || index >= seats.size())
      throw new IllegalDomainArgumentException("Invalid seat index: " + index);

    if (!newOccupantStatus.isOccupied())
      throw new IllegalDomainArgumentException("New occupant must be a valid passenger type");

    if (seats.get(index) == SeatStatus.EMPTY)
      throw new IllegalDomainArgumentException("Cannot change passenger on empty seat");

    return updateStatus(index, newOccupantStatus);
  }

  public SeatMap releaseSeat(int index) {
    if (index <= 0 || index >= seats.size())
      throw new IllegalDomainArgumentException("Invalid seat index: " + index);

    if (seats.get(index) == SeatStatus.EMPTY)
      throw new IllegalDomainArgumentException("Seat is already empty");

    return updateStatus(index, SeatStatus.EMPTY);
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

  public boolean hasAvailableSeats() {
    return seats.contains(SeatStatus.EMPTY);
  }
}