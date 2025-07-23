package org.project.domain.user.exceptions;

import org.project.domain.shared.exceptions.DomainException;

public class UnableToLoginException extends DomainException {
    public UnableToLoginException(String message) {
        super(message);
    }
}
