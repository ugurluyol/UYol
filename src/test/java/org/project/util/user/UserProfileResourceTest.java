package org.project.util.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.domain.shared.value_objects.AccountDates;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.domain.user.value_objects.KeyAndCounter;
import org.project.domain.user.value_objects.PersonalData;
import org.project.util.PostgresTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class UserProfileResourceTest {

	@Inject
	UserRepository userRepository;

	@BeforeEach
	void setup() {
		PersonalData personalData = new PersonalData("Test", "User", "+994501234567", "OldPassword123!",
				"test@example.com", LocalDate.of(2000, 1, 1));
		KeyAndCounter keyAndCounter = new KeyAndCounter("test-secret-key", 2);

		User userWithPicture = User.fromRepository(UUID.randomUUID(), personalData, true, false, keyAndCounter,
				AccountDates.defaultDates(), false);

		userRepository.save(userWithPicture);

	}
	@Test
	void shouldChangeProfilePictureSuccessfully() {
		byte[] image = new byte[] { 1, 2, 3 };
		ByteArrayInputStream stream = new ByteArrayInputStream(image);

		given().auth().oauth2(getTestToken()).contentType("application/octet-stream").body(stream).when()
				.put("/uyol/user/profile/picture/change").then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}


	@Test
	void shouldReturnProfilePictureIfExists() {
		given().auth().oauth2(getTestToken()).when().get("/uyol/user/profile/picture").then().statusCode(200)
				.body("imageType", notNullValue()).body("data", notNullValue());
	}

	@Test
	void shouldReturn404IfProfilePictureNotExists() {
		given().auth().oauth2(getTokenForUserWithoutPicture()).when().get("/uyol/user/profile/picture").then()
				.statusCode(404);
	}

	@Test
	void shouldReturn400IfEmptyPictureUploaded() {
		ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);

		given().auth().oauth2(getTestToken()).contentType("application/octet-stream").body(emptyStream).when()
				.put("/uyol/user/profile/picture/change").then().statusCode(400);
	}

	private String getTestToken() {
		return generateJwtToken("test@example.com");
	}

	private String getTokenForUserWithoutPicture() {
		return generateJwtToken("nopicture@example.com");
	}

	private String generateJwtToken(String email) {
		String jwt = Jwt.claims().claim("sub", email).claim("email", email).claim("groups", Set.of("user")).sign();
		return jwt;
	}
}

