package org.project.util.user;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.project.application.service.AuthService;
import org.project.domain.shared.containers.Result;
import org.project.domain.user.entities.OTP;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.OTPRepository;
import org.project.domain.user.repositories.UserRepository;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.Phone;
import org.project.infrastructure.communication.EmailInteractionService;
import org.project.infrastructure.communication.PhoneInteractionService;
import org.project.util.PostgresTestResource;
import org.project.util.TestDataGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
public class RegistrationTest {

	static final ObjectMapper objectMapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.build();

	@Inject
	AuthService authService;

	@Test
	void validRegistration() throws JsonProcessingException {

		doNothing()
				.when(phoneInteractionService)
				.sendOTP(any(Phone.class), any(OTP.class));

		given().contentType(ContentType.JSON)
				.body(objectMapper.writeValueAsString(TestDataGenerator.generateRegistrationForm()))
				.when()
				.post("/auth/registration")
				.then()
				.assertThat()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void registrationFailsWhenFormIsNull() {
		given()
				.contentType(ContentType.JSON)
				.body("")
				.when()
				.post("/auth/registration")
				.then()
				.statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	void shouldReturnVerifiedUserByPhone() {
		String phoneNumber = "+994501234567";
		User user = mock(User.class);
		when(user.isVerified()).thenReturn(true);

		when(userRepository.findBy(any(Phone.class))).thenReturn(Result.success(user));

		User result = authService.getVerifiedUserByIdentifier(phoneNumber);

		assertNotNull(result);
		assertEquals(user, result);
	}

	@Test
	void shouldReturnVerifiedUserByEmail() {
		String emailStr = "test@example.com";
		Email email = new Email(emailStr);
		User user = mock(User.class);
		when(user.isVerified()).thenReturn(true);

		when(userRepository.findBy(email)).thenReturn(Result.success(user));

		User result = authService.getVerifiedUserByIdentifier(emailStr);

		assertNotNull(result);
		assertEquals(user, result);
	}

	@Test
	void shouldThrowUnauthorizedWhenUserNotFound() {
		String identifier = "+994501234567";
		Phone phone = new Phone(identifier);
		when(userRepository.findBy(phone)).thenReturn(Result.failure(new RuntimeException("User not found")));

		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> authService.getVerifiedUserByIdentifier(identifier));

		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), exception.getResponse().getStatus());
	}

	@Test
	void shouldThrowForbiddenWhenUserNotVerified() {
		String identifier = "test@example.com";
		Email email = new Email(identifier);
		User user = mock(User.class);
		when(user.isVerified()).thenReturn(false);

		when(userRepository.findBy(email)).thenReturn(Result.success(user));

		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> authService.getVerifiedUserByIdentifier(identifier));

		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), exception.getResponse().getStatus());
	}

	@Test
	void verification_shouldSucceed_whenValidOTPGiven() {
		String otpCode = "123456";
		UUID userId = UUID.randomUUID();

		OTP otp = Mockito.mock(OTP.class);
		User user = Mockito.mock(User.class);

		try (MockedStatic<OTP> mockedStatic = Mockito.mockStatic(OTP.class)) {
			mockedStatic.when(() -> OTP.validate(otpCode)).thenAnswer(invocation -> null);

			Mockito.when(otpRepository.findBy(otpCode)).thenReturn(Result.success(otp));
			Mockito.when(otp.userID()).thenReturn(userId);
			Mockito.when(userRepository.findBy(userId)).thenReturn(Result.success(user));

			Mockito.when(user.isVerified()).thenReturn(false);
			Mockito.when(otp.isExpired()).thenReturn(false);

			Mockito.doNothing().when(otp).confirm();
			Mockito.when(otpRepository.updateConfirmation(otp)).thenReturn(Result.success(1));
			Mockito.doNothing().when(user).enable();
			Mockito.when(userRepository.updateVerification(user)).thenReturn(Result.success(1));

			assertDoesNotThrow(() -> authService.verification(otpCode));

			Mockito.verify(otpRepository).updateConfirmation(otp);
			Mockito.verify(userRepository).updateVerification(user);
		}
	}
}
