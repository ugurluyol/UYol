package org.project.domain.user.value_objects;

import org.project.domain.shared.annotations.Nullable;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public final class PersonalData {
    private final String firstname;
    private final String surname;
    private final @Nullable String email;
    private final @Nullable String phone;
    private final LocalDate birthDate;
    private final String password;

    public PersonalData(
            String firstname,
            String surname,
            @Nullable String phone,
            @Nullable String password,
            String email,
            LocalDate birthDate) {

        if (phone == null && email == null)
            throw new IllegalDomainArgumentException("You need to specify phone or email.");

        Firstname.validate(firstname);
        Surname.validate(surname);
        if (phone != null) Phone.validate(phone);
        if (email != null) Email.validate(email);
        Birthdate.validate(birthDate);

        this.firstname = firstname;
        this.surname = surname;
        this.phone = phone;
        this.password = password;
        this.email = email;
        this.birthDate = birthDate;
    }

    public String firstname() {
        return firstname;
    }

    public String surname() {
        return surname;
    }

    public Optional<String> phone() {
        return Optional.ofNullable(phone);
    }

    public Optional<String> password() {
        return Optional.ofNullable(password);
    }

    public Optional<String> email() {
        return Optional.ofNullable(email);
    }

    public LocalDate birthDate() {
        return birthDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PersonalData that = (PersonalData) o;
        return Objects.equals(firstname, that.firstname) &&
                Objects.equals(surname, that.surname) &&
                Objects.equals(email, that.email) &&
                Objects.equals(birthDate, that.birthDate) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstname, surname, email, birthDate, phone, password);
    }
}