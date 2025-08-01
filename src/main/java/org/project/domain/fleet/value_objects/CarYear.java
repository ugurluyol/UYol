package org.project.domain.fleet.value_objects;

import java.time.Year;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record CarYear(int value) {
    public CarYear {
        int currentYear = Year.now().getValue();
        if (value < 1950 || value > currentYear)
            throw new IllegalDomainArgumentException("carYear must be between 1950 and " + currentYear);
    }
}
