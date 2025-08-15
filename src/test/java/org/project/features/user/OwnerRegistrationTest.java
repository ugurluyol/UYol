package org.project.features.user;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.infrastructure.security.JWTUtility;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class OwnerRegistrationTest {

    @Inject
	UserRepository userRepository;

	@Inject
	JWTUtility jwtUtility;

    @Test
    void successfulOwnerRegistration() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		String voen = TestDataGenerator.voen().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void invalidVoen() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		String voen = "";

		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(400);
    }

    @Test
    void userAccountDontExists() {
		User fakeUser = TestDataGenerator.user();
		String jwtToken = jwtUtility.generateToken(fakeUser);

		String voen = TestDataGenerator.voen().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(404);
    }

    @Test
    void ownerAlreadyExists() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		String voen = TestDataGenerator.voen().value();


		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(Response.Status.ACCEPTED.getStatusCode());


		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(409);
    }

    @Test
    void voenAlreadyExists() {

		User user1 = TestDataGenerator.user();
		userRepository.save(user1);
		String jwtToken1 = jwtUtility.generateToken(user1);

		String voen = TestDataGenerator.voen().value();

		given().header("Authorization", "Bearer " + jwtToken1).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(Response.Status.ACCEPTED.getStatusCode());


		User user2 = TestDataGenerator.user();
		userRepository.save(user2);
		String jwtToken2 = jwtUtility.generateToken(user2);

		given().header("Authorization", "Bearer " + jwtToken2).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(409);
    }
}
