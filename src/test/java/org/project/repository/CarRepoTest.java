package org.project.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.value_objects.CarID;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.value_objects.Pageable;
import org.project.domain.user.entities.User;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.features.util.DBManagementUtils;
import org.project.infrastructure.repository.JetCarRepository;
import org.project.infrastructure.repository.JetUserRepository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
public class CarRepoTest {

	@Inject
	JetCarRepository repo;

	@Inject
	JetUserRepository userRepository;

	@Inject
	DBManagementUtils dbManagement;

	private User savedUser;
	private UserID savedUserId;
	private List<Car> testCars;

	@BeforeEach
	void setup() {
		savedUser = TestDataGenerator.user();
		userRepository.save(savedUser);
		savedUserId = new UserID(savedUser.id());

		testCars = Stream.generate(() -> Car.of(savedUserId, TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount())).limit(3).toList();
	}

	@Test
	void successfullySaveCars() {
		for (Car car : testCars) {
			var result = repo.save(car);
			assertTrue(result.success());
			assertEquals(1, result.value());
		}
	}

	@Test
	void successfulFindByCarId() {
		for (Car car : testCars) {
			repo.save(car);
			var findResult = repo.findBy(car.id());
			assertTrue(findResult.success());
			assertEquals(car.id(), findResult.value().id());
		}
	}

	@Test
	void failFindByNonExistentCarId() {
		var nonExistentId = new CarID(UUID.randomUUID());
		var findResult = repo.findBy(nonExistentId);
		assertFalse(findResult.success());
	}

	@Test
	void pageOfCarsByUserId() {
		for (Car car : testCars) {
			repo.save(car);
		}

		Pageable pageable = new Pageable() {
			@Override
			public int limit() {
				return 10;
			}

			@Override
			public int offset() {
				return 0;
			}
		};

		var pageResult = repo.pageOf(pageable, savedUserId);
		assertTrue(pageResult.success());

		List<Car> cars = pageResult.value();
		for (Car car : testCars) {
			assertTrue(cars.stream().anyMatch(c -> c.id().equals(car.id())));
		}
	}

	@Test
	void testIsLicenseTemplateExists() throws JsonProcessingException {
		User user = dbManagement.saveAndRetrieveUser(TestDataGenerator.generateRegistrationForm());
		UserID userId = new UserID(user.id());

		LicensePlate licensePlate = TestDataGenerator.generateLicensePlate();
		assertDoesNotThrow(() -> assertFalse(repo.isLicenseTemplateExists(licensePlate)));

		Car car = TestDataGenerator.car(userId, licensePlate);
		repo.save(car);

		assertDoesNotThrow(() -> assertTrue(repo.isLicenseTemplateExists(licensePlate)));
	}
}