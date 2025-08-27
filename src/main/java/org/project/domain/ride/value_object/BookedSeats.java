package org.project.domain.ride.value_object;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.util.*;

import static org.project.domain.shared.util.Utils.required;

public record BookedSeats(List<PassengerSeat> bookedSeats) {
    public BookedSeats {
        required("bookedSeats", bookedSeats);

        bookedSeats = new ArrayList<>(bookedSeats);
        for (PassengerSeat seat : bookedSeats) required("seat", seat);

        Set<Integer> uniqueIndexes = new HashSet<>();
        for (PassengerSeat seat : bookedSeats)
            if (!uniqueIndexes.add(seat.index()))
                throw new IllegalDomainArgumentException("Duplicate seat index found: " + seat.index());
    }

    public static BookedSeats empty() {
        return new BookedSeats(List.of());
    }

    public List<PassengerSeat> bookedSeats() {
        return new ArrayList<>(bookedSeats);
    }

    public int size() {
        return bookedSeats.size();
    }

    public boolean containsIndex(int index) {
        return bookedSeats.stream().anyMatch(seat -> seat.index() == index);
    }

    public Optional<PassengerSeat> getByIndex(int index) {
        return bookedSeats.stream()
                .filter(seat -> seat.index() == index)
                .findFirst();
    }

    public boolean isEmpty() {
        return bookedSeats.isEmpty();
    }
}