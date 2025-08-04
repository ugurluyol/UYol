package org.project.domain.fleet.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.util.regex.Pattern;

import static org.project.domain.shared.util.Utils.required;

public record LicensePlate(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[A-Z0-9-]{3,12}$");

    public LicensePlate {
        required("licensePlate", value);

        if (value.length() > 12)
            throw new IllegalDomainArgumentException("licensePlate is too long");

        if (!PATTERN.matcher(value).matches())
            throw new IllegalDomainArgumentException("Invalid license plate: " + value);
    }
}
