package org.project.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.user.entities.User;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.features.util.DBManagementUtils;
import org.project.infrastructure.repository.JetDriverRepository;
import org.project.infrastructure.repository.JetUserRepository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DriverRepoTest {

	@Inject
	JetUserRepository userRepo;

	@Inject
	JetDriverRepository repo;

	@Inject
	DBManagementUtils dbManagement;

	private User savedUser;
	private UserID savedUserId;
	private List<Driver> testDrivers;

	@BeforeEach
	void setup() {

		savedUser = TestDataGenerator.user();
		userRepo.save(savedUser);
		savedUserId = new UserID(savedUser.id());

		testDrivers = Stream.generate(() -> {
			User u = TestDataGenerator.user();
			userRepo.save(u);
			UserID userId = new UserID(u.id());
			return Driver.of(userId, TestDataGenerator.driverLicense());
		}).limit(5).toList();
	}

	@Test
	void successfullySaveDriver() {
		Driver driver = Driver.of(savedUserId, TestDataGenerator.driverLicense());
		var result = repo.save(driver);
		assertTrue(result.success());
		assertEquals(1, result.value());
	}

	@Test
	void successfulFindByDriverId() {
		for (Driver driver : testDrivers) {
			repo.save(driver);
			var findResult = repo.findBy(driver.id());
			assertTrue(findResult.success());
			assertEquals(driver.id(), findResult.value().id());
		}
	}

	@Test
	void failFindByNonExistentDriverId() {
		var nonExistentId = new DriverID(UUID.randomUUID());
		var findResult = repo.findBy(nonExistentId);
		assertFalse(findResult.success());
	}

	@Test
	void successfulFindByUserId() {
		for (Driver driver : testDrivers) {
			repo.save(driver);
			var findResult = repo.findBy(driver.userID());
			assertTrue(findResult.success());
			assertEquals(driver.userID(), findResult.value().userID());
		}
	}

	@Test
	void failFindByNonExistentUserId() {
		var nonExistentUserId = new UserID(UUID.randomUUID());
		var findResult = repo.findBy(nonExistentUserId);
		assertFalse(findResult.success());
	}

	@Test
	void testIsOwnerExists() throws JsonProcessingException {
		User user = dbManagement.saveAndRetrieveUser(TestDataGenerator.generateRegistrationForm());
		UserID userID = new UserID(user.id());

		assertDoesNotThrow(() -> assertFalse(repo.isDriverExists(userID)));

		Driver driver = Driver.of(userID, TestDataGenerator.driverLicense());
		repo.save(driver);

		assertDoesNotThrow(() -> assertTrue(repo.isDriverExists(userID)));
	}

	@Test
	void testIsDriverExists() throws JsonProcessingException {
		User user = dbManagement.saveAndRetrieveUser(TestDataGenerator.generateRegistrationForm());
		UserID userID = new UserID(user.id());
		DriverLicense license = TestDataGenerator.driverLicense();

		assertDoesNotThrow(() -> assertFalse(repo.isLicenseExists(license)));

		Driver driver = Driver.of(userID, license);
		repo.save(driver);

		assertDoesNotThrow(() -> assertTrue(repo.isLicenseExists(license)));
	}
}
