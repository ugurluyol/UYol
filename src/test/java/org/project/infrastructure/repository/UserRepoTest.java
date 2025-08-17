package org.project.infrastructure.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.project.domain.user.entities.User;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.Phone;
import org.project.domain.user.value_objects.RefreshToken;
import org.project.infrastructure.repository.JetUserRepository;
import org.project.infrastructure.security.JWTUtility;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class UserRepoTest {

    @Inject
    JetUserRepository repo;

    @Inject
    JWTUtility jwtUtility;

    private static Stream<User> userProvider() {
        return IntStream.range(0, 10)
                .mapToObj(i -> TestDataGenerator.user());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfullySaveUser(User user) {
        var result = repo.save(user);

        assertTrue(result.success());
        assertEquals(1, result.value());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfullySaveRefreshToken(User user) {
        var token = new RefreshToken(user.id(), jwtUtility.generateRefreshToken(user));

        var userSaveResult = repo.save(user);

        assertTrue(userSaveResult.success());
        assertEquals(1, userSaveResult.value());

        var refreshTokenSaveResult = repo.saveRefreshToken(token);

        assertTrue(refreshTokenSaveResult.success());
        assertEquals(1, refreshTokenSaveResult.value());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfullyUpdateCounter(User user) {
        var saveResult = repo.save(user);

        assertTrue(saveResult.success());
        assertEquals(1, saveResult.value());

        user.incrementCounter();
        var updateCounterResult = repo.updateCounter(user);

        assertTrue(updateCounterResult.success());
        assertEquals(1, updateCounterResult.value());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfullyUpdateVerification(User user) {
        var saveResult = repo.save(user);

        assertTrue(saveResult.success());
        assertEquals(1, saveResult.value());

        user.incrementCounter();
        user.enable();
        var verificationResult = repo.updateVerification(user);

        assertTrue(verificationResult.success());
        assertEquals(1, verificationResult.value());
    }

    @Test
    public void successfullyUpdateBan() {
        User user = TestDataGenerator.user();

        user.ban();

        assertTrue(repo.updateBan(user).success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfullyUpdate2FA(User user) {
        var saveResult = repo.save(user);

        assertTrue(saveResult.success());
        assertEquals(1, saveResult.value());

        user.incrementCounter();
        user.enable();
        user.incrementCounter();
        user.enable2FA();
        var _2faResult = repo.update2FA(user);

        assertTrue(_2faResult.success());
        assertEquals(1, _2faResult.value());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfulIsEmailExists(User user) {
        var saveResult = repo.save(user);

        assertTrue(saveResult.success());
        assertEquals(1, saveResult.value());

        var isExistsResult = repo.isEmailExists(new Email(user.personalData().email().orElseThrow()));

        assertTrue(isExistsResult);
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failIsExistsByNonExistentEmail(User user) {
        var isExistsResult = repo.isEmailExists(new Email(user.personalData().email().orElseThrow()));

        assertFalse(isExistsResult);
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfulIsPhoneNumberExists(User user) {
        var saveResult = repo.save(user);

        assertTrue(saveResult.success());

        var isExistsResult = repo.isPhoneExists(new Phone(user.personalData().phone().orElseThrow()));

        assertTrue(isExistsResult);
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failIsExistsByNonExistentPhoneNumber(User user) {
        var isExistsResult = repo.isPhoneExists(new Phone(user.personalData().phone().orElseThrow()));

        assertFalse(isExistsResult);
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfulFindById(User user) {
        var saveResult = repo.save(user);

        assertTrue(saveResult.success());

        var findResult = repo.findBy(user.id());

        assertTrue(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failFindByNonExistentId(User user) {
        var findResult = repo.findBy(user.id());

        assertFalse(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfulFindByEmail(User user) {
        var saveResult = repo.save(user);

        assertTrue(saveResult.success());

        var findResult = repo.findBy(new Email(user.personalData().email().orElseThrow()));

        assertTrue(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failFindByNonExistentEmail(User user) {
        var findResult = repo.findBy(new Email(user.personalData().email().orElseThrow()));

        assertFalse(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfulFindByPhoneNumber(User user) {
        var saveResult = repo.save(user);

        assertTrue(saveResult.success());

        var findResult = repo.findBy(new Phone(user.personalData().phone().orElseThrow()));

        assertTrue(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failFindByNonExistentPhoneNumber(User user) {
        var findResult = repo.findBy(new Phone(user.personalData().phone().orElseThrow()));

        assertFalse(findResult.success());
    }
}
