package org.project.infrastructure.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.project.infrastructure.repository.JetOTPRepository;
import org.project.infrastructure.repository.JetUserRepository;
import org.project.infrastructure.security.HOTPGenerator;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.domain.user.entities.OTP;
import org.project.domain.user.entities.User;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class OTPRepoTest {

    @Inject
    JetOTPRepository otpRepo;

    @Inject
    JetUserRepository userRepo;

    private HOTPGenerator hotpGenerator;

    @BeforeEach
    public void setup() {
        hotpGenerator = new HOTPGenerator();
    }

    private static Stream<User> userProvider() {
        return IntStream.range(0, 10)
                .mapToObj(i -> TestDataGenerator.user());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void saveOTpTest(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());
        assertEquals(1, userSaveResult.value());

        var result = otpRepo.save(otp);

        assertTrue(result.success());
        assertEquals(1, result.value());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failSavingOTPOfNonExistentUser(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var result = otpRepo.save(otp);

        assertFalse(result.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfullyUpdateOTPConfirmation(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());
        assertEquals(1, userSaveResult.value());

        var saveResult = otpRepo.save(otp);

        assertTrue(saveResult.success());
        assertEquals(1, saveResult.value());

        otp.confirm();
        var updateResult = otpRepo.updateConfirmation(otp);

        assertTrue(updateResult.success());
        assertEquals(1, updateResult.value());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfullyRemoveOTP(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());
        assertEquals(1, userSaveResult.value());

        var saveResult = otpRepo.save(otp);

        assertTrue(saveResult.success());
        assertEquals(1, saveResult.value());

        var removeResult = otpRepo.remove(otp);

        assertTrue(removeResult.success());
        assertEquals(1, removeResult.value());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void otpContainsUserIdSuccess(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());
        assertEquals(1, userSaveResult.value());

        var saveResult = otpRepo.save(otp);

        assertTrue(saveResult.success());
        assertEquals(1, saveResult.value());

        var containsResult = otpRepo.contains(user.id());

        assertTrue(containsResult);
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfulFindByOTP(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());
        assertEquals(1, userSaveResult.value());

        var saveResult = otpRepo.save(otp);

        assertTrue(saveResult.success());
        assertEquals(1, saveResult.value());

        var findResult = otpRepo.findBy(otp);

        assertTrue(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failFindByNonExistentOTP(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());

        var findResult = otpRepo.findBy(otp);

        assertFalse(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfulFindByOTPString(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());

        var saveResult = otpRepo.save(otp);

        assertTrue(saveResult.success());

        var findResult = otpRepo.findBy(otp.otp());

        assertTrue(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failFindByNonExistentOTPString(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());

        var findResult = otpRepo.findBy(otp.otp());

        assertFalse(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void successfulFindByUserId(User user) {
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());

        var saveResult = otpRepo.save(otp);

        assertTrue(saveResult.success());

        var findResult = otpRepo.findBy(user.id());

        assertTrue(findResult.success());
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    public void failFindByNonExistentUserId(User user) {
        var noNameUser = TestDataGenerator.user();
        var otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

        var userSaveResult = userRepo.save(user);

        assertTrue(userSaveResult.success());

        var saveResult = otpRepo.save(otp);

        assertTrue(saveResult.success());

        var findResult = otpRepo.findBy(noNameUser.id());

        assertFalse(findResult.success());
    }
}