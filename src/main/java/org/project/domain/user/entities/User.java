package org.project.domain.user.entities;

import static org.project.domain.shared.util.Utils.required;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.project.domain.shared.annotations.Nullable;
import org.project.domain.shared.enumerations.UserRole;
import org.project.domain.shared.exceptions.IllegalDomainStateException;
import org.project.domain.user.exceptions.BannedUserException;
import org.project.domain.user.value_objects.AccountDates;
import org.project.domain.user.value_objects.KeyAndCounter;
import org.project.domain.user.value_objects.Password;
import org.project.domain.user.value_objects.PersonalData;
import org.project.domain.user.value_objects.ProfilePicture;

public class User {
    private final UUID id;
    private final UserRole userRole;
    private PersonalData personalData;
    private boolean isVerified;
    private boolean isBanned;
    private KeyAndCounter keyAndCounter;
    private AccountDates accountDates;
    private boolean is2FAEnabled;
    private @Nullable ProfilePicture profilePicture;

    private User(
            UUID id,
            PersonalData personalData,
            boolean isVerified,
            boolean isBanned,
            KeyAndCounter keyAndCounter,
            AccountDates accountDates,
            boolean is2FAEnabled) {

        if (isBanned)
            throw new BannedUserException(
                    "Access denied: this user account has been banned due to a violation of platform rules. Contact support for further assistance.");

        this.id = id;
        this.personalData = personalData;
        this.userRole = UserRole.USER;
        this.isVerified = isVerified;
        this.keyAndCounter = keyAndCounter;
        this.accountDates = accountDates;
        this.is2FAEnabled = is2FAEnabled;
    }

    public static User of(PersonalData personalData, String secretKey) {
        required("personalData", personalData);
        required("secretKey", secretKey);

        return new User(UUID.randomUUID(), personalData, false, false, new KeyAndCounter(secretKey, 0),
                AccountDates.defaultDates(), false);
    }

    public static User fromRepository(
            UUID id,
            PersonalData personalData,
            boolean isVerified,
            boolean isBanned,
            KeyAndCounter keyAndCounter,
            AccountDates accountDates,
            boolean is2FAEnabled) {

        return new User(id, personalData, isVerified, isBanned, keyAndCounter, accountDates, is2FAEnabled);
    }

    public UUID id() {
        return id;
    }

    public PersonalData personalData() {
        return personalData;
    }

    public UserRole role() {
        return userRole;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean is2FAEnabled() {
        return is2FAEnabled;
    }

    public void changePassword(Password newPassword) {
        required("newPassword", newPassword);
        if (!isVerified)
            throw new IllegalDomainStateException("You cannot change password on unverified.");
        if (isBanned)
            throw new IllegalDomainStateException("You cannot change password with banned account.");

        this.personalData = new PersonalData(
                personalData.firstname(),
                personalData.surname(),
                personalData.phone().orElse(null),
                newPassword.password(),
                personalData.email().orElse(null),
                personalData.birthDate());
    }

    public boolean isBanned() {
        return isBanned;
    }

    public KeyAndCounter keyAndCounter() {
        return keyAndCounter;
    }

    public AccountDates accountDates() {
        return accountDates;
    }

    public void incrementCounter() {
        this.keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter() + 1);
    }

    public void profilePicture(ProfilePicture profilePicture) {
        required("profilePicture", profilePicture);

        this.profilePicture = profilePicture;
    }

    public Optional<ProfilePicture> profilePicture() {
        return Optional.ofNullable(profilePicture);
    }

    public void enable() {
        verifyPotentialBan();
        if (isVerified)
            throw new IllegalDomainStateException("You can`t active already verified user.");
        if (keyAndCounter.counter() == 0)
            throw new IllegalDomainStateException(
                    "It is prohibited to activate an account that has not been verified.");

        this.isVerified = true;
        this.keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter());
    }

    public boolean canLogin() {
        return isVerified && !isBanned;
    }

    public void enable2FA() {
        verifyPotentialBan();
        if (!isVerified)
            throw new IllegalDomainStateException("You can`t enable 2FA on not verified account");
        if (keyAndCounter.counter() == 0 || keyAndCounter.counter() == 1)
            throw new IllegalDomainStateException("Counter need to be incremented");
        if (is2FAEnabled)
            throw new IllegalDomainStateException("You can`t activate 2FA twice");

        this.is2FAEnabled = true;
    }

    public void ban() {
        if (isBanned)
            throw new IllegalDomainStateException("You can`t ban already banned user.");

        this.isBanned = true;
    }

    private void verifyPotentialBan() {
        if (isBanned)
            throw new BannedUserException(
                    "Access denied: this user account has been banned due to a violation of platform rules. Contact support for further assistance.");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return isVerified == user.isVerified &&
                isBanned == user.isBanned &&
                is2FAEnabled == user.is2FAEnabled &&
                id.equals(user.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isVerified, isBanned, isVerified);
    }
}
