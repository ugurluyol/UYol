package org.project.features.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.project.application.dto.ride.RideDTO;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.ride.enumerations.RideStatus;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.repository.JetCarRepository;
import org.project.infrastructure.repository.JetDriverRepository;
import org.project.infrastructure.repository.JetUserRepository;
import org.project.infrastructure.security.JWTUtility;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class DriverRideRegistrationTest {

    @Inject
    JetUserRepository userRepo;

    @Inject
    JetDriverRepository driverRepo;

    @Inject
    JetCarRepository carRepo;

    @Inject
    JWTUtility jwtUtility;

    ObjectMapper objectMapper = JsonMapper.builder()
            .enable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES)
            .addModule(new JavaTimeModule())
            .build();

    @Test
    void successfulRideRegistration() throws JsonProcessingException {
        User user = TestDataGenerator.user();
        userRepo.save(user);

        UserID userID = new UserID(user.id());

        Driver driver = Driver.of(userID, TestDataGenerator.driverLicense());
        driverRepo.save(driver);

        Car car = TestDataGenerator.car(userID);
        carRepo.save(car);

        String token = jwtUtility.generateToken(user);
        String body = objectMapper.writeValueAsString(TestDataGenerator.driverRideForm(car.licensePlate()));

        RideDTO createdRide = given()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post("/uyol/driver/create/ride")
                .then()
                .statusCode(200)
                .extract().body().as(RideDTO.class);

        Assertions.assertNotNull(createdRide);
        Assertions.assertEquals(driver.id().value().toString(), createdRide.driverId());
        Assertions.assertEquals(RideStatus.PENDING, createdRide.status());
    }
}
