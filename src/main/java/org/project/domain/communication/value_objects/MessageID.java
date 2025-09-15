package org.project.domain.communication.value_objects;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record MessageID(UUID value) {
    public MessageID {
        required("messageID", value);
    }

    public static MessageID from(String value) {
        return new MessageID(UUID.fromString(value));
    }

    public static MessageID newID() {
        return new MessageID(UUID.randomUUID());
    }
}
