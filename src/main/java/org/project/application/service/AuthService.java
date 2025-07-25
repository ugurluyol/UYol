package org.project.application.service;

import static org.project.application.util.RestUtil.responseException;

import java.util.Objects;

import org.project.application.dto.auth.RegistrationForm;
import org.project.domain.user.entities.OTP;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.OTPRepository;
import org.project.domain.user.repositories.UserRepository;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.Password;
import org.project.domain.user.value_objects.PersonalData;
import org.project.domain.user.value_objects.Phone;
import org.project.infrastructure.communication.EmailInteractionService;
import org.project.infrastructure.communication.PhoneInteractionService;
import org.project.infrastructure.security.HOTPGenerator;
import org.project.infrastructure.security.JWTUtility;
import org.project.infrastructure.security.PasswordEncoder;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthService {

	private final JWTUtility jwtUtility;

	private final HOTPGenerator hotpGenerator;

	private final OTPRepository otpRepository;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final EmailInteractionService emailInteractionService;

	private final PhoneInteractionService phoneInteractionService;

	AuthService(JWTUtility jwtUtility, UserRepository userRepository, OTPRepository otpRepository,
			EmailInteractionService emailInteractionService, PhoneInteractionService phoneInteractionService,
			PasswordEncoder passwordEncoder) {

		this.jwtUtility = jwtUtility;
		this.userRepository = userRepository;
		this.otpRepository = otpRepository;
		this.emailInteractionService = emailInteractionService;
		this.phoneInteractionService = phoneInteractionService;
		this.passwordEncoder = passwordEncoder;
		this.hotpGenerator = new HOTPGenerator();
	}

	public void registration(RegistrationForm registrationForm) {
		if (registrationForm == null)
			throw responseException(Response.Status.BAD_REQUEST, "Registration form is null");

		if (!Objects.equals(registrationForm.password(), registrationForm.passwordConfirmation())) {
			Log.errorf("Registration failure, passwords do not match");
			throw responseException(Response.Status.BAD_REQUEST, "Passwords do not match");
		}

		Password.validate(registrationForm.password());

		if (registrationForm.email() != null) {
			Email email = new Email(registrationForm.email());
			if (userRepository.isEmailExists(email))
				throw responseException(Response.Status.CONFLICT, "Email already used");
		}

		if (registrationForm.phone() != null) {
			Phone phone = new Phone(registrationForm.phone());
			if (userRepository.isPhoneExists(phone))
				throw responseException(Response.Status.CONFLICT, "Phone already used");
		}

		String encodedPassword = passwordEncoder.encode(registrationForm.password());

		PersonalData personalData = new PersonalData(
				registrationForm.firstname(),
				registrationForm.surname(),
				registrationForm.phone(),
				encodedPassword,
				registrationForm.email(),
				registrationForm.birthDate());
		String secretKey = HOTPGenerator.generateSecretKey();

		User user = User.of(personalData, secretKey);
		userRepository.save(user)
				.orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
						"Unable to register your account at the moment. Please try again later."));

		generateAndSendOTP(user);
		if (registrationForm.email() != null)
			emailInteractionService.sendSoftVerificationMessage(new Email(registrationForm.email()));
	}

	private void generateAndSendOTP(User user) {
		OTP otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

		otpRepository.save(otp).orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
				"Unable to process your request at the moment. Please try again."));

		user.incrementCounter();

		userRepository.updateCounter(user).orElseThrow(() -> {
			otpRepository.remove(otp).ifFailure(throwable -> Log.error("Can`t remove otp.", throwable));
			return responseException(Response.Status.INTERNAL_SERVER_ERROR,
					"Unable to process your request at the moment. Please try again.");
		});

		phoneInteractionService.sendOTP(new Phone(user.personalData().phone().orElseThrow()), otp);
	}
}
