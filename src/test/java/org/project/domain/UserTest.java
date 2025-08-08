package org.project.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.project.domain.shared.exceptions.IllegalDomainStateException;
import org.project.domain.user.entities.User;
import org.project.domain.user.exceptions.BannedUserException;
import org.project.infrastructure.security.HOTPGenerator;
import org.project.features.TestDataGenerator;

public class UserTest {

    @Test
    public void shouldCreateUserWithValidProperties() {
        var personalData = TestDataGenerator.personalData();
        var secretKey = HOTPGenerator.generateSecretKey();

        var user = User.of(personalData, secretKey);

        assertNotNull(user.personalData());
        assertNotNull(user.id());
        assertNotNull(user.keyAndCounter());
        assertEquals(0, user.keyAndCounter().counter());
        assertNotNull(user.accountDates());
        assertFalse(user.isVerified());
        assertFalse(user.isBanned());
    }

    @Test
    public void shouldSuccessfullyVerifyUser() {
        var user = TestDataGenerator.user();

        user.incrementCounter();

        assertFalse(user.isVerified());
        assertEquals(1, user.keyAndCounter().counter());

        user.enable();

        assertTrue(user.isVerified());
        assertEquals(1, user.keyAndCounter().counter());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToVerifyUserWithoutGeneratedOTP() {
        var user = TestDataGenerator.user();

        var exception = assertThrows(IllegalDomainStateException.class, user::enable);
        assertEquals("It is prohibited to activate an account that has not been verified.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToVerifyUserTwice() {
        var user = TestDataGenerator.user();
        user.incrementCounter();
        user.enable();

        var exception = assertThrows(IllegalDomainStateException.class, user::enable);
        assertEquals("You can`t active already verified user.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToVerifyBannedUser() {
        var user = TestDataGenerator.user();
        user.ban();

        var exception = assertThrows(BannedUserException.class, user::enable);
        assertEquals("Access denied: this user account has been banned due to a violation of platform rules. Contact support for further assistance.",
                exception.getMessage());
    }

    @Test
    public void shouldEnable2FASuccessfullyWhenAllConditionsAreMet() {
        var user = TestDataGenerator.user();
        user.incrementCounter(); // counter = 1
        user.enable();           // user verified
        user.incrementCounter(); // counter = 2

        user.enable2FA();

        assertTrue(user.is2FAEnabled());
    }

    @Test
    public void shouldThrowExceptionWhenEnabling2FAOnUnverifiedUser() {
        var user = TestDataGenerator.user();
        user.incrementCounter();
        user.incrementCounter();

        var exception = assertThrows(IllegalDomainStateException.class, user::enable2FA);
        assertEquals("You can`t enable 2FA on not verified account", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenEnabling2FATwice() {
        var user = TestDataGenerator.user();
        user.incrementCounter();
        user.enable();
        user.incrementCounter();
        user.enable2FA();

        var exception = assertThrows(IllegalDomainStateException.class, user::enable2FA);
        assertEquals("You can`t activate 2FA twice", exception.getMessage());
    }
}