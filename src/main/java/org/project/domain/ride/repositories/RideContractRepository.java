package org.project.domain.ride.repositories;

import org.project.domain.ride.entities.RideContract;
import org.project.domain.ride.value_object.RideContractID;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Pageable;
import org.project.domain.shared.value_objects.UserID;

import java.util.List;

public interface RideContractRepository {

    Result<Integer, Throwable> save(RideContract rideContract);

    Result<RideContract, Throwable> findBy(RideContractID rideContractID);

    Result<List<RideContract>, Throwable> findBy(RideID rideID, Pageable page);

    Result<List<RideContract>, Throwable> findBy(UserID userID, Pageable page);
}
