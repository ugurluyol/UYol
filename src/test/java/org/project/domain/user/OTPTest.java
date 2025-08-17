package org.project.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.shared.exceptions.IllegalDomainStateException;
import org.project.domain.user.entities.OTP;
import org.project.features.util.TestDataGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

public class OTPTest {

    @Test
    public void shouldCreateOTPAndConfirmItSuccessfully() {
        var user = TestDataGenerator.user();
        var otpCode = TestDataGenerator.otp();
        var otp = OTP.of(user, otpCode);

        assertFalse(otp.isConfirmed());
        assertFalse(otp.isExpired());

        otp.confirm();

        assertTrue(otp.isConfirmed());
    }

    @Test
    public void shouldNotConfirmAlreadyConfirmedOTP() {
        var user = TestDataGenerator.user();
        var otp = OTP.of(user, TestDataGenerator.otp());
        otp.confirm();

        assertThrows(IllegalDomainArgumentException.class, otp::confirm);
    }

    @Test
    public void shouldNotAllowToCreateInvalidOTPWithLetters() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> OTP.of(TestDataGenerator.user(), "12a4b6"));
    }

    @Test
    public void shouldBeExpiredIfExpirationTimeHasPassed() {
        var otp = OTP.fromRepository(
                "123456",
                UUID.randomUUID(),
                false,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(1)
        );

        assertTrue(otp.isExpired());
    }

    @Test
    public void shouldThrowWhenConfirmingExpiredOTP() {
        var otp = OTP.fromRepository(
                "123456",
                UUID.randomUUID(),
                false,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(1)
        );

        assertThrows(IllegalDomainStateException.class, otp::confirm);
    }
}