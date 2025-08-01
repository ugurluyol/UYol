package org.project.util.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.project.util.TestDataGenerator.generateBirthdate;
import static org.project.util.TestDataGenerator.generateEmail;
import static org.project.util.TestDataGenerator.generateFirstname;
import static org.project.util.TestDataGenerator.generatePhone;
import static org.project.util.TestDataGenerator.generateSurname;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.application.dto.auth.LoginForm;
import org.project.application.dto.auth.PasswordChangeForm;
import org.project.application.dto.auth.RegistrationForm;
import org.project.application.dto.auth.Token;
import org.project.application.dto.auth.Tokens;
import org.project.domain.shared.containers.Result;
import org.project.domain.user.entities.OTP;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.OTPRepository;
import org.project.domain.user.repositories.UserRepository;
import org.project.domain.user.value_objects.AccountDates;
import org.project.domain.user.value_objects.KeyAndCounter;
import org.project.domain.user.value_objects.PersonalData;
import org.project.util.PostgresTestResource;
import org.project.util.TestDataGenerator;
import org.project.util.util.DBManagementUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class AuthResourceTest {

	static final ObjectMapper objectMapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.build();

	private final JWTParser jwtParser;

	private final DBManagementUtils dbManagement;

	AuthResourceTest(JWTParser jwtParser, DBManagementUtils dbManagement) {
		this.jwtParser = jwtParser;
		this.dbManagement = dbManagement;
	}

	@Inject
	OTPRepository otpRepository;

	@Inject
	UserRepository userRepository;

	private User testUser;

	@BeforeEach
	void setup() {
		PersonalData personalData = new PersonalData("Test", "User", "+994501234567", "OldPassword123!",
				"test@example.com", LocalDate.of(2000, 1, 1));

		KeyAndCounter keyAndCounter = new KeyAndCounter("test-secret-key", 2);

		User testUser = User.fromRepository(UUID.randomUUID(), personalData, true, false, keyAndCounter,
				AccountDates.defaultDates(), false
		);

		Result<Integer, Throwable> userSaveResult = userRepository.save(testUser);
		userSaveResult.ifFailure(error -> fail("User save failed: " + error.getMessage()));


		OTP expiredOtp = OTP.fromRepository("expiredOtp123", testUser.id(), false, LocalDateTime.now().minusMinutes(10),
				LocalDateTime.now().minusMinutes(5));

		OTP validOtp = OTP.fromRepository("validOtp123", testUser.id(), false, LocalDateTime.now(),
				LocalDateTime.now().plusMinutes(5));

		Result<Integer, Throwable> expiredOtpSaveResult = otpRepository.save(expiredOtp);
		expiredOtpSaveResult.ifFailure(error -> fail("Expired OTP save failed: " + error.getMessage()));

		Result<Integer, Throwable> validOtpSaveResult = otpRepository.save(validOtp);
		validOtpSaveResult.ifFailure(error -> fail("Valid OTP save failed: " + error.getMessage()));

	}
	@Test
	void registrationFailsWhenFormIsNull() {
		given()
				.contentType(ContentType.JSON)
				.body("")
				.when()
				.post("/uyol/auth/registration")
				.then()
				.statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	void validRegistration() throws JsonProcessingException {
		RegistrationForm form = TestDataGenerator.generateRegistrationForm();

		given().contentType(ContentType.JSON)
				.body(objectMapper.writeValueAsString(form))
				.when()
				.post("/uyol/auth/registration")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void registrationFailsWhenPasswordMismatch() throws JsonProcessingException {
		RegistrationForm form = new RegistrationForm(
				generateFirstname().firstname(),
				generateSurname().surname(),
				generatePhone().phoneNumber(),
				generateEmail().email(),
				"password1984",
				"password1063231",
				generateBirthdate().birthDate());

		given().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(form)).when()
				.post("/uyol/auth/registration").then().statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	void registrationFailsWhenEmailAlreadyUsed() throws JsonProcessingException {
		RegistrationForm form = TestDataGenerator.generateRegistrationForm();

		given().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(form)).when()
				.post("/uyol/auth/registration").then().statusCode(Response.Status.ACCEPTED.getStatusCode());

		given().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(form)).when()
				.post("/uyol/auth/registration").then().statusCode(Response.Status.CONFLICT.getStatusCode());
	}

	@Test
	void registrationFailsWhenPhoneAlreadyUsed() throws JsonProcessingException {
		RegistrationForm form = TestDataGenerator.generateRegistrationForm();

		given().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(form)).when()
				.post("/uyol/auth/registration").then().statusCode(Response.Status.ACCEPTED.getStatusCode());

		given().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(form)).when()
				.post("/uyol/auth/registration").then().statusCode(Response.Status.CONFLICT.getStatusCode());
	}

	@Test
	void validLogin() throws JsonProcessingException {
		RegistrationForm form = TestDataGenerator.generateRegistrationForm();
		dbManagement.saveAndVerifyUser(form);

		Tokens tokens = given().contentType(ContentType.JSON)
				.body(objectMapper.writeValueAsString(new LoginForm(form.phone(), form.password())))
				.when()
				.post("/uyol/auth/login")
				.then()
				.assertThat()
				.contentType(MediaType.APPLICATION_JSON)
				.statusCode(Response.Status.OK.getStatusCode()).extract()
				.as(Tokens.class);

		assertNotNull(tokens);
		assertNotNull(tokens.token());
		assertFalse(tokens.token().isBlank());
		assertDoesNotThrow(() -> jwtParser.parse(tokens.token()));
		assertNotNull(tokens.refreshToken());
		assertFalse(tokens.refreshToken().isBlank());
		assertDoesNotThrow(() -> jwtParser.parse(tokens.refreshToken()));
	}

	@Test
	void loginMismatchPassword() throws JsonProcessingException {
		RegistrationForm form = TestDataGenerator.generateRegistrationForm();
		dbManagement.saveAndVerifyUser(form);

		given().contentType(ContentType.JSON)
				.body(objectMapper.writeValueAsString(new LoginForm(form.phone(), "password")))
				.when()
				.post("/uyol/auth/login")
				.then()
				.assertThat()
				.statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
				.body(containsString("Invalid credentials"));
	}

	@Test
	void otpVerificationShouldFailWithInvalidOTP() {
		given().queryParam("otp", "invalid-otp").when().patch("/uyol/auth/verification").then()
				.statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	void validVerification() throws JsonProcessingException {
		OTP otp = dbManagement.saveUser(TestDataGenerator.generateRegistrationForm());

		given().queryParam("otp", otp.otp()).when().patch("/uyol/auth/verification").then().assertThat()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void verificationWithInvalidOTP() throws JsonProcessingException {
		OTP ignore = dbManagement.saveUser(TestDataGenerator.generateRegistrationForm());

		given().queryParam("otp", "invalidotp").when().patch("/uyol/auth/verification").then().assertThat()
				.statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	void refreshToken() throws JsonProcessingException {
		RegistrationForm form = TestDataGenerator.generateRegistrationForm();
		dbManagement.saveAndVerifyUser(form);

		Tokens tokens = given().contentType(ContentType.JSON)
				.body(objectMapper.writeValueAsString(new LoginForm(form.phone(), form.password())))
				.when()
				.post("/uyol/auth/login").then().assertThat()
				.statusCode(Response.Status.OK.getStatusCode())
				.extract()
				.as(Tokens.class);

		Token token = given()
				.header("Refresh-Token", tokens.refreshToken())
				.when()
				.patch("/uyol/auth/refresh-token")
				.then()
				.assertThat()
				.statusCode(Response.Status.OK.getStatusCode())
				.contentType(ContentType.JSON).extract()
				.as(Token.class);

		assertNotNull(token);
		assertNotNull(token.token());
		assertFalse(token.token().isEmpty());
		assertDoesNotThrow(() -> jwtParser.parse(token.token()));
	}

	@Test
	void refreshTokenShouldFailIfMissing() {
		given().when().patch("/uyol/auth/refresh-token").then().statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	void twoFactorVerificationFailsWithWrongOTP() {
		given().queryParam("otp", "000000").when().patch("/uyol/auth/2FA/verification").then()
				.statusCode(Response.Status.NOT_FOUND.getStatusCode());
	}

	@Test
	void applyPasswordChange_shouldReturn202_whenSuccess() {
		PasswordChangeForm form = new PasswordChangeForm("validotp123", "Password1!", "Password1!");

		given().contentType(ContentType.JSON).body(form).when().patch("/uyol/auth/apply/password/change").then()
				.statusCode(202);
	}

	@Test
	void applyPasswordChange_shouldReturn400_whenPasswordsMismatch() {
		PasswordChangeForm form = new PasswordChangeForm("someotp", "Password1!", "Mismatch123");

		given().contentType(ContentType.JSON).body(form).when().patch("/uyol/auth/apply/password/change").then()
				.statusCode(400).body(containsString("Passwords do not match"));
	}

	@Test
	void applyPasswordChange_shouldReturn404_whenOTPNotFound() {
		PasswordChangeForm form = new PasswordChangeForm("nonexistentotp", "Password1!", "Password1!");

		given().contentType(ContentType.JSON).body(form).when().patch("/uyol/auth/apply/password/change").then()
				.statusCode(404).body(containsString("OTP not found"));
	}

	////
	@Test
	void applyPasswordChange_shouldReturn410_whenOTPExpired() {
		PasswordChangeForm form = new PasswordChangeForm("expiredOtp123", "StrongPass1!", "StrongPass1!");

		given().contentType(ContentType.JSON).body(form).when().patch("/uyol/auth/apply/password/change").then()
				.statusCode(410)
				.body(containsString("OTP is gone"));
	}

	@Test
	void applyPasswordChange_shouldReturn400_whenOTPIsNull() {
		PasswordChangeForm form = new PasswordChangeForm(null, "StrongPass1!", "StrongPass1!");

		given().contentType(ContentType.JSON).body(form).when().patch("/uyol/auth/apply/password/change").then()
				.statusCode(400);
	}

	@Test
	void applyPasswordChange_shouldReturn400_whenPasswordTooWeak() {
		PasswordChangeForm form = new PasswordChangeForm("validOtp123", "123", "123");

		given().contentType(ContentType.JSON).body(form).when().patch("/uyol/auth/apply/password/change").then()
				.statusCode(400).body(containsString("Password is too weak"));
	}

	@Test
	void applyPasswordChange_shouldReturn400_whenFormIsEmpty() {
		given().contentType(ContentType.JSON).body("{}").when().patch("/uyol/auth/apply/password/change").then()
				.statusCode(400);
	}
}
