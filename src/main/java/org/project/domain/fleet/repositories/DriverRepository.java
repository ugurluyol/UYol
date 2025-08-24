package org.project.domain.fleet.repositories;

import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.containers.Result;

public interface DriverRepository {

    Result<Integer, Throwable> save(Driver driver);

    Result<Integer, Throwable> updateLicense(Driver driver);

    Result<Integer, Throwable> updateStatus(Driver driver);

    Result<Integer, Throwable> updateRides(Driver driver);

    Result<Integer, Throwable> updateRating(Driver driver);

    Result<Driver, Throwable> findBy(DriverID driverID);

    Result<Driver, Throwable> findBy(UserID userID);

    boolean isLicenseExists(DriverLicense license);

    boolean isDriverExists(UserID userID);
}
