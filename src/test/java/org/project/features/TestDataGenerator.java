package org.project.features;

import org.jetbrains.annotations.NotNull;
import org.project.application.dto.auth.RegistrationForm;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.value_objects.*;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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

    public static Driver driver() {
        return Driver.of(UserID.newID(), driverLicense());
    }

    public static Owner owner() {
        return Owner.of(UserID.newID(), voen());
    }

    public static Ride rideWithoutDelivery() {
        return Ride.of(
                generateRideOwner(),
                generateRoute(),
                generateRideTime(),
                generatePrice(),
                generateSeatMap(),
                generateRideDesc(),
                generateRideRules()
        );
    }

    public static Ride rideWithDelivery() {
        return Ride.of(
                generateRideOwner(),
                generateRoute(),
                generateRideTime(),
                generateSeatMap(),
                generatePrice(),
                generatePrice(),
                generateRideDesc(),
                generateRideRules()
        );
    }

    public static Route generateRoute() {
        Location from = new Location(
                "Start Point",
                randomInRange(-90.0, 90.0),
                randomInRange(-180.0, 180.0)
        );

        Location to;
        do {
            to = new Location(
                    "End Point",
                    randomInRange(-90.0, 90.0),
                    randomInRange(-180.0, 180.0)
            );
        } while (from.equals(to));

        return new Route(from, to);
    }

    public static Location generateLocation() {
        return new Location(faker.address().streetName(), randomInRange(-90.0, 90.0), randomInRange(-180.0, 180.0));
    }

    public static double randomInRange(double min, double max) {
        return min + (Math.random() * (max - min));
    }

    public static RideTime generateRideTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusHours(ThreadLocalRandom.current().nextInt(1, 25));
        LocalDateTime end = start.plusHours(ThreadLocalRandom.current().nextInt(1, 5));
        return new RideTime(start, end);
    }

    public static RideDesc generateRideDesc() {
        return new RideDesc(faker.lorem().characters(64));
    }

    public static Set<RideRule> generateRideRules() {
        Set<RideRule> rideRules = new HashSet<>();
        RideRule[] values = RideRule.values();
        for (int i = 0; i < 10; i++) {
            rideRules.add(values[ThreadLocalRandom.current().nextInt(values.length)]);
        }
        return rideRules;
    }

    public static Price generatePrice() {
        return new Price(BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(100_000L, 999999999999999999L)));
    }

    public static SeatMap generateSeatMap() {
        int seatCount = ThreadLocalRandom.current().nextInt(2, 65);
        List<SeatStatus> seats = new ArrayList<>(seatCount);
        seats.add(SeatStatus.DRIVER);
        seats.add(SeatStatus.EMPTY);
        seats.add(SeatStatus.EMPTY);

        SeatStatus[] nonDriverStatuses = getNonDriverStatuses();

        for (int i = 3; i < seatCount; i++) {
            seats.add(getRandomNonDriverStatus(nonDriverStatuses));
        }

        return new SeatMap(seats);
    }

    public static SeatStatus[] getNonDriverStatuses() {
        SeatStatus[] allStatuses = SeatStatus.values();
        SeatStatus[] nonDriverStatuses = new SeatStatus[allStatuses.length - 1];

        int index = 0;
        for (SeatStatus status : allStatuses) {
            if (status != SeatStatus.DRIVER) {
                nonDriverStatuses[index++] = status;
            }
        }
        return nonDriverStatuses;
    }

    public static SeatStatus getRandomNonDriverStatus(SeatStatus[] nonDriverStatuses) {
        return nonDriverStatuses[ThreadLocalRandom.current().nextInt(nonDriverStatuses.length)];
    }

    public static Car car() {
        return Car.of(UserID.newID(),
                generateLicensePlate(),
                generateCarBrand(),
                generateCarModel(),
                generateCarColor(),
                generateCarYear(),
                generateSeatCount());
    }

    public static Voen voen() {
        int length = faker.random().nextBoolean() ? 7 : 10;
        String voen = faker.number().digits(length);
        return new Voen(voen);
    }

    public static DriverLicense driverLicense() {
        return new DriverLicense(faker.numerify("## ## ######"));
    }

    public static @NotNull RideOwner generateRideOwner() {
        return new RideOwner(DriverID.newID(), OwnerID.newID());
    }

    public static @NotNull SeatCount generateSeatCount() {
        return new SeatCount(ThreadLocalRandom.current().nextInt(2, 10));
    }

    public static CarYear generateCarYear() {
        return new CarYear(ThreadLocalRandom.current().nextInt(2010, 2022));
    }

    public static @NotNull CarColor generateCarColor() {
        return new CarColor(faker.color().name());
    }

    public static LicensePlate generateLicensePlate() {
        int length = ThreadLocalRandom.current().nextInt(3, 13);
        String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int idx = ThreadLocalRandom.current().nextInt(allowed.length());
            sb.append(allowed.charAt(idx));
        }

        return new LicensePlate(sb.toString());
    }

    public static @NotNull CarModel generateCarModel() {
        return new CarModel(faker.vehicle().model());
    }

    public static CarBrand generateCarBrand() {
        return new CarBrand(faker.vehicle().manufacturer());
    }

    public static PersonalData personalData() {
        return new PersonalData(
                generateFirstname().firstname(),
                generateSurname().surname(),
                generatePhone().phoneNumber(),
                generatePassword().password(),
                generateEmail().email(),
                generateBirthdate().birthDate());
    }

    public static Firstname generateFirstname() {
        while (true) {
            var firstnameResult = Result.ofThrowable(() -> new Firstname(faker.name().firstName()));
            if (!firstnameResult.success())
                continue;
            return firstnameResult.value();
        }
    }

    public static Surname generateSurname() {
        while (true) {
            var surnameResult = Result.ofThrowable(() -> new Surname(faker.name().lastName()));
            if (!surnameResult.success())
                continue;
            return surnameResult.value();
        }
    }

    public static Phone generatePhone() {
        while (true) {
            var phoneResult = Result.ofThrowable(() -> new Phone(faker.phoneNumber().phoneNumber()));
            if (!phoneResult.success())
                continue;
            return phoneResult.value();
        }
    }

    public static Email generateEmail() {
        while (true) {
            var emailResult = Result.ofThrowable(() -> new Email(faker.internet().emailAddress()));
            if (!emailResult.success())
                continue;
            return emailResult.value();
        }
    }

    public static Password generatePassword() {
        while (true) {
            var passwordResult = Result.ofThrowable(() -> new Password(faker.internet().password()));
            if (!passwordResult.success())
                continue;
            return passwordResult.value();
        }
    }

    public static Birthdate generateBirthdate() {
        while (true) {
            var birthdateResult = Result.ofThrowable(() -> new Birthdate(faker.timeAndDate().birthday(18, 120)));
            if (!birthdateResult.success())
                continue;
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
