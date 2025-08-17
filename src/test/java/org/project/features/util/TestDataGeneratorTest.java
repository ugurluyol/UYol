package org.project.features.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

class TestDataGeneratorTest {

  @Test
  @DisplayName("rideWithoutDelivery()")
  void testRideWithoutDelivery() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.rideWithoutDelivery());
    });
  }

  @Test
  @DisplayName("generateRoute()")
  void testRoute() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateRoute());
    });
  }

  @Test
  @DisplayName("otp()")
  void testOtp() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.otp());
    });
  }

  @Test
  void testOtpWithKey() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      String key = "testkey";
      assertNotNull(TestDataGenerator.otp(key));
    });
  }

  @Test
  void testOtpWithKeyAndCounter() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      String key = "testkey";
      assertNotNull(TestDataGenerator.otp(key, 1));
    });
  }

  @Test
  void testUser() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.user());
    });
  }

  @Test
  void testDriver() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.driver());
    });
  }

  @Test
  void testOwner() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.owner());
    });
  }

  @Test
  void testCar() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.car());
    });
  }

  @Test
  void testVoen() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.voen());
    });
  }

  @Test
  void testDriverLicense() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.driverLicense());
    });
  }

  @Test
  void testGenerateSeatCount() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateSeatCount());
    });
  }

  @Test
  void testGenerateCarYear() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateCarYear());
    });
  }

  @Test
  void testGenerateCarColor() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateCarColor());
    });
  }

  @Test
  void testGenerateLicensePlate() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateLicensePlate());
    });
  }

  @Test
  void testGenerateCarModel() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateCarModel());
    });
  }

  @Test
  void testGenerateCarBrand() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateCarBrand());
    });
  }

  @Test
  void testPersonalData() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.personalData());
    });
  }

  @Test
  void testGenerateFirstname() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateFirstname());
    });
  }

  @Test
  void testGenerateSurname() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateSurname());
    });
  }

  @Test
  void testGeneratePhone() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generatePhone());
    });
  }

  @Test
  void testGenerateEmail() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateEmail());
    });
  }

  @Test
  void testGeneratePassword() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generatePassword());
    });
  }

  @Test
  void testGenerateBirthdate() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateBirthdate());
    });
  }

  @Test
  void testGenerateRegistrationForm() {
    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
      assertNotNull(TestDataGenerator.generateRegistrationForm());
    });
  }
}
