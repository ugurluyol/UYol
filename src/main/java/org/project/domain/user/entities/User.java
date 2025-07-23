package org.project.domain.user.entities;

import org.project.domain.shared.enumerations.UserRole;
import org.project.domain.shared.exceptions.IllegalDomainStateException;
import org.project.domain.user.exceptions.BannedUserException;
import org.project.domain.user.value_objects.*;

import java.util.Objects;
import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public class User {
    private final UUID id;
    private final UserRole userRole;
    private PersonalData personalData;
    private boolean isVerified;
    private boolean isBanned;
    private KeyAndCounter keyAndCounter;
    private AccountDates accountDates;

    private User(
            UUID id,
            PersonalData personalData,
            boolean isVerified,
            boolean isBanned,
            KeyAndCounter keyAndCounter,
            AccountDates accountDates) {

        if (isBanned) throw new BannedUserException("Access denied: this user account has been banned due to a violation of platform rules. Contact support for further assistance.");

        this.id = id;
        this.personalData = personalData;
        this.userRole = UserRole.USER;
        this.isVerified = isVerified;
        this.keyAndCounter = keyAndCounter;
        this.accountDates = accountDates;
    }

    public static User of(PersonalData personalData, String secretKey) {
        required("personalData", personalData);
        required("secretKey", secretKey);

        return new User(UUID.randomUUID(), personalData, false, false, new KeyAndCounter(secretKey, 0), AccountDates.defaultDates());
    }

    public static User fromRepository(
            UUID id,
            PersonalData personalData,
            boolean isVerified,
            boolean isBanned,
            KeyAndCounter keyAndCounter,
            AccountDates accountDates) {

        return new User(id, personalData, isVerified, isBanned, keyAndCounter, accountDates);
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

    public void enable() {
        verifyPotentialBan();
        if (isVerified)
            throw new IllegalDomainStateException("You can`t active already verified user.");
        if (keyAndCounter.counter() == 0)
            throw new IllegalDomainStateException("It is prohibited to activate an account that has not been verified.");

        this.isVerified = true;
        this.keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter());
    }

    public void ban() {
        if (isBanned)
            throw new IllegalDomainStateException("You can`t ban already banned user.");

        this.isBanned = true;
    }

    public boolean ableToLogin() {
        return isVerified && !isBanned;
    }

    private void verifyPotentialBan() {
        if (isBanned) throw new BannedUserException("Access denied: this user account has been banned due to a violation of platform rules. Contact support for further assistance.");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isVerified == user.isVerified &&
                isBanned == user.isBanned &&
                Objects.equals(id, user.id) &&
                Objects.equals(personalData, user.personalData) &&
                Objects.equals(keyAndCounter, user.keyAndCounter) &&
                Objects.equals(accountDates, user.accountDates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personalData, isVerified, isBanned, keyAndCounter, accountDates);
    }
}
