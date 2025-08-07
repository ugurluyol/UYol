package org.project.domain.ride.repositories;

import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface RideRepository {

    Result<Integer, Throwable> save(Ride ride);

    Result<Integer, Throwable> updateRoute(Ride ride);

    Result<Integer, Throwable> updateSeats(Ride ride);

    Result<Integer, Throwable> updateStatus(Ride ride);

    Result<Integer, Throwable> updateDelivery(Ride ride);

    Result<Integer, Throwable> updateRules(Ride ride);

    Result<Ride, Throwable> findBy(RideID rideID);

    Result<List<Ride>, Throwable> pageOf(OwnerID ownerID, Pageable pageable);

    Result<List<Ride>, Throwable> pageOf(DriverID ownerID, Pageable pageable);

    Result<List<Ride>, Throwable> pageOf(LocalDate localDate, Pageable pageable);
}
