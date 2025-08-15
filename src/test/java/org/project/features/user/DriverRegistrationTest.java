package org.project.features.user;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.infrastructure.security.JWTUtility;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class DriverRegistrationTest {

    @Inject
	UserRepository userRepository;

	@Inject
	JWTUtility jwtUtility;

	@Test
    void successfullyRegisterDriver() throws JsonProcessingException {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		DriverLicense license = TestDataGenerator.driverLicense();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("driver_license", license.licenseNumber())
				.when().post("uyol/driver/registration").then().statusCode(202);
	}

	@Test
	void invalidDriverLicense() throws JsonProcessingException {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		given().header("Authorization", "Bearer " + jwtToken).queryParam("driver_license", "").when()
				.post("uyol/driver/registration").then().statusCode(400);
	}

	@Test
	void invalidIdentifier() {
		User fakeUser = TestDataGenerator.user();
		String jwtToken = jwtUtility.generateToken(fakeUser);

		DriverLicense license = TestDataGenerator.driverLicense();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("driver_license", license.licenseNumber())
				.when().post("uyol/driver/registration").then().statusCode(404);
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
	void driverAlreadyExists() throws JsonProcessingException {
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
	void driverLicenseAlreadyExists() throws JsonProcessingException {

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
}
