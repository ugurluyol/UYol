package org.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.value_objects.CarID;
import org.project.domain.shared.value_objects.Pageable;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.infrastructure.repository.JetCarRepository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
public class CarRepoTest {

	@Inject
	JetCarRepository repo;

	private static Stream<Car> carProvider() {
		// Hər dəfə fərqli Car yaradırıq ki, UUID-lər təkrar olmasın
		return Stream.generate(() -> {
			Car car = TestDataGenerator.car();
			return Car.of(car.owner(), car.licensePlate(), car.carBrand(), car.carModel(), car.carColor(),
					car.carYear(), car.seatCount());
		}).limit(3);
	}

	@ParameterizedTest
	@MethodSource("carProvider")
	void successfullySaveCar(Car car) {
		var result = repo.save(car);
		assertTrue(result.success());
		assertEquals(1, result.value());
	}

	@ParameterizedTest
	@MethodSource("carProvider")
	void successfulFindByCarId(Car car) {
		var saveResult = repo.save(car);
		assertTrue(saveResult.success());

		var findResult = repo.findBy(car.id());
		assertTrue(findResult.success());
		assertEquals(car.id(), findResult.value().id());
	}

	@ParameterizedTest
	void failFindByNonExistentCarId() {
		var nonExistentId = new CarID(UUID.randomUUID());
		var findResult = repo.findBy(nonExistentId);
		assertFalse(findResult.success());
	}

	@ParameterizedTest
	@MethodSource("carProvider")
	void pageOfCarsByUserId(Car car) {
		var saveResult = repo.save(car);
		assertTrue(saveResult.success());

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

		var pageResult = repo.pageOf(pageable, car.owner());
		assertTrue(pageResult.success());
		List<Car> cars = pageResult.value();
		assertTrue(cars.stream().anyMatch(c -> c.id().equals(car.id())));
	}
}
