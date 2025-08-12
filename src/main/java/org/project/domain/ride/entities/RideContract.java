package org.project.domain.ride.entities;

import org.project.domain.ride.value_object.BookedSeats;
import org.project.domain.ride.value_object.Price;
import org.project.domain.ride.value_object.RideID;
import java.util.Objects;

import static org.project.domain.shared.util.Utils.required;

public class RideContract {
    private final RideID rideID;
    private final Price pricePerSeat;
    private final BookedSeats bookedSeats;

    private RideContract(RideID rideID, Price pricePerSeat, BookedSeats bookedSeats) {
        required("rideId", rideID);
        required("pricePerSeat", pricePerSeat);
        required("bookedSeats", bookedSeats);

        this.rideID = rideID;
        this.pricePerSeat = pricePerSeat;
        this.bookedSeats = bookedSeats;
    }

    public static RideContract of(RideID rideId, Price pricePerSeat, BookedSeats bookedSeats) {
        return new RideContract(rideId, pricePerSeat, bookedSeats);
    }

    public static RideContract fromRepository(RideID rideId,  Price pricePerSeat, BookedSeats bookedSeats) {
        return new RideContract(rideId, pricePerSeat, bookedSeats);
    }

    public RideID rideID() {
        return rideID;
    }

    public Price pricePerSeat() {
        return pricePerSeat;
    }

    public BookedSeats bookedSeats() {
        return bookedSeats;
    }

    public boolean hasSeat(int seatIndex) {
        return bookedSeats.bookedSeats().stream().anyMatch(s -> s.index() == seatIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RideContract that = (RideContract) o;
        return Objects.equals(rideID, that.rideID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rideID);
    }
}
