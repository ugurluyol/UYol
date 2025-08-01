package org.project.domain.driver.value_objects;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record DriverID(UUID value) {
    public DriverID {
        required("driverID", value);
    }
}
