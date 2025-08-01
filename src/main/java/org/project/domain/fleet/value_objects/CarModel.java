package org.project.domain.fleet.value_objects;

import static org.project.domain.shared.util.Utils.required;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record CarModel(String value) {

    public CarModel {
        required("carModel", value);

        if (value.length() > 64)
            throw new IllegalDomainArgumentException("carModel is too long");
    }
}
