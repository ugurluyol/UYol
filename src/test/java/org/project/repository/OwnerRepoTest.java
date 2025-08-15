package org.project.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.fleet.value_objects.Voen;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.user.entities.User;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.features.util.DBManagementUtils;
import org.project.infrastructure.repository.JetOwnerRepository;
import org.project.infrastructure.repository.JetUserRepository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OwnerRepoTest {

	@Inject
	JetUserRepository userRepo;

	@Inject
	JetOwnerRepository ownerRepo;

	@Inject
	DBManagementUtils dbManagement;

	private User savedUser;
	private UserID savedUserId;
	private List<Owner> testOwners;

	@BeforeEach
	void setup() {
		savedUser = TestDataGenerator.user();
		userRepo.save(savedUser);
		savedUserId = new UserID(savedUser.id());

		testOwners = Stream.generate(() -> {
			User u = TestDataGenerator.user();
			userRepo.save(u);
			UserID userId = new UserID(u.id());
			return Owner.of(userId, TestDataGenerator.voen());
		}).limit(5).toList();
	}

	@Test
	void successfullySaveOwner() {
		Owner owner = Owner.of(savedUserId, TestDataGenerator.voen());
		var result = ownerRepo.save(owner);
		assertTrue(result.success());
		assertEquals(1, result.value());
	}

	@Test
	void successfulFindByOwnerId() {
		for (Owner owner : testOwners) {
			ownerRepo.save(owner);
			var findResult = ownerRepo.findBy(owner.id());
			assertTrue(findResult.success());
			assertEquals(owner.id(), findResult.value().id());
		}
	}

	@Test
	void failFindByNonExistentOwnerId() {
		var nonExistentId = new OwnerID(UUID.randomUUID());
		var findResult = ownerRepo.findBy(nonExistentId);
		assertFalse(findResult.success());
	}

	@Test
	void successfulFindByUserId() {
		for (Owner owner : testOwners) {
			var saveResult = ownerRepo.save(owner);
			assertTrue(saveResult.success());

			var findResult = ownerRepo.findBy(owner.userID());
			assertTrue(findResult.success());

		}
	}

	@Test
	void failFindByNonExistentUserId() {
		var nonExistentUserId = new UserID(UUID.randomUUID());
		var findResult = ownerRepo.findBy(nonExistentUserId);
		assertFalse(findResult.success());
	}

	@Test
	void testIsOwnerExists() throws JsonProcessingException {
		User user = dbManagement.saveAndRetrieveUser(TestDataGenerator.generateRegistrationForm());
		UserID userId = new UserID(user.id());

		assertDoesNotThrow(() -> assertFalse(ownerRepo.isOwnerExists(userId)));

		Owner owner = Owner.of(userId, TestDataGenerator.voen());
		ownerRepo.save(owner);
		assertDoesNotThrow(() -> assertTrue(ownerRepo.isOwnerExists(userId)));
	}

	@Test
	void testIsVoenExists() throws JsonProcessingException {
		User user = dbManagement.saveAndRetrieveUser(TestDataGenerator.generateRegistrationForm());
		UserID userId = new UserID(user.id());
		Voen voen = TestDataGenerator.voen();

		assertDoesNotThrow(() -> assertFalse(ownerRepo.isVoenExists(voen)));

		Owner owner = Owner.of(userId, voen);
		ownerRepo.save(owner);
		assertDoesNotThrow(() -> assertTrue(ownerRepo.isVoenExists(voen)));
	}
}
