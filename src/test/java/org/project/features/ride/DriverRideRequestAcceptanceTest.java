package org.project.features.ride;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.ride.entities.RideRequest;
import org.project.domain.ride.value_object.RideRequestID;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.cache.RideRequests;
import org.project.infrastructure.security.JWTUtility;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class DriverRideRequestAcceptanceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    DriverRepository driverRepository;

    @Inject
    OwnerRepository ownerRepository;

    @Inject
    CarRepository carRepository;

    @Inject
    RideRequests rideRequests;

    @Inject
    JWTUtility jwtUtility;

    @Test
    void successfullyGetRideRequests() {
        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);
        String driverJwtToken = jwtUtility.generateToken(driverUser);

        User ownerUser = TestDataGenerator.user();
        userRepository.save(ownerUser);
        Owner owner = TestDataGenerator.owner();
        ownerRepository.save(owner);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        carRepository.save(car);

        createRideRequestForDriver(driver, owner, car);

        given().header("Authorization", "Bearer " + driverJwtToken)
                .when()
                .get("/uyol/driver/ride-requests")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].driverID", equalTo(driver.id().value().toString()))
                .body("[0].licensePlate", equalTo(plate.value()));
    }

    @Test
    void getRideRequestsWithNoDriverAccount() {
        User user = TestDataGenerator.user();
        userRepository.save(user);
        String jwtToken = jwtUtility.generateToken(user);

        given().header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/uyol/driver/ride-requests")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void successfullyAcceptRideRequest() {
        User driverUser = TestDataGenerator.user();
        assertTrue(userRepository.save(driverUser).success());
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        assertTrue(driverRepository.save(driver).success());
        String driverJwtToken = jwtUtility.generateToken(driverUser);

        User ownerUser = TestDataGenerator.user();
        assertTrue(userRepository.save(ownerUser).success());
        Owner owner = Owner.of(ownerUser.userID(), TestDataGenerator.voen());
        assertTrue(ownerRepository.save(owner).success());

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        assertTrue(carRepository.save(car).success());

        RideRequest rideRequest = createRideRequestForDriver(driver, owner, car);

        given().header("Authorization", "Bearer " + driverJwtToken)
                .queryParam("rideRequestID", rideRequest.id().value())
                .when()
                .post("/uyol/driver/accept/ride-request")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", notNullValue())
                .body("driverId", equalTo(driver.id().value().toString()))
                .body("ownerId", equalTo(owner.id().value().toString()));

        given().header("Authorization", "Bearer " + driverJwtToken)
                .when()
                .get("/uyol/driver/ride-requests")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("size()", equalTo(0));
    }

    @Test
    void acceptNonExistentRideRequest() {
        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);
        String driverJwtToken = jwtUtility.generateToken(driverUser);

        given().header("Authorization", "Bearer " + driverJwtToken)
                .queryParam("rideRequestID", UUID.randomUUID())
                .when()
                .post("/uyol/driver/accept/ride-request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void acceptRideRequestWithNoDriverAccount() {
        User user = TestDataGenerator.user();
        userRepository.save(user);
        String jwtToken = jwtUtility.generateToken(user);

        given().header("Authorization", "Bearer " + jwtToken)
                .queryParam("rideRequestID", UUID.randomUUID())
                .when()
                .post("/uyol/driver/accept/ride-request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void acceptRideRequestForDifferentDriver() {
        User requestingDriverUser = TestDataGenerator.user();
        userRepository.save(requestingDriverUser);
        Driver requestingDriver = Driver.of(requestingDriverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(requestingDriver);
        String requestingDriverJwtToken = jwtUtility.generateToken(requestingDriverUser);

        User actualDriverUser = TestDataGenerator.user();
        userRepository.save(actualDriverUser);
        Driver actualDriver = Driver.of(actualDriverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(actualDriver);

        User ownerUser = TestDataGenerator.user();
        userRepository.save(ownerUser);
        Owner owner = Owner.of(ownerUser.userID(), TestDataGenerator.voen());
        ownerRepository.save(owner);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        carRepository.save(car);

        RideRequest rideRequest = createRideRequestForDriver(actualDriver, owner, car);

        given().header("Authorization", "Bearer " + requestingDriverJwtToken)
                .queryParam("rideRequestID", rideRequest.id().value())
                .when()
                .post("/uyol/driver/accept/ride-request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void acceptRideRequestWithNonExistentCar() {
        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);
        String driverJwtToken = jwtUtility.generateToken(driverUser);

        User ownerUser = TestDataGenerator.user();
        userRepository.save(ownerUser);
        Owner owner = TestDataGenerator.owner();
        ownerRepository.save(owner);

        RideRequest rideRequest = new RideRequest(
                new RideRequestID(UUID.randomUUID()),
                driver.id(),
                owner.id(),
                new LicensePlate("NONEXISTENT"),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules(),
                java.time.LocalDateTime.now()
        );
        rideRequests.put(driver.id(), rideRequest);

        given().header("Authorization", "Bearer " + driverJwtToken)
                .queryParam("rideRequestID", rideRequest.id().value())
                .when()
                .post("/uyol/driver/accept/ride-request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getEmptyRideRequestsList() {
        User driverUser = TestDataGenerator.user();
        userRepository.save(driverUser);
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        driverRepository.save(driver);
        String driverJwtToken = jwtUtility.generateToken(driverUser);

        given().header("Authorization", "Bearer " + driverJwtToken)
                .when()
                .get("/uyol/driver/ride-requests")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("size()", equalTo(0));
    }

    @Test
    void acceptAlreadyAcceptedRideRequest() {
        User driverUser = TestDataGenerator.user();
        assertTrue(userRepository.save(driverUser).success());
        Driver driver = Driver.of(driverUser.userID(), TestDataGenerator.driverLicense());
        assertTrue(driverRepository.save(driver).success());
        String driverJwtToken = jwtUtility.generateToken(driverUser);

        User ownerUser = TestDataGenerator.user();
        assertTrue(userRepository.save(ownerUser).success());
        Owner owner = Owner.of(ownerUser.userID(), TestDataGenerator.voen());
        assertTrue(ownerRepository.save(owner).success());

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = TestDataGenerator.car(owner.userID(), plate);
        carRepository.save(car);

        RideRequest rideRequest = createRideRequestForDriver(driver, owner, car);

        given().header("Authorization", "Bearer " + driverJwtToken)
                .queryParam("rideRequestID", rideRequest.id().value())
                .when()
                .post("/uyol/driver/accept/ride-request")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().header("Authorization", "Bearer " + driverJwtToken)
                .queryParam("rideRequestID", rideRequest.id().value())
                .when()
                .post("/uyol/driver/accept/ride-request")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    private RideRequest createRideRequestForDriver(Driver driver, Owner owner, Car car) {
        RideRequest rideRequest = new RideRequest(
                new RideRequestID(UUID.randomUUID()),
                driver.id(),
                owner.id(),
                car.licensePlate(),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules(),
                java.time.LocalDateTime.now()
        );
        rideRequests.put(driver.id(), rideRequest);
        return rideRequest;
    }
}