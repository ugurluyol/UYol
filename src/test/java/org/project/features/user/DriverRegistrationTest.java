package org.project.features.user;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.project.application.dto.fleet.CarForm;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.infrastructure.security.JWTUtility;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class DriverRegistrationTest {

    @Inject
	UserRepository userRepository;

	@Inject
	DriverRepository driverRepository;

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

//	@Test
//	void successfullySaveCar() {
//		User user = TestDataGenerator.user();
//		userRepository.save(user);
//
//		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
//		driverRepository.save(driver);
//
//		String jwtToken = jwtUtility.generateToken(user);
//
//		CarForm carForm = TestDataGenerator.carForm();
//
//		given().header("Authorization", "Bearer " + jwtToken).contentType("application/json").body(carForm).when()
//				.post("/uyol/driver/car/save")
//				.then().statusCode(Response.Status.ACCEPTED.getStatusCode());
//	}

	@Test
	void successfullySaveCar() {

		User user = TestDataGenerator.user();
		userRepository.save(user);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		String jwtToken = jwtUtility.generateToken(user);

		CarForm carForm = TestDataGenerator.carForm();

		given().header("Authorization", "Bearer " + jwtToken).contentType(ContentType.JSON).body(carForm).when()
				.post("/uyol/driver/car/save").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

}
