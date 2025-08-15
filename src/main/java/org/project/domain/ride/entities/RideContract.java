package org.project.domain.ride.entities;

import org.project.domain.ride.value_object.BookedSeats;
import org.project.domain.ride.value_object.Price;
import org.project.domain.ride.value_object.RideContractID;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.value_objects.UserID;

import java.util.Objects;

import static org.project.domain.shared.util.Utils.required;

public class RideContract {
    private final RideContractID id;
    private final UserID userID;
    private final RideID rideID;
    private final Price pricePerSeat;
    private final BookedSeats bookedSeats;

    private RideContract(RideContractID id, UserID userID, RideID rideID, Price pricePerSeat, BookedSeats bookedSeats) {
        this.id = id;
        this.userID = userID;
        this.rideID = rideID;
        this.pricePerSeat = pricePerSeat;
        this.bookedSeats = bookedSeats;
    }

    public static RideContract of(UserID userID, RideID rideID, Price pricePerSeat, BookedSeats bookedSeats) {
        required("userID", userID);
        required("rideID", rideID);
        required("pricePerSeat", pricePerSeat);
        required("bookedSeats", bookedSeats);

        return new RideContract(RideContractID.newID(), userID, rideID, pricePerSeat, bookedSeats);
    }

    public static RideContract fromRepository(RideContractID id, UserID userID, RideID rideId,
                                              Price pricePerSeat, BookedSeats bookedSeats) {
        return new RideContract(id, userID, rideId, pricePerSeat, bookedSeats);
    }

    public RideContractID id() {
        return id;
    }

    public UserID userID() {
        return userID;
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
