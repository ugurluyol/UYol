package org.project.application.dto.ride;

import org.project.domain.ride.entities.RideContract;
import org.project.domain.ride.value_object.PassengerSeat;

import java.math.BigDecimal;
import java.util.List;

public record RideContractDTO(
        String rideContractID,
        String userID,
        String rideID,
        BigDecimal pricePerSeat,
        BigDecimal totalPrice,
        List<PassengerSeat> passengerSeats) {

    public static RideContractDTO from(RideContract rideContract) {
        return new RideContractDTO(
                rideContract.id().value().toString(),
                rideContract.userID().value().toString(),
                rideContract.rideID().value().toString(),
                rideContract.pricePerSeat().amount(),
                rideContract.totalPrice().amount(),
                rideContract.bookedSeats().bookedSeats()
        );
    }
}
