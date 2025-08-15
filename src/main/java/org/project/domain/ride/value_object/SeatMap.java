package org.project.domain.ride.value_object;

import static org.project.domain.shared.util.Utils.required;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;

import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record SeatMap(SeatStatus[][] seats) {

  public SeatMap {
    required("seats", seats);
    if (seats.length == 0 || seats[0].length == 0)
      throw new IllegalDomainArgumentException("Seat matrix cannot be empty");

    int totalSeats = 0;
    boolean hasDriver = false;

    for (SeatStatus[] row : seats) {
      for (SeatStatus seat : row) {
        required("seat", seat);
        totalSeats++;

        if (seat == SeatStatus.DRIVER) {
          if (hasDriver) {
            throw new IllegalDomainArgumentException("There can be only one driver");
          }
          hasDriver = true;
        }
      }
    }

    if (totalSeats < 2 || totalSeats > 64)
      throw new IllegalDomainArgumentException("Invalid seats count: min 2, max 64");

    if (!hasDriver)
      throw new IllegalDomainArgumentException("First seat must be for the driver");
  }

  public static SeatMap ofEmpty(int rows, int cols) {
    if (rows < 1 || cols < 1 || rows * cols < 2 || rows * cols > 64)
      throw new IllegalDomainArgumentException("Total seats must be between 2 and 64");

    SeatStatus[][] matrix = new SeatStatus[rows][cols];
    matrix[0][0] = SeatStatus.DRIVER;

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (i == 0 && j == 0) continue;
        matrix[i][j] = SeatStatus.EMPTY;
      }
    }

    return new SeatMap(matrix);
  }

  public SeatStatus[][] seats() {
    SeatStatus[][] copy = new SeatStatus[seats.length][];
    for (int i = 0; i < seats.length; i++) {
      copy[i] = Arrays.copyOf(seats[i], seats[i].length);
    }
    return copy;
  }

  public List<SeatStatus> seatsList() {
    List<SeatStatus> allSeats = new ArrayList<>();
    for (SeatStatus[] row : seats) {
      allSeats.addAll(Arrays.asList(row));
    }
    return allSeats;
  }

  public SeatStatus status(int index) {
    if (index < 0 || index >= totalSeats())
      throw new IllegalDomainArgumentException("Invalid seat index: " + index);

    int row = index / seats[0].length;
    int col = index % seats[0].length;
    return seats[row][col];
  }

  private SeatMap updateStatus(int index, SeatStatus newStatus) {
    required("newStatus", newStatus);
    if (index < 0 || index >= totalSeats())
      throw new IllegalDomainArgumentException("Invalid seat index: " + index);

    if (index == 0 && newStatus != SeatStatus.DRIVER)
      throw new IllegalDomainArgumentException("First seat must always be for the driver");

    if (index != 0 && newStatus == SeatStatus.DRIVER)
      throw new IllegalDomainArgumentException("There can be only one driver");

    SeatStatus[][] newMatrix = new SeatStatus[seats.length][];
    for (int i = 0; i < seats.length; i++) {
      newMatrix[i] = Arrays.copyOf(seats[i], seats[i].length);
    }

    int row = index / seats[0].length;
    int col = index % seats[0].length;
    newMatrix[row][col] = newStatus;

    return new SeatMap(newMatrix);
  }

  public boolean isAvailable(int index) {
    if (index < 0 || index >= totalSeats())
      return false;

    int row = index / seats[0].length;
    int col = index % seats[0].length;
    return seats[row][col] == SeatStatus.EMPTY;
  }

  public SeatMap occupy(int index, SeatStatus occupantStatus) {
    required("occupantStatus", occupantStatus);
    if (index <= 0 || index >= totalSeats())
      throw new IllegalDomainArgumentException("Invalid seat index: " + index);

    if (!occupantStatus.isOccupied())
      throw new IllegalDomainArgumentException("Seat must be occupied with valid occupant");

    if (!isAvailable(index))
      throw new IllegalDomainArgumentException("Seat is already occupied");

    return updateStatus(index, occupantStatus);
  }

  public int size() {
    return totalSeats();
  }

  private int totalSeats() {
    int count = 0;
    for (SeatStatus[] row : seats) {
      count += row.length;
    }
    return count;
  }

  public List<Integer> occupiedIndexes() {
    List<Integer> occupied = new ArrayList<>();
    int currentIndex = 0;

    for (SeatStatus[] row : seats) {
      for (SeatStatus seat : row) {
        if (seat.isOccupied()) {
          occupied.add(currentIndex);
        }
        currentIndex++;
      }
    }
    return occupied;
  }

  public boolean hasAvailableSeats() {
    for (SeatStatus[] row : seats) {
      for (SeatStatus seat : row) {
        if (seat == SeatStatus.EMPTY) {
          return true;
        }
      }
    }
    return false;
  }

  public int rowCount() {
    return seats.length;
  }

  public int columnCount() {
    return seats.length > 0 ? seats[0].length : 0;
  }

  public SeatStatus status(int row, int col) {
    if (row < 0 || row >= seats.length || col < 0 || col >= seats[row].length)
      throw new IllegalDomainArgumentException("Invalid seat coordinates: [" + row + "][" + col + "]");
    return seats[row][col];
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    SeatMap seatMap = (SeatMap) o;
    return Objects.deepEquals(seats, seatMap.seats);
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(seats);
  }

  @Override
  public String toString() {
    return "SeatMap{" +
            "seats=" + Arrays.toString(seats) +
            '}';
  }
}