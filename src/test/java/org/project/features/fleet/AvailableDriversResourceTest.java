package org.project.features.fleet;

import static io.restassured.RestAssured.given;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.project.application.dto.fleet.DriverDTO;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.repository.JetDriverRepository;
import org.project.infrastructure.repository.JetOwnerRepository;
import org.project.infrastructure.repository.JetUserRepository;
import org.project.infrastructure.security.JWTUtility;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class AvailableDriversResourceTest {

	@Inject
	JetUserRepository userRepo;

	@Inject
	JetOwnerRepository ownerRepo;

	@Inject
	JetDriverRepository driverRepo;

	@Inject
	JWTUtility jwtUtility;

	@Test
	void shouldReturnAvailableDrivers() {
		User user = TestDataGenerator.user();
		userRepo.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepo.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepo.save(driver);

		String token = jwtUtility.generateToken(user);

		List<DriverDTO> drivers = given().contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token).queryParam("page", 1).queryParam("size", 5).when()
				.get("/uyol/available/drivers").then().statusCode(200).extract().body().jsonPath()
				.getList(".", DriverDTO.class);

		Assertions.assertFalse(drivers.isEmpty());
		Assertions.assertEquals(driver.id().value().toString(), drivers.get(0).driverID());
	}

	@Test
	void shouldReturnForbidden_WhenUserIsNotOwner() {
		User user = TestDataGenerator.user();
		userRepo.save(user);

		String token = jwtUtility.generateToken(user);

		given().contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + token).queryParam("page", 1)
				.queryParam("size", 5).when().get("/uyol/available/drivers").then().statusCode(403);
	}

	@Test
	void shouldReturnEmptyList_WhenNoDriversExist() {
		User user = TestDataGenerator.user();
		userRepo.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepo.save(owner);

		String token = jwtUtility.generateToken(user);

		List<DriverDTO> drivers = given().contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token).queryParam("page", 1).queryParam("size", 5).when()
				.get("/uyol/available/drivers").then().statusCode(200).extract().body().jsonPath()
				.getList(".", DriverDTO.class);

		Assertions.assertTrue(drivers.isEmpty());
	}
}
