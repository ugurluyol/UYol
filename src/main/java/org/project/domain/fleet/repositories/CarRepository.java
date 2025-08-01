package org.project.domain.fleet.repositories;

import java.util.List;

import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.value_objects.CarID;
import org.project.domain.fleet.value_objects.UserID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Pageable;

public interface CarRepository {

  Result<Integer, Throwable> save(Car car);

  Result<Car, Throwable> findBy(CarID carID);

  Result<List<Car>, Throwable> pageOf(Pageable pageable, UserID userID);
}
