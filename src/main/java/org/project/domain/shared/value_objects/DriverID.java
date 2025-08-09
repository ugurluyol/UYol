package org.project.domain.shared.value_objects;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record DriverID(UUID value) {
    public DriverID {
        required("driverID", value);
    }

    public static DriverID newID() {
        return new DriverID(UUID.randomUUID());
    }
}
