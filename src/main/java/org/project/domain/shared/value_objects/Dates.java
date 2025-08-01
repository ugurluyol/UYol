package org.project.domain.shared.value_objects;

import java.time.LocalDateTime;

import static org.project.domain.shared.util.Utils.required;

public record Dates(LocalDateTime createdAt, LocalDateTime lastUpdated) {
    public Dates {
        required("createdAt", createdAt);
        required("lastUpdated", lastUpdated);
    }

    public static Dates defaultDates() {
        return new Dates(LocalDateTime.now(), LocalDateTime.now());
    }
}
