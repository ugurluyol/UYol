package org.project.domain.user.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.time.LocalDate;
import java.time.Period;

public record Birthdate(LocalDate birthDate) {

    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 120;

    public Birthdate {
        validate(birthDate);
    }

    public static void validate(LocalDate birthDate) {
        if (birthDate == null) throw new IllegalDomainArgumentException("Birthdate is null");
        if (birthDate.isAfter(LocalDate.now())) throw new IllegalDomainArgumentException("Birthdate cannot be in the future");
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < MIN_AGE || age > MAX_AGE) throw new IllegalDomainArgumentException("Age must be between 18 and 120");
    }
}
