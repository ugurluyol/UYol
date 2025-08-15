package org.project.domain.fleet.value_objects;

import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.util.Arrays;

import static org.project.domain.shared.util.Utils.required;

public record CarSeatMap(SeatStatus[][] seats) {

    public CarSeatMap {
        required("seats", seats);
        if (seats.length == 0 || seats[0].length == 0)
            throw new IllegalDomainArgumentException("Seat matrix cannot be empty");

        if (seats[0][0] != SeatStatus.DRIVER)
            throw new IllegalDomainArgumentException("Seat matrix must start with driver");

        int totalSeats = 0;

        for (SeatStatus[] row : seats) {
            if (row.length > 4)
                throw new IllegalDomainArgumentException("Seat matrix contains more than 4 seats in a row");

            for (SeatStatus seat : row) {
                required("seat", seat);
                totalSeats++;

                if (totalSeats > 64)
                    throw new IllegalDomainArgumentException("Invalid seats count: min 2, max 64");

                if (seat == SeatStatus.DRIVER)
                    throw new IllegalDomainArgumentException("There can be only one driver");
            }
        }

        if (totalSeats < 2)
            throw new IllegalDomainArgumentException("Invalid seats count: min 2, max 64");
    }

    public static CarSeatMap ofEmpty(int rows, int cols) {
        validateRowsAndColumns(rows, cols);

        SeatStatus[][] matrix = new SeatStatus[rows][cols];
        matrix[0][0] = SeatStatus.DRIVER;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == 0 && j == 0)
                    continue;
                matrix[i][j] = SeatStatus.EMPTY;
            }
        }

        return new CarSeatMap(matrix);
    }

    public SeatStatus[][] seats() {
        SeatStatus[][] copy = new SeatStatus[seats.length][];
        for (int i = 0; i < seats.length; i++) {
            copy[i] = Arrays.copyOf(seats[i], seats[i].length);
        }
        return copy;
    }

    public SeatStatus status(int row, int col) {
        if (row < 0 || row >= seats.length || col < 0 || col >= seats[row].length) {
            throw new IllegalDomainArgumentException(
                    "Invalid seat coordinates: [" + row + "][" + col + "]");
        }
        return seats[row][col];
    }

    public int rowCount() {
        return seats.length;
    }

    public int columnCount() {
        return seats.length > 0 ? seats[0].length : 0;
    }

    private int totalSeats() {
        int count = 0;
        for (SeatStatus[] row : seats) {
            count += row.length;
        }
        return count;
    }

    private static void validateRowsAndColumns(int rows, int cols) {
        if (rows < 1 || cols < 1 || rows * cols < 2 || rows * cols > 64)
            throw new IllegalDomainArgumentException("Total seats must be between 2 and 64");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CarSeatMap that = (CarSeatMap) o;
        return Arrays.deepEquals(seats, that.seats);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(seats);
    }

    @Override
    public String toString() {
        return "CarSeatMap{" +
                "seats=" + Arrays.deepToString(seats) +
                '}';
    }
}
