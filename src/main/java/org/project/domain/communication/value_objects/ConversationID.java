package org.project.domain.communication.value_objects;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record ConversationID(UUID value) {
    public ConversationID {
        required("conversationID", value);
    }

    public static ConversationID from(String value) {
        return new ConversationID(UUID.fromString(value));
    }
}
