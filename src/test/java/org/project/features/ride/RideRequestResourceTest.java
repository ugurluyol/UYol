package org.project.features.ride;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.project.application.dto.ride.RideRequestToDriver;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.security.JWTUtility;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class RideRequestResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    DriverRepository driverRepository;

    @Inject
    OwnerRepository ownerRepository;

    @Inject
    CarRepository carRepository;

    @Inject
    JWTUtility jwtUtility;

    @Test
    void successfullyRequestRideToDriver() {
        User ownerUser = TestDataGenerator.user();
        assertTrue(userRepository.save(ownerUser).success());
        Owner owner = Owner.of(new UserID(ownerUser.id()), TestDataGenerator.voen());
        assertTrue(ownerRepository.save(owner).success());
        String ownerJwtToken = jwtUtility.generateToken(ownerUser);

        User driverUser = TestDataGenerator.user();
        assertTrue(userRepository.save(driverUser).success());
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        assertTrue(driverRepository.save(driver).success());

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        assertTrue(carRepository.save(car).success());

        SeatStatus[][] seatMap = TestDataGenerator.generateSeatMap().seats();

        RideRequestToDriver rideRequest = new RideRequestToDriver(
                driver.id().value(),
                plate.value(),
                "From location",
                40.0,
                50.0,
                "To location",
                41.0,
                51.0,
                seatMap,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BigDecimal.valueOf(15.0),
                "Test ride request",
                new org.project.domain.ride.enumerations.RideRule[]{org.project.domain.ride.enumerations.RideRule.NO_SMOKING}
        );

        given().header("Authorization", "Bearer " + ownerJwtToken)
                .contentType(ContentType.JSON)
                .body(rideRequest)
                .when()
                .post("/uyol/owner/ride/request")
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void requestRideWithNonExistentOwner() {
        User fakeUser = TestDataGenerator.user();
        String fakeJwtToken = jwtUtility.generateToken(fakeUser);

        RideRequestToDriver rideRequest = new RideRequestToDriver(
                UUID.randomUUID(),
                "ABC123",
                "From location",
                40.0,
                50.0,
                "To location",
                41.0,
                51.0,
                new SeatStatus[0][0],
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BigDecimal.valueOf(15.0),
                "Test ride",
                new org.project.domain.ride.enumerations.RideRule[0]
        );

        given().header("Authorization", "Bearer " + fakeJwtToken)
                .contentType(ContentType.JSON)
                .body(rideRequest)
                .when()
                .post("/uyol/owner/ride/request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void requestRideWithNonExistentCar() {
        User ownerUser = TestDataGenerator.user();
        userRepository.save(ownerUser);
        Owner owner = Owner.of(new UserID(ownerUser.id()), TestDataGenerator.voen());
        ownerRepository.save(owner);
        String ownerJwtToken = jwtUtility.generateToken(ownerUser);

        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);

        RideRequestToDriver rideRequest = new RideRequestToDriver(
                driver.id().value(),
                "NONEXISTENT",
                "From location",
                40.0,
                50.0,
                "To location",
                41.0,
                51.0,
                TestDataGenerator.generateSeatMap().seats(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BigDecimal.valueOf(15.0),
                "Test ride",
                new org.project.domain.ride.enumerations.RideRule[0]
        );

        given().header("Authorization", "Bearer " + ownerJwtToken)
                .contentType(ContentType.JSON)
                .body(rideRequest)
                .when()
                .post("/uyol/owner/ride/request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void requestRideWithCarNotOwnedByUser() {
        User requestingOwnerUser = TestDataGenerator.user();
        userRepository.save(requestingOwnerUser);
        Owner requestingOwner = Owner.of(requestingOwnerUser.userID(), TestDataGenerator.voen());
        ownerRepository.save(requestingOwner);
        String requestingOwnerJwtToken = jwtUtility.generateToken(requestingOwnerUser);

        User actualOwnerUser = TestDataGenerator.user();
        userRepository.save(actualOwnerUser);
        Owner actualOwner = TestDataGenerator.owner();
        ownerRepository.save(actualOwner);

        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(actualOwner.userID(), plate);
        carRepository.save(car);

        RideRequestToDriver rideRequest = new RideRequestToDriver(
                driver.id().value(),
                plate.value(),
                "From location",
                40.0,
                50.0,
                "To location",
                41.0,
                51.0,
                TestDataGenerator.generateSeatMap().seats(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BigDecimal.valueOf(15.0),
                "Test ride",
                new org.project.domain.ride.enumerations.RideRule[0]
        );

        given().header("Authorization", "Bearer " + requestingOwnerJwtToken)
                .contentType(ContentType.JSON)
                .body(rideRequest)
                .when()
                .post("/uyol/owner/ride/request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void requestRideWithNonExistentDriver() {
        User ownerUser = TestDataGenerator.user();
        userRepository.save(ownerUser);
        Owner owner = Owner.of(new UserID(ownerUser.id()), TestDataGenerator.voen());
        ownerRepository.save(owner);
        String ownerJwtToken = jwtUtility.generateToken(ownerUser);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        carRepository.save(car);

        RideRequestToDriver rideRequest = new RideRequestToDriver(
                UUID.randomUUID(),
                plate.value(),
                "From location",
                40.0,
                50.0,
                "To location",
                41.0,
                51.0,
                TestDataGenerator.generateSeatMap().seats(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BigDecimal.valueOf(15.0),
                "Test ride",
                new org.project.domain.ride.enumerations.RideRule[0]
        );

        given().header("Authorization", "Bearer " + ownerJwtToken)
                .contentType(ContentType.JSON)
                .body(rideRequest)
                .when()
                .post("/uyol/owner/ride/request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void requestRideWithInvalidSeatMap() {
        User ownerUser = TestDataGenerator.user();
        userRepository.save(ownerUser);
        Owner owner = Owner.of(new UserID(ownerUser.id()), TestDataGenerator.voen());
        ownerRepository.save(owner);
        String ownerJwtToken = jwtUtility.generateToken(ownerUser);

        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        carRepository.save(car);

        SeatStatus[][] invalidSeatMap = new SeatStatus[4][1];
        for (int i = 0; i < invalidSeatMap.length; i++) {
            invalidSeatMap[i][0] = SeatStatus.EMPTY;
        }

        RideRequestToDriver rideRequest = new RideRequestToDriver(
                driver.id().value(),
                plate.value(),
                "From location",
                40.0,
                50.0,
                "To location",
                41.0,
                51.0,
                invalidSeatMap,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BigDecimal.valueOf(15.0),
                "Test ride",
                new org.project.domain.ride.enumerations.RideRule[0]
        );

        given().header("Authorization", "Bearer " + ownerJwtToken)
                .contentType(ContentType.JSON)
                .body(rideRequest)
                .when()
                .post("/uyol/owner/ride/request")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void requestRideWithPastStartTime() {
        User ownerUser = TestDataGenerator.user();
        userRepository.save(ownerUser);
        Owner owner = Owner.of(new UserID(ownerUser.id()), TestDataGenerator.voen());
        ownerRepository.save(owner);
        String ownerJwtToken = jwtUtility.generateToken(ownerUser);

        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        carRepository.save(car);

        RideRequestToDriver rideRequest = new RideRequestToDriver(
                driver.id().value(),
                plate.value(),
                "From location",
                40.0,
                50.0,
                "To location",
                41.0,
                51.0,
                TestDataGenerator.generateSeatMap().seats(),
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1),
                BigDecimal.valueOf(15.0),
                "Test ride",
                new org.project.domain.ride.enumerations.RideRule[0]
        );

        given().header("Authorization", "Bearer " + ownerJwtToken)
                .contentType(ContentType.JSON)
                .body(rideRequest)
                .when()
                .post("/uyol/owner/ride/request")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void requestRideWithEndTimeBeforeStartTime() {
        User ownerUser = TestDataGenerator.user();
        userRepository.save(ownerUser);
        Owner owner = Owner.of(new UserID(ownerUser.id()), TestDataGenerator.voen());
        ownerRepository.save(owner);
        String ownerJwtToken = jwtUtility.generateToken(ownerUser);

        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        carRepository.save(car);

        RideRequestToDriver rideRequest = new RideRequestToDriver(
                driver.id().value(),
                plate.value(),
                "From location",
                40.0,
                50.0,
                "To location",
                41.0,
                51.0,
                TestDataGenerator.generateSeatMap().seats(),
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(2),
                BigDecimal.valueOf(15.0),
                "Test ride",
                new org.project.domain.ride.enumerations.RideRule[0]
        );

        given().header("Authorization", "Bearer " + ownerJwtToken)
                .contentType(ContentType.JSON)
                .body(rideRequest)
                .when()
                .post("/uyol/owner/ride/request")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}