package org.project.domain.fleet.entities;

import org.project.domain.fleet.value_objects.CarBrand;
import org.project.domain.fleet.value_objects.CarColor;
import org.project.domain.fleet.value_objects.CarID;
import org.project.domain.fleet.value_objects.CarModel;
import org.project.domain.fleet.value_objects.CarYear;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.fleet.value_objects.UserID;

import static org.project.domain.shared.util.Utils.required;

import java.util.Objects;
import java.util.UUID;

public class Car {
    private final CarID id;
    private final UserID owner;
    private final LicensePlate licensePlate;
    private final CarBrand carBrand;
    private final CarModel carModel;
    private final CarColor carColor;
    private final CarYear carYear;

    private Car(
            CarID id,
            UserID owner,
            LicensePlate licensePlate,
            CarBrand carBrand,
            CarModel carModel,
            CarColor carColor,
            CarYear carYear) {

        this.id = id;
        this.owner = owner;
        this.licensePlate = licensePlate;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.carColor = carColor;
        this.carYear = carYear;
    }

    public static Car of(
            UserID owner,
            LicensePlate licensePlate,
            CarBrand carBrand,
            CarModel carModel,
            CarColor carColor,
            CarYear carYear) {

        required("owner", owner);
        required("licensePlate", licensePlate);
        required("carBrand", carBrand);
        required("carModel", carModel);
        required("carColor", carColor);
        required("carYear", carYear);

        return new Car(new CarID(UUID.randomUUID()), owner, licensePlate, carBrand, carModel, carColor, carYear);
    }

    public static Car fromRepository(
            CarID id,
            UserID owner,
            LicensePlate licensePlate,
            CarBrand carBrand,
            CarModel carModel,
            CarColor carColor,
            CarYear carYear) {

        return new Car(new CarID(UUID.randomUUID()), owner, licensePlate, carBrand, carModel, carColor, carYear);
    }

    public CarID id() {
        return id;
    }

    public UserID owner() {
        return owner;
    }

    public LicensePlate licensePlate() {
        return licensePlate;
    }

    public CarBrand carBrand() {
        return carBrand;
    }

    public CarModel carModel() {
        return carModel;
    }

    public CarColor carColor() {
        return carColor;
    }

    public CarYear carYear() {
        return carYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Car car = (Car) o;
        return Objects.equals(id, car.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
