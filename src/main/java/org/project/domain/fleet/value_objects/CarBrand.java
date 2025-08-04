package org.project.domain.fleet.value_objects;

import static org.project.domain.shared.util.Utils.required;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record CarBrand(String value) {

    public CarBrand {
        required("carBrand", value);

        if (value.length() > 64)
            throw new IllegalDomainArgumentException("carBrand is too long");
    }
}
