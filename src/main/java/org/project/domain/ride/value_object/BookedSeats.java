package org.project.domain.ride.value_object;

import java.util.ArrayList;
import java.util.List;

public record BookedSeats(List<PassengerSeat> bookedSeats) {
    public BookedSeats {
        bookedSeats = new ArrayList<>(bookedSeats);
    }

    public List<PassengerSeat> bookedSeats() {
        return new ArrayList<>(bookedSeats);
    }
}
