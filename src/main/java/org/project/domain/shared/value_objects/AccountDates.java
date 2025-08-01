package org.project.domain.shared.value_objects;

import java.time.LocalDateTime;

import static org.project.domain.shared.util.Utils.required;

public record AccountDates(LocalDateTime createdAt, LocalDateTime lastUpdated) {
    public AccountDates {
        required("createdAt", createdAt);
        required("lastUpdated", lastUpdated);
    }

    public static AccountDates defaultDates() {
        return new AccountDates(LocalDateTime.now(), LocalDateTime.now());
    }
}
