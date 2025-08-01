package org.project.util.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.project.util.TestDataGenerator.*;

import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.project.application.dto.auth.LoginForm;
import org.project.application.dto.auth.PasswordChangeForm;
import org.project.application.dto.auth.RegistrationForm;
import org.project.application.dto.auth.Token;
import org.project.application.dto.auth.Tokens;
import org.project.domain.user.entities.OTP;
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
	void validPasswordChange() throws JsonProcessingException {
		RegistrationForm form = TestDataGenerator.generateRegistrationForm();
		dbManagement.saveAndVerifyUser(form);
		String newPassword = TestDataGenerator.generatePassword().password();

		given()
				.queryParam("identifier", form.email())
				.when()
				.post("/uyol/auth/start/password/change")
				.then()
				.assertThat()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(containsString("Confirm OTP."));

		OTP userOTP = dbManagement.getUserOTP(form.email());

		given()
				.contentType(ContentType.JSON)
				.body(objectMapper.writeValueAsString(new PasswordChangeForm(userOTP.otp(), newPassword, newPassword)))
				.when()
				.patch("/uyol/auth/apply/password/change")
				.then()
				.assertThat()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());

		given()
				.contentType(ContentType.JSON)
				.body(objectMapper.writeValueAsString(new LoginForm(form.phone(), form.password())))
				.when()
				.post("/uyol/auth/login")
				.then()
				.assertThat()
				.statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
				.body(containsString("Invalid credentials"));

		Tokens tokens = given().contentType(ContentType.JSON)
				.body(objectMapper.writeValueAsString(new LoginForm(form.phone(), newPassword)))
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
}
