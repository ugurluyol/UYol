package org.project.features.fleet;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.project.application.dto.fleet.CarDTO;
import org.project.application.dto.ride.DriverRideForm;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.ride.enumerations.RideRule;
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
class DriverResourceTest {

    @Inject
	UserRepository userRepository;

	@Inject
	DriverRepository driverRepository;

	@Inject
	CarRepository carRepository;

	@Inject
	JWTUtility jwtUtility;

	@Test
    void successfullyRegisterDriver() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		DriverLicense license = TestDataGenerator.driverLicense();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("driver_license", license.licenseNumber())
				.when().post("uyol/driver/registration").then().statusCode(202);
	}

	@Test
	void invalidDriverLicense() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		given().header("Authorization", "Bearer " + jwtToken).queryParam("driver_license", "").when()
				.post("uyol/driver/registration").then().statusCode(400);
	}

	@Test
	void userAccountDontExists() {
		User fakeUser = TestDataGenerator.user();
		String jwtToken = jwtUtility.generateToken(fakeUser);

		DriverLicense license = TestDataGenerator.driverLicense();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("driver_license", license.licenseNumber())
				.when().post("uyol/driver/registration").then().statusCode(404);
	}

    @Test
	void driverAlreadyExists() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		DriverLicense license = TestDataGenerator.driverLicense();


		given().header("Authorization", "Bearer " + jwtToken).queryParam("driver_license", license.licenseNumber())
				.when().post("uyol/driver/registration").then().statusCode(202);


		given().header("Authorization", "Bearer " + jwtToken).queryParam("driver_license", license.licenseNumber())
				.when().post("uyol/driver/registration").then().statusCode(409);
    }

    @Test
	void driverLicenseAlreadyExists() {

		User user1 = TestDataGenerator.user();
		userRepository.save(user1);
		String jwtToken1 = jwtUtility.generateToken(user1);

		DriverLicense license = TestDataGenerator.driverLicense();

		given().header("Authorization", "Bearer " + jwtToken1).queryParam("driver_license", license.licenseNumber())
				.when().post("uyol/driver/registration").then().statusCode(202);


		User user2 = TestDataGenerator.user();
		userRepository.save(user2);
		String jwtToken2 = jwtUtility.generateToken(user2);

		given().header("Authorization", "Bearer " + jwtToken2).queryParam("driver_license", license.licenseNumber())
				.when().post("uyol/driver/registration").then().statusCode(409);
    }

	@Test
	void successfullySaveCar() {

		User user = TestDataGenerator.user();
		userRepository.save(user);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		String jwtToken = jwtUtility.generateToken(user);

		CarDTO carDTO = TestDataGenerator.carForm();

		given().header("Authorization", "Bearer " + jwtToken).contentType(ContentType.JSON).body(carDTO).when()
				.post("/uyol/driver/car/save").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void successfullyRemoveRideRule() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		
		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		String jwtToken = jwtUtility.generateToken(user);

		LicensePlate plate = TestDataGenerator.generateLicensePlate();
		Car car = Car.of(driver.userID(), plate, TestDataGenerator.generateCarBrand(),
				TestDataGenerator.generateCarModel(), TestDataGenerator.generateCarColor(),
				TestDataGenerator.generateCarYear(), TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		var rideForm = TestDataGenerator.driverRideForm(plate);

		UUID rideID = given().header("Authorization", "Bearer " + jwtToken).contentType(ContentType.JSON).body(rideForm)
				.when().post("/uyol/driver/create/ride").then().statusCode(Response.Status.OK.getStatusCode()).extract()
				.body().jsonPath().getUUID("id");

		given().header("Authorization", "Bearer " + jwtToken).queryParam("ride-rule", RideRule.NO_SMOKING)
				.queryParam("rideID", rideID).when().patch("/uyol/driver/add/ride-rule").then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());

		given().header("Authorization", "Bearer " + jwtToken).queryParam("ride-rule", RideRule.NO_SMOKING)
				.queryParam("rideID", rideID).when().patch("/uyol/driver/remove/ride-rule").then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void successfullyAddRideRule() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		String jwtToken = jwtUtility.generateToken(user);

		LicensePlate plate = TestDataGenerator.generateLicensePlate();
		Car car = Car.of(driver.userID(), plate, TestDataGenerator.generateCarBrand(),
				TestDataGenerator.generateCarModel(), TestDataGenerator.generateCarColor(),
				TestDataGenerator.generateCarYear(), TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		SeatStatus[][] seatMap = new SeatStatus[car.seatCount().value()][1];
		for (int i = 0; i < seatMap.length; i++)
			seatMap[i][0] = SeatStatus.EMPTY;
		seatMap[0][0] = SeatStatus.DRIVER;

		DriverRideForm rideForm = new DriverRideForm(plate.value(), "From location", 40.0, 50.0, "To location", 41.0,
				51.0, seatMap, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
				BigDecimal.valueOf(10.0), "Test ride", new RideRule[] {});

		UUID rideID = given().header("Authorization", "Bearer " + jwtToken).contentType(ContentType.JSON).body(rideForm)
				.when().post("/uyol/driver/create/ride").then().statusCode(Response.Status.OK.getStatusCode()).extract()
				.body().jsonPath().getUUID("id");

		given().header("Authorization", "Bearer " + jwtToken).queryParam("ride-rule", RideRule.NO_SMOKING)
				.queryParam("rideID", rideID).when().patch("/uyol/driver/add/ride-rule").then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}


	@Test
	void successfullyCreateRide() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		String jwtToken = jwtUtility.generateToken(user);

		LicensePlate plate = TestDataGenerator.generateLicensePlate();
		Car car = Car.of(driver.userID(), plate, TestDataGenerator.generateCarBrand(),
				TestDataGenerator.generateCarModel(), TestDataGenerator.generateCarColor(),
				TestDataGenerator.generateCarYear(), TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		SeatStatus[][] seatMap = new SeatStatus[car.seatCount().value()][1];
		for (int i = 0; i < seatMap.length; i++)
			seatMap[i][0] = SeatStatus.EMPTY;
		seatMap[0][0] = SeatStatus.DRIVER;

		DriverRideForm rideForm = new DriverRideForm(plate.value(), "From location", 40.0, 50.0, "To location", 41.0,
				51.0, seatMap, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
				BigDecimal.valueOf(15.0), "Test ride", new RideRule[] {});

		given().header("Authorization", "Bearer " + jwtToken).contentType(ContentType.JSON).body(rideForm).when()
				.post("/uyol/driver/create/ride").then().statusCode(Response.Status.OK.getStatusCode());
	}

	@Test
	void successfullyStartRide() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		String jwtToken = jwtUtility.generateToken(user);

		LicensePlate plate = TestDataGenerator.generateLicensePlate();
		Car car = Car.of(driver.userID(), plate, TestDataGenerator.generateCarBrand(),
				TestDataGenerator.generateCarModel(), TestDataGenerator.generateCarColor(),
				TestDataGenerator.generateCarYear(), TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		DriverRideForm rideForm = TestDataGenerator.driverRideForm(plate);
		UUID rideID = given().header("Authorization", "Bearer " + jwtToken).contentType(ContentType.JSON).body(rideForm)
				.when().post("/uyol/driver/create/ride").then().statusCode(Response.Status.OK.getStatusCode()).extract()
				.body().jsonPath().getUUID("id");

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/driver/start/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void successfullyCancelRide() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		String jwtToken = jwtUtility.generateToken(user);

		LicensePlate plate = TestDataGenerator.generateLicensePlate();
		Car car = Car.of(driver.userID(), plate, TestDataGenerator.generateCarBrand(),
				TestDataGenerator.generateCarModel(), TestDataGenerator.generateCarColor(),
				TestDataGenerator.generateCarYear(), TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		DriverRideForm rideForm = TestDataGenerator.driverRideForm(plate);
		UUID rideID = given().header("Authorization", "Bearer " + jwtToken).contentType(ContentType.JSON).body(rideForm)
				.when().post("/uyol/driver/create/ride").then().statusCode(Response.Status.OK.getStatusCode()).extract()
				.body().jsonPath().getUUID("id");

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/driver/cancel/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void successfullyFinishRide() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		String jwtToken = jwtUtility.generateToken(user);

		LicensePlate plate = TestDataGenerator.generateLicensePlate();
		Car car = Car.of(driver.userID(), plate, TestDataGenerator.generateCarBrand(),
				TestDataGenerator.generateCarModel(), TestDataGenerator.generateCarColor(),
				TestDataGenerator.generateCarYear(), TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		DriverRideForm rideForm = TestDataGenerator.driverRideForm(plate);
		UUID rideID = given().header("Authorization", "Bearer " + jwtToken).contentType(ContentType.JSON).body(rideForm)
				.when().post("/uyol/driver/create/ride").then().statusCode(Response.Status.OK.getStatusCode()).extract()
				.body().jsonPath().getUUID("id");

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/driver/start/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/driver/finish/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}



}
