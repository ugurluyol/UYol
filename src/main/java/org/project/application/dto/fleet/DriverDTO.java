package org.project.application.dto.fleet;

import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.value_objects.NoRating;
import org.project.domain.fleet.value_objects.Rated;

public record DriverDTO(
        String driverID,
        int totalRides,
        int totalReviews,
        double averageScore
) {
    public static DriverDTO from(Driver driver) {
        int totalReviews;
        double averageScore;
        switch (driver.rating()) {
            case NoRating nr -> { totalReviews = 0; averageScore = 0; }
            case Rated r -> { totalReviews = r.totalReviews(); averageScore = r.average(); }
        }

        return new DriverDTO(
                driver.id().value().toString(),
                driver.rides().value(),
                totalReviews,
                averageScore
        );
    }
}
