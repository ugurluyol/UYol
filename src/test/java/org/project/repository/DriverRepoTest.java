package org.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.value_objects.UserID;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.user.entities.User;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.infrastructure.repository.JetDriverRepository;
import org.project.infrastructure.repository.JetUserRepository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DriverRepoTest {

	@Inject
	JetUserRepository userRepo;

	@Inject
	JetDriverRepository repo;

	private User savedUser;
	private UserID savedUserId;
	private List<Driver> testDrivers;

	@BeforeAll
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
}
