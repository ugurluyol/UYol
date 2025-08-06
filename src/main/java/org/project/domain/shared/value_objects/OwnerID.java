package org.project.domain.shared.value_objects;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record OwnerID(UUID value) {
    public OwnerID {
        required("ownerID", value);
    }
}
