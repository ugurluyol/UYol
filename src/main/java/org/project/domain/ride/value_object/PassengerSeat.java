package org.project.domain.ride.value_object;

import org.project.domain.ride.enumerations.SeatStatus;

public record PassengerSeat(int index, SeatStatus status) {}
