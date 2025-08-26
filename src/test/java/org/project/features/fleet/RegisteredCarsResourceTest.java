package org.project.features.fleet;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.project.application.dto.fleet.CarDTO;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.security.JWTUtility;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegisteredCarsResourceTest {

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

    private User user;
    private Driver driver;
    private Owner owner;

    @BeforeAll
    void setUp() {
        this.user = TestDataGenerator.user();
        userRepository.save(user);
        this.driver = Driver.of(new UserID(this.user.id()), TestDataGenerator.driverLicense());
        driverRepository.save(driver);
        this.owner = Owner.of(new UserID(this.user.id()), TestDataGenerator.voen());
        ownerRepository.save(owner);

        List<Car> testCars = Stream.generate(() -> Car.of(new UserID(user.id()),
                TestDataGenerator.generateLicensePlate(),
                TestDataGenerator.generateCarBrand(),
                TestDataGenerator.generateCarModel(),
                TestDataGenerator.generateCarColor(),
                TestDataGenerator.generateCarYear(),
                TestDataGenerator.generateSeatCount())
        ).limit(10).toList();

        testCars.forEach(carRepository::save);
    }


    @Test
    void successfulRideHistoryRead() {
        String token = jwtUtility.generateToken(user);

        List<CarDTO> cars = given()
                .header("Authorization", "Bearer " + token)
                .queryParam("pageNumber", 0)
                .queryParam("pageSize", 10)
                .when()
                .get("/uyol/registered/cars")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<List<CarDTO>>() {});

        assertNotNull(cars);
        assertEquals(10, cars.size());
    }
}
