package org.project.domain.ride.repositories;

import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.Pageable;

import java.time.LocalDate;
import java.util.List;

public class JetRideRepository implements RideRepository {
    @Override
    public Result<Integer, Throwable> save(Ride ride) {
        return null;
    }

    @Override
    public Result<Integer, Throwable> updateRoute(Ride ride) {
        return null;
    }

    @Override
    public Result<Integer, Throwable> updateSeats(Ride ride) {
        return null;
    }

    @Override
    public Result<Integer, Throwable> updateStatus(Ride ride) {
        return null;
    }

    @Override
    public Result<Integer, Throwable> updateDelivery(Ride ride) {
        return null;
    }

    @Override
    public Result<Integer, Throwable> updateRules(Ride ride) {
        return null;
    }

    @Override
    public Result<Ride, Throwable> findBy(RideID rideID) {
        return null;
    }

    @Override
    public Result<List<Ride>, Throwable> pageOf(OwnerID ownerID, Pageable pageable) {
        return null;
    }

    @Override
    public Result<List<Ride>, Throwable> pageOf(DriverID ownerID, Pageable pageable) {
        return null;
    }

    @Override
    public Result<List<Ride>, Throwable> pageOf(LocalDate localDate, Pageable pageable) {
        return null;
    }
}
