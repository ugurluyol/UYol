package org.project.application.dto.auth;

public record PasswordChangeForm(String otp, String newPassword, String passwordConfirmation) {
}
