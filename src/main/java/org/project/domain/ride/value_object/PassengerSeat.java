package org.project.domain.ride.value_object;

import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record PassengerSeat(int index, SeatStatus status) {
    public PassengerSeat {
        if (index < 0)
            throw new IllegalDomainArgumentException("Seat index cannot be negative: " + index);

        if (status == null)
            throw new IllegalDomainArgumentException("Seat status cannot be null");

        if (!status.isOccupied() || status == SeatStatus.DRIVER)
            throw new IllegalDomainArgumentException("Passenger seat must have occupied status (not EMPTY or DRIVER): " + status);
    }
}