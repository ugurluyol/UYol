package org.project.domain.ride.value_object;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.util.ArrayList;
import java.util.List;

import static org.project.domain.shared.util.Utils.required;

public record Route(
    Location from,
    Location to,
    List<Location> stops
) {
    public Route {
        required("from", from);
        required("to", to);

        if (from.equals(to))
            throw new IllegalDomainArgumentException("Start and end locations must be different.");

        if (stops != null && (stops.contains(from) || stops.contains(to)))
            throw new IllegalArgumentException("Stops must not include start or end locations.");
    }

    public boolean isDirect() {
        return stops == null || stops.isEmpty();
    }

    public List<Location> stops() {
        return new ArrayList<>(stops);
    }

    public Route addStop(Location stop) {
        List<Location> updatedStops = new ArrayList<>(stops);
        updatedStops.add(stop);
        return new Route(from, to, updatedStops);
    }

    public Route removeStop(Location stop) {
        List<Location> updatedStops = new ArrayList<>(stops);
        updatedStops.remove(stop);
        return new Route(from, to, updatedStops);
    }
}
