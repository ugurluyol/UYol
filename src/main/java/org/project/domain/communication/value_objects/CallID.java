package org.project.domain.communication.value_objects;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record CallID(UUID value) {
    public CallID {
        required("callID", value);
    }

    public static CallID newID() {
        return new CallID(UUID.randomUUID());
    }

    public static CallID fromString(String value) {
        return new CallID(UUID.fromString(value));
    }
}
