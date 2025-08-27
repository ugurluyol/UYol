package org.project.features.ride;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
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

import java.util.*;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class RideReservationResourceTest {

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

    private UUID createRideAndReturnId(User driverUser) {
        userRepository.save(driverUser);
        Driver driver = Driver.of(new UserID(driverUser.id()), TestDataGenerator.driverLicense());
        driverRepository.save(driver);

        String driverJwt = jwtUtility.generateToken(driverUser);

        LicensePlate plate = TestDataGenerator.generateLicensePlate();
        Car car = Car.of(driver.userID(), plate, TestDataGenerator.generateCarBrand(),
                TestDataGenerator.generateCarModel(), TestDataGenerator.generateCarColor(),
                TestDataGenerator.generateCarYear(), TestDataGenerator.generateSeatCount());
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

    @Test
    void bookRide_integration_happyPath() {
        User driverUser = TestDataGenerator.user();
        UUID rideID = createRideAndReturnId(driverUser);

        User passenger = TestDataGenerator.user();
        userRepository.save(passenger);
        String passengerJwt = jwtUtility.generateToken(passenger);

        Ride ride = jetRideRepository.findBy(new RideID(rideID)).orElseThrow();
        int emptySeatIndex = 2;
        for (int i = 0; i < ride.seatMap().size(); i++) {
            if (ride.seatMap().isAvailable(i)) emptySeatIndex = i;
        }

        Map<String, Object> bookingBody = new HashMap<>();
        bookingBody.put("rideID", rideID.toString());
        bookingBody.put("bookedSeats", List.of(new PassengerSeat(emptySeatIndex, SeatStatus.FEMALE_OCCUPIED)));

        given()
                .header("Authorization", "Bearer " + passengerJwt)
                .contentType(ContentType.JSON)
                .body(bookingBody)
                .when()
                .post("/uyol/ride/reservation/book")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void rateDriver_integration_happyPath() {
        User driverUser = TestDataGenerator.user();
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
        UUID rideID = given()
                .header("Authorization", "Bearer " + driverJwt)
                .contentType(ContentType.JSON)
                .body(rideForm)
                .when()
                .post("/uyol/driver/create/ride")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().jsonPath().getUUID("id");

        User passenger = TestDataGenerator.user();
        userRepository.save(passenger);
        String passengerJwt = jwtUtility.generateToken(passenger);

        Ride ride = jetRideRepository.findBy(new RideID(rideID)).orElseThrow();
        int emptySeatIndex = 2;
        for (int i = 0; i < ride.seatMap().size(); i++) {
            if (ride.seatMap().isAvailable(i)) emptySeatIndex = i;
        }

        Map<String, Object> bookingBody = new HashMap<>();
        bookingBody.put("rideID", rideID.toString());
        bookingBody.put("bookedSeats", List.of(new PassengerSeat(emptySeatIndex, SeatStatus.FEMALE_OCCUPIED)));

        given()
                .header("Authorization", "Bearer " + passengerJwt)
                .contentType(ContentType.JSON)
                .body(bookingBody)
                .when()
                .post("/uyol/ride/reservation/book")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .header("Authorization", "Bearer " + driverJwt)
                .queryParam("rideID", rideID)
                .when()
                .post("/uyol/driver/start/ride")
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        given()
                .header("Authorization", "Bearer " + driverJwt)
                .queryParam("rideID", rideID)
                .when()
                .post("/uyol/driver/finish/ride")
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        given()
                .header("Authorization", "Bearer " + passengerJwt)
                .queryParam("rideID", rideID)
                .queryParam("score", 5)
                .when()
                .post("/uyol/ride/reservation/rate/driver")
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void rateDriver_integration_invalidScore() {
        User driverUser = TestDataGenerator.user();
        UUID rideID = createRideAndReturnId(driverUser);

        User passenger = TestDataGenerator.user();
        userRepository.save(passenger);
        String passengerJwt = jwtUtility.generateToken(passenger);

        given()
                .header("Authorization", "Bearer " + passengerJwt)
                .queryParam("rideID", rideID)
                .queryParam("score", -1)
                .when()
                .post("/uyol/ride/reservation/rate/driver")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}