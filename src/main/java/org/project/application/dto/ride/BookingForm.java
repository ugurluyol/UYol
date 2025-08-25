package org.project.application.dto.ride;

import org.project.domain.ride.value_object.PassengerSeat;

import java.util.List;
import java.util.UUID;

public record BookingForm(UUID rideID, List<PassengerSeat> passengerSeats) {}
