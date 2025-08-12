package org.project.domain.ride.value_object;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import static org.project.domain.shared.util.Utils.required;

public record Route(Location from, Location to) {
    public Route {
        required("from", from);
        required("to", to);

        if (from.equals(to))
            throw new IllegalDomainArgumentException("Start and end locations must be different.");
    }
}

