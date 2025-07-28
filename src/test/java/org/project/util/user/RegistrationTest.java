package org.project.util.user;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.project.util.PostgresTestResource;
import org.project.util.TestDataGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
public class RegistrationTest {

	static final ObjectMapper objectMapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.build();

	@Test
	void validRegistration() throws JsonProcessingException {
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
