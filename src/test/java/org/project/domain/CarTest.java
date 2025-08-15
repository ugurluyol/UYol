package org.project.domain;

import org.junit.jupiter.api.Test;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.value_objects.*;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.shared.value_objects.UserID;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.project.features.TestDataGenerator.*;

class CarTest {

    @Test
    void shouldCreateCarWithOfMethod() {
        var owner = UserID.newID();
        var licensePlate = generateLicensePlate();
        var carBrand = generateCarBrand();
        var carModel = generateCarModel();
        var carColor = generateCarColor();
        var carYear = generateCarYear();
        var seatCount = generateSeatCount();

        var car = Car.of(owner, licensePlate, carBrand, carModel, carColor, carYear, seatCount);

        assertNotNull(car.id());
        assertEquals(owner, car.owner());
        assertEquals(licensePlate, car.licensePlate());
        assertEquals(carBrand, car.carBrand());
        assertEquals(carModel, car.carModel());
        assertEquals(carColor, car.carColor());
        assertEquals(carYear, car.carYear());
        assertEquals(seatCount, car.seatCount());
        assertNotNull(car.createdAt());
    }

    @Test
    void shouldCreateCarFromRepository() {
        var id = new CarID(UUID.randomUUID());
        var owner = UserID.newID();
        var licensePlate = generateLicensePlate();
        var carBrand = generateCarBrand();
        var carModel = generateCarModel();
        var carColor = generateCarColor();
        var carYear = generateCarYear();
        var seatCount = generateSeatCount();
        var createdAt = LocalDateTime.now();

        var car = Car.fromRepository(id, owner, licensePlate, carBrand, carModel, carColor, carYear, seatCount, createdAt);

        assertEquals(id, car.id());
        assertEquals(owner, car.owner());
        assertEquals(licensePlate, car.licensePlate());
        assertEquals(carBrand, car.carBrand());
        assertEquals(carModel, car.carModel());
        assertEquals(carColor, car.carColor());
        assertEquals(carYear, car.carYear());
        assertEquals(seatCount, car.seatCount());
        assertEquals(createdAt, car.createdAt());
    }

    @Test
    void shouldThrowWhenOwnerIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Car.of(null, generateLicensePlate(), generateCarBrand(), generateCarModel(),
                        generateCarColor(), generateCarYear(), generateSeatCount()));
    }

    @Test
    void shouldThrowWhenLicensePlateIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Car.of(UserID.newID(), null, generateCarBrand(), generateCarModel(),
                        generateCarColor(), generateCarYear(), generateSeatCount()));
    }

    @Test
    void shouldThrowWhenCarBrandIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Car.of(UserID.newID(), generateLicensePlate(), null, generateCarModel(),
                        generateCarColor(), generateCarYear(), generateSeatCount()));
    }

    @Test
    void shouldThrowWhenCarModelIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Car.of(UserID.newID(), generateLicensePlate(), generateCarBrand(), null,
                        generateCarColor(), generateCarYear(), generateSeatCount()));
    }

    @Test
    void shouldThrowWhenCarColorIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Car.of(UserID.newID(), generateLicensePlate(), generateCarBrand(), generateCarModel(),
                        null, generateCarYear(), generateSeatCount()));
    }

    @Test
    void shouldThrowWhenCarYearIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Car.of(UserID.newID(), generateLicensePlate(), generateCarBrand(), generateCarModel(),
                        generateCarColor(), null, generateSeatCount()));
    }

    @Test
    void shouldThrowWhenSeatCountIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Car.of(UserID.newID(), generateLicensePlate(), generateCarBrand(), generateCarModel(),
                        generateCarColor(), generateCarYear(), null));
    }

    @Test
    void shouldBeEqualWhenIdIsSame() {
        var id = new CarID(UUID.randomUUID());
        var c1 = Car.fromRepository(id, UserID.newID(), generateLicensePlate(), generateCarBrand(),
                generateCarModel(), generateCarColor(), generateCarYear(), generateSeatCount(), LocalDateTime.now());
        var c2 = Car.fromRepository(id, UserID.newID(), generateLicensePlate(), generateCarBrand(),
                generateCarModel(), generateCarColor(), generateCarYear(), generateSeatCount(), LocalDateTime.now().minusDays(1));

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenIdIsDifferent() {
        var c1 = car();
        var c2 = car();

        assertNotEquals(c1, c2);
    }
}
