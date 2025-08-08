package org.project.features;

import org.project.application.dto.auth.RegistrationForm;
import org.project.domain.shared.containers.Result;
import org.project.domain.user.entities.User;
import org.project.domain.user.value_objects.Birthdate;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.Firstname;
import org.project.domain.user.value_objects.Password;
import org.project.domain.user.value_objects.PersonalData;
import org.project.domain.user.value_objects.Phone;
import org.project.domain.user.value_objects.Surname;
import org.project.infrastructure.security.HOTPGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import net.datafaker.Faker;

@ApplicationScoped
public class TestDataGenerator {

    static final Faker faker = new Faker();

    static final HOTPGenerator hotpGenerator = new HOTPGenerator();

    public static String otp() {
        return hotpGenerator.generateHOTP(HOTPGenerator.generateSecretKey(), 0);
    }

    public static String otp(String key) {
        return hotpGenerator.generateHOTP(key, 0);
    }

    public static String otp(String key, int counter) {
        return hotpGenerator.generateHOTP(key, counter);
    }

    public static User user() {
        return User.of(personalData(), HOTPGenerator.generateSecretKey());
    }

    public static PersonalData personalData() {
        return new PersonalData(
                generateFirstname().firstname(),
                generateSurname().surname(),
                generatePhone().phoneNumber(),
                generatePassword().password(),
                generateEmail().email(),
                generateBirthdate().birthDate()
        );
    }

    public static Firstname generateFirstname() {
        while (true) {
            var firstnameResult = Result.ofThrowable(() -> new Firstname(faker.name().firstName()));
            if (!firstnameResult.success()) continue;
            return firstnameResult.value();
        }
    }

    public static Surname generateSurname() {
        while (true) {
            var surnameResult = Result.ofThrowable(() -> new Surname(faker.name().lastName()));
            if (!surnameResult.success()) continue;
            return surnameResult.value();
        }
    }

    public static Phone generatePhone() {
        while (true) {
            var phoneResult = Result.ofThrowable(() -> new Phone(faker.phoneNumber().phoneNumber()));
            if (!phoneResult.success()) continue;
            return phoneResult.value();
        }
    }

    public static Email generateEmail() {
        while (true) {
            var emailResult = Result.ofThrowable(() -> new Email(faker.internet().emailAddress()));
            if (!emailResult.success()) continue;
            return emailResult.value();
        }
    }

    public static Password generatePassword() {
        while (true) {
            var passwordResult = Result.ofThrowable(() -> new Password(faker.internet().password()));
            if (!passwordResult.success()) continue;
            return passwordResult.value();
        }
    }

    public static Birthdate generateBirthdate() {
        while (true) {
            var birthdateResult = Result.ofThrowable(() -> new Birthdate(faker.timeAndDate().birthday(18, 120)));
            if (!birthdateResult.success()) continue;
            return birthdateResult.value();
        }
    }

	public static RegistrationForm generateRegistrationForm() {
		String password = generatePassword().password();
		return new RegistrationForm(generateFirstname().firstname(), generateSurname().surname(),
				generatePhone().phoneNumber(), generateEmail().email(), password, password,
				generateBirthdate().birthDate());
	}

}
