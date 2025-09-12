package org.project.application.dto.profile;

import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.value_objects.NoRating;
import org.project.domain.fleet.value_objects.Rated;
import org.project.domain.shared.annotations.Nullable;
import org.project.domain.user.entities.User;

import java.time.LocalDate;

public record UserProfileDTO(
        String firstname,
        String surname,
        @Nullable String email,
        @Nullable String phone,
        LocalDate birthDate,
        boolean isVerified,
        boolean is2faEnabled,
        @Nullable String driverLicense,
        @Nullable Integer totalRides,
        @Nullable Integer totalReviews,
        @Nullable Integer sumOfScores,
        @Nullable String voen) {

    public static UserProfileDTO from(User user) {
        return new UserProfileDTO(
                user.personalData().firstname(),
                user.personalData().surname(),
                user.personalData().email().orElse(null),
                user.personalData().phone().orElse(null),
                user.personalData().birthDate(),
                user.isVerified(),
                user.is2FAEnabled(),
                null,
                null,
                null,
                null,
                null
        );
    }

    public static UserProfileDTO from(User user, Driver driver) {
        int totalReviews = 0;
        int sumOfScores = 0;
        switch (driver.rating()) {
            case NoRating ignored -> {}
            case Rated rated -> {
                totalReviews = rated.totalReviews();
                sumOfScores = rated.sumOfScores();
            }
        }

        return new UserProfileDTO(
                user.personalData().firstname(),
                user.personalData().surname(),
                user.personalData().email().orElse(null),
                user.personalData().phone().orElse(null),
                user.personalData().birthDate(),
                user.isVerified(),
                user.is2FAEnabled(),
                driver.license().licenseNumber(),
                driver.rides().value(),
                totalReviews,
                sumOfScores,
                null
        );
    }

    public static UserProfileDTO from(User user, Owner owner) {
        return new UserProfileDTO(
                user.personalData().firstname(),
                user.personalData().surname(),
                user.personalData().email().orElse(null),
                user.personalData().phone().orElse(null),
                user.personalData().birthDate(),
                user.isVerified(),
                user.is2FAEnabled(),
                null,
                null,
                null,
                null,
                owner.voen().value()
        );
    }

    public static UserProfileDTO from(User user, @Nullable Driver driver, @Nullable Owner owner) {
        int totalReviews = 0;
        int sumOfScores = 0;
        Integer totalRides = null;
        String driverLicense = null;

        if (driver != null) {
            driverLicense = driver.license().licenseNumber();
            totalRides = driver.rides().value();
            switch (driver.rating()) {
                case Rated rated -> {
                    totalReviews = rated.totalReviews();
                    sumOfScores = rated.sumOfScores();
                }
                case NoRating ignored -> {}
            }
        }

        String voen = owner != null ? owner.voen().value() : null;

        return new UserProfileDTO(
                user.personalData().firstname(),
                user.personalData().surname(),
                user.personalData().email().orElse(null),
                user.personalData().phone().orElse(null),
                user.personalData().birthDate(),
                user.isVerified(),
                user.is2FAEnabled(),
                driverLicense,
                totalRides,
                totalReviews,
                sumOfScores,
                voen
        );
    }
}
