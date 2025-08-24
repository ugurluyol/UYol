package org.project.domain.fleet.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record TotalRides(int value) {
    public TotalRides {
        if (value < 0)
            throw new IllegalDomainArgumentException("Total Rides should not be bellow zero");
    }

    public static TotalRides defaultRides() {
        return new TotalRides(0);
    }

    public TotalRides newRide() {
        return new TotalRides(value + 1);
    }
}
