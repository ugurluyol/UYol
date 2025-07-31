package org.project.application.service;

import static org.project.application.util.RestUtil.responseException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.project.application.dto.auth.LoginForm;
import org.project.application.dto.auth.RegistrationForm;
import org.project.application.dto.auth.Token;
import org.project.application.dto.auth.Tokens;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.shared.exceptions.IllegalDomainStateException;
import org.project.domain.user.entities.OTP;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.OTPRepository;
import org.project.domain.user.repositories.UserRepository;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.Password;
import org.project.domain.user.value_objects.PersonalData;
import org.project.domain.user.value_objects.Phone;
import org.project.domain.user.value_objects.RefreshToken;
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
	}

	public User login(LoginForm form) {
		User user = verifiedUserBy(form.identifier());
		String hashedPassword = user.personalData().password().orElseThrow(
				() -> responseException(Response.Status.FORBIDDEN, "User password is missing"));

		if (!passwordEncoder.verify(form.password(), hashedPassword))
			throw responseException(Response.Status.UNAUTHORIZED, "Invalid credentials");

		return user;
	}

	public void resendOTP(String identifier) {
		User user = verifiedUserBy(identifier);
		OTP otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));

		otpRepository.save(otp)
				.orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to save OTP"));

		user.incrementCounter();

		userRepository.updateCounter(user).orElseThrow(() -> {
			otpRepository.remove(otp).ifFailure(throwable -> Log.error("Can't remove OTP", throwable));
			return responseException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to update user counter");
		});

		user.personalData().phone().map(Phone::new).ifPresent(phone -> phoneInteractionService.sendOTP(phone, otp));

		user.personalData().email().map(Email::new)
				.ifPresent(email -> emailInteractionService.sendSoftVerificationMessage(email));
	}

	public void verification(String receivedOTP) {
		try {
			OTP.validate(receivedOTP);
			OTP otp = otpRepository.findBy(receivedOTP)
					.orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "OTP not found."));
			User user = userRepository.findBy(otp.userID()).orElseThrow();

			if (user.isVerified())
				throw responseException(Response.Status.BAD_REQUEST, "User already verified.");

			if (otp.isExpired())
				throw responseException(Response.Status.GONE, "OTP is gone.");

			otp.confirm();
			otpRepository.updateConfirmation(otp)
					.orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
							"Unable to confirm you account at the moment. Please try again later."));

			user.enable();
			userRepository.updateVerification(user)
					.orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
							"Unable to update your verification status at the moment. Please try again later."));
		} catch (IllegalDomainStateException e) {
			throw responseException(Response.Status.FORBIDDEN, e.getMessage());
		}
	}

	public Token refreshToken(String refreshToken) {
		if (refreshToken == null)
			throw responseException(Response.Status.BAD_REQUEST, "Refresh token can`t be null");

		RefreshToken foundedPairResult = userRepository.findRefreshToken(refreshToken)
				.orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "This refresh token is not found."));

		long tokenExpirationDate = jwtUtility.parse(foundedPairResult.refreshToken())
				.orElseThrow(
						() -> responseException(Response.Status.BAD_REQUEST, "Something went wrong, try again later."))
				.getExpirationTime();

		var tokenExpiration = LocalDateTime.ofEpochSecond(tokenExpirationDate, 0, ZoneOffset.UTC);

		if (LocalDateTime.now(ZoneOffset.UTC).isAfter(tokenExpiration))
			throw responseException(Response.Status.BAD_REQUEST, "Refresh token is expired, you need to login.");

		final User user = userRepository.findBy(foundedPairResult.userID()).orElseThrow();

		String token = jwtUtility.generateToken(user);
		return new Token(token);
	}

	public Tokens twoFactorAuth(String receivedOTP) {
		try {
			OTP.validate(receivedOTP);
			OTP otp = otpRepository.findBy(receivedOTP)
					.orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "OTP not found."));
			User user = userRepository.findBy(otp.userID()).orElseThrow();

			if (!user.canLogin())
				throw responseException(Response.Status.FORBIDDEN,
						"You can`t login with unverified or banned account.");

			if (!user.is2FAEnabled()) {
				Log.info("Two factor authentication is enabled and verified for user.");
				user.enable2FA();
				userRepository.update2FA(user)
						.orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
								"Unable to enable your account 2FA at the moment. Please try again later."));
			}

			Tokens tokens = generateTokens(user);
			userRepository.saveRefreshToken(new RefreshToken(user.id(), tokens.refreshToken()))
					.orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
							"Unable to authenticate your account at the moment. Please try again later."));
			return tokens;
		} catch (IllegalDomainStateException e) {
			throw responseException(Response.Status.FORBIDDEN, e.getMessage());
		}
	}

	public void enable2FA(LoginForm loginForm) {
		if (loginForm == null)
			throw responseException(Response.Status.BAD_REQUEST, "Please fill the login form.");

		User user = verifiedUserBy(loginForm.identifier());
		Password.validate(loginForm.password());

		if (!user.canLogin())
			throw responseException(Response.Status.FORBIDDEN, "You can`t login with unverified or banned account.");

		final boolean isValidPasswordProvided = passwordEncoder.verify(loginForm.password(),
				user.personalData().password().orElseThrow());
		if (!isValidPasswordProvided)
			throw responseException(Response.Status.BAD_REQUEST, "Password do not match.");

		if (otpRepository.contains(user.id()))
			throw responseException(Response.Status.BAD_REQUEST, "You can`t request 2FA activation twice");

		generateAndSendOTP(user);
	}

	private Result<User, Throwable> findUserByIdentifier(String identifier) {
		try {
			return userRepository.findBy(new Email(identifier));
		} catch (IllegalDomainArgumentException e) {
			return userRepository.findBy(new Phone(identifier));
		}
	}

	private User verifiedUserBy(String identifier) {
		Result<User, Throwable> result = findUserByIdentifier(identifier);

		if (result.isFailure())
			throw responseException(Response.Status.UNAUTHORIZED, "Invalid credentials");

		User user = result.get();
		if (!user.isVerified())
			throw responseException(Response.Status.FORBIDDEN, "Account not verified");

		return user;
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

		if (user.personalData().email().isPresent()) {
			emailInteractionService.sendOTP(otp, new Email(user.personalData().email().get()));
			return;
		}

		phoneInteractionService.sendOTP(new Phone(user.personalData().phone().orElseThrow()), otp);
	}

	private Tokens generateTokens(User user) {
		String token = jwtUtility.generateToken(user);
		String refreshToken = jwtUtility.generateRefreshToken(user);
		return new Tokens(token, refreshToken);
	}
}
