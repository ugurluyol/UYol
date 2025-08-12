//package org.project.repository;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import java.util.UUID;
//import java.util.stream.Stream;
//
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.project.domain.fleet.entities.Driver;
//import org.project.domain.fleet.value_objects.UserID;
//import org.project.domain.shared.value_objects.DriverID;
//import org.project.features.PostgresTestResource;
//import org.project.features.TestDataGenerator;
//import org.project.infrastructure.repository.JetDriverRepository;
//
//import io.quarkus.test.common.QuarkusTestResource;
//import io.quarkus.test.junit.QuarkusTest;
//import jakarta.inject.Inject;
//
//@QuarkusTest
//@QuarkusTestResource(PostgresTestResource.class)
//public class DriverRepoTest {
//
//	@Inject
//	JetDriverRepository repo;
//
//	private static Stream<Driver> driverProvider() {
//		return Stream.generate(TestDataGenerator::driver).limit(5);
//	}
//
//	@ParameterizedTest
//	@MethodSource("driverProvider")
//	void successfullySaveDriver(Driver driver) {
//		var result = repo.save(driver);
//
//		assertTrue(result.success());
//		assertEquals(1, result.value());
//	}
//
//	@ParameterizedTest
//	@MethodSource("driverProvider")
//	void successfulFindByDriverId(Driver driver) {
//		var saveResult = repo.save(driver);
//
//		assertTrue(saveResult.success());
//
//		var findResult = repo.findBy(driver.id());
//
//		assertTrue(findResult.success());
//		assertEquals(driver.id(), findResult.value().id());
//	}
//
//	@ParameterizedTest
//	void failFindByNonExistentDriverId() {
//		var nonExistentId = new DriverID(UUID.randomUUID());
//
//		var findResult = repo.findBy(nonExistentId);
//
//		assertFalse(findResult.success());
//	}
//
//	@ParameterizedTest
//	@MethodSource("driverProvider")
//	void successfulFindByUserId(Driver driver) {
//		var saveResult = repo.save(driver);
//
//		assertTrue(saveResult.success());
//
//		var findResult = repo.findBy(driver.userID());
//
//		assertTrue(findResult.success());
//		assertEquals(driver.userID(), findResult.value().userID());
//	}
//
//	@ParameterizedTest
//	void failFindByNonExistentUserId() {
//		var nonExistentUserId = new UserID(UUID.randomUUID());
//
//		var findResult = repo.findBy(nonExistentUserId);
//
//		assertFalse(findResult.success());
//	}
//}
