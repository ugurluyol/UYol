package org.project.domain.shared.value_objects;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record OwnerID(UUID value) {
    public OwnerID {
        required("ownerID", value);
    }

    public static OwnerID newID() {
        return new OwnerID(UUID.randomUUID());
    }

    public static OwnerID fromString(String ownerId) {
        return new OwnerID(UUID.fromString(ownerId));
    }
}
