package org.project.features.ride;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.project.application.dto.ride.DriverRideForm;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.value_object.PassengerSeat;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.repository.JetRideRepository;
import org.project.infrastructure.security.JWTUtility;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class RideContractResourceIntegrationTest {

    @Inject
    UserRepository userRepository;

    @Inject
    DriverRepository driverRepository;

    @Inject
    CarRepository carRepository;

    @Inject
    JWTUtility jwtUtility;
    @Inject
    JetRideRepository jetRideRepository;

    private UUID createRide(User driverUser) {
        userRepository.save(driverUser);
        Driver driver = Driver.of(new UserID(driverUser.id()), TestDataGenerator.driverLicense());
        driverRepository.save(driver);

        String driverJwt = jwtUtility.generateToken(driverUser);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = Car.of(driver.userID(), plate,
                TestDataGenerator.generateCarBrand(),
                TestDataGenerator.generateCarModel(),
                TestDataGenerator.generateCarColor(),
                TestDataGenerator.generateCarYear(),
                TestDataGenerator.generateSeatCount());
        carRepository.save(car);

        DriverRideForm rideForm = TestDataGenerator.driverRideForm(plate);

        return given()
                .header("Authorization", "Bearer " + driverJwt)
                .contentType(ContentType.JSON)
                .body(rideForm)
                .when()
                .post("/uyol/driver/create/ride")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().jsonPath().getUUID("id");
    }

    private UUID bookRide(UUID rideID, User passenger) {
        userRepository.save(passenger);
        String passengerJwt = jwtUtility.generateToken(passenger);

        Ride ride = jetRideRepository.findBy(new RideID(rideID)).orElseThrow();
        int emptySeatIndex = 2;
        for (int i = 0; i < ride.seatMap().size(); i++) {
            if (ride.seatMap().isAvailable(i)) emptySeatIndex = i;
        }

        Map<String, Object> bookingBody = new HashMap<>();
        bookingBody.put("rideID", rideID.toString());
        bookingBody.put("bookedSeats",  List.of(new PassengerSeat(emptySeatIndex, SeatStatus.FEMALE_OCCUPIED)));

        return given()
                .header("Authorization", "Bearer " + passengerJwt)
                .contentType(ContentType.JSON)
                .body(bookingBody)
                .when()
                .post("/uyol/ride/reservation/book")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().jsonPath().getUUID("rideContractID");
    }

    @Test
    void getRideContract_byId_happyPath() {
        User driverUser = TestDataGenerator.user();
        UUID rideID = createRide(driverUser);

        User passenger = TestDataGenerator.user();
        UUID contractID = bookRide(rideID, passenger);

        String passengerJwt = jwtUtility.generateToken(passenger);

        given()
                .header("Authorization", "Bearer " + passengerJwt)
                .queryParam("rideContractID", contractID)
        .when()
                .get("/uyol/ride/contract")
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("rideContractID", Matchers.equalTo(contractID.toString()))
                .body("rideID", Matchers.equalTo(rideID.toString()));
    }

    @Test
    void getRideContracts_ofRide() {
        User driverUser = TestDataGenerator.user();
        UUID rideID = createRide(driverUser);

        User passenger = TestDataGenerator.user();
        bookRide(rideID, passenger);

        String passengerJwt = jwtUtility.generateToken(passenger);

        List<Map<String, Object>> contracts = given()
                .header("Authorization", "Bearer " + passengerJwt)
                .queryParam("rideID", rideID)
                .queryParam("page", 0)
                .queryParam("size", 10)
        .when()
                .get("/uyol/ride/contract/of/ride")
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().jsonPath().getList("$");

        org.junit.jupiter.api.Assertions.assertFalse(contracts.isEmpty());
    }

    @Test
    void getRideContracts_ofUser() {
        User driverUser = TestDataGenerator.user();
        UUID rideID = createRide(driverUser);

        User passenger = TestDataGenerator.user();
        bookRide(rideID, passenger);

        String passengerJwt = jwtUtility.generateToken(passenger);

        List<Map<String, Object>> contracts = given()
                .header("Authorization", "Bearer " + passengerJwt)
                .queryParam("page", 0)
                .queryParam("size", 10)
        .when()
                .get("/uyol/ride/contract/all")
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().jsonPath().getList("$");

        org.junit.jupiter.api.Assertions.assertFalse(contracts.isEmpty());
    }
}
