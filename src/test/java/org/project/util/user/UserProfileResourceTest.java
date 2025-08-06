package org.project.util.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.infrastructure.security.JWTUtility;
import org.project.util.PostgresTestResource;
import org.project.util.TestDataGenerator;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import javax.imageio.ImageIO;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class UserProfileResourceTest {

	@Inject
	UserRepository userRepository;

	@Inject
	JWTUtility jwtUtility;

	private User userWithPicture;
	private User userWithoutPicture;

	private String tokenWithPicture;
	private String tokenWithoutPicture;

	@BeforeEach
	void setup() {
		userWithPicture = TestDataGenerator.user();
		userRepository.save(userWithPicture);

		userWithoutPicture = TestDataGenerator.user();
		userRepository.save(userWithoutPicture);

		tokenWithPicture = jwtUtility.generateToken(userWithPicture);
		tokenWithoutPicture = jwtUtility.generateToken(userWithoutPicture);
	}

	@Test
	void shouldChangeProfilePictureSuccessfully() throws IOException {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);

		ByteArrayInputStream stream = new ByteArrayInputStream(baos.toByteArray());

		given()
				.auth().oauth2(tokenWithPicture)
				.contentType("application/octet-stream")
				.body(stream)
				.when()
				.put("/uyol/user/profile/picture/change")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void shouldReturnProfilePictureIfExists() throws IOException {
		shouldChangeProfilePictureSuccessfully();

		given()
				.auth().oauth2(tokenWithPicture)
				.when()
				.get("/uyol/user/profile/picture")
				.then()
				.statusCode(200)
				.body("imageType", notNullValue())
				.body("profilePicture", notNullValue());
	}

	@Test
	void shouldReturn404IfProfilePictureNotExists() {
		given().auth().oauth2(tokenWithoutPicture).when().get("/uyol/user/profile/picture").then().statusCode(404);
	}

	@Test
    void shouldReturn400IfEmptyPictureUploaded() {
        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        given()
            .auth().oauth2(tokenWithPicture)
            .contentType("application/octet-stream")
            .body(emptyStream)
        .when()
            .put("/uyol/user/profile/picture/change")
        .then()
				.statusCode(400);
	}
}