package org.project.util.user;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.project.domain.user.entities.OTP;
import org.project.domain.user.value_objects.Phone;
import org.project.infrastructure.communication.EmailInteractionService;
import org.project.infrastructure.communication.PhoneInteractionService;
import org.project.util.PostgresTestResource;
import org.project.util.TestDataGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;

@QuarkusTest
// @TestProfile(MyTestProf.class)
@QuarkusTestResource(PostgresTestResource.class)
public class RegistrationTest {

	static final ObjectMapper objectMapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.build();

	@InjectMock
	PhoneInteractionService phoneInteractionService;

	@InjectMock
	EmailInteractionService emailService;

	@BeforeEach
	void setup() {
		Mockito.doNothing().when(emailService).sendSoftVerificationMessage(Mockito.any());
	}

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

}
