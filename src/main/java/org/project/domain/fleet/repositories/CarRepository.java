package org.project.domain.fleet.repositories;

import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.value_objects.CarID;
import org.project.domain.shared.containers.Result;

public interface CarRepository {

  Result<Integer, Throwable> save(Car car);

  Result<Car, Throwable> findBy(CarID carID);
}
