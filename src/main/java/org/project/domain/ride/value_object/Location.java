package org.project.domain.ride.value_object;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record Location(String description, double latitude, double longitude) {

  public Location {
    if (description == null || description.isBlank())
      throw new IllegalDomainArgumentException("Description is required");

    if (latitude < -90.0 || latitude > 90.0)
      throw new IllegalDomainArgumentException("Latitude must be between -90 and 90");

    if (longitude < -180.0 || longitude > 180.0)
      throw new IllegalDomainArgumentException("Longitude must be between -180 and 180");
  }

  @Override
  public String toString() {
    return description + " (" + latitude + ", " + longitude + ")";
  }
}
