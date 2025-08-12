package org.project.domain.ride.entities;

import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.value_object.Price;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.project.domain.shared.util.Utils.required;

public class RideContract {

    public record PassengerSeat(int index, SeatStatus status) {}

    private final RideID rideId;
    private final Price pricePerSeat;
    private final List<PassengerSeat> bookedSeats;

    public RideContract(RideID rideId, Price pricePerSeat) {
        required("rideId", rideId);
        required("pricePerSeat", pricePerSeat);

        this.rideId = rideId;
        this.pricePerSeat = pricePerSeat;
        this.bookedSeats = new ArrayList<>();
    }

    public RideID rideId() {
        return rideId;
    }

    public Price pricePerSeat() {
        return pricePerSeat;
    }

    public List<PassengerSeat> bookedSeats() {
        return new ArrayList<>(bookedSeats);
    }

    public void addPassenger(int seatIndex, SeatStatus status) {
        required("status", status);

        boolean alreadyBooked = bookedSeats.stream()
                .anyMatch(s -> s.index() == seatIndex);
        if (alreadyBooked)
            throw new IllegalDomainArgumentException("This seat is already booked in the contract.");

        bookedSeats.add(new PassengerSeat(seatIndex, status));
    }

    public boolean hasSeat(int seatIndex) {
        return bookedSeats.stream().anyMatch(s -> s.index() == seatIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RideContract that)) return false;
        return Objects.equals(rideId, that.rideId) &&
               Objects.equals(pricePerSeat, that.pricePerSeat) &&
               Objects.equals(bookedSeats, that.bookedSeats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rideId, pricePerSeat, bookedSeats);
    }
}
