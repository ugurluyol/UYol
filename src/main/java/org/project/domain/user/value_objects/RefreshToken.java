package org.project.domain.user.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.util.UUID;

public record RefreshToken(UUID userID, String refreshToken) {

    public RefreshToken {
        if (userID == null || refreshToken == null)
            throw new IllegalDomainArgumentException("User id or refresh token is null");
    }
}
