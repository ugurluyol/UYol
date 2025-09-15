package org.project.domain.communication.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import static org.project.domain.shared.util.Utils.required;

public record MessageContent(String value) {
    public MessageContent {
        required("messageContent", value);
        if (value.isBlank())
            throw new IllegalDomainArgumentException("Message content must not be blank.");

        if (value.length() > 256)
            throw new IllegalDomainArgumentException("Message content must not be longer than 256 characters.");
    }
}
