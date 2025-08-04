package org.project.domain.shared.util;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public class Utils {

    private Utils() {}

    public static <T> T required(String fieldName, T value) {
        if (value == null)
            throw new IllegalDomainArgumentException(fieldName + " must not be null");

        return value;
    }
}
