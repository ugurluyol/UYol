package org.project.application.service;

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
        boolean is2faEnabled) {

    public static UserProfileDTO from(User user) {
        return new UserProfileDTO(
                user.personalData().firstname(),
                user.personalData().surname(),
                user.personalData().email().orElse(null),
                user.personalData().phone().orElse(null),
                user.personalData().birthDate(),
                user.isVerified(),
                user.is2FAEnabled()
        );
    }
}
