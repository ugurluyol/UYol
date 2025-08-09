package org.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.value_objects.UserID;
import org.project.domain.fleet.value_objects.Voen;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.infrastructure.repository.JetOwnerRepository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class OwnerRepoTest {

	@Inject
	JetOwnerRepository repo;

	private static Stream<Owner> ownerProvider() {
	    return Stream.generate(TestDataGenerator::owner).limit(5);
	}

	@ParameterizedTest
	@MethodSource("ownerProvider")
	void successfullySaveOwner(Owner owner) {
		var result = repo.save(owner);

		assertTrue(result.success());
		assertEquals(1, result.value());
	}

	@ParameterizedTest
	@MethodSource("ownerProvider")
	void successfulFindByOwnerId(Owner owner) {
		var saveResult = repo.save(owner);

		assertTrue(saveResult.success());

		var findResult = repo.findBy(owner.id());

		assertTrue(findResult.success());
		assertEquals(owner.id(), findResult.value().id());
	}

	@ParameterizedTest
	@MethodSource("ownerProvider")
	void failFindByNonExistentOwnerId(Owner owner) {
		var findResult = repo.findBy(owner.id());

		assertFalse(findResult.success());
	}

	@ParameterizedTest
	@MethodSource("ownerProvider")
	void successfulFindByUserId(Owner owner) {
		var saveResult = repo.save(owner);

		assertTrue(saveResult.success());

		var findResult = repo.findBy(owner.userID());

		assertTrue(findResult.success());
		assertEquals(owner.userID(), findResult.value());
	}

	@ParameterizedTest
	@MethodSource("ownerProvider")
	void failFindByNonExistentUserId(Owner owner) {
		var findResult = repo.findBy(owner.userID());

		assertFalse(findResult.success());
	}
}
