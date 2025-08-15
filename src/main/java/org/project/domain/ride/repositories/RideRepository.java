package org.project.domain.ride.repositories;

import org.project.application.dto.ride.RideDTO;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.value_object.Location;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.Pageable;
import org.project.domain.shared.value_objects.UserID;

import java.time.LocalDate;
import java.util.List;

public interface RideRepository {

    Result<Integer, Throwable> save(Ride ride);

    Result<Integer, Throwable> updateSeats(Ride ride);

    Result<Integer, Throwable> updateStatus(Ride ride);

    Result<Integer, Throwable> updateRules(Ride ride);

    Result<Ride, Throwable> findBy(RideID rideID);

    Result<List<RideDTO>, Throwable> pageOf(UserID userID, Pageable page);

    Result<List<RideDTO>, Throwable> pageOf(OwnerID ownerID, Pageable page);

    Result<List<RideDTO>, Throwable> pageOf(DriverID driverID, Pageable page);

    Result<List<RideDTO>, Throwable> pageOf(LocalDate localDate, Pageable page);

    Result<List<RideDTO>, Throwable> actualFor(Location startPoint, Location destination, Pageable page);
}
