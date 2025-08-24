package org.project.domain.fleet.value_objects;

public record NoRating() implements DriverRating {
    @Override
    public String toString() {
        return "No rating yet";
    }
}
