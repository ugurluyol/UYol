package org.project.domain.fleet.repositories;

import java.util.List;

import org.project.application.dto.fleet.CarDTO;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.value_objects.CarID;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Pageable;

public interface CarRepository {

  Result<Integer, Throwable> save(Car car);

  Result<Car, Throwable> findBy(CarID carID);

  Result<Car, Throwable> findBy(LicensePlate licensePlate);

  Result<List<CarDTO>, Throwable> pageOf(Pageable pageable, UserID userID);

  boolean isLicenseTemplateExists(LicensePlate license);;
}
