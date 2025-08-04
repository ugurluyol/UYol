package org.project.domain.fleet.value_objects;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record UserID(UUID value) {
    public UserID {
        required("value", value);
    }
}
