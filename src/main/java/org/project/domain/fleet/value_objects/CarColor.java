package org.project.domain.fleet.value_objects;

import static org.project.domain.shared.util.Utils.required;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record CarColor(String value) {

    public CarColor {
        required("carColor", value);

        if (value.length() > 64)
            throw new IllegalDomainArgumentException("carColor is too long");
    }
}
