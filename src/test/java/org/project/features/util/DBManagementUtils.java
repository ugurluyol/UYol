package org.project.features.util;

import static com.hadzhy.jetquerious.sql.QueryForge.batchOf;
import static com.hadzhy.jetquerious.sql.QueryForge.delete;
import static io.restassured.RestAssured.given;

import java.util.Objects;

import org.project.application.dto.auth.RegistrationForm;
import org.project.domain.shared.containers.Result;
import org.project.domain.user.entities.OTP;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.OTPRepository;
import org.project.domain.user.repositories.UserRepository;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.Phone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hadzhy.jetquerious.jdbc.JetQuerious;

import io.restassured.http.ContentType;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;

@Singleton
public class DBManagementUtils {

	private final OTPRepository otpRepository;

	private final UserRepository userRepository;

	private final JetQuerious jetQuerious = JetQuerious.instance();

	private static final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

	public static final String DELETE_USER = batchOf(
			delete().from("otp").where("user_id = (SELECT id FROM user_account WHERE email = ?)").build().toSQlQuery(),
			delete().from("refresh_token").where("user_id = (SELECT id FROM user_account WHERE email = ?)").build()
					.toSQlQuery(),
			delete().from("user_account").where("email = ?").build().toSQlQuery());

	DBManagementUtils(UserRepository userRepository, OTPRepository otpRepository) {
		this.userRepository = userRepository;
		this.otpRepository = otpRepository;
	}

	public OTP saveUser(RegistrationForm form) throws JsonProcessingException {
		given().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(form)).when()
				.post("/uyol/auth/registration").then().assertThat()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());

		Result<User, Throwable> user = userRepository.findBy(new Phone(form.phone()));
		return otpRepository.findBy(Objects.requireNonNull(user.orElseThrow()).id()).orElseThrow();
	}

	public OTP getUserOTP(String email) {
		User user = Objects.requireNonNull(userRepository.findBy(new Email(email)).orElseThrow());
		return otpRepository.findBy(user.id()).orElseThrow();
	}

	public OTP saveAndVerifyUser(RegistrationForm form) throws JsonProcessingException {
		OTP otp = saveUser(form);

		given().queryParam("otp", otp.otp()).when().patch("/uyol/auth/verification").then().assertThat()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());

		return otp;
	}

	public void removeUser(String email) {
		jetQuerious.write(DELETE_USER, email, email, email);
	}
}
