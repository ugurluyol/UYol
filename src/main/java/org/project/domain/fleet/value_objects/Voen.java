package org.project.domain.fleet.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.util.regex.Pattern;

import static org.project.domain.shared.util.Utils.required;

public record Voen(String value) {

    private static final Pattern VOEN_PATTERN = Pattern.compile("^(\\d{7}|\\d{10})$");

    public Voen {
        required("voen", value);

        if (value.isBlank())
            throw new IllegalDomainArgumentException("VOEN cannot be blank");

        if (value.length() > 10)
            throw new IllegalDomainArgumentException("VOEN must be less than 10 characters");

        if (!VOEN_PATTERN.matcher(value).matches())
            throw new IllegalDomainArgumentException("Invalid voen format: " + value);
    }
}
