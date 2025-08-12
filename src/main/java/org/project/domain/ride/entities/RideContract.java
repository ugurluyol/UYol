package org.project.domain.ride.entities;

import org.project.domain.ride.value_object.BookedSeats;
import org.project.domain.ride.value_object.Price;
import org.project.domain.ride.value_object.RideContractID;
import org.project.domain.ride.value_object.RideID;
import java.util.Objects;

import static org.project.domain.shared.util.Utils.required;

public class RideContract {
    private final RideContractID id;
    private final RideID rideID;
    private final Price pricePerSeat;
    private final BookedSeats bookedSeats;

    private RideContract(RideContractID id, RideID rideID, Price pricePerSeat, BookedSeats bookedSeats) {
        this.id = id;
        required("rideId", rideID);
        required("pricePerSeat", pricePerSeat);
        required("bookedSeats", bookedSeats);

        this.rideID = rideID;
        this.pricePerSeat = pricePerSeat;
        this.bookedSeats = bookedSeats;
    }

    public static RideContract of(RideID rideId, Price pricePerSeat, BookedSeats bookedSeats) {
        return new RideContract(RideContractID.newID(), rideId, pricePerSeat, bookedSeats);
    }

    public static RideContract fromRepository(RideContractID id, RideID rideId,
                                              Price pricePerSeat, BookedSeats bookedSeats) {
        return new RideContract(id, rideId, pricePerSeat, bookedSeats);
    }

    public RideContractID id() {
        return id;
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
